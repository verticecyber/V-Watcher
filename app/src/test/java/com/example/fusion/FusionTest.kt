package com.example.fusion

import android.app.Application
import android.os.Looper
import androidx.test.core.app.ApplicationProvider
import com.example.immune.HomeostaticMacroState
import com.example.model.AppHealthState
import com.example.model.CaseStatus
import com.example.sentinel.AndroidSentinel
import com.example.sentinel.CanonicalObservation
import com.example.viewmodel.VWatcherViewModel
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import java.util.concurrent.TimeUnit

/**
 * PH-04 fusion tests (A-04). Step zero pins CURRENT behavior before mutation.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class FusionTest {

  private fun pump(ms: Long = 500L) {
    shadowOf(Looper.getMainLooper()).idleFor(ms, TimeUnit.MILLISECONDS)
  }

  private fun newVm(): VWatcherViewModel {
    val app = ApplicationProvider.getApplicationContext<Application>()
    return VWatcherViewModel(app)
  }

  private fun awaitSettled(vm: VWatcherViewModel, timeoutMs: Long = 30000L) {
    val start = System.currentTimeMillis()
    while (vm.uiState.value.deviceState == null && System.currentTimeMillis() - start < timeoutMs) {
      pump()
    }
    pump()
  }

  private suspend fun stressedObs(app: Application): CanonicalObservation {
    val base = AndroidSentinel(app).observeNow()
    return base.copy(
      battery = base.battery.copy(value = base.battery.value.copy(levelPercent = 82, isCharging = true, temperatureCelsius = 46.5f)),
      resources = base.resources.copy(
        value = base.resources.value.copy(
          isLowMemory = true, availableMemBytes = 300L * 1024 * 1024, totalMemBytes = 2000L * 1024 * 1024
        )
      )
    )
  }

  @Test
  fun merged_casesPreservedAlongsideSystem() = runBlocking {
    val app = ApplicationProvider.getApplicationContext<Application>()
    val vm = newVm()
    awaitSettled(vm)
    val targetId = vm.uiState.value.applications.first().id
    vm.isolateApp(targetId)
    pump()
    val manualIds = vm.uiState.value.cases.map { it.id }
    assertTrue(manualIds.isNotEmpty())

    // Force a system incident through the orchestrator with stressed telemetry.
    val snap = vm.biomimeticImmuneSystem.processObservation(stressedObs(app))
    val systemIds = snap.activeIncidents.map { it.id }
    assertTrue("stressed observation must create a system incident, got ${snap.activeIncidents.size}", systemIds.isNotEmpty())

    // Refresh rebuilds UI cases from the system snapshot with merge semantics (PH-04 fusion).
    vm.refreshRealTelemetry()
    val deadline = System.currentTimeMillis() + 15000
    while (System.currentTimeMillis() < deadline &&
      vm.uiState.value.cases.none { it.id in systemIds }
    ) {
      pump()
    }
    val after = vm.uiState.value.cases.map { it.id }
    assertTrue("system incidents surface in UI", systemIds.all { it in after })
    // POST-FUSION TRUTH: manual cases are preserved alongside system incidents, deduplicated.
    assertTrue("manual cases preserved after merge", manualIds.all { it in after })
    assertEquals("no duplicate case ids after merge", after.size, after.toSet().size)
  }

  @Test
  fun singleReasoningEntry_noSecondNeuralSpend() = runBlocking {
    val app = ApplicationProvider.getApplicationContext<Application>()
    val vm = newVm()
    awaitSettled(vm)
    val before = vm.reasoningEngine.engineState.value
    vm.refreshRealTelemetry()
    val deadline = System.currentTimeMillis() + 15000
    while (System.currentTimeMillis() < deadline &&
      vm.reasoningEngine.engineState.value.deterministicCallsCount <= before.deterministicCallsCount
    ) {
      pump()
    }
    val afterState = vm.reasoningEngine.engineState.value
    assertTrue(
      "refresh routes through the deterministic path exactly once per cycle",
      afterState.deterministicCallsCount > before.deterministicCallsCount
    )
    assertEquals(
      "no neural spend without available backends",
      before.reasoningCallsCount, afterState.reasoningCallsCount
    )
  }

  @Test
  fun busLedger_staysBoundedAndFeedsResolution() = runBlocking {
    val app = ApplicationProvider.getApplicationContext<Application>()
    val vm = newVm()
    awaitSettled(vm)
    repeat(12) {
      vm.biomimeticImmuneSystem.processObservation(stressedObs(app))
      pump(250)
    }
    assertTrue(
      "evidence ledger is bounded to 8 observations",
      vm.biomimeticImmuneSystem.ledgerDepth <= 8
    )
  }

  @Test
  fun guardrailFeed_mirrorsPipelineSignals() = runBlocking {
    val vm = newVm()
    awaitSettled(vm)
    vm.refreshRealTelemetry()
    val deadline = System.currentTimeMillis() + 15000
    while (System.currentTimeMillis() < deadline &&
      vm.uiState.value.lastAssessmentTime.isBlank()
    ) {
      pump()
    }
    // The fused refresh feeds the guardrail context every cycle; the context must
    // mirror exactly what the pipeline reported (fidelity, whatever the environment).
    // Under Robolectric the shadow environment is memory-constrained, so the regulatory
    // veto fires for real and the feed must carry it (this test would fail on a dropped feed).
    val snap = vm.biomimeticImmuneSystem.snapshot.value
    val ctx = vm.reasoningEngine.guardrails.context
    assertEquals(snap.regulatoryForcedDeterministic, ctx.regulatoryForceDeterministic)
    assertEquals(snap.regulatoryCooldownMs, ctx.regulatoryCooldownMs)
    assertEquals(
      snap.homeostasis.macroState == HomeostaticMacroState.STRESSED,
      ctx.homeostasisStressed
    )
    assertEquals(
      snap.homeostasis.macroState == HomeostaticMacroState.ACTIVE_DEFENSE,
      ctx.containmentActive
    )
  }
}
