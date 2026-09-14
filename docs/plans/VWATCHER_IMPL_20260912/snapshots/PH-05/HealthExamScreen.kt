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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.theme.*
import com.example.viewmodel.VWatcherUiState

@Composable
fun HealthExamScreen(
  uiState: VWatcherUiState,
  onIsolateApp: (String) -> Unit,
  onReleaseApp: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  var selectedSubTab by remember { mutableStateOf("EXAM") }
  var selectedCategoryDetail by remember { mutableStateOf<ExamCategory?>(null) }

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
          text = "CLINICAL HEALTH & EXAM",
          style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.2.sp,
            color = MedicalBluePrimary
          )
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
          text = "Device Examination",
          style = MaterialTheme.typography.headlineSmall.copy(
            fontWeight = FontWeight.Bold,
            color = ClinicalTextPrimary
          )
        )
        Text(
          text = "Comprehensive diagnostic review across applications, permissions, and network",
          style = MaterialTheme.typography.bodySmall.copy(color = ClinicalTextSecondary)
        )
      }
    }

    // Sub-Navigation Tabs
    ScrollableTabRow(
      selectedTabIndex = when (selectedSubTab) {
        "EXAM" -> 0
        "APPS" -> 1
        "PERMISSIONS" -> 2
        "NETWORK" -> 3
        else -> 4
      },
      containerColor = ClinicalBackground,
      contentColor = MedicalBluePrimary,
      edgePadding = 20.dp,
      divider = { HorizontalDivider(color = ClinicalOutline, thickness = 1.dp) },
      modifier = Modifier.fillMaxWidth()
    ) {
      Tab(
        selected = selectedSubTab == "EXAM",
        onClick = { selectedSubTab = "EXAM" },
        text = { Text("Device Exam", fontWeight = if (selectedSubTab == "EXAM") FontWeight.Bold else FontWeight.Normal) },
        modifier = Modifier.testTag("tab_device_exam")
      )
      Tab(
        selected = selectedSubTab == "APPS",
        onClick = { selectedSubTab = "APPS" },
        text = { Text("App Health (${uiState.applications.size})", fontWeight = if (selectedSubTab == "APPS") FontWeight.Bold else FontWeight.Normal) },
        modifier = Modifier.testTag("tab_app_health")
      )
      Tab(
        selected = selectedSubTab == "PERMISSIONS",
        onClick = { selectedSubTab = "PERMISSIONS" },
        text = { Text("Permissions (${uiState.permissions.size})", fontWeight = if (selectedSubTab == "PERMISSIONS") FontWeight.Bold else FontWeight.Normal) },
        modifier = Modifier.testTag("tab_permissions")
      )
      Tab(
        selected = selectedSubTab == "NETWORK",
        onClick = { selectedSubTab = "NETWORK" },
        text = { Text("Network", fontWeight = if (selectedSubTab == "NETWORK") FontWeight.Bold else FontWeight.Normal) },
        modifier = Modifier.testTag("tab_network")
      )
      Tab(
        selected = selectedSubTab == "HISTORY",
        onClick = { selectedSubTab = "HISTORY" },
        text = { Text("History", fontWeight = if (selectedSubTab == "HISTORY") FontWeight.Bold else FontWeight.Normal) },
        modifier = Modifier.testTag("tab_history")
      )
    }

    LazyColumn(
      modifier = Modifier.fillMaxSize(),
      contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      when (selectedSubTab) {
        "EXAM" -> {
          item {
            Text(
              text = "EXAMINATION CATEGORIES",
              style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = ClinicalTextSecondary
              )
            )
          }
          items(uiState.examCategories) { category ->
            ExamCategoryCard(
              category = category,
              isExpanded = selectedCategoryDetail?.id == category.id,
              onClick = {
                selectedCategoryDetail = if (selectedCategoryDetail?.id == category.id) null else category
              }
            )
          }
        }

        "APPS" -> {
          item {
            Text(
              text = "PATIENT APPLICATION RECORDS",
              style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = ClinicalTextSecondary
              )
            )
            Text(
              text = "Each application has an active behavioral profile and permission baseline.",
              style = MaterialTheme.typography.bodySmall.copy(color = ClinicalTextMuted)
            )
          }
          items(uiState.applications) { app ->
            ApplicationRecordCard(
              app = app,
              onIsolate = { onIsolateApp(app.id) },
              onRelease = { onReleaseApp(app.id) }
            )
          }
        }

        "PERMISSIONS" -> {
          item {
            Text(
              text = "PERMISSION REVIEW",
              style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = ClinicalTextSecondary
              )
            )
            Text(
              text = "Sensory access frequency compared against device physiological baseline.",
              style = MaterialTheme.typography.bodySmall.copy(color = ClinicalTextMuted)
            )
          }
          items(uiState.permissions) { perm ->
            PermissionItemCard(permission = perm)
          }
        }

        "NETWORK" -> {
          item {
            Card(
              shape = RoundedCornerShape(18.dp),
              colors = CardDefaults.cardColors(containerColor = ClinicalSurface),
              border = BorderStroke(1.dp, ClinicalOutline),
              modifier = Modifier.fillMaxWidth()
            ) {
              Column(modifier = Modifier.padding(18.dp)) {
                Text(
                  text = "NETWORK STATUS",
                  style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = ClinicalTextSecondary
                  )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Text(
                    text = "Healthy",
                    style = MaterialTheme.typography.titleMedium.copy(
                      fontWeight = FontWeight.Bold,
                      color = ClinicalGreenHealthy
                    )
                  )
                  Spacer(modifier = Modifier.width(8.dp))
                  Text(
                    text = "• Protective Barrier: ${uiState.barrierState.displayName}",
                    style = MaterialTheme.typography.bodySmall.copy(color = ClinicalTextSecondary)
                  )
                }
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Column {
                    Text("${uiState.networkConnections.size} active", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                    Text("conduits", style = MaterialTheme.typography.labelSmall.copy(color = ClinicalTextMuted))
                  }
                  Column {
                    val isolatedCount = uiState.cases.count { it.status == CaseStatus.ISOLATED || it.status == CaseStatus.RESOLVED }
                    Text("$isolatedCount isolated", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = MedicalBluePrimary))
                    Text("today", style = MaterialTheme.typography.labelSmall.copy(color = ClinicalTextMuted))
                  }
                  Column {
                    Text("0 uncontained", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = ClinicalGreenHealthy))
                    Text("threats", style = MaterialTheme.typography.labelSmall.copy(color = ClinicalTextMuted))
                  }
                }
              }
            }
          }

          item {
            Text(
              text = "RECENT MONITORED CONDUITS",
              style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = ClinicalTextSecondary
              )
            )
          }
          items(uiState.networkConnections) { conn ->
            NetworkConnectionRow(connection = conn)
          }
        }

        "HISTORY" -> {
          item {
            Text(
              text = "HEALTH HISTORY",
              style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = ClinicalTextSecondary
              )
            )
          }
          items(uiState.healthHistory) { history ->
            HealthHistoryCard(point = history)
          }
        }
      }

      item {
        Spacer(modifier = Modifier.height(72.dp))
      }
    }
  }
}

