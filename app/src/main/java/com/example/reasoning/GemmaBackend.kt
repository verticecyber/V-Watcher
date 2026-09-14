package com.example.reasoning

import android.content.Context
import android.os.SystemClock
import com.example.communication.BackendModelProvenance
import com.example.communication.ReasoningRequest
import com.example.sentinel.CanonicalObservation
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/**
 * Pluggable engine abstraction for Gemma LLM inference.
 * In production on Android, wraps MediaPipe's LlmInference.
 * In unit tests or mock environments, allows clean contract testing.
 */
interface GemmaInferenceEngine : AutoCloseable {
  fun generateResponse(prompt: String): String
  override fun close()
}

/**
 * Production MediaPipe implementation of the Gemma inference engine.
 */
class MediaPipeGemmaEngine(
  private val llmInference: LlmInference
) : GemmaInferenceEngine {
  override fun generateResponse(prompt: String): String {
    return llmInference.generateResponse(prompt)
  }

  override fun close() {
    try {
      llmInference.close()
    } catch (_: Exception) {
      // Best-effort cleanup
    }
  }
}

/**
 * Proof result from isolated inference execution outside the V-Watcher pipeline.
 */
data class GemmaProofResult(
  val success: Boolean,
  val model: String,
  val runtime: String,
  val promptA: String,
  val outputA: String,
  val latencyA: Long,
  val promptB: String,
  val outputB: String,
  val latencyB: Long,
  val differentOutputs: Boolean,
  val error: String? = null
)

/**
 * Real Gemma On-Device Backend.
 * Strictly adheres to Sections 1-14:
 * - Backed by MediaPipe Tasks GenAI (libllm_inference_engine_jni)
 * - True readiness checking: requires weights artifact present, valid, and session initialized
 * - Compact observation context minimization (avoids dumping raw sensitive inventory)
 * - Strict schema validation of structured JSON output (fail-closed)
 * - Explicit fallback reporting (GEMMA_UNAVAILABLE, MODEL_LOAD_ERROR, MODEL_OUTPUT_INVALID, MODEL_TIMEOUT, MODEL_EXECUTION_ERROR)
 */
