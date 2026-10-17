package com.example.contract

import android.app.ActivityManager
import android.app.AppOpsManager
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.os.BatteryManager
import android.os.Process
import androidx.test.core.app.ApplicationProvider
import com.example.telemetry.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowActivityManager
import org.robolectric.shadows.ShadowAppOpsManager
import org.robolectric.shadows.ShadowPackageManager

/**
 * ANDROID CONTRACT / ROBOLECTRIC IN-PROCESS TEST SUITE
 * 
 * Verifies that each telemetry provider properly queries Android platform APIs
 * and translates Android system states into typed TelemetryResult models.
 *
 * PROVENANCE: Executes against Robolectric Android API shadows on local JVM.
 * Proves API consumption and translation contract.
 * Does not prove physical external hardware kernel events.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class AndroidProviderContractTest {

  private lateinit var context: Context
  private lateinit var application: Application

  @Before
  fun setUp() {
    application = ApplicationProvider.getApplicationContext()
    context = application
  }

  // =========================================================================
  // 1. BATTERY MANAGER CONTRACT
  // =========================================================================

  @Test
  fun batteryProvider_normalState_translatesAccurately() {
    val batteryIntent = Intent(Intent.ACTION_BATTERY_CHANGED).apply {
      putExtra(BatteryManager.EXTRA_LEVEL, 85)
      putExtra(BatteryManager.EXTRA_SCALE, 100)
      putExtra(BatteryManager.EXTRA_STATUS, BatteryManager.BATTERY_STATUS_DISCHARGING)
      putExtra(BatteryManager.EXTRA_PLUGGED, 0)
      putExtra(BatteryManager.EXTRA_TEMPERATURE, 295) // 29.5°C
      putExtra(BatteryManager.EXTRA_HEALTH, BatteryManager.BATTERY_HEALTH_GOOD)
    }
    application.sendStickyBroadcast(batteryIntent)

    val provider = BatteryProvider(context)
    val result = provider.collect()

    assertEquals(TelemetryAvailability.AVAILABLE, result.availability)
    assertTrue(result.isReal)
    assertEquals(85, result.value.levelPercent)
    assertFalse(result.value.isCharging)
    assertEquals(29.5f, result.value.temperatureCelsius, 0.1f)
    assertEquals("Good", result.value.health)
  }

  @Test
  fun batteryProvider_temperatureThreshold_capturedAccurately() {
    val batteryIntent = Intent(Intent.ACTION_BATTERY_CHANGED).apply {
      putExtra(BatteryManager.EXTRA_LEVEL, 40)
      putExtra(BatteryManager.EXTRA_SCALE, 100)
      putExtra(BatteryManager.EXTRA_STATUS, BatteryManager.BATTERY_STATUS_CHARGING)
      putExtra(BatteryManager.EXTRA_PLUGGED, BatteryManager.BATTERY_PLUGGED_AC)
      putExtra(BatteryManager.EXTRA_TEMPERATURE, 465) // 46.5°C (overheat ceiling)
      putExtra(BatteryManager.EXTRA_HEALTH, BatteryManager.BATTERY_HEALTH_OVERHEAT)
    }
    application.sendStickyBroadcast(batteryIntent)

    val provider = BatteryProvider(context)
    val result = provider.collect()

    assertEquals(TelemetryAvailability.AVAILABLE, result.availability)
    assertTrue(result.isReal)
    assertTrue(result.value.isCharging)
    assertEquals("AC", result.value.chargePlugType)
    assertEquals(46.5f, result.value.temperatureCelsius, 0.1f)
    assertEquals("Overheat", result.value.health)
  }

  // =========================================================================
  // 2. ACTIVITY MANAGER CONTRACT (RESOURCES)
  // =========================================================================

  @Test
  fun resourceProvider_normalMemory_translatesAccurately() {
    val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    val shadowAm: ShadowActivityManager = shadowOf(am)
    
    val memInfo = ActivityManager.MemoryInfo().apply {
      availMem = 1024L * 1024L * 1024L // 1024 MB
      totalMem = 2048L * 1024L * 1024L // 2048 MB
      lowMemory = false
      threshold = 200L * 1024L * 1024L
    }
    shadowAm.setMemoryInfo(memInfo)

    val provider = DeviceResourceProvider(context)
    val result = provider.collect()

    assertEquals(TelemetryAvailability.AVAILABLE, result.availability)
    assertTrue(result.isReal)
    assertEquals(1024L, result.value.availableMemMb)
    assertFalse(result.value.isLowMemory)
  }

  @Test
  fun resourceProvider_lowMemoryCondition_triggersLowMemFlag() {
    val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    val shadowAm: ShadowActivityManager = shadowOf(am)

    val memInfo = ActivityManager.MemoryInfo().apply {
      availMem = 120L * 1024L * 1024L // 120 MB
      totalMem = 2048L * 1024L * 1024L
      lowMemory = true
      threshold = 200L * 1024L * 1024L
    }
    shadowAm.setMemoryInfo(memInfo)

    val provider = DeviceResourceProvider(context)
    val result = provider.collect()

    assertEquals(TelemetryAvailability.AVAILABLE, result.availability)
    assertTrue(result.isReal)
    assertEquals(120L, result.value.availableMemMb)
    assertTrue(result.value.isLowMemory)
  }

  // =========================================================================
  // 3. CONNECTIVITY MANAGER CONTRACT
  // =========================================================================

  @Test
  fun networkProvider_translatesAccurately() {
    val provider = NetworkTelemetryProvider(context)
    val result = provider.collect()

    assertEquals(TelemetryAvailability.AVAILABLE, result.availability)
    assertTrue(result.isReal)
    assertNotNull(result.value.transportType)
  }

  // =========================================================================
  // 4. PACKAGE MANAGER CONTRACT (APP INVENTORY)
  // =========================================================================

  @Test
  fun appInventoryProvider_collectsInstalledPackages() {
    val pm = context.packageManager
    val shadowPm: ShadowPackageManager = shadowOf(pm)

    // Add a synthetic third-party package to shadow package manager
    val pkgInfo = PackageInfo().apply {
      packageName = "com.sample.testapp"
      versionName = "2.1"
      applicationInfo = ApplicationInfo().apply {
        packageName = "com.sample.testapp"
        flags = 0 // User app, not system
      }
      requestedPermissions = arrayOf("android.permission.CAMERA")
    }
    shadowPm.addPackage(pkgInfo)

    val provider = AppInventoryProvider(context)
    val result = provider.collect()

    assertEquals(TelemetryAvailability.AVAILABLE, result.availability)
    assertTrue(result.isReal)
    assertTrue(result.value.totalAppsCount > 0)
    assertTrue(result.value.apps.any { it.packageName == "com.sample.testapp" })
    val installed = result.value.apps.first { it.packageName == "com.sample.testapp" }
    assertFalse(installed.isSystemApp)
    assertEquals("2.1", installed.versionName)
    assertTrue(installed.requestedPermissions.contains("android.permission.CAMERA"))
  }

  // =========================================================================
  // 5. APPOPS / USAGE STATS CONTRACT
  // =========================================================================

  @Test
  fun appUsageProvider_denied_honestlyYieldsPermissionRequired() {
    val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
    val shadowAppOps: ShadowAppOpsManager = shadowOf(appOps)

    shadowAppOps.setMode(
      AppOpsManager.OPSTR_GET_USAGE_STATS,
      Process.myUid(),
      context.packageName,
      AppOpsManager.MODE_IGNORED
    )

    val provider = AppUsageProvider(context)
    assertFalse(provider.isUsageAccessGranted())

    val result = provider.collect()
    assertEquals(TelemetryAvailability.PERMISSION_REQUIRED, result.availability)
    assertFalse(result.isReal)
    assertFalse(result.value.isAccessGranted)
  }

  @Test
  fun appUsageProvider_granted_reflectsAccess() {
    val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
    val shadowAppOps: ShadowAppOpsManager = shadowOf(appOps)

    shadowAppOps.setMode(
      AppOpsManager.OPSTR_GET_USAGE_STATS,
      Process.myUid(),
      context.packageName,
      AppOpsManager.MODE_ALLOWED
    )

    val provider = AppUsageProvider(context)
    assertTrue(provider.isUsageAccessGranted())

    val result = provider.collect()
    assertTrue(result.value.isAccessGranted)
  }
}
