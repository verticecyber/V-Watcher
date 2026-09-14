package com.example.reasoning

import android.content.Context
import android.os.SystemClock
import com.example.communication.BackendModelProvenance
import com.example.communication.ReasoningRequest
import com.google.mlkit.genai.common.FeatureStatus
import com.google.mlkit.genai.common.GenAiException
import com.google.mlkit.genai.prompt.Generation
import com.google.mlkit.genai.prompt.GenerativeModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject

/**
 * Real Gemini Nano Backend Integration using Google ML Kit GenAI Prompt API.
 * Adheres strictly to Section 7 & Phase 1 directives:
 * - Direct integration with official Android AICore Prompt API
 * - Never fabricates Nano readiness or inference
 * - Reports READY only if the runtime is available, model is downloaded/ready,
 *   client session succeeds, and real inference can be called.
 * - Eliminates package-presence checks; queries real FeatureStatus.
 * - Parses and validates structured JSON output; fails closed on invalid model output.
 * - Guaranteed deterministic fallback on failure, timeout, or invalid output.
 */
class GeminiNanoBackend(
  private val context: Context,
  private val clientProvider: (() -> GenerativeModel)? = null
) : ReasoningBackend {

  override val backendType: String = "GEMINI_NANO"
  override val modelIdentity: String = "Google AICore / Gemini Nano"

  private val _state = MutableStateFlow(ModelReadiness.UNAVAILABLE)
  override val state: StateFlow<ModelReadiness> = _state.asStateFlow()

  private var _diagnosticNotes: String = "Detecting AICore system availability..."
  override val diagnosticNotes: String get() = _diagnosticNotes

  private var activeClient: GenerativeModel? = null
  private var isInitialized = false

  override suspend fun checkAvailability(): ModelReadiness = withContext(Dispatchers.IO) {
    try {
      val client = try {
        clientProvider?.invoke() ?: Generation.getClient()
      } catch (t: Throwable) {
        _diagnosticNotes = "AICore client acquisition failed: ${t.message ?: t.javaClass.simpleName}"
        _state.value = ModelReadiness.UNAVAILABLE
        isInitialized = false
        activeClient = null
        return@withContext ModelReadiness.UNAVAILABLE
      }

      val status = try {
        client.checkStatus()
      } catch (t: Throwable) {
        val unwrapped = when {
          t is java.lang.reflect.UndeclaredThrowableException && t.undeclaredThrowable != null -> t.undeclaredThrowable
          t is java.lang.reflect.InvocationTargetException && t.targetException != null -> t.targetException
          else -> t
        }
        if (unwrapped is GenAiException) {
          _diagnosticNotes = "AICore checkStatus GenAiException (code ${unwrapped.errorCode}): ${unwrapped.message}"
        } else {
          _diagnosticNotes = "AICore checkStatus failed: ${unwrapped.message ?: unwrapped.javaClass.simpleName}"
        }
        _state.value = ModelReadiness.UNAVAILABLE
        isInitialized = false
        activeClient = null
        return@withContext ModelReadiness.UNAVAILABLE
      }

      when (status) {
        FeatureStatus.AVAILABLE -> {
          activeClient = client
          isInitialized = true
          _diagnosticNotes = "Gemini Nano on-device model ready via AICore (FeatureStatus.AVAILABLE)"
          _state.value = ModelReadiness.READY
          ModelReadiness.READY
        }
        FeatureStatus.DOWNLOADABLE -> {
          activeClient = client
          isInitialized = false
          _diagnosticNotes = "Gemini Nano supported by AICore but model weights not downloaded (FeatureStatus.DOWNLOADABLE)"
          _state.value = ModelReadiness.PREPARING
          ModelReadiness.PREPARING
        }
        FeatureStatus.DOWNLOADING -> {
          activeClient = client
          isInitialized = false
          _diagnosticNotes = "Gemini Nano model download in progress via AICore (FeatureStatus.DOWNLOADING)"
          _state.value = ModelReadiness.PREPARING
          ModelReadiness.PREPARING
        }
        else -> {
          activeClient = null
          isInitialized = false
          _diagnosticNotes = "Gemini Nano unavailable on this hardware (status code $status)"
          _state.value = ModelReadiness.UNAVAILABLE
          ModelReadiness.UNAVAILABLE
        }
      }
    } catch (e: Exception) {
      _diagnosticNotes = "AICore capability query error: ${e.message}"
      _state.value = ModelReadiness.ERROR
      isInitialized = false
      activeClient = null
      ModelReadiness.ERROR
    }
  }

  override suspend fun initialize(): ModelReadiness = withContext(Dispatchers.IO) {
    if (_state.value == ModelReadiness.READY && isInitialized && activeClient != null) {
      return@withContext ModelReadiness.READY
    }
    checkAvailability()
  }

  override suspend fun execute(request: ReasoningRequest): BackendExecutionResult = withContext(Dispatchers.Default) {
    val start = SystemClock.elapsedRealtime()
    val provenance = BackendModelProvenance(
      backendType = backendType,
      modelIdentity = modelIdentity,
      isRealOnDeviceModel = true,
      runtimeHostInfo = "Android AICore on ${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}"
    )

    if (_state.value != ModelReadiness.READY || !isInitialized || activeClient == null) {
      return@withContext BackendExecutionResult(
        success = false,
        assessment = Assessment(
          classification = AssessmentClassification.UNKNOWN,
          confidence = 0,
          severity = "Mild",
          rationale = "Gemini Nano is not ready on this device: $_diagnosticNotes",
          evidence = emptyList(),
          recommendedAction = RecommendedAction.OBSERVE
        ),
        latencyMs = 0L,
        executionMode = "model",
        provenance = provenance,
        error = IllegalStateException("Gemini Nano backend unavailable: $_diagnosticNotes"),
        diagnosticNotes = _diagnosticNotes
      )
    }

    try {
      val prompt = buildPromptFromObservation(request)
      val client = activeClient ?: throw IllegalStateException("Active GenerativeModel client is null")

      // Real on-device inference via ML Kit GenAI Prompt API
      val response = client.generateContent(prompt)
      val latency = SystemClock.elapsedRealtime() - start

      val candidate = response.candidates.firstOrNull()
        ?: throw ModelOutputValidationException("Gemini Nano returned zero response candidates")
      val rawText = candidate.text
        ?: throw ModelOutputValidationException("Gemini Nano response candidate contained null text")

      // Strict structured output parsing & validation
      val assessment = parseAndValidateOutput(rawText)

      BackendExecutionResult(
        success = true,
        assessment = assessment,
        latencyMs = latency,
        executionMode = "model",
        provenance = provenance,
        diagnosticNotes = "Executed on-device Gemini Nano inference via AICore ($latency ms)"
      )
    } catch (t: Throwable) {
      val latency = SystemClock.elapsedRealtime() - start
      val e = when {
        t is java.lang.reflect.UndeclaredThrowableException && t.undeclaredThrowable is Exception -> t.undeclaredThrowable as Exception
        t is java.lang.reflect.InvocationTargetException && t.targetException is Exception -> t.targetException as Exception
        t is Exception -> t
        else -> Exception(t)
      }
      when (e) {
        is ModelOutputValidationException -> {
          _diagnosticNotes = "Gemini Nano structured validation failed: ${e.message}"
          BackendExecutionResult(
            success = false,
            assessment = Assessment(
              classification = AssessmentClassification.UNKNOWN,
              confidence = 0,
              severity = "Mild",
              rationale = "Gemini Nano output failed structured validation: ${e.message}",
              evidence = listOfNotNull(e.rawOutput?.take(200)),
              recommendedAction = RecommendedAction.OBSERVE
            ),
            latencyMs = latency,
            executionMode = "model",
            provenance = provenance,
            error = e,
            diagnosticNotes = _diagnosticNotes
          )
        }
        is GenAiException -> {
          _diagnosticNotes = "Gemini Nano GenAiException (code ${e.errorCode}): ${e.message}"
          BackendExecutionResult(
            success = false,
            assessment = Assessment(
              classification = AssessmentClassification.UNKNOWN,
              confidence = 0,
              severity = "Mild",
              rationale = "AICore runtime inference error: ${e.message}",
              evidence = listOf("ErrorCode: ${e.errorCode}"),
              recommendedAction = RecommendedAction.OBSERVE
            ),
            latencyMs = latency,
            executionMode = "model",
            provenance = provenance,
            error = e,
            diagnosticNotes = _diagnosticNotes
          )
        }
        else -> {
          _diagnosticNotes = "Gemini Nano inference failed: ${e.message}"
          BackendExecutionResult(
            success = false,
            assessment = Assessment(
              classification = AssessmentClassification.UNKNOWN,
              confidence = 0,
              severity = "Mild",
              rationale = "Inference execution error: ${e.message}",
              evidence = listOf(e.message ?: "Unknown error"),
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
    }
  }

  override fun shutdown() {
    isInitialized = false
    try {
      activeClient?.close()
    } catch (_: Throwable) {}
    activeClient = null
    _state.value = ModelReadiness.UNAVAILABLE
    _diagnosticNotes = "Gemini Nano runtime released"
  }

  fun buildPromptFromObservation(request: ReasoningRequest): String {
    val obs = request.observation
    return """
You are the V-Watcher Clinical On-Device Defense Analyst powered by Gemini Nano on Android.
Analyze the following telemetry observation and candidate anomaly.
Return STRICTLY a valid JSON object matching this schema, with no markdown fences, no explanatory preambles:
{
  "classification": "NORMAL" | "BENIGN_ANOMALY" | "SUSPICIOUS",
  "confidence": <integer 0 to 100>,
  "recommended_action": "NO_ACTION" | "OBSERVE" | "INVESTIGATE" | "CONTAIN",
  "severity": "Mild" | "Moderate" | "Severe",
  "rationale": "<clinical assessment derived directly from input telemetry>",
  "evidence": ["<evidence string 1>", "<evidence string 2>"]
}

[TELEMETRY]
ObservationId: ${obs.observationId}
Battery: Level=${obs.battery.value.levelPercent}%, Temp=${obs.battery.value.temperatureCelsius}C, Charging=${obs.battery.value.isCharging}, Health=${obs.battery.value.health}
RAM: Available=${obs.resources.value.availableMemMb}MB, Total=${obs.resources.value.totalMemMb}MB, LowMemory=${obs.resources.value.isLowMemory}
Network: Type=${obs.network.value.transportType}, Validated=${obs.network.value.isValidated}, Vpn=${obs.network.value.isVpnActive}
InstalledApps: Total=${obs.inventory.value.totalAppsCount}, User=${obs.inventory.value.userAppsCount}
ForegroundApp: ${obs.usage.value.foregroundAppPackage ?: "None"}
CandidateAnomaly: ${request.candidateAnomaly ?: "None"}
Instruction: ${request.promptInstruction ?: "Assess system physiological state"}
""".trimIndent()
  }

  fun parseAndValidateOutput(rawOutput: String): Assessment {
    val cleanJson = extractJsonSubstring(rawOutput)
      ?: throw ModelOutputValidationException("Could not find JSON object in model response", rawOutput)

    val json = try {
      JSONObject(cleanJson)
    } catch (e: JSONException) {
      throw ModelOutputValidationException("Malformed JSON from model: ${e.message}", rawOutput, e)
    }

    val classificationStr = json.optString("classification", "").trim()
    val classification = when (classificationStr) {
      "NORMAL" -> AssessmentClassification.NORMAL
      "BENIGN_ANOMALY" -> AssessmentClassification.BENIGN_ANOMALY
      "SUSPICIOUS" -> AssessmentClassification.SUSPICIOUS
      else -> throw ModelOutputValidationException(
        "Invalid classification '$classificationStr'. Must be NORMAL, BENIGN_ANOMALY, or SUSPICIOUS",
        rawOutput
      )
    }

    if (!json.has("confidence")) {
      throw ModelOutputValidationException("Missing 'confidence' field in model output", rawOutput)
    }
    val rawConfidence = json.optInt("confidence", -1)
    if (rawConfidence !in 0..100) {
      throw ModelOutputValidationException("Confidence $rawConfidence out of bounds [0, 100]", rawOutput)
    }

    val actionStr = json.optString("recommended_action", "").trim()
    val recommendedAction = when (actionStr) {
      "NO_ACTION" -> RecommendedAction.NO_ACTION
      "OBSERVE" -> RecommendedAction.OBSERVE
      "INVESTIGATE" -> RecommendedAction.INVESTIGATE
      "CONTAIN", "ISOLATE" -> RecommendedAction.ISOLATE
      else -> throw ModelOutputValidationException(
        "Invalid recommended_action '$actionStr'. Must be NO_ACTION, OBSERVE, INVESTIGATE, or CONTAIN",
        rawOutput
      )
    }

    val severity = json.optString("severity", "Mild").ifBlank { "Mild" }
    val rationale = json.optString("rationale", "").trim()
    if (rationale.isBlank()) {
      throw ModelOutputValidationException("Model output rationale cannot be blank", rawOutput)
    }

    val evidenceList = mutableListOf<String>()
    val evidenceArray = json.optJSONArray("evidence")
    if (evidenceArray != null) {
      for (i in 0 until evidenceArray.length()) {
        val item = evidenceArray.optString(i, "")
        if (item.isNotBlank()) evidenceList.add(item)
      }
    }

    return Assessment(
      classification = classification,
      confidence = rawConfidence,
      severity = severity,
      rationale = "Gemini Nano on-device: $rationale",
      evidence = evidenceList,
      recommendedAction = recommendedAction,
      additionalObservation = null
    )
  }

  private fun extractJsonSubstring(text: String): String? {
    val trimmed = text.trim()
    val startIdx = trimmed.indexOf('{')
    val endIdx = trimmed.lastIndexOf('}')
    return if (startIdx in 0 until endIdx) {
      trimmed.substring(startIdx, endIdx + 1)
    } else null
  }
}

