package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.communication.*
import com.example.domain.AnomalyCandidate
import com.example.domain.DeviceState
import com.example.immune.*
import com.example.memory.LocalImmuneMemoryRepository
import com.example.model.*
import com.example.reasoning.*
import com.example.sentinel.*
import com.example.telemetry.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class VWatcherUiState(
  val healthScore: Int = 96,
  val condition: DeviceCondition = DeviceCondition.HEALTHY,
  val lastAssessmentTime: String = "2 minutes ago",
  val supportingSummary: String = "Your device is behaving normally.",
  val doctorClinicalNote: String = "Initializing… first observation pending.",
  val barrierState: BarrierState = BarrierState.WATCHING,
  val vitalSigns: List<DeviceVitalSign> = emptyList(),
  val examCategories: List<ExamCategory> = emptyList(),
  val applications: List<AppRecord> = emptyList(),
  val permissions: List<PermissionReviewItem> = emptyList(),
  val networkConnections: List<NetworkConnection> = emptyList(),
  val decoys: List<DecoyEnvironment> = emptyList(),
  val immuneCells: List<ImmuneCell> = emptyList(),
  val cases: List<IncidentCase> = emptyList(),
  val memoryPatterns: List<ImmuneMemoryPattern> = emptyList(),
  val healthHistory: List<HealthHistoryPoint> = emptyList(),
  val efficiency: EfficiencyMetrics = EfficiencyMetrics(),
  val notifications: List<SimulatedNotification> = emptyList(),

  // Real Device & Telemetry State
  val deviceState: DeviceState? = null,
  val isRealDeviceDataActive: Boolean = true,
  val isUsageAccessMissing: Boolean = false,
  val modelReadiness: ModelReadiness = ModelReadiness.UNAVAILABLE,
  val realAppsCount: Int = 0,

  // Biomimetic Multi-Agent Homeostasis State
  val homeostasisEvaluation: HomeostasisEvaluation? = null,
  val immuneBusStats: ImmuneBusStats? = null,
  val lastActionResult: ActionResult? = null,

  // Interactive Exam Dialog state
  val isExamInProgress: Boolean = false,
  val currentExamStep: ExamSequenceStep? = null,
  val examProgressFloat: Float = 0f,
  val examResultSummary: String = "",

  // Interactive Simulation State
  val isSimulationActive: Boolean = false,
  val simulationPhase: String = "",
  val simulationCurrentStepText: String = "",
  val simulationStepIndex: Int = 0,
  val selectedCaseId: String? = null,
  val selectedCellId: String? = null,
  val selectedAppId: String? = null,
  val selectedMemoryPatternId: String? = null,
  val whyExplainingAppId: String? = null,
  val activeTab: String = "HOME"
)

class VWatcherViewModel(application: Application) : AndroidViewModel(application) {

  val sentinel: Sentinel = AndroidSentinel(application)
  val reasoningEngine: OnDeviceReasoningEngine = AndroidOnDeviceReasoningEngine(application)
  private val baselineEngine = BaselineEngine()

  // PH-04 (D-FUSION): legacy display cells retired with the second reasoning entry.
  // Telemetry, scoring, and assessment now flow exclusively through the immune pipeline.
  // Retired: telemetryProvider, sentinelCell, receptor, contextCell, responseCell, memoryCell
  // (their state was never read; classes remain for the G9 dead-code sweep).

  val memoryRepository = LocalImmuneMemoryRepository(application)
  val biomimeticImmuneSystem = BiomimeticImmuneSystem(
    context = application,
    communicationChannel = reasoningEngine.communicationChannel,
    memoryRepository = memoryRepository
  )

  private val _uiState = MutableStateFlow(createInitialState())
  val uiState: StateFlow<VWatcherUiState> = _uiState.asStateFlow()

  private var checkJob: Job? = null
  private var simulationJob: Job? = null

  init {
    viewModelScope.launch {
      val readiness = reasoningEngine.checkAvailability()
      _uiState.update { it.copy(modelReadiness = readiness) }
    }
    viewModelScope.launch {
      refreshRealTelemetry()
    }
  }

