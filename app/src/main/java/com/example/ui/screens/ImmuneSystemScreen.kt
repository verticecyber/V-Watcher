package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.components.BiologicalImmuneNetworkCanvas
import com.example.ui.components.ImmuneCellDetailBottomSheet
import com.example.ui.components.ProtectiveBarrierCard
import com.example.ui.theme.*
import com.example.viewmodel.VWatcherUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImmuneSystemScreen(
  uiState: VWatcherUiState,
  onNavigateToNetwork: () -> Unit,
  modifier: Modifier = Modifier
) {
  var selectedCellDetail by remember { mutableStateOf<ImmuneCell?>(null) }
  var selectedDecoyDetail by remember { mutableStateOf<DecoyEnvironment?>(null) }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(ClinicalBackground)
  ) {
    // Header
    Surface(
      color = ClinicalBackground,
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 20.dp, vertical = 14.dp)
      ) {
        Text(
          text = "INTERNAL ARCHITECTURE",
          style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.2.sp,
            color = MedicalBluePrimary
          )
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
          text = "Immune System",
          style = MaterialTheme.typography.headlineSmall.copy(
            fontWeight = FontWeight.Bold,
            color = ClinicalTextPrimary
          )
        )
        Text(
          text = "Autonomous biological defense architecture operating locally on device",
          style = MaterialTheme.typography.bodySmall.copy(color = ClinicalTextSecondary)
        )
      }
    }

    LazyColumn(
      modifier = Modifier.fillMaxSize(),
      contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // Biological Immune Network Visualizer
      item {
        BiologicalImmuneNetworkCanvas(
          cells = uiState.immuneCells,
          barrierState = uiState.barrierState,
          onCellClick = { cell -> selectedCellDetail = cell }
        )
      }

      // Protective Barrier Card
      item {
        ProtectiveBarrierCard(
          state = uiState.barrierState,
          onViewNetworkClick = onNavigateToNetwork
        )
      }

      // Homeostasis & Immune Multi-Agent Bus Status Card
      item {
        val homeostasis = uiState.homeostasisEvaluation
        val busStats = uiState.immuneBusStats
        val macroLabel = macroStateDisplayName(homeostasis?.macroState)

        val (badgeBg, badgeText) = when (homeostasis?.macroState) {
          com.example.immune.HomeostaticMacroState.HOMEOSTATIC -> ClinicalGreenLight to ClinicalGreenHealthy
          com.example.immune.HomeostaticMacroState.WATCH -> ClinicalAmberLight to ClinicalAmberAttention
          com.example.immune.HomeostaticMacroState.STRESSED -> ClinicalAmberLight to ClinicalAmberAttention
          com.example.immune.HomeostaticMacroState.ACTIVE_DEFENSE -> ClinicalCoralLight to ClinicalCoralCritical
          com.example.immune.HomeostaticMacroState.RECOVERING -> MedicalTealLight to MedicalTealSecondary
          com.example.immune.HomeostaticMacroState.DEGRADED -> ClinicalCoralLight to ClinicalCoralCritical
          com.example.immune.HomeostaticMacroState.UNKNOWN -> ClinicalSurfaceVariant to ClinicalTextMuted
          null -> ClinicalSurfaceVariant to ClinicalTextMuted
        }

        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = ClinicalSurface),
          border = BorderStroke(1.dp, ClinicalOutline),
          modifier = Modifier.fillMaxWidth().testTag("homeostasis_status_card")
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.MonitorHeart, contentDescription = null, tint = MedicalBluePrimary, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = "HOMEOSTASIS ENGINE",
                  style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                )
              }
              Surface(color = badgeBg, shape = RoundedCornerShape(6.dp)) {
                Text(
                  text = macroLabel,
                  style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = badgeText, fontSize = 10.sp),
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
              }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = homeostasisReasonDisplay(homeostasis?.primaryReason),
              style = MaterialTheme.typography.bodySmall.copy(color = ClinicalTextSecondary)
            )
            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = ClinicalOutline, thickness = 0.8.dp)
            Spacer(modifier = Modifier.height(10.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Column {
                Text("Confidence", style = MaterialTheme.typography.labelSmall.copy(color = ClinicalTextMuted, fontSize = 10.sp))
                Text(homeostasisConfidenceDisplay(homeostasis?.confidenceScore), style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = ClinicalTextSecondary))
              }
              Column {
                Text("Bus Rate", style = MaterialTheme.typography.labelSmall.copy(color = ClinicalTextMuted, fontSize = 10.sp))
                Text("${"%.1f".format(busStats?.currentDispatchesPerSecond ?: 0.0)}/s", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = ClinicalTextPrimary))
              }
              Column {
                Text("Storm Protection", style = MaterialTheme.typography.labelSmall.copy(color = ClinicalTextMuted, fontSize = 10.sp))
                Text(if (busStats?.activeStormDetected == true) "Active" else "Nominal", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = if (busStats?.activeStormDetected == true) ClinicalCoralCritical else ClinicalGreenHealthy))
              }
              Column {
                Text("Active Effectors", style = MaterialTheme.typography.labelSmall.copy(color = ClinicalTextMuted, fontSize = 10.sp))
                Text("${homeostasis?.immuneActivity?.activeAgentsCount ?: 0} cells", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = MedicalBluePrimary))
              }
            }
          }
        }
      }

      // Interactive Immune Cells List
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "ACTIVE IMMUNE CELLS",
            style = MaterialTheme.typography.labelMedium.copy(
              fontWeight = FontWeight.Bold,
              letterSpacing = 1.sp,
              color = ClinicalTextSecondary
            )
          )
          Text(
            text = "${uiState.immuneCells.size} cells instantiated",
            style = MaterialTheme.typography.labelSmall.copy(color = ClinicalTextMuted)
          )
        }
      }

      items(uiState.immuneCells) { cell ->
        ImmuneCellCard(
          cell = cell,
          onClick = { selectedCellDetail = cell }
        )
      }

      // Decoy Environments / Honeypots
      item {
        Spacer(modifier = Modifier.height(8.dp))
        Column {
          Text(
            text = "DECOY ENVIRONMENTS",
            style = MaterialTheme.typography.labelMedium.copy(
              fontWeight = FontWeight.Bold,
              letterSpacing = 1.sp,
              color = ClinicalTextSecondary
            )
          )
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = "Decoy capacity is not implemented in this build: no traffic is diverted anywhere.",
            style = MaterialTheme.typography.bodySmall.copy(color = ClinicalTextSecondary)
          )
        }
      }

      // Diversion sequence indicator
      item {
        Surface(
          color = ClinicalSurfaceVariant,
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Text(
            text = "No diversion pipeline exists in this build: there is no live interception, decoy routing, or containment of external traffic.",
            style = MaterialTheme.typography.labelSmall.copy(
              color = ClinicalTextSecondary,
              fontFamily = FontFamily.Monospace,
              fontSize = 9.5.sp
            ),
            modifier = Modifier.padding(10.dp)
          )
        }
      }

      items(uiState.decoys) { decoy ->
        DecoyEnvironmentCard(
          decoy = decoy,
          onClick = { selectedDecoyDetail = decoy }
        )
      }

      item {
        Spacer(modifier = Modifier.height(72.dp))
      }
    }
  }

  // Cell Detail Bottom Sheet
  selectedCellDetail?.let { cell ->
    ImmuneCellDetailBottomSheet(
      cell = cell,
      onDismiss = { selectedCellDetail = null }
    )
  }

  // Decoy Detail Bottom Sheet
  selectedDecoyDetail?.let { decoy ->
    ModalBottomSheet(
      onDismissRequest = { selectedDecoyDetail = null },
      containerColor = ClinicalSurface,
      shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 24.dp, vertical = 16.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = decoy.name,
            style = MaterialTheme.typography.titleLarge.copy(
              fontWeight = FontWeight.Bold,
              color = ClinicalTextPrimary
            )
          )
          Surface(
            color = if (decoy.interactionsCount > 0) ClinicalAmberLight else ClinicalGreenLight,
            shape = RoundedCornerShape(8.dp)
          ) {
            Text(
              text = decoy.status,
              style = MaterialTheme.typography.labelSmall.copy(
                color = if (decoy.interactionsCount > 0) ClinicalAmberAttention else ClinicalGreenHealthy,
                fontWeight = FontWeight.Bold
              ),
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
            )
          }
        }

        Spacer(modifier = Modifier.height(12.dp))
        Text(
          text = decoy.description,
          style = MaterialTheme.typography.bodyMedium.copy(color = ClinicalTextSecondary)
        )
        Spacer(modifier = Modifier.height(14.dp))
        HorizontalDivider(color = ClinicalOutline)
        Spacer(modifier = Modifier.height(14.dp))
        Text(
          text = "Interactions Captured: ${decoy.interactionsCount}",
          style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
        )
        Text(
          text = "Last Observation: ${decoy.lastObservation}",
          style = MaterialTheme.typography.bodySmall.copy(color = ClinicalTextSecondary)
        )
        Text(
          text = "Behavioral Trace: ${decoy.behaviorCaptured}",
          style = MaterialTheme.typography.bodySmall.copy(color = ClinicalTextSecondary)
        )
        Spacer(modifier = Modifier.height(32.dp))
      }
    }
  }
}