@Composable
private fun ExamCategoryCard(
  category: ExamCategory,
  isExpanded: Boolean,
  onClick: () -> Unit
) {
  val statusColor = if (category.isHealthy) ClinicalGreenHealthy else ClinicalAmberAttention
  Card(
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = ClinicalSurface),
    border = BorderStroke(1.dp, ClinicalOutline),
    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    modifier = Modifier
      .fillMaxWidth()
      .clickable { onClick() }
      .testTag("exam_cat_${category.id}")
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
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = category.name,
            style = MaterialTheme.typography.titleSmall.copy(
              fontWeight = FontWeight.Bold,
              color = ClinicalTextPrimary,
              letterSpacing = 0.8.sp
            )
          )
          Text(
            text = category.summary,
            style = MaterialTheme.typography.bodySmall.copy(
              color = ClinicalTextSecondary,
              fontSize = 12.sp
            )
          )
        }
        Surface(
          color = statusColor.copy(alpha = 0.12f),
          shape = RoundedCornerShape(8.dp)
        ) {
          Text(
            text = category.status,
            style = MaterialTheme.typography.labelSmall.copy(
              color = statusColor,
              fontWeight = FontWeight.Bold
            ),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
          )
        }
      }

      AnimatedVisibility(visible = isExpanded) {
        Column(modifier = Modifier.padding(top = 12.dp)) {
          HorizontalDivider(color = ClinicalOutline, thickness = 0.8.dp)
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = "Details: ${category.detailCountText}",
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium, color = ClinicalTextPrimary)
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = category.notes,
            style = MaterialTheme.typography.bodySmall.copy(color = ClinicalTextSecondary, fontSize = 12.sp)
          )
        }
      }
    }
  }
}

