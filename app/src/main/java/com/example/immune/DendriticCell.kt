package com.example.immune

import com.example.communication.CommunicationChannel
import com.example.communication.ReasoningRequest
import com.example.communication.ReasoningResponse
import com.example.model.ImmuneCell
import com.example.model.ImmuneCellState
import com.example.sentinel.CanonicalObservation
import java.util.UUID

/**
 * Dendritic Agent — Antigen Presentation / Context Assembly (Section 6).
 * Bridges innate observation and cognitive reasoning.
 * Assembles compact, evidence-grounded incident context and presents it
 * to the reasoning runtime (Gemini Nano, Gemma, or Deterministic).
 */
class DendriticCell(
  private val communicationChannel: CommunicationChannel
) {
  var state: ImmuneCellState = ImmuneCellState.MONITORING
    private set

  var eventsHandled: Int = 0
    private set

  var lastAction: String = "Monitoring biological signals for antigen presentation"
    private set

  var confidence: Int = 95
    private set

  var lastPresentation: AntigenPresentation? = null
    private set

  fun assembleAntigenPresentation(
    incidentId: String,
    observation: CanonicalObservation,
    evidence: List<EvidenceItem>,
    cellsInvolved: List<AgentRole>
  ): AntigenPresentation {
    eventsHandled++
    state = ImmuneCellState.INVESTIGATING

    val missingInfo = mutableListOf<String>()
    if (observation.battery.value.temperatureCelsius == 0f) {
      missingInfo.add("Battery temperature reading absent")
    }
    if (!observation.network.value.isValidated) {
      missingInfo.add("Network reachability unvalidated")
    }

    val provenanceList = observation.provenance.map { "${it.providerId} (${it.sourceSystem})" }

    val presentation = AntigenPresentation(
      incidentId = incidentId,
      observationId = observation.observationId,
      timestamp = System.currentTimeMillis(),
      evidenceRefs = evidence,
      sourceProvenance = provenanceList,
      freshnessMs = observation.freshness.observationAgeMs,
      patternSignals = evidence.map { "${it.signalType}: ${it.description}" },
      cellsInvolved = cellsInvolved,
      missingInformation = missingInfo,
      resourceState = "RAM: ${observation.resources.value.availableMemMb}MB free, Battery: ${observation.battery.value.levelPercent}%",
      candidateHypotheses = listOf(
        "Benign physiological adaptation to load",
        "Environmental network transition / portal",
        "Uncontained background activity"
      ),
      correlationId = "corr_${UUID.randomUUID().toString().take(8)}"
    )

    lastPresentation = presentation
    lastAction = "Assembled antigen presentation for incident $incidentId with ${evidence.size} verified evidence refs"
    return presentation
  }

  suspend fun presentToReasoningSubstrate(
    presentation: AntigenPresentation,
    observation: CanonicalObservation
  ): ReasoningResponse {
    state = ImmuneCellState.ASSESSING
    val promptInstruction = buildString {
      append("Clinical Device Immunologist Assessment:\n")
      append("Incident: ${presentation.incidentId}\n")
      append("Signals: ${presentation.patternSignals.joinToString("; ")}\n")
      append("Resource State: ${presentation.resourceState}\n")
      append("Provide classification and recommended non-destructive clinical action.")
    }

    val request = ReasoningRequest(
      requestId = "req_${UUID.randomUUID().toString().take(8)}",
      correlationId = presentation.correlationId,
      timestamp = System.currentTimeMillis(),
      observation = observation,
      candidateAnomaly = presentation.patternSignals.firstOrNull(),
      promptInstruction = promptInstruction
    )

    val response = communicationChannel.sendReasoningRequest(request)
    state = ImmuneCellState.ACTIVE
    lastAction = "Cognitive reasoning concluded via ${response.actualBackendUsed} (${response.latencyMs}ms)"
    confidence = response.assessment.confidence
    return response
  }

  fun toModel(): ImmuneCell {
    return ImmuneCell(
      id = "cell_dendritic",
      name = "DENDRITIC CELL",
      role = "Assembles structured evidence and presents antigens for cognitive reasoning",
      state = state,
      activity = if (state == ImmuneCellState.ASSESSING || state == ImmuneCellState.INVESTIGATING) "Presenting structured antigen" else "Context assembly standby",
      confidence = confidence,
      eventsHandled = eventsHandled,
      lastAction = lastAction,
      description = "Antigen-presenting sentinel synthesizing multi-provider evidence and dispatching reasoning requests to on-device models."
    )
  }
}
