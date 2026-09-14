package com.example.immune

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import java.io.File
import java.util.UUID

/**
 * Android Reality Boundary (Section 17).
 * Strictly bridges biological immune metaphors to real Android APIs.
 *
 * Biological Action <-> Android Mechanism <-> Permission <-> Limitations <-> Failure Modes:
 * 1. Neutrophil Degranulation -> Throttle V-Watcher's own coroutines & inference engine.
 * 2. Macrophage Phagocytosis -> Trim internal cache files & garbage collection. Produces real bytes-freed proof.
 * 3. NK Cell Apoptosis -> Cancel & restart V-Watcher's own workers or isolate corrupted internal memory state.
 * 4. Cytotoxic Targeted Lysis:
 *    - Internal: Quarantines V-Watcher cached objects or isolates memory buffers.
 *    - External: Navigates user to Android App Settings. For killing external apps: UNAVAILABLE due to sandbox!
 */
class AndroidRealityBoundary(private val context: Context) {

  // Track internally isolated subsystems (thread-safe: mutated on Default dispatcher, read anywhere)
  private val isolatedSubsystems = java.util.concurrent.ConcurrentHashMap.newKeySet<String>()

  @Volatile
  private var isInternalInferenceThrottled: Boolean = false

  fun isSubsystemIsolated(subsystemName: String): Boolean = isolatedSubsystems.contains(subsystemName)

  fun isInferenceThrottled(): Boolean = isInternalInferenceThrottled

  fun executeRealAction(
    actionName: String,
    incidentId: String,
    parameters: Map<String, String>
  ): ActionResult {
    val actionId = "act_${UUID.randomUUID().toString().take(8)}"

    return when (actionName) {
      "THROTTLE_INTERNAL_INFERENCE" -> {
        isInternalInferenceThrottled = true
        ActionResult(
          actionId = actionId,
          targetAction = actionName,
          status = ActionExecutionStatus.EXECUTED,
          platformReason = "V-Watcher internal reasoning engine throttled to deterministic-only mode to preserve battery and memory.",
          evidence = mapOf(
            "throttledState" to "true",
            "incidentId" to incidentId,
            "mechanism" to "V-Watcher Coroutine Dispatcher Throttling"
          )
        )
      }

      "ISOLATE_INTERNAL_SUBSYSTEM" -> {
        val targetSubsystem = parameters["subsystem"] ?: "UNKNOWN_SUBSYSTEM"
        isolatedSubsystems.add(targetSubsystem)
        ActionResult(
          actionId = actionId,
          targetAction = actionName,
          status = ActionExecutionStatus.EXECUTED,
          platformReason = "Subsystem '$targetSubsystem' isolated from live message bus.",
          evidence = mapOf(
            "isolatedSubsystem" to targetSubsystem,
            "activeIsolatedCount" to isolatedSubsystems.size.toString(),
            "incidentId" to incidentId
          )
        )
      }

      "RECLAIM_INTERNAL_CACHE" -> {
        // Real Android cleanup: inspect and trim app cache directory
        var bytesFreed = 0L
        var filesRemoved = 0
        try {
          val cacheDir: File? = context.cacheDir
          if (cacheDir != null && cacheDir.exists()) {
            val files = cacheDir.listFiles() ?: emptyArray()
            for (file in files) {
              if (file.isFile && file.name.startsWith("vwatcher_temp_")) {
                val len = file.length()
                if (file.delete()) {
                  bytesFreed += len
                  filesRemoved++
                }
              }
            }
          }
          // Suggest GC to system without blocking
          System.gc()
        } catch (e: Exception) {
          return ActionResult(
            actionId = actionId,
            targetAction = actionName,
            status = ActionExecutionStatus.FAILED,
            platformReason = "Cache reclamation failed: ${e.message}",
            evidence = mapOf("error" to (e.message ?: "Unknown IO error"))
          )
        }

        ActionResult(
          actionId = actionId,
          targetAction = actionName,
          status = ActionExecutionStatus.EXECUTED,
          platformReason = "App internal cache trimmed. $filesRemoved stale files purged, reclaiming $bytesFreed bytes.",
          evidence = mapOf(
            "bytesFreed" to bytesFreed.toString(),
            "filesRemoved" to filesRemoved.toString(),
            "targetDirectory" to (context.cacheDir?.absolutePath ?: "unknown")
          )
        )
      }

      "NAVIGATE_APP_SETTINGS" -> {
        val targetPackage = parameters["package"] ?: context.packageName
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
          data = Uri.fromParts("package", targetPackage, null)
          addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
          context.startActivity(intent)
          ActionResult(
            actionId = actionId,
            targetAction = actionName,
            status = ActionExecutionStatus.EXECUTED,
            platformReason = "Android Settings launched for target package: $targetPackage",
            evidence = mapOf(
              "intentAction" to Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
              "targetPackage" to targetPackage,
              "launched" to "true"
            )
          )
        } catch (e: Exception) {
          ActionResult(
            actionId = actionId,
            targetAction = actionName,
            status = ActionExecutionStatus.FAILED,
            platformReason = "Settings launch failed: ${e.message}",
            evidence = mapOf(
              "intentAction" to Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
              "targetPackage" to targetPackage,
              "launched" to "false",
              "error" to (e.message ?: "Unknown launch error")
            )
          )
        }
      }

      "KILL_EXTERNAL_PROCESS" -> {
        val targetPackage = parameters["package"] ?: "com.unknown.external"
        // Strict Android Reality check:
        // A normal Android app CANNOT kill another app's process without system/root privileges.
        // We MUST NOT fake execution!
        ActionResult(
          actionId = actionId,
          targetAction = actionName,
          status = ActionExecutionStatus.UNAVAILABLE,
          platformReason = "Android platform security sandbox prevents third-party applications from terminating external processes without root privileges or system signature (android.permission.KILL_BACKGROUND_PROCESSES only hints system memory manager). Action safely converted to clinical user recommendation.",
          evidence = mapOf(
            "targetPackage" to targetPackage,
            "requiredPlatformCapability" to "root/system_server",
            "recommendedAlternative" to "NAVIGATE_APP_SETTINGS"
          )
        )
      }

      "RESET_SUBSYSTEM_ISOLATION" -> {
        val targetSubsystem = parameters["subsystem"] ?: "ALL"
        if (targetSubsystem == "ALL") {
          isolatedSubsystems.clear()
        } else {
          isolatedSubsystems.remove(targetSubsystem)
        }
        isInternalInferenceThrottled = false
        ActionResult(
          actionId = actionId,
          targetAction = actionName,
          status = ActionExecutionStatus.EXECUTED,
          platformReason = "Internal isolation cleared. Subsystems returned to normal operational baseline.",
          evidence = mapOf("targetSubsystem" to targetSubsystem)
        )
      }

      else -> {
        ActionResult(
          actionId = actionId,
          targetAction = actionName,
          status = ActionExecutionStatus.DENIED,
          platformReason = "Action '$actionName' is not in the certified clinical Android actuator registry.",
          evidence = emptyMap()
        )
      }
    }
  }
}