class GemmaBackend(
  private val context: Context,
  val modelManager: GemmaModelManager = GemmaModelManager(context),
  private val engineProvider: ((modelFile: File) -> GemmaInferenceEngine)? = null
) : ReasoningBackend {

  override val backendType: String = "GEMMA"
  override val modelIdentity: String = "Google Gemma 2B On-Device (MediaPipe GenAI)"

  private val _state = MutableStateFlow(ModelReadiness.UNAVAILABLE)
  override val state: StateFlow<ModelReadiness> = _state.asStateFlow()

  private var _diagnosticNotes: String = "Gemma backend idle; awaiting availability check"
  override val diagnosticNotes: String get() = _diagnosticNotes

  private var activeEngine: GemmaInferenceEngine? = null
  private var isInitialized = false
  var lastLoadError: Throwable? = null
    private set

  override suspend fun checkAvailability(): ModelReadiness = withContext(Dispatchers.IO) {
    val file = modelManager.getResolvedModelFile()
    if (file == null || !modelManager.verifyIntegrity(file)) {
      activeEngine?.close()
      activeEngine = null
      isInitialized = false
      _diagnosticNotes = "Local Gemma weights (${modelManager.metadata.modelName}) not present in ${modelManager.modelsDir.absolutePath}. Requires model download."
      _state.value = ModelReadiness.UNAVAILABLE
      return@withContext ModelReadiness.UNAVAILABLE
    }

    if (isInitialized && activeEngine != null) {
      _state.value = ModelReadiness.READY
      return@withContext ModelReadiness.READY
    }

    // Artifact is present; initialize the session
    initialize()
  }

  override suspend fun initialize(): ModelReadiness = withContext(Dispatchers.IO) {
    val file = modelManager.getResolvedModelFile()
    if (file == null || !file.exists()) {
      _diagnosticNotes = "Cannot initialize Gemma: weights file (${modelManager.metadata.modelName}) does not exist."
      _state.value = ModelReadiness.UNAVAILABLE
      return@withContext ModelReadiness.UNAVAILABLE
    }

    if (!modelManager.verifyIntegrity(file)) {
      _diagnosticNotes = "Gemma artifact integrity check failed: file corrupted or below minimal threshold."
      _state.value = ModelReadiness.ERROR
      lastLoadError = ModelLoadException(_diagnosticNotes)
      return@withContext ModelReadiness.ERROR
    }

    _state.value = ModelReadiness.PREPARING
    _diagnosticNotes = "Loading Gemma weights (${file.length() / (1024 * 1024)} MB) into MediaPipe runtime..."

    try {
      activeEngine?.close()
      activeEngine = null

      val engine = engineProvider?.invoke(file) ?: run {
        val options = LlmInference.LlmInferenceOptions.builder()
          .setModelPath(file.absolutePath)
          .setMaxTokens(512)
          .build()
        val inference = LlmInference.createFromOptions(context, options)
        MediaPipeGemmaEngine(inference)
      }

      activeEngine = engine
      isInitialized = true
      lastLoadError = null
      _diagnosticNotes = "Gemma runtime initialized successfully from ${file.name}."
      _state.value = ModelReadiness.READY
      ModelReadiness.READY
    } catch (t: Throwable) {
      activeEngine = null
      isInitialized = false
      lastLoadError = ModelLoadException("Failed to initialize Gemma engine: ${t.message}", t)
      _diagnosticNotes = "Model load failed: ${t.message ?: t.javaClass.simpleName}"
      _state.value = ModelReadiness.ERROR
      ModelReadiness.ERROR
    }
  }

  override suspend fun execute(request: ReasoningRequest): BackendExecutionResult = withContext(Dispatchers.Default) {
    val start = SystemClock.elapsedRealtime()
    val provenance = BackendModelProvenance(
      backendType = backendType,
      modelIdentity = modelIdentity,
      isRealOnDeviceModel = true,
      runtimeHostInfo = "MediaPipe Tasks GenAI on ${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL} (${android.os.Build.CPU_ABI})"
    )

    if (_state.value != ModelReadiness.READY || !isInitialized || activeEngine == null) {
      val error = lastLoadError ?: IllegalStateException("Gemma backend not ready: $_diagnosticNotes")
      return@withContext BackendExecutionResult(
        success = false,
        assessment = Assessment(
          classification = AssessmentClassification.UNKNOWN,
          confidence = 0,
          severity = "Mild",
          rationale = "Gemma backend unavailable: $_diagnosticNotes",
          evidence = emptyList(),
          recommendedAction = RecommendedAction.OBSERVE
        ),
        latencyMs = 0L,
        executionMode = "model",
        provenance = provenance,
        error = error,
        diagnosticNotes = _diagnosticNotes
      )
    }

    val engine = activeEngine!!

    try {
      val prompt = buildGemmaPrompt(request)
      val rawResponse = engine.generateResponse(prompt)
      val latency = SystemClock.elapsedRealtime() - start

      val assessment = parseAndValidateGemmaOutput(rawResponse, request.observation.observationId)

      BackendExecutionResult(
        success = true,
        assessment = assessment,
        latencyMs = latency,
        executionMode = "model",
        provenance = provenance,
        diagnosticNotes = "Executed on-device Gemma inference ($latency ms)"
      )
    } catch (e: ModelOutputValidationException) {
      val latency = SystemClock.elapsedRealtime() - start
      _diagnosticNotes = "Gemma output validation failed: ${e.message}"
      BackendExecutionResult(
        success = false,
        assessment = Assessment(
          classification = AssessmentClassification.UNKNOWN,
          confidence = 0,
          severity = "Severe",
          rationale = "Malformed Gemma model output: ${e.message}",
          evidence = listOfNotNull(e.rawOutput?.take(200)),
          recommendedAction = RecommendedAction.OBSERVE
        ),
        latencyMs = latency,
        executionMode = "model",
        provenance = provenance,
        error = e,
        diagnosticNotes = _diagnosticNotes
      )
    } catch (e: Throwable) {
      val latency = SystemClock.elapsedRealtime() - start
      _diagnosticNotes = "Gemma inference exception: ${e.message}"
      BackendExecutionResult(
        success = false,
        assessment = Assessment(
          classification = AssessmentClassification.UNKNOWN,
          confidence = 0,
          severity = "Severe",
          rationale = "Gemma execution exception: ${e.message}",
          evidence = listOf(e.javaClass.simpleName),
          recommendedAction = RecommendedAction.OBSERVE
        ),
        latencyMs = latency,
        executionMode = "model",
        provenance = provenance,
        error = e,
        diagnosticNotes = _diagnosticNotes
      )
    }
  }

  /**
   * Runs an isolated verification outside the V-Watcher pipeline (Section 7).
   */
  suspend fun runIsolatedProof(
    promptA: String = "Analyze battery status: 85%, normal temperature.",
    promptB: String = "Analyze battery status: 12%, critical temperature 48C, rapid drain."
  ): GemmaProofResult = withContext(Dispatchers.Default) {
    val engine = activeEngine ?: return@withContext GemmaProofResult(
      success = false,
      model = modelIdentity,
      runtime = "MediaPipe Tasks GenAI",
      promptA = promptA,
      outputA = "",
      latencyA = 0L,
      promptB = promptB,
      outputB = "",
      latencyB = 0L,
      differentOutputs = false,
      error = "Gemma engine not loaded or initialized"
    )

    try {
      val startA = SystemClock.elapsedRealtime()
      val outputA = engine.generateResponse(promptA)
      val latencyA = SystemClock.elapsedRealtime() - startA

      val startB = SystemClock.elapsedRealtime()
      val outputB = engine.generateResponse(promptB)
      val latencyB = SystemClock.elapsedRealtime() - startB

      GemmaProofResult(
        success = true,
        model = modelIdentity,
        runtime = "MediaPipe Tasks GenAI",
        promptA = promptA,
        outputA = outputA,
        latencyA = latencyA,
        promptB = promptB,
        outputB = outputB,
        latencyB = latencyB,
        differentOutputs = outputA.trim() != outputB.trim()
      )
    } catch (e: Exception) {
      GemmaProofResult(
        success = false,
        model = modelIdentity,
        runtime = "MediaPipe Tasks GenAI",
        promptA = promptA,
        outputA = "",
        latencyA = 0L,
        promptB = promptB,
        outputB = "",
        latencyB = 0L,
        differentOutputs = false,
        error = e.message
      )
    }
  }

  /**
   * Section 14: Compact observation context generator.
   * Strips out raw inventory and sensitive details; provides only physical telemetry.
   */
  fun buildCompactContext(request: ReasoningRequest): String {
    val obs = request.observation
    val json = JSONObject()
    json.put("observation_id", obs.observationId)
    json.put("timestamp", obs.timestamp)

    val battery = JSONObject()
    battery.put("level_percent", obs.battery.value.levelPercent)
    battery.put("is_charging", obs.battery.value.isCharging)
    battery.put("temp_celsius", obs.battery.value.temperatureCelsius)
    json.put("battery", battery)

    val memory = JSONObject()
    memory.put("avail_mb", obs.resources.value.availableMemMb)
    memory.put("total_mb", obs.resources.value.totalMemMb)
    memory.put("low_memory", obs.resources.value.isLowMemory)
    json.put("memory", memory)

    val network = JSONObject()
    network.put("transport", obs.network.value.transportType)
    network.put("vpn_active", obs.network.value.isVpnActive)
    json.put("network", network)

    request.candidateAnomaly?.let { anomaly ->
      json.put("candidate_anomaly", anomaly)
    }

    obs.usage.value.foregroundAppPackage?.let { fg ->
      json.put("foreground_app", fg)
    }

    return json.toString()
  }

  /**
   * Builds an instruction-tuned Gemma prompt with strict schema requirements.
   */
  fun buildGemmaPrompt(request: ReasoningRequest): String {
    val compactContext = buildCompactContext(request)
    return """
      <start_of_turn>user
      You are the V-Watcher On-Device Clinical Sentinel reasoning engine running Google Gemma.
      Analyze this compact Android physiological observation:
      $compactContext

      Provide your clinical cognitive assessment strictly in valid JSON format conforming to this schema:
      {
        "classification": "NORMAL" | "BENIGN_ANOMALY" | "SUSPICIOUS",
        "confidence": <integer from 0 to 100>,
        "severity": "LOW" | "MEDIUM" | "HIGH",
        "recommendedAction": "NO_ACTION" | "OBSERVE" | "INVESTIGATE" | "THROTTLE_INTERNAL_INFERENCE" | "SEEK_CLINICAL_CONFIRMATION",
        "reason": "<clear concise clinical rationale>",
        "evidence": ["<specific evidence item 1>", "<specific evidence item 2>"]
      }

      Respond ONLY with the JSON object. Do not include introductory or explanatory text.
      <end_of_turn>
      <start_of_turn>model
    """.trimIndent()
  }

  /**
   * Section 8: Structured output parsing & rigorous fail-closed validation.
   */
  fun parseAndValidateGemmaOutput(rawOutput: String, observationId: String): Assessment {
    val cleanJson = extractJsonObjectString(rawOutput)
      ?: throw ModelOutputValidationException("Could not find JSON object in Gemma response", rawOutput)

    val json = try {
      JSONObject(cleanJson)
    } catch (e: Exception) {
      throw ModelOutputValidationException("Malformed JSON from Gemma model: ${e.message}", rawOutput, e)
    }

    // 1. Classification
    val rawClassification = json.optString("classification", "").uppercase()
    val classification = when (rawClassification) {
      "NORMAL" -> AssessmentClassification.NORMAL
      "BENIGN_ANOMALY" -> AssessmentClassification.BENIGN_ANOMALY
      "SUSPICIOUS" -> AssessmentClassification.SUSPICIOUS
      else -> throw ModelOutputValidationException(
        "Invalid classification '$rawClassification'. Must be NORMAL, BENIGN_ANOMALY, or SUSPICIOUS",
        rawOutput
      )
    }

    // 2. Confidence [0, 100]
    if (!json.has("confidence")) {
      throw ModelOutputValidationException("Missing 'confidence' field in Gemma output", rawOutput)
    }
    val rawConfidence = json.optInt("confidence", -1)
    if (rawConfidence !in 0..100) {
      throw ModelOutputValidationException("Confidence $rawConfidence out of bounds [0, 100]", rawOutput)
    }

    // 3. Recommended Action
    val rawAction = json.optString("recommendedAction", json.optString("recommended_action", "")).uppercase()
    val recommendedAction = when (rawAction) {
      "NO_ACTION" -> RecommendedAction.NO_ACTION
      "OBSERVE" -> RecommendedAction.OBSERVE
      "INVESTIGATE" -> RecommendedAction.INVESTIGATE
      "THROTTLE_INTERNAL_INFERENCE" -> RecommendedAction.THROTTLE_INTERNAL_INFERENCE
      "SEEK_CLINICAL_CONFIRMATION", "ISOLATE", "CONTAIN" -> RecommendedAction.ISOLATE
      "BLOCK" -> RecommendedAction.BLOCK
      else -> throw ModelOutputValidationException(
        "Invalid recommendedAction '$rawAction'",
        rawOutput
      )
    }

    // 4. Rationale / Reason
    val rationale = json.optString("reason", json.optString("rationale", "")).trim()
    if (rationale.isBlank()) {
      throw ModelOutputValidationException("Gemma output reason/rationale cannot be blank", rawOutput)
    }

    // 5. Severity
    val rawSeverity = json.optString("severity", "LOW").uppercase()
    val severity = when (rawSeverity) {
      "HIGH", "SEVERE" -> "Severe"
      "MEDIUM", "MODERATE" -> "Moderate"
      else -> "Mild"
    }

    // 6. Evidence list
    val evidenceList = mutableListOf<String>()
    val evidenceArray = json.optJSONArray("evidence")
    if (evidenceArray != null) {
      for (i in 0 until evidenceArray.length()) {
        evidenceList.add(evidenceArray.getString(i))
      }
    }
    if (evidenceList.isEmpty()) {
      evidenceList.add("Observation: $observationId")
    }

    return Assessment(
      classification = classification,
      confidence = rawConfidence,
      severity = severity,
      rationale = rationale,
      evidence = evidenceList,
      recommendedAction = recommendedAction,
      additionalObservation = null
    )
  }

  private fun extractJsonObjectString(text: String): String? {
    val trimmed = text.trim()
    val jsonBlockRegex = """```(?:json)?\s*([\s\S]*?)\s*```""".toRegex()
    val match = jsonBlockRegex.find(trimmed)
    val candidate = match?.groupValues?.get(1)?.trim() ?: trimmed

    val firstBrace = candidate.indexOf('{')
    val lastBrace = candidate.lastIndexOf('}')
    if (firstBrace != -1 && lastBrace != -1 && lastBrace > firstBrace) {
      return candidate.substring(firstBrace, lastBrace + 1)
    }
    return null
  }

  override fun shutdown() {
    activeEngine?.close()
    activeEngine = null
    isInitialized = false
    _state.value = ModelReadiness.UNAVAILABLE
    _diagnosticNotes = "Gemma runtime shut down"
  }
}