@Composable
private fun ApplicationRecordCard(
  app: AppRecord,
  onIsolate: () -> Unit,
  onRelease: () -> Unit
) {
  val isIsolated = app.healthState == AppHealthState.ISOLATED
  val stateColor = when (app.healthState) {
    AppHealthState.HEALTHY -> ClinicalGreenHealthy
    AppHealthState.UNDER_REVIEW, AppHealthState.ATTENTION -> ClinicalAmberAttention
    AppHealthState.ISOLATED -> ClinicalCoralCritical
  }

  Card(
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = ClinicalSurface),
    border = BorderStroke(1.dp, if (isIsolated) ClinicalCoralCritical.copy(alpha = 0.5f) else ClinicalOutline),
    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    modifier = Modifier.fillMaxWidth().testTag("app_card_${app.id}")
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
              .size(40.dp)
              .clip(RoundedCornerShape(10.dp))
              .background(MedicalBlueLight),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = when (app.iconKind) {
                "chat" -> Icons.Outlined.Chat
                "security" -> Icons.Outlined.Lock
                "sync" -> Icons.Outlined.Sync
                "location" -> Icons.Outlined.NearMe
                "audio" -> Icons.Outlined.Headphones
                "web" -> Icons.Outlined.Public
                else -> Icons.Outlined.Apps
              },
              contentDescription = null,
              tint = MedicalBlueDark,
              modifier = Modifier.size(22.dp)
            )
          }
          Spacer(modifier = Modifier.width(12.dp))
          Column {
            Text(
              text = app.name,
              style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                color = ClinicalTextPrimary
              )
            )
            Text(
              text = app.packageName,
              style = MaterialTheme.typography.bodySmall.copy(
                color = ClinicalTextMuted,
                fontSize = 11.sp
              )
            )
          }
        }
        Surface(
          color = stateColor.copy(alpha = 0.12f),
          shape = RoundedCornerShape(8.dp)
        ) {
          Text(
            text = app.healthState.displayName,
            style = MaterialTheme.typography.labelSmall.copy(
              color = stateColor,
              fontWeight = FontWeight.Bold
            ),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(
          text = "Permissions: ${app.permissionSummary}",
          style = MaterialTheme.typography.bodySmall.copy(color = ClinicalTextSecondary, fontSize = 12.sp)
        )
        Text(
          text = "Behavior: ${app.behaviorState}",
          style = MaterialTheme.typography.bodySmall.copy(color = ClinicalTextSecondary, fontSize = 12.sp)
        )
        Text(
          text = "Last observation: ${app.lastObserved}",
          style = MaterialTheme.typography.bodySmall.copy(color = ClinicalTextMuted, fontSize = 11.sp)
        )
        if (app.isolationReason.isNotEmpty()) {
          Text(
            text = "Reason: ${app.isolationReason}",
            style = MaterialTheme.typography.bodySmall.copy(
              color = ClinicalCoralCritical,
              fontSize = 11.sp,
              fontWeight = FontWeight.Medium
            )
          )
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
      ) {
        if (isIsolated) {
          Button(
            onClick = onRelease,
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ClinicalGreenHealthy),
            modifier = Modifier.height(36.dp).testTag("release_app_button_${app.id}")
          ) {
            Icon(Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Release / Restore", fontSize = 12.sp)
          }
        } else {
          OutlinedButton(
            onClick = onIsolate,
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, ClinicalAmberAttention),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = ClinicalAmberAttention),
            modifier = Modifier.height(36.dp).testTag("isolate_app_button_${app.id}")
          ) {
            Icon(Icons.Outlined.Shield, contentDescription = null, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Clinical Quarantine", fontSize = 12.sp)
          }
        }
      }
    }
  }
}

