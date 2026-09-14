package com.example.reasoning

enum class ModelReadiness(val displayName: String) {
  READY("Ready"),
  PREPARING("Preparing"),
  UNAVAILABLE("Unavailable"),
  ERROR("Error")
}

enum class AssessmentClassification(val displayName: String) {
  NORMAL("Normal"),
  BENIGN_ANOMALY("Benign Anomaly"),
  SUSPICIOUS("Suspicious"),
  HIGH_CONFIDENCE_ANOMALY("High Confidence Anomaly"),
  UNKNOWN("Unknown")
}

enum class RecommendedAction(val displayName: String) {
  OBSERVE("Observe"),
  INVESTIGATE("Investigate"),
  ISOLATE("Isolate"),
  BLOCK("Block"),
  NO_ACTION("No Action"),
  THROTTLE_INTERNAL_INFERENCE("Throttle Internal Inference")
}

data class Assessment(
  val classification: AssessmentClassification,
  val confidence: Int, // 0..100
  val severity: String, // "Mild", "Moderate", "Elevated"
  val rationale: String,
  val evidence: List<String>,
  val recommendedAction: RecommendedAction,
  val additionalObservation: String? = null
)

data class ObservationBundle(
  val deviceStateSummary: String,
  val applicationContext: String,
  val networkContext: String,
  val recentEvents: List<String>,
  val baselineSummary: String,
  val candidateAnomaly: String
)

data class ReasoningEngineState(
  val status: ModelReadiness = ModelReadiness.UNAVAILABLE,
  val backendName: String = "AICore / ML Kit GenAI Prompt API",
  val isWarm: Boolean = false,
  val lastInferenceTimeAgo: String = "Never",
  val latencyMs: Long = 0L,
  val reasoningCallsCount: Int = 0,
  val callsAvoidedByDeterministicLayer: Int = 0,
  val statusMessage: String = "Unavailable on this device",
  val geminiNanoStatus: ModelReadiness = ModelReadiness.UNAVAILABLE,
  val geminiNanoNotes: String = "Detecting AICore system availability...",
  val gemmaStatus: ModelReadiness = ModelReadiness.UNAVAILABLE,
  val gemmaNotes: String = "Local Gemma model weights not found in storage",
  val deterministicStatus: ModelReadiness = ModelReadiness.READY,
  val deterministicCallsCount: Int = 0,
  val selectedBackend: String = "GEMINI_NANO",
  val actualBackendUsed: String = "DETERMINISTIC",
  val lastFallbackReason: String = "None",
  val resourceAdmissionDecision: String = "Allowed (Standard baseline)",
  val latestDegradation: String? = null
)

sealed class ReasoningResult {
  data class Success(val assessment: Assessment, val latencyMs: Long) : ReasoningResult()
  data class Skipped(val reason: String, val fallbackAssessment: Assessment) : ReasoningResult()
  data class Unavailable(val message: String, val fallbackAssessment: Assessment) : ReasoningResult()
  data class Failed(val error: Throwable, val fallbackAssessment: Assessment) : ReasoningResult()
}

class ModelOutputValidationException(
  message: String,
  val rawOutput: String? = null,
  cause: Throwable? = null
) : IllegalArgumentException(message, cause)

class ModelLoadException(
  message: String,
  cause: Throwable? = null
) : IllegalStateException(message, cause)

