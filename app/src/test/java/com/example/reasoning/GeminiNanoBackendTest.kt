package com.example.reasoning

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.communication.*
import com.example.sentinel.AndroidSentinel
import com.google.mlkit.genai.common.FeatureStatus
import com.google.mlkit.genai.common.GenAiException
import com.google.mlkit.genai.prompt.Candidate
import com.google.mlkit.genai.prompt.GenerateContentResponse
import com.google.mlkit.genai.prompt.GenerativeModel
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class GeminiNanoBackendTest {

  private val context: Context
    get() = ApplicationProvider.getApplicationContext()

  private fun createTestResponse(text: String): GenerateContentResponse {
    val candidateConstructor = Candidate::class.java.declaredConstructors.first { it.parameterTypes.size >= 2 }
    candidateConstructor.isAccessible = true
    val candidateArgs = Array<Any?>(candidateConstructor.parameterTypes.size) { null }
    candidateArgs[0] = text
    candidateArgs[1] = null
    val candidate = candidateConstructor.newInstance(*candidateArgs) as Candidate

    val responseConstructor = GenerateContentResponse::class.java.declaredConstructors.first { it.parameterTypes.size >= 2 }
    responseConstructor.isAccessible = true
    val responseArgs = Array<Any?>(responseConstructor.parameterTypes.size) { null }
    responseArgs[0] = listOf(candidate)
    responseArgs[1] = emptyList<Any>()
    return responseConstructor.newInstance(*responseArgs) as GenerateContentResponse
  }

  private fun createGenerativeModelProxy(
    status: Int = FeatureStatus.AVAILABLE,
    responseGenerator: (String) -> GenerateContentResponse = {
      createTestResponse(
        """
          {
            "classification": "NORMAL",
            "confidence": 88,
            "recommended_action": "NO_ACTION",
            "severity": "Mild",
            "rationale": "All vitals in equilibrium",
            "evidence": ["Battery 85%", "RAM healthy"]
          }
        """.trimIndent()
      )
    },
    throwOnGenerate: Throwable? = null,
    throwOnCheckStatus: Throwable? = null
  ): GenerativeModel {
    val handler = InvocationHandler { _, method: Method, args: Array<Any?>? ->
      when (method.name) {
        "checkStatus" -> {
          if (throwOnCheckStatus != null) throw throwOnCheckStatus
          status
        }
        "generateContent" -> {
          if (throwOnGenerate != null) throw throwOnGenerate
          val prompt = args?.firstOrNull { it is String } as? String ?: ""
          responseGenerator(prompt)
        }
        "close" -> Unit
        "toString" -> "GenerativeModelTestProxy"
        "hashCode" -> 42
        "equals" -> false
        else -> null
      }
    }
    return Proxy.newProxyInstance(
      GenerativeModel::class.java.classLoader,
      arrayOf(GenerativeModel::class.java),
      handler
    ) as GenerativeModel
  }

  private suspend fun buildTestRequest(candidateAnomaly: String? = null): ReasoningRequest {
    val sentinel = AndroidSentinel(context)
    val obs = sentinel.observeNow()
    return ReasoningRequest(
      requestId = "req-nano-test-1",
      correlationId = "corr-nano-test-1",
      observation = obs,
      candidateAnomaly = candidateAnomaly,
      preferredBackend = "GEMINI_NANO"
    )
  }

  @Test
  fun test_nano_does_not_claim_real_inference_when_unavailable() = runBlocking {
    // Default client on Robolectric / emulator without AICore
    val backend = GeminiNanoBackend(context)
    val readiness = backend.checkAvailability()

    // Must be UNAVAILABLE (never fabricated READY)
    assertEquals(ModelReadiness.UNAVAILABLE, readiness)
    assertEquals(ModelReadiness.UNAVAILABLE, backend.state.value)

    // Direct execution must fail closed
    val request = buildTestRequest()
    val result = backend.execute(request)
    assertFalse("Must fail when backend is unavailable", result.success)
    assertEquals(AssessmentClassification.UNKNOWN, result.assessment.classification)
    assertTrue(result.error is IllegalStateException)
  }

  @Test
  fun test_nano_availability_and_initialization() = runBlocking {
    val fakeModel = createGenerativeModelProxy(status = FeatureStatus.AVAILABLE)
    val backend = GeminiNanoBackend(context, clientProvider = { fakeModel })

    val readiness = backend.checkAvailability()
    assertEquals(ModelReadiness.READY, readiness)
    assertEquals(ModelReadiness.READY, backend.state.value)
    assertTrue(backend.diagnosticNotes.contains("AVAILABLE"))
  }

  @Test
  fun test_nano_preparing_when_downloadable() = runBlocking {
    val fakeModel = createGenerativeModelProxy(status = FeatureStatus.DOWNLOADABLE)
    val backend = GeminiNanoBackend(context, clientProvider = { fakeModel })

    val readiness = backend.checkAvailability()
    assertEquals(ModelReadiness.PREPARING, readiness)
    assertNotEquals(ModelReadiness.READY, backend.state.value)
  }

  @Test
  fun test_nano_preparing_when_downloading() = runBlocking {
    val fakeModel = createGenerativeModelProxy(status = FeatureStatus.DOWNLOADING)
    val backend = GeminiNanoBackend(context, clientProvider = { fakeModel })

    val readiness = backend.checkAvailability()
    assertEquals(ModelReadiness.PREPARING, readiness)
    assertNotEquals(ModelReadiness.READY, backend.state.value)
  }

  @Test
  fun test_nano_successful_inference() = runBlocking {
    var capturedPrompt = ""
    val fakeModel = createGenerativeModelProxy(
      status = FeatureStatus.AVAILABLE,
      responseGenerator = { prompt ->
        capturedPrompt = prompt
        createTestResponse(
          """
            {
              "classification": "BENIGN_ANOMALY",
              "confidence": 75,
              "recommended_action": "OBSERVE",
              "severity": "Mild",
              "rationale": "High temperature detected under active charging",
              "evidence": ["Battery Temp 38C", "Charging Active"]
            }
          """.trimIndent()
        )
      }
    )

    val backend = GeminiNanoBackend(context, clientProvider = { fakeModel })
    backend.checkAvailability()

    val request = buildTestRequest("Battery thermal excursion")
    val result = backend.execute(request)

    assertTrue("Execution should succeed", result.success)
    assertEquals(AssessmentClassification.BENIGN_ANOMALY, result.assessment.classification)
    assertEquals(75, result.assessment.confidence)
    assertEquals(RecommendedAction.OBSERVE, result.assessment.recommendedAction)
    assertTrue(result.assessment.rationale.contains("High temperature"))
    assertEquals(2, result.assessment.evidence.size)
    assertEquals("model", result.executionMode)
    assertEquals("GEMINI_NANO", result.provenance.backendType)

    // Verify prompt was derived from telemetry input
    assertTrue(capturedPrompt.contains(request.observation.observationId))
    assertTrue(capturedPrompt.contains("Battery thermal excursion"))
  }

  @Test
  fun test_nano_different_inputs_produce_different_prompts_and_outputs() = runBlocking {
    val capturedPrompts = mutableListOf<String>()
    val fakeModel = createGenerativeModelProxy(
      status = FeatureStatus.AVAILABLE,
      responseGenerator = { prompt ->
        capturedPrompts.add(prompt)
        val classification = if (prompt.contains("Network socket anomaly")) "SUSPICIOUS" else "NORMAL"
        val action = if (prompt.contains("Network socket anomaly")) "INVESTIGATE" else "NO_ACTION"
        createTestResponse(
          """
            {
              "classification": "$classification",
              "confidence": 90,
              "recommended_action": "$action",
              "severity": "Moderate",
              "rationale": "Derived strictly from prompt anomaly marker",
              "evidence": ["Evidence for $classification"]
            }
          """.trimIndent()
        )
      }
    )

    val backend = GeminiNanoBackend(context, clientProvider = { fakeModel })
    backend.checkAvailability()

    val request1 = buildTestRequest("Normal baseline check")
    val result1 = backend.execute(request1)

    val request2 = buildTestRequest("Network socket anomaly")
    val result2 = backend.execute(request2)

    assertEquals(2, capturedPrompts.size)
    assertNotEquals("Prompts must be distinct across different inputs", capturedPrompts[0], capturedPrompts[1])
    assertTrue("First prompt must contain first anomaly", capturedPrompts[0].contains("Normal baseline check"))
    assertTrue("Second prompt must contain second anomaly", capturedPrompts[1].contains("Network socket anomaly"))

    assertEquals(AssessmentClassification.NORMAL, result1.assessment.classification)
    assertEquals(RecommendedAction.NO_ACTION, result1.assessment.recommendedAction)

    assertEquals(AssessmentClassification.SUSPICIOUS, result2.assessment.classification)
    assertEquals(RecommendedAction.INVESTIGATE, result2.assessment.recommendedAction)
  }

  @Test
  fun test_invalid_model_output_fails_closed() = runBlocking {
    val fakeModel = createGenerativeModelProxy(
      status = FeatureStatus.AVAILABLE,
      responseGenerator = {
        // Model returns invalid classification and missing confidence
        createTestResponse("This is raw unstructured text from a hallucinating model")
      }
    )

    val backend = GeminiNanoBackend(context, clientProvider = { fakeModel })
    backend.checkAvailability()

    val request = buildTestRequest()
    val result = backend.execute(request)

    assertFalse("Must fail closed on invalid model output", result.success)
    assertTrue(result.error is ModelOutputValidationException)
    assertEquals(AssessmentClassification.UNKNOWN, result.assessment.classification)
  }

  @Test
  fun test_nano_exception_handled_safely() = runBlocking {
    val fakeModel = createGenerativeModelProxy(
      status = FeatureStatus.AVAILABLE,
      throwOnGenerate = GenAiException("AICore IPC error", null, 1)
    )

    val backend = GeminiNanoBackend(context, clientProvider = { fakeModel })
    backend.checkAvailability()

    val request = buildTestRequest()
    val result = backend.execute(request)

    assertFalse("Must report failure", result.success)
    assertTrue(result.error is GenAiException)
  }

  @Test
  fun test_nano_fallback_is_explicit_when_unavailable() = runBlocking {
    val nanoBackend = GeminiNanoBackend(context) // Unavailable by default
    val gemmaBackend = GemmaBackend(context)
    val deterministicBackend = DeterministicBackend()
    val guardrails = ResourceGuardrails(minIntervalMs = 0L)
    val router = ReasoningRouter(nanoBackend, gemmaBackend, deterministicBackend, guardrails)

    val request = buildTestRequest()
    val response = router.routeAndExecute(request)

    assertEquals("GEMINI_NANO", response.selectedBackend)
    assertEquals("DETERMINISTIC", response.actualBackendUsed)
    assertEquals("deterministic", response.executionMode)
    assertEquals(ExecutionStatus.FALLBACK, response.executionStatus)
    assertEquals(FallbackReason.GEMINI_NANO_UNAVAILABLE, response.fallbackReason)
  }

  @Test
  fun test_actual_backend_matches_runtime_when_nano_ready() = runBlocking {
    val fakeModel = createGenerativeModelProxy(status = FeatureStatus.AVAILABLE)
    val nanoBackend = GeminiNanoBackend(context, clientProvider = { fakeModel })
    nanoBackend.checkAvailability()

    val gemmaBackend = GemmaBackend(context)
    val deterministicBackend = DeterministicBackend()
    val guardrails = ResourceGuardrails(minIntervalMs = 0L)
    val router = ReasoningRouter(nanoBackend, gemmaBackend, deterministicBackend, guardrails)

    val request = buildTestRequest()
    val response = router.routeAndExecute(request)

    assertEquals("GEMINI_NANO", response.selectedBackend)
    assertEquals("GEMINI_NANO", response.actualBackendUsed)
    assertEquals("model", response.executionMode)
    assertEquals(ExecutionStatus.SUCCESS, response.executionStatus)
    assertNull(response.fallbackReason)
    assertEquals(88, response.assessment.confidence)
  }

  @Test
  fun test_invalid_model_output_fails_closed_and_falls_back_in_router() = runBlocking {
    val fakeModel = createGenerativeModelProxy(
      status = FeatureStatus.AVAILABLE,
      responseGenerator = {
        createTestResponse("INVALID_JSON_OUTPUT")
      }
    )
    val nanoBackend = GeminiNanoBackend(context, clientProvider = { fakeModel })
    nanoBackend.checkAvailability()

    val gemmaBackend = GemmaBackend(context)
    val deterministicBackend = DeterministicBackend()
    val guardrails = ResourceGuardrails(minIntervalMs = 0L)
    val router = ReasoningRouter(nanoBackend, gemmaBackend, deterministicBackend, guardrails)

    val request = buildTestRequest()
    val response = router.routeAndExecute(request)

    assertEquals("GEMINI_NANO", response.selectedBackend)
    assertEquals("DETERMINISTIC", response.actualBackendUsed)
    assertEquals("deterministic", response.executionMode)
    assertEquals(ExecutionStatus.FAILED, response.executionStatus)
    assertEquals(FallbackReason.MODEL_OUTPUT_INVALID, response.fallbackReason)
  }

  @Test
  fun test_nano_timeout_fallback_in_router() = runBlocking {
    val fakeModel = createGenerativeModelProxy(
      status = FeatureStatus.AVAILABLE,
      responseGenerator = {
        Thread.sleep(200)
        createTestResponse("{}")
      }
    )
    val nanoBackend = GeminiNanoBackend(context, clientProvider = { fakeModel })
    nanoBackend.checkAvailability()

    val gemmaBackend = GemmaBackend(context)
    val deterministicBackend = DeterministicBackend()
    val guardrails = ResourceGuardrails(minIntervalMs = 0L)
    val router = ReasoningRouter(nanoBackend, gemmaBackend, deterministicBackend, guardrails)

    val request = buildTestRequest().copy(timeoutMs = 50L)
    val response = router.routeAndExecute(request)

    assertEquals("GEMINI_NANO", response.selectedBackend)
    assertEquals("DETERMINISTIC", response.actualBackendUsed)
    assertEquals("deterministic", response.executionMode)
    assertEquals(ExecutionStatus.TIMEOUT, response.executionStatus)
    assertEquals(FallbackReason.MODEL_TIMEOUT, response.fallbackReason)
  }
}
