package com.example.communication

import com.example.reasoning.ReasoningRouter
import com.example.sentinel.CanonicalObservation
import com.example.sentinel.SentinelTelemetryEvent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

interface CommunicationChannel {
  val state: StateFlow<CommunicationChannelState>
  val telemetryEvents: SharedFlow<SentinelTelemetryEvent>
  val reasoningResponses: SharedFlow<ReasoningResponse>

  suspend fun dispatchTelemetry(event: SentinelTelemetryEvent)
  suspend fun dispatchTelemetry(observation: CanonicalObservation)
  suspend fun sendReasoningRequest(request: ReasoningRequest): ReasoningResponse
}

/**
 * Model-Independent Communication Conduit.
 * Strictly adheres to Section 5:
 * - Decoupled from specific model implementations
 * - Carries structured telemetry from Sentinel
 * - Routes reasoning requests to the underlying reasoning router
 * - Preserves correlation IDs, timestamps, execution status, and backend provenance
 */
class DefaultCommunicationChannel(
  private val router: ReasoningRouter
) : CommunicationChannel {

  private val _state = MutableStateFlow(CommunicationChannelState())
  override val state: StateFlow<CommunicationChannelState> = _state.asStateFlow()

  private val _telemetryEvents = MutableSharedFlow<SentinelTelemetryEvent>(replay = 1, extraBufferCapacity = 64)
  override val telemetryEvents: SharedFlow<SentinelTelemetryEvent> = _telemetryEvents.asSharedFlow()

  private val _reasoningResponses = MutableSharedFlow<ReasoningResponse>(replay = 1, extraBufferCapacity = 64)
  override val reasoningResponses: SharedFlow<ReasoningResponse> = _reasoningResponses.asSharedFlow()

  override suspend fun dispatchTelemetry(event: SentinelTelemetryEvent) {
    _state.update {
      it.copy(
        totalTelemetryDispatched = it.totalTelemetryDispatched + 1,
        lastTelemetryTimestamp = event.timestamp,
        lastTelemetryObservationId = event.observationId
      )
    }
    _telemetryEvents.emit(event)
  }

  override suspend fun dispatchTelemetry(observation: CanonicalObservation) {
    val event = SentinelTelemetryEvent(
      eventId = "evt_${System.currentTimeMillis()}",
      timestamp = observation.timestamp,
      observationId = observation.observationId,
      eventType = com.example.sentinel.SentinelEventType.SNAPSHOT_CAPTURED,
      description = "Telemetry dispatched to communication conduit",
      observation = observation
    )
    dispatchTelemetry(event)
  }

  override suspend fun sendReasoningRequest(request: ReasoningRequest): ReasoningResponse {
    _state.update {
      it.copy(
        totalReasoningRequests = it.totalReasoningRequests + 1,
        lastReasoningRequest = request
      )
    }

    return try {
      val response = router.routeAndExecute(request)
      _state.update {
        it.copy(
          lastReasoningResponse = response,
          lastError = if (response.executionStatus == ExecutionStatus.FAILED) response.errorMessage else null
        )
      }
      _reasoningResponses.emit(response)
      response
    } catch (e: Exception) {
      _state.update { it.copy(lastError = e.message) }
      throw e
    }
  }
}
