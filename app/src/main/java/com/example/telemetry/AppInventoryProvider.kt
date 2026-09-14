package com.example.telemetry

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build

class AppInventoryProvider(private val context: Context) {

  fun collect(): TelemetryResult<AppInventoryTelemetry> {
    val pm = context.packageManager
    val appList = mutableListOf<InstalledAppInfo>()

    try {
      val packages: List<PackageInfo> = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        pm.getInstalledPackages(PackageManager.PackageInfoFlags.of(PackageManager.GET_PERMISSIONS.toLong()))
      } else {
        @Suppress("DEPRECATION")
        pm.getInstalledPackages(PackageManager.GET_PERMISSIONS)
      }

      for (pkg in packages) {
        val appInfo = pkg.applicationInfo ?: continue
        val appName = try {
          pm.getApplicationLabel(appInfo).toString()
        } catch (_: Exception) {
          pkg.packageName
        }
        val isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
        val vCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
          pkg.longVersionCode
        } else {
          @Suppress("DEPRECATION")
          pkg.versionCode.toLong()
        }
        val perms = pkg.requestedPermissions?.toList() ?: emptyList()

        appList.add(
          InstalledAppInfo(
            packageName = pkg.packageName,
            appName = appName.ifBlank { pkg.packageName },
            versionName = pkg.versionName ?: "1.0",
            versionCode = vCode,
            isSystemApp = isSystem,
            firstInstallTime = pkg.firstInstallTime,
            lastUpdateTime = pkg.lastUpdateTime,
            requestedPermissions = perms
          )
        )
      }

      val systemCount = appList.count { it.isSystemApp }
      val userCount = appList.count { !it.isSystemApp }

      return TelemetryResult(
        value = AppInventoryTelemetry(
          totalAppsCount = appList.size,
          systemAppsCount = systemCount,
          userAppsCount = userCount,
          apps = appList.sortedBy { it.appName.lowercase() }
        ),
        source = "Android PackageManager",
        availability = TelemetryAvailability.AVAILABLE,
        permissionState = "Granted (QUERY_ALL_PACKAGES)",
        isReal = true,
        diagnosticNotes = "Local on-device package enumeration. Scoped strictly to device memory."
      )
    } catch (e: Exception) {
      return TelemetryResult(
        value = AppInventoryTelemetry(
          totalAppsCount = 0,
          systemAppsCount = 0,
          userAppsCount = 0,
          apps = emptyList()
        ),
        source = "Android PackageManager",
        availability = TelemetryAvailability.ERROR,
        permissionState = "Error: ${e.message}",
        isReal = false,
        diagnosticNotes = "Package enumeration failed: ${e.localizedMessage}"
      )
    }
  }
}