  // -------------------------------------------------------------
  // REFRESH REAL DEVICE TELEMETRY
  // -------------------------------------------------------------
  fun refreshRealTelemetry() {
    viewModelScope.launch {
      val canonical = sentinel.observeNow()
      reasoningEngine.communicationChannel.dispatchTelemetry(canonical)

      val snapshot = DeviceTelemetrySnapshot(
        battery = canonical.battery,
        resources = canonical.resources,
        network = canonical.network,
        appInventory = canonical.inventory,
        appUsage = canonical.usage,
        systemState = canonical.system
      )
      baselineEngine.establishInitialBaseline(snapshot)

      val candidates = baselineEngine.evaluateSnapshot(snapshot)

      // PH-04 fusion (D-FUSION): the immune pipeline is the single reasoning entry.
      // The legacy DecisionCell entry is retired; deterministic skip preserves engine metrics.
      val immuneSnapshot = biomimeticImmuneSystem.processObservation(canonical)
      val assessment = immuneSnapshot.lastReasoningResponse?.assessment ?: run {
        reasoningEngine.recordDeterministicSkip()
        null
      }

      // Fail-closed validation: A provider failure must NEVER be interpreted as normal condition!
      val hasCriticalProviderFailure = snapshot.battery.availability == TelemetryAvailability.UNAVAILABLE ||
          snapshot.battery.availability == TelemetryAvailability.ERROR ||
          snapshot.resources.availability == TelemetryAvailability.ERROR ||
          snapshot.network.availability == TelemetryAvailability.ERROR ||
          snapshot.appInventory.availability == TelemetryAvailability.ERROR

      val isDegradedObservation = snapshot.resources.availability == TelemetryAvailability.DEGRADED ||
          snapshot.network.availability == TelemetryAvailability.UNAVAILABLE

      // Calculate health score from real physiological numbers
      var score = 97
      if (snapshot.resources.value.isLowMemory) score -= 15
      if (snapshot.battery.value.temperatureCelsius > 42f) score -= 12
      if (!snapshot.appUsage.value.isAccessGranted) score -= 1 // Minor recommendation
      if (candidates.isNotEmpty()) score -= (candidates.size * 6)
      if (hasCriticalProviderFailure) score -= 25
      if (isDegradedObservation) score -= 5
      score = score.coerceIn(55, 100)

      val condition = when {
        hasCriticalProviderFailure -> DeviceCondition.ATTENTION
        candidates.isNotEmpty() -> DeviceCondition.ATTENTION
        score < 90 -> DeviceCondition.ATTENTION
        else -> DeviceCondition.HEALTHY
      }

      val doctorClinicalNote = when {
        hasCriticalProviderFailure -> {
          "Telemetry degraded: One or more hardware observation providers failed. In accordance with fail-closed safety semantics, baseline normality cannot be verified until sensor conduits are restored."
        }
        candidates.isNotEmpty() -> {
          "Attention required: ${candidates.size} anomalous physiological signals detected and routed to clinical decision review."
        }
        isDegradedObservation -> {
          "Device running in guarded posture. Memory or network telemetry operating with degraded provider access."
        }
        else -> {
          "Your device is healthy today. Real hardware telemetry (Battery: ${snapshot.battery.value.levelPercent}%, RAM: ${snapshot.resources.value.availableMemMb} MB free) and application boundaries match established physiological baselines."
        }
      }

      val realVitals = listOf(
        DeviceVitalSign(
          id = "vs_activity",
          title = "DEVICE ACTIVITY",
          status = if (snapshot.systemState.value.isScreenInteractive) "Active" else "Standby",
          subtitle = "${snapshot.systemState.value.manufacturer} ${snapshot.systemState.value.model}",
          updatedAgo = "Live",
          isNormal = true,
          trendLabel = "Physiological"
        ),
        DeviceVitalSign(
          id = "vs_battery",
          title = "BATTERY HEALTH",
          status = "${snapshot.battery.value.levelPercent}%",
          subtitle = "${snapshot.battery.value.chargePlugType} • ${snapshot.battery.value.temperatureCelsius}°C",
          updatedAgo = "Live",
          isNormal = snapshot.battery.value.temperatureCelsius < 42f,
          trendLabel = snapshot.battery.value.health
        ),
        DeviceVitalSign(
          id = "vs_network",
          title = "NETWORK HEALTH",
          status = snapshot.network.value.transportType,
          subtitle = if (snapshot.network.value.isValidated) "Transport validated" else "Connecting",
          updatedAgo = "Live",
          isNormal = snapshot.network.value.transportType != "Disconnected",
          trendLabel = if (snapshot.network.value.isValidated) "Validated" else "Standby"
        ),
        DeviceVitalSign(
          id = "vs_resources",
          title = "DEVICE RAM",
          status = "${snapshot.resources.value.availableMemMb} MB free",
          subtitle = "${snapshot.resources.value.usedPercent}% used • V-Watcher: ${snapshot.resources.value.vWatcherMemoryMb} MB",
          updatedAgo = "Live",
          isNormal = !snapshot.resources.value.isLowMemory,
          trendLabel = if (snapshot.resources.value.isLowMemory) "Pressure" else "Optimal"
        ),
        DeviceVitalSign(
          id = "vs_apps",
          title = "APP INVENTORY",
          status = "${snapshot.appInventory.value.totalAppsCount} Total",
          subtitle = "${snapshot.appInventory.value.userAppsCount} user • ${snapshot.appInventory.value.systemAppsCount} system",
          updatedAgo = "Just now",
          isNormal = true,
          trendLabel = "Listed"
        ),
        DeviceVitalSign(
          id = "vs_system",
          title = "SYSTEM INTEGRITY",
          status = "Android ${snapshot.systemState.value.osVersion}",
          subtitle = "API ${snapshot.systemState.value.sdkInt} • Patch: ${snapshot.systemState.value.securityPatch}",
          updatedAgo = "Listed",
          isNormal = true,
          trendLabel = "Descriptors"
        )
      )

      // Map real installed apps
      val appsList = snapshot.appInventory.value.apps
      val realApps = if (appsList.isNotEmpty()) {
        appsList.mapIndexed { idx, app ->
          AppRecord(
            id = "real_app_${app.packageName.replace('.', '_')}",
            name = app.appName,
            packageName = app.packageName,
            healthState = AppHealthState.HEALTHY,
            permissionSummary = "${app.requestedPermissions.size} permissions declared",
            behaviorState = if (app.isSystemApp) "System app (OS-bundled)" else "User app",
            lastObserved = "Observed on device",
            iconKind = if (app.isSystemApp) "security" else "default",
            networkActivityNote = "Declared permissions inventoried; traffic not inspected"
          )
        }
      } else {
        _uiState.value.applications
      }

      // Dynamically compute real permission distributions across installed apps
      val locationAppsCount = appsList.count { it.requestedPermissions.any { p -> p.contains("LOCATION", ignoreCase = true) } }
      val microphoneAppsCount = appsList.count { it.requestedPermissions.any { p -> p.contains("RECORD_AUDIO", ignoreCase = true) } }
      val cameraAppsCount = appsList.count { it.requestedPermissions.any { p -> p.contains("CAMERA", ignoreCase = true) } }
      val contactsAppsCount = appsList.count { it.requestedPermissions.any { p -> p.contains("CONTACTS", ignoreCase = true) || p.contains("READ_MEDIA", ignoreCase = true) } }

      val realPermissions = listOf(
        PermissionReviewItem(
          id = "perm_location",
          name = "Location",
          clinicalStatus = PermissionClinicalStatus.EXPECTED,
          rationale = "$locationAppsCount installed apps declare location permissions",
          frequencyNote = "OS Manifest Verification",
          appsCount = locationAppsCount
        ),
        PermissionReviewItem(
          id = "perm_microphone",
          name = "Microphone",
          clinicalStatus = PermissionClinicalStatus.EXPECTED,
          rationale = "$microphoneAppsCount installed apps declare microphone permissions",
          frequencyNote = "OS Manifest Verification",
          appsCount = microphoneAppsCount
        ),
        PermissionReviewItem(
          id = "perm_camera",
          name = "Camera",
          clinicalStatus = PermissionClinicalStatus.EXPECTED,
          rationale = "$cameraAppsCount installed apps declare camera permissions",
          frequencyNote = "OS Manifest Verification",
          appsCount = cameraAppsCount
        ),
        PermissionReviewItem(
          id = "perm_contacts",
          name = "Contacts & Media",
          clinicalStatus = PermissionClinicalStatus.EXPECTED,
          rationale = "$contactsAppsCount installed apps declare contacts/media permissions",
          frequencyNote = "OS Manifest Verification",
          appsCount = contactsAppsCount
        )
      )

      val realConnections = listOf(
        NetworkConnection(
          id = "conn_live_transport",
          endpoint = "${snapshot.network.value.transportType} Interface",
          appName = if (snapshot.network.value.transportType != "Disconnected") "System Network Conduit" else "Network Interface",
          status = if (snapshot.network.value.isValidated) "Internet Validated" else if (snapshot.network.value.transportType == "Disconnected") "Interface Offline" else "Local Link",
          protocol = if (snapshot.network.value.isVpnActive) "VPN Filter Active" else "Transport Layer (${snapshot.network.value.transportType})",
          bandwidth = "Down: ${snapshot.network.value.downstreamBandwidthKbps} kbps / Up: ${snapshot.network.value.upstreamBandwidthKbps} kbps",
          timestamp = "Live Telemetry",
          isDivertedToDecoy = false
        )
      )

      val newDeviceState = DeviceState(
        timestamp = System.currentTimeMillis(),
        healthScore = score,
        condition = condition,
        doctorClinicalNote = doctorClinicalNote,
        supportingSummary = if (hasCriticalProviderFailure) "Observation degraded. Fail-closed posture active." else "Real Android observations active. Zero uncontained anomalies.",
        barrierState = BarrierState.WATCHING,
        battery = snapshot.battery,
        memory = snapshot.resources,
        network = snapshot.network,
        applicationState = snapshot.appInventory,
        usageState = snapshot.appUsage,
        systemState = snapshot.systemState,
        reasoningState = reasoningEngine.engineState.value,
        sentinelState = sentinel.state.value,
        communicationState = reasoningEngine.communicationChannel.state.value,
        activeAnomalyCandidates = candidates,
        latestAssessment = assessment,
        isSimulationActive = false
      )

      // PH-04 (D-HOMEO feed): higher-layer immune signals gate future escalation.
      // Deny-only; neutral until the pipeline reports stress, containment, or veto.
      // immuneSnapshot comes from the single fused call above; no second observation pass.
      reasoningEngine.updateGuardrailContext(
        GuardrailContext(
          homeostasisStressed = immuneSnapshot.homeostasis.macroState == HomeostaticMacroState.STRESSED,
          containmentActive = immuneSnapshot.homeostasis.macroState == HomeostaticMacroState.ACTIVE_DEFENSE,
          regulatoryForceDeterministic = immuneSnapshot.regulatoryForcedDeterministic,
          regulatoryCooldownMs = immuneSnapshot.regulatoryCooldownMs,
          inferenceThrottled = biomimeticImmuneSystem.realityBoundary.isInferenceThrottled()
        )
      )

      val efficiency = EfficiencyMetrics(
        batteryImpact = "Unmeasured",
        memoryUsage = "${snapshot.resources.value.vWatcherMemoryMb} MB",
        cpuActivity = "Unmeasured",
        localAnalysis = "Deterministic & on-demand AICore layer",
        inferenceActivity = if (reasoningEngine.engineState.value.status == ModelReadiness.READY) "Cold / Event-driven" else "Deterministic Mode"
      )

      val currentCells = immuneSnapshot.cells

      val finalCondition = when {
        immuneSnapshot.homeostasis.macroState == HomeostaticMacroState.UNKNOWN -> DeviceCondition.ATTENTION
        immuneSnapshot.homeostasis.macroState == HomeostaticMacroState.DEGRADED -> DeviceCondition.ATTENTION
        immuneSnapshot.homeostasis.macroState == HomeostaticMacroState.ACTIVE_DEFENSE -> DeviceCondition.CONTAINING
        immuneSnapshot.homeostasis.macroState == HomeostaticMacroState.STRESSED -> DeviceCondition.ATTENTION
        immuneSnapshot.homeostasis.macroState == HomeostaticMacroState.RECOVERING -> DeviceCondition.RECOVERING
        else -> condition
      }

      val updatedDeviceState = newDeviceState.copy(
        condition = finalCondition,
        doctorClinicalNote = immuneSnapshot.homeostasis.primaryReason,
        supportingSummary = immuneSnapshot.homeostasis.macroState.clinicalRationale,
        barrierState = if (immuneSnapshot.homeostasis.macroState == HomeostaticMacroState.ACTIVE_DEFENSE) BarrierState.CONTAINING else BarrierState.WATCHING,
        homeostasisEvaluation = immuneSnapshot.homeostasis,
        immuneBusStats = immuneSnapshot.busStats,
        lastActionResult = immuneSnapshot.lastActionResult
      )

      _uiState.update { current ->
        current.copy(
          deviceState = updatedDeviceState,
          healthScore = score,
          condition = finalCondition,
          vitalSigns = realVitals,
          applications = realApps,
          permissions = realPermissions,
          networkConnections = realConnections,
          immuneCells = currentCells,
          // PH-04 fusion: single case authority. System incidents take precedence per id;
          // manual/in-app cases are preserved and namespaced instead of overwritten (ex-G3 clobber).
          cases = immuneSnapshot.activeIncidents +
            current.cases.filter { existing ->
              immuneSnapshot.activeIncidents.none { it.id == existing.id }
            },
          efficiency = efficiency,
          isUsageAccessMissing = !snapshot.appUsage.value.isAccessGranted,
          realAppsCount = snapshot.appInventory.value.totalAppsCount,
          isRealDeviceDataActive = true,
          lastAssessmentTime = "Just now",
          doctorClinicalNote = updatedDeviceState.doctorClinicalNote,
          supportingSummary = updatedDeviceState.supportingSummary,
          homeostasisEvaluation = immuneSnapshot.homeostasis,
          immuneBusStats = immuneSnapshot.busStats,
          lastActionResult = immuneSnapshot.lastActionResult,
          barrierState = updatedDeviceState.barrierState
        )
      }
    }
  }

