package com.example.immune

import com.example.domain.AnomalyCandidate
import com.example.model.ImmuneCell
import com.example.model.ImmuneCellState
import com.example.model.IncidentCase
import com.example.model.ImmuneMemoryPattern
import com.example.reasoning.Assessment
import com.example.reasoning.OnDeviceReasoningEngine
import com.example.telemetry.DeviceTelemetrySnapshot

data class TelemetrySignal(
  val id: String,
  val timestamp: Long = System.currentTimeMillis(),
  val type: String,
  val source: String,
  val description: String,
  val isAnomaly: Boolean
)

class SentinelCell {
  var lastAction: String = "Monitoring host telemetry streams"
  var eventsHandled: Int = 0
  var state: ImmuneCellState = ImmuneCellState.MONITORING

  fun observe(snapshot: DeviceTelemetrySnapshot): String {
    eventsHandled++
    state = ImmuneCellState.ACTIVE
    lastAction = "Sampled battery (${snapshot.battery.value.levelPercent}%), memory (${snapshot.resources.value.availableMemMb}MB free), network"
    return lastAction
  }

  fun observe(canonical: com.example.sentinel.CanonicalObservation): String {
    eventsHandled++
    state = ImmuneCellState.ACTIVE
    lastAction = "Sampled canonical observation ${canonical.observationId} (Battery: ${canonical.battery.value.levelPercent}%, RAM: ${canonical.resources.value.availableMemMb}MB free)"
    return lastAction
  }

  fun toModel(): ImmuneCell {
    return ImmuneCell(
      id = "cell_sentinel",
      name = "SENTINEL",
      role = "Monitors real changes around your device",
      state = state,
      activity = if (state == ImmuneCellState.ACTIVE) "Processing live telemetry stream" else "Passive telemetry monitoring",
      confidence = 98,
      eventsHandled = eventsHandled,
      lastAction = lastAction,
      description = "First-line observational agents scanning live device battery, memory, process inventory, and network sockets."
    )
  }
}

class ReceptorCell {
  var lastAction: String = "Parsed network and hardware signals"
  var eventsHandled: Int = 0
  var state: ImmuneCellState = ImmuneCellState.MONITORING

  fun receive(signalsCount: Int): String {
    eventsHandled += signalsCount
    state = ImmuneCellState.ACTIVE
    lastAction = "Dispatched $signalsCount signals to biological bus"
    return lastAction
  }

  fun toModel(): ImmuneCell {
    return ImmuneCell(
      id = "cell_receptor",
      name = "RECEPTOR",
      role = "Receives system and network signals",
      state = state,
      activity = if (state == ImmuneCellState.ACTIVE) "Transducing telemetry signals" else "Listening on protected communication bus",
      confidence = 99,
      eventsHandled = eventsHandled,
      lastAction = lastAction,
      description = "Physiological sensors capturing incoming signals and translating them into structured immune events."
    )
  }
}

class ContextCell {
  var lastAction: String = "Baseline consistency verified"
  var eventsHandled: Int = 0
  var state: ImmuneCellState = ImmuneCellState.MONITORING

  fun correlate(candidates: List<AnomalyCandidate>): Boolean {
    eventsHandled++
    return if (candidates.isNotEmpty()) {
      state = ImmuneCellState.INVESTIGATING
      lastAction = "Correlated ${candidates.size} anomaly candidates against temporal baseline"
      true
    } else {
      state = ImmuneCellState.MONITORING
      lastAction = "Verified baseline equilibrium across all sensors"
      false
    }
  }

  fun toModel(): ImmuneCell {
    return ImmuneCell(
      id = "cell_context",
      name = "CONTEXT CELL",
      role = "Correlates observations and behavioral patterns",
      state = state,
      activity = if (state == ImmuneCellState.INVESTIGATING) "Correlating candidate anomaly with baseline" else "Evaluating current state against temporal baseline",
      confidence = 95,
      eventsHandled = eventsHandled,
      lastAction = lastAction,
      description = "Synthesizes multi-source telemetry to determine whether an anomaly is meaningful or benign."
    )
  }
}

class ResponseCell {
  var lastAction: String = "Maintained protective boundary"
  var eventsHandled: Int = 0
  var state: ImmuneCellState = ImmuneCellState.MONITORING

  fun safeResponse(assessment: Assessment?): String {
    eventsHandled++
    return if (assessment != null && assessment.confidence > 75) {
      state = ImmuneCellState.ACTIVE
      lastAction = "Safe action: ${assessment.recommendedAction.displayName} (Non-destructive clinical review)"
      lastAction
    } else {
      state = ImmuneCellState.MONITORING
      lastAction = "Maintaining calm protective perimeter"
      lastAction
    }
  }

  fun toModel(): ImmuneCell {
    return ImmuneCell(
      id = "cell_response",
      name = "RESPONSE CELL",
      role = "Contains or remediates unusual behavior",
      state = state,
      activity = if (state == ImmuneCellState.ACTIVE) "Executing safe clinical response" else "Isolation mechanics in standby",
      confidence = 97,
      eventsHandled = eventsHandled,
      lastAction = lastAction,
      description = "Active biological response agents executing safe, non-destructive actions: observation logging, case formulation, and clinical alerts."
    )
  }
}

class MemoryCell {
  var lastAction: String = "Indexed historical patterns"
  var eventsHandled: Int = 0
  var state: ImmuneCellState = ImmuneCellState.MONITORING

  fun remember(pattern: ImmuneMemoryPattern) {
    eventsHandled++
    state = ImmuneCellState.ACTIVE
    lastAction = "Registered pattern ${pattern.patternCode} in local memory"
  }

  fun toModel(): ImmuneCell {
    return ImmuneCell(
      id = "cell_memory",
      name = "MEMORY CELL",
      role = "Remembers previous patterns and responses",
      state = state,
      activity = "Local memory index active",
      confidence = 99,
      eventsHandled = eventsHandled,
      lastAction = lastAction,
      description = "Stores behavioral signatures and verified outcomes locally to enable instant recognition of familiar patterns."
    )
  }
}