@Composable
private fun ImmuneCellCard(
  cell: ImmuneCell,
  onClick: () -> Unit
) {
  val isElevated = cell.state != ImmuneCellState.MONITORING && cell.state != ImmuneCellState.IDLE
  val stateColor = when {
    isElevated -> ClinicalAmberAttention
    cell.name.contains("RESPONSE") -> MedicalTealSecondary
    cell.name.contains("MEMORY") -> MemoryLavender
    else -> MedicalBluePrimary
  }

  Card(
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = ClinicalSurface),
    border = BorderStroke(1.dp, if (isElevated) stateColor else ClinicalOutline),
    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    modifier = Modifier
      .fillMaxWidth()
      .clickable { onClick() }
      .testTag("cell_card_${cell.id}")
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(10.dp)
              .clip(CircleShape)
              .background(stateColor)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = cell.name,
            style = MaterialTheme.typography.titleSmall.copy(
              fontWeight = FontWeight.Bold,
              letterSpacing = 0.8.sp,
              color = ClinicalTextPrimary
            )
          )
        }
        Surface(
          color = stateColor.copy(alpha = 0.12f),
          shape = RoundedCornerShape(8.dp)
        ) {
          Text(
            text = cell.state.displayName,
            style = MaterialTheme.typography.labelSmall.copy(
              color = stateColor,
              fontWeight = FontWeight.Bold
            ),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(6.dp))

      Text(
        text = "\"${cell.role}\"",
        style = MaterialTheme.typography.bodySmall.copy(
          color = ClinicalTextSecondary,
          fontSize = 12.sp,
          fontWeight = FontWeight.Medium
        )
      )

      Spacer(modifier = Modifier.height(8.dp))

      Text(
        text = "Activity: ${cell.activity}",
        style = MaterialTheme.typography.bodySmall.copy(
          color = ClinicalTextSecondary,
          fontSize = 11.sp
        )
      )

      Spacer(modifier = Modifier.height(8.dp))
      HorizontalDivider(color = ClinicalOutline, thickness = 0.8.dp)
      Spacer(modifier = Modifier.height(8.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Text(
          text = "Confidence: ${cell.confidence}%",
          style = MaterialTheme.typography.labelSmall.copy(color = ClinicalTextMuted, fontSize = 11.sp)
        )
        Text(
          text = "${cell.eventsHandled} events handled",
          style = MaterialTheme.typography.labelSmall.copy(color = ClinicalTextMuted, fontSize = 11.sp)
        )
      }
    }
  }
}

