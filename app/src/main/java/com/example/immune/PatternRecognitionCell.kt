package com.example.immune

import com.example.model.ImmuneCell
import com.example.model.ImmuneCellState
import com.example.sentinel.CanonicalObservation
import java.util.UUID

/**
 * PRR (Pattern Recognition Receptor) Cell (Section 3).
 * Innate immunity: Recognizes conserved danger patterns across telemetry streams
 * without requiring expensive LLM inference.
 */
class PatternRecognitionCell {
  var state: ImmuneCellState = ImmuneCellState.MONITORING
    private set

  var eventsHandled: Int = 0
    private set

  var lastAction: String = "Monitoring canonical telemetry stream"
    private set

  var confidence: Int = 98
    private set

  fun scan(observation: CanonicalObservation): List<EvidenceItem> {
    eventsHandled++
    val detectedEvidence = mutableListOf<EvidenceItem>()

    val batt = observation.battery.value
    val res = observation.resources.value
    val net = observation.network.value

    // Pattern 1: Thermal or Battery Stress
    if (batt.temperatureCelsius > 42.0f) {
      detectedEvidence.add(
        EvidenceItem(
          id = "ev_batt_temp_${UUID.randomUUID().toString().take(6)}",
          providerId = "BatteryProvider",
          signalType = "THERMAL_ELEVATION",
          description = "Battery temperature elevated to ${batt.temperatureCelsius}°C (threshold: 42°C)",
          confidence = 94,
          rawMetricValue = "${batt.temperatureCelsius}°C"
        )
      )
    }

    // Pattern 2: Severe Host Memory Pressure
    if (res.isLowMemory || res.availableMemMb < 200) {
      detectedEvidence.add(
        EvidenceItem(
          id = "ev_mem_crit_${UUID.randomUUID().toString().take(6)}",
          providerId = "DeviceResourceProvider",
          signalType = "RESOURCE_DEPLETION",
          description = "Critical memory pressure: only ${res.availableMemMb} MB free (used ${res.usedPercent}%)",
          confidence = 98,
          rawMetricValue = "${res.availableMemMb}MB"
        )
      )
    }

    // Pattern 3: Network Transport Discontinuity or Unvalidated Connectivity
    if (net.transportType != "Disconnected" && !net.isValidated) {
      detectedEvidence.add(
        EvidenceItem(
          id = "ev_net_unval_${UUID.randomUUID().toString().take(6)}",
          providerId = "NetworkTelemetryProvider",
          signalType = "PORTAL_OR_INTERCEPTION",
          description = "Network connected on ${net.transportType} but Internet connectivity is unvalidated (possible captive portal or proxy)",
          confidence = 88,
          rawMetricValue = net.transportType
        )
      )
    }

    if (detectedEvidence.isNotEmpty()) {
      state = ImmuneCellState.INVESTIGATING
      confidence = 96
      lastAction = "Dispatched ${detectedEvidence.size} danger pattern evidences to immune bus"
    } else {
      state = ImmuneCellState.MONITORING
      confidence = 98
      lastAction = "Deterministic pattern scan clean: All physiological parameters within baselines"
    }

    return detectedEvidence
  }

  fun toModel(): ImmuneCell {
    return ImmuneCell(
      id = "cell_prr",
      name = "PRR CELL",
      role = "Recognizes conserved danger patterns deterministically",
      state = state,
      activity = if (state == ImmuneCellState.INVESTIGATING) "Correlating active danger patterns" else "Passive pattern scanning",
      confidence = confidence,
      eventsHandled = eventsHandled,
      lastAction = lastAction,
      description = "Innate pattern recognition receptors scanning multi-dimensional telemetry for anomalous physiological signatures."
    )
  }
}
