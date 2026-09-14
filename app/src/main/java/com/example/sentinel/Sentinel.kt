package com.example.sentinel

import android.content.Context
import android.os.SystemClock
import com.example.telemetry.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.UUID

data class SentinelState(
  val isRunning: Boolean = false,
  val observationCount: Long = 0L,
  val lastObservation: CanonicalObservation? = null,
  val lastObservationTime: Long = 0L,
  val lastObservationAgeMs: Long = 0L,
  val isStale: Boolean = false,
  val providerHealthMap: Map<String, ProviderHealth> = emptyMap(),
  val lastError: String? = null
)

interface Sentinel {
  val state: StateFlow<SentinelState>
  val events: SharedFlow<SentinelTelemetryEvent>

  suspend fun observeNow(): CanonicalObservation
  fun startContinuousObserving(scope: CoroutineScope, intervalMs: Long = 10_000L)
  fun stopContinuousObserving()
}

class AndroidSentinel(
  private val context: Context,
  private val batteryProvider: BatteryProvider = BatteryProvider(context),
  private val resourceProvider: DeviceResourceProvider = DeviceResourceProvider(context),
  private val networkProvider: NetworkTelemetryProvider = NetworkTelemetryProvider(context),
  private val appInventoryProvider: AppInventoryProvider = AppInventoryProvider(context),
  private val appUsageProvider: AppUsageProvider = AppUsageProvider(context),
  private val systemStateProvider: SystemStateProvider = SystemStateProvider(context),
  private val stalenessThresholdMs: Long = 30_000L
) : Sentinel {

  private val _state = MutableStateFlow(SentinelState())
  override val state: StateFlow<SentinelState> = _state.asStateFlow()

  private val _events = MutableSharedFlow<SentinelTelemetryEvent>(replay = 1, extraBufferCapacity = 64)
  override val events: SharedFlow<SentinelTelemetryEvent> = _events.asSharedFlow()

  private var continuousJob: Job? = null
  private val providerHealthStore = mutableMapOf<String, ProviderHealth>()

  override suspend fun observeNow(): CanonicalObservation = withContext(Dispatchers.IO) {
    val observationId = "obs_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(8)}"
    val captureStart = SystemClock.elapsedRealtime()
    val now = System.currentTimeMillis()

    // 1. Collect from each provider and track provider health + provenance
    val (batteryResult, batteryHealth) = measureCollection("BatteryProvider") {
      batteryProvider.collect()
    }

    val (resourceResult, resourceHealth) = measureCollection("DeviceResourceProvider") {
      resourceProvider.collect()
    }

    val (networkResult, networkHealth) = measureCollection("NetworkTelemetryProvider") {
      networkProvider.collect()
    }

    val (inventoryResult, inventoryHealth) = measureCollection("AppInventoryProvider") {
      appInventoryProvider.collect()
    }

    val (usageResult, usageHealth) = measureCollection("AppUsageProvider") {
      appUsageProvider.collect()
    }

    val (systemResult, systemHealth) = measureCollection("SystemStateProvider") {
      systemStateProvider.collect()
    }

    // Update internal health registry
    providerHealthStore["BatteryProvider"] = batteryHealth
    providerHealthStore["DeviceResourceProvider"] = resourceHealth
    providerHealthStore["NetworkTelemetryProvider"] = networkHealth
    providerHealthStore["AppInventoryProvider"] = inventoryHealth
    providerHealthStore["AppUsageProvider"] = usageHealth
    providerHealthStore["SystemStateProvider"] = systemHealth

    // 2. Build explicit provenance records
    val provenanceList = listOf(
      ProviderProvenance(
        providerId = "BatteryProvider",
        sourceSystem = batteryResult.source,
        permissionRequired = batteryResult.permissionState,
        isRealHardware = batteryResult.isReal,
        collectionTimestamp = batteryResult.timestamp
      ),
      ProviderProvenance(
        providerId = "DeviceResourceProvider",
        sourceSystem = resourceResult.source,
        permissionRequired = resourceResult.permissionState,
        isRealHardware = resourceResult.isReal,
        collectionTimestamp = resourceResult.timestamp
      ),
      ProviderProvenance(
        providerId = "NetworkTelemetryProvider",
        sourceSystem = networkResult.source,
        permissionRequired = networkResult.permissionState,
        isRealHardware = networkResult.isReal,
        collectionTimestamp = networkResult.timestamp
      ),
      ProviderProvenance(
        providerId = "AppInventoryProvider",
        sourceSystem = inventoryResult.source,
        permissionRequired = inventoryResult.permissionState,
        isRealHardware = inventoryResult.isReal,
        collectionTimestamp = inventoryResult.timestamp
      ),
      ProviderProvenance(
        providerId = "AppUsageProvider",
        sourceSystem = usageResult.source,
        permissionRequired = usageResult.permissionState,
        isRealHardware = usageResult.isReal,
        collectionTimestamp = usageResult.timestamp
      ),
      ProviderProvenance(
        providerId = "SystemStateProvider",
        sourceSystem = systemResult.source,
        permissionRequired = systemResult.permissionState,
        isRealHardware = systemResult.isReal,
        collectionTimestamp = systemResult.timestamp
      )
    )

    // 3. Compute Freshness
    val previousObs = _state.value.lastObservation
    val observationAgeMs = if (previousObs != null) now - previousObs.timestamp else 0L
    val isStale = observationAgeMs > stalenessThresholdMs

    val freshnessInfo = FreshnessInfo(
      capturedAt = now,
      observationAgeMs = observationAgeMs,
      isStale = isStale,
      stalestProviderId = providerHealthStore.minByOrNull { it.value.lastCollectedTimestamp }?.key,
      freshestProviderId = providerHealthStore.maxByOrNull { it.value.lastCollectedTimestamp }?.key
    )

    val canonicalObservation = CanonicalObservation(
      observationId = observationId,
      timestamp = now,
      battery = batteryResult,
      resources = resourceResult,
      network = networkResult,
      inventory = inventoryResult,
      usage = usageResult,
      system = systemResult,
      providerStatus = providerHealthStore.toMap(),
      freshness = freshnessInfo,
      provenance = provenanceList
    )

    // 4. Update Sentinel state
    _state.update {
      it.copy(
        observationCount = it.observationCount + 1,
        lastObservation = canonicalObservation,
        lastObservationTime = now,
        lastObservationAgeMs = 0L,
        isStale = isStale,
        providerHealthMap = providerHealthStore.toMap(),
        lastError = null
      )
    }

    // 5. Emit structured event
    val event = SentinelTelemetryEvent(
      eventId = "evt_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}",
      timestamp = now,
      observationId = observationId,
      eventType = if (isStale) SentinelEventType.STALENESS_DETECTED else SentinelEventType.SNAPSHOT_CAPTURED,
      description = "Canonical observation captured with ${canonicalObservation.providerStatus.size} providers",
      observation = canonicalObservation
    )
    _events.emit(event)

    canonicalObservation
  }

  override fun startContinuousObserving(scope: CoroutineScope, intervalMs: Long) {
    continuousJob?.cancel()
    _state.update { it.copy(isRunning = true) }

    continuousJob = scope.launch {
      while (isActive) {
        try {
          observeNow()
        } catch (e: CancellationException) {
          throw e
        } catch (e: Exception) {
          _state.update { it.copy(lastError = e.message) }
        }
        delay(intervalMs)
      }
    }
  }

  override fun stopContinuousObserving() {
    continuousJob?.cancel()
    continuousJob = null
    _state.update { it.copy(isRunning = false) }
  }

  private inline fun <T> measureCollection(
    providerId: String,
    collector: () -> TelemetryResult<T>
  ): Pair<TelemetryResult<T>, ProviderHealth> {
    val start = SystemClock.elapsedRealtime()
    return try {
      val result = collector()
      val duration = SystemClock.elapsedRealtime() - start
      val previousHealth = providerHealthStore[providerId]
      val health = ProviderHealth(
        providerId = providerId,
        availability = result.availability,
        lastCollectedTimestamp = result.timestamp,
        latencyMs = duration,
        errorCount = previousHealth?.errorCount ?: 0,
        lastError = if (result.availability == TelemetryAvailability.ERROR) result.diagnosticNotes else previousHealth?.lastError,
        diagnosticNotes = result.diagnosticNotes
      )
      Pair(result, health)
    } catch (e: Exception) {
      val duration = SystemClock.elapsedRealtime() - start
      val previousHealth = providerHealthStore[providerId]
      val errCount = (previousHealth?.errorCount ?: 0) + 1
      val health = ProviderHealth(
        providerId = providerId,
        availability = TelemetryAvailability.ERROR,
        lastCollectedTimestamp = System.currentTimeMillis(),
        latencyMs = duration,
        errorCount = errCount,
        lastError = e.message ?: "Collection threw an exception",
        diagnosticNotes = "Exception: ${e.localizedMessage}"
      )
      // Provide an empty/safe TelemetryResult preserving the ERROR state
      val dummyResult = TelemetryResult(
        value = collectorSafeFallback(providerId) as T,
        source = providerId,
        availability = TelemetryAvailability.ERROR,
        permissionState = "Exception",
        isReal = false,
        diagnosticNotes = e.message ?: "Collection failure"
      )
      Pair(dummyResult, health)
    }
  }

  @Suppress("UNCHECKED_CAST")
  private fun collectorSafeFallback(providerId: String): Any {
    return when (providerId) {
      "BatteryProvider" -> BatteryTelemetry(
        levelPercent = 0,
        isCharging = false,
        chargePlugType = "Unknown",
        temperatureCelsius = 0f,
        health = "Unknown",
        isPowerSaveMode = false
      )
      "DeviceResourceProvider" -> ResourceTelemetry(0, 0, false, 0, 0)
      "NetworkTelemetryProvider" -> NetworkTelemetry("Unknown", false, false, 0, 0)
      "AppInventoryProvider" -> AppInventoryTelemetry(0, 0, 0, emptyList())
      "AppUsageProvider" -> AppUsageTelemetry(false)
      else -> SystemStateTelemetry("Android", 0, "Unknown", "Unknown", "Unknown", "", "", "", false)
    }
  }
}
