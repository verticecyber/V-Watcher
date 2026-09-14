package com.example.reasoning

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.communication.*
import com.example.sentinel.AndroidSentinel
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class GemmaBackendTest {

  private val context: Context
    get() = ApplicationProvider.getApplicationContext()

  private lateinit var testModelsDir: File
  private lateinit var testModelFile: File

  @Before
  fun setUp() {
    testModelsDir = File(context.filesDir, "models")
    testModelsDir.mkdirs()
    testModelFile = File(testModelsDir, "gemma-2b-it-cpu-int4.bin")
    if (testModelFile.exists()) testModelFile.delete()
  }

  @After
  fun tearDown() {
    if (testModelFile.exists()) testModelFile.delete()
  }

  private fun createFakeValidModelFile(sizeBytes: Long = 2048L): File {
    testModelFile.outputStream().use { output ->
      val chunk = ByteArray(1024) { 0x42 }
      var written = 0L
      while (written < sizeBytes) {
        val toWrite = minOf(chunk.size.toLong(), sizeBytes - written).toInt()
        output.write(chunk, 0, toWrite)
        written += toWrite
      }
    }
    return testModelFile
  }

  private fun createFakeEngine(
    delayMs: Long = 0L,
    generator: (String) -> String = {
      """
        {
          "classification": "NORMAL",
          "confidence": 92,
          "severity": "LOW",
          "recommendedAction": "NO_ACTION",
          "reason": "All device physical parameters in homeostatic equilibrium",
          "evidence": ["Battery 85%", "RAM adequate"]
        }
      """.trimIndent()
    }
  ): GemmaInferenceEngine {
    return object : GemmaInferenceEngine {
      override fun generateResponse(prompt: String): String {
        if (delayMs > 0) Thread.sleep(delayMs)
        return generator(prompt)
      }
      override fun close() = Unit
    }
  }

  private suspend fun buildTestRequest(candidateAnomaly: String? = null): ReasoningRequest {
    val sentinel = AndroidSentinel(context)
    val obs = sentinel.observeNow()
    return ReasoningRequest(
      requestId = "req-gemma-test-1",
      correlationId = "corr-gemma-test-1",
      observation = obs,
      candidateAnomaly = candidateAnomaly,
      preferredBackend = "GEMMA",
      timeoutMs = 5000L
    )
  }

  @Test
  fun test_gemma_unavailable_falls_back() = runBlocking {
    val modelManager = GemmaModelManager(context)
    val gemma = GemmaBackend(context, modelManager = modelManager)

    // Verify availability check returns UNAVAILABLE when no weights file exists
    val readiness = gemma.checkAvailability()
    assertEquals(ModelReadiness.UNAVAILABLE, readiness)
    assertEquals(ModelReadiness.UNAVAILABLE, gemma.state.value)

    val nano = GeminiNanoBackend(context) { throw IllegalStateException("No nano") }
    val det = DeterministicBackend()
    val guardrails = ResourceGuardrails(minIntervalMs = 0L)
    val router = ReasoningRouter(nano, gemma, det, guardrails)

    val request = buildTestRequest()
    val response = router.routeAndExecute(request)

    assertEquals("GEMMA", response.selectedBackend)
    assertEquals("DETERMINISTIC", response.actualBackendUsed)
    assertEquals(FallbackReason.GEMMA_UNAVAILABLE, response.fallbackReason)
    assertEquals(ExecutionStatus.FALLBACK, response.executionStatus)
  }

  @Test
  fun test_gemma_readiness_requires_loaded_model() = runBlocking {
    // Model artifact file exists on disk
    createFakeValidModelFile()
    val modelManager = GemmaModelManager(context)

    var engineCreated = false
    val gemma = GemmaBackend(
      context,
      modelManager = modelManager,
      engineProvider = {
        engineCreated = true
        createFakeEngine()
      }
    )

    // Before checkAvailability or initialize, state must be UNAVAILABLE, not READY
    assertEquals(ModelReadiness.UNAVAILABLE, gemma.state.value)
    assertFalse(engineCreated)

    // Calling checkAvailability with valid artifact triggers load and transitions to READY
    val status = gemma.checkAvailability()
    assertEquals(ModelReadiness.READY, status)
    assertEquals(ModelReadiness.READY, gemma.state.value)
    assertTrue(engineCreated)
  }

  @Test
  fun test_gemma_invalid_artifact_rejected() = runBlocking {
    // Write corrupted tiny file (< 1024 bytes)
    testModelFile.writeBytes(byteArrayOf(0x00, 0x01, 0x02))
    val modelManager = GemmaModelManager(context)

    var engineAttempted = false
    val gemma = GemmaBackend(
      context,
      modelManager = modelManager,
      engineProvider = {
        engineAttempted = true
        createFakeEngine()
      }
    )

    val status = gemma.checkAvailability()
    assertEquals(ModelReadiness.UNAVAILABLE, status)
    assertFalse(engineAttempted)

    // Directly attempting initialize() on invalid artifact must yield ERROR
    val initStatus = gemma.initialize()
    assertEquals(ModelReadiness.ERROR, initStatus)
    assertEquals(ModelReadiness.ERROR, gemma.state.value)
    assertNotNull(gemma.lastLoadError)
  }

  @Test
  fun test_gemma_model_load_failure_falls_back() = runBlocking {
    createFakeValidModelFile()
    val modelManager = GemmaModelManager(context)

    val gemma = GemmaBackend(
      context,
      modelManager = modelManager,
      engineProvider = {
        throw UnsatisfiedLinkError("libllm_inference_engine_jni.so: cannot open shared object file")
      }
    )

    val initStatus = gemma.initialize()
    assertEquals(ModelReadiness.ERROR, initStatus)
    assertEquals(ModelReadiness.ERROR, gemma.state.value)
    assertTrue(gemma.diagnosticNotes.contains("load", ignoreCase = true))

    val nano = GeminiNanoBackend(context) { throw IllegalStateException("No nano") }
    val det = DeterministicBackend()
    val guardrails = ResourceGuardrails(minIntervalMs = 0L)
    val router = ReasoningRouter(nano, gemma, det, guardrails)

    val request = buildTestRequest()
    val response = router.routeAndExecute(request)

    assertEquals("GEMMA", response.selectedBackend)
    assertEquals("DETERMINISTIC", response.actualBackendUsed)
    assertEquals(FallbackReason.MODEL_LOAD_ERROR, response.fallbackReason)
  }

  @Test
  fun test_gemma_real_inference_contract() = runBlocking {
    createFakeValidModelFile()
    val modelManager = GemmaModelManager(context)

    var capturedPrompt = ""
    val gemma = GemmaBackend(
      context,
      modelManager = modelManager,
      engineProvider = {
        createFakeEngine { prompt ->
          capturedPrompt = prompt
          """
            {
              "classification": "SUSPICIOUS",
              "confidence": 85,
              "severity": "HIGH",
              "recommendedAction": "THROTTLE_INTERNAL_INFERENCE",
              "reason": "Anomalous sustained thermal surge with low battery reserve",
              "evidence": ["Battery 14%", "Temp 46.2C"]
            }
          """.trimIndent()
        }
      }
    )

    gemma.initialize()
    assertEquals(ModelReadiness.READY, gemma.state.value)

    val request = buildTestRequest(candidateAnomaly = "Thermal runaway critical")
    val result = gemma.execute(request)

    assertTrue(result.success)
    assertEquals(AssessmentClassification.SUSPICIOUS, result.assessment.classification)
    assertEquals(85, result.assessment.confidence)
    assertEquals("Severe", result.assessment.severity)
    assertEquals(RecommendedAction.THROTTLE_INTERNAL_INFERENCE, result.assessment.recommendedAction)
    assertEquals("Anomalous sustained thermal surge with low battery reserve", result.assessment.rationale)
    assertEquals(listOf("Battery 14%", "Temp 46.2C"), result.assessment.evidence)
    assertTrue(result.provenance.isRealOnDeviceModel)
    assertEquals("GEMMA", result.provenance.backendType)
    assertTrue(capturedPrompt.contains("<start_of_turn>user"))
    assertTrue(capturedPrompt.contains("Thermal runaway critical"))
  }

  @Test
  fun test_gemma_invalid_output_fails_closed() = runBlocking {
    createFakeValidModelFile()
    val modelManager = GemmaModelManager(context)

    val gemma = GemmaBackend(
      context,
      modelManager = modelManager,
      engineProvider = {
        createFakeEngine { "NOT JSON AT ALL - COMPLETELY BROKEN OUTPUT" }
      }
    )

    gemma.initialize()
    val request = buildTestRequest()
    val result = gemma.execute(request)

    // Must fail execution with ModelOutputValidationException
    assertFalse(result.success)
    assertTrue(result.error is ModelOutputValidationException)
    assertEquals(AssessmentClassification.UNKNOWN, result.assessment.classification)

    // In router, must fail closed to deterministic baseline with MODEL_OUTPUT_INVALID
    val nano = GeminiNanoBackend(context) { throw IllegalStateException("No nano") }
    val det = DeterministicBackend()
    val guardrails = ResourceGuardrails(minIntervalMs = 0L)
    val router = ReasoningRouter(nano, gemma, det, guardrails)

    val response = router.routeAndExecute(request)
    assertEquals("GEMMA", response.selectedBackend)
    assertEquals("DETERMINISTIC", response.actualBackendUsed)
    assertEquals(FallbackReason.MODEL_OUTPUT_INVALID, response.fallbackReason)
    assertEquals(ExecutionStatus.FAILED, response.executionStatus)
    assertNotEquals(AssessmentClassification.UNKNOWN, response.assessment.classification)
  }

  @Test
  fun test_gemma_timeout_falls_back() = runBlocking {
    createFakeValidModelFile()
    val modelManager = GemmaModelManager(context)

    val gemma = GemmaBackend(
      context,
      modelManager = modelManager,
      engineProvider = {
        createFakeEngine(delayMs = 1000L)
      }
    )

    gemma.initialize()

    val nano = GeminiNanoBackend(context) { throw IllegalStateException("No nano") }
    val det = DeterministicBackend()
    val guardrails = ResourceGuardrails(minIntervalMs = 0L)
    val router = ReasoningRouter(nano, gemma, det, guardrails)

    val fastDeadlineRequest = buildTestRequest().copy(timeoutMs = 150L)
    val response = router.routeAndExecute(fastDeadlineRequest)

    assertEquals("GEMMA", response.selectedBackend)
    assertEquals("DETERMINISTIC", response.actualBackendUsed)
    assertEquals(FallbackReason.MODEL_TIMEOUT, response.fallbackReason)
    assertEquals(ExecutionStatus.TIMEOUT, response.executionStatus)
  }

  @Test
  fun test_router_reports_actual_gemma_backend() = runBlocking {
    createFakeValidModelFile()
    val modelManager = GemmaModelManager(context)

    val gemma = GemmaBackend(
      context,
      modelManager = modelManager,
      engineProvider = { createFakeEngine() }
    )

    gemma.initialize()
    assertEquals(ModelReadiness.READY, gemma.state.value)

    val nano = GeminiNanoBackend(context) { throw IllegalStateException("No nano") }
    val det = DeterministicBackend()
    val guardrails = ResourceGuardrails(minIntervalMs = 0L)
    val router = ReasoningRouter(nano, gemma, det, guardrails)

    val request = buildTestRequest()
    val response = router.routeAndExecute(request)

    assertEquals("GEMMA", response.selectedBackend)
    assertEquals("GEMMA", response.actualBackendUsed)
    assertNull(response.fallbackReason)
    assertEquals(ExecutionStatus.SUCCESS, response.executionStatus)
    assertEquals(AssessmentClassification.NORMAL, response.assessment.classification)
    assertEquals(92, response.assessment.confidence)
  }

  @Test
  fun test_router_reports_deterministic_fallback() = runBlocking {
    val modelManager = GemmaModelManager(context)
    val gemma = GemmaBackend(context, modelManager = modelManager)

    val nano = GeminiNanoBackend(context) { throw IllegalStateException("No nano") }
    val det = DeterministicBackend()
    val guardrails = ResourceGuardrails(minIntervalMs = 0L)
    val router = ReasoningRouter(nano, gemma, det, guardrails)

    val request = buildTestRequest()
    val response = router.routeAndExecute(request)

    assertEquals("GEMMA", response.selectedBackend)
    assertEquals("DETERMINISTIC", response.actualBackendUsed)
    assertEquals(FallbackReason.GEMMA_UNAVAILABLE, response.fallbackReason)
  }

  @Test
  fun test_gemma_different_inputs_change_outputs() = runBlocking {
    createFakeValidModelFile()
    val modelManager = GemmaModelManager(context)

    val gemma = GemmaBackend(
      context,
      modelManager = modelManager,
      engineProvider = {
        createFakeEngine { prompt ->
          if (prompt.contains("CRITICAL_BATTERY_DRAIN") || prompt.contains("48C") || prompt.contains("drain")) {
            """
              {
                "classification": "SUSPICIOUS",
                "confidence": 95,
                "severity": "HIGH",
                "recommendedAction": "THROTTLE_INTERNAL_INFERENCE",
                "reason": "Severe battery depletion detected",
                "evidence": ["Battery 5%"]
              }
            """.trimIndent()
          } else {
            """
              {
                "classification": "NORMAL",
                "confidence": 90,
                "severity": "LOW",
                "recommendedAction": "NO_ACTION",
                "reason": "Nominal system operation",
                "evidence": ["Battery healthy"]
              }
            """.trimIndent()
          }
        }
      }
    )

    gemma.initialize()

    val reqNormal = buildTestRequest(null)
    val resNormal = gemma.execute(reqNormal)

    val reqAnom = buildTestRequest("CRITICAL_BATTERY_DRAIN: 5% remaining")
    val resAnom = gemma.execute(reqAnom)

    assertTrue(resNormal.success)
    assertTrue(resAnom.success)
    assertEquals(AssessmentClassification.NORMAL, resNormal.assessment.classification)
    assertEquals(AssessmentClassification.SUSPICIOUS, resAnom.assessment.classification)
    assertNotEquals(resNormal.assessment.rationale, resAnom.assessment.rationale)
    assertNotEquals(resNormal.assessment.recommendedAction, resAnom.assessment.recommendedAction)

    // Also run isolated proof (Section 7)
    val proof = gemma.runIsolatedProof()
    assertTrue(proof.success)
    assertTrue(proof.differentOutputs)
  }
}
