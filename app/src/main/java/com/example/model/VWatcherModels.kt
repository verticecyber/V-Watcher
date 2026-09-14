package com.example.model

enum class DeviceCondition(val displayName: String) {
  HEALTHY("Healthy"),
  ATTENTION("Attention"),
  UNDER_REVIEW("Under Review"),
  CONTAINING("Containing"),
  RECOVERING("Recovering")
}

enum class BarrierState(val displayName: String, val clinicalDescription: String) {
  WATCHING("WATCHING", "Permeable boundary observing passive network flows"),
  DEFENDING("DEFENDING", "Tightening membrane around suspicious port activity"),
  CONTAINING("CONTAINING", "Active biological containment perimeter established"),
  STABLE("STABLE", "Membrane equilibrium restored to baseline")
}

enum class ImmuneCellState(val displayName: String) {
  MONITORING("MONITORING"),
  ACTIVE("ACTIVE"),
  ASSESSING("ASSESSING"),
  INVESTIGATING("INVESTIGATING"),
  CONTAINING("CONTAINING"),
  RECOVERING("RECOVERING"),
  IDLE("IDLE")
}

enum class CaseStatus(val displayName: String) {
  OBSERVING("Observing"),
  ASSESSING("Assessing"),
  ISOLATED("Isolated"),
  CONTAINED("Contained"),
  RECOVERING("Recovering"),
  RESOLVED("Resolved")
}

enum class AppHealthState(val displayName: String) {
  HEALTHY("Healthy"),
  UNDER_REVIEW("Under Review"),
  ATTENTION("Attention"),
  ISOLATED("Isolated")
}

enum class PermissionClinicalStatus(val displayName: String) {
  EXPECTED("Expected"),
  REVIEW_RECOMMENDED("Review recommended"),
  UNUSUAL("Unusual")
}

data class DeviceVitalSign(
  val id: String,
  val title: String,
  val status: String,
  val subtitle: String,
  val updatedAgo: String,
  val isNormal: Boolean = true,
  val trendLabel: String = "Stable"
)

data class ExamCategory(
  val id: String,
  val name: String,
  val status: String,
  val summary: String,
  val detailCountText: String,
  val isHealthy: Boolean = true,
  val notes: String = ""
)

data class AppRecord(
  val id: String,
  val name: String,
  val packageName: String,
  val healthState: AppHealthState,
  val permissionSummary: String,
  val behaviorState: String,
  val lastObserved: String,
  val iconKind: String = "default",
  val isolationReason: String = "",
  val networkActivityNote: String = "Normal periodic handshake"
)

data class PermissionReviewItem(
  val id: String,
  val name: String,
  val clinicalStatus: PermissionClinicalStatus,
  val rationale: String,
  val frequencyNote: String,
  val appsCount: Int
)

data class NetworkConnection(
  val id: String,
  val endpoint: String,
  val appName: String,
  val status: String, // "Trusted", "Unusual", "Isolated", "Active"
  val protocol: String = "TLS 1.3",
  val bandwidth: String = "12 KB/s",
  val timestamp: String = "Just now",
  val isDivertedToDecoy: Boolean = false
)

data class DecoyEnvironment(
  val id: String,
  val name: String,
  val status: String, // "Quiet", "Observed", "Active Containment"
  val interactionsCount: Int,
  val lastObservation: String,
  val behaviorCaptured: String = "Observed baseline interactions",
  val description: String = ""
)

data class ImmuneCell(
  val id: String,
  val name: String,
  val role: String,
  val state: ImmuneCellState,
  val activity: String,
  val confidence: Int,
  val eventsHandled: Int,
  val lastAction: String,
  val description: String
)

data class CaseTimelineEvent(
  val time: String,
  val title: String,
  val detail: String,
  val stage: String // "Observation", "Correlation", "Action", "Containment", "Outcome", "Memory"
)

data class IncidentCase(
  val id: String,
  val caseCode: String,
  val title: String,
  val date: String,
  val severity: String, // "Mild", "Moderate", "Elevated"
  val status: CaseStatus,
  val assessment: String,
  val actionTaken: String,
  val outcome: String,
  val confidencePercent: Int,
  val affectedApp: String?,
  val evidence: List<String>,
  val timeline: List<CaseTimelineEvent>
)

data class ImmuneMemoryPattern(
  val id: String,
  val patternCode: String,
  val name: String,
  val category: String,
  val observedCount: Int,
  val lastSeen: String,
  val typicalResponse: String,
  val confidenceScore: String,
  val description: String,
  val causalImpact: String
)

data class HealthHistoryPoint(
  val periodLabel: String,
  val healthScore: Int,
  val condition: String,
  val casesCount: Int = 0,
  val anomaliesCount: Int = 0,
  val summaryNote: String = "Normal baseline"
)

data class EfficiencyMetrics(
  val batteryImpact: String = "Very low (< 0.4%/day)",
  val memoryUsage: String = "84 MB",
  val cpuActivity: String = "Low (0.8% avg)",
  val localAnalysis: String = "Deterministic & on-demand local neural filter",
  val inferenceActivity: String = "Idle (escalated only upon anomaly)"
)

data class SimulatedNotification(
  val id: String,
  val timestamp: String,
  val title: String,
  val message: String,
  val isDoctorTone: Boolean = true
)

enum class ExamSequenceStep(val title: String, val subtitle: String, val progress: Float) {
  CHECKING_APPLICATIONS("CHECKING APPLICATIONS", "Observing process signatures and background behaviors", 0.16f),
  CHECKING_PERMISSIONS("CHECKING PERMISSIONS", "Comparing runtime privilege frequency with physiological baseline", 0.33f),
  CHECKING_NETWORK("CHECKING NETWORK", "Evaluating protective barrier integrity and connection telemetry", 0.50f),
  CHECKING_SYSTEM("CHECKING SYSTEM", "Examining system partitions and cryptographic integrity", 0.67f),
  CHECKING_BEHAVIOR("CHECKING BEHAVIOR", "Context cells correlating temporal heuristics", 0.84f),
  CHECKING_IMMUNE_MEMORY("CHECKING IMMUNE MEMORY", "Cross-referencing observed patterns with memory cells", 1.0f),
  EXAMINATION_COMPLETE("EXAMINATION COMPLETE", "Device diagnosis and physiological report finalized", 1.0f)
}
