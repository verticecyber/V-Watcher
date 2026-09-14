package com.example.reasoning

import android.os.SystemClock
import com.example.communication.BackendModelProvenance
import com.example.communication.ReasoningRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.atomic.AtomicInteger

/**
 * First-Class Deterministic Baseline Backend.
 * Strictly adheres to Section 10:
 * - Guaranteed defensive baseline
 * - Explicitly identifies execution_mode = deterministic
 * - Never disguises output as having been produced by a neural model
 * - Evaluates real CanonicalObservation parameters deterministically
 */
class DeterministicBackend : ReasoningBackend {

  override val backendType: String = "DETERMINISTIC"
  override val modelIdentity: String = "Deterministic Clinical Baseline Rule Engine"

  private val _state = MutableStateFlow(ModelReadiness.READY)
  override val state: StateFlow<ModelReadiness> = _state.asStateFlow()

  override val diagnosticNotes: String = "Deterministic baseline operational. Guaranteed defensive fallback ready."

  val invocationCount = AtomicInteger(0)

  override suspend fun checkAvailability(): ModelReadiness {
    _state.value = ModelReadiness.READY
    return ModelReadiness.READY
  }

  override suspend fun initialize(): ModelReadiness {
    _state.value = ModelReadiness.READY
    return ModelReadiness.READY
  }

  override suspend fun execute(request: ReasoningRequest): BackendExecutionResult {
    val start = SystemClock.elapsedRealtime()
    invocationCount.incrementAndGet()

    val obs = request.observation
    val candidate = request.candidateAnomaly?.lowercase() ?: ""

    // Evaluate deterministic clinical rules
    val battery = obs.battery.value
    val resources = obs.resources.value
    val network = obs.network.value

    val isThermalExceeded = battery.temperatureCelsius > 42.0f
    val isMemoryHigh = resources.isLowMemory || resources.usedPercent > 88
    val isSocketAnomaly = candidate.contains("socket") || candidate.contains("packet") || candidate.contains("burst")
    val isPowerMismatch = battery.isPowerSaveMode && battery.levelPercent > 50

    val assessment: Assessment = when {
      isThermalExceeded -> {
        Assessment(
          classification = AssessmentClassification.SUSPICIOUS,
          confidence = 90,
          severity = "Moderate",
          rationale = "Deterministic Rule [THERMAL_EXCEEDED]: Battery temperature at ${battery.temperatureCelsius}°C exceeds safe physiological ceiling.",
          evidence = listOf(
            "Hardware battery sensor reads ${battery.temperatureCelsius}°C",
            "Observed under power state: plug=${battery.chargePlugType}"
          ),
          recommendedAction = RecommendedAction.INVESTIGATE,
          additionalObservation = "Maintain passive thermal monitoring; observe if temperature drops."
        )
      }
      isSocketAnomaly -> {
        Assessment(
          classification = AssessmentClassification.SUSPICIOUS,
          confidence = 88,
          severity = "Moderate",
          rationale = "Deterministic Rule [NETWORK_ANOMALY]: Unexpected background socket activity registered without foreground user trigger.",
          evidence = listOf(
            "Candidate anomaly: ${request.candidateAnomaly}",
            "Active network interface: ${network.transportType}, validated: ${network.isValidated}"
          ),
          recommendedAction = RecommendedAction.OBSERVE,
          additionalObservation = "Continue passive telemetry observation."
        )
      }
      isMemoryHigh -> {
        Assessment(
          classification = AssessmentClassification.BENIGN_ANOMALY,
          confidence = 82,
          severity = "Mild",
          rationale = "Deterministic Rule [MEMORY_PRESSURE]: System RAM pressure elevated (${resources.usedPercent}% used).",
          evidence = listOf(
            "Available RAM: ${resources.availableMemMb} MB of ${resources.totalMemMb} MB",
            "Low memory flag: ${resources.isLowMemory}"
          ),
          recommendedAction = RecommendedAction.NO_ACTION,
          additionalObservation = "Transient memory pressure consistent with routine application caching."
        )
      }
      isPowerMismatch -> {
        Assessment(
          classification = AssessmentClassification.BENIGN_ANOMALY,
          confidence = 75,
          severity = "Mild",
          rationale = "Deterministic Rule [POWER_SAVER_DISCREPANCY]: Power saver mode active with elevated battery reserve (${battery.levelPercent}%).",
          evidence = listOf("Battery level at ${battery.levelPercent}% while OS power saver flag is TRUE"),
          recommendedAction = RecommendedAction.NO_ACTION,
          additionalObservation = null
        )
      }
      else -> {
        Assessment(
          classification = AssessmentClassification.NORMAL,
          confidence = 96,
          severity = "Mild",
          rationale = "Deterministic Rule [EQUILIBRIUM]: All observable hardware parameters (battery, RAM, network) match established physiological baselines.",
          evidence = listOf(
            "Battery: ${battery.levelPercent}% (${battery.health})",
            "RAM: ${resources.availableMemMb} MB available",
            "Network: ${network.transportType} (Validated: ${network.isValidated})"
          ),
          recommendedAction = RecommendedAction.NO_ACTION,
          additionalObservation = null
        )
      }
    }

    val latency = (SystemClock.elapsedRealtime() - start).coerceAtLeast(1L)
    val provenance = BackendModelProvenance(
      backendType = backendType,
      modelIdentity = modelIdentity,
      isRealOnDeviceModel = false, // Honestly declares it is deterministic, not a neural model
      runtimeHostInfo = "JVM Deterministic Rule Engine"
    )

    return BackendExecutionResult(
      success = true,
      assessment = assessment,
      latencyMs = latency,
      executionMode = "deterministic",
      provenance = provenance,
      diagnosticNotes = "Executed deterministic baseline rules ($latency ms)"
    )
  }

  override fun shutdown() {
    // Stateless, no resources to release
  }
}
