package com.example.reasoning

import android.content.Context
import android.os.SystemClock
import com.example.communication.*
import com.example.sentinel.CanonicalObservation
import com.example.sentinel.FreshnessInfo
import com.example.telemetry.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import java.util.UUID

interface OnDeviceReasoningEngine {
  val engineState: StateFlow<ReasoningEngineState>
  val router: ReasoningRouter
  val communicationChannel: CommunicationChannel
  val geminiNanoBackend: GeminiNanoBackend
  val gemmaBackend: GemmaBackend
  val deterministicBackend: DeterministicBackend
  val guardrails: ResourceGuardrails

  suspend fun checkAvailability(): ModelReadiness
  suspend fun warmup()
  suspend fun reason(request: ReasoningRequest): ReasoningResponse
  suspend fun reason(bundle: ObservationBundle, battery: BatteryTelemetry?): ReasoningResult
  fun release()
  fun recordDeterministicSkip()
}

class AndroidOnDeviceReasoningEngine(
  private val context: Context
) : OnDeviceReasoningEngine {

  companion object {
    const val SYSTEM_INSTRUCTION = """
You are the V-Watcher Local Clinical Reasoning Cell on Android.
Rules:
- Operate strictly on supplied device observations.
- Do not invent telemetry or fabricate facts.
- Clearly distinguish direct observation from clinical inference.
- Prefer calm uncertainty over unsupported conclusions.
- Do not claim access to unavailable Android state.
- Do not claim an application is malicious without sufficient evidence.
- Recommend additional observation when uncertainty is high.
- Produce concise structured output.
- Optimize for low latency and minimal energy usage.
"""
  }

  override val geminiNanoBackend = GeminiNanoBackend(context)
  override val gemmaBackend = GemmaBackend(context)
  override val deterministicBackend = DeterministicBackend()
  override val guardrails = ResourceGuardrails(minIntervalMs = 2500L, criticalBatteryPercent = 15)
  override val router = ReasoningRouter(geminiNanoBackend, gemmaBackend, deterministicBackend, guardrails)
  override val communicationChannel: CommunicationChannel = DefaultCommunicationChannel(router)

  private val _engineState = MutableStateFlow(
    ReasoningEngineState(
      status = ModelReadiness.PREPARING,
      backendName = "Evaluating on-device backends...",
      statusMessage = "Probing local AICore and model runtime capability...",
      geminiNanoStatus = ModelReadiness.PREPARING,
      gemmaStatus = ModelReadiness.PREPARING,
      deterministicStatus = ModelReadiness.READY,
      selectedBackend = "GEMINI_NANO",
      actualBackendUsed = "DETERMINISTIC"
    )
  )
  override val engineState: StateFlow<ReasoningEngineState> = _engineState.asStateFlow()

  override suspend fun checkAvailability(): ModelReadiness = withContext(Dispatchers.IO) {
    val nanoStatus = geminiNanoBackend.checkAvailability()
    val gemmaStatus = gemmaBackend.checkAvailability()
    val detStatus = deterministicBackend.checkAvailability()

    val overallStatus = when {
      nanoStatus == ModelReadiness.READY -> ModelReadiness.READY
      gemmaStatus == ModelReadiness.READY -> ModelReadiness.READY
      else -> ModelReadiness.UNAVAILABLE
    }

    val activeBackend = when {
      nanoStatus == ModelReadiness.READY -> geminiNanoBackend.modelIdentity
      gemmaStatus == ModelReadiness.READY -> gemmaBackend.modelIdentity
      else -> "${deterministicBackend.modelIdentity} (Defense Baseline)"
    }

    val statusMsg = when {
      nanoStatus == ModelReadiness.READY -> "Gemini Nano active via system AICore"
      gemmaStatus == ModelReadiness.READY -> "Gemma on-device model active"
      else -> "Neural models unavailable on this hardware. Operating in 100% Deterministic Defensive Mode."
    }

    val selectedBackendValue = if (nanoStatus == ModelReadiness.READY) "GEMINI_NANO" else if (gemmaStatus == ModelReadiness.READY) "GEMMA" else "DETERMINISTIC"

    _engineState.update {
      it.copy(
        status = overallStatus,
        backendName = activeBackend,
        statusMessage = statusMsg,
        geminiNanoStatus = nanoStatus,
        geminiNanoNotes = geminiNanoBackend.diagnosticNotes,
        gemmaStatus = gemmaStatus,
        gemmaNotes = gemmaBackend.diagnosticNotes,
        deterministicStatus = detStatus,
        selectedBackend = selectedBackendValue,
        actualBackendUsed = if (overallStatus == ModelReadiness.READY) selectedBackendValue else "DETERMINISTIC"
      )
    }

    overallStatus
  }

  override suspend fun warmup() = withContext(Dispatchers.Default) {
    if (geminiNanoBackend.state.value == ModelReadiness.READY) {
      geminiNanoBackend.initialize()
      _engineState.update { it.copy(isWarm = true) }
    } else if (gemmaBackend.state.value == ModelReadiness.READY) {
      gemmaBackend.initialize()
      _engineState.update { it.copy(isWarm = true) }
    }
  }

  override suspend fun reason(request: ReasoningRequest): ReasoningResponse = withContext(Dispatchers.Default) {
    val response = communicationChannel.sendReasoningRequest(request)

    _engineState.update { current ->
      val isDeterministic = response.actualBackendUsed == "DETERMINISTIC"
      current.copy(
        lastInferenceTimeAgo = "Just now",
        latencyMs = response.latencyMs,
        reasoningCallsCount = if (!isDeterministic) current.reasoningCallsCount + 1 else current.reasoningCallsCount,
        callsAvoidedByDeterministicLayer = if (isDeterministic) current.callsAvoidedByDeterministicLayer + 1 else current.callsAvoidedByDeterministicLayer,
        deterministicCallsCount = if (isDeterministic) current.deterministicCallsCount + 1 else current.deterministicCallsCount,
        selectedBackend = response.selectedBackend,
        actualBackendUsed = response.actualBackendUsed,
        lastFallbackReason = response.fallbackReason?.code ?: "None",
        resourceAdmissionDecision = router.lastAdmissionDecision,
        latestDegradation = if (response.executionStatus == ExecutionStatus.FALLBACK || response.executionStatus == ExecutionStatus.DEGRADED) {
          "Fallback to ${response.actualBackendUsed}: ${response.fallbackReason?.description ?: "Rule triggered"}"
        } else current.latestDegradation
      )
    }

    response
  }

  override suspend fun reason(
    bundle: ObservationBundle,
    battery: BatteryTelemetry?
  ): ReasoningResult = withContext(Dispatchers.Default) {
    val obs = buildSyntheticCanonicalObservation(bundle, battery)
    val request = ReasoningRequest(
      requestId = "req_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}",
      correlationId = "corr_${System.currentTimeMillis()}",
      observation = obs,
      candidateAnomaly = bundle.candidateAnomaly,
      promptInstruction = SYSTEM_INSTRUCTION
    )

    val response = reason(request)

    when (response.executionStatus) {
      ExecutionStatus.SUCCESS -> ReasoningResult.Success(response.assessment, response.latencyMs)
      ExecutionStatus.RESOURCE_DENIED -> ReasoningResult.Skipped(
        reason = response.fallbackReason?.description ?: "Resource guardrail denial",
        fallbackAssessment = response.assessment
      )
      ExecutionStatus.FALLBACK -> {
        if (response.fallbackReason == FallbackReason.GEMINI_NANO_UNAVAILABLE ||
            response.fallbackReason == FallbackReason.GEMMA_UNAVAILABLE) {
          ReasoningResult.Unavailable(
            message = response.fallbackReason.description,
            fallbackAssessment = response.assessment
          )
        } else {
          ReasoningResult.Skipped(
            reason = response.fallbackReason?.description ?: "Fallback",
            fallbackAssessment = response.assessment
          )
        }
      }
      ExecutionStatus.FAILED -> ReasoningResult.Failed(
        error = IllegalStateException(response.errorMessage ?: "Reasoning failed"),
        fallbackAssessment = response.assessment
      )
      ExecutionStatus.TIMEOUT -> ReasoningResult.Failed(
        error = IllegalStateException("Execution timed out: ${response.errorMessage}"),
        fallbackAssessment = response.assessment
      )
      ExecutionStatus.DEGRADED -> ReasoningResult.Success(response.assessment, response.latencyMs)
    }
  }

  override fun release() {
    geminiNanoBackend.shutdown()
    gemmaBackend.shutdown()
    deterministicBackend.shutdown()
    _engineState.update { it.copy(isWarm = false) }
  }

  override fun recordDeterministicSkip() {
    _engineState.update {
      it.copy(
        callsAvoidedByDeterministicLayer = it.callsAvoidedByDeterministicLayer + 1,
        deterministicCallsCount = it.deterministicCallsCount + 1
      )
    }
  }

  private fun buildSyntheticCanonicalObservation(
    bundle: ObservationBundle,
    battery: BatteryTelemetry?
  ): CanonicalObservation {
    val now = System.currentTimeMillis()
    val batteryTel = battery ?: BatteryTelemetry(100, false, "Unknown", 25.0f, "Normal", false)
    return CanonicalObservation(
      observationId = "obs_synth_${now}",
      timestamp = now,
      battery = TelemetryResult(batteryTel, source = "BatteryProvider", availability = TelemetryAvailability.AVAILABLE, permissionState = "Granted"),
      resources = TelemetryResult(ResourceTelemetry(512L * 1024 * 1024, 2048L * 1024 * 1024, false, 256L * 1024 * 1024, 64L * 1024 * 1024), source = "DeviceResourceProvider", availability = TelemetryAvailability.AVAILABLE, permissionState = "Granted"),
      network = TelemetryResult(NetworkTelemetry("Wi-Fi", true, false, 50000, 20000), source = "NetworkTelemetryProvider", availability = TelemetryAvailability.AVAILABLE, permissionState = "Granted"),
      inventory = TelemetryResult(AppInventoryTelemetry(50, 30, 20, emptyList()), source = "AppInventoryProvider", availability = TelemetryAvailability.AVAILABLE, permissionState = "Granted"),
      usage = TelemetryResult(AppUsageTelemetry(false), source = "AppUsageProvider", availability = TelemetryAvailability.PERMISSION_REQUIRED, permissionState = "Requires User Grant"),
      system = TelemetryResult(SystemStateTelemetry("Android", 36, "Google", "Device", "Pixel", "2026-09", "en", "UTC", true), source = "SystemStateProvider", availability = TelemetryAvailability.AVAILABLE, permissionState = "Granted"),
      providerStatus = emptyMap(),
      freshness = FreshnessInfo(now, 0L, false),
      provenance = emptyList()
    )
  }
}
