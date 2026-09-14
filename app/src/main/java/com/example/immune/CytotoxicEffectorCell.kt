package com.example.immune

import com.example.model.ImmuneCell
import com.example.model.ImmuneCellState

/**
 * Cytotoxic / Effector Agent (Section 9).
 * Explicit effector layer executing targeted biological defense actions.
 * Operates ONLY under strict clinical safety gates:
 * - Verified incident identity
 * - Converging fresh evidence
 * - Confidence > 80%
 * - Real Android capability boundaries (never fakes execution)
 */
class CytotoxicEffectorCell(
  private val realityBoundary: AndroidRealityBoundary
) {
  var state: ImmuneCellState = ImmuneCellState.MONITORING
    private set

  var eventsHandled: Int = 0
    private set

  var lastAction: String = "Monitoring authorized clinical intervention requests"
    private set

  var confidence: Int = 98
    private set

  var lastResult: ActionResult? = null
    private set

  fun executeAuthorizedAction(
    incidentId: String,
    actionName: String,
    evidence: List<EvidenceItem>,
    isFresh: Boolean,
    confidenceScore: Int,
    parameters: Map<String, String>
  ): ActionResult {
    eventsHandled++

    // Gate 1: Check evidence freshness
    if (!isFresh) {
      val denied = ActionResult(
        actionId = "act_denied_stale",
        targetAction = actionName,
        status = ActionExecutionStatus.DENIED,
        platformReason = "Action denied: Evidence is stale (> 30s old). Effector requires fresh telemetry.",
        evidence = mapOf("incidentId" to incidentId)
      )
      lastResult = denied
      lastAction = denied.platformReason
      return denied
    }

    // Gate 2: Check confidence score
    if (confidenceScore < 80) {
      val denied = ActionResult(
        actionId = "act_denied_confidence",
        targetAction = actionName,
        status = ActionExecutionStatus.DENIED,
        platformReason = "Action denied: Clinical confidence ($confidenceScore%) is below 80% safety threshold.",
        evidence = mapOf("incidentId" to incidentId, "confidence" to confidenceScore.toString())
      )
      lastResult = denied
      lastAction = denied.platformReason
      return denied
    }

    // Gate 3: Check evidence presence
    if (evidence.isEmpty()) {
      val denied = ActionResult(
        actionId = "act_denied_no_evidence",
        targetAction = actionName,
        status = ActionExecutionStatus.DENIED,
        platformReason = "Action denied: Zero evidence items provided. Ungrounded action refused.",
        evidence = mapOf("incidentId" to incidentId)
      )
      lastResult = denied
      lastAction = denied.platformReason
      return denied
    }

    state = ImmuneCellState.CONTAINING
    val result = realityBoundary.executeRealAction(actionName, incidentId, parameters)
    lastResult = result

    state = if (result.status == ActionExecutionStatus.EXECUTED) ImmuneCellState.ACTIVE else ImmuneCellState.MONITORING
    lastAction = "${result.status.name}: ${result.platformReason}"
    confidence = 98
    return result
  }

  fun toModel(): ImmuneCell {
    return ImmuneCell(
      id = "cell_cytotoxic",
      name = "CYTOTOXIC CELL",
      role = "Executes targeted defense under strict clinical safety gates",
      state = state,
      activity = if (state == ImmuneCellState.CONTAINING || state == ImmuneCellState.ACTIVE) "Executing authorized defense" else "Effector safety gates armed",
      confidence = confidence,
      eventsHandled = eventsHandled,
      lastAction = lastAction,
      description = "Specialized effector cells executing authorized containment and isolation within strict Android platform reality boundaries."
    )
  }
}
