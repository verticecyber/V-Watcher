package com.example.ui.screens

import android.content.Intent
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.reasoning.ModelReadiness
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.VWatcherUiState
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
  uiState: VWatcherUiState,
  onRunCheck: () -> Unit,
  onSimulateAnomaly: () -> Unit,
  onNavigateToTab: (String) -> Unit,
  onOpenNotifications: () -> Unit,
  onOpenDiagnostics: () -> Unit = {},
  onRefreshTelemetry: () -> Unit = {},
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  var selectedCellForDetail by remember { mutableStateOf<ImmuneCell?>(null) }
  val listState = rememberLazyListState()
  val coroutineScope = rememberCoroutineScope()
  var selectedFilter by remember { mutableStateOf("ALL") }

  // Polished Android-Native Detail Surface for Immune Cells
  if (selectedCellForDetail != null) {
    ImmuneCellDetailBottomSheet(
      cell = selectedCellForDetail!!,
      onDismiss = { selectedCellForDetail = null }
    )
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(WhoopBlackBg)
  ) {
    // 1. PINNED APP HEADER (Native Android App Identity, stays anchored at top)
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .background(WhoopBlackBg)
        .statusBarsPadding()
    ) {
      ClinicalTopBar(
        onNotificationsClick = onOpenNotifications,
        notificationCount = uiState.notifications.size,
        onDiagnosticsClick = onOpenDiagnostics,
        onRefreshTelemetry = onRefreshTelemetry
      )

      // 2. LIVE BIO-SIGNAL ECG RADAR STRIP (Visual feedback that app is actively listening)
      BioSignalPulseStrip(
        isObserving = uiState.deviceState != null,
        condition = uiState.condition,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
      )
    }

    // 3. SCROLLABLE CLINICAL CONTENT
    LazyColumn(
      state = listState,
      modifier = Modifier.fillMaxSize(),
      contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      // Apple / WHOOP Minimalist Navigation Bar (Clean Typography + Underline Active Indicator)
      item {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            val filterTabs = listOf(
              "ALL" to "Visão Geral",
              "CORE" to "Bio-Núcleo",
              "COMMAND" to "Comando",
              "VITALS" to "Sinais Vitais",
              "BARRIER" to "Barreira",
              "ENGINE" to "Motor Local"
            )
            filterTabs.forEach { (id, label) ->
              val isSelected = selectedFilter == id
              Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                  .clickable {
                    selectedFilter = id
                    coroutineScope.launch {
                      when (id) {
                        "ALL" -> listState.animateScrollToItem(0)
                        "CORE" -> listState.animateScrollToItem(2)
                        "COMMAND" -> listState.animateScrollToItem(3)
                        "VITALS" -> listState.animateScrollToItem(5)
                        "BARRIER" -> listState.animateScrollToItem(6)
                        "ENGINE" -> listState.animateScrollToItem(7)
                      }
                    }
                  }
                  .padding(top = 4.dp, bottom = 2.dp)
              ) {
                Text(
                  text = label,
                  style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) WhoopTextPrimary else WhoopTextMuted,
                    fontSize = 14.sp,
                    letterSpacing = (-0.2).sp
                  )
                )
                Spacer(modifier = Modifier.height(6.dp))
                // Clean Apple-style indicator bar
                Box(
                  modifier = Modifier
                    .width(if (isSelected) 24.dp else 0.dp)
                    .height(2.5.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(if (isSelected) WhoopTextPrimary else Color.Transparent)
                )
              }
            }
          }
          // Subtle hairline divider underneath the tabs
          HorizontalDivider(
            thickness = 0.5.dp,
            color = Color(0x1FFFFFFF),
            modifier = Modifier.padding(top = 2.dp)
          )
        }
      }

      // 1b. Real Telemetry & Intelligence Status Bar (Apple Precision Biometric Strip)
      item {
        Card(
          shape = RoundedCornerShape(18.dp),
          colors = CardDefaults.cardColors(containerColor = WhoopSurface),
          border = BorderStroke(1.dp, Color(0x28FFFFFF)),
          elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
          modifier = Modifier
            .fillMaxWidth()
            .shadow(
              elevation = 6.dp,
              shape = RoundedCornerShape(18.dp),
              ambientColor = Color.Black.copy(alpha = 0.6f),
              spotColor = Color.Black.copy(alpha = 0.6f)
            )
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .background(
                Brush.verticalGradient(
                  colors = listOf(
                    Color(0xFF181C26),
                    Color(0xFF11141A)
                  )
                )
              )
              .padding(horizontal = 14.dp, vertical = 12.dp)
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              // Left side: Telemetry pills
              Row(
                modifier = Modifier
                  .weight(1f)
                  .horizontalScroll(rememberScrollState()),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                if (deviceDataBadgeVisible(uiState.deviceState != null)) {
                  Surface(
                    color = WhoopRecoveryGreen.copy(alpha = 0.14f),
                    border = BorderStroke(1.dp, WhoopRecoveryGreen.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(8.dp)
                  ) {
                    Row(
                      verticalAlignment = Alignment.CenterVertically,
                      modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                      Box(
                        modifier = Modifier
                          .size(5.dp)
                          .clip(CircleShape)
                          .background(WhoopRecoveryGreen)
                      )
                      Spacer(modifier = Modifier.width(5.dp))
                      Text(
                        text = "REAL TELEMETRY",
                        style = MaterialTheme.typography.labelSmall.copy(
                          fontWeight = FontWeight.Bold,
                          color = WhoopRecoveryGreen,
                          fontSize = 9.sp,
                          letterSpacing = 0.5.sp
                        )
                      )
                    }
                  }
                }
                val backendUsed = uiState.deviceState?.reasoningState?.actualBackendUsed
                val isModelReady = modelReadinessIsReady(uiState.modelReadiness)
                val modelBadgeLabel = modelReadinessDisplayName(uiState.modelReadiness, backendUsed)
                Surface(
                  color = if (isModelReady) WhoopStrainBlue.copy(alpha = 0.14f) else WhoopSurfaceElevated,
                  border = BorderStroke(
                    1.dp,
                    if (isModelReady) WhoopStrainBlue.copy(alpha = 0.4f) else Color(0x22FFFFFF)
                  ),
                  shape = RoundedCornerShape(8.dp)
                ) {
                  Text(
                    text = modelBadgeLabel,
                    style = MaterialTheme.typography.labelSmall.copy(
                      fontWeight = FontWeight.Bold,
                      color = if (isModelReady) WhoopStrainBlue else WhoopTextMuted,
                      fontSize = 9.sp,
                      letterSpacing = 0.4.sp
                    ),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                  )
                }
                val macroState = uiState.homeostasisEvaluation?.macroState
                val macroLabel = macroStateDisplayName(uiState.homeostasisEvaluation?.macroState)
                val isMacroHomeostatic = macroState == com.example.immune.HomeostaticMacroState.HOMEOSTATIC
                val isMacroNeutral = macroState == null || macroState == com.example.immune.HomeostaticMacroState.UNKNOWN
                val macroBg = when {
                  isMacroHomeostatic -> WhoopRecoveryGreen.copy(alpha = 0.14f)
                  isMacroNeutral -> WhoopSurfaceElevated
                  macroState == com.example.immune.HomeostaticMacroState.RECOVERING -> WhoopSleepTeal.copy(alpha = 0.14f)
                  else -> WhoopWarningAmber.copy(alpha = 0.14f)
                }
                val macroTextColor = when {
                  isMacroHomeostatic -> WhoopRecoveryGreen
                  isMacroNeutral -> WhoopTextMuted
                  macroState == com.example.immune.HomeostaticMacroState.RECOVERING -> WhoopSleepTeal
                  else -> WhoopWarningAmber
                }
                Surface(
                  color = macroBg,
                  border = BorderStroke(1.dp, macroTextColor.copy(alpha = 0.4f)),
                  shape = RoundedCornerShape(8.dp)
                ) {
                  Text(
                    text = macroLabel,
                    style = MaterialTheme.typography.labelSmall.copy(
                      fontWeight = FontWeight.Bold,
                      color = macroTextColor,
                      fontSize = 9.sp,
                      letterSpacing = 0.4.sp
                    ),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                  )
                }
              }

              Spacer(modifier = Modifier.width(8.dp))

              // Right side: Polished Apple-style Diagnostics Button
              Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(0x3300B4D8),
                border = BorderStroke(1.dp, WhoopStrainBlue.copy(alpha = 0.45f)),
                modifier = Modifier
                  .appTactile(0.92f)
                  .clickable { onOpenDiagnostics() }
                  .testTag("open_diagnostics_button")
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                  Icon(
                    imageVector = Icons.Outlined.Analytics,
                    contentDescription = null,
                    tint = WhoopStrainBlue,
                    modifier = Modifier.size(13.dp)
                  )
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(
                    text = "Diagnostics",
                    maxLines = 1,
                    style = MaterialTheme.typography.labelSmall.copy(
                      fontWeight = FontWeight.Bold,
                      color = WhoopStrainBlue,
                      fontSize = 11.5.sp
                    )
                  )
                  Spacer(modifier = Modifier.width(2.dp))
                  Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = WhoopStrainBlue,
                    modifier = Modifier.size(13.dp)
                  )
                }
              }
            }
          }
        }
      }

    // 1c. Usage Access Prompt Card if not enabled (Refined Apple Dark Warning Surface)
    if (uiState.isUsageAccessMissing) {
      item {
        Card(
          shape = RoundedCornerShape(18.dp),
          colors = CardDefaults.cardColors(containerColor = WhoopSurface),
          border = BorderStroke(1.dp, WhoopWarningAmber.copy(alpha = 0.4f)),
          elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
          modifier = Modifier
            .fillMaxWidth()
            .shadow(
              elevation = 6.dp,
              shape = RoundedCornerShape(18.dp),
              ambientColor = Color.Black.copy(alpha = 0.6f),
              spotColor = Color.Black.copy(alpha = 0.6f)
            )
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .background(
                Brush.verticalGradient(
                  colors = listOf(
                    Color(0xFF2A1B0B),
                    Color(0xFF18130E)
                  )
                )
              )
              .padding(16.dp)
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                  contentAlignment = Alignment.Center,
                  modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(WhoopWarningAmber.copy(alpha = 0.18f))
                    .border(1.dp, WhoopWarningAmber.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
                ) {
                  Icon(
                    imageVector = Icons.Outlined.SecurityUpdateWarning,
                    contentDescription = null,
                    tint = WhoopWarningAmber,
                    modifier = Modifier.size(19.dp)
                  )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                      text = "SECURITY ACCESS",
                      style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp,
                        color = WhoopWarningAmber,
                        fontSize = 10.sp
                      )
                    )
                  }
                  Text(
                    text = "Usage Stats Not Granted",
                    style = MaterialTheme.typography.titleSmall.copy(
                      fontWeight = FontWeight.Bold,
                      color = WhoopTextPrimary
                    )
                  )
                }
              }

              Surface(
                color = WhoopWarningAmber.copy(alpha = 0.18f),
                border = BorderStroke(1.dp, WhoopWarningAmber.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(6.dp)
              ) {
                Text(
                  text = "ACTION NEEDED",
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = WhoopWarningAmber,
                    fontSize = 8.5.sp,
                    letterSpacing = 0.5.sp
                  ),
                  modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                )
              }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
              text = "V-Watcher uses Android Usage Access to identify foreground app transitions and verify authentic behavioral patterns locally without cloud inspection.",
              style = MaterialTheme.typography.bodySmall.copy(
                color = WhoopTextSecondary,
                lineHeight = 18.sp,
                fontSize = 12.sp
              )
            )

            Spacer(modifier = Modifier.height(14.dp))
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
              shape = RoundedCornerShape(12.dp),
              colors = ButtonDefaults.buttonColors(
                containerColor = WhoopWarningAmber,
                contentColor = WhoopBlackBg
              ),
              contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
              modifier = Modifier
                .appTactile(0.94f)
                .shadow(4.dp, RoundedCornerShape(12.dp), ambientColor = WhoopWarningAmber.copy(alpha = 0.3f), spotColor = WhoopWarningAmber.copy(alpha = 0.3f))
                .testTag("enable_usage_access_home_button")
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = Icons.Outlined.Settings,
                  contentDescription = null,
                  modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = "Enable in Android Settings",
                  fontSize = 12.5.sp,
                  fontWeight = FontWeight.Black
                )
              }
            }
          }
        }
      }
    }

    // 2. Live Simulation Banner if active
    if (uiState.isSimulationActive) {
      item {
        Card(
          shape = RoundedCornerShape(18.dp),
          colors = CardDefaults.cardColors(containerColor = ClinicalAmberLight.copy(alpha = 0.85f)),
          border = BorderStroke(1.dp, ClinicalAmberAttention),
          elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              contentAlignment = Alignment.Center,
              modifier = Modifier.size(36.dp)
            ) {
              CircularProgressIndicator(
                modifier = Modifier.size(32.dp),
                strokeWidth = 3.dp,
                color = ClinicalAmberAttention
              )
              Icon(
                imageVector = Icons.Default.Biotech,
                contentDescription = null,
                tint = ClinicalAmberAttention,
                modifier = Modifier.size(16.dp)
              )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                  text = "IMMUNE REFLEX",
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = ClinicalAmberAttention,
                    letterSpacing = 1.sp
                  )
                )
                if (uiState.simulationPhase.isNotEmpty()) {
                  Spacer(modifier = Modifier.width(6.dp))
                  Surface(
                    color = ClinicalAmberAttention,
                    shape = RoundedCornerShape(4.dp)
                  ) {
                    Text(
                      text = uiState.simulationPhase,
                      style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 8.5.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                      ),
                      modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.5.dp)
                    )
                  }
                }
              }
              Spacer(modifier = Modifier.height(2.dp))
              Text(
                text = uiState.simulationCurrentStepText,
                style = MaterialTheme.typography.bodySmall.copy(
                  color = ClinicalTextPrimary,
                  fontSize = 12.5.sp,
                  fontWeight = FontWeight.Medium
                )
              )
            }
          }
        }
      }
    }

    // 3. SOVEREIGN DEFENSE CORE (Equilibrium Index + Live Cellular Network)
    item {
      SovereignDefenseCore(
        healthScore = uiState.healthScore,
        condition = uiState.condition,
        supportingSentence = uiState.supportingSummary,
        cells = uiState.immuneCells,
        barrierState = uiState.barrierState,
        isObserving = uiState.deviceState != null,
        onCellClick = { selectedCellForDetail = it },
        onViewNetworkClick = { onNavigateToTab("HEALTH") }
      )
    }

    // 4. TACTICAL DEFENSE COMMAND CONSOLE (Scan & Stress Simulation)
    item {
      TacticalDefenseCommandPanel(
        isSimulationActive = uiState.isSimulationActive,
        onRunCheck = onRunCheck,
        onSimulateAnomaly = onSimulateAnomaly
      )
    }

    // 5. CLINICAL / IMMUNOLOGICAL INTELLIGENCE SUMMARY
    item {
      DoctorClinicalSummarySection(
        clinicalAssessment = uiState.doctorClinicalNote,
        lastAssessment = uiState.lastAssessmentTime
      )
    }

    // 6. SOVEREIGN BIOMETRIC VITALS MATRIX (Unified Panel)
    item {
      SovereignVitalsPanel(
        vitals = uiState.vitalSigns
      )
    }

    // 7. Biomimetic Firewall / Protective Barrier
    item {
      ProtectiveBarrierCard(
        state = uiState.barrierState,
        onViewNetworkClick = { onNavigateToTab("HEALTH") }
      )
    }

    // 10. Local Intelligence & Efficiency Card
    item {
      Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = WhoopSurface),
        border = BorderStroke(1.dp, Color(0x28FFFFFF)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
          .fillMaxWidth()
          .shadow(
            elevation = 6.dp,
            shape = RoundedCornerShape(20.dp),
            ambientColor = Color.Black.copy(alpha = 0.6f),
            spotColor = Color.Black.copy(alpha = 0.6f)
          )
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .background(
              Brush.verticalGradient(
                colors = listOf(
                  Color(0xFF181C26),
                  Color(0xFF11141A)
                )
              )
            )
            .padding(18.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                  .size(34.dp)
                  .clip(RoundedCornerShape(10.dp))
                  .background(Color(0x3300B4D8))
                  .border(1.dp, WhoopStrainBlue.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
              ) {
                Icon(
                  imageVector = Icons.Outlined.Memory,
                  contentDescription = null,
                  tint = WhoopStrainBlue,
                  modifier = Modifier.size(19.dp)
                )
              }
              Spacer(modifier = Modifier.width(10.dp))
              Text(
                text = "LOCAL TELEMETRY ENGINE",
                style = MaterialTheme.typography.labelMedium.copy(
                  fontWeight = FontWeight.Black,
                  color = WhoopTextPrimary,
                  letterSpacing = 1.sp
                )
              )
            }
            Surface(
              color = Color(0x2822C55E),
              border = BorderStroke(1.dp, WhoopRecoveryGreen.copy(alpha = 0.35f)),
              shape = RoundedCornerShape(8.dp)
            ) {
              Text(
                text = "Active • Zero Cloud",
                style = MaterialTheme.typography.labelSmall.copy(
                  color = WhoopRecoveryGreen,
                  fontWeight = FontWeight.Bold,
                  fontSize = 10.sp
                ),
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
              )
            }
          }
          Spacer(modifier = Modifier.height(10.dp))
          Text(
            text = "Behavioral telemetry is computed within device sandbox memory. This build declares no network endpoints, so there is nowhere for identifiers or payloads to be sent.",
            style = MaterialTheme.typography.bodySmall.copy(
              color = WhoopTextSecondary,
              lineHeight = 18.sp,
              fontSize = 12.sp
            )
          )
          Spacer(modifier = Modifier.height(14.dp))
          HorizontalDivider(color = Color(0x20FFFFFF), thickness = 0.8.dp)
          Spacer(modifier = Modifier.height(12.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "Battery Drain",
                style = MaterialTheme.typography.labelSmall.copy(color = WhoopTextMuted, fontSize = 10.5.sp)
              )
              Spacer(modifier = Modifier.height(2.dp))
              Text(
                text = "Unmeasured",
                style = MaterialTheme.typography.bodySmall.copy(
                  fontWeight = FontWeight.Bold,
                  color = WhoopTextPrimary
                )
              )
            }
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "RAM Overhead",
                style = MaterialTheme.typography.labelSmall.copy(color = WhoopTextMuted, fontSize = 10.5.sp)
              )
              Spacer(modifier = Modifier.height(2.dp))
              Text(
                text = uiState.efficiency.memoryUsage.ifEmpty { "—" },
                style = MaterialTheme.typography.bodySmall.copy(
                  fontWeight = FontWeight.Bold,
                  color = WhoopTextPrimary
                )
              )
            }
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "CPU Footprint",
                style = MaterialTheme.typography.labelSmall.copy(color = WhoopTextMuted, fontSize = 10.5.sp)
              )
              Spacer(modifier = Modifier.height(2.dp))
              Text(
                text = uiState.efficiency.cpuActivity.ifEmpty { "Unmeasured" },
                style = MaterialTheme.typography.bodySmall.copy(
                  fontWeight = FontWeight.Bold,
                  color = WhoopTextPrimary
                )
              )
            }
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "Inference Mode",
                style = MaterialTheme.typography.labelSmall.copy(color = WhoopTextMuted, fontSize = 10.5.sp)
              )
              Spacer(modifier = Modifier.height(2.dp))
              Text(
                text = uiState.efficiency.inferenceActivity.ifEmpty { "—" },
                style = MaterialTheme.typography.bodySmall.copy(
                  fontWeight = FontWeight.Bold,
                  color = WhoopTextPrimary
                )
              )
            }
          }
        }
      }
    }

    item {
      Spacer(modifier = Modifier.height(80.dp))
    }
  }
}
}
