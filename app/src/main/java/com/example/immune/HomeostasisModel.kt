package com.example.immune

import com.example.sentinel.CanonicalObservation
import com.example.telemetry.TelemetryAvailability

enum class HomeostaticMacroState(val displayName: String, val clinicalRationale: String) {
  HOMEOSTATIC("HOMEOSTATIC", "Physiological equilibrium maintained. Normal cellular surveillance."),
  WATCH("WATCH", "Mild deviations detected. Non-invasive surveillance active."),
  STRESSED("STRESSED", "Elevated host or internal system load. Throttling and resource protection applied."),
  ACTIVE_DEFENSE("ACTIVE_DEFENSE", "Targeted biological defense and containment actively executing."),
  RECOVERING("RECOVERING", "Threat neutralized. Macrophages verifying cleanup and baseline restoration."),
  DEGRADED("DEGRADED", "Substrate failure or critical sensor loss. Operating in fail-closed safety state."),
  UNKNOWN("UNKNOWN", "Insufficient or stale telemetry. Homeostatic equilibrium cannot be assumed.")
}

data class EnergyDimension(
  val batteryLevelPercent: Int,
  val isCharging: Boolean,
  val temperatureC: Float,
  val isPowerSaveMode: Boolean,
  val isStressed: Boolean
)

data class MemoryPressureDimension(
  val totalMemMb: Long,
  val availableMemMb: Long,
  val usedPercent: Int,
  val isLowMemory: Boolean,
  val vWatcherHeapMb: Long,
  val isUnderPressure: Boolean
)

data class NetworkDimension(
  val transportType: String,
  val isValidated: Boolean,
  val isConnected: Boolean,
  val isAnomalous: Boolean
)

data class StabilityDimension(
  val totalAppsCount: Int,
  val unusualPermissionCount: Int,
  val recentAbnormalEventsCount: Int
)

data class SecurityDimension(
  val securityPatch: String,
  val isInteractive: Boolean,
  val isDeviceSecure: Boolean
)

data class RuntimeIntegrityDimension(
  val activeWorkersCount: Int,
  val internalCorruptionsDetected: Int,
  val isInternalExecutionHealthy: Boolean
)

data class ImmuneActivityDimension(
  val activeAgentsCount: Int,
  val messagesPerSec: Double,
  val totalInferencesDispatched: Int,
  val isStorming: Boolean,
  val isSelfDestabilizing: Boolean
)

data class IncidentLoadDimension(
  val totalActiveIncidents: Int,
  val containedIncidents: Int,
  val resolvingIncidents: Int,
  val unresolvedIncidents: Int
)

data class HomeostasisEvaluation(
  val macroState: HomeostaticMacroState,
  val confidenceScore: Int,
  val primaryReason: String,
  val energy: EnergyDimension,
  val memory: MemoryPressureDimension,
  val network: NetworkDimension,
  val stability: StabilityDimension,
  val security: SecurityDimension,
  val integrity: RuntimeIntegrityDimension,
  val immuneActivity: ImmuneActivityDimension,
  val incidentLoad: IncidentLoadDimension,
  val evaluatedAt: Long = System.currentTimeMillis()
)

object HomeostasisEngine {

