package com.example.contract

import android.app.ActivityManager
import android.app.AppOpsManager
import android.app.Application
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.os.Process
import androidx.test.core.app.ApplicationProvider
import com.example.communication.DefaultCommunicationChannel
import com.example.immune.AgentRole
import com.example.immune.BiomimeticImmuneSystem
import com.example.memory.LocalImmuneMemoryRepository
import com.example.model.CaseStatus
import com.example.reasoning.DeterministicBackend
import com.example.reasoning.GeminiNanoBackend
import com.example.reasoning.GemmaBackend
import com.example.reasoning.ReasoningRouter
import com.example.reasoning.ResourceGuardrails
import com.example.sentinel.AndroidSentinel
import com.example.sentinel.CanonicalObservation
import com.example.telemetry.TelemetryAvailability
import kotlinx.coroutines.runBlocking
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
 * ANDROID CONTRACT / ROBOLECTRIC IN-PROCESS END-TO-END SUITE
 *
 * Executes the entire production pipeline:
 * Android Shadow System State -> Provider -> Sentinel -> CanonicalObservation -> BiomimeticImmuneSystem -> ImmuneBus -> State
 *
 * PROVENANCE: Evaluated inside Robolectric JVM using real compiled production classes.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class InProcessEndToEndContractTest {

  private lateinit var application: Application
  private lateinit var context: Context
  private lateinit var sentinel: AndroidSentinel
  private lateinit var immuneSystem: BiomimeticImmuneSystem
  private lateinit var memoryRepo: LocalImmuneMemoryRepository

  @Before
  fun setUp() {
    application = ApplicationProvider.getApplicationContext()
    context = application
    val nano = GeminiNanoBackend(context)
    val gemma = GemmaBackend(context)
    val deterministic = DeterministicBackend()
    val guardrails = ResourceGuardrails()
    val router = ReasoningRouter(nano, gemma, deterministic, guardrails)
    val channel = DefaultCommunicationChannel(router)
    memoryRepo = LocalImmuneMemoryRepository(context)
    sentinel = AndroidSentinel(context)
    immuneSystem = BiomimeticImmuneSystem(context, channel, memoryRepo)
  }

  // =========================================================================
  // CHAIN A: RESOURCE DEGRADATION CAUSAL PIPELINE
  // Shadow Memory -> DeviceResourceProvider -> Sentinel -> Observation -> Immune Evaluation
  // =========================================================================

  @Test
  fun chainA_resourceDegradation_flowsThroughSentinelToMacrophageScavenge(): Unit = runBlocking {
    val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    val shadowAm: ShadowActivityManager = shadowOf(am)

    // Induce low-memory state in Android shadow
    val memInfo = ActivityManager.MemoryInfo().apply {
      availMem = 110L * 1024L * 1024L // 110MB available (< 250MB threshold)
      totalMem = 2048L * 1024L * 1024L
      lowMemory = true
      threshold = 200L * 1024L * 1024L
    }
    shadowAm.setMemoryInfo(memInfo)

    // Collect via production Sentinel
    val obs: CanonicalObservation = sentinel.observeNow()
    assertTrue(obs.resources.value.isLowMemory)
    assertEquals(110L, obs.resources.value.availableMemMb)

    // Process through BiomimeticImmuneSystem
    val snapshot = immuneSystem.processObservation(obs)

    // Verify pipeline reactions:
    // 1. Evidence was detected for resource depletion
    assertTrue("Should detect resource depletion evidence", snapshot.recentEvidence.any { it.signalType == "RESOURCE_DEPLETION" })
    // 2. Macrophage executed scavenging
    assertNotNull(snapshot.lastActionResult)
    assertEquals("RECLAIM_INTERNAL_CACHE", snapshot.lastActionResult?.targetAction)
    // 3. Homeostasis macroState reflects strain
    assertNotNull(snapshot.homeostasis)
    assertTrue(
      "Homeostasis should be stressed or watch",
      snapshot.homeostasis.macroState != com.example.immune.HomeostaticMacroState.UNKNOWN
    )
  }

  // =========================================================================
  // CHAIN B: APPOPS USAGE PERMISSION STATE FLOW
  // Shadow AppOps -> AppUsageProvider -> Sentinel -> Observation -> Truth Guard
  // =========================================================================

  @Test
  fun chainB_appOpsTransition_flowsThroughSentinelWithHonestyGuard(): Unit = runBlocking {
    val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
    val shadowAppOps: ShadowAppOpsManager = shadowOf(appOps)

    // State 1: AppOps Denied
    shadowAppOps.setMode(
      AppOpsManager.OPSTR_GET_USAGE_STATS,
      Process.myUid(),
      context.packageName,
      AppOpsManager.MODE_IGNORED
    )

    val obsDenied = sentinel.observeNow()
    assertEquals(TelemetryAvailability.PERMISSION_REQUIRED, obsDenied.usage.availability)
    assertFalse(obsDenied.usage.isReal)
    assertFalse(obsDenied.usage.value.isAccessGranted)

    // State 2: AppOps Granted
    shadowAppOps.setMode(
      AppOpsManager.OPSTR_GET_USAGE_STATS,
      Process.myUid(),
      context.packageName,
      AppOpsManager.MODE_ALLOWED
    )

    val obsGranted = sentinel.observeNow()
    assertTrue(obsGranted.usage.value.isAccessGranted)
  }

  // =========================================================================
  // CHAIN C: PACKAGE INVENTORY NORMALITY VS THREAT
  // Proves CHANGE != THREAT: Normal benign app addition does NOT trigger escalation
  // =========================================================================

  @Test
  fun chainC_normalPackageAddition_doesNotTriggerImmuneEscalation(): Unit = runBlocking {
    val pm = context.packageManager
    val shadowPm: ShadowPackageManager = shadowOf(pm)

    // Add a benign standard utility app with standard normal permissions
    val benignApp = PackageInfo().apply {
      packageName = "com.utility.calculator"
      versionName = "1.0"
      applicationInfo = ApplicationInfo().apply {
        packageName = "com.utility.calculator"
        flags = 0
      }
      requestedPermissions = arrayOf("android.permission.VIBRATE")
    }
    shadowPm.addPackage(benignApp)

    val obs = sentinel.observeNow()
    assertTrue(obs.inventory.value.apps.any { it.packageName == "com.utility.calculator" })

    // Process through immune system
    val snapshot = immuneSystem.processObservation(obs)

    // Crucial Principle: Change != Threat. Standard app does not trigger anomaly evidence
    val appEvidence = snapshot.recentEvidence.filter { it.signalType == "ANOMALOUS_APPLICATION_BEHAVIOR" }
    assertTrue("Benign app install must not trigger anomaly candidate evidence", appEvidence.isEmpty())
    assertEquals(0, snapshot.activeIncidents.size)
  }
}
