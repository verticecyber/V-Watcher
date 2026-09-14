package com.example.immune

import com.example.communication.ReasoningRequest
import com.example.communication.ReasoningResponse
import com.example.sentinel.CanonicalObservation

enum class AgentRole(val displayName: String, val functionalRole: String) {
  SENTINEL("Sentinel", "Canonical observation boundary aggregating real hardware telemetry"),
  PRR("PRR Cell", "Pattern recognition receptor detecting danger patterns deterministically"),
  NEUTROPHIL("Neutrophil", "Rapid first responder executing bounded, short-lived containment"),
  MACROPHAGE("Macrophage", "Scavenger and recycler reclaiming resources and verifying cleanup"),
  DENDRITIC("Dendritic Cell", "Antigen presenter assembling evidence context for reasoning"),
  NK("NK Cell", "Compromise detector isolating corrupted internal subsystems"),
  T_HELPER("T-Helper Coordinator", "Immune coordinator integrating evidence and arbitrating escalation"),
  B_CELL("B-Cell", "Antibody binder neutralizing known recurring patterns from memory"),
  CYTOTOXIC("Cytotoxic Effector", "Effector executing authorized actions under strict Android reality"),
  REGULATORY("Regulatory T-Cell", "Homeostatic regulator preventing self-damage, storms, and runaway escalation"),
  RESOLUTION("Resolution Cell", "Verifier transitioning incidents back toward baseline equilibrium"),
  MEMORY("Memory Cell", "Evidence-backed memory store recording validated encounters")
}

enum class AgentLifecycleState {
  INITIALIZING,
  READY,
  OBSERVING,
  ACTIVE,
  COOLDOWN,
  DEGRADED,
  STOPPED,
  ERROR
}

enum class ActionExecutionStatus {
  EXECUTED,
  DENIED,
  UNAVAILABLE,
  FAILED,
  SKIPPED,
  SUPERSEDED
}

data class ActionResult(
  val actionId: String,
  val targetAction: String,
  val status: ActionExecutionStatus,
  val executionTimestamp: Long = System.currentTimeMillis(),
  val platformReason: String,
  val evidence: Map<String, String> = emptyMap()
)

data class EvidenceItem(
  val id: String,
  val providerId: String,
  val signalType: String,
  val description: String,
  val confidence: Int,
  val observedAt: Long = System.currentTimeMillis(),
  val isStale: Boolean = false,
  val rawMetricValue: String = ""
)

data class AntigenPresentation(
  val incidentId: String,
  val observationId: String,
  val timestamp: Long = System.currentTimeMillis(),
  val evidenceRefs: List<EvidenceItem>,
  val sourceProvenance: List<String>,
  val freshnessMs: Long,
  val patternSignals: List<String>,
  val cellsInvolved: List<AgentRole>,
  val missingInformation: List<String>,
  val resourceState: String,
  val candidateHypotheses: List<String>,
  val correlationId: String
)

sealed class ImmunePayload {
  data class TelemetryArrival(val observation: CanonicalObservation) : ImmunePayload()
  data class PatternDetected(
    val evidence: List<EvidenceItem>,
    val signalClass: String,
    val severity: String,
    val candidateAnomaly: String
  ) : ImmunePayload()
  data class RapidResponseRequested(
    val target: String,
    val reason: String,
    val requiresInternalThrottle: Boolean
  ) : ImmunePayload()
  data class AntigenPresented(val presentation: AntigenPresentation) : ImmunePayload()
  data class ReasoningDispatched(val request: ReasoningRequest) : ImmunePayload()
  data class ReasoningConcluded(val response: ReasoningResponse) : ImmunePayload()
  data class CompromiseSuspected(
    val componentName: String,
    val anomalyDescription: String,
    val isVWatcherOwned: Boolean
  ) : ImmunePayload()
  data class CoordinationDirective(
    val directive: String,
    val recruitedAgents: List<AgentRole>,
    val requiresReasoning: Boolean
  ) : ImmunePayload()
  data class AntibodyMatch(
    val patternCode: String,
    val isBenign: Boolean,
    val neutralizationSummary: String
  ) : ImmunePayload()
  data class EffectorActionRequest(
    val actionName: String,
    val incidentId: String,
    val parameters: Map<String, String>
  ) : ImmunePayload()
  data class EffectorActionCompleted(val result: ActionResult) : ImmunePayload()
  data class CleanupCompleted(
    val cleanupType: String,
    val verifiedBytesReclaimed: Long,
    val removedArtifactsCount: Int,
    val proofDescription: String
  ) : ImmunePayload()
  data class RegulatorySuppression(
    val targetRole: AgentRole,
    val reason: String,
    val cooldownDurationMs: Long,
    val forcedDeterministicMode: Boolean
  ) : ImmunePayload()
  data class ResolutionTransition(
    val incidentId: String,
    val fromState: String,
    val toState: String,
    val verificationEvidence: String
  ) : ImmunePayload()
}

data class ImmuneMessage(
  val messageId: String,
  val correlationId: String,
  val incidentId: String?,
  val observationId: String,
  val timestamp: Long = System.currentTimeMillis(),
  val emitterRole: AgentRole,
  val emitterId: String,
  val targetRole: AgentRole?,
  val freshnessMs: Long,
  val isStale: Boolean,
  val evidenceRefs: List<EvidenceItem>,
  val requestedAction: String?,
  val senderState: AgentLifecycleState,
  val payload: ImmunePayload
)
