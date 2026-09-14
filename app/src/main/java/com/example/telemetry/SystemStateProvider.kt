package com.example.telemetry

import android.content.Context
import android.os.Build
import android.os.PowerManager
import java.util.Locale
import java.util.TimeZone

class SystemStateProvider(private val context: Context) {

  fun collect(): TelemetryResult<SystemStateTelemetry> {
    val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
    val isInteractive = powerManager?.isInteractive ?: true

    val securityPatch = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      Build.VERSION.SECURITY_PATCH
    } else {
      "Standard Release"
    }

    return TelemetryResult(
      value = SystemStateTelemetry(
        osVersion = Build.VERSION.RELEASE ?: "Android",
        sdkInt = Build.VERSION.SDK_INT,
        manufacturer = Build.MANUFACTURER?.replaceFirstChar { it.uppercase() } ?: "Android",
        model = Build.MODEL ?: "Generic Device",
        device = Build.DEVICE ?: "Generic",
        securityPatch = securityPatch,
        locale = Locale.getDefault().toLanguageTag(),
        timezone = TimeZone.getDefault().id,
        isScreenInteractive = isInteractive
      ),
      source = "Android Build & System Environment",
      availability = TelemetryAvailability.AVAILABLE,
      permissionState = "Granted (Standard System API)",
      isReal = true,
      diagnosticNotes = "Host platform specifications collected without privileged kernel assumptions."
    )
  }
}