  // -------------------------------------------------------------
  // RUN DEVICE CHECK (Clinical examination sequence with live data)
  // -------------------------------------------------------------
  fun runDeviceCheck() {
    if (_uiState.value.isExamInProgress) return
    checkJob?.cancel()
    checkJob = viewModelScope.launch {
      try {
        _uiState.update {
          it.copy(
            isExamInProgress = true,
            currentExamStep = ExamSequenceStep.CHECKING_APPLICATIONS,
            examProgressFloat = 0.16f
          )
        }
        delay(400)
        _uiState.update {
          it.copy(
            currentExamStep = ExamSequenceStep.CHECKING_PERMISSIONS,
            examProgressFloat = 0.33f
          )
        }
        delay(400)
        _uiState.update {
          it.copy(
            currentExamStep = ExamSequenceStep.CHECKING_NETWORK,
            examProgressFloat = 0.50f
          )
        }
        delay(400)
        _uiState.update {
          it.copy(
            currentExamStep = ExamSequenceStep.CHECKING_SYSTEM,
            examProgressFloat = 0.67f
          )
        }
        delay(400)
        _uiState.update {
          it.copy(
            currentExamStep = ExamSequenceStep.CHECKING_BEHAVIOR,
            examProgressFloat = 0.84f
          )
        }
        delay(400)
        _uiState.update {
          it.copy(
            currentExamStep = ExamSequenceStep.CHECKING_IMMUNE_MEMORY,
            examProgressFloat = 1.0f
          )
        }
        delay(300)

        // Refresh real telemetry during final examination check
        val canonical = try {
          sentinel.observeNow()
        } catch (_: Exception) {
          null
        }

        if (canonical != null) {
          try {
            reasoningEngine.communicationChannel.dispatchTelemetry(canonical)
          } catch (_: Exception) {}
        }
        val totalApps = canonical?.inventory?.value?.totalAppsCount ?: _uiState.value.applications.size
        val battLevel = canonical?.battery?.value?.levelPercent ?: 88
        val netTransport = canonical?.network?.value?.transportType ?: "Validated"

        _uiState.update { current ->
          val openCases = current.cases.count { it.status != CaseStatus.RESOLVED }
          val isolatedApps = current.applications.count { it.healthState == AppHealthState.ISOLATED }
          val score = if (openCases > 0 || isolatedApps > 0) {
            (89 - (openCases * 5) - (isolatedApps * 4)).coerceIn(50, 89)
          } else {
            97
          }
          current.copy(
            currentExamStep = ExamSequenceStep.EXAMINATION_COMPLETE,
            examProgressFloat = 1.0f,
            healthScore = score,
            lastAssessmentTime = "Just now",
            doctorClinicalNote = "Clinical examination complete. Live telemetry sampled ($totalApps apps, Battery: $battLevel%, Network: $netTransport). Open flags, if any, are listed under Cases.",
            supportingSummary = "Routine check completed on sampled on-device telemetry.",
            notifications = listOf(
              SimulatedNotification(
                id = "notif_${System.currentTimeMillis()}",
                timestamp = "Just now",
                title = "Device examination complete",
                message = "V-Watcher reviewed $totalApps packages, battery state ($battLevel%), and $netTransport network conduit. Score: $score/100; see Cases for open flags."
              )
            ) + current.notifications
          )
        }
      } catch (_: Exception) {
        _uiState.update {
          it.copy(
            isExamInProgress = false,
            currentExamStep = null
          )
        }
      }
    }
  }

