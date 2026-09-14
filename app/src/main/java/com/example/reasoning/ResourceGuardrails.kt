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
 * Higher-layer immune signals fed by the owning engine (see OnDeviceReasoningEngine.updateGuardrailContext).
 * All-primitive by design: no dependency edge toward immune/orchestrator packages.
 * Neutral default preserves legacy battery/memory/cooldown behavior exactly.
 */
data class GuardrailContext(
  val homeostasisStressed: Boolean = false,
  val containmentActive: Boolean = false,
  val regulatoryForceDeterministic: Boolean = false,
  val regulatoryCooldownMs: Long = 0L,
  val inferenceThrottled: Boolean = false
)

/**
 * Hardware & System Resource Guardrails.
 * Strictly adheres to Section 11:
 * - Suppresses inference when battery is critical (<15%) and discharging
 * - Prevents rapid repeated activations (rate-limiting / cooldown)
 * - Respects severe memory pressure
 * - Enforces concurrency safety (mutex lock against concurrent local LLM execution)
 * - Exposes explicit denial reason for deterministic fallback
 * PH-03: additionally honors GuardrailContext (homeostasis/regulatory/boundary signals),
 * deny-only. Production feed lands via updateGuardrailContext; see D-HOMEO and D-FLAGS.
 */
class ResourceGuardrails(
  private val minIntervalMs: Long = 3000L,
  private val criticalBatteryPercent: Int = 15
) {
  private var lastExecutionTime: Long = 0L
  val concurrencyLock = Mutex()

  @Volatile
  var context: GuardrailContext = GuardrailContext()
    private set

  @Synchronized
  fun updateContext(context: GuardrailContext) {
    this.context = context
  }

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

    // 4. Immune-context checks (PH-03, deny-only; evaluated after hardware precision checks
    // so battery/memory reasons keep priority in explanations)
    val ctx = context
    if (ctx.regulatoryForceDeterministic) {
      return GuardrailDecision.Denied(
        reason = FallbackReason.RESOURCE_GUARD_DENIED,
        explanation = "Regulatory veto forces deterministic mode. Local model inference suppressed by immune regulation."
      )
    }
    if (ctx.regulatoryCooldownMs > 0) {
      return GuardrailDecision.Denied(
        reason = FallbackReason.RATE_LIMIT_COOLDOWN,
        explanation = "Regulatory escalation cooldown active (${ctx.regulatoryCooldownMs} ms imposed). Local model inference suppressed."
      )
    }
    if (ctx.inferenceThrottled) {
      return GuardrailDecision.Denied(
        reason = FallbackReason.RESOURCE_GUARD_DENIED,
        explanation = "Internal inference throttled by acute-stress response. Local model inference suppressed until reset."
      )
    }
    if (ctx.homeostasisStressed) {
      return GuardrailDecision.Denied(
        reason = FallbackReason.RESOURCE_GUARD_DENIED,
        explanation = "Homeostatic macro-state is STRESSED. Local model inference suppressed to protect host resources."
      )
    }
    if (ctx.containmentActive) {
      return GuardrailDecision.Denied(
        reason = FallbackReason.RESOURCE_GUARD_DENIED,
        explanation = "Active containment in progress. Local model inference suppressed to avoid destabilizing the response."
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
