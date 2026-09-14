package com.example.immune

import com.example.model.ImmuneCell
import com.example.model.ImmuneCellState

/**
 * T-Helper / Coordinator Agent (Section 8).
 * Central coordinator of the adaptive immune layer.
 * Integrates evidence from PRR, Sentinel, and Dendritic cells,
 * determines whether escalation is clinically justified,
 * arbitrates conflicting assessments, and recruits specialized cells.
 */
class THelperCoordinatorCell {
  var state: ImmuneCellState = ImmuneCellState.MONITORING
    private set

  var eventsHandled: Int = 0
    private set

  var lastAction: String = "Monitoring multi-cell immune communications"
    private set

  var confidence: Int = 96
    private set

  data class CoordinationPlan(
    val incidentId: String,
    val isEscalationJustified: Boolean,
    val rationale: String,
    val recruitedAgents: List<AgentRole>,
    val recommendedEffectorAction: String?
  )

  fun coordinate(
    incidentId: String,
    evidence: List<EvidenceItem>,
    isKnownBenignPattern: Boolean,
    isSuppressedByRegulation: Boolean
  ): CoordinationPlan {
    eventsHandled++

    if (isSuppressedByRegulation) {
      state = ImmuneCellState.MONITORING
      lastAction = "Regulatory veto respected: Escalation suppressed to preserve host equilibrium"
      return CoordinationPlan(
        incidentId = incidentId,
        isEscalationJustified = false,
        rationale = "Regulatory veto active: Cooldown or budget cap enforced",
        recruitedAgents = emptyList(),
        recommendedEffectorAction = null
      )
    }

    if (isKnownBenignPattern) {
      state = ImmuneCellState.ACTIVE
      lastAction = "Memory binding matched benign pattern: Escalation averted"
      return CoordinationPlan(
        incidentId = incidentId,
        isEscalationJustified = false,
        rationale = "Recognized by memory B-cell as verified benign variation",
        recruitedAgents = listOf(AgentRole.B_CELL),
        recommendedEffectorAction = null
      )
    }

    // Converging Evidence rule: A single weak signal (< 80 confidence) cannot trigger cytotoxic escalation
    val highConfidenceEvidence = evidence.filter { it.confidence >= 85 }
    if (evidence.size == 1 && highConfidenceEvidence.isEmpty()) {
      state = ImmuneCellState.INVESTIGATING
      lastAction = "Single isolated weak signal (${evidence.first().confidence}%): Withholding escalation"
      return CoordinationPlan(
        incidentId = incidentId,
        isEscalationJustified = false,
        rationale = "Insufficient converging evidence for systemic defense escalation",
        recruitedAgents = listOf(AgentRole.PRR),
        recommendedEffectorAction = null
      )
    }

    if (evidence.isNotEmpty()) {
      state = ImmuneCellState.ACTIVE
      val recruited = mutableListOf(AgentRole.CYTOTOXIC, AgentRole.MACROPHAGE)
      lastAction = "Converging evidence confirmed (${evidence.size} signals): Recruited Cytotoxic and Macrophage cells"
      return CoordinationPlan(
        incidentId = incidentId,
        isEscalationJustified = true,
        rationale = "Multi-provider evidence converged above critical safety threshold",
        recruitedAgents = recruited,
        recommendedEffectorAction = "ISOLATE_INTERNAL_SUBSYSTEM"
      )
    }

    state = ImmuneCellState.MONITORING
    lastAction = "Equilibrium confirmed across all sensors"
    return CoordinationPlan(
      incidentId = incidentId,
      isEscalationJustified = false,
      rationale = "No anomalous telemetry detected",
      recruitedAgents = emptyList(),
      recommendedEffectorAction = null
    )
  }

  fun toModel(): ImmuneCell {
    return ImmuneCell(
      id = "cell_t_helper",
      name = "T-HELPER CELL",
      role = "Coordinates cellular signaling, arbitration, and escalation",
      state = state,
      activity = if (state == ImmuneCellState.ACTIVE) "Coordinating multi-agent defense" else "Signal arbitration standby",
      confidence = confidence,
      eventsHandled = eventsHandled,
      lastAction = lastAction,
      description = "Immune coordinator arbitrating competing assessments, enforcing converging evidence rules, and recruiting specialized effectors."
    )
  }
}