@Composable
private fun PermissionItemCard(permission: PermissionReviewItem) {
  val statusColor = when (permission.clinicalStatus) {
    PermissionClinicalStatus.EXPECTED -> ClinicalGreenHealthy
    PermissionClinicalStatus.REVIEW_RECOMMENDED -> ClinicalAmberAttention
    PermissionClinicalStatus.UNUSUAL -> ClinicalCoralCritical
  }

  Card(
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = ClinicalSurface),
    border = BorderStroke(1.dp, ClinicalOutline),
    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    modifier = Modifier.fillMaxWidth().testTag("permission_${permission.id}")
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
        Text(
          text = permission.name.uppercase(),
          style = MaterialTheme.typography.labelMedium.copy(
            fontWeight = FontWeight.Bold,
            color = ClinicalTextPrimary,
            letterSpacing = 1.sp
          )
        )
        Surface(
          color = statusColor.copy(alpha = 0.12f),
          shape = RoundedCornerShape(8.dp)
        ) {
          Text(
            text = permission.clinicalStatus.displayName,
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
        text = "\"${permission.rationale}\"",
        style = MaterialTheme.typography.bodySmall.copy(
          color = ClinicalTextSecondary,
          fontSize = 12.sp
        )
      )

      Spacer(modifier = Modifier.height(6.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Text(
          text = "Frequency: ${permission.frequencyNote}",
          style = MaterialTheme.typography.labelSmall.copy(color = ClinicalTextMuted, fontSize = 11.sp)
        )
        Text(
          text = "${permission.appsCount} apps authorized",
          style = MaterialTheme.typography.labelSmall.copy(color = ClinicalTextMuted, fontSize = 11.sp)
        )
      }
    }
  }
}

@Composable
private fun NetworkConnectionRow(connection: NetworkConnection) {
  Card(
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(containerColor = ClinicalSurface),
    border = BorderStroke(1.dp, ClinicalOutline),
    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    modifier = Modifier.fillMaxWidth()
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = connection.endpoint,
          style = MaterialTheme.typography.bodyMedium.copy(
            fontWeight = FontWeight.SemiBold,
            color = ClinicalTextPrimary
          )
        )
        Text(
          text = "${connection.appName} • ${connection.protocol} • ${connection.bandwidth}",
          style = MaterialTheme.typography.bodySmall.copy(
            color = ClinicalTextMuted,
            fontSize = 11.sp
          )
        )
      }

      Surface(
        color = ClinicalGreenLight,
        shape = RoundedCornerShape(6.dp)
      ) {
        Text(
          text = connection.status,
          style = MaterialTheme.typography.labelSmall.copy(
            color = ClinicalGreenHealthy,
            fontWeight = FontWeight.SemiBold
          ),
          modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
      }
    }
  }
}

@Composable
private fun HealthHistoryCard(point: HealthHistoryPoint) {
  Card(
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = ClinicalSurface),
    border = BorderStroke(1.dp, ClinicalOutline),
    modifier = Modifier.fillMaxWidth()
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = point.periodLabel,
          style = MaterialTheme.typography.titleSmall.copy(
            fontWeight = FontWeight.Bold,
            color = ClinicalTextPrimary
          )
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
          text = point.summaryNote,
          style = MaterialTheme.typography.bodySmall.copy(
            color = ClinicalTextSecondary,
            fontSize = 12.sp
          )
        )
      }
      Column(horizontalAlignment = Alignment.End) {
        Text(
          text = "${point.healthScore}",
          style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.Bold,
            color = MedicalBluePrimary
          )
        )
        Text(
          text = point.condition,
          style = MaterialTheme.typography.labelSmall.copy(
            color = ClinicalGreenHealthy,
            fontWeight = FontWeight.Medium
          )
        )
      }
    }
  }
}