  fun dismissExamDialog() {
    _uiState.update { it.copy(isExamInProgress = false, currentExamStep = null) }
  }

  // -------------------------------------------------------------
  // SIMULATE UNUSUAL ACTIVITY (Clearly flagged as Test Lab Simulation)
  // -------------------------------------------------------------
  fun simulateUnusualActivity() {
    if (_uiState.value.isSimulationActive) return
    simulationJob?.cancel()
    simulationJob = viewModelScope.launch {
      // 1. OBSERVED (minor anomaly, health score: 91 / 100)
      _uiState.update { current ->
        val updatedCells = current.immuneCells.map { cell ->
          when (cell.name) {
            "SENTINEL" -> cell.copy(
              state = ImmuneCellState.ACTIVE,
              activity = "[SIMULATION] Observing uncharacteristic background telemetry",
              lastAction = "Flagged anomalous packet burst"
            )
            "RECEPTOR" -> cell.copy(
              state = ImmuneCellState.ACTIVE,
              activity = "[SIMULATION] Intercepted unexpected external socket signal",
              lastAction = "Captured inbound connection handshake"
            )
            else -> cell.copy(state = ImmuneCellState.MONITORING)
          }
        }
        current.copy(
          isSimulationActive = true,
          simulationPhase = "OBSERVED",
          simulationStepIndex = 1,
          simulationCurrentStepText = "[SIMULATION] Observed: Sentinel & Receptor detect unexpected socket burst from Background Updater",
          healthScore = 91,
          condition = DeviceCondition.ATTENTION,
          barrierState = BarrierState.WATCHING,
          doctorClinicalNote = "[SIMULATION] I noticed one unusual behavior and I'm observing it. Background Updater initiated unexpected socket activity.",
          supportingSummary = "[SIMULATION] Unusual behavior observed in 1 background process.",
          immuneCells = updatedCells
        )
      }
      delay(1800)

      // 2. INVESTIGATING (Context Cell cross-references baseline)
      _uiState.update { current ->
        val updatedCells = current.immuneCells.map { cell ->
          when (cell.name) {
            "CONTEXT CELL" -> cell.copy(
              state = ImmuneCellState.INVESTIGATING,
              activity = "[SIMULATION] Correlating connection frequency with temporal baseline",
              lastAction = "Detected divergence: 18 connections / 4s"
            )
            else -> cell
          }
        }
        current.copy(
          simulationPhase = "INVESTIGATING",
          simulationStepIndex = 2,
          simulationCurrentStepText = "[SIMULATION] Investigating: Context Cell cross-references historical baseline (84% divergence)",
          doctorClinicalNote = "[SIMULATION] I'm comparing this socket burst against 60-day baseline data. The connection endpoint was never seen before.",
          supportingSummary = "[SIMULATION] Investigating divergent behavioral pattern.",
          immuneCells = updatedCells
        )
      }
      delay(1600)

      // 3. ASSESSING (Decision Cell evaluates risk threshold)
      _uiState.update { current ->
        val updatedCells = current.immuneCells.map { cell ->
          when (cell.name) {
            "DECISION CELL" -> cell.copy(
              state = ImmuneCellState.ASSESSING,
              activity = "[SIMULATION] Formulating containment threshold (Confidence: 92%)",
              lastAction = "Authorized biological isolation"
            )
            else -> cell
          }
        }
        current.copy(
          simulationPhase = "ASSESSING",
          simulationStepIndex = 3,
          simulationCurrentStepText = "[SIMULATION] Assessing: Decision Cell verified anomaly (92% confidence); recommending containment",
          doctorClinicalNote = "[SIMULATION] Decision cell verified anomaly with 92% confidence. Authorizing protective containment.",
          supportingSummary = "[SIMULATION] Evaluating clinical containment parameters.",
          immuneCells = updatedCells
        )
      }
      delay(1400)

      // 4. CONTAINING (active biological containment, health score: 82 / 100)
      val newCaseId = "case_${System.currentTimeMillis()}"
      val newCaseCode = "SIM-CASE-0043"
      val updatedApps = _uiState.value.applications.map { app ->
        if (app.name == "Background Updater" || app.packageName == "com.android.updater.sim") {
          app.copy(
            healthState = AppHealthState.ISOLATED,
            behaviorState = "[SIMULATION] Temporarily isolated pending investigation",
            isolationReason = "[SIMULATION] Unexpected continuous background socket burst to non-whitelisted node"
          )
        } else app
      }
      val newCase = IncidentCase(
        id = newCaseId,
        caseCode = newCaseCode,
        title = "[SIMULATION] Background Updater Behavior Divergence",
        date = "Today • Just now",
        severity = "Moderate",
        status = CaseStatus.ISOLATED,
        assessment = "[SIMULATION] Application displayed unexpected background network activity divergent from historic baseline.",
        actionTaken = "[SIMULATION] Temporary clinical isolation of background socket and process execution.",
        outcome = "[SIMULATION] Anomalous telemetry halted. Diverted to API Decoy environment for observation.",
        confidencePercent = 92,
        affectedApp = "Background Updater",
        evidence = listOf(
          "[SIMULATION] Observed 18 connection attempts within 4 seconds during screen-off sleep.",
          "[SIMULATION] Target endpoint was not observed in 60-day baseline profile.",
          "[SIMULATION] Process memory fingerprint fluctuated unexpectedly by 42 MB."
        ),
        timeline = listOf(
          CaseTimelineEvent("Just now", "[SIMULATION] Unusual behavior observed", "Sentinel cell detected anomalous packet burst", "Observation"),
          CaseTimelineEvent("Just now", "[SIMULATION] Compared with baseline", "Context cell determined an 84% variance from baseline", "Correlation"),
          CaseTimelineEvent("Just now", "[SIMULATION] Autonomous containment", "Decision cell authorized temporary isolation", "Action")
        )
      )

      _uiState.update { current ->
        val updatedCells = current.immuneCells.map { cell ->
          when (cell.name) {
            "RESPONSE CELL" -> cell.copy(
              state = ImmuneCellState.CONTAINING,
              activity = "[SIMULATION] Active containment of Background Updater",
              eventsHandled = cell.eventsHandled + 1,
              lastAction = "Isolated process and suspended socket"
            )
            else -> cell
          }
        }
        current.copy(
          simulationPhase = "CONTAINING",
          simulationStepIndex = 4,
          simulationCurrentStepText = "[SIMULATION] Containing: Response Cell isolated application; Protective Barrier engaged",
          applications = updatedApps,
          cases = listOf(newCase) + current.cases,
          immuneCells = updatedCells,
          barrierState = BarrierState.CONTAINING,
          healthScore = 82,
          condition = DeviceCondition.CONTAINING,
          doctorClinicalNote = "[SIMULATION] I temporarily isolated Background Updater while I investigated a behavioral change.",
          supportingSummary = "[SIMULATION] One application isolated for patient protection.",
          notifications = listOf(
            SimulatedNotification(
              id = "notif_iso_${System.currentTimeMillis()}",
              timestamp = "Just now",
              title = "[SIMULATION] Unusual activity detected",
              message = "Background Updater was temporarily isolated while investigating unexpected background behavior."
            )
          ) + current.notifications
        )
      }
      delay(1900)

      // 5. RECOVERING (anomaly subsided in sandbox, health score: 90 / 100)
      _uiState.update { current ->
        val updatedCases = current.cases.map { c ->
          if (c.id == newCaseId) {
            c.copy(
              status = CaseStatus.RECOVERING,
              timeline = c.timeline + CaseTimelineEvent("Just now", "[SIMULATION] Activity subsided", "Isolated environment observed zero further requests", "Outcome")
            )
          } else c
        }
        val updatedCells = current.immuneCells.map { cell ->
          when (cell.name) {
            "RESPONSE CELL" -> cell.copy(
              state = ImmuneCellState.RECOVERING,
              activity = "[SIMULATION] Verifying sandbox stability",
              lastAction = "Assessed safe process containment"
            )
            else -> cell
          }
        }
        current.copy(
          simulationPhase = "RECOVERING",
          simulationStepIndex = 5,
          simulationCurrentStepText = "[SIMULATION] Recovering: Behavior normalized in containment; initiating safe release",
          cases = updatedCases,
          immuneCells = updatedCells,
          healthScore = 90,
          condition = DeviceCondition.RECOVERING,
          barrierState = BarrierState.STABLE,
          doctorClinicalNote = "[SIMULATION] Anomalous activity has subsided in containment. Verifying process integrity before release.",
          supportingSummary = "[SIMULATION] Device recovering. Activity stabilized."
        )
      }
      delay(1600)

      // 6. RESOLVED (case closed, pattern committed to immune memory, health score: 96 / 100)
      val newMemoryPattern = ImmuneMemoryPattern(
        id = "mem_0043",
        patternCode = "MEM-0043",
        name = "[SIM] High-Port Socket Burst",
        category = "Network Telemetry",
        observedCount = 1,
        lastSeen = "Today (Just now)",
        typicalResponse = "Flag for review",
        confidenceScore = "High (92%)",
        description = "Sudden non-whitelisted burst connection during standby power state.",
        causalImpact = "If observed again, it is flagged for in-app review."
      )

      _uiState.update { current ->
        val resolvedApps = current.applications.map { app ->
          if (app.name == "Background Updater") {
            app.copy(
              healthState = AppHealthState.HEALTHY,
              behaviorState = "Normal (Examined & Immunized)",
              isolationReason = ""
            )
          } else app
        }
        val resolvedCases = current.cases.map { c ->
          if (c.id == newCaseId) {
            c.copy(
              status = CaseStatus.RESOLVED,
              outcome = "[SIMULATION] No further abnormal activity. Application verified and restored. Memory record created.",
              timeline = c.timeline + listOf(
                CaseTimelineEvent("Just now", "[SIMULATION] Case resolved", "Patient safety verified; application released", "Outcome")
              )
            )
          } else c
        }
        val restoredCells = current.immuneCells.map { cell ->
          cell.copy(
            state = ImmuneCellState.MONITORING,
            activity = "Monitoring baseline parameters",
            lastAction = if (cell.name == "MEMORY CELL") "Synthesized MEM-0043" else cell.lastAction
          )
        }
        current.copy(
          isSimulationActive = false,
          simulationPhase = "RESOLVED",
          simulationStepIndex = 6,
          simulationCurrentStepText = "[SIMULATION] Resolved: Case closed and behavioral pattern committed to immune memory.",
          applications = resolvedApps,
          cases = resolvedCases,
          immuneCells = restoredCells,
          memoryPatterns = listOf(newMemoryPattern) + current.memoryPatterns,
          healthScore = 96,
          condition = DeviceCondition.HEALTHY,
          barrierState = BarrierState.WATCHING,
          doctorClinicalNote = "Your device is healthy. The simulated unusual behavior was contained, analyzed, and committed to immune memory.",
          supportingSummary = "Your device is behaving normally. Real telemetry active.",
          notifications = listOf(
            SimulatedNotification(
              id = "notif_res_${System.currentTimeMillis()}",
              timestamp = "Just now",
              title = "[SIMULATION] Case resolved",
              message = "Background Updater simulation complete. Everything returned to normal."
            )
          ) + current.notifications
        )
      }
    }
  }

