package com.example.uibinding

import com.example.immune.HomeostaticMacroState
import com.example.model.CaseStatus
import com.example.model.ExamCategory
import com.example.model.IncidentCase
import com.example.ui.screens.bindExamCategories
import com.example.ui.screens.deviceDataBadgeVisible
import com.example.ui.screens.examConditionLabel
import com.example.ui.screens.homeostasisConfidenceDisplay
import com.example.ui.screens.homeostasisReasonDisplay
import com.example.ui.screens.isolatedCaseCount
import com.example.ui.screens.macroStateDisplayName
import com.example.ui.screens.sentinelRunningLabel
import com.example.ui.screens.uncontainedCaseCount
import androidx.compose.material3.Text
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.example.model.ExamSequenceStep
import com.example.ui.screens.ExamProgressDialog
import com.example.viewmodel.VWatcherUiState
import org.junit.Rule
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers

/**
 * PH-05 UI binding tests (A-05): every listed claim ends BACKEND_BOUND,
 * CONDITIONALLY_TRUE, or REWORDED/REMOVED. Pure binding functions plus a
 * banned-literal tripwire over main sources.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class UiBindingTest {

  @get:Rule
  val composeRule = createComposeRule()

  @Test
  fun banner_gatedOnObservation() {
    assertFalse(deviceDataBadgeVisible(false))
    assertTrue(deviceDataBadgeVisible(true))
  }

  private fun kase(id: String, status: CaseStatus) = IncidentCase(
    id = id, caseCode = id, title = id, date = "Today", severity = "Mild",
    status = status, assessment = "", actionTaken = "", outcome = "",
    confidencePercent = 0, affectedApp = null, evidence = emptyList(), timeline = emptyList()
  )

  @Test
  fun macroState_neverDefaultsGreen() {
    assertEquals("EVALUATING", macroStateDisplayName(null))
    assertEquals("HOMEOSTATIC", macroStateDisplayName(HomeostaticMacroState.HOMEOSTATIC))
    assertEquals("UNKNOWN", macroStateDisplayName(HomeostaticMacroState.UNKNOWN))
  }

  @Test
  fun homeostasisPlaceholders_admitIgnorance() {
    assertEquals("Evaluation pending — no equilibrium claim made yet.", homeostasisReasonDisplay(null))
    assertEquals("—", homeostasisConfidenceDisplay(null))
    assertEquals("92%", homeostasisConfidenceDisplay(92))
  }

  @Test
  fun sentinelBadge_idleUnlessObservedRunning() {
    assertEquals("IDLE", sentinelRunningLabel(null))
  }

  @Test
  fun examCondition_followsScoreBands() {
    assertEquals("Healthy", examConditionLabel(97))
    assertEquals("Healthy", examConditionLabel(90))
    assertEquals("Attention", examConditionLabel(89))
  }

  @Test
  fun caseAccounting_noInflationNoHardcodedZero() {
    val cases = listOf(
      kase("a", CaseStatus.ISOLATED), kase("b", CaseStatus.RESOLVED),
      kase("c", CaseStatus.OBSERVING), kase("d", CaseStatus.RECOVERING)
    )
    assertEquals(1, isolatedCaseCount(cases))
    assertEquals(2, uncontainedCaseCount(cases))
    assertEquals(0, uncontainedCaseCount(emptyList()))
  }

  @Test
  fun categories_recomputedFromLiveState() {
    val base = listOf(
      ExamCategory("ec_apps", "APPLICATIONS", "Healthy", "seed", "seed", true, "seed"),
      ExamCategory("ec_network", "NETWORK", "Healthy", "seed", "seed", true, "seed"),
      ExamCategory("ec_immune", "IMMUNE MEMORY", "Healthy", "seed", "seed", true, "seed")
    )
    val ui = VWatcherUiState()
    val bound = bindExamCategories(base, ui).associateBy { it.id }
    assertTrue(bound["ec_apps"]!!.summary.contains("inventoried"))
    assertTrue(bound["ec_network"]!!.notes.contains("no per-app TLS verification"))
    assertTrue(bound["ec_immune"]!!.detailCountText.contains("nothing persisted"))
    assertEquals("Standby", bound["ec_network"]!!.status)
  }

  @Test
  fun examDialog_rendersBoundCondition() {
    composeRule.setContent {
      ExamProgressDialog(
        currentStep = ExamSequenceStep.EXAMINATION_COMPLETE,
        progressFloat = 1.0f,
        healthScore = 85,
        doctorSummary = "Attention required",
        onDismiss = {}
      )
    }
    composeRule.onNodeWithText("Condition: Attention", substring = true).assertIsDisplayed()
    composeRule.onNodeWithText("Condition: Healthy", substring = true).assertDoesNotExist()
  }

  @Test
  fun bannedLiterals_absentFromMainSources() {
    val roots: List<java.io.File> = listOf(java.io.File("src/main/java"), java.io.File("app/src/main/java"))
    val root: java.io.File = roots.firstOrNull { it.isDirectory }
      ?: throw AssertionError("no main source root found from working dir ${System.getProperty("user.dir")}")
    val banned = listOf(
      "Validated TLS conduit",
      "Certified User Application",
      "Verified System Service",
      "Signatures verified",
      "Integrity intact",
      "0 uncontained",
      "Clinical Quarantine",
      "compressed from 1.2s to 35ms",
      "calibrated sub-50ms",
      "< 0.4% / day",
      "<0.3%/day",
      "< 0.3% / day",
      "0.06% idle",
      "0.06% Idle",
      "Isolate & Decoy",
      "Suspicious Connection → Diverted",
      "TLS encrypted sessions",
      "certified bounds",
      "Condition: Healthy",
      "Zero telemetry or tokens leave",
      "Zero Exfiltration",
      "Zero user identifiers",
      "0 Cloud Leakage",
      "Equilibrium verified.",
      "confidenceScore ?: 98",
      "isRunning ?: true",
      "ifEmpty { \"18 MB\" }",
      // Repo seed (LocalImmuneMemoryRepository MEM-0035) keeps its own copy: out of PH-05
      // targets, recorded as residual. This scan covers viewmodel and ui only for that literal.
      "will execute in < 40ms",
      "Microphone and camera idle during standby"
    )
    val hits = mutableListOf<String>()
    root.let { java.nio.file.Files.walk(it.toPath()).use { stream ->
      stream.filter { p -> p.toString().endsWith(".kt") }.forEach { path ->
        val text = path.toFile().readText()
        banned.forEach { literal ->
          if (text.contains(literal)) hits += "${path.fileName}: $literal"
        }
      }
    } }
    assertTrue("banned overclaim literals still present: $hits", hits.isEmpty())
  }
}
