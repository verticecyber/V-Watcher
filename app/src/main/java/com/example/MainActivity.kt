package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.viewmodel.VWatcherViewModel

class MainActivity : ComponentActivity() {

  private val viewModel: VWatcherViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        VWatcherApp(viewModel = viewModel)
      }
    }
  }
}

@Composable
fun VWatcherApp(
  viewModel: VWatcherViewModel,
  modifier: Modifier = Modifier
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  var currentTab by remember { mutableStateOf("HOME") }
  var isDiagnosticsOpen by remember { mutableStateOf(false) }
  var isNotificationsOpen by remember { mutableStateOf(false) }

  // Back Navigation Handlers to ensure app never hangs or freezes
  BackHandler(enabled = uiState.isExamInProgress) {
    viewModel.dismissExamDialog()
  }
  BackHandler(enabled = isNotificationsOpen) {
    isNotificationsOpen = false
  }
  BackHandler(enabled = isDiagnosticsOpen) {
    isDiagnosticsOpen = false
  }
  BackHandler(enabled = !isDiagnosticsOpen && !isNotificationsOpen && !uiState.isExamInProgress && currentTab != "HOME") {
    currentTab = "HOME"
  }

  // Interactive Examination Dialog (Routine Check)
  if (uiState.isExamInProgress) {
    ExamProgressDialog(
      currentStep = uiState.currentExamStep,
      progressFloat = uiState.examProgressFloat,
      healthScore = uiState.healthScore,
      doctorSummary = uiState.doctorClinicalNote,
      onDismiss = { viewModel.dismissExamDialog() }
    )
  }

  // Clinical Notifications Bottom Sheet
  if (isNotificationsOpen) {
    SimulatedNotificationsSheet(
      notifications = uiState.notifications,
      onDismiss = { isNotificationsOpen = false }
    )
  }

  if (isDiagnosticsOpen) {
    DiagnosticScreen(
      deviceState = uiState.deviceState,
      onBack = { isDiagnosticsOpen = false },
      onRefreshTelemetry = { viewModel.refreshRealTelemetry() }
    )
  } else {
    Scaffold(
      containerColor = ClinicalBackground,
      bottomBar = {
        NavigationBar(
          containerColor = ClinicalSurface,
          contentColor = MedicalBluePrimary,
          tonalElevation = 2.dp,
          modifier = Modifier.testTag("bottom_nav_bar")
        ) {
            NavigationBarItem(
              selected = currentTab == "HOME",
              onClick = { currentTab = "HOME" },
              icon = {
                Icon(
                  imageVector = if (currentTab == "HOME") Icons.Filled.HealthAndSafety else Icons.Outlined.HealthAndSafety,
                  contentDescription = "Home"
                )
              },
              label = {
                Text(
                  "Home",
                  style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = if (currentTab == "HOME") FontWeight.Bold else FontWeight.Medium
                  )
                )
              },
              colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MedicalBluePrimary,
                selectedTextColor = MedicalBluePrimary,
                indicatorColor = MedicalBlueLight
              ),
              modifier = Modifier.testTag("nav_home")
            )

            NavigationBarItem(
              selected = currentTab == "HEALTH",
              onClick = { currentTab = "HEALTH" },
              icon = {
                Icon(
                  imageVector = if (currentTab == "HEALTH") Icons.Filled.FactCheck else Icons.Outlined.FactCheck,
                  contentDescription = "Health & Exam"
                )
              },
              label = {
                Text(
                  "Exam",
                  style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = if (currentTab == "HEALTH") FontWeight.Bold else FontWeight.Medium
                  )
                )
              },
              colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MedicalBluePrimary,
                selectedTextColor = MedicalBluePrimary,
                indicatorColor = MedicalBlueLight
              ),
              modifier = Modifier.testTag("nav_health")
            )

            NavigationBarItem(
              selected = currentTab == "CASES",
              onClick = { currentTab = "CASES" },
              icon = {
                BadgedBox(
                  badge = {
                    val activeCount = uiState.cases.count { it.status != com.example.model.CaseStatus.RESOLVED }
                    if (activeCount > 0) {
                      Badge(
                        containerColor = ClinicalCoralCritical,
                        contentColor = Color.White
                      ) {
                        Text(
                          "$activeCount",
                          style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.5.sp
                          )
                        )
                      }
                    }
                  }
                ) {
                  Icon(
                    imageVector = if (currentTab == "CASES") Icons.Filled.Assignment else Icons.Outlined.Assignment,
                    contentDescription = "Cases"
                  )
                }
              },
              label = {
                Text(
                  "Cases",
                  style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = if (currentTab == "CASES") FontWeight.Bold else FontWeight.Medium
                  )
                )
              },
              colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MedicalBluePrimary,
                selectedTextColor = MedicalBluePrimary,
                indicatorColor = MedicalBlueLight
              ),
              modifier = Modifier.testTag("nav_cases")
            )

            NavigationBarItem(
              selected = currentTab == "IMMUNE",
              onClick = { currentTab = "IMMUNE" },
              icon = {
                Icon(
                  imageVector = if (currentTab == "IMMUNE") Icons.Filled.Biotech else Icons.Outlined.Biotech,
                  contentDescription = "Immune"
                )
              },
              label = {
                Text(
                  "Immune",
                  style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = if (currentTab == "IMMUNE") FontWeight.Bold else FontWeight.Medium
                  )
                )
              },
              colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MedicalBluePrimary,
                selectedTextColor = MedicalBluePrimary,
                indicatorColor = MedicalBlueLight
              ),
              modifier = Modifier.testTag("nav_immune")
            )

            NavigationBarItem(
              selected = currentTab == "MEMORY",
              onClick = { currentTab = "MEMORY" },
              icon = {
                Icon(
                  imageVector = if (currentTab == "MEMORY") Icons.Filled.Psychology else Icons.Outlined.Psychology,
                  contentDescription = "Memory"
                )
              },
              label = {
                Text(
                  "Memory",
                  style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = if (currentTab == "MEMORY") FontWeight.Bold else FontWeight.Medium
                  )
                )
              },
              colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MemoryLavender,
                selectedTextColor = MemoryLavender,
                indicatorColor = MemoryLavenderLight
              ),
              modifier = Modifier.testTag("nav_memory")
            )
          }
        },
        modifier = modifier.fillMaxSize()
      ) { innerPadding ->
        Box(
          modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
        ) {
          when (currentTab) {
            "HOME" -> HomeScreen(
              uiState = uiState,
              onRunCheck = { viewModel.runDeviceCheck() },
              onSimulateAnomaly = { viewModel.simulateUnusualActivity() },
              onNavigateToTab = { currentTab = it },
              onOpenNotifications = { isNotificationsOpen = true },
              onOpenDiagnostics = { isDiagnosticsOpen = true }
            )

            "HEALTH" -> HealthExamScreen(
              uiState = uiState,
              onIsolateApp = { viewModel.isolateApp(it) },
              onReleaseApp = { viewModel.releaseApp(it) }
            )

            "CASES" -> CasesScreen(
              uiState = uiState,
              onResolveCase = { viewModel.resolveCase(it) },
              onReleaseAppByName = { name ->
                val app = uiState.applications.find { it.name == name }
                if (app != null) viewModel.releaseApp(app.id)
              }
            )

            "IMMUNE" -> ImmuneSystemScreen(
              uiState = uiState,
              onNavigateToNetwork = { currentTab = "HEALTH" }
            )

            "MEMORY" -> MemoryScreen(
              uiState = uiState
            )
          }
        }
      }
    }
  }

// Retained for test compatibility & preview
@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
  Text(text = "Hello $name!", modifier = modifier)
}
