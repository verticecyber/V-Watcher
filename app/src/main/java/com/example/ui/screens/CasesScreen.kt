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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.components.ExplainabilityCard
import com.example.ui.theme.*
import com.example.viewmodel.VWatcherUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CasesScreen(
  uiState: VWatcherUiState,
  onResolveCase: (String) -> Unit,
  onReleaseAppByName: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  var selectedCaseForDetail by remember { mutableStateOf<IncidentCase?>(null) }
  var filterStatus by remember { mutableStateOf("ALL") }

  val filteredCases = remember(uiState.cases, filterStatus) {
    when (filterStatus) {
      "ACTIVE" -> uiState.cases.filter { it.status != CaseStatus.RESOLVED }
      "RESOLVED" -> uiState.cases.filter { it.status == CaseStatus.RESOLVED }
      else -> uiState.cases
    }
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(ClinicalBackground)
  ) {
    // Screen Title Bar
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
          text = "CLINICAL INCIDENTS",
          style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.2.sp,
            color = MedicalBluePrimary
          )
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
          text = "Cases & Timeline",
          style = MaterialTheme.typography.headlineSmall.copy(
            fontWeight = FontWeight.Bold,
            color = ClinicalTextPrimary
          )
        )
        Text(
          text = "Each security observation is managed as a transparent clinical patient case",
          style = MaterialTheme.typography.bodySmall.copy(color = ClinicalTextSecondary)
        )
      }
    }

    // Filter Chips
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp, vertical = 6.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      FilterChip(
        selected = filterStatus == "ALL",
        onClick = { filterStatus = "ALL" },
        label = { Text("All Cases (${uiState.cases.size})") },
        colors = FilterChipDefaults.filterChipColors(
          selectedContainerColor = MedicalBlueLight,
          selectedLabelColor = MedicalBlueDark
        ),
        modifier = Modifier.testTag("filter_all_cases")
      )
      FilterChip(
        selected = filterStatus == "ACTIVE",
        onClick = { filterStatus = "ACTIVE" },
        label = { Text("Active (${uiState.cases.count { it.status != CaseStatus.RESOLVED }})") },
        colors = FilterChipDefaults.filterChipColors(
          selectedContainerColor = ClinicalAmberLight,
          selectedLabelColor = ClinicalAmberAttention
        ),
        modifier = Modifier.testTag("filter_active_cases")
      )
      FilterChip(
        selected = filterStatus == "RESOLVED",
        onClick = { filterStatus = "RESOLVED" },
        label = { Text("Resolved (${uiState.cases.count { it.status == CaseStatus.RESOLVED }})") },
        colors = FilterChipDefaults.filterChipColors(
          selectedContainerColor = ClinicalGreenLight,
          selectedLabelColor = ClinicalGreenHealthy
        ),
        modifier = Modifier.testTag("filter_resolved_cases")
      )
    }

    LazyColumn(
      modifier = Modifier.fillMaxSize(),
      contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      if (filteredCases.isEmpty()) {
        item {
          Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = ClinicalSurface),
            border = BorderStroke(1.dp, ClinicalOutline),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 36.dp),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Box(
                modifier = Modifier
                  .size(56.dp)
                  .clip(CircleShape)
                  .background(if (filterStatus == "ACTIVE") ClinicalGreenLight else MedicalBlueLight),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = if (filterStatus == "ACTIVE") Icons.Default.CheckCircle else Icons.Outlined.AssignmentLate,
                  contentDescription = null,
                  tint = if (filterStatus == "ACTIVE") ClinicalGreenHealthy else MedicalBluePrimary,
                  modifier = Modifier.size(30.dp)
                )
              }
              Spacer(modifier = Modifier.height(16.dp))
              Text(
                text = when (filterStatus) {
                  "ACTIVE" -> "Zero Active Incidents"
                  "RESOLVED" -> "No Resolved Cases"
                  else -> "No Incidents Recorded"
                },
                style = MaterialTheme.typography.titleMedium.copy(
                  fontWeight = FontWeight.Bold,
                  color = ClinicalTextPrimary
                ),
                textAlign = TextAlign.Center
              )
              Spacer(modifier = Modifier.height(6.dp))
              Text(
                text = when (filterStatus) {
                  "ACTIVE" -> "All observed behavioral anomalies have been analyzed and resolved. Biological homeostasis is maintained."
                  "RESOLVED" -> "Resolved cases will be archived here once clinical review and containment actions are completed."
                  else -> "V-Watcher is actively monitoring telemetry signals. No behavioral incidents have occurred."
                },
                style = MaterialTheme.typography.bodySmall.copy(
                  color = ClinicalTextSecondary,
                  fontSize = 12.5.sp,
                  lineHeight = 17.sp
                ),
                textAlign = TextAlign.Center
              )
              if (filterStatus != "ALL") {
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedButton(
                  onClick = { filterStatus = "ALL" },
                  shape = RoundedCornerShape(12.dp),
                  border = BorderStroke(1.dp, MedicalBluePrimary.copy(alpha = 0.5f)),
                  colors = ButtonDefaults.outlinedButtonColors(contentColor = MedicalBluePrimary),
                  modifier = Modifier.height(44.dp)
                ) {
                  Text(
                    text = "View All Cases (${uiState.cases.size})",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                  )
                }
              }
            }
          }
        }
      } else {
        items(filteredCases) { incidentCase ->
          IncidentCaseSummaryCard(
            incidentCase = incidentCase,
            onCardClick = { selectedCaseForDetail = incidentCase }
          )
        }
      }

      item {
        Spacer(modifier = Modifier.height(72.dp))
      }
    }
  }

  // Detailed Clinical Case Modal Sheet (Timeline + Explainability "Why?")
  selectedCaseForDetail?.let { caseItem ->
    ModalBottomSheet(
      onDismissRequest = { selectedCaseForDetail = null },
      containerColor = ClinicalBackground,
      shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
      LazyColumn(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
      ) {
        // Modal Header
        item {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = caseItem.caseCode,
                style = MaterialTheme.typography.labelMedium.copy(
                  fontWeight = FontWeight.Bold,
                  letterSpacing = 1.1.sp,
                  color = MedicalBluePrimary
                )
              )
              Text(
                text = caseItem.title,
                style = MaterialTheme.typography.titleMedium.copy(
                  fontWeight = FontWeight.Bold,
                  color = ClinicalTextPrimary
                )
              )
              Text(
                text = "Initiated: ${caseItem.date} • Severity: ${caseItem.severity}",
                style = MaterialTheme.typography.bodySmall.copy(color = ClinicalTextMuted)
              )
            }
            Surface(
              color = when (caseItem.status) {
                CaseStatus.RESOLVED -> ClinicalGreenLight
                CaseStatus.ISOLATED, CaseStatus.CONTAINED -> ClinicalCoralLight
                else -> ClinicalAmberLight
              },
              shape = RoundedCornerShape(8.dp)
            ) {
              Text(
                text = caseItem.status.displayName,
                style = MaterialTheme.typography.labelSmall.copy(
                  color = when (caseItem.status) {
                    CaseStatus.RESOLVED -> ClinicalGreenHealthy
                    CaseStatus.ISOLATED, CaseStatus.CONTAINED -> ClinicalCoralCritical
                    else -> ClinicalAmberAttention
                  },
                  fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
              )
            }
          }
        }

        // Case Timeline
        item {
          Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = ClinicalSurface),
            border = BorderStroke(1.dp, ClinicalOutline),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = Icons.Outlined.Timeline,
                  contentDescription = null,
                  tint = MedicalBluePrimary,
                  modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = "CLINICAL TIMELINE",
                  style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = ClinicalTextSecondary
                  )
                )
              }
              Spacer(modifier = Modifier.height(14.dp))
              caseItem.timeline.forEachIndexed { index, event ->
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  verticalAlignment = Alignment.Top
                ) {
                  // Medical timestamp
                  Text(
                    text = event.time,
                    style = MaterialTheme.typography.labelSmall.copy(
                      fontWeight = FontWeight.Bold,
                      fontFamily = FontFamily.Monospace,
                      color = MedicalBluePrimary
                    ),
                    modifier = Modifier.width(60.dp)
                  )
                  // Timeline bullet & line
                  Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 8.dp)
                  ) {
                    Box(
                      modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(MedicalTealSecondary)
                    )
                    if (index < caseItem.timeline.size - 1) {
                      Box(
                        modifier = Modifier
                          .width(1.5.dp)
                          .height(34.dp)
                          .background(ClinicalOutline)
                      )
                    }
                  }
                  // Detail
                  Column(modifier = Modifier.weight(1f)) {
                    Text(
                      text = event.title,
                      style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = ClinicalTextPrimary
                      )
                    )
                    Text(
                      text = event.detail,
                      style = MaterialTheme.typography.bodySmall.copy(
                        color = ClinicalTextSecondary,
                        fontSize = 11.sp
                      )
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                  }
                }
              }
            }
          }
        }

        // Explainability Report
        item {
          ExplainabilityCard(
            incidentCase = caseItem,
            onResolveClick = {
              onResolveCase(caseItem.id)
              caseItem.affectedApp?.let { appName -> onReleaseAppByName(appName) }
              selectedCaseForDetail = null
            }
          )
        }

        item {
          Spacer(modifier = Modifier.height(40.dp))
        }
      }
    }
  }
}

