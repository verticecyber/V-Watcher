package com.example.immune

import android.content.Context
import com.example.communication.CommunicationChannel
import com.example.communication.ReasoningResponse
import com.example.memory.LocalImmuneMemoryRepository
import com.example.memory.SignatureProvenance
import com.example.model.CaseStatus
import com.example.model.ImmuneCell
import com.example.model.ImmuneMemoryPattern
import com.example.model.IncidentCase
import com.example.sentinel.CanonicalObservation
import com.example.telemetry.TelemetryAvailability
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

data class ImmuneSystemSnapshot(
  val timestamp: Long = System.currentTimeMillis(),
  val cells: List<ImmuneCell>,
  val homeostasis: HomeostasisEvaluation,
  val activeIncidents: List<IncidentCase>,
  val recentEvidence: List<EvidenceItem>,
  val lastReasoningResponse: ReasoningResponse?,
  val lastActionResult: ActionResult?,
  val busStats: ImmuneBusStats
)

/**
 * Coordinated Biomimetic Immune Multi-Agent System (V-Watcher).
 * Coordinates 11 cooperating immune agents over the model-independent ImmuneBus.
 * Zero-mock, grounded on real Android device telemetry and capabilities.
 */
class BiomimeticImmuneSystem(
  private val context: Context,
  private val communicationChannel: CommunicationChannel,
  val memoryRepository: LocalImmuneMemoryRepository
) {
  private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

  val bus = ImmuneBus()
  val realityBoundary = AndroidRealityBoundary(context)

  // 11 Cooperating Agents
  val prrCell = PatternRecognitionCell()
  val neutrophilCell = NeutrophilCell(realityBoundary)
  val macrophageCell = MacrophageCell(realityBoundary)
  val dendriticCell = DendriticCell(communicationChannel)
  val nkCell = NaturalKillerCell(realityBoundary)
  val tHelperCell = THelperCoordinatorCell()
  val bCell = BCell()
  val cytotoxicCell = CytotoxicEffectorCell(realityBoundary)
  val regulatoryCell = RegulatoryTCell()
  val resolutionCell = ResolutionCell()
  val sentinelCell = SentinelCell()
  val memoryCell = MemoryCell()

  private val _snapshot = MutableStateFlow(createInitialSnapshot())
  val snapshot: StateFlow<ImmuneSystemSnapshot> = _snapshot.asStateFlow()

  private val activeIncidentsMap = mutableMapOf<String, IncidentCase>()
  private var lastObservation: CanonicalObservation? = null

  init {
    // Listen to bus for real-time state tracking
    scope.launch {
      bus.messages.collect { message ->
        // Track message events
      }
    }
  }

  suspend fun processObservation(observation: CanonicalObservation): ImmuneSystemSnapshot {
    lastObservation = observation
    sentinelCell.observe(observation)

    // Step 1: Sentinel dispatches TelemetryArrival to ImmuneBus
    val telemetryMsg = ImmuneMessage(
      messageId = "msg_${UUID.randomUUID().toString().take(8)}",
      correlationId = "corr_${observation.observationId}",
      incidentId = null,
      observationId = observation.observationId,
      timestamp = observation.timestamp,
      emitterRole = AgentRole.SENTINEL,
      emitterId = "sentinel_01",
      targetRole = null,
      freshnessMs = observation.freshness.observationAgeMs,
      isStale = observation.freshness.isStale,
      evidenceRefs = emptyList(),
      requestedAction = null,
      senderState = AgentLifecycleState.ACTIVE,
      payload = ImmunePayload.TelemetryArrival(observation)
    )
    bus.dispatch(telemetryMsg)

    // Step 2: PRR Cell Scans for Danger Patterns
    val detectedEvidence = prrCell.scan(observation)

    if (detectedEvidence.isNotEmpty()) {
      bus.dispatch(
        ImmuneMessage(
          messageId = "msg_${UUID.randomUUID().toString().take(8)}",
          correlationId = "corr_${observation.observationId}",
          incidentId = null,
          observationId = observation.observationId,
          emitterRole = AgentRole.PRR,
          emitterId = "prr_01",
          targetRole = AgentRole.T_HELPER,
          freshnessMs = observation.freshness.observationAgeMs,
          isStale = false,
          evidenceRefs = detectedEvidence,
          requestedAction = "EVALUATE_DANGER_PATTERNS",
          senderState = AgentLifecycleState.ACTIVE,
          payload = ImmunePayload.PatternDetected(
            evidence = detectedEvidence,
            signalClass = detectedEvidence.first().signalType,
            severity = "Moderate",
            candidateAnomaly = detectedEvidence.first().description
          )
        )
      )
    }

    // Step 3: Regulatory T-Cell checks safety budgets & storm limits
    val batt = observation.battery.value
    val res = observation.resources.value
    val busStats = bus.stats.value

    val regCheck = regulatoryCell.evaluateEscalationRequest(
      incidentId = "inc_${observation.observationId}",
      batteryPercent = batt.levelPercent,
      isDischarging = !batt.isCharging,
      availableMemMb = res.availableMemMb,
      isBusStorming = busStats.activeStormDetected,
      activeIncidentsCount = activeIncidentsMap.size
    )

    // Step 4: Neutrophil Rapid Response if acute stress
    var lastActionRes: ActionResult? = null
    if (detectedEvidence.isNotEmpty()) {
      lastActionRes = neutrophilCell.respondToAcuteStress(
        incidentId = "inc_${observation.observationId}",
        evidence = detectedEvidence
      )
    }

    // Step 5: B-Cell Memory Binding Check
    val knownPatterns = memoryRepository.patterns.value
    val binding = if (detectedEvidence.isNotEmpty()) {
      bCell.bindAntibody(detectedEvidence.first().description, knownPatterns)
    } else {
      bCell.bindAntibody("", knownPatterns)
    }

    // Step 6: T-Helper Coordination & Escalation Arbitration
    val coordPlan = tHelperCell.coordinate(
      incidentId = "inc_${observation.observationId}",
      evidence = detectedEvidence,
      isKnownBenignPattern = binding.isMatched && binding.isBenign,
      isSuppressedByRegulation = !regCheck.isPermitted
    )

    var lastReasoningResp: ReasoningResponse? = null

    // Step 7: Dendritic Antigen Presentation & Reasoning (if escalation justified)
    if (coordPlan.isEscalationJustified && detectedEvidence.isNotEmpty()) {
      val incidentId = "INC-${System.currentTimeMillis().toString().takeLast(4)}"
      val presentation = dendriticCell.assembleAntigenPresentation(
        incidentId = incidentId,
        observation = observation,
        evidence = detectedEvidence,
        cellsInvolved = listOf(AgentRole.PRR, AgentRole.DENDRITIC, AgentRole.T_HELPER)
      )

      lastReasoningResp = dendriticCell.presentToReasoningSubstrate(presentation, observation)

      // Step 8: Cytotoxic Effector Execution
      if (coordPlan.recommendedEffectorAction != null && lastReasoningResp.assessment.confidence >= 80) {
        lastActionRes = cytotoxicCell.executeAuthorizedAction(
          incidentId = incidentId,
          actionName = coordPlan.recommendedEffectorAction,
          evidence = detectedEvidence,
          isFresh = !observation.freshness.isStale,
          confidenceScore = lastReasoningResp.assessment.confidence,
          parameters = mapOf("subsystem" to "EXPERIMENTAL_SOCKETS")
        )
      }

      // Record or update active incident
      val newCase = IncidentCase(
        id = incidentId,
        caseCode = incidentId,
        title = detectedEvidence.first().signalType,
        date = "Today",
        severity = lastReasoningResp.assessment.severity,
        status = CaseStatus.CONTAINED,
        assessment = lastReasoningResp.assessment.rationale,
        actionTaken = lastActionRes?.platformReason ?: "Under clinical observation",
        outcome = "Membrane contained",
        confidencePercent = lastReasoningResp.assessment.confidence,
        affectedApp = null,
        evidence = detectedEvidence.map { it.description },
        timeline = emptyList()
      )
      activeIncidentsMap[incidentId] = newCase
      memoryRepository.addCase(newCase)
    }

    // Step 9: Macrophage Scavenging & Cache Cleanup
    if (res.isLowMemory || res.availableMemMb < 250) {
      lastActionRes = macrophageCell.scavengeAndRecycle("inc_${observation.observationId}")
    }

    // Step 10: Resolution Cell verification
    for ((id, incCase) in activeIncidentsMap.toMap()) {
      val resVerification = resolutionCell.verifyBaselineReturn(
        incidentId = id,
        currentStatus = incCase.status,
        observation = observation,
        activeEvidence = detectedEvidence,
        isCleanupVerified = macrophageCell.totalBytesReclaimed > 0
      )

      if (resVerification.canResolve) {
        val resolved = incCase.copy(
          status = CaseStatus.RESOLVED,
          outcome = resVerification.verificationEvidence
        )
        activeIncidentsMap[id] = resolved
        memoryRepository.resolveCase(id)

        // Step 11: Memory Cell commits validated pattern with cryptographic provenance
        val validatedPattern = ImmuneMemoryPattern(
          id = "mem_${UUID.randomUUID().toString().take(6)}",
          patternCode = "MEM-${id.takeLast(4)}",
          name = incCase.title,
          category = "Cellular Homeostasis",
          observedCount = 1,
          lastSeen = "Just now",
          typicalResponse = incCase.actionTaken,
          confidenceScore = "${incCase.confidencePercent}%",
          description = incCase.assessment,
          causalImpact = "Resolved under clinical verification: ${resVerification.verificationEvidence}"
        )
        memoryCell.remember(validatedPattern)
        memoryRepository.addPattern(
          pattern = validatedPattern,
          provenance = SignatureProvenance(
            sourceProvider = "BiomimeticImmuneSystem",
            telemetryAvailability = TelemetryAvailability.AVAILABLE,
            confidenceScore = incCase.confidencePercent,
            evaluatedBy = "ResolutionCell & Macrophage",
            deterministicRuleTriggered = "Physiological Baseline Return",
            isSynthetic = false
          )
        )
      } else {
        activeIncidentsMap[id] = incCase.copy(status = resVerification.newStatus)
      }
    }

    // Step 12: Homeostasis Evaluation
    val activeCount = activeIncidentsMap.values.count { it.status != CaseStatus.RESOLVED }
    val containedCount = activeIncidentsMap.values.count { it.status == CaseStatus.CONTAINED }
    val resolvingCount = activeIncidentsMap.values.count { it.status == CaseStatus.RECOVERING }

    val homeostasis = HomeostasisEngine.evaluate(
      observation = observation,
      activeAgentsCount = getAllActiveCellsCount(),
      messagesPerSec = busStats.currentDispatchesPerSecond,
      isBusStorming = busStats.activeStormDetected,
      totalInferencesDispatched = dendriticCell.eventsHandled,
      activeIncidentsCount = activeCount,
      containedIncidentsCount = containedCount,
      resolvingIncidentsCount = resolvingCount,
      internalCorruptions = nkCell.internalCorruptionsDetected
    )

    val updatedSnapshot = ImmuneSystemSnapshot(
      timestamp = System.currentTimeMillis(),
      cells = getAllCellsAsModels(),
      homeostasis = homeostasis,
      activeIncidents = activeIncidentsMap.values.toList(),
      recentEvidence = detectedEvidence,
      lastReasoningResponse = lastReasoningResp,
      lastActionResult = lastActionRes,
      busStats = bus.stats.value
    )

    _snapshot.value = updatedSnapshot
    return updatedSnapshot
  }

  fun getAllCellsAsModels(): List<ImmuneCell> {
    return listOf(
      sentinelCell.toModel(),
      prrCell.toModel(),
      neutrophilCell.toModel(),
      macrophageCell.toModel(),
      dendriticCell.toModel(),
      nkCell.toModel(),
      tHelperCell.toModel(),
      bCell.toModel(),
      cytotoxicCell.toModel(),
      regulatoryCell.toModel(),
      resolutionCell.toModel(),
      memoryCell.toModel()
    )
  }

  private fun getAllActiveCellsCount(): Int {
    return listOf(
      sentinelCell.state,
      prrCell.state,
      neutrophilCell.state,
      macrophageCell.state,
      dendriticCell.state,
      nkCell.state,
      tHelperCell.state,
      bCell.state,
      cytotoxicCell.state,
      regulatoryCell.state,
      resolutionCell.state,
      memoryCell.state
    ).count { it != com.example.model.ImmuneCellState.MONITORING && it != com.example.model.ImmuneCellState.IDLE }
  }

  private fun createInitialSnapshot(): ImmuneSystemSnapshot {
    return ImmuneSystemSnapshot(
      timestamp = System.currentTimeMillis(),
      cells = getAllCellsAsModels(),
      homeostasis = HomeostasisEngine.evaluate(
        observation = null,
        activeAgentsCount = 0,
        messagesPerSec = 0.0,
        isBusStorming = false,
        totalInferencesDispatched = 0,
        activeIncidentsCount = 0,
        containedIncidentsCount = 0,
        resolvingIncidentsCount = 0,
        internalCorruptions = 0
      ),
      activeIncidents = emptyList(),
      recentEvidence = emptyList(),
      lastReasoningResponse = null,
      lastActionResult = null,
      busStats = bus.stats.value
    )
  }
}
