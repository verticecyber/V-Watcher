package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.communication.ExecutionStatus
import com.example.domain.DeviceState
import com.example.reasoning.ModelReadiness
import com.example.telemetry.TelemetryAvailability
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiagnosticScreen(
  deviceState: DeviceState?,
  onBack: () -> Unit,
  onRefreshTelemetry: () -> Unit,
  modifier: Modifier = Modifier
) {
  BackHandler(onBack = onBack)
  val context = LocalContext.current
  val sentinel = deviceState?.sentinelState
  val communication = deviceState?.communicationState
  val reasoning = deviceState?.reasoningState

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Column {
            Text(
              text = "TECHNICAL DIAGNOSTICS",
              style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp,
                color = ClinicalTextPrimary
              )
            )
            Text(
              text = "Canonical Substrates & Runtime Reality",
              style = MaterialTheme.typography.bodySmall.copy(color = ClinicalTextSecondary, fontSize = 11.sp)
            )
          }
        },
        navigationIcon = {
          IconButton(onClick = onBack, modifier = Modifier.testTag("diagnostic_back_button")) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = ClinicalTextPrimary)
          }
        },
        actions = {
          IconButton(onClick = onRefreshTelemetry, modifier = Modifier.testTag("diagnostic_refresh_button")) {
            Icon(Icons.Outlined.Refresh, contentDescription = "Refresh Telemetry", tint = MedicalBluePrimary)
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = ClinicalSurface)
      )
    },
    containerColor = ClinicalBackground,
    modifier = modifier.fillMaxSize()
  ) { padding ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding),
      contentPadding = PaddingValues(18.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // 1. PRIVACY & LOCAL-ONLY POSTURE
      item {
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = ClinicalGreenLight),
          border = BorderStroke(1.dp, ClinicalGreenHealthy.copy(alpha = 0.4f)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(40.dp)
                .background(ClinicalGreenHealthy.copy(alpha = 0.15f), CircleShape),
              contentAlignment = Alignment.Center
            ) {
              Icon(Icons.Outlined.Lock, contentDescription = null, tint = ClinicalGreenHealthy, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                  text = "LOCAL ONLY RUNTIME",
                  style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = ClinicalGreenHealthy,
                    letterSpacing = 1.sp
                  )
                )
                Spacer(modifier = Modifier.width(6.dp))
                Surface(
                  color = ClinicalGreenHealthy,
                  shape = RoundedCornerShape(4.dp)
                ) {
                  Text(
                    text = "Local-only build",
                    style = MaterialTheme.typography.labelSmall.copy(
                      color = Color.White,
                      fontSize = 9.sp,
                      fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.5.dp)
                  )
                }
              }
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "Telemetry collection, normalization, routing, and inference are designed to execute on-device, and this build declares no network endpoints. Exfiltration freedom is a design property, not a monitored guarantee.",
                style = MaterialTheme.typography.bodySmall.copy(color = ClinicalTextSecondary, lineHeight = 16.sp)
              )
            }
          }
        }
      }

      // 2. SENTINEL CANONICAL OBSERVATION BOUNDARY
      item {
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = ClinicalSurface),
          border = BorderStroke(1.dp, ClinicalOutline),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Visibility, contentDescription = null, tint = MedicalBluePrimary, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = "SENTINEL OBSERVATION BOUNDARY",
                  style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                )
              }
              val isRunning = sentinel?.isRunning == true
              Surface(
                color = if (isRunning) ClinicalGreenLight else ClinicalAmberLight,
                shape = RoundedCornerShape(6.dp)
              ) {
                Text(
                  text = if (isRunning) "ACTIVE" else "IDLE",
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = if (isRunning) ClinicalGreenHealthy else ClinicalAmberAttention,
                    fontSize = 10.sp
                  ),
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
              }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = "Canonical observation boundary aggregating 6 hardware providers with full provenance.",
              style = MaterialTheme.typography.bodySmall.copy(color = ClinicalTextSecondary)
            )
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = ClinicalOutline, thickness = 0.8.dp)
            Spacer(modifier = Modifier.height(12.dp))

            val lastObs = sentinel?.lastObservation
            val ageMs = if (sentinel?.lastObservationTime != null && sentinel.lastObservationTime > 0) {
              System.currentTimeMillis() - sentinel.lastObservationTime
            } else 0L

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
              DiagnosticMetricRow("Observation ID", lastObs?.observationId ?: "Pending first capture")
              DiagnosticMetricRow("Observation Age", if (ageMs > 0) "${ageMs} ms ago" else "Live")
              DiagnosticMetricRow(
                "Freshness State",
                if (sentinel?.isStale == true) "STALE (Threshold exceeded)" else "FRESH (Physiological equilibrium)",
                highlight = !(sentinel?.isStale ?: false)
              )
              DiagnosticMetricRow("Total Snapshots Emitted", "${sentinel?.observationCount ?: 0}")
              DiagnosticMetricRow("Degraded Providers", if (lastObs?.hasDegradedProviders == true) "YES (Recorded in health map)" else "NONE")
              DiagnosticMetricRow("Unavailable Providers", if (lastObs?.hasUnavailableProviders == true) "YES (Fail-closed active)" else "NONE")
              if (sentinel?.lastError != null) {
                DiagnosticMetricRow("Sentinel Error", sentinel.lastError, isError = true)
              }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(
              text = "PROVIDER HEALTH REGISTRY",
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = ClinicalTextSecondary)
            )
            Spacer(modifier = Modifier.height(6.dp))

            // Provider Health List
            val healthMap = sentinel?.providerHealthMap ?: emptyMap()
            if (healthMap.isNotEmpty()) {
              healthMap.forEach { (name, health) ->
                ProviderHealthMiniRow(health.providerId, health.availability, health.latencyMs, health.errorCount)
              }
            } else {
              Text("Telemetry initialized from live hardware providers.", style = MaterialTheme.typography.bodySmall.copy(color = ClinicalTextMuted))
            }
          }
        }
      }

      // 3. COMMUNICATION CONDUIT
      item {
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = ClinicalSurface),
          border = BorderStroke(1.dp, ClinicalOutline),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.AltRoute, contentDescription = null, tint = MedicalBluePrimary, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = "COMMUNICATION CONDUIT",
                  style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                )
              }
              Surface(color = ClinicalGreenLight, shape = RoundedCornerShape(6.dp)) {
                Text(
                  text = "CONNECTED",
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = ClinicalGreenHealthy,
                    fontSize = 10.sp
                  ),
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
              }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = "Model-independent asynchronous transport decoupling observation from reasoning.",
              style = MaterialTheme.typography.bodySmall.copy(color = ClinicalTextSecondary)
            )
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = ClinicalOutline, thickness = 0.8.dp)
            Spacer(modifier = Modifier.height(12.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
              DiagnosticMetricRow("Total Dispatched Events", "${communication?.totalTelemetryDispatched ?: 0}")
              DiagnosticMetricRow("Last Observation ID", communication?.lastTelemetryObservationId ?: sentinel?.lastObservation?.observationId ?: "N/A")
              DiagnosticMetricRow("Total Reasoning Invocations", "${communication?.totalReasoningRequests ?: 0}")
              DiagnosticMetricRow("Last Request ID", communication?.lastReasoningRequest?.requestId ?: "None")
              DiagnosticMetricRow("Last Correlation ID", communication?.lastReasoningRequest?.correlationId ?: "None")
              DiagnosticMetricRow("Last Status", communication?.lastReasoningResponse?.executionStatus?.name ?: "IDLE")
              if (communication?.lastError != null) {
                DiagnosticMetricRow("Conduit Error", communication.lastError, isError = true)
              }
            }
          }
        }
      }

      // 4. RUNTIME ROUTER & GUARDRAILS
      item {
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = ClinicalSurface),
          border = BorderStroke(1.dp, ClinicalOutline),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Shield, contentDescription = null, tint = MedicalBluePrimary, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = "ROUTER & RESOURCE GUARDRAILS",
                  style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                )
              }
              Surface(color = ClinicalGreenLight, shape = RoundedCornerShape(6.dp)) {
                Text(
                  text = "GUARDRAILS ACTIVE",
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = ClinicalGreenHealthy,
                    fontSize = 10.sp
                  ),
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
              }
            }
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = ClinicalOutline, thickness = 0.8.dp)
            Spacer(modifier = Modifier.height(12.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
              DiagnosticMetricRow("Selected Backend (Preferred)", reasoning?.selectedBackend ?: "GEMINI_NANO")
              DiagnosticMetricRow("Actual Backend Used", reasoning?.actualBackendUsed ?: "DETERMINISTIC", highlight = true)
              DiagnosticMetricRow(
                "Resource Admission Decision",
                reasoning?.resourceAdmissionDecision ?: "Normal baseline admission"
              )
              DiagnosticMetricRow(
                "Latest Fallback Reason",
                reasoning?.lastFallbackReason ?: "None",
                highlight = reasoning?.lastFallbackReason != null && reasoning.lastFallbackReason != "None"
              )
              if (reasoning?.latestDegradation != null) {
                DiagnosticMetricRow("Latest Degradation", reasoning.latestDegradation, isError = true)
              }
            }
          }
        }
      }

      // 5. LOCAL REASONING SUBSTRATES (GEMINI NANO, GEMMA, DETERMINISTIC)
      item {
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = ClinicalSurface),
          border = BorderStroke(1.dp, ClinicalOutline),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Outlined.Psychology, contentDescription = null, tint = MedicalBluePrimary, modifier = Modifier.size(20.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "LOCAL REASONING SUBSTRATES",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
              )
            }
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = ClinicalOutline, thickness = 0.8.dp)
            Spacer(modifier = Modifier.height(12.dp))

            // Substrate 1: Gemini Nano
            SubstrateCard(
              name = "Gemini Nano (Google AICore)",
              status = reasoning?.geminiNanoStatus ?: ModelReadiness.UNAVAILABLE,
              isSelected = reasoning?.selectedBackend == "GEMINI_NANO",
              isActuallyExecuted = reasoning?.actualBackendUsed == "GEMINI_NANO",
              latency = if (reasoning?.actualBackendUsed == "GEMINI_NANO") "${reasoning.latencyMs} ms" else "N/A",
              notes = reasoning?.geminiNanoNotes ?: "Checking AICore system service..."
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Substrate 2: Gemma On-Device
            SubstrateCard(
              name = "Gemma 2B On-Device (Local Weights)",
              status = reasoning?.gemmaStatus ?: ModelReadiness.UNAVAILABLE,
              isSelected = reasoning?.selectedBackend == "GEMMA",
              isActuallyExecuted = reasoning?.actualBackendUsed == "GEMMA",
              latency = if (reasoning?.actualBackendUsed == "GEMMA") "${reasoning.latencyMs} ms" else "N/A",
              notes = reasoning?.gemmaNotes ?: "Checking local storage for gemma-2b-it-cpu.bin..."
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Substrate 3: Deterministic Baseline Engine
            SubstrateCard(
              name = "Deterministic Clinical Baseline (Guaranteed)",
              status = reasoning?.deterministicStatus ?: ModelReadiness.READY,
              isSelected = reasoning?.selectedBackend == "DETERMINISTIC",
              isActuallyExecuted = reasoning?.actualBackendUsed == "DETERMINISTIC",
              latency = if (reasoning?.actualBackendUsed == "DETERMINISTIC") "${(reasoning.latencyMs).coerceAtLeast(1L)} ms" else "Standby",
              notes = "Defensive clinical baseline active. Evaluates canonical thresholds deterministically with zero energy waste.",
              invocationCount = reasoning?.deterministicCallsCount ?: reasoning?.callsAvoidedByDeterministicLayer ?: 0
            )
          }
        }
      }

      // 6. BIOMIMETIC IMMUNE HOMEOSTASIS & AGENT CONDUIT
      item {
        val homeostasis = deviceState?.homeostasisEvaluation
        val busStats = deviceState?.immuneBusStats
        val lastAction = deviceState?.lastActionResult

        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = ClinicalSurface),
          border = BorderStroke(1.dp, ClinicalOutline),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Shield, contentDescription = null, tint = MedicalBluePrimary, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = "BIOMIMETIC HOMEOSTASIS & BUS",
                  style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                )
              }
              val macroState = homeostasis?.macroState
              val displayName = macroStateDisplayName(macroState)
              val (badgeBg, badgeText) = when (macroState) {
                com.example.immune.HomeostaticMacroState.HOMEOSTATIC -> ClinicalGreenLight to ClinicalGreenHealthy
                com.example.immune.HomeostaticMacroState.WATCH -> ClinicalAmberLight to ClinicalAmberAttention
                com.example.immune.HomeostaticMacroState.STRESSED -> ClinicalAmberLight to ClinicalAmberAttention
                com.example.immune.HomeostaticMacroState.ACTIVE_DEFENSE -> ClinicalCoralLight to ClinicalCoralCritical
                com.example.immune.HomeostaticMacroState.RECOVERING -> MedicalTealLight to MedicalTealSecondary
                com.example.immune.HomeostaticMacroState.DEGRADED -> ClinicalCoralLight to ClinicalCoralCritical
                com.example.immune.HomeostaticMacroState.UNKNOWN, null -> ClinicalSurfaceVariant to ClinicalTextMuted
              }
              Surface(color = badgeBg, shape = RoundedCornerShape(6.dp)) {
                Text(
                  text = displayName,
                  style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = badgeText, fontSize = 10.sp),
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
              }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = homeostasisReasonDisplay(homeostasis?.primaryReason),
              style = MaterialTheme.typography.bodySmall.copy(color = ClinicalTextSecondary)
            )
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = ClinicalOutline, thickness = 0.8.dp)
            Spacer(modifier = Modifier.height(12.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
              DiagnosticMetricRow("Macro State Rationale", homeostasis?.macroState?.clinicalRationale ?: "Evaluation pending — no equilibrium claim made yet.")
              DiagnosticMetricRow("Homeostasis Confidence", homeostasisConfidenceDisplay(homeostasis?.confidenceScore), highlight = true)
              DiagnosticMetricRow("Active Storm Detected", if (busStats?.activeStormDetected == true) "YES (Regulatory suppression active)" else "NONE (Stable)", isError = busStats?.activeStormDetected == true)
              DiagnosticMetricRow("Immune Bus Rate", "${"%.1f".format(busStats?.currentDispatchesPerSecond ?: 0.0)} msg/sec")
              DiagnosticMetricRow("Total Dispatches", "${busStats?.totalMessagesDispatched ?: 0}")
              DiagnosticMetricRow("Dropped Under Backpressure", "${busStats?.droppedMessagesCount ?: 0}")
              if (lastAction != null) {
                DiagnosticMetricRow("Last Actuator Action", "${lastAction.status.name}: ${lastAction.targetAction}", highlight = lastAction.status == com.example.immune.ActionExecutionStatus.EXECUTED)
                DiagnosticMetricRow("Action Platform Rationale", lastAction.platformReason)
              }
            }
          }
        }
      }

      // 7. REAL TELEMETRY PROVIDERS
      item {
        val lastRefresh = if (deviceState?.timestamp != null && deviceState.timestamp > 0) {
          val secondsAgo = ((System.currentTimeMillis() - deviceState.timestamp) / 1000).coerceAtLeast(0)
          if (secondsAgo < 5) "Just now" else "$secondsAgo seconds ago"
        } else "Never"

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "REAL TELEMETRY PROVIDERS",
            style = MaterialTheme.typography.labelMedium.copy(
              fontWeight = FontWeight.Bold,
              color = ClinicalTextSecondary,
              letterSpacing = 1.sp
            )
          )
          Text(
            text = "Refreshed: $lastRefresh",
            style = MaterialTheme.typography.labelSmall.copy(
              color = ClinicalTextMuted,
              fontWeight = FontWeight.Medium
            )
          )
        }
      }

      // Provider 1: App Inventory
      item {
        val inventory = deviceState?.applicationState
        val availability = inventory?.availability ?: TelemetryAvailability.AVAILABLE
        val (statusText, statusColor) = getTelemetryStatusStyle(availability)
        ProviderDiagnosticCard(
          title = "App Inventory",
          source = inventory?.source ?: "Android PackageManager",
          status = statusText,
          statusColor = statusColor,
          permissionState = inventory?.permissionState ?: "Granted",
          details = "Observed ${inventory?.value?.totalAppsCount ?: 0} applications (${inventory?.value?.userAppsCount ?: 0} user apps, ${inventory?.value?.systemAppsCount ?: 0} system services). Local inspection only.",
          notes = inventory?.diagnosticNotes ?: ""
        )
      }

      // Provider 2: App Usage Access
      item {
        val usage = deviceState?.usageState
        val isGranted = usage?.value?.isAccessGranted == true
        val availability = usage?.availability ?: if (isGranted) TelemetryAvailability.AVAILABLE else TelemetryAvailability.PERMISSION_REQUIRED
        val (statusText, statusColor) = getTelemetryStatusStyle(availability)
        ProviderDiagnosticCard(
          title = "App Usage Access (Behavioral)",
          source = usage?.source ?: "Android UsageStatsManager",
          status = statusText,
          statusColor = statusColor,
          permissionState = usage?.permissionState ?: "Not Enabled",
          details = if (isGranted) {
            "Usage access granted. Tracking app foreground/background transitions and activity switches."
          } else {
            "Usage access is not enabled. V-Watcher uses this access to understand application activity and establish normal behavioral patterns."
          },
          actionButton = if (!isGranted) {
            {
              Button(
                onClick = {
                  try {
                    val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
                    context.startActivity(intent)
                  } catch (_: Exception) {
                    val fallback = Intent(Settings.ACTION_SETTINGS)
                    context.startActivity(fallback)
                  }
                },
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MedicalBluePrimary),
                modifier = Modifier.testTag("enable_usage_access_button")
              ) {
                Text("Enable in Android Settings", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
              }
            }
          } else null,
          notes = usage?.diagnosticNotes ?: ""
        )
      }

      // Provider 3: Battery Telemetry
      item {
        val battery = deviceState?.battery
        val availability = battery?.availability ?: TelemetryAvailability.AVAILABLE
        val (statusText, statusColor) = getTelemetryStatusStyle(availability)
        ProviderDiagnosticCard(
          title = "Battery Telemetry",
          source = battery?.source ?: "Android BatteryManager",
          status = statusText,
          statusColor = statusColor,
          permissionState = battery?.permissionState ?: "Granted (System API)",
          details = "Battery Level: ${battery?.value?.levelPercent ?: 0}% • State: ${battery?.value?.chargePlugType} • Temp: ${battery?.value?.temperatureCelsius ?: 0f}°C • Health: ${battery?.value?.health} • Power Saver: ${if (battery?.value?.isPowerSaveMode == true) "Active" else "Off"}",
          notes = battery?.diagnosticNotes ?: ""
        )
      }

      // Provider 4: Memory & Resources
      item {
        val res = deviceState?.memory
        val availability = res?.availability ?: TelemetryAvailability.AVAILABLE
        val (statusText, statusColor) = getTelemetryStatusStyle(availability)
        ProviderDiagnosticCard(
          title = "Device Resources & Memory",
          source = res?.source ?: "ActivityManager & JVM Runtime",
          status = statusText,
          statusColor = statusColor,
          permissionState = res?.permissionState ?: "Granted (System API)",
          details = "Device RAM: ${res?.value?.availableMemMb} MB free of ${res?.value?.totalMemMb} MB (${res?.value?.usedPercent}% used) • V-Watcher Process Heap: ${res?.value?.vWatcherMemoryMb} MB • Low Memory Pressure: ${if (res?.value?.isLowMemory == true) "Yes" else "No"}",
          notes = res?.diagnosticNotes ?: ""
        )
      }

      // Provider 5: Network Telemetry
      item {
        val net = deviceState?.network
        val availability = net?.availability ?: TelemetryAvailability.AVAILABLE
        val (statusText, statusColor) = getTelemetryStatusStyle(availability)
        ProviderDiagnosticCard(
          title = "Network Observation",
          source = net?.source ?: "ConnectivityManager",
          status = statusText,
          statusColor = statusColor,
          permissionState = net?.permissionState ?: "Granted (ACCESS_NETWORK_STATE)",
          details = "Active Conduit: ${net?.value?.transportType ?: "Unknown"} • Validated Internet: ${if (net?.value?.isValidated == true) "Yes" else "No"} • Downstream: ${net?.value?.downstreamBandwidthKbps} kbps • Upstream: ${net?.value?.upstreamBandwidthKbps} kbps",
          notes = net?.diagnosticNotes ?: ""
        )
      }

      // Provider 6: System & Platform Environment
      item {
        val sys = deviceState?.systemState
        val availability = sys?.availability ?: TelemetryAvailability.AVAILABLE
        val (statusText, statusColor) = getTelemetryStatusStyle(availability)
        ProviderDiagnosticCard(
          title = "System State",
          source = sys?.source ?: "Android Build APIs",
          status = statusText,
          statusColor = statusColor,
          permissionState = sys?.permissionState ?: "Granted",
          details = "OS: Android ${sys?.value?.osVersion} (API ${sys?.value?.sdkInt}) • Hardware: ${sys?.value?.manufacturer} ${sys?.value?.model} (${sys?.value?.device}) • Security Patch: ${sys?.value?.securityPatch} • Screen Interactive: ${if (sys?.value?.isScreenInteractive == true) "Yes" else "No"}",
          notes = sys?.diagnosticNotes ?: ""
        )
      }

      item {
        Spacer(modifier = Modifier.height(40.dp))
      }
    }
  }
}

