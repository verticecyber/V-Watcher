package com.example.telemetry

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.PowerManager

class BatteryProvider(private val context: Context) {

  fun collect(): TelemetryResult<BatteryTelemetry> {
    val batteryIntent: Intent? = context.registerReceiver(
      null,
      IntentFilter(Intent.ACTION_BATTERY_CHANGED)
    )

    val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
    val isPowerSave = powerManager?.isPowerSaveMode ?: false

    if (batteryIntent == null) {
      return TelemetryResult(
        value = BatteryTelemetry(
          levelPercent = 100,
          isCharging = false,
          chargePlugType = "Unknown",
          temperatureCelsius = 25.0f,
          health = "Good",
          isPowerSaveMode = isPowerSave
        ),
        source = "BatteryManager",
        availability = TelemetryAvailability.UNAVAILABLE,
        permissionState = "Granted (Standard API)",
        isReal = false,
        diagnosticNotes = "Sticky battery intent returned null"
      )
    }

    val rawLevel = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
    val scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
    val level = if (rawLevel >= 0 && scale > 0) {
      ((rawLevel.toFloat() / scale.toFloat()) * 100).toInt()
    } else 100

    val status = batteryIntent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
    val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
        status == BatteryManager.BATTERY_STATUS_FULL

    val plugged = batteryIntent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)
    val plugType = when (plugged) {
      BatteryManager.BATTERY_PLUGGED_AC -> "AC"
      BatteryManager.BATTERY_PLUGGED_USB -> "USB"
      BatteryManager.BATTERY_PLUGGED_WIRELESS -> "Wireless"
      else -> if (isCharging) "Charging" else "Discharging"
    }

    val rawTemp = batteryIntent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 250)
    val tempC = rawTemp / 10.0f

    val rawHealth = batteryIntent.getIntExtra(BatteryManager.EXTRA_HEALTH, BatteryManager.BATTERY_HEALTH_UNKNOWN)
    val healthStr = when (rawHealth) {
      BatteryManager.BATTERY_HEALTH_GOOD -> "Good"
      BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheat"
      BatteryManager.BATTERY_HEALTH_DEAD -> "Dead"
      BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Over Voltage"
      BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> "Failure"
      BatteryManager.BATTERY_HEALTH_COLD -> "Cold"
      else -> "Normal"
    }

    return TelemetryResult(
      value = BatteryTelemetry(
        levelPercent = level,
        isCharging = isCharging,
        chargePlugType = plugType,
        temperatureCelsius = tempC,
        health = healthStr,
        isPowerSaveMode = isPowerSave
      ),
      source = "Android BatteryManager",
      availability = TelemetryAvailability.AVAILABLE,
      permissionState = "Granted (Standard System API)",
      isReal = true,
      diagnosticNotes = "Live battery telemetry collected from kernel power supply driver"
    )
  }
}
