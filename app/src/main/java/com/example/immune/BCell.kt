package com.example.immune

import com.example.model.ImmuneCell
import com.example.model.ImmuneCellState
import com.example.model.ImmuneMemoryPattern

/**
 * B-Cell Agent — Antigen Binding & Neutralization (Adaptive Immunity).
 * Produces biological "antibody" matching: rapidly recognizes recurring
 * behavioral signatures in local memory, neutralizing known benign alerts
 * without invoking expensive reasoning.
 */
class BCell {
  var state: ImmuneCellState = ImmuneCellState.MONITORING
    private set

  var eventsHandled: Int = 0
    private set

  var lastAction: String = "Monitoring memory receptor bindings"
    private set

  var confidence: Int = 99
    private set

  data class BindingResult(
    val isMatched: Boolean,
    val patternCode: String?,
    val isBenign: Boolean,
    val neutralizationSummary: String
  )

  fun bindAntibody(
    candidateDescription: String,
    knownPatterns: List<ImmuneMemoryPattern>
  ): BindingResult {
    eventsHandled++

    val matched = knownPatterns.find { pattern ->
      candidateDescription.contains(pattern.name, ignoreCase = true) ||
          pattern.description.contains(candidateDescription, ignoreCase = true)
    }

    if (matched != null) {
      state = ImmuneCellState.ACTIVE
      val isBenign = matched.typicalResponse.equals("Allow", ignoreCase = true) ||
          matched.typicalResponse.equals("Observe", ignoreCase = true)

      val summary = if (isBenign) {
        "Antibody binding confirmed for ${matched.patternCode} (${matched.name}). Neutralized benign variation in 2ms."
      } else {
        "Antibody binding identified verified threat pattern ${matched.patternCode}."
      }

      lastAction = summary
      confidence = 99
      return BindingResult(
        isMatched = true,
        patternCode = matched.patternCode,
        isBenign = isBenign,
        neutralizationSummary = summary
      )
    }

    state = ImmuneCellState.MONITORING
    lastAction = "No antibody binding found for novel candidate pattern"
    return BindingResult(
      isMatched = false,
      patternCode = null,
      isBenign = false,
      neutralizationSummary = "Novel pattern: Requires innate PRR or Dendritic examination"
    )
  }

  fun toModel(): ImmuneCell {
    return ImmuneCell(
      id = "cell_b_cell",
      name = "B-CELL",
      role = "Binds antibodies to known signatures for instant neutralization",
      state = state,
      activity = if (state == ImmuneCellState.ACTIVE) "Antibody neutralization active" else "Passive receptor surveillance",
      confidence = confidence,
      eventsHandled = eventsHandled,
      lastAction = lastAction,
      description = "Adaptive humoral defender leveraging memory antibodies to rapidly neutralize familiar patterns without CPU/LLM overhead."
    )
  }
}
