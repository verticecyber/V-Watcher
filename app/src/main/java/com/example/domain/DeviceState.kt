package com.example.domain

import com.example.model.BarrierState
import com.example.model.DeviceCondition
import com.example.reasoning.Assessment
import com.example.reasoning.ReasoningEngineState
import com.example.telemetry.*

data class AnomalyCandidate(
  val id: String,
  val timestamp: Long = System.currentTimeMillis(),
  val source: String,
  val title: String,
  val description: String,
  val severity: String = "Mild", // "Mild", "Moderate", "Elevated"
  val confidence: Int = 80,
  val affectedPackage: String? = null,
  val deterministicRuleTriggered: String,
  val isSimulation: Boolean = false
)

data class DeviceState(
  val timestamp: Long = System.currentTimeMillis(),
  val healthScore: Int = 96,
  val condition: DeviceCondition = DeviceCondition.HEALTHY,
  val doctorClinicalNote: String = "Your device is healthy today. Real hardware telemetry and application boundaries match established physiological baselines.",
  val supportingSummary: String = "Live Android telemetry active. Zero uncontained anomalies.",
  val barrierState: BarrierState = BarrierState.WATCHING,

  // Real Observed Telemetry
  val battery: TelemetryResult<BatteryTelemetry>,
  val memory: TelemetryResult<ResourceTelemetry>,
  val network: TelemetryResult<NetworkTelemetry>,
  val applicationState: TelemetryResult<AppInventoryTelemetry>,
  val usageState: TelemetryResult<AppUsageTelemetry>,
  val systemState: TelemetryResult<SystemStateTelemetry>,

  // Reasoning State
  val reasoningState: ReasoningEngineState,

  // Sentinel & Communication Substrate States
  val sentinelState: com.example.sentinel.SentinelState? = null,
  val communicationState: com.example.communication.CommunicationChannelState? = null,

  // Biomimetic Immune Layer States
  val homeostasisEvaluation: com.example.immune.HomeostasisEvaluation? = null,
  val immuneBusStats: com.example.immune.ImmuneBusStats? = null,
  val lastActionResult: com.example.immune.ActionResult? = null,

  // Active Anomaly Candidates & Assessments
  val activeAnomalyCandidates: List<AnomalyCandidate> = emptyList(),
  val latestAssessment: Assessment? = null,

  // Mode Flag
  val isSimulationActive: Boolean = false
)
