package com.example.consumption

import android.content.Context
import android.os.Looper
import androidx.test.core.app.ApplicationProvider
import com.example.communication.FallbackReason
import com.example.reasoning.AndroidOnDeviceReasoningEngine
import com.example.reasoning.GuardrailContext
import com.example.reasoning.GuardrailDecision
import com.example.reasoning.ResourceGuardrails
import com.example.sentinel.AndroidSentinel
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import java.util.concurrent.TimeUnit
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

/**
 * PH-03 consumption tests (A-03): homeostasis/regulatory/boundary signals gate escalation.
 * Deny-only. Production feed lands in PH-04; here the guardrail decision function is pinned.
 * READY-backend router consultation stays device-only (both neural backends UNAVAILABLE under L2).
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ConsumptionTest {

  private val context: Context
    get() = ApplicationProvider.getApplicationContext()

  /**
   * Advances the Robolectric scheduler clock past guardrail cooldown windows.
   * Shadow SystemClock.elapsedRealtime starts at 0, so fresh guardrails would
   * otherwise deny everything with RATE_LIMIT_COOLDOWN before context rules run.
   */
  private fun pastCooldown() {
    shadowOf(Looper.getMainLooper()).idleFor(3500L, TimeUnit.MILLISECONDS)
  }

  private suspend fun healthyObs() = runBlocking {
    val base = AndroidSentinel(context).observeNow()
    base.copy(
      battery = base.battery.copy(
        value = base.battery.value.copy(levelPercent = 82, isCharging = true)
      ),
      resources = base.resources.copy(
        value = base.resources.value.copy(
          isLowMemory = false,
          availableMemBytes = 1500L * 1024 * 1024,
          totalMemBytes = 2000L * 1024 * 1024
        )
      )
    )
  }

  private fun deniedWith(reason: FallbackReason, d: GuardrailDecision): Boolean =
    d is GuardrailDecision.Denied && d.reason == reason

  @Test
  fun neutralContext_preservesLegacyBehavior() = runBlocking {
    val obs = healthyObs()
    val g = ResourceGuardrails()
    pastCooldown()
    assertTrue(g.evaluate(obs) is GuardrailDecision.Allowed)
  }

  @Test
  fun stressedHomeostasis_deniesDeterministicFallback() = runBlocking {
    val obs = healthyObs()
    val g = ResourceGuardrails()
    pastCooldown()
    g.updateContext(GuardrailContext(homeostasisStressed = true))
    assertTrue(deniedWith(FallbackReason.RESOURCE_GUARD_DENIED, g.evaluate(obs)))
  }

  @Test
  fun containmentActive_denies() = runBlocking {
    val obs = healthyObs()
    val g = ResourceGuardrails()
    pastCooldown()
    g.updateContext(GuardrailContext(containmentActive = true))
    assertTrue(deniedWith(FallbackReason.RESOURCE_GUARD_DENIED, g.evaluate(obs)))
  }

  @Test
  fun regulatoryForceDeterministic_denies() = runBlocking {
    val obs = healthyObs()
    val g = ResourceGuardrails()
    pastCooldown()
    g.updateContext(GuardrailContext(regulatoryForceDeterministic = true))
    assertTrue(deniedWith(FallbackReason.RESOURCE_GUARD_DENIED, g.evaluate(obs)))
  }

  @Test
  fun regulatoryCooldown_deniesAsRateLimit() = runBlocking {
    val obs = healthyObs()
    val g = ResourceGuardrails()
    pastCooldown()
    g.updateContext(GuardrailContext(regulatoryCooldownMs = 20000L))
    assertTrue(deniedWith(FallbackReason.RATE_LIMIT_COOLDOWN, g.evaluate(obs)))
  }

  @Test
  fun throttleFlag_deniesViaWiredReader() = runBlocking {
    val obs = healthyObs()
    val g = ResourceGuardrails()
    pastCooldown()
    g.updateContext(GuardrailContext(inferenceThrottled = true))
    val d = g.evaluate(obs)
    assertTrue(deniedWith(FallbackReason.RESOURCE_GUARD_DENIED, d))
    assertTrue((d as GuardrailDecision.Denied).explanation.contains("throttled", ignoreCase = true))
  }

  @Test
  fun batteryPrecision_beatsContextReason() = runBlocking {
    val base = healthyObs()
    val obs = base.copy(
      battery = base.battery.copy(value = base.battery.value.copy(levelPercent = 9, isCharging = false))
    )
    val g = ResourceGuardrails()
    pastCooldown()
    g.updateContext(GuardrailContext(homeostasisStressed = true, containmentActive = true))
    assertTrue(deniedWith(FallbackReason.BATTERY_CRITICAL_DISCHARGING, g.evaluate(obs)))
  }

  @Test
  fun engineFeed_reachesGuardrailDecision() = runBlocking {
    val engine = AndroidOnDeviceReasoningEngine(context)
    pastCooldown()
    val obs = healthyObs()
    engine.updateGuardrailContext(GuardrailContext(homeostasisStressed = true))
    val d = engine.guardrails.evaluate(obs)
    assertTrue(deniedWith(FallbackReason.RESOURCE_GUARD_DENIED, d))
    engine.release()
    val fresh = AndroidOnDeviceReasoningEngine(context)
    pastCooldown()
    assertTrue(fresh.guardrails.evaluate(obs) is GuardrailDecision.Allowed)
  }

}
