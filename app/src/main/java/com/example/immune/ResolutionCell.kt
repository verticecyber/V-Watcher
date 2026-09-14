package com.example.immune

import com.example.model.CaseStatus
import com.example.model.ImmuneCell
import com.example.model.ImmuneCellState
import com.example.sentinel.CanonicalObservation

/**
 * Resolution Cell Agent (Section 11).
 * Active resolution mechanics. Verifies that the host system has actually
 * returned toward baseline before declaring an incident resolved.
 * An alarm that never turns off is pathology; an alarm dismissed prematurely is dangerous.
 * CRITICAL RULE: Never resolves an incident merely because time passed.
 */
class ResolutionCell {
  var state: ImmuneCellState = ImmuneCellState.MONITORING
    private set

  var eventsHandled: Int = 0
    private set

  var lastAction: String = "Monitoring active cases for physiological baseline return"
    private set

  var confidence: Int = 98
    private set

  data class ResolutionVerification(
    val incidentId: String,
    val canResolve: Boolean,
    val newStatus: CaseStatus,
    val verificationEvidence: String
  )

  fun verifyBaselineReturn(
    incidentId: String,
    currentStatus: CaseStatus,
    observation: CanonicalObservation,
    activeEvidence: List<EvidenceItem>,
    isCleanupVerified: Boolean
  ): ResolutionVerification {
    eventsHandled++

    // Rule 1: If active evidence is still being detected, incident CANNOT be resolved
    if (activeEvidence.isNotEmpty()) {
      state = ImmuneCellState.ACTIVE
      val msg = "Active anomaly signals still present (${activeEvidence.size} signals). Resolution refused."
      lastAction = msg
      return ResolutionVerification(
        incidentId = incidentId,
        canResolve = false,
        newStatus = if (currentStatus == CaseStatus.OBSERVING) CaseStatus.ASSESSING else currentStatus,
        verificationEvidence = msg
      )
    }

    // Rule 2: Check resource normalization
    val res = observation.resources.value
    val batt = observation.battery.value
    val isResourcesNormal = !res.isLowMemory && res.availableMemMb >= 250 && batt.temperatureCelsius <= 40.0f

    if (!isResourcesNormal) {
      state = ImmuneCellState.CONTAINING
      val msg = "Host resources not yet normalized (RAM: ${res.availableMemMb}MB free, Temp: ${batt.temperatureCelsius}°C). Maintaining containment."
      lastAction = msg
      return ResolutionVerification(
        incidentId = incidentId,
        canResolve = false,
        newStatus = CaseStatus.CONTAINED,
        verificationEvidence = msg
      )
    }

    // Rule 3: Progressive transition
    return when (currentStatus) {
      CaseStatus.OBSERVING, CaseStatus.ASSESSING -> {
        state = ImmuneCellState.RECOVERING
        val msg = "Signals cleared. Advancing incident to recovering phase."
        lastAction = msg
        ResolutionVerification(incidentId, false, CaseStatus.RECOVERING, msg)
      }
      CaseStatus.ISOLATED, CaseStatus.CONTAINED -> {
        state = ImmuneCellState.RECOVERING
        val msg = if (isCleanupVerified) {
          "Cleanup confirmed by Macrophage and telemetry stabilized. Moving to recovering."
        } else {
          "Awaiting Macrophage cleanup verification before releasing containment."
        }
        lastAction = msg
        ResolutionVerification(
          incidentId = incidentId,
          canResolve = false,
          newStatus = if (isCleanupVerified) CaseStatus.RECOVERING else CaseStatus.CONTAINED,
          verificationEvidence = msg
        )
      }
      CaseStatus.RECOVERING -> {
        state = ImmuneCellState.MONITORING
        val msg = "Full physiological equilibrium verified. No residual secondary effects. Incident resolved."
        lastAction = msg
        ResolutionVerification(incidentId, true, CaseStatus.RESOLVED, msg)
      }
      CaseStatus.RESOLVED -> {
        state = ImmuneCellState.MONITORING
        ResolutionVerification(incidentId, true, CaseStatus.RESOLVED, "Already in homeostatic baseline")
      }
    }
  }

  fun toModel(): ImmuneCell {
    return ImmuneCell(
      id = "cell_resolution",
      name = "RESOLUTION CELL",
      role = "Validates return to baseline before restoring homeostatic equilibrium",
      state = state,
      activity = if (state == ImmuneCellState.RECOVERING) "Verifying baseline normalization" else "Resolution surveillance active",
      confidence = confidence,
      eventsHandled = eventsHandled,
      lastAction = lastAction,
      description = "Homeostatic resolution mechanics actively verifying that anomalies have cleared and resources returned to baseline before closing cases."
    )
  }
}
