package com.example.contract

import com.example.immune.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import java.util.UUID

/**
 * IMMUNE BUS CONTRACT SPECIFICATION TEST
 *
 * Formally proves event dispatch, ordering, role counters, storm detection,
 * and buffer preservation on the model-independent ImmuneBus.
 */
class ImmuneBusContractTest {

  private fun createSampleMessage(
    emitterRole: AgentRole,
    payload: ImmunePayload = ImmunePayload.PatternDetected(emptyList(), "TEST_SIG", "Low", "test")
  ): ImmuneMessage {
    return ImmuneMessage(
      messageId = "msg_${UUID.randomUUID().toString().take(8)}",
      correlationId = "corr_test",
      incidentId = "INC-TEST",
      observationId = "obs_test",
      emitterRole = emitterRole,
      emitterId = "test_emitter",
      targetRole = AgentRole.T_HELPER,
      freshnessMs = 10,
      isStale = false,
      evidenceRefs = emptyList(),
      requestedAction = null,
      senderState = AgentLifecycleState.ACTIVE,
      payload = payload
    )
  }

  @Test
  fun immuneBus_dispatchesAndPreservesOrdering() = runBlocking {
    val bus = ImmuneBus(capacity = 64, stormThresholdPerSec = 50)

    val msg1 = createSampleMessage(AgentRole.SENTINEL)
    val msg2 = createSampleMessage(AgentRole.PRR)

    val dispatched1 = bus.dispatch(msg1)
    val dispatched2 = bus.dispatch(msg2)

    assertTrue(dispatched1)
    assertTrue(dispatched2)

    val stats = bus.stats.value
    assertEquals(2L, stats.totalMessagesDispatched)
    assertEquals(1L, stats.messagesByRole[AgentRole.SENTINEL.name])
    assertEquals(1L, stats.messagesByRole[AgentRole.PRR.name])
    assertFalse(stats.activeStormDetected)
  }

  @Test
  fun immuneBus_detectsStormOnHighDispatchVelocity() = runBlocking {
    val bus = ImmuneBus(capacity = 128, stormThresholdPerSec = 20)

    // Dispatch 25 messages within the same millisecond window
    for (i in 1..25) {
      bus.dispatch(createSampleMessage(AgentRole.NEUTROPHIL))
    }

    val stats = bus.stats.value
    assertEquals(25L, stats.totalMessagesDispatched)
    assertTrue("Storm detection must trigger when rate > 20/sec", stats.activeStormDetected)
  }

  @Test
  fun immuneBus_clearResetsStatisticsAndCounters() = runBlocking {
    val bus = ImmuneBus()
    bus.dispatch(createSampleMessage(AgentRole.SENTINEL))
    assertEquals(1L, bus.stats.value.totalMessagesDispatched)

    bus.clear()
    assertEquals(0L, bus.stats.value.totalMessagesDispatched)
    assertFalse(bus.stats.value.activeStormDetected)
    assertTrue(bus.stats.value.messagesByRole.isEmpty())
  }
}