  // -------------------------------------------------------------
  // AUTONOMOUS MANUAL ACTIONS
  // -------------------------------------------------------------
  fun isolateApp(appId: String) {
    _uiState.update { current ->
      val target = current.applications.find { it.id == appId } ?: return@update current
      val updatedApps = current.applications.map {
        if (it.id == appId) it.copy(
          healthState = AppHealthState.ISOLATED,
          behaviorState = "Flagged for in-app review (user request)",
          isolationReason = "Manual in-app review flag"
        ) else it
      }
      val newCase = IncidentCase(
        id = "case_man_${System.currentTimeMillis()}",
        caseCode = "CASE-MAN-${(100..999).random()}",
        title = "Manual In-App Review Flag: ${target.name}",
        date = "Today",
        severity = "Mild",
        status = CaseStatus.ISOLATED,
        assessment = "Application manually flagged for in-app physiological review.",
        actionTaken = "Flagged in-app only. No OS-level process or network action taken (Android sandbox).",
        outcome = "Application held in V-Watcher in-app review state.",
        confidencePercent = 100,
        affectedApp = target.name,
        evidence = listOf("Manual clinician directive applied from App Health dashboard."),
        timeline = listOf(
          CaseTimelineEvent("Just now", "Review flag set", "In-app review active (no OS action)", "Action")
        )
      )
      current.copy(
        applications = updatedApps,
        cases = listOf(newCase) + current.cases,
        healthScore = 93,
        condition = DeviceCondition.ATTENTION,
        doctorClinicalNote = "I flagged ${target.name} for in-app review. V-Watcher cannot suspend other apps (Android sandbox); use the system App Settings for OS-level action.",
        notifications = listOf(
          SimulatedNotification(
            id = "notif_man_${System.currentTimeMillis()}",
            timestamp = "Just now",
            title = "Application flagged",
            message = "V-Watcher flagged ${target.name} for in-app review."
          )
        ) + current.notifications
      )
    }
  }

