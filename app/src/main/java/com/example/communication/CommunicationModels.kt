package com.example.communication

import com.example.reasoning.Assessment
import com.example.sentinel.CanonicalObservation

enum class ExecutionStatus(val displayName: String) {
  SUCCESS("Success"),
  DEGRADED("Degraded"),
  FALLBACK("Fallback"),
  RESOURCE_DENIED("Resource Denied"),
  FAILED("Failed"),
  TIMEOUT("Timeout")
}

enum class FallbackReason(val code: String, val description: String) {
  GEMINI_NANO_UNAVAILABLE("GEMINI_NANO_UNAVAILABLE", "AICore / Gemini Nano not available on host device"),
  GEMMA_UNAVAILABLE("GEMMA_UNAVAILABLE", "Local Gemma model weights or runtime not found"),
  MODEL_LOAD_ERROR("MODEL_LOAD_ERROR", "Failed to load model weights or initialize inference session"),
  RESOURCE_GUARD_DENIED("RESOURCE_GUARD_DENIED", "Resource guardrail suppressed inference to protect device"),
  BATTERY_CRITICAL_DISCHARGING("BATTERY_CRITICAL_DISCHARGING", "Battery < 15% and discharging; local LLM inference suppressed"),
  SEVERE_MEMORY_PRESSURE("SEVERE_MEMORY_PRESSURE", "Severe RAM pressure detected; avoiding out-of-memory crash"),
  RATE_LIMIT_COOLDOWN("RATE_LIMIT_COOLDOWN", "Thermal cooldown active; minimum inference interval enforced"),
  CONCURRENCY_LOCKED("CONCURRENCY_LOCKED", "Another local model inference is already executing"),
  MODEL_INITIALIZATION_FAILED("MODEL_INITIALIZATION_FAILED", "Model runtime initialization returned an error"),
  MODEL_TIMEOUT("MODEL_TIMEOUT", "Model inference exceeded strict execution deadline"),
  MODEL_OUTPUT_INVALID("MODEL_OUTPUT_INVALID", "Model output was malformed or failed structured parsing validation"),
  MODEL_EXECUTION_ERROR("MODEL_EXECUTION_ERROR", "Model inference threw an unhandled runtime exception"),
  NONE("NONE", "No fallback occurred")
}

data class BackendModelProvenance(
  val backendType: String, // "GEMINI_NANO", "GEMMA", "DETERMINISTIC"
  val modelIdentity: String,
  val isRealOnDeviceModel: Boolean,
  val runtimeHostInfo: String
)

data class ReasoningRequest(
  val requestId: String,
  val correlationId: String,
  val timestamp: Long = System.currentTimeMillis(),
  val observation: CanonicalObservation,
  val candidateAnomaly: String? = null,
  val promptInstruction: String? = null,
  val preferredBackend: String? = null,
  val timeoutMs: Long = 5000L
)

data class ReasoningResponse(
  val requestId: String,
  val correlationId: String,
  val timestamp: Long = System.currentTimeMillis(),
  val selectedBackend: String,
  val actualBackendUsed: String,
  val executionMode: String, // "model" or "deterministic"
  val executionStatus: ExecutionStatus,
  val fallbackReason: FallbackReason? = null,
  val assessment: Assessment,
  val latencyMs: Long,
  val provenance: BackendModelProvenance,
  val errorMessage: String? = null
)

data class CommunicationChannelState(
  val isReady: Boolean = true,
  val totalTelemetryDispatched: Long = 0L,
  val totalReasoningRequests: Long = 0L,
  val lastTelemetryTimestamp: Long? = null,
  val lastTelemetryObservationId: String? = null,
  val lastReasoningRequest: ReasoningRequest? = null,
  val lastReasoningResponse: ReasoningResponse? = null,
  val lastError: String? = null
)
