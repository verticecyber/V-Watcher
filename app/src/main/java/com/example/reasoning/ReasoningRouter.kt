package com.example.reasoning

import com.example.communication.ExecutionStatus
import com.example.communication.FallbackReason
import com.example.communication.ReasoningRequest
import com.example.communication.ReasoningResponse
import kotlinx.coroutines.withTimeoutOrNull

data class RoutingSelection(
  val selectedBackend: ReasoningBackend,
  val isFallback: Boolean,
  val fallbackReason: FallbackReason?
)

/**
 * Technical Substrate Router.
 * Strictly adheres to Sections 9, 12, 13:
 * - Answers solely: "Which backend is actually available and safe to execute now?"
 * - Never attempts threat interpretation or containment policy.
 * - Prevents silent fallbacks; records explicit reason whenever fallback occurs.
 * - Distinguishes selectedBackend vs actualBackendUsed.
 */
class ReasoningRouter(
  val geminiNanoBackend: GeminiNanoBackend,
  val gemmaBackend: GemmaBackend,
  val deterministicBackend: DeterministicBackend,
  val guardrails: ResourceGuardrails
) {

  var lastFallbackReason: FallbackReason? = null
    private set
  var lastAdmissionDecision: String = "Normal"
    private set

  /**
   * Evaluates available backends and resource constraints to select the safe backend.
   */
  fun selectBackend(request: ReasoningRequest): RoutingSelection {
    val preferred = request.preferredBackend

    // Priority 1: Check if Gemini Nano is preferred or first in line
    if (preferred == null || preferred == geminiNanoBackend.backendType) {
      val nanoState = geminiNanoBackend.state.value
      if (nanoState == ModelReadiness.READY) {
        val guardDecision = guardrails.evaluate(request.observation)
        if (guardDecision is GuardrailDecision.Allowed) {
          lastAdmissionDecision = "Allowed for Gemini Nano"
          return RoutingSelection(geminiNanoBackend, isFallback = false, fallbackReason = null)
        } else if (guardDecision is GuardrailDecision.Denied) {
          lastAdmissionDecision = guardDecision.explanation
          lastFallbackReason = guardDecision.reason
          return RoutingSelection(
            selectedBackend = deterministicBackend,
            isFallback = true,
            fallbackReason = guardDecision.reason
          )
        }
      }
    }

    // Priority 2: Check Gemma
    if (preferred == null || preferred == gemmaBackend.backendType) {
      val gemmaState = gemmaBackend.state.value
      if (gemmaState == ModelReadiness.READY) {
        val guardDecision = guardrails.evaluate(request.observation)
        if (guardDecision is GuardrailDecision.Allowed) {
          lastAdmissionDecision = "Allowed for Gemma"
          return RoutingSelection(gemmaBackend, isFallback = false, fallbackReason = null)
        } else if (guardDecision is GuardrailDecision.Denied) {
          lastAdmissionDecision = guardDecision.explanation
          lastFallbackReason = guardDecision.reason
          return RoutingSelection(
            selectedBackend = deterministicBackend,
            isFallback = true,
            fallbackReason = guardDecision.reason
          )
        }
      }
    }

    // Fallback: Neither neural model is available
    val fallbackReason = when {
      geminiNanoBackend.state.value != ModelReadiness.READY && preferred == geminiNanoBackend.backendType ->
        FallbackReason.GEMINI_NANO_UNAVAILABLE
      gemmaBackend.state.value != ModelReadiness.READY && preferred == gemmaBackend.backendType ->
        FallbackReason.GEMMA_UNAVAILABLE
      geminiNanoBackend.state.value != ModelReadiness.READY ->
        FallbackReason.GEMINI_NANO_UNAVAILABLE
      else -> FallbackReason.GEMMA_UNAVAILABLE
    }

    lastAdmissionDecision = "Model unavailable; routing to deterministic baseline"
    lastFallbackReason = fallbackReason

    return RoutingSelection(
      selectedBackend = deterministicBackend,
      isFallback = true,
      fallbackReason = fallbackReason
    )
  }

  /**
   * Executes the request through the selected backend with concurrency safety,
   * timeout enforcement, and guaranteed explicit fallback.
   */
  suspend fun routeAndExecute(request: ReasoningRequest): ReasoningResponse {
    val selection = selectBackend(request)
    val candidateBackend = selection.selectedBackend
    val requestedBackendName = request.preferredBackend ?: geminiNanoBackend.backendType

    // If already fallback to deterministic prior to execution
    if (selection.isFallback && candidateBackend == deterministicBackend) {
      val deterministicResult = deterministicBackend.execute(request)
      return ReasoningResponse(
        requestId = request.requestId,
        correlationId = request.correlationId,
        timestamp = System.currentTimeMillis(),
        selectedBackend = requestedBackendName,
        actualBackendUsed = deterministicBackend.backendType,
        executionMode = "deterministic",
        executionStatus = ExecutionStatus.FALLBACK,
        fallbackReason = selection.fallbackReason,
        assessment = deterministicResult.assessment,
        latencyMs = deterministicResult.latencyMs,
        provenance = deterministicResult.provenance,
        errorMessage = null
      )
    }

    // Attempt neural model execution with concurrency lock & timeout
    val lockAcquired = guardrails.concurrencyLock.tryLock()
    if (!lockAcquired) {
      lastFallbackReason = FallbackReason.CONCURRENCY_LOCKED
      val deterministicResult = deterministicBackend.execute(request)
      return ReasoningResponse(
        requestId = request.requestId,
        correlationId = request.correlationId,
        timestamp = System.currentTimeMillis(),
        selectedBackend = candidateBackend.backendType,
        actualBackendUsed = deterministicBackend.backendType,
        executionMode = "deterministic",
        executionStatus = ExecutionStatus.FALLBACK,
        fallbackReason = FallbackReason.CONCURRENCY_LOCKED,
        assessment = deterministicResult.assessment,
        latencyMs = deterministicResult.latencyMs,
        provenance = deterministicResult.provenance,
        errorMessage = "Concurrent inference rejected: lock in use"
      )
    }

    try {
      val result = withTimeoutOrNull(request.timeoutMs) {
        candidateBackend.execute(request)
      }

      guardrails.markExecutionCompleted()

      if (result == null) {
        // Timed out
        lastFallbackReason = FallbackReason.MODEL_TIMEOUT
        val deterministicResult = deterministicBackend.execute(request)
        return ReasoningResponse(
          requestId = request.requestId,
          correlationId = request.correlationId,
          timestamp = System.currentTimeMillis(),
          selectedBackend = candidateBackend.backendType,
          actualBackendUsed = deterministicBackend.backendType,
          executionMode = "deterministic",
          executionStatus = ExecutionStatus.TIMEOUT,
          fallbackReason = FallbackReason.MODEL_TIMEOUT,
          assessment = deterministicResult.assessment,
          latencyMs = request.timeoutMs,
          provenance = deterministicResult.provenance,
          errorMessage = "Model exceeded deadline of ${request.timeoutMs}ms"
        )
      }

      if (!result.success) {
        // Model failure
        lastFallbackReason = FallbackReason.MODEL_INITIALIZATION_FAILED
        val deterministicResult = deterministicBackend.execute(request)
        return ReasoningResponse(
          requestId = request.requestId,
          correlationId = request.correlationId,
          timestamp = System.currentTimeMillis(),
          selectedBackend = candidateBackend.backendType,
          actualBackendUsed = deterministicBackend.backendType,
          executionMode = "deterministic",
          executionStatus = ExecutionStatus.FAILED,
          fallbackReason = FallbackReason.MODEL_INITIALIZATION_FAILED,
          assessment = deterministicResult.assessment,
          latencyMs = result.latencyMs,
          provenance = deterministicResult.provenance,
          errorMessage = result.error?.message ?: result.diagnosticNotes
        )
      }

      // True Model Success
      return ReasoningResponse(
        requestId = request.requestId,
        correlationId = request.correlationId,
        timestamp = System.currentTimeMillis(),
        selectedBackend = candidateBackend.backendType,
        actualBackendUsed = candidateBackend.backendType,
        executionMode = "model",
        executionStatus = ExecutionStatus.SUCCESS,
        fallbackReason = null,
        assessment = result.assessment,
        latencyMs = result.latencyMs,
        provenance = result.provenance,
        errorMessage = null
      )
    } finally {
      guardrails.concurrencyLock.unlock()
    }
  }
}
