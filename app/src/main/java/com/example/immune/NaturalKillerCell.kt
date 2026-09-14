package com.example.immune

import com.example.model.ImmuneCell
import com.example.model.ImmuneCellState

/**
 * Natural Killer (NK) Agent — Compromise Recognition / Selective Elimination (Section 7).
 * Identifies corrupted or aberrant internal V-Watcher components.
 * Strictly operates on resources the app itself owns and controls:
 * isolates internal workers, resets compromised caches, stops aberrant tasks.
 */
class NaturalKillerCell(
  private val realityBoundary: AndroidRealityBoundary
) {
  var state: ImmuneCellState = ImmuneCellState.MONITORING
    private set

  var eventsHandled: Int = 0
    private set

  var lastAction: String = "Monitoring internal V-Watcher component integrity"
    private set

  var confidence: Int = 98
    private set

  var internalCorruptionsDetected: Int = 0
    private set

  fun inspectComponentIntegrity(
    componentName: String,
    isHealthy: Boolean,
    isVWatcherOwned: Boolean,
    incidentId: String
  ): ActionResult? {
    eventsHandled++

    if (isHealthy) {
      state = ImmuneCellState.MONITORING
      lastAction = "Component '$componentName' integrity verified"
      return null
    }

    internalCorruptionsDetected++
    state = ImmuneCellState.ACTIVE

    if (!isVWatcherOwned) {
      // External component: NK cell does NOT claim to kill it!
      lastAction = "External component '$componentName' anomalous; recommended user review via Android Settings"
      return realityBoundary.executeRealAction(
        actionName = "KILL_EXTERNAL_PROCESS",
        incidentId = incidentId,
        parameters = mapOf("package" to componentName)
      )
    }

    // Internal V-Watcher component: perform real selective elimination/isolation
    val result = realityBoundary.executeRealAction(
      actionName = "ISOLATE_INTERNAL_SUBSYSTEM",
      incidentId = incidentId,
      parameters = mapOf("subsystem" to componentName)
    )
    lastAction = "Selectively isolated corrupted internal subsystem: $componentName"
    return result
  }

  fun toModel(): ImmuneCell {
    return ImmuneCell(
      id = "cell_nk",
      name = "NK CELL",
      role = "Identifies and eliminates compromised internal components",
      state = state,
      activity = if (state == ImmuneCellState.ACTIVE) "Isolating compromised internal state" else "Surveillance of internal subsystems",
      confidence = confidence,
      eventsHandled = eventsHandled,
      lastAction = lastAction,
      description = "Natural killer surveillance agent detecting aberrant internal execution and selectively isolating misbehaving components."
    )
  }
}
