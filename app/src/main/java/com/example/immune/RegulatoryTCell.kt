package com.example.immune

import com.example.model.ImmuneCell
import com.example.model.ImmuneCellState

/**
 * Regulatory T-Cell Agent (Section 10 - CRITICAL).
 * Balances immune defense against self-inflicted host damage.
 * Monitors activation frequency, storming, inference cost, and false alarms.
 * Has explicit veto power over downstream escalation:
 * - Suppresses redundant responses
 * - Enforces cooldown windows
 * - Forces deterministic mode under resource strain
 * - Mitigates agent storms and runaway feedback loops
 */
class RegulatoryTCell {
  var state: ImmuneCellState = ImmuneCellState.MONITORING
    private set

  var eventsHandled: Int = 0
    private set

  var lastAction: String = "Monitoring homeostatic balance and activation budgets"
    private set

  var confidence: Int = 99
    private set

  var totalVetoesIssued: Int = 0
    private set

  var isDeterministicModeForced: Boolean = false
    private set

  private var lastEscalationTimestamp: Long = 0L
  private val MIN_ESCALATION_COOLDOWN_MS = 15_000L // 15s cooldown between systemic escalations

  data class RegulatoryCheckResult(
    val isPermitted: Boolean,
    val reason: String,
    val forceDeterministic: Boolean,
    val imposedCooldownMs: Long = 0L
  )

  fun evaluateEscalationRequest(
    incidentId: String,
    batteryPercent: Int,
    isDischarging: Boolean,
    availableMemMb: Long,
    isBusStorming: Boolean,
    activeIncidentsCount: Int
  ): RegulatoryCheckResult {
    eventsHandled++
    val now = System.currentTimeMillis()

    // 1. Storm Protection Rule: Bus storming or excessive dispatch rate
    if (isBusStorming) {
      totalVetoesIssued++
      state = ImmuneCellState.ACTIVE
      isDeterministicModeForced = true
      lastAction = "Agent storm detected: Regulatory suppression active. Escalation vetoed."
      return RegulatoryCheckResult(
        isPermitted = false,
        reason = "Agent storm detected: Event dispatch rate exceeded safety limits. Escalation suppressed to protect device.",
        forceDeterministic = true,
        imposedCooldownMs = 30_000L
      )
    }

    // 2. Battery Conservation Rule: Battery < 15% and discharging
    if (batteryPercent < 15 && isDischarging) {
      totalVetoesIssued++
      state = ImmuneCellState.ACTIVE
      isDeterministicModeForced = true
      lastAction = "Battery critical (< 15% discharging): Heavy reasoning vetoed by regulatory cell"
      return RegulatoryCheckResult(
        isPermitted = false,
        reason = "Host battery critical ($batteryPercent% discharging). High-power immune actions vetoed.",
        forceDeterministic = true,
        imposedCooldownMs = 60_000L
      )
    }

    // 3. Memory Conservation Rule: Host RAM constrained (< 200MB free)
    if (availableMemMb < 200) {
      totalVetoesIssued++
      state = ImmuneCellState.ACTIVE
      isDeterministicModeForced = true
      lastAction = "Memory constrained (${availableMemMb}MB free): Expensive operations suppressed"
      return RegulatoryCheckResult(
        isPermitted = false,
        reason = "Memory constrained (${availableMemMb}MB free). Effector expansion restricted.",
        forceDeterministic = true,
        imposedCooldownMs = 20_000L
      )
    }

    // 4. Cooldown Enforcement: Prevent runaway rapid-fire escalations
    val timeSinceLast = now - lastEscalationTimestamp
    if (lastEscalationTimestamp > 0 && timeSinceLast < MIN_ESCALATION_COOLDOWN_MS) {
      totalVetoesIssued++
      state = ImmuneCellState.ACTIVE
      val remaining = MIN_ESCALATION_COOLDOWN_MS - timeSinceLast
      lastAction = "Cooldown active: Escalation throttled for ${remaining}ms"
      return RegulatoryCheckResult(
        isPermitted = false,
        reason = "Regulatory cooldown active ($remaining ms remaining). Suppressing rapid-fire escalation.",
        forceDeterministic = false,
        imposedCooldownMs = remaining
      )
    }

    // Escalation admitted
    lastEscalationTimestamp = now
    isDeterministicModeForced = false
    state = ImmuneCellState.MONITORING
    lastAction = "Regulatory clearance granted for incident $incidentId"
    return RegulatoryCheckResult(
      isPermitted = true,
      reason = "Clinical safety parameters nominal. Defense permitted.",
      forceDeterministic = false
    )
  }

  fun reset() {
    isDeterministicModeForced = false
    lastEscalationTimestamp = 0L
    state = ImmuneCellState.MONITORING
  }

  fun toModel(): ImmuneCell {
    return ImmuneCell(
      id = "cell_regulatory",
      name = "REGULATORY T-CELL",
      role = "Enforces self-regulation, prevents storms, and caps resource consumption",
      state = state,
      activity = if (isDeterministicModeForced) "Enforcing regulatory throttling" else "Homeostatic balance maintained",
      confidence = confidence,
      eventsHandled = eventsHandled,
      lastAction = lastAction,
      description = "Mandatory regulatory cell suppressing unnecessary escalation, curbing false alarms, and protecting host battery and memory."
    )
  }
}