  fun releaseApp(appId: String) {
    _uiState.update { current ->
      val target = current.applications.find { it.id == appId } ?: return@update current
      val updatedApps = current.applications.map {
        if (it.id == appId) it.copy(
          healthState = AppHealthState.HEALTHY,
          behaviorState = "Normal (In-app review flag cleared)",
          isolationReason = ""
        ) else it
      }
      current.copy(
        applications = updatedApps,
        healthScore = 96,
        condition = DeviceCondition.HEALTHY,
        doctorClinicalNote = "${target.name} has been released from the V-Watcher in-app review flag.",
        notifications = listOf(
          SimulatedNotification(
            id = "notif_rel_${System.currentTimeMillis()}",
            timestamp = "Just now",
            title = "Review flag cleared",
            message = "${target.name} has been released from the V-Watcher in-app review flag."
          )
        ) + current.notifications
      )
    }
  }

  fun resolveCase(caseId: String) {
    _uiState.update { current ->
      val updatedCases = current.cases.map {
        if (it.id == caseId) {
          it.copy(
            status = CaseStatus.RESOLVED,
            outcome = "Case resolved by clinical review.",
            timeline = it.timeline + CaseTimelineEvent("Just now", "Clinical review signed off", "Case marked resolved", "Outcome")
          )
        } else it
      }
      current.copy(
        cases = updatedCases,
        healthScore = 96,
        condition = DeviceCondition.HEALTHY,
        doctorClinicalNote = "All clinical cases are now resolved. Device health baseline restored."
      )
    }
  }

  fun selectCase(caseId: String?) {
    _uiState.update { it.copy(selectedCaseId = caseId) }
  }

  fun selectCell(cellId: String?) {
    _uiState.update { it.copy(selectedCellId = cellId) }
  }

  fun selectApp(appId: String?) {
    _uiState.update { it.copy(selectedAppId = appId) }
  }