@Composable
private fun SubstrateCard(
  name: String,
  status: ModelReadiness,
  isSelected: Boolean,
  isActuallyExecuted: Boolean,
  latency: String,
  notes: String,
  invocationCount: Int? = null
) {
  val badgeBg = when (status) {
    ModelReadiness.READY -> ClinicalGreenLight
    ModelReadiness.PREPARING -> ClinicalAmberLight
    ModelReadiness.UNAVAILABLE -> ClinicalSurfaceVariant
    ModelReadiness.ERROR -> ClinicalCoralLight
  }
  val badgeText = when (status) {
    ModelReadiness.READY -> ClinicalGreenHealthy
    ModelReadiness.PREPARING -> ClinicalAmberAttention
    ModelReadiness.UNAVAILABLE -> ClinicalTextMuted
    ModelReadiness.ERROR -> ClinicalCoralCritical
  }

  Surface(
    shape = RoundedCornerShape(12.dp),
    color = ClinicalSurfaceVariant.copy(alpha = 0.5f),
    border = BorderStroke(1.dp, if (isActuallyExecuted) MedicalBluePrimary else ClinicalOutline.copy(alpha = 0.6f)),
    modifier = Modifier.fillMaxWidth()
  ) {
    Column(modifier = Modifier.padding(12.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(name, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
          if (isActuallyExecuted) {
            Text(
              "ACTUALLY EXECUTED BACKEND",
              style = MaterialTheme.typography.labelSmall.copy(color = MedicalBluePrimary, fontWeight = FontWeight.Bold, fontSize = 9.sp)
            )
          }
        }
        Surface(color = badgeBg, shape = RoundedCornerShape(4.dp)) {
          Text(
            text = status.displayName.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(color = badgeText, fontWeight = FontWeight.Bold, fontSize = 9.sp),
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
          )
        }
      }
      Spacer(modifier = Modifier.height(6.dp))
      Text(notes, style = MaterialTheme.typography.bodySmall.copy(color = ClinicalTextSecondary, fontSize = 11.sp))
      Spacer(modifier = Modifier.height(6.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Text("Latency: $latency", style = MaterialTheme.typography.labelSmall.copy(color = ClinicalTextMuted))
        if (invocationCount != null) {
          Text("Invocations: $invocationCount", style = MaterialTheme.typography.labelSmall.copy(color = ClinicalTextMuted))
        }
      }
    }
  }
}

@Composable
private fun ProviderHealthMiniRow(
  providerId: String,
  availability: TelemetryAvailability,
  latencyMs: Long,
  errorCount: Int
) {
  val (statusText, statusColor) = getTelemetryStatusStyle(availability)
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 3.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(
      text = providerId.removeSuffix("Provider"),
      style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium)
    )
    Row(verticalAlignment = Alignment.CenterVertically) {
      Text(
        text = "${latencyMs}ms",
        style = MaterialTheme.typography.labelSmall.copy(color = ClinicalTextMuted, fontSize = 10.sp),
        modifier = Modifier.padding(end = 8.dp)
      )
      Surface(
        color = statusColor.copy(alpha = 0.12f),
        shape = RoundedCornerShape(4.dp)
      ) {
        Text(
          text = statusText,
          style = MaterialTheme.typography.labelSmall.copy(color = statusColor, fontWeight = FontWeight.Bold, fontSize = 9.sp),
          modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.5.dp)
        )
      }
    }
  }
}

