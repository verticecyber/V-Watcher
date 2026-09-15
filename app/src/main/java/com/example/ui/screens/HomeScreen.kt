package com.example.ui.screens

import android.content.Intent
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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

@Composable
fun HomeScreen(
  uiState: VWatcherUiState,
  onRunCheck: () -> Unit,
  onSimulateAnomaly: () -> Unit,
  onNavigateToTab: (String) -> Unit,
  onOpenNotifications: () -> Unit,
  onOpenDiagnostics: () -> Unit = {},
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  var selectedCellForDetail by remember { mutableStateOf<ImmuneCell?>(null) }

  // Polished Android-Native Detail Surface for Immune Cells
  if (selectedCellForDetail != null) {
    ImmuneCellDetailBottomSheet(
      cell = selectedCellForDetail!!,
      onDismiss = { selectedCellForDetail = null }
    )
  }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(ClinicalBackground),
    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // 1. Top Clinical App Bar
    item {
      ClinicalTopBar(
        onNotificationsClick = onOpenNotifications,
        notificationCount = uiState.notifications.size,
        onDiagnosticsClick = onOpenDiagnostics
      )
    }

    // 1b. Real Telemetry & Intelligence Status Bar
    item {
      Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = ClinicalSurface),
        border = BorderStroke(1.dp, ClinicalOutline),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 10.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            if (deviceDataBadgeVisible(uiState.deviceState != null)) {
              Surface(
                color = ClinicalGreenLight,
                shape = RoundedCornerShape(6.dp)
              ) {
                Text(
                  text = "REAL DEVICE DATA",
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = ClinicalGreenHealthy,
                    fontSize = 9.5.sp,
                    letterSpacing = 0.5.sp
                  ),
                  modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.5.dp)
                )
              }
            }
            Surface(
              color = if (uiState.modelReadiness == ModelReadiness.READY) ClinicalGreenLight else ClinicalSurfaceVariant,
              shape = RoundedCornerShape(6.dp)
            ) {
              Text(
                text = if (uiState.modelReadiness == ModelReadiness.READY) "AICORE READY" else "DETERMINISTIC MODE",
                style = MaterialTheme.typography.labelSmall.copy(
                  fontWeight = FontWeight.Bold,
                  color = if (uiState.modelReadiness == ModelReadiness.READY) ClinicalGreenHealthy else ClinicalTextMuted,
                  fontSize = 9.5.sp,
                  letterSpacing = 0.5.sp
                ),
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.5.dp)
              )
            }
            val macroState = uiState.homeostasisEvaluation?.macroState
            val macroLabel = macroStateDisplayName(uiState.homeostasisEvaluation?.macroState)
            val macroLive = macroState == com.example.immune.HomeostaticMacroState.HOMEOSTATIC
            Surface(
              color = if (macroLive) ClinicalGreenLight else ClinicalAmberLight,
              shape = RoundedCornerShape(6.dp)
            ) {
              Text(
                text = macroLabel,
                style = MaterialTheme.typography.labelSmall.copy(
                  fontWeight = FontWeight.Bold,
                  color = if (macroLive) ClinicalGreenHealthy else ClinicalAmberAttention,
                  fontSize = 9.5.sp,
                  letterSpacing = 0.5.sp
                ),
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.5.dp)
              )
            }
          }

          TextButton(
            onClick = onOpenDiagnostics,
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
            modifier = Modifier.testTag("open_diagnostics_button")
          ) {
            Text(
              text = "Diagnostics",
              style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = MedicalBluePrimary,
                fontSize = 11.sp
              )
            )
            Spacer(modifier = Modifier.width(2.dp))
            Icon(
              imageVector = Icons.Default.ChevronRight,
              contentDescription = null,
              tint = MedicalBluePrimary,
              modifier = Modifier.size(14.dp)
            )
          }
        }
      }
    }

    // 1c. Usage Access Prompt Card if not enabled
    if (uiState.isUsageAccessMissing) {
      item {
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = ClinicalAmberLight.copy(alpha = 0.85f)),
          border = BorderStroke(1.dp, ClinicalAmberAttention),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Outlined.SecurityUpdateWarning,
                contentDescription = null,
                tint = ClinicalAmberAttention,
                modifier = Modifier.size(20.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Column {
                Text(
                  text = "USAGE ACCESS",
                  style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = ClinicalAmberAttention
                  )
                )
                Text(
                  text = "Not enabled",
                  style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = ClinicalTextPrimary
                  )
                )
              }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = "Why: V-Watcher uses this access to understand application activity and establish normal behavioral patterns.",
              style = MaterialTheme.typography.bodySmall.copy(
                color = ClinicalTextSecondary,
                lineHeight = 17.sp
              )
            )
            Spacer(modifier = Modifier.height(10.dp))
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
              colors = ButtonDefaults.buttonColors(containerColor = ClinicalAmberAttention),
              modifier = Modifier.testTag("enable_usage_access_home_button")
            ) {
              Text(
                text = "Enable in Android Settings",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
              )
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

    // 3. DEVICE HEALTH: Circular Gauge
    item {
      CircularHealthScoreGauge(
        score = uiState.healthScore,
        condition = uiState.condition,
        supportingSentence = uiState.supportingSummary
      )
    }

    // 4. CLINICAL SUMMARY
    item {
      DoctorClinicalSummarySection(
        clinicalAssessment = uiState.doctorClinicalNote,
        lastAssessment = uiState.lastAssessmentTime
      )
    }

    // 5. ACTIONS: Prominent RUN DEVICE CHECK + Subordinate SIMULATE ANOMALY
    item {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Card(
          shape = RoundedCornerShape(20.dp),
          colors = CardDefaults.cardColors(containerColor = Color.Transparent),
          border = BorderStroke(1.dp, MedicalBlueLight.copy(alpha = 0.35f)),
          elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable { onRunCheck() }
            .testTag("run_device_check_button")
        ) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .background(
                brush = Brush.horizontalGradient(
                  colors = listOf(
                    MedicalBlueDeep,
                    MedicalBlueDark,
                    MedicalBluePrimary
                  )
                )
              )
              .padding(horizontal = 20.dp, vertical = 18.dp)
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Box(
                  contentAlignment = Alignment.Center,
                  modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.15f))
                ) {
                  Box(
                    modifier = Modifier
                      .size(36.dp)
                      .clip(CircleShape)
                      .background(MedicalBlueGlow.copy(alpha = 0.25f))
                  )
                  Icon(
                    imageVector = Icons.Default.Troubleshoot,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                  )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                      text = "RUN DEVICE CHECK",
                      style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        letterSpacing = 0.8.sp
                      )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                      color = MedicalTealGlow.copy(alpha = 0.3f),
                      shape = RoundedCornerShape(6.dp)
                    ) {
                      Text(
                        text = "ROUTINE EXAM",
                        style = MaterialTheme.typography.labelSmall.copy(
                          fontSize = 8.5.sp,
                          color = Color.White,
                          fontWeight = FontWeight.Bold,
                          letterSpacing = 0.5.sp
                        ),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                      )
                    }
                  }
                  Spacer(modifier = Modifier.height(3.dp))
                  Text(
                    text = "Evaluate apps, permissions, network & immune reflex",
                    style = MaterialTheme.typography.bodySmall.copy(
                      color = Color.White.copy(alpha = 0.88f),
                      fontSize = 12.sp,
                      lineHeight = 16.sp
                    )
                  )
                }
              }
              Spacer(modifier = Modifier.width(8.dp))
              Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                  .size(34.dp)
                  .clip(CircleShape)
                  .background(Color.White.copy(alpha = 0.18f))
              ) {
                Icon(
                  imageVector = Icons.Default.ArrowForward,
                  contentDescription = null,
                  tint = Color.White,
                  modifier = Modifier.size(18.dp)
                )
              }
            }
          }
        }

        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = ClinicalSurface),
          border = BorderStroke(1.dp, ClinicalOutline),
          elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(enabled = !uiState.isSimulationActive) { onSimulateAnomaly() }
            .testTag("simulate_unusual_activity_button")
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Row(
              modifier = Modifier.weight(1f),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                  .size(34.dp)
                  .clip(RoundedCornerShape(10.dp))
                  .background(ClinicalSurfaceVariant)
              ) {
                Icon(
                  imageVector = Icons.Outlined.Biotech,
                  contentDescription = null,
                  tint = MedicalBluePrimary,
                  modifier = Modifier.size(18.dp)
                )
              }
              Spacer(modifier = Modifier.width(12.dp))
              Column {
                Text(
                  text = "Test Lab: Simulate Anomaly",
                  style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = ClinicalTextPrimary
                  )
                )
                Text(
                  text = "Observe autonomous biological containment reflex",
                  style = MaterialTheme.typography.bodySmall.copy(
                    color = ClinicalTextSecondary,
                    fontSize = 11.5.sp
                  )
                )
              }
            }
            Icon(
              imageVector = Icons.Default.PlayArrow,
              contentDescription = null,
              tint = MedicalBluePrimary,
              modifier = Modifier.size(20.dp)
            )
          }
        }
      }
    }

    // 6. IMMUNE STATUS: Interactive Biological Immune Topology Card
    item {
      BiologicalImmuneNetworkCanvas(
        cells = uiState.immuneCells,
        barrierState = uiState.barrierState,
        onCellClick = { selectedCellForDetail = it }
      )
    }

    // 7. Vital Signs Section Header
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Outlined.Speed,
            contentDescription = null,
            tint = MedicalBluePrimary,
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "PHYSIOLOGICAL VITALS",
            style = MaterialTheme.typography.labelMedium.copy(
              fontWeight = FontWeight.Bold,
              letterSpacing = 1.1.sp,
              color = ClinicalTextSecondary
            )
          )
        }
        Text(
          text = "On-Demand Device Snapshot",
          style = MaterialTheme.typography.labelSmall.copy(
            color = ClinicalTextMuted,
            fontSize = 10.5.sp
          )
        )
      }
    }

    // 8. Vital Signs Cards (Symmetrical 2-column grid)
    item {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        val vitals = uiState.vitalSigns
        val chunked = vitals.chunked(2)
        chunked.forEach { rowVitals ->
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            rowVitals.forEach { vital ->
              VitalSignCard(
                vital = vital,
                modifier = Modifier
                  .weight(1f)
                  .testTag("vital_${vital.id}")
              )
            }
            if (rowVitals.size == 1) {
              Spacer(modifier = Modifier.weight(1f))
            }
          }
        }
      }
    }

    // 9. Biomimetic Firewall / Protective Barrier
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
        colors = CardDefaults.cardColors(containerColor = ClinicalSurface),
        border = BorderStroke(1.dp, ClinicalOutline),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(18.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Outlined.Memory,
                contentDescription = null,
                tint = MedicalBluePrimary,
                modifier = Modifier.size(18.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "LOCAL TELEMETRY ENGINE",
                style = MaterialTheme.typography.labelMedium.copy(
                  fontWeight = FontWeight.Bold,
                  color = ClinicalTextSecondary,
                  letterSpacing = 1.sp
                )
              )
            }
            Surface(
              color = ClinicalGreenLight,
              shape = RoundedCornerShape(8.dp)
            ) {
              Text(
                text = "Active • No network endpoints",
                style = MaterialTheme.typography.labelSmall.copy(
                  color = ClinicalGreenHealthy,
                  fontWeight = FontWeight.Bold,
                  fontSize = 10.sp
                ),
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
              )
            }
          }
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = "Behavioral telemetry is computed within device sandbox memory. This build declares no network endpoints, so there is nowhere for identifiers or payloads to be sent.",
            style = MaterialTheme.typography.bodySmall.copy(
              color = ClinicalTextSecondary,
              lineHeight = 18.sp,
              fontSize = 12.sp
            )
          )
          Spacer(modifier = Modifier.height(14.dp))
          HorizontalDivider(color = ClinicalOutline, thickness = 0.8.dp)
          Spacer(modifier = Modifier.height(12.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "Battery Drain",
                style = MaterialTheme.typography.labelSmall.copy(color = ClinicalTextMuted, fontSize = 10.5.sp)
              )
              Spacer(modifier = Modifier.height(2.dp))
              Text(
                text = "Unmeasured",
                style = MaterialTheme.typography.bodySmall.copy(
                  fontWeight = FontWeight.Bold,
                  color = ClinicalTextPrimary
                )
              )
            }
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "RAM Overhead",
                style = MaterialTheme.typography.labelSmall.copy(color = ClinicalTextMuted, fontSize = 10.5.sp)
              )
              Spacer(modifier = Modifier.height(2.dp))
              Text(
                text = uiState.efficiency.memoryUsage.ifEmpty { "—" },
                style = MaterialTheme.typography.bodySmall.copy(
                  fontWeight = FontWeight.Bold,
                  color = ClinicalTextPrimary
                )
              )
            }
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "CPU Footprint",
                style = MaterialTheme.typography.labelSmall.copy(color = ClinicalTextMuted, fontSize = 10.5.sp)
              )
              Spacer(modifier = Modifier.height(2.dp))
              Text(
                text = uiState.efficiency.cpuActivity.ifEmpty { "Unmeasured" },
                style = MaterialTheme.typography.bodySmall.copy(
                  fontWeight = FontWeight.Bold,
                  color = ClinicalTextPrimary
                )
              )
            }
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "Inference Mode",
                style = MaterialTheme.typography.labelSmall.copy(color = ClinicalTextMuted, fontSize = 10.5.sp)
              )
              Spacer(modifier = Modifier.height(2.dp))
              Text(
                text = uiState.efficiency.inferenceActivity.ifEmpty { "—" },
                style = MaterialTheme.typography.bodySmall.copy(
                  fontWeight = FontWeight.Bold,
                  color = ClinicalTextPrimary
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
