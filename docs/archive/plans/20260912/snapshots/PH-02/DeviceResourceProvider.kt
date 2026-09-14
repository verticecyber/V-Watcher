package com.example.telemetry

import android.app.ActivityManager
import android.content.Context

class DeviceResourceProvider(private val context: Context) {

  fun collect(): TelemetryResult<ResourceTelemetry> {
    val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
    val memInfo = ActivityManager.MemoryInfo()

    val availableMem: Long
    val totalMem: Long
    val isLow: Boolean
    val threshold: Long

    if (activityManager != null) {
      activityManager.getMemoryInfo(memInfo)
      availableMem = memInfo.availMem
      totalMem = memInfo.totalMem
      isLow = memInfo.lowMemory
      threshold = memInfo.threshold
    } else {
      availableMem = Runtime.getRuntime().freeMemory()
      totalMem = Runtime.getRuntime().totalMemory()
      isLow = false
      threshold = 0
    }

    val runtime = Runtime.getRuntime()
    val vWatcherAllocatedBytes = runtime.totalMemory() - runtime.freeMemory()

    val availability = if (activityManager != null) TelemetryAvailability.AVAILABLE else TelemetryAvailability.DEGRADED
    val diagnosticNotes = if (activityManager != null) {
      "System RAM obtained via ActivityManager.MemoryInfo; App heap obtained via Runtime"
    } else {
      "ActivityManager unavailable; degraded to JVM process heap runtime measurement"
    }

    return TelemetryResult(
      value = ResourceTelemetry(
        availableMemBytes = availableMem,
        totalMemBytes = totalMem,
        isLowMemory = isLow,
        lowMemThresholdBytes = threshold,
        vWatcherMemoryBytes = vWatcherAllocatedBytes
      ),
      source = "Android ActivityManager & JVM Runtime",
      availability = availability,
      permissionState = "Granted (Standard System API)",
      isReal = true,
      diagnosticNotes = diagnosticNotes
    )
  }
}
