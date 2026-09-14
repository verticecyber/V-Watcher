package com.example.immune

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

data class ImmuneBusStats(
  val totalMessagesDispatched: Long = 0L,
  val droppedMessagesCount: Long = 0L,
  val lastMessageTimestamp: Long? = null,
  val activeStormDetected: Boolean = false,
  val currentDispatchesPerSecond: Double = 0.0,
  val messagesByRole: Map<String, Long> = emptyMap()
)

/**
 * Model-Independent Immune Event Bus.
 * Connects Sentinel observation stream and cooperating immune cells.
 * - Preserves event, agent, model, and action provenance
 * - Implements concurrency and storm protection
 * - Enforces bounded queues with backpressure to protect device resources
 */
class ImmuneBus(
  private val capacity: Int = 128,
  private val stormThresholdPerSec: Int = 30
) {
  private val _messages = MutableSharedFlow<ImmuneMessage>(
    replay = 20,
    extraBufferCapacity = capacity,
    onBufferOverflow = BufferOverflow.DROP_OLDEST
  )
  val messages: SharedFlow<ImmuneMessage> = _messages.asSharedFlow()

  private val _stats = MutableStateFlow(ImmuneBusStats())
  val stats: StateFlow<ImmuneBusStats> = _stats.asStateFlow()

  private val recentTimestamps = ArrayDeque<Long>()
  private val roleCounters = ConcurrentHashMap<String, AtomicLong>()
  private val mutex = Mutex()

  suspend fun dispatch(message: ImmuneMessage): Boolean = mutex.withLock {
    val now = System.currentTimeMillis()

    // Clean timestamps older than 1 second to calculate dispatch rate
    while (recentTimestamps.isNotEmpty() && (now - recentTimestamps.first()) > 1000L) {
      recentTimestamps.removeFirst()
    }
    recentTimestamps.addLast(now)

    val currentRate = recentTimestamps.size.toDouble()
    val isStorm = currentRate > stormThresholdPerSec

    val counter = roleCounters.computeIfAbsent(message.emitterRole.name) { AtomicLong(0L) }
    counter.incrementAndGet()

    val emitted = _messages.tryEmit(message)

    _stats.update { current ->
      current.copy(
        totalMessagesDispatched = current.totalMessagesDispatched + 1,
        droppedMessagesCount = if (emitted) current.droppedMessagesCount else current.droppedMessagesCount + 1,
        lastMessageTimestamp = now,
        activeStormDetected = isStorm,
        currentDispatchesPerSecond = currentRate,
        messagesByRole = roleCounters.mapValues { it.value.get() }
      )
    }

    if (!emitted) {
      _messages.emit(message) // suspend if buffer full
    }

    return emitted
  }

  fun clear() {
    recentTimestamps.clear()
    roleCounters.clear()
    _stats.value = ImmuneBusStats()
  }
}
