package com.example.immune

import com.example.model.ImmuneCell
import com.example.model.ImmuneCellState

/**
 * Macrophage Agent — Cleanup / Recovery / Recycling (Section 5).
 * Inspects V-Watcher state, clears stale temporary artifacts, trims memory caches,
 * and consolidates duplicate incidents.
 * Produces real verified proof of reclamation.
 */
class MacrophageCell(
  private val realityBoundary: AndroidRealityBoundary
) {
  var state: ImmuneCellState = ImmuneCellState.MONITORING
    private set

  var eventsHandled: Int = 0
    private set

  var lastAction: String = "Monitoring internal caches and transient state"
    private set

  var confidence: Int = 99
    private set

  var totalBytesReclaimed: Long = 0L
    private set

  fun scavengeAndRecycle(incidentId: String): ActionResult {
    eventsHandled++
    state = ImmuneCellState.ACTIVE

    val result = realityBoundary.executeRealAction(
      actionName = "RECLAIM_INTERNAL_CACHE",
      incidentId = incidentId,
      parameters = emptyMap()
    )

    val freed = result.evidence["bytesFreed"]?.toLongOrNull() ?: 0L
    totalBytesReclaimed += freed

    state = ImmuneCellState.RECOVERING
    lastAction = "Phagocytosis complete: Reclaimed $freed bytes (${result.evidence["filesRemoved"]} artifacts cleared)"
    confidence = 99
    return result
  }

  fun consolidateIncidents(incidentCodes: List<String>): List<String> {
    eventsHandled++
    // Deduplicate and return consolidated unique list
    val distinctCodes = incidentCodes.distinct()
    lastAction = "Consolidated ${incidentCodes.size} incident references down to ${distinctCodes.size} distinct signatures"
    return distinctCodes
  }

  fun toModel(): ImmuneCell {
    return ImmuneCell(
      id = "cell_macrophage",
      name = "MACROPHAGE",
      role = "Scavenges stale artifacts and verifies resource cleanup",
      state = state,
      activity = if (state == ImmuneCellState.RECOVERING || state == ImmuneCellState.ACTIVE) "Recycling internal state" else "Passive scavenging standby",
      confidence = confidence,
      eventsHandled = eventsHandled,
      lastAction = lastAction,
      description = "Cellular scavenger reclaiming resources, purging stale artifacts, and validating physiological cleanup."
    )
  }
}
