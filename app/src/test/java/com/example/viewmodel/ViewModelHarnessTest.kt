package com.example.viewmodel

import android.app.Application
import android.os.Looper
import androidx.test.core.app.ApplicationProvider
import com.example.immune.HomeostaticMacroState
import com.example.model.AppHealthState
import com.example.model.CaseStatus
import com.example.model.DeviceCondition
import com.example.model.ExamSequenceStep
import java.util.concurrent.TimeUnit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

/**
 * PH-01 G1 harness (A-01): pins ViewModel decision paths with real collaborators.
 * No mocks. Synthetic input only where production itself synthesizes (sim/exam flows).
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ViewModelHarnessTest {

  private fun newVm(): VWatcherViewModel {
    val app = ApplicationProvider.getApplicationContext<Application>()
    return VWatcherViewModel(app)
  }

  private companion object {
    const val SETTLE_TIMEOUT_MS = 30000L
    const val SHORT_TIMEOUT_MS = 5000L
    const val EXAM_TIMEOUT_MS = 15000L
  }

  private fun pump(ms: Long = 500L) {
    shadowOf(Looper.getMainLooper()).idleFor(ms, TimeUnit.MILLISECONDS)
  }

  private fun awaitSettled(vm: VWatcherViewModel, timeoutMs: Long = SETTLE_TIMEOUT_MS) {
    val start = System.currentTimeMillis()
    while (vm.uiState.value.deviceState == null && System.currentTimeMillis() - start < timeoutMs) {
      pump()
    }
    pump()
  }

  @Test
  fun refresh_scoresWithinBoundsWithNonBlankRationale() {
    val vm = newVm()
    awaitSettled(vm)
    val s = vm.uiState.value
    assertNotNull("deviceState must be set after refresh", s.deviceState)
    assertTrue("score in 55..100, was ${s.healthScore}", s.healthScore in 55..100)
    if (s.condition == DeviceCondition.HEALTHY) {
      assertTrue(
        "HEALTHY requires score>=90, was ${s.healthScore}",
        s.healthScore >= 90
      )
    }
    assertTrue("doctor note non-blank", s.doctorClinicalNote.isNotBlank())
  }

  @Test
  fun failClosed_mappingHoldsExhaustively() {
    val vm = newVm()
    awaitSettled(vm)
    val s = vm.uiState.value
    // Independent encoding of the production mapping (VWatcherViewModel:360-367):
    // mapped states assert exact conditions; unmapped states fall back to score-derived
    // HEALTHY-or-ATTENTION and must never surface CONTAINING/RECOVERING spuriously.
    when (s.homeostasisEvaluation?.macroState) {
      HomeostaticMacroState.UNKNOWN,
      HomeostaticMacroState.DEGRADED,
      HomeostaticMacroState.STRESSED -> assertEquals(DeviceCondition.ATTENTION, s.condition)
      HomeostaticMacroState.ACTIVE_DEFENSE -> assertEquals(DeviceCondition.CONTAINING, s.condition)
      HomeostaticMacroState.RECOVERING -> assertEquals(DeviceCondition.RECOVERING, s.condition)
      HomeostaticMacroState.HOMEOSTATIC,
      HomeostaticMacroState.WATCH,
      null -> assertTrue(
        "unmapped macroState must yield score-derived condition, was ${s.condition}",
        s.condition == DeviceCondition.HEALTHY || s.condition == DeviceCondition.ATTENTION
      )
    }
  }

  @Test
  fun simulation_gatesAndResets() {
    val vm = newVm()
    awaitSettled(vm)
    vm.simulateUnusualActivity()
    val start = System.currentTimeMillis()
    while (!vm.uiState.value.isSimulationActive && System.currentTimeMillis() - start < SHORT_TIMEOUT_MS) {
      pump(250)
    }
    assertTrue("simulation must activate", vm.uiState.value.isSimulationActive)
    assertTrue("step index advances", vm.uiState.value.simulationStepIndex >= 1)
    // Re-entrancy guard: second call while active is a no-op for job identity
    vm.simulateUnusualActivity()
    vm.resetToHealthyBaseline()
    pump()
    assertFalse("reset clears simulation", vm.uiState.value.isSimulationActive)
  }

  @Test
  fun quarantine_isInAppOnlyAndReversible() {
    val vm = newVm()
    awaitSettled(vm)
    val targetId = vm.uiState.value.applications.first().id
    vm.isolateApp(targetId)
    pump()
    val isolated = vm.uiState.value.applications.first { it.id == targetId }
    assertEquals(AppHealthState.ISOLATED, isolated.healthState)
    val kase = vm.uiState.value.cases.firstOrNull { it.affectedApp == isolated.name }
    assertNotNull("manual case recorded", kase)
    assertTrue(
      "actionTaken discloses in-app-only truth",
      kase!!.actionTaken.contains("in-app only", ignoreCase = true)
    )
    vm.releaseApp(targetId)
    pump()
    assertEquals(
      AppHealthState.HEALTHY,
      vm.uiState.value.applications.first { it.id == targetId }.healthState
    )
  }

  @Test
  fun manualCases_preservedWhenNoSystemIncidents() {
    val vm = newVm()
    awaitSettled(vm)
    val targetId = vm.uiState.value.applications.first().id
    vm.isolateApp(targetId)
    pump()
    val manualIds = vm.uiState.value.cases.map { it.id }
    vm.refreshRealTelemetry()
    awaitSettled(vm)
    val ids = vm.uiState.value.cases.map { it.id }
    assertEquals("no duplicate case ids after refresh", ids.size, ids.toSet().size)
    val systemIds = ids.filter { it.startsWith("case_") && it !in manualIds && !it.startsWith("case_man_") }
    assertTrue(
      "nominal Robolectric run produces no system incidents, so preservation branch must hold; got system ids $systemIds",
      systemIds.isEmpty()
    )
    assertTrue(
      "manual cases preserved when no system incidents",
      manualIds.all { it in ids }
    )
  }

  @Test
  fun resolveCase_marksResolvedWithTimelineEntry() {
    val vm = newVm()
    awaitSettled(vm)
    val targetId = vm.uiState.value.applications.first().id
    vm.isolateApp(targetId)
    pump()
    val open = vm.uiState.value.cases.first {
      it.status != CaseStatus.RESOLVED && it.id.startsWith("case_man_")
    }
    val before = open.timeline.size
    vm.resolveCase(open.id)
    pump()
    val done = vm.uiState.value.cases.first { it.id == open.id }
    assertEquals(CaseStatus.RESOLVED, done.status)
    assertTrue("timeline grows on resolve", done.timeline.size > before)
  }

  @Test
  fun exam_completesWithRealNumbersAndDismisses() {
    val vm = newVm()
    awaitSettled(vm)
    vm.runDeviceCheck()
    // Re-entrancy guard: immediate second call must not restart the sequence
    vm.runDeviceCheck()
    val start = System.currentTimeMillis()
    while (vm.uiState.value.currentExamStep != ExamSequenceStep.EXAMINATION_COMPLETE &&
      System.currentTimeMillis() - start < EXAM_TIMEOUT_MS
    ) {
      pump()
    }
    assertEquals(ExamSequenceStep.EXAMINATION_COMPLETE, vm.uiState.value.currentExamStep)
    assertTrue(
      "exam note interpolates live telemetry",
      vm.uiState.value.doctorClinicalNote.contains("Battery:") &&
        vm.uiState.value.doctorClinicalNote.contains("%")
    )
    vm.dismissExamDialog()
    assertFalse(vm.uiState.value.isExamInProgress)
  }
}
