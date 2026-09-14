package com.example.telemetry

import android.app.AppOpsManager
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.os.Build
import android.os.Process

class AppUsageProvider(private val context: Context) {

  fun isUsageAccessGranted(): Boolean {
    val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as? AppOpsManager ?: return false
    val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      appOps.unsafeCheckOpNoThrow(
        AppOpsManager.OPSTR_GET_USAGE_STATS,
        Process.myUid(),
        context.packageName
      )
    } else {
      @Suppress("DEPRECATION")
      appOps.checkOpNoThrow(
        AppOpsManager.OPSTR_GET_USAGE_STATS,
        Process.myUid(),
        context.packageName
      )
    }
    return mode == AppOpsManager.MODE_ALLOWED
  }

  fun collect(): TelemetryResult<AppUsageTelemetry> {
    val isGranted = isUsageAccessGranted()
    if (!isGranted) {
      return TelemetryResult(
        value = AppUsageTelemetry(
          isAccessGranted = false,
          usageExplanation = "V-Watcher uses this access to understand application activity and establish normal behavioral patterns.",
          recentEvents = emptyList(),
          foregroundAppPackage = null
        ),
        source = "Android UsageStatsManager",
        availability = TelemetryAvailability.PERMISSION_REQUIRED,
        permissionState = "Not Enabled (Requires user grant in Settings)",
        isReal = true,
        diagnosticNotes = "Usage access is a user-controlled privacy permission. Detected as not yet granted."
      )
    }

    val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
    if (usageStatsManager == null) {
      return TelemetryResult(
        value = AppUsageTelemetry(isAccessGranted = true),
        source = "Android UsageStatsManager",
        availability = TelemetryAvailability.UNAVAILABLE,
        permissionState = "Service unavailable",
        isReal = false,
        diagnosticNotes = "UsageStatsManager system service not returned by context"
      )
    }

    val now = System.currentTimeMillis()
    val startTime = now - (24 * 60 * 60 * 1000L) // Past 24 hours
    val events = mutableListOf<AppUsageEvent>()
    var latestForegroundPackage: String? = null

    try {
      val usageEvents = usageStatsManager.queryEvents(startTime, now)
      val event = UsageEvents.Event()
      while (usageEvents.hasNextEvent()) {
        usageEvents.getNextEvent(event)
        val typeStr = when (event.eventType) {
          UsageEvents.Event.ACTIVITY_RESUMED -> "APP_FOREGROUND"
          UsageEvents.Event.ACTIVITY_PAUSED -> "APP_BACKGROUND"
          UsageEvents.Event.CONFIGURATION_CHANGE -> "APP_ACTIVITY_CHANGE"
          else -> null
        }
        if (typeStr != null) {
          if (event.eventType == UsageEvents.Event.ACTIVITY_RESUMED) {
            latestForegroundPackage = event.packageName
          }
          events.add(
            AppUsageEvent(
              packageName = event.packageName,
              appName = event.packageName.substringAfterLast('.'),
              eventType = typeStr,
              timestamp = event.timeStamp,
              totalTimeInForegroundMs = 0L,
              lastTimeUsed = event.timeStamp
            )
          )
        }
      }

      // Also get daily aggregate foreground time
      val statsList = usageStatsManager.queryUsageStats(
        UsageStatsManager.INTERVAL_DAILY,
        startTime,
        now
      )

      val sortedStats = statsList
        .filter { it.totalTimeInForeground > 0 }
        .sortedByDescending { it.lastTimeUsed }
        .take(15)
        .map { stat ->
          AppUsageEvent(
            packageName = stat.packageName,
            appName = stat.packageName.substringAfterLast('.'),
            eventType = "APP_FOREGROUND",
            timestamp = stat.lastTimeUsed,
            totalTimeInForegroundMs = stat.totalTimeInForeground,
            lastTimeUsed = stat.lastTimeUsed
          )
        }

      val displayEvents = if (events.isNotEmpty()) events.takeLast(20).reversed() else sortedStats

      return TelemetryResult(
        value = AppUsageTelemetry(
          isAccessGranted = true,
          usageExplanation = "V-Watcher uses this access to understand application activity and establish normal behavioral patterns.",
          recentEvents = displayEvents,
          foregroundAppPackage = latestForegroundPackage
        ),
        source = "Android UsageStatsManager",
        availability = TelemetryAvailability.AVAILABLE,
        permissionState = "Granted (User Authorized)",
        isReal = true,
        diagnosticNotes = "Real foreground/background app transitions queried from OS event log."
      )
    } catch (e: Exception) {
      return TelemetryResult(
        value = AppUsageTelemetry(isAccessGranted = true),
        source = "Android UsageStatsManager",
        availability = TelemetryAvailability.ERROR,
        permissionState = "Error querying events: ${e.message}",
        isReal = false,
        diagnosticNotes = e.localizedMessage ?: "Unknown error"
      )
    }
  }
}
