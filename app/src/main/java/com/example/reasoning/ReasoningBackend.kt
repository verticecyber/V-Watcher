package com.example.reasoning

import com.example.communication.BackendModelProvenance
import com.example.communication.ReasoningRequest
import kotlinx.coroutines.flow.StateFlow

data class BackendExecutionResult(
  val success: Boolean,
  val assessment: Assessment,
  val latencyMs: Long,
  val executionMode: String, // "model" or "deterministic"
  val provenance: BackendModelProvenance,
  val error: Throwable? = null,
  val diagnosticNotes: String = ""
)

interface ReasoningBackend {
  val backendType: String // "GEMINI_NANO", "GEMMA", "DETERMINISTIC"
  val modelIdentity: String
  val state: StateFlow<ModelReadiness>
  val diagnosticNotes: String

  suspend fun checkAvailability(): ModelReadiness
  suspend fun initialize(): ModelReadiness
  suspend fun execute(request: ReasoningRequest): BackendExecutionResult
  fun shutdown()
}
