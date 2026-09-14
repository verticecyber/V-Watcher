package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.communication.*
import com.example.reasoning.*
import com.example.sentinel.AndroidSentinel
import com.example.sentinel.CanonicalObservation
import com.example.telemetry.TelemetryAvailability
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class HandoffFoundationTest {

  private val context: Context = ApplicationProvider.getApplicationContext()

  @Test
  fun sentinel_capturesCanonicalObservationWithProvenanceAndHealth() = runBlocking {
    val sentinel = AndroidSentinel(context)
    val obs = sentinel.observeNow()

    assertNotNull(obs.observationId)
    assertTrue(obs.timestamp > 0)
    assertNotNull(obs.battery)
    assertNotNull(obs.resources)
    assertNotNull(obs.network)
    assertNotNull(obs.inventory)
    assertNotNull(obs.usage)
    assertNotNull(obs.system)

    // Verify freshness
    assertNotNull(obs.freshness)
    assertFalse(obs.freshness.isStale)

    // Verify provenance contains all 6 providers
    assertEquals(6, obs.provenance.size)
    assertTrue(obs.provenance.any { it.providerId == "BatteryProvider" })
    assertTrue(obs.provenance.any { it.providerId == "DeviceResourceProvider" })

    // Verify provider status registry
    assertTrue(obs.providerStatus.containsKey("BatteryProvider"))
    assertTrue(obs.providerStatus.containsKey("SystemStateProvider"))
  }

  @Test
  fun reasoningRouter_fallsBackExplicitlyWhenNanoUnavailable() = runBlocking {
    val nano = GeminiNanoBackend(context)
    val gemma = GemmaBackend(context)
    val deterministic = DeterministicBackend()
    val guardrails = ResourceGuardrails()
    val router = ReasoningRouter(nano, gemma, deterministic, guardrails)

    val sentinel = AndroidSentinel(context)
    val obs = sentinel.observeNow()

    val request = ReasoningRequest(
      requestId = "req_test_1",
      correlationId = "corr_test_1",
      observation = obs,
      candidateAnomaly = null,
      preferredBackend = "GEMINI_NANO"
    )

    val response = router.routeAndExecute(request)

    // Explicit fallback verification
    assertEquals("GEMINI_NANO", response.selectedBackend)
    assertEquals("DETERMINISTIC", response.actualBackendUsed)
    assertEquals("deterministic", response.executionMode)
    assertEquals(ExecutionStatus.FALLBACK, response.executionStatus)
    assertEquals(FallbackReason.GEMINI_NANO_UNAVAILABLE, response.fallbackReason)
    assertNotNull(response.assessment)
  }

  @Test
  fun resourceGuardrails_suppressesInferenceOnCriticalDischargingBattery() = runBlocking {
    val nano = GeminiNanoBackend(context)
    val gemma = GemmaBackend(context)
    val deterministic = DeterministicBackend()
    val guardrails = ResourceGuardrails(criticalBatteryPercent = 15)
    val router = ReasoningRouter(nano, gemma, deterministic, guardrails)

    val sentinel = AndroidSentinel(context)
    val baseObs = sentinel.observeNow()

    // Create observation with critical battery (< 15% and discharging)
    val criticalBatteryObs = baseObs.copy(
      battery = baseObs.battery.copy(
        value = baseObs.battery.value.copy(
          levelPercent = 10,
          isCharging = false
        )
      )
    )

    val decision = guardrails.evaluate(criticalBatteryObs)
    assertTrue(decision is GuardrailDecision.Denied)
    assertEquals(FallbackReason.BATTERY_CRITICAL_DISCHARGING, (decision as GuardrailDecision.Denied).reason)
  }

  @Test
  fun communicationChannel_dispatchesTelemetryAndPreservesCorrelation() = runBlocking {
    val nano = GeminiNanoBackend(context)
    val gemma = GemmaBackend(context)
    val deterministic = DeterministicBackend()
    val guardrails = ResourceGuardrails()
    val router = ReasoningRouter(nano, gemma, deterministic, guardrails)
    val channel = DefaultCommunicationChannel(router)

    val sentinel = AndroidSentinel(context)
    val obs = sentinel.observeNow()

    channel.dispatchTelemetry(obs)
    assertEquals(1L, channel.state.value.totalTelemetryDispatched)
    assertEquals(obs.observationId, channel.state.value.lastTelemetryObservationId)

    val request = ReasoningRequest(
      requestId = "req_custom_id_42",
      correlationId = "corr_custom_session_99",
      observation = obs,
      candidateAnomaly = "High port socket burst"
    )

    val response = channel.sendReasoningRequest(request)
    assertEquals("req_custom_id_42", response.requestId)
    assertEquals("corr_custom_session_99", response.correlationId)
    assertEquals(1L, channel.state.value.totalReasoningRequests)
    assertNotNull(channel.state.value.lastReasoningResponse)
  }

  @Test
  fun deterministicBackend_neverDisguisesAsNeuralModel() = runBlocking {
    val deterministic = DeterministicBackend()
    val sentinel = AndroidSentinel(context)
    val obs = sentinel.observeNow()

    val request = ReasoningRequest(
      requestId = "req_baseline_test",
      correlationId = "corr_baseline",
      observation = obs
    )

    val result = deterministic.execute(request)
    assertTrue(result.success)
    assertEquals("deterministic", result.executionMode)
    assertFalse(result.provenance.isRealOnDeviceModel)
    assertEquals("Deterministic Clinical Baseline Rule Engine", result.provenance.modelIdentity)
  }
}
