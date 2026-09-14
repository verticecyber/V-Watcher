package com.example.reasoning

import android.os.SystemClock
import com.example.communication.FallbackReason
import com.example.sentinel.CanonicalObservation
import kotlinx.coroutines.sync.Mutex

sealed class GuardrailDecision {
  object Allowed : GuardrailDecision()
  data class Denied(val reason: FallbackReason, val explanation: String) : GuardrailDecision()
}

/**
 * Hardware & System Resource Guardrails.
 * Strictly adheres to Section 11:
 * - Suppresses inference when battery is critical (<15%) and discharging
 * - Prevents rapid repeated activations (rate-limiting / cooldown)
 * - Respects severe memory pressure
 * - Enforces concurrency safety (mutex lock against concurrent local LLM execution)
 * - Exposes explicit denial reason for deterministic fallback
 */
class ResourceGuardrails(
  private val minIntervalMs: Long = 3000L,
  private val criticalBatteryPercent: Int = 15
) {
  private var lastExecutionTime: Long = 0L
  val concurrencyLock = Mutex()

  @Synchronized
  fun evaluate(observation: CanonicalObservation): GuardrailDecision {
    // 1. Critical Battery Check
    val battery = observation.battery.value
    if (battery.levelPercent < criticalBatteryPercent && !battery.isCharging) {
      return GuardrailDecision.Denied(
        reason = FallbackReason.BATTERY_CRITICAL_DISCHARGING,
        explanation = "Battery is critical (${battery.levelPercent}%) and discharging. Local model inference suppressed to prevent power depletion."
      )
    }

    // 2. Severe Memory Pressure Check
    val resources = observation.resources.value
    if (resources.isLowMemory || resources.usedPercent > 92) {
      return GuardrailDecision.Denied(
        reason = FallbackReason.SEVERE_MEMORY_PRESSURE,
        explanation = "Severe system RAM pressure detected (${resources.availableMemMb} MB free). Local model inference suppressed to prevent OOM termination."
      )
    }

    // 3. Rate-limiting / Cooldown Check
    val now = SystemClock.elapsedRealtime()
    val elapsedSinceLast = now - lastExecutionTime
    if (elapsedSinceLast < minIntervalMs) {
      val remainingMs = minIntervalMs - elapsedSinceLast
      return GuardrailDecision.Denied(
        reason = FallbackReason.RATE_LIMIT_COOLDOWN,
        explanation = "Cooldown active: $remainingMs ms remaining before next model inference allowed (minimum ${minIntervalMs}ms window)."
      )
    }

    return GuardrailDecision.Allowed
  }

  @Synchronized
  fun markExecutionCompleted() {
    lastExecutionTime = SystemClock.elapsedRealtime()
  }

  @Synchronized
  fun getLastExecutionAgeMs(): Long {
    val now = SystemClock.elapsedRealtime()
    return if (lastExecutionTime > 0) now - lastExecutionTime else -1L
  }
}