@Composable
private fun DecoyEnvironmentCard(
  decoy: DecoyEnvironment,
  onClick: () -> Unit
) {
  val hasInteractions = decoy.interactionsCount > 0
  val statusColor = if (hasInteractions) ClinicalAmberAttention else ClinicalGreenHealthy

  Card(
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = ClinicalSurface),
    border = BorderStroke(1.dp, if (hasInteractions) ClinicalAmberAttention.copy(alpha = 0.5f) else ClinicalOutline),
    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    modifier = Modifier
      .fillMaxWidth()
      .clickable { onClick() }
      .testTag("decoy_${decoy.id}")
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Outlined.DeviceHub,
            contentDescription = null,
            tint = MedicalBluePrimary,
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = decoy.name,
            style = MaterialTheme.typography.titleSmall.copy(
              fontWeight = FontWeight.Bold,
              color = ClinicalTextPrimary
            )
          )
        }
        Surface(
          color = statusColor.copy(alpha = 0.12f),
          shape = RoundedCornerShape(8.dp)
        ) {
          Text(
            text = decoy.status,
            style = MaterialTheme.typography.labelSmall.copy(
              color = statusColor,
              fontWeight = FontWeight.Bold
            ),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(6.dp))

      Text(
        text = decoy.description,
        style = MaterialTheme.typography.bodySmall.copy(
          color = ClinicalTextSecondary,
          fontSize = 12.sp
        )
      )

      Spacer(modifier = Modifier.height(8.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Text(
          text = "Interactions: ${decoy.interactionsCount}",
          style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = FontWeight.SemiBold,
            color = if (hasInteractions) ClinicalAmberAttention else ClinicalTextSecondary
          )
        )
        Text(
          text = "Last: ${decoy.lastObservation}",
          style = MaterialTheme.typography.labelSmall.copy(color = ClinicalTextMuted)
        )
      }
    }
  }
}