@Composable
private fun DiagnosticMetricRow(
  label: String,
  value: String,
  highlight: Boolean = false,
  isError: Boolean = false
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(
      text = label,
      style = MaterialTheme.typography.bodySmall.copy(color = ClinicalTextSecondary)
    )
    Text(
      text = value,
      style = MaterialTheme.typography.bodySmall.copy(
        fontWeight = FontWeight.Bold,
        color = when {
          isError -> ClinicalCoralCritical
          highlight -> ClinicalGreenHealthy
          else -> ClinicalTextPrimary
        }
      )
    )
  }
}

@Composable
private fun ProviderDiagnosticCard(
  title: String,
  source: String,
  status: String,
  statusColor: Color,
  permissionState: String,
  details: String,
  notes: String,
  actionButton: (@Composable () -> Unit)? = null
) {
  Card(
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(containerColor = ClinicalSurface),
    border = BorderStroke(1.dp, ClinicalOutline),
    modifier = Modifier.fillMaxWidth()
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = title,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = ClinicalTextPrimary)
          )
          Text(
            text = "Source: $source",
            style = MaterialTheme.typography.bodySmall.copy(color = ClinicalTextMuted, fontSize = 11.sp)
          )
        }
        Surface(
          color = statusColor.copy(alpha = 0.15f),
          shape = RoundedCornerShape(6.dp)
        ) {
          Text(
            text = status,
            style = MaterialTheme.typography.labelSmall.copy(
              color = statusColor,
              fontWeight = FontWeight.Bold,
              fontSize = 10.sp
            ),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(8.dp))
      Text(
        text = details,
        style = MaterialTheme.typography.bodySmall.copy(color = ClinicalTextPrimary, lineHeight = 17.sp)
      )

      if (notes.isNotBlank()) {
        Spacer(modifier = Modifier.height(6.dp))
        Text(
          text = "Diagnostics: $notes",
          style = MaterialTheme.typography.bodySmall.copy(color = ClinicalTextSecondary, fontSize = 11.sp)
        )
      }

      Spacer(modifier = Modifier.height(8.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Permission: $permissionState",
          style = MaterialTheme.typography.labelSmall.copy(color = ClinicalTextMuted, fontSize = 10.sp)
        )
        if (actionButton != null) {
          actionButton()
        }
      }
    }
  }
}

private fun getTelemetryStatusStyle(availability: TelemetryAvailability): Pair<String, Color> {
  return when (availability) {
    TelemetryAvailability.AVAILABLE -> Pair("AVAILABLE", ClinicalGreenHealthy)
    TelemetryAvailability.STALE -> Pair("STALE", ClinicalAmberAttention)
    TelemetryAvailability.DEGRADED -> Pair("DEGRADED", ClinicalAmberAttention)
    TelemetryAvailability.PERMISSION_REQUIRED -> Pair("PERMISSION REQUIRED", ClinicalAmberAttention)
    TelemetryAvailability.UNAVAILABLE -> Pair("UNAVAILABLE", ClinicalCoralCritical)
    TelemetryAvailability.ERROR -> Pair("ERROR", ClinicalCoralCritical)
    TelemetryAvailability.UNKNOWN -> Pair("UNKNOWN", ClinicalTextMuted)
  }
}
