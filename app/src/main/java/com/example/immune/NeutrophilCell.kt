package com.example.immune

import com.example.model.ImmuneCell
import com.example.model.ImmuneCellState

/**
 * Neutrophil Agent — Rapid Response (Section 4).
 * Fast first responder. Low latency, deterministic, inexpensive, short-lived actions.
 * Focuses on immediate containment of V-Watcher internal workloads under host distress.
 */
class NeutrophilCell(
  private val realityBoundary: AndroidRealityBoundary
) {
  var state: ImmuneCellState = ImmuneCellState.MONITORING
    private set

  var eventsHandled: Int = 0
    private set

  var lastAction: String = "Monitoring for acute physiological distress"
    private set

  var confidence: Int = 97
    private set

  var lastActionResult: ActionResult? = null
    private set

  fun respondToAcuteStress(
    incidentId: String,
    evidence: List<EvidenceItem>
  ): ActionResult? {
    eventsHandled++
    val hasThermalOrMemCrisis = evidence.any {
      it.signalType == "THERMAL_ELEVATION" || it.signalType == "RESOURCE_DEPLETION"
    }

    if (!hasThermalOrMemCrisis) {
      state = ImmuneCellState.MONITORING
      lastAction = "No acute host distress requiring rapid containment"
      return null
    }

    state = ImmuneCellState.ACTIVE
    // Rapid response: immediately throttle V-Watcher internal reasoning
    val result = realityBoundary.executeRealAction(
      actionName = "THROTTLE_INTERNAL_INFERENCE",
      incidentId = incidentId,
      parameters = mapOf("reason" to "Acute host distress detected by Neutrophil")
    )
    lastActionResult = result
    lastAction = "Rapid containment: ${result.platformReason}"
    confidence = 96
    return result
  }

  fun attemptContainment(
    targetAction: String,
    incidentId: String,
    parameters: Map<String, String>
  ): ActionResult {
    eventsHandled++
    state = ImmuneCellState.CONTAINING
    val result = realityBoundary.executeRealAction(targetAction, incidentId, parameters)
    lastActionResult = result
    lastAction = "${result.status.name}: ${result.platformReason}"
    return result
  }

  fun toModel(): ImmuneCell {
    return ImmuneCell(
      id = "cell_neutrophil",
      name = "NEUTROPHIL",
      role = "Rapid first responder executing bounded containment",
      state = state,
      activity = if (state == ImmuneCellState.ACTIVE || state == ImmuneCellState.CONTAINING) "Executing rapid containment" else "Standby for acute distress",
      confidence = confidence,
      eventsHandled = eventsHandled,
      lastAction = lastAction,
      description = "First-responder immune cells deployed immediately to stabilize the host and suppress non-essential workloads."
    )
  }
}
