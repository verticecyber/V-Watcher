package com.example.sentinel

import com.example.telemetry.*

data class FreshnessInfo(
  val capturedAt: Long,
  val observationAgeMs: Long,
  val isStale: Boolean,
  val stalestProviderId: String? = null,
  val freshestProviderId: String? = null
)

/**
 * Canonical Sentinel Observation.
 * Adheres strictly to Section 4 telemetry contract:
 * observation_id, timestamp, battery, resources, network, inventory,
 * usage, system, provider_status, freshness, provenance.
 */
data class CanonicalObservation(
  val observationId: String,
  val timestamp: Long,
  val battery: TelemetryResult<BatteryTelemetry>,
  val resources: TelemetryResult<ResourceTelemetry>,
  val network: TelemetryResult<NetworkTelemetry>,
  val inventory: TelemetryResult<AppInventoryTelemetry>,
  val usage: TelemetryResult<AppUsageTelemetry>,
  val system: TelemetryResult<SystemStateTelemetry>,
  val providerStatus: Map<String, ProviderHealth>,
  val freshness: FreshnessInfo,
  val provenance: List<ProviderProvenance>
) {
  /**
   * Helper to check if any critical sensor is degraded or failed
   */
  val hasDegradedProviders: Boolean
    get() = providerStatus.values.any {
      it.availability == TelemetryAvailability.DEGRADED ||
      it.availability == TelemetryAvailability.ERROR ||
      it.availability == TelemetryAvailability.STALE
    }

  val hasUnavailableProviders: Boolean
    get() = providerStatus.values.any {
      it.availability == TelemetryAvailability.UNAVAILABLE ||
      it.availability == TelemetryAvailability.PERMISSION_REQUIRED
    }
}

/**
 * Structured Telemetry Event emitted by Sentinel to the communication layer.
 */
data class SentinelTelemetryEvent(
  val eventId: String,
  val timestamp: Long = System.currentTimeMillis(),
  val observationId: String,
  val eventType: SentinelEventType,
  val description: String,
  val observation: CanonicalObservation
)

enum class SentinelEventType {
  SNAPSHOT_CAPTURED,
  PROVIDER_STATE_CHANGED,
  STALENESS_DETECTED,
  COLLECTION_FAILED
}