  fun evaluate(
    observation: CanonicalObservation?,
    activeAgentsCount: Int,
    messagesPerSec: Double,
    isBusStorming: Boolean,
    totalInferencesDispatched: Int,
    activeIncidentsCount: Int,
    containedIncidentsCount: Int,
    resolvingIncidentsCount: Int,
    internalCorruptions: Int
  ): HomeostasisEvaluation {
    if (observation == null || observation.freshness.isStale) {
      return createUnknownState(
        reason = if (observation == null) "No canonical telemetry captured" else "Telemetry is stale (${observation.freshness.observationAgeMs}ms old). Equilibrium unknown."
      )
    }

    // 1. Check Sensor Substrate Degradation
    val hasCriticalProviderFailure = observation.battery.availability == TelemetryAvailability.UNAVAILABLE ||
        observation.battery.availability == TelemetryAvailability.ERROR ||
        observation.resources.availability == TelemetryAvailability.ERROR ||
        observation.network.availability == TelemetryAvailability.ERROR

    if (hasCriticalProviderFailure) {
      return createDegradedState(
        observation = observation,
        reason = "Critical hardware sensor provider failed or unavailable. Operating in defensive fail-closed mode."
      )
    }

    // Extract real dimensions
    val battVal = observation.battery.value
    val resVal = observation.resources.value
    val netVal = observation.network.value
    val invVal = observation.inventory.value
    val sysVal = observation.system.value

    val isBatteryStressed = battVal.levelPercent < 15 && !battVal.isCharging
    val isMemoryStressed = resVal.isLowMemory || resVal.usedPercent > 92 || resVal.availableMemMb < 250
    val isSelfDestabilizing = resVal.vWatcherMemoryMb > 200 || isBusStorming || messagesPerSec > 25.0

    val energyDim = EnergyDimension(
      batteryLevelPercent = battVal.levelPercent,
      isCharging = battVal.isCharging,
      temperatureC = battVal.temperatureCelsius,
      isPowerSaveMode = battVal.isPowerSaveMode,
      isStressed = isBatteryStressed
    )

    val memDim = MemoryPressureDimension(
      totalMemMb = resVal.totalMemMb,
      availableMemMb = resVal.availableMemMb,
      usedPercent = resVal.usedPercent,
      isLowMemory = resVal.isLowMemory,
      vWatcherHeapMb = resVal.vWatcherMemoryMb,
      isUnderPressure = isMemoryStressed
    )

    val netDim = NetworkDimension(
      transportType = netVal.transportType,
      isValidated = netVal.isValidated,
      isConnected = netVal.transportType != "Disconnected",
      isAnomalous = false
    )

    val stabDim = StabilityDimension(
      totalAppsCount = invVal.totalAppsCount,
      unusualPermissionCount = 0,
      recentAbnormalEventsCount = 0
    )

    val secDim = SecurityDimension(
      securityPatch = sysVal.securityPatch,
      isInteractive = sysVal.isScreenInteractive,
      isDeviceSecure = true
    )

    val intDim = RuntimeIntegrityDimension(
      activeWorkersCount = activeAgentsCount,
      internalCorruptionsDetected = internalCorruptions,
      isInternalExecutionHealthy = internalCorruptions == 0
    )

    val immDim = ImmuneActivityDimension(
      activeAgentsCount = activeAgentsCount,
      messagesPerSec = messagesPerSec,
      totalInferencesDispatched = totalInferencesDispatched,
      isStorming = isBusStorming,
      isSelfDestabilizing = isSelfDestabilizing
    )

    val incDim = IncidentLoadDimension(
      totalActiveIncidents = activeIncidentsCount,
      containedIncidents = containedIncidentsCount,
      resolvingIncidents = resolvingIncidentsCount,
      unresolvedIncidents = (activeIncidentsCount - containedIncidentsCount - resolvingIncidentsCount).coerceAtLeast(0)
    )

    // Transparent homeostatic state determination
    val macroState: HomeostaticMacroState
    val primaryReason: String
    val confidence: Int

    when {
      // Self-Protection Invariant: V-Watcher is destabilizing the host
      isSelfDestabilizing -> {
        macroState = HomeostaticMacroState.STRESSED
        primaryReason = "V-Watcher immune activity is placing high load on device resources. Regulatory suppression active."
        confidence = 96
      }
      internalCorruptions > 0 -> {
        macroState = HomeostaticMacroState.ACTIVE_DEFENSE
        primaryReason = "Internal subsystem compromise detected by NK cell. Selective isolation in progress."
        confidence = 94
      }
      containedIncidentsCount > 0 || activeIncidentsCount > 0 -> {
        macroState = HomeostaticMacroState.ACTIVE_DEFENSE
        primaryReason = "Active anomaly containment executing under clinical effector supervision."
        confidence = 92
      }
      resolvingIncidentsCount > 0 -> {
        macroState = HomeostaticMacroState.RECOVERING
        primaryReason = "Threat cleared. Macrophage verifying cleanup and baseline return."
        confidence = 95
      }
      isBatteryStressed || isMemoryStressed -> {
        macroState = HomeostaticMacroState.STRESSED
        primaryReason = if (isBatteryStressed) "Host battery critical (< 15% discharging). Defensive conservation mode." else "Host memory constrained (< 250MB free). Cache trimming initiated."
        confidence = 98
      }
      observation.hasDegradedProviders -> {
        macroState = HomeostaticMacroState.WATCH
        primaryReason = "One or more non-critical telemetry providers reported degraded responsiveness."
        confidence = 88
      }
      else -> {
        macroState = HomeostaticMacroState.HOMEOSTATIC
        primaryReason = "All physiological baselines within normal limits. Full immune surveillance active."
        confidence = 98
      }
    }

    return HomeostasisEvaluation(
      macroState = macroState,
      confidenceScore = confidence,
      primaryReason = primaryReason,
      energy = energyDim,
      memory = memDim,
      network = netDim,
      stability = stabDim,
      security = secDim,
      integrity = intDim,
      immuneActivity = immDim,
      incidentLoad = incDim
    )
  }

  private fun createUnknownState(reason: String): HomeostasisEvaluation {
    return HomeostasisEvaluation(
      macroState = HomeostaticMacroState.UNKNOWN,
      confidenceScore = 0,
      primaryReason = reason,
      energy = EnergyDimension(0, false, 0f, false, false),
      memory = MemoryPressureDimension(0, 0, 0, false, 0, false),
      network = NetworkDimension("UNKNOWN", false, false, false),
      stability = StabilityDimension(0, 0, 0),
      security = SecurityDimension("UNKNOWN", false, false),
      integrity = RuntimeIntegrityDimension(0, 0, false),
      immuneActivity = ImmuneActivityDimension(0, 0.0, 0, false, false),
      incidentLoad = IncidentLoadDimension(0, 0, 0, 0)
    )
  }

  private fun createDegradedState(observation: CanonicalObservation, reason: String): HomeostasisEvaluation {
    val battVal = observation.battery.value
    val resVal = observation.resources.value
    return HomeostasisEvaluation(
      macroState = HomeostaticMacroState.DEGRADED,
      confidenceScore = 60,
      primaryReason = reason,
      energy = EnergyDimension(battVal.levelPercent, battVal.isCharging, battVal.temperatureCelsius, battVal.isPowerSaveMode, false),
      memory = MemoryPressureDimension(resVal.totalMemMb, resVal.availableMemMb, resVal.usedPercent, resVal.isLowMemory, resVal.vWatcherMemoryMb, false),
      network = NetworkDimension(observation.network.value.transportType, observation.network.value.isValidated, observation.network.value.transportType != "Disconnected", false),
      stability = StabilityDimension(observation.inventory.value.totalAppsCount, 0, 0),
      security = SecurityDimension(observation.system.value.securityPatch, observation.system.value.isScreenInteractive, true),
      integrity = RuntimeIntegrityDimension(1, 0, true),
      immuneActivity = ImmuneActivityDimension(0, 0.0, 0, false, false),
      incidentLoad = IncidentLoadDimension(0, 0, 0, 0)
    )
  }
}