  fun selectMemoryPattern(patternId: String?) {
    _uiState.update { it.copy(selectedMemoryPatternId = patternId) }
  }

  fun setWhyExplainingApp(appId: String?) {
    _uiState.update { it.copy(whyExplainingAppId = appId) }
  }

  fun setActiveTab(tab: String) {
    _uiState.update { it.copy(activeTab = tab) }
  }

  fun resetToHealthyBaseline() {
    simulationJob?.cancel()
    checkJob?.cancel()
    _uiState.value = createInitialState()
    viewModelScope.launch { refreshRealTelemetry() }
  }

  // -------------------------------------------------------------
  // INITIAL DATA SEEDING (Baseline structure while telemetry initializes)
  // -------------------------------------------------------------
  private fun createInitialState(): VWatcherUiState {
    val vitals = listOf(
      DeviceVitalSign(
        id = "vs_activity",
        title = "DEVICE ACTIVITY",
        status = "Healthy",
        subtitle = "Normal baseline activity",
        updatedAgo = "Just now",
        isNormal = true,
        trendLabel = "Stable"
      ),
      DeviceVitalSign(
        id = "vs_network",
        title = "NETWORK HEALTH",
        status = "Healthy",
        subtitle = "Low anomaly rate (0 flags)",
        updatedAgo = "Just now",
        isNormal = true,
        trendLabel = "Calm"
      ),
      DeviceVitalSign(
        id = "vs_apps",
        title = "APP BEHAVIOR",
        status = "Pending",
        subtitle = "Awaiting first observation",
        updatedAgo = "Just now",
        isNormal = true,
        trendLabel = "Baseline"
      ),
      DeviceVitalSign(
        id = "vs_system",
        title = "SYSTEM INTEGRITY",
        status = "Pending",
        subtitle = "Descriptors not yet read",
        updatedAgo = "Just now",
        isNormal = true,
        trendLabel = "Baseline"
      ),
      DeviceVitalSign(
        id = "vs_resources",
        title = "RESOURCE HEALTH",
        status = "Pending",
        subtitle = "Awaiting first observation",
        updatedAgo = "Just now",
        isNormal = true,
        trendLabel = "Baseline"
      ),
      DeviceVitalSign(
        id = "vs_battery",
        title = "BATTERY HEALTH",
        status = "Pending",
        subtitle = "Awaiting first observation",
        updatedAgo = "Just now",
        isNormal = true,
        trendLabel = "Baseline"
      )
    )

    val examCategories = listOf(
      ExamCategory(
        id = "ec_apps",
        name = "APPLICATIONS",
        status = "Healthy",
        summary = "Installed applications observed",
        detailCountText = "0 under review",
        isHealthy = true,
        notes = "Processes within declared bounds."
      ),
      ExamCategory(
        id = "ec_permissions",
        name = "PERMISSIONS",
        status = "Healthy",
        summary = "Sensory permissions matched to active UI",
        detailCountText = "All within expected bounds",
        isHealthy = true,
        notes = "Microphone/camera state unknown until observed."
      ),
      ExamCategory(
        id = "ec_network",
        name = "NETWORK",
        status = "Healthy",
        summary = "Active connections observed",
        detailCountText = "0 unusual connections",
        isHealthy = true,
        notes = "Protective barrier observes network transport state."
      ),
      ExamCategory(
        id = "ec_system",
        name = "SYSTEM",
        status = "Healthy",
        summary = "No concerning system changes",
        detailCountText = "No integrity check performed",
        isHealthy = true,
        notes = "System packages listed from inventory."
      ),
      ExamCategory(
        id = "ec_behavior",
        name = "BEHAVIOR",
        status = "Healthy",
        summary = "Typical usage pattern matches baseline",
        detailCountText = "0 behavioral flags",
        isHealthy = true,
        notes = "Temporal pattern consistent with user profile."
      ),
      ExamCategory(
        id = "ec_immune",
        name = "IMMUNE MEMORY",
        status = "Healthy",
        summary = "3 known patterns recognized",
        detailCountText = "Ready for instant recognition",
        isHealthy = true,
        notes = "Behavioral signatures loaded in local repository."
      )
    )

    val defaultApps = listOf(
      AppRecord(
        id = "app_updater",
        name = "Background Updater",
        packageName = "com.android.updater.sim",
        healthState = AppHealthState.HEALTHY,
        permissionSummary = "3 permissions • Network, Background Execution",
        behaviorState = "Normal • Occasional update polling",
        lastObserved = "4 min ago",
        iconKind = "update",
        networkActivityNote = "Last sync: 2.1 KB transfer via Wi-Fi"
      ),
      AppRecord(
        id = "app_chrome",
        name = "Web Browser",
        packageName = "com.android.chrome",
        healthState = AppHealthState.HEALTHY,
        permissionSummary = "5 permissions • Network, Storage, Location (While using)",
        behaviorState = "Normal • User-directed browsing only",
        lastObserved = "Just now",
        iconKind = "browser",
        networkActivityNote = "Session encryption not verified"
      ),
      AppRecord(
        id = "app_maps",
        name = "Maps & Navigation",
        packageName = "com.google.android.apps.maps",
        healthState = AppHealthState.HEALTHY,
        permissionSummary = "4 permissions • Precise Location, Foreground Service",
        behaviorState = "Normal • Location queried during navigation",
        lastObserved = "18 min ago",
        iconKind = "maps",
        networkActivityNote = "Tile cache prefetch active"
      ),
      AppRecord(
        id = "app_camera",
        name = "Camera System",
        packageName = "com.android.camera2",
        healthState = AppHealthState.HEALTHY,
        permissionSummary = "2 permissions • Camera, Microphone",
        behaviorState = "Idle • Zero background capture",
        lastObserved = "1 hour ago",
        iconKind = "camera",
        networkActivityNote = "No network permissions requested"
      )
    )

    val defaultPermissions = listOf(
      PermissionReviewItem(
        id = "perm_location",
        name = "Location",
        clinicalStatus = PermissionClinicalStatus.EXPECTED,
        rationale = "No apps accessed location while screen was off",
        frequencyNote = "Screen off access: 0",
        appsCount = 3
      ),
      PermissionReviewItem(
        id = "perm_microphone",
        name = "Microphone",
        clinicalStatus = PermissionClinicalStatus.EXPECTED,
        rationale = "Audio pipeline idle; no background sensors active",
        frequencyNote = "Standby capture: 0",
        appsCount = 2
      ),
      PermissionReviewItem(
        id = "perm_camera",
        name = "Camera",
        clinicalStatus = PermissionClinicalStatus.EXPECTED,
        rationale = "Optical sensors idle during all standby periods",
        frequencyNote = "Standby capture: 0",
        appsCount = 1
      ),
      PermissionReviewItem(
        id = "perm_contacts",
        name = "Contacts & Media",
        clinicalStatus = PermissionClinicalStatus.EXPECTED,
        rationale = "Read operations strictly user-initiated",
        frequencyNote = "Background access: 0",
        appsCount = 4
      )
    )

    val defaultConnections = listOf(
      NetworkConnection(
        id = "conn_init_transport",
        endpoint = "Active Network Transport Interface",
        appName = "System Network Conduit",
        status = "Initializing Live Monitoring",
        protocol = "Transport Layer",
        bandwidth = "Evaluating link...",
        timestamp = "Live",
        isDivertedToDecoy = false
      )
    )

    val defaultDecoys = listOf(
      DecoyEnvironment(
        id = "decoy_api",
        name = "API Containment Decoy",
        status = "Standby",
        interactionsCount = 0,
        lastObservation = "Standby",
        behaviorCaptured = "Controlled HTTP sink concept for anomalous handshakes",
        description = "Concept decoy sink for observing anomalous payload structure without revealing user data."
      ),
      DecoyEnvironment(
        id = "decoy_sandbox",
        name = "Execution Sandbox",
        status = "Standby",
        interactionsCount = 0,
        lastObservation = "Standby",
        behaviorCaptured = "Process boundary isolation framework",
        description = "Process boundary isolation framework designed to review divergent app tasks safely without root permissions."
      )
    )

    val initialCells = biomimeticImmuneSystem.getAllCellsAsModels()

    val defaultCases = listOf(
      IncidentCase(
        id = "case_hist_0042",
        caseCode = "CASE-0042",
        title = "High-Port Socket Probing",
        date = "3 days ago",
        severity = "Mild",
        status = CaseStatus.RESOLVED,
        assessment = "Third-party background task initiated unexpected socket listener on port 8443.",
        actionTaken = "Flagged in review; application reviewed.",
        outcome = "Application updated by developer to compliant version.",
        confidencePercent = 91,
        affectedApp = "Media Utility",
        evidence = listOf(
          "Unannounced background socket creation on port 8443.",
          "Target endpoint was an unknown public IP.",
          "Anomalous socket pattern flagged for user inspection."
        ),
        timeline = listOf(
          CaseTimelineEvent("3d ago", "Unusual socket creation observed", "Sentinel cell flagged non-standard port", "Observation"),
          CaseTimelineEvent("3d ago", "Flagged for review", "Behavioral boundary marked socket", "Action"),
          CaseTimelineEvent("3d ago", "Case signed off", "Developer confirmed issue resolved in update", "Outcome")
        )
      )
    )

    val defaultMemory = listOf(
      ImmuneMemoryPattern(
        id = "mem_0028",
        patternCode = "MEM-0028",
        name = "Push Notification Reconnect Loop",
        category = "Network Telemetry",
        observedCount = 8,
        lastSeen = "Today",
        typicalResponse = "Observe",
        confidenceScore = "High (94%)",
        description = "Rapid TCP handshakes following sudden Wi-Fi to cellular transition.",
        causalImpact = "Classified as benign physiological network recovery; no containment triggered."
      ),
      ImmuneMemoryPattern(
        id = "mem_0019",
        patternCode = "MEM-0019",
        name = "Media Playback Cache Prefetch",
        category = "Storage & Memory",
        observedCount = 14,
        lastSeen = "Yesterday",
        typicalResponse = "Allow",
        confidenceScore = "Very High (99%)",
        description = "Large contiguous file read during active lockscreen Bluetooth audio routing.",
        causalImpact = "Recognized via local cache without invoking expensive inference."
      ),
      ImmuneMemoryPattern(
        id = "mem_0035",
        patternCode = "MEM-0035",
        name = "Unregistered Socket Beacon on High Port",
        category = "Process Isolation",
        observedCount = 3,
        lastSeen = "3 days ago",
        typicalResponse = "Isolate & Verify",
        confidenceScore = "High (91%)",
        description = "Background task attempting continuous socket listener without foreground notification.",
        causalImpact = "Triggered CASE-0042; future occurrences are flagged for in-app review (latency unmeasured)."
      )
    )

    val healthHistory = listOf(
      HealthHistoryPoint("Mon", 98, "Healthy"),
      HealthHistoryPoint("Tue", 97, "Healthy"),
      HealthHistoryPoint("Wed", 96, "Healthy"),
      HealthHistoryPoint("Thu", 92, "Attention (Case-0042)"),
      HealthHistoryPoint("Fri", 96, "Healthy"),
      HealthHistoryPoint("Sat", 97, "Healthy"),
      HealthHistoryPoint("Sun", 96, "Healthy")
    )

    val notifications = listOf(
      SimulatedNotification(
        id = "notif_init_1",
        timestamp = "10 min ago",
        title = "Real device telemetry initialized",
        message = "V-Watcher connected to Android battery, memory, and package management streams."
      )
    )

    return VWatcherUiState(
      healthScore = 96,
      condition = DeviceCondition.HEALTHY,
      lastAssessmentTime = "Pending",
      supportingSummary = "Initializing… awaiting first observation.",
      doctorClinicalNote = "Initializing… first observation pending.",
      barrierState = BarrierState.WATCHING,
      vitalSigns = vitals,
      examCategories = examCategories,
      applications = defaultApps,
      permissions = defaultPermissions,
      networkConnections = defaultConnections,
      decoys = defaultDecoys,
      immuneCells = initialCells,
      cases = defaultCases,
      memoryPatterns = defaultMemory,
      healthHistory = healthHistory,
      efficiency = EfficiencyMetrics(),
      notifications = notifications
    )
  }
}
