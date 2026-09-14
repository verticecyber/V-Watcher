package com.example.honesty

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.immune.ActionExecutionStatus
import com.example.immune.AndroidRealityBoundary
import com.example.reasoning.ModelReadiness
import com.example.reasoning.OnDeviceReasoningEngine
import com.example.reasoning.AndroidOnDeviceReasoningEngine
import com.example.telemetry.AppUsageProvider
import com.example.telemetry.DeviceResourceProvider
import com.example.telemetry.TelemetryAvailability
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

/**
 * PH-02 honesty tests (A-02): G12 intent launch, G14 isReal split, G17 backend mirror.
 * Real collaborators under Robolectric. READY-branch behavior is device-only (G18/G19).
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class HonestyTest {

  private val context: Context
    get() = ApplicationProvider.getApplicationContext<Application>()

  @Test
  fun navigateAction_launchesSettingsIntent() {
    val boundary = AndroidRealityBoundary(context)
    val result = boundary.executeRealAction(
      "NAVIGATE_APP_SETTINGS", "incident_honesty",
      mapOf("package" to context.packageName)
    )
    assertEquals(ActionExecutionStatus.EXECUTED, result.status)
    assertEquals("true", result.evidence["launched"])
    val sent = shadowOf(context as Application).nextStartedActivity
    assertNotNull("settings intent must actually be sent", sent)
    assertTrue(sent.action!!.contains("APPLICATION_DETAILS_SETTINGS"))
  }

  @Test
  fun unknownAction_isDenied() {
    val boundary = AndroidRealityBoundary(context)
    val result = boundary.executeRealAction("DO_IMPOSSIBLE_THING", "incident_honesty", emptyMap())
    assertEquals(ActionExecutionStatus.DENIED, result.status)
  }

  @Test
  fun usageDenied_carriesNoHardwareTruth() {
    val provider = AppUsageProvider(context)
    val result = provider.collect()
    if (result.availability == TelemetryAvailability.PERMISSION_REQUIRED) {
      assertFalse("denied path must not claim hardware truth", result.isReal)
    }
  }

  @Test
  fun resourceAvailability_matchesHardwareBacking() {
    val provider = DeviceResourceProvider(context)
    val result = provider.collect()
    if (result.availability == TelemetryAvailability.AVAILABLE) {
      assertTrue("AVAILABLE resource telemetry is hardware-backed", result.isReal)
    } else {
      assertFalse("degraded resource telemetry must not claim hardware truth", result.isReal)
    }
  }

  @Test
  fun engineMirror_hasNoStaleSelection(): Unit = runBlocking {
    val engine: OnDeviceReasoningEngine = AndroidOnDeviceReasoningEngine(context)
    val status = engine.checkAvailability()
    val state = engine.engineState.value
    if (status == ModelReadiness.READY) {
      assertEquals(state.selectedBackend, state.actualBackendUsed)
    } else {
      assertEquals("DETERMINISTIC", state.selectedBackend)
      assertEquals("DETERMINISTIC", state.actualBackendUsed)
    }
  }
}
