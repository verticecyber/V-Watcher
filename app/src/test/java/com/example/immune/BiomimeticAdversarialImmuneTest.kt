package com.example.immune

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.model.CaseStatus
import com.example.model.ImmuneCellState
import com.example.sentinel.AndroidSentinel
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.UUID

/**
 * Section 23. Negative / Adversarial Test Layer.
 * Specifically intended to stress-test and attempt to falsify the immune multi-agent architecture:
 * - Stale telemetry rejection
 * - Low-confidence defense denial
 * - Zero-evidence defense refusal
 * - High-velocity agent storm detection and throttling
 * - Critical battery regulatory suppression
 * - RAM constraint regulatory suppression
 * - External process kill boundary enforcement
 * - Premature and time-only resolution prevention
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class BiomimeticAdversarialImmuneTest {

  private lateinit var context: Context
  private lateinit var realityBoundary: AndroidRealityBoundary
  private lateinit var sentinel: AndroidSentinel

  @Before
  fun setUp() {
    context = ApplicationProvider.getApplicationContext()
    realityBoundary = AndroidRealityBoundary(context)
    sentinel = AndroidSentinel(context)
  }

  @Test
  fun adversarial_staleTelemetryRejectedByCytotoxicCell() {
    val cytotoxic = CytotoxicEffectorCell(realityBoundary)
    val evidence = listOf(
      EvidenceItem(
        id = "ev_stale",
        providerId = "Sensor",
        signalType = "TEST_ALERT",
        description = "Telemetry from 60 seconds ago",
        confidence = 95
      )
    )

    // Adversarial injection: isFresh = false (e.g. telemetry delayed or cached)
    val result = cytotoxic.executeAuthorizedAction(
      incidentId = "INC-STALE-1",
      actionName = "ISOLATE_INTERNAL_SUBSYSTEM",
      evidence = evidence,
      isFresh = false,
      confidenceScore = 95,
      parameters = mapOf("subsystem" to "NETWORK_WORKER")
    )

    assertEquals(ActionExecutionStatus.DENIED, result.status)
    assertTrue(result.platformReason.contains("stale", ignoreCase = true))
  }

  @Test
  fun adversarial_lowConfidenceDefenseDeniedByCytotoxicCell() {
    val cytotoxic = CytotoxicEffectorCell(realityBoundary)
    val evidence = listOf(
      EvidenceItem(
        id = "ev_low_conf",
        providerId = "Sensor",
        signalType = "SUSPICIOUS_BEHAVIOR",
        description = "Unclear signal",
        confidence = 55
      )
    )

    // Adversarial injection: confidence = 55% (< 80% gate)
    val result = cytotoxic.executeAuthorizedAction(
      incidentId = "INC-LOWCONF-1",
      actionName = "ISOLATE_INTERNAL_SUBSYSTEM",
      evidence = evidence,
      isFresh = true,
      confidenceScore = 55,
      parameters = mapOf("subsystem" to "WORKER")
    )

    assertEquals(ActionExecutionStatus.DENIED, result.status)
    assertTrue(result.platformReason.contains("below 80%", ignoreCase = true))
  }

  @Test
  fun adversarial_emptyEvidenceRefusedByCytotoxicCell() {
    val cytotoxic = CytotoxicEffectorCell(realityBoundary)

    // Adversarial injection: empty evidence list
    val result = cytotoxic.executeAuthorizedAction(
      incidentId = "INC-NOEV-1",
      actionName = "ISOLATE_INTERNAL_SUBSYSTEM",
      evidence = emptyList(),
      isFresh = true,
      confidenceScore = 99,
      parameters = mapOf("subsystem" to "WORKER")
    )

    assertEquals(ActionExecutionStatus.DENIED, result.status)
    assertTrue(result.platformReason.contains("Zero evidence", ignoreCase = true))
  }

  @Test
  fun adversarial_agentStormDetectedAndVetoedByRegulatoryCell() = runBlocking {
    val bus = ImmuneBus()
    val reg = RegulatoryTCell()

    // Adversarial burst: rapidly dispatch 35 messages to simulate agent storm
    val observation = sentinel.observeNow()
    for (i in 1..35) {
      val msg = ImmuneMessage(
        messageId = "msg_storm_$i",
        correlationId = "corr_storm",
        incidentId = "INC-STORM",
        observationId = observation.observationId,
        emitterRole = AgentRole.SENTINEL,
        emitterId = "sentinel_storm",
        targetRole = AgentRole.T_HELPER,
        freshnessMs = 5,
        isStale = false,
        evidenceRefs = emptyList(),
        requestedAction = null,
        senderState = AgentLifecycleState.ACTIVE,
        payload = ImmunePayload.PatternDetected(emptyList(), "STORM_PING", "Low", "ping $i")
      )
      bus.dispatch(msg)
    }

    val stats = bus.stats.value
    assertTrue("Storm should be detected on high dispatch frequency", stats.activeStormDetected || stats.currentDispatchesPerSecond >= 30.0)

    // Regulatory cell evaluation under storm conditions
    val check = reg.evaluateEscalationRequest(
      incidentId = "INC-STORM",
      batteryPercent = 70,
      isDischarging = true,
      availableMemMb = 800,
      isBusStorming = true,
      activeIncidentsCount = 5
    )

    assertFalse("Regulatory cell MUST veto escalation during agent storm", check.isPermitted)
    assertTrue(check.forceDeterministic)
    assertTrue(reg.totalVetoesIssued >= 1)
  }

  @Test
  fun adversarial_criticalDischargingBatteryTriggersRegulatoryVeto() {
    val reg = RegulatoryTCell()

    // Adversarial condition: Battery is 8% and discharging
    val check = reg.evaluateEscalationRequest(
      incidentId = "INC-BATT-LOW",
      batteryPercent = 8,
      isDischarging = true,
      availableMemMb = 1200,
      isBusStorming = false,
      activeIncidentsCount = 1
    )

    assertFalse("Regulatory cell MUST veto heavy escalation on critical battery", check.isPermitted)
    assertTrue(check.forceDeterministic)
    assertTrue(check.reason.contains("battery critical", ignoreCase = true))
    assertTrue(reg.isDeterministicModeForced)
  }

  @Test
  fun adversarial_constrainedMemoryTriggersRegulatoryVetoAndMacrophageScavenge() {
    val reg = RegulatoryTCell()
    val macrophage = MacrophageCell(realityBoundary)

    // Adversarial condition: RAM available is only 90MB (< 200MB free)
    val check = reg.evaluateEscalationRequest(
      incidentId = "INC-RAM-LOW",
      batteryPercent = 60,
      isDischarging = false,
      availableMemMb = 90,
      isBusStorming = false,
      activeIncidentsCount = 1
    )

    assertFalse("Regulatory cell MUST suppress effector expansion under memory constraint", check.isPermitted)
    assertTrue(check.forceDeterministic)

    // Macrophage triggered to scavenge
    val scavengeResult = macrophage.scavengeAndRecycle("INC-RAM-LOW")
    assertEquals(ActionExecutionStatus.EXECUTED, scavengeResult.status)
    assertEquals("RECLAIM_INTERNAL_CACHE", scavengeResult.targetAction)
  }

  @Test
  fun adversarial_externalAppKillRefusedAndRedirectedToSettings() {
    // Reality Boundary strictly forbids pretending to kill 3rd party apps
    val result = realityBoundary.executeRealAction(
      actionName = "KILL_EXTERNAL_PROCESS",
      incidentId = "INC-EXT-1",
      parameters = mapOf("package" to "com.evil.malware")
    )

    // Must be UNAVAILABLE due to sandbox; must never pretend success
    assertEquals(ActionExecutionStatus.UNAVAILABLE, result.status)
    assertEquals("KILL_EXTERNAL_PROCESS", result.targetAction)
    assertTrue("Must mention Android platform sandbox", result.platformReason.contains("sandbox", ignoreCase = true))
    assertEquals("NAVIGATE_APP_SETTINGS", result.evidence["recommendedAlternative"])
  }

  @Test
  fun adversarial_prematureResolutionRefusedWhileSignalsRemain() = runBlocking {
    val resolution = ResolutionCell()
    val baseObs = sentinel.observeNow()

    val persistentAnomalies = listOf(
      EvidenceItem(
        id = "ev_persist",
        providerId = "BatteryProvider",
        signalType = "THERMAL_ELEVATION",
        description = "Battery still 47C",
        confidence = 92
      )
    )

    val verification = resolution.verifyBaselineReturn(
      incidentId = "INC-RESOLVE-PREMATURE",
      currentStatus = CaseStatus.CONTAINED,
      observation = baseObs,
      activeEvidence = persistentAnomalies,
      isCleanupVerified = true
    )

    assertFalse("Incident CANNOT be resolved while active anomaly evidence persists", verification.canResolve)
    assertEquals(CaseStatus.CONTAINED, verification.newStatus)
    assertTrue(verification.verificationEvidence.contains("Active anomaly signals still present"))
  }

  @Test
  fun adversarial_timeOnlyResolutionRefusedIfResourcesNotNormalized() = runBlocking {
    val resolution = ResolutionCell()
    val baseObs = sentinel.observeNow()

    // Host telemetry is NOT normalized: RAM is critical and battery temp is 44°C
    val unnormalizedObs = baseObs.copy(
      battery = baseObs.battery.copy(
        value = baseObs.battery.value.copy(temperatureCelsius = 44.0f)
      ),
      resources = baseObs.resources.copy(
        value = baseObs.resources.value.copy(
          isLowMemory = true,
          availableMemBytes = 110L * 1024 * 1024
        )
      )
    )

    // Evidence array is empty (time passed, signals cleared), but resources are unnormalized!
    val verification = resolution.verifyBaselineReturn(
      incidentId = "INC-TIME-ONLY",
      currentStatus = CaseStatus.CONTAINED,
      observation = unnormalizedObs,
      activeEvidence = emptyList(),
      isCleanupVerified = true
    )

    assertFalse("Resolution MUST be refused if host resources have not normalized to baseline", verification.canResolve)
    assertEquals(CaseStatus.CONTAINED, verification.newStatus)
    assertTrue(verification.verificationEvidence.contains("Host resources not yet normalized"))
  }
}
