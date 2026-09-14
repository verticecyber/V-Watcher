package com.example.ui.screens

import com.example.immune.HomeostaticMacroState
import com.example.model.CaseStatus
import com.example.model.ExamCategory
import com.example.model.IncidentCase
import com.example.sentinel.SentinelState
import com.example.viewmodel.VWatcherUiState

/**
 * PH-05 (G13): single source of truth mapping backend state to UI strings.
 * Every function returns either a backend-bound value or an explicitly non-committal
 * placeholder. Dispositions: BACKEND_BOUND, CONDITIONALLY_TRUE, REWORDED/REMOVED.
 */

/** Claim 1 BACKEND_BOUND: banner shows only after a real observation exists. */
fun deviceDataBadgeVisible(hasObservation: Boolean): Boolean = hasObservation

/** Claims 2/Immune BACKEND_BOUND: null evaluation renders as unevaluated, never green. */
fun macroStateDisplayName(macroState: HomeostaticMacroState?): String =
  macroState?.name ?: "EVALUATING"

fun homeostasisReasonDisplay(reason: String?): String =
  reason ?: "Evaluation pending — no equilibrium claim made yet."

fun homeostasisConfidenceDisplay(confidence: Int?): String =
  confidence?.let { "$it%" } ?: "—"

/** Claim 3 BACKEND_BOUND: sentinel badge is ACTIVE only on observed running state. */
fun sentinelRunningLabel(state: SentinelState?): String =
  if (state?.isRunning == true) "ACTIVE" else "IDLE"

/** Claim 4 BACKEND_BOUND: exam dialog condition follows the examined score bands. */
fun examConditionLabel(healthScore: Int): String =
  if (healthScore >= 90) "Healthy" else "Attention"

/** Claims 6/7 BACKEND_BOUND: honest case accounting (no RESOLVED inflation, no hardcoded zero). */
fun isolatedCaseCount(cases: List<IncidentCase>): Int =
  cases.count { it.status == CaseStatus.ISOLATED }

fun uncontainedCaseCount(cases: List<IncidentCase>): Int =
  cases.count { it.status != CaseStatus.RESOLVED && it.status != CaseStatus.ISOLATED }

/** Claim 5 BACKEND_BOUND: exam categories recomputed from live UI state on every render. */
fun bindExamCategories(base: List<ExamCategory>, ui: VWatcherUiState): List<ExamCategory> {
  val isolated = isolatedCaseCount(ui.cases)
  val open = uncontainedCaseCount(ui.cases)
  val validated = ui.deviceState?.network?.value?.isValidated == true
  return base.map { category ->
    when (category.id) {
      "ec_apps" -> category.copy(
        status = if (isolated == 0) "Healthy" else "Attention",
        summary = "${ui.applications.size} applications inventoried",
        detailCountText = "$isolated under review",
        isHealthy = isolated == 0,
        notes = "Counts from on-device package inventory."
      )
      "ec_permissions" -> category.copy(
        status = "Inventoried",
        summary = "Declared permissions inventoried from manifests",
        detailCountText = ui.permissions.joinToString { "${it.name}: ${it.appsCount}" }
          .ifEmpty { "No permission data" },
        isHealthy = true,
        notes = "Declaration counts, not runtime access frequency."
      )
      "ec_network" -> category.copy(
        status = if (validated) "Healthy" else "Standby",
        summary = "Transport: ${ui.deviceState?.network?.value?.transportType ?: "unknown"}",
        detailCountText = "${ui.networkConnections.size} conduits listed",
        isHealthy = validated,
        notes = "Transport state only; no per-app TLS verification."
      )
      "ec_system" -> category.copy(
        status = "Listed",
        summary = "OS ${ui.deviceState?.systemState?.value?.osVersion ?: "unknown"} · patch ${ui.deviceState?.systemState?.value?.securityPatch ?: "unknown"}",
        detailCountText = "No integrity attestation performed",
        isHealthy = true,
        notes = "System descriptors read from OS; signatures not verified."
      )
      "ec_behavior" -> category.copy(
        status = if (ui.isUsageAccessMissing) "Standby" else if (open == 0) "Healthy" else "Attention",
        summary = if (ui.isUsageAccessMissing) "Awaiting usage-access grant" else "Foreground/background transitions observed",
        detailCountText = "$open open flags",
        isHealthy = open == 0,
        notes = "Behavioral baseline only with usage access granted."
      )
      "ec_immune" -> category.copy(
        status = "Ready",
        summary = "${ui.memoryPatterns.size} patterns in local repository",
        detailCountText = "RAM-only; nothing persisted",
        isHealthy = true,
        notes = "Short-circuit lookup; match latency unmeasured."
      )
      else -> category
    }
  }
}
