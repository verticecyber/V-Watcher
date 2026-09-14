package com.example.telemetry

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class DeviceTelemetrySnapshot(
  val timestamp: Long = System.currentTimeMillis(),
  val battery: TelemetryResult<BatteryTelemetry>,
  val resources: TelemetryResult<ResourceTelemetry>,
  val network: TelemetryResult<NetworkTelemetry>,
  val appInventory: TelemetryResult<AppInventoryTelemetry>,
  val appUsage: TelemetryResult<AppUsageTelemetry>,
  val systemState: TelemetryResult<SystemStateTelemetry>
)

class DeviceTelemetryProvider(private val context: Context) {
  val batteryProvider = BatteryProvider(context)
  val resourceProvider = DeviceResourceProvider(context)
  val networkProvider = NetworkTelemetryProvider(context)
  val appInventoryProvider = AppInventoryProvider(context)
  val appUsageProvider = AppUsageProvider(context)
  val systemStateProvider = SystemStateProvider(context)

  suspend fun captureSnapshot(): DeviceTelemetrySnapshot = withContext(Dispatchers.IO) {
    val battery = batteryProvider.collect()
    val resources = resourceProvider.collect()
    val network = networkProvider.collect()
    val appInventory = appInventoryProvider.collect()
    val appUsage = appUsageProvider.collect()
    val system = systemStateProvider.collect()

    DeviceTelemetrySnapshot(
      timestamp = System.currentTimeMillis(),
      battery = battery,
      resources = resources,
      network = network,
      appInventory = appInventory,
      appUsage = appUsage,
      systemState = system
    )
  }
}
