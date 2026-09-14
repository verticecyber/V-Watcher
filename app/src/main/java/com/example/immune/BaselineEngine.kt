package com.example.immune

import com.example.domain.AnomalyCandidate
import com.example.telemetry.DeviceTelemetrySnapshot

class BaselineEngine {

  data class BaselineParameters(
    val maxHealthyTempCelsius: Float = 42.0f,
    val minHealthyBatteryPercent: Int = 10,
    val maxHealthyMemoryUsagePercent: Int = 88,
    val establishedPackages: Set<String> = emptySet()
  )

  private var currentBaseline = BaselineParameters()

  fun establishInitialBaseline(snapshot: DeviceTelemetrySnapshot) {
    val knownPackages = snapshot.appInventory.value.apps.map { it.packageName }.toSet()
    currentBaseline = currentBaseline.copy(establishedPackages = knownPackages)
  }

  /**
   * Deterministic anomaly candidate evaluation.
   * Compares incoming real device telemetry against established baseline thresholds.
   * Generates AnomalyCandidate, NOT a "threat".
   */
  fun evaluateSnapshot(snapshot: DeviceTelemetrySnapshot): List<AnomalyCandidate> {
    val candidates = mutableListOf<AnomalyCandidate>()

    // 1. Thermal check
    val battery = snapshot.battery.value
    if (battery.temperatureCelsius > currentBaseline.maxHealthyTempCelsius) {
      candidates.add(
        AnomalyCandidate(
          id = "anom_temp_${System.currentTimeMillis()}",
          source = "BatteryProvider",
          title = "Elevated Battery Temperature",
          description = "Observed battery temperature (${battery.temperatureCelsius}°C) exceeds baseline ceiling (${currentBaseline.maxHealthyTempCelsius}°C).",
          severity = "Moderate",
          confidence = 85,
          deterministicRuleTriggered = "RULE_THERMAL_CEILING_EXCEEDED"
        )
      )
    }

    // 2. Memory pressure check
    val memory = snapshot.resources.value
    if (memory.isLowMemory || memory.usedPercent > currentBaseline.maxHealthyMemoryUsagePercent) {
      candidates.add(
        AnomalyCandidate(
          id = "anom_mem_${System.currentTimeMillis()}",
          source = "DeviceResourceProvider",
          title = "System Memory Pressure",
          description = "Available RAM (${memory.availableMemMb} MB / ${memory.totalMemMb} MB) is below normal operational equilibrium.",
          severity = "Mild",
          confidence = 90,
          deterministicRuleTriggered = "RULE_MEMORY_EQUILIBRIUM_LOW"
        )
      )
    }

    // 3. Power Save Mode vs Active Foreground Usage Check
    if (battery.isPowerSaveMode && battery.levelPercent > 50) {
      candidates.add(
        AnomalyCandidate(
          id = "anom_power_${System.currentTimeMillis()}",
          source = "BatteryProvider",
          title = "Power Saver State Discrepancy",
          description = "Device power saver mode is active while battery reserve is elevated (${battery.levelPercent}%).",
          severity = "Mild",
          confidence = 75,
          deterministicRuleTriggered = "RULE_POWER_SAVE_STATE_DIVERGENCE"
        )
      )
    }

    return candidates
  }
}
