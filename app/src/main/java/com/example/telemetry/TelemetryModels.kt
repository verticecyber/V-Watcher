package com.example.telemetry

/**
 * Standard Telemetry Availability & Source Metadata
 */
enum class TelemetryAvailability(val displayName: String) {
  AVAILABLE("AVAILABLE"),
  STALE("STALE"),
  DEGRADED("DEGRADED"),
  UNAVAILABLE("UNAVAILABLE"),
  PERMISSION_REQUIRED("PERMISSION_REQUIRED"),
  ERROR("ERROR"),
  UNKNOWN("UNKNOWN")
}

data class ProviderProvenance(
  val providerId: String,
  val sourceSystem: String,
  val permissionRequired: String?,
  val isRealHardware: Boolean,
  val collectionTimestamp: Long = System.currentTimeMillis()
)

data class ProviderHealth(
  val providerId: String,
  val availability: TelemetryAvailability,
  val lastCollectedTimestamp: Long = System.currentTimeMillis(),
  val latencyMs: Long = 0L,
  val errorCount: Int = 0,
  val lastError: String? = null,
  val diagnosticNotes: String = ""
)

data class TelemetryResult<T>(
  val value: T,
  val timestamp: Long = System.currentTimeMillis(),
  val source: String,
  val availability: TelemetryAvailability,
  val permissionState: String,
  val isReal: Boolean = true,
  val diagnosticNotes: String = ""
)

/**
 * Real Host Device Battery Telemetry
 */
data class BatteryTelemetry(
  val levelPercent: Int,
  val isCharging: Boolean,
  val chargePlugType: String,
  val temperatureCelsius: Float,
  val health: String,
  val isPowerSaveMode: Boolean
)

/**
 * Real Host Device Memory & Process Resource Telemetry
 */
data class ResourceTelemetry(
  val availableMemBytes: Long,
  val totalMemBytes: Long,
  val isLowMemory: Boolean,
  val lowMemThresholdBytes: Long,
  val vWatcherMemoryBytes: Long
) {
  val availableMemMb: Long get() = availableMemBytes / (1024 * 1024)
  val totalMemMb: Long get() = totalMemBytes / (1024 * 1024)
  val vWatcherMemoryMb: Long get() = vWatcherMemoryBytes / (1024 * 1024)
  val usedPercent: Int
    get() = if (totalMemBytes > 0) {
      (((totalMemBytes - availableMemBytes).toDouble() / totalMemBytes) * 100).toInt()
    } else 0
}

/**
 * Real Host Network Interface Telemetry
 */
data class NetworkTelemetry(
  val transportType: String,
  val isValidated: Boolean,
  val isVpnActive: Boolean,
  val downstreamBandwidthKbps: Int,
  val upstreamBandwidthKbps: Int,
  val advancedPacketInspectionAvailable: Boolean = false,
  val vpnConsentRequiredNote: String = "Requires VPN authorization"
)

/**
 * Real Installed Application Telemetry
 */
data class InstalledAppInfo(
  val packageName: String,
  val appName: String,
  val versionName: String,
  val versionCode: Long,
  val isSystemApp: Boolean,
  val firstInstallTime: Long,
  val lastUpdateTime: Long,
  val requestedPermissions: List<String>
)

data class AppInventoryTelemetry(
  val totalAppsCount: Int,
  val systemAppsCount: Int,
  val userAppsCount: Int,
  val apps: List<InstalledAppInfo>
)

/**
 * Real Application Usage Statistics (Subject to user-granted PACKAGE_USAGE_STATS access)
 */
data class AppUsageEvent(
  val packageName: String,
  val appName: String,
  val eventType: String, // APP_FOREGROUND, APP_BACKGROUND, APP_ACTIVITY_CHANGE
  val timestamp: Long,
  val totalTimeInForegroundMs: Long,
  val lastTimeUsed: Long
)

data class AppUsageTelemetry(
  val isAccessGranted: Boolean,
  val usageExplanation: String = "V-Watcher uses this access to understand application activity and establish normal behavioral patterns.",
  val recentEvents: List<AppUsageEvent> = emptyList(),
  val foregroundAppPackage: String? = null
)

/**
 * Real System & Platform Environmental Telemetry
 */
data class SystemStateTelemetry(
  val osVersion: String,
  val sdkInt: Int,
  val manufacturer: String,
  val model: String,
  val device: String,
  val securityPatch: String,
  val locale: String,
  val timezone: String,
  val isScreenInteractive: Boolean
)
