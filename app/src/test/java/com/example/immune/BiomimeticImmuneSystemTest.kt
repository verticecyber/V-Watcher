package com.example.immune

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.communication.DefaultCommunicationChannel
import com.example.communication.ExecutionStatus
import com.example.memory.LocalImmuneMemoryRepository
import com.example.model.CaseStatus
import com.example.model.ImmuneCellState
import com.example.model.ImmuneMemoryPattern
import com.example.reasoning.DeterministicBackend
import com.example.reasoning.GeminiNanoBackend
import com.example.reasoning.GemmaBackend
import com.example.reasoning.ReasoningRouter
import com.example.reasoning.ResourceGuardrails
import com.example.sentinel.AndroidSentinel
import com.example.sentinel.CanonicalObservation
import com.example.telemetry.BatteryTelemetry
import com.example.telemetry.NetworkTelemetry
import com.example.telemetry.ResourceTelemetry
import com.example.telemetry.SystemStateTelemetry
import com.example.telemetry.TelemetryAvailability
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.UUID

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class BiomimeticImmuneSystemTest {

  private lateinit var context: Context
  private lateinit var communicationChannel: DefaultCommunicationChannel
  private lateinit var memoryRepo: LocalImmuneMemoryRepository
  private lateinit var realityBoundary: AndroidRealityBoundary
  private lateinit var sentinel: AndroidSentinel

  @Before
  fun setUp() {
    context = ApplicationProvider.getApplicationContext()
    val nano = GeminiNanoBackend(context)
    val gemma = GemmaBackend(context)
    val deterministic = DeterministicBackend()
    val guardrails = ResourceGuardrails()
    val router = ReasoningRouter(nano, gemma, deterministic, guardrails)
    communicationChannel = DefaultCommunicationChannel(router)
    memoryRepo = LocalImmuneMemoryRepository(context)
    realityBoundary = AndroidRealityBoundary(context)
    sentinel = AndroidSentinel(context)
  }

  // ==========================================
  // LAYER A: COMPONENT-LEVEL TESTS
  // ==========================================

  @Test
  fun prrCell_detectsThermalAndMemoryDangerPatterns() = runBlocking {
    val prr = PatternRecognitionCell()
    val baseObs = sentinel.observeNow()

    // Stress observation: 46°C battery + critical memory
    val stressedObs = baseObs.copy(
      battery = baseObs.battery.copy(
        value = baseObs.battery.value.copy(temperatureCelsius = 46.5f)
      ),
      resources = baseObs.resources.copy(
        value = baseObs.resources.value.copy(
          isLowMemory = true,
          availableMemBytes = 120L * 1024 * 1024
        )
      )
    )

    val evidence = prr.scan(stressedObs)
    assertTrue("Evidence should be detected for thermal and memory strain", evidence.isNotEmpty())
    assertTrue(evidence.any { it.signalType == "THERMAL_ELEVATION" })
    assertTrue(evidence.any { it.signalType == "RESOURCE_DEPLETION" })
    assertEquals(ImmuneCellState.INVESTIGATING, prr.state)
    assertTrue(prr.eventsHandled >= 1)
  }

  @Test
  fun neutrophilCell_executesRapidAcuteContainment() {
    val neutrophil = NeutrophilCell(realityBoundary)
    val evidence = listOf(
      EvidenceItem(
        id = "ev_test_1",
        providerId = "BatteryProvider",
        signalType = "THERMAL_ELEVATION",
        description = "Battery temperature 46.5°C exceeded safety ceiling",
        confidence = 95
      )
    )

    val result = neutrophil.respondToAcuteStress("INC-1001", evidence)
    assertNotNull(result)
    assertEquals(ActionExecutionStatus.EXECUTED, result?.status)
    assertEquals("THROTTLE_INTERNAL_INFERENCE", result?.targetAction)
    assertEquals(ImmuneCellState.ACTIVE, neutrophil.state)
    assertTrue(neutrophil.eventsHandled >= 1)
  }

  @Test
  fun macrophageCell_scavengesAndReclaimsCacheMemory() {
    val macrophage = MacrophageCell(realityBoundary)
    val result = macrophage.scavengeAndRecycle("INC-1002")

    assertEquals(ActionExecutionStatus.EXECUTED, result.status)
    assertEquals("RECLAIM_INTERNAL_CACHE", result.targetAction)
    assertTrue(macrophage.eventsHandled >= 1)
    assertTrue(macrophage.totalBytesReclaimed >= 0)
    assertEquals(ImmuneCellState.RECOVERING, macrophage.state)
  }

  @Test
  fun dendriticCell_assemblesAntigenPresentationAndDispatchesToReasoning() = runBlocking {
    val dendritic = DendriticCell(communicationChannel)
    val baseObs = sentinel.observeNow()
    val evidence = listOf(
      EvidenceItem(
        id = "ev_dendritic_1",
        providerId = "NetworkProvider",
        signalType = "PORTAL_OR_INTERCEPTION",
        description = "Unvalidated portal detected",
        confidence = 90
      )
    )

    val presentation = dendritic.assembleAntigenPresentation(
      incidentId = "INC-1003",
      observation = baseObs,
      evidence = evidence,
      cellsInvolved = listOf(AgentRole.PRR, AgentRole.DENDRITIC)
    )

    assertEquals("INC-1003", presentation.incidentId)
    assertEquals(1, presentation.evidenceRefs.size)
    assertNotNull(presentation.correlationId)

    val response = dendritic.presentToReasoningSubstrate(presentation, baseObs)
    assertNotNull(response)
    assertEquals(ExecutionStatus.FALLBACK, response.executionStatus) // Deterministic fallback verified
    assertEquals("DETERMINISTIC", response.actualBackendUsed)
    assertEquals(ImmuneCellState.ACTIVE, dendritic.state)
  }

  @Test
  fun naturalKillerCell_isolatesInternalSubsystemsAndRecommendsSettingsForExternal() {
    val nk = NaturalKillerCell(realityBoundary)

    // Test 1: Internal component aberrant
    val internalResult = nk.inspectComponentIntegrity(
      componentName = "EXPERIMENTAL_WORKER",
      isHealthy = false,
      isVWatcherOwned = true,
      incidentId = "INC-1004"
    )
    assertNotNull(internalResult)
    assertEquals(ActionExecutionStatus.EXECUTED, internalResult?.status)
    assertEquals("ISOLATE_INTERNAL_SUBSYSTEM", internalResult?.targetAction)

    // Test 2: External package aberrant - NK cell must NOT claim to kill it, acknowledges unavailable and provides Settings reference
    val externalResult = nk.inspectComponentIntegrity(
      componentName = "com.thirdparty.malicious",
      isHealthy = false,
      isVWatcherOwned = false,
      incidentId = "INC-1005"
    )
    assertNotNull(externalResult)
    assertEquals(ActionExecutionStatus.UNAVAILABLE, externalResult?.status)
    assertEquals("KILL_EXTERNAL_PROCESS", externalResult?.targetAction)
    assertTrue(externalResult!!.platformReason.contains("sandbox"))
  }

  @Test
  fun bCell_neutralizesKnownBenignSignaturesImmediately() {
    val bCell = BCell()
    val knownPatterns = listOf(
      ImmuneMemoryPattern(
        id = "pat_1",
        patternCode = "MEM-PORTAL",
        name = "Captive Portal Check",
        category = "Network",
        observedCount = 5,
        lastSeen = "Yesterday",
        typicalResponse = "Allow",
        confidenceScore = "99%",
        description = "Verified captive portal transition on Wi-Fi login",
        causalImpact = "Benign"
      )
    )

    // Match known pattern
    val result = bCell.bindAntibody("Captive Portal Check", knownPatterns)
    assertTrue(result.isMatched)
    assertTrue(result.isBenign)
    assertEquals("MEM-PORTAL", result.patternCode)
    assertEquals(ImmuneCellState.ACTIVE, bCell.state)

    // Novel pattern
    val novelResult = bCell.bindAntibody("Unknown alien socket storm", knownPatterns)
    assertFalse(novelResult.isMatched)
  }

  @Test
  fun tHelperCoordinator_enforcesConvergingEvidenceThreshold() {
    val tHelper = THelperCoordinatorCell()

    // Single weak evidence (< 85% confidence) -> Escalation withheld
    val weakEvidence = listOf(
      EvidenceItem(
        id = "ev_weak",
        providerId = "BatteryProvider",
        signalType = "TRANSIENT_SPIKE",
        description = "Minor momentary temp blip",
        confidence = 70
      )
    )

    val planWeak = tHelper.coordinate(
      incidentId = "INC-1006",
      evidence = weakEvidence,
      isKnownBenignPattern = false,
      isSuppressedByRegulation = false
    )
    assertFalse("Escalation should NOT be justified on a single weak signal", planWeak.isEscalationJustified)

    // Converging strong evidence -> Escalation justified
    val strongEvidence = listOf(
      EvidenceItem("ev1", "BatteryProvider", "THERMAL_ELEVATION", "46C", 95),
      EvidenceItem("ev2", "DeviceResourceProvider", "RESOURCE_DEPLETION", "Low RAM", 90)
    )

    val planStrong = tHelper.coordinate(
      incidentId = "INC-1007",
      evidence = strongEvidence,
      isKnownBenignPattern = false,
      isSuppressedByRegulation = false
    )
    assertTrue("Escalation SHOULD be justified on converging multi-provider evidence", planStrong.isEscalationJustified)
    assertTrue(planStrong.recruitedAgents.contains(AgentRole.CYTOTOXIC))
  }

  @Test
  fun regulatoryTCell_enforcesCooldownAndSafetyCaps() {
    val reg = RegulatoryTCell()

    // 1st request permitted
    val firstCheck = reg.evaluateEscalationRequest(
      incidentId = "INC-1008",
      batteryPercent = 80,
      isDischarging = true,
      availableMemMb = 1200,
      isBusStorming = false,
      activeIncidentsCount = 0
    )
    assertTrue(firstCheck.isPermitted)

    // Immediate 2nd request throttled by cooldown
    val secondCheck = reg.evaluateEscalationRequest(
      incidentId = "INC-1009",
      batteryPercent = 80,
      isDischarging = true,
      availableMemMb = 1200,
      isBusStorming = false,
      activeIncidentsCount = 1
    )
    assertFalse("Rapid consecutive escalation must be throttled by cooldown", secondCheck.isPermitted)
    assertTrue(reg.totalVetoesIssued >= 1)
  }

  @Test
  fun resolutionCell_verifiesBaselineReturnBeforeCaseClosure() = runBlocking {
    val resolution = ResolutionCell()
    val baseObs = sentinel.observeNow()

    // Ensure baseline is strictly healthy
    val normalObs = baseObs.copy(
      battery = baseObs.battery.copy(
        value = baseObs.battery.value.copy(temperatureCelsius = 28.0f)
      ),
      resources = baseObs.resources.copy(
        value = baseObs.resources.value.copy(
          isLowMemory = false,
          availableMemBytes = 1024L * 1024 * 1024
        )
      )
    )

    // Case 1: Active evidence present -> Resolution refused
    val activeEvidence = listOf(EvidenceItem("ev1", "Prov", "SIG", "Active anomaly", 90))
    val attempt1 = resolution.verifyBaselineReturn(
      incidentId = "INC-1010",
      currentStatus = CaseStatus.CONTAINED,
      observation = normalObs,
      activeEvidence = activeEvidence,
      isCleanupVerified = true
    )
    assertFalse("Cannot resolve while active evidence signals persist", attempt1.canResolve)

    // Case 2: Clean signals, progressive transition from CONTAINED -> RECOVERING
    val attempt2 = resolution.verifyBaselineReturn(
      incidentId = "INC-1010",
      currentStatus = CaseStatus.CONTAINED,
      observation = normalObs,
      activeEvidence = emptyList(),
      isCleanupVerified = true
    )
    assertFalse(attempt2.canResolve)
    assertEquals(CaseStatus.RECOVERING, attempt2.newStatus)

    // Case 3: RECOVERING -> RESOLVED
    val attempt3 = resolution.verifyBaselineReturn(
      incidentId = "INC-1010",
      currentStatus = CaseStatus.RECOVERING,
      observation = normalObs,
      activeEvidence = emptyList(),
      isCleanupVerified = true
    )
    assertTrue("Physiological equilibrium verified: case resolves", attempt3.canResolve)
    assertEquals(CaseStatus.RESOLVED, attempt3.newStatus)
  }

  // ==========================================
  // LAYER B: REAL INTEGRATION PIPELINE TEST
  // ==========================================

  @Test
  fun realImmunePipeline_endToEndExecution() = runBlocking {
    val immuneSystem = BiomimeticImmuneSystem(context, communicationChannel, memoryRepo)

    // Step 1: Real Sentinel captures Real Telemetry
    val realObservation = sentinel.observeNow()
    assertNotNull(realObservation.observationId)
    assertEquals(6, realObservation.provenance.size)

    // Step 2: Immune System processes Observation through full Multi-Agent Loop
    val snapshot = immuneSystem.processObservation(realObservation)

    // Step 3: Verify all 12 immune cells are instantiated, mapped, and operating
    assertEquals(12, snapshot.cells.size)
    assertTrue(snapshot.cells.any { it.name.contains("SENTINEL") })
    assertTrue(snapshot.cells.any { it.name.contains("PRR") })
    assertTrue(snapshot.cells.any { it.name.contains("NEUTROPHIL") })
    assertTrue(snapshot.cells.any { it.name.contains("MACROPHAGE") })
    assertTrue(snapshot.cells.any { it.name.contains("DENDRITIC") })
    assertTrue(snapshot.cells.any { it.name.contains("NK") })
    assertTrue(snapshot.cells.any { it.name.contains("T-HELPER") })
    assertTrue(snapshot.cells.any { it.name.contains("B-CELL") })
    assertTrue(snapshot.cells.any { it.name.contains("CYTOTOXIC") })
    assertTrue(snapshot.cells.any { it.name.contains("REGULATORY") })
    assertTrue(snapshot.cells.any { it.name.contains("RESOLUTION") })
    assertTrue(snapshot.cells.any { it.name.contains("MEMORY") })

    // Step 4: Verify Homeostasis Evaluation
    assertNotNull(snapshot.homeostasis)
    assertNotNull(snapshot.homeostasis.macroState)
    assertTrue(snapshot.homeostasis.confidenceScore >= 50)

    // Step 5: Verify Immune Bus stats
    assertNotNull(snapshot.busStats)
    assertTrue(snapshot.busStats.totalMessagesDispatched >= 1)
    assertFalse(snapshot.busStats.activeStormDetected)
  }
}