@Composable
private fun IncidentCaseSummaryCard(
  incidentCase: IncidentCase,
  onCardClick: () -> Unit
) {
  val isResolved = incidentCase.status == CaseStatus.RESOLVED
  val statusColor = when (incidentCase.status) {
    CaseStatus.RESOLVED -> ClinicalGreenHealthy
    CaseStatus.ISOLATED, CaseStatus.CONTAINED -> ClinicalCoralCritical
    else -> ClinicalAmberAttention
  }

  Card(
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = ClinicalSurface),
    border = BorderStroke(1.dp, if (isResolved) ClinicalOutline else statusColor.copy(alpha = 0.5f)),
    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    modifier = Modifier
      .fillMaxWidth()
      .clickable { onCardClick() }
      .testTag("case_item_${incidentCase.id}")
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
          Text(
            text = incidentCase.caseCode,
            style = MaterialTheme.typography.labelMedium.copy(
              fontWeight = FontWeight.Bold,
              letterSpacing = 1.sp,
              color = MedicalBluePrimary
            )
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "• ${incidentCase.date}",
            style = MaterialTheme.typography.bodySmall.copy(color = ClinicalTextMuted, fontSize = 11.sp)
          )
        }
        Surface(
          color = statusColor.copy(alpha = 0.12f),
          shape = RoundedCornerShape(8.dp)
        ) {
          Text(
            text = incidentCase.status.displayName,
            style = MaterialTheme.typography.labelSmall.copy(
              color = statusColor,
              fontWeight = FontWeight.Bold
            ),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      Text(
        text = incidentCase.title,
        style = MaterialTheme.typography.titleSmall.copy(
          fontWeight = FontWeight.Bold,
          color = ClinicalTextPrimary
        )
      )

      Spacer(modifier = Modifier.height(4.dp))

      Text(
        text = "\"${incidentCase.assessment}\"",
        style = MaterialTheme.typography.bodySmall.copy(
          color = ClinicalTextSecondary,
          fontSize = 12.sp
        ),
        maxLines = 2
      )

      Spacer(modifier = Modifier.height(10.dp))
      HorizontalDivider(color = ClinicalOutline, thickness = 0.8.dp)
      Spacer(modifier = Modifier.height(8.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Action: ${incidentCase.actionTaken}",
          style = MaterialTheme.typography.bodySmall.copy(
            color = ClinicalTextSecondary,
            fontSize = 11.sp
          ),
          modifier = Modifier.weight(1f)
        )
        Text(
          text = "View Timeline →",
          style = MaterialTheme.typography.labelSmall.copy(
            color = MedicalBluePrimary,
            fontWeight = FontWeight.SemiBold
          )
        )
      }
    }
  }
}
