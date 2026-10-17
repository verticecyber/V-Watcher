package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.appTactile
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
      containerColor = WhoopBlackBg,
      bottomBar = {
        // High-contrast WHOOP-inspired bottom navigation bar (Matte dark surface with neon accent indicators)
        Surface(
          color = WhoopSurface,
          border = BorderStroke(1.dp, WhoopSurfaceBorderSubtle),
          tonalElevation = 0.dp,
          shadowElevation = 0.dp,
          modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
        ) {
          NavigationBar(
            containerColor = WhoopSurface,
            contentColor = WhoopTextPrimary,
            tonalElevation = 0.dp,
            windowInsets = WindowInsets(0, 0, 0, 0),
            modifier = Modifier
              .fillMaxWidth()
              .height(64.dp)
              .testTag("bottom_nav_bar")
          ) {
            NavigationBarItem(
              selected = currentTab == "HOME",
              onClick = { currentTab = "HOME" },
              icon = {
                Icon(
                  imageVector = if (currentTab == "HOME") Icons.Filled.HealthAndSafety else Icons.Outlined.HealthAndSafety,
                  contentDescription = "Home",
                  modifier = Modifier.size(22.dp)
                )
              },
              label = {
                Text(
                  "Home",
                  style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = if (currentTab == "HOME") FontWeight.Black else FontWeight.Medium
                  )
                )
              },
              colors = NavigationBarItemDefaults.colors(
                selectedIconColor = WhoopRecoveryGreen,
                selectedTextColor = WhoopRecoveryGreen,
                unselectedIconColor = WhoopTextMuted,
                unselectedTextColor = WhoopTextMuted,
                indicatorColor = WhoopSurfaceElevated
              ),
              modifier = Modifier
                .appTactile(0.92f)
                .testTag("nav_home")
            )

            NavigationBarItem(
              selected = currentTab == "HEALTH",
              onClick = { currentTab = "HEALTH" },
              icon = {
                Icon(
                  imageVector = if (currentTab == "HEALTH") Icons.Filled.FactCheck else Icons.Outlined.FactCheck,
                  contentDescription = "Health & Exam",
                  modifier = Modifier.size(22.dp)
                )
              },
              label = {
                Text(
                  "Exam",
                  style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = if (currentTab == "HEALTH") FontWeight.Black else FontWeight.Medium
                  )
                )
              },
              colors = NavigationBarItemDefaults.colors(
                selectedIconColor = WhoopStrainBlue,
                selectedTextColor = WhoopStrainBlue,
                unselectedIconColor = WhoopTextMuted,
                unselectedTextColor = WhoopTextMuted,
                indicatorColor = WhoopSurfaceElevated
              ),
              modifier = Modifier
                .appTactile(0.92f)
                .testTag("nav_health")
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
                        containerColor = WhoopAlertCoral,
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
                    contentDescription = "Cases",
                    modifier = Modifier.size(22.dp)
                  )
                }
              },
              label = {
                Text(
                  "Cases",
                  style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = if (currentTab == "CASES") FontWeight.Black else FontWeight.Medium
                  )
                )
              },
              colors = NavigationBarItemDefaults.colors(
                selectedIconColor = WhoopWarningAmber,
                selectedTextColor = WhoopWarningAmber,
                unselectedIconColor = WhoopTextMuted,
                unselectedTextColor = WhoopTextMuted,
                indicatorColor = WhoopSurfaceElevated
              ),
              modifier = Modifier
                .appTactile(0.92f)
                .testTag("nav_cases")
            )

            NavigationBarItem(
              selected = currentTab == "IMMUNE",
              onClick = { currentTab = "IMMUNE" },
              icon = {
                Icon(
                  imageVector = if (currentTab == "IMMUNE") Icons.Filled.Biotech else Icons.Outlined.Biotech,
                  contentDescription = "Immune",
                  modifier = Modifier.size(22.dp)
                )
              },
              label = {
                Text(
                  "Immune",
                  style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = if (currentTab == "IMMUNE") FontWeight.Black else FontWeight.Medium
                  )
                )
              },
              colors = NavigationBarItemDefaults.colors(
                selectedIconColor = WhoopSleepTeal,
                selectedTextColor = WhoopSleepTeal,
                unselectedIconColor = WhoopTextMuted,
                unselectedTextColor = WhoopTextMuted,
                indicatorColor = WhoopSurfaceElevated
              ),
              modifier = Modifier
                .appTactile(0.92f)
                .testTag("nav_immune")
            )

            NavigationBarItem(
              selected = currentTab == "MEMORY",
              onClick = { currentTab = "MEMORY" },
              icon = {
                Icon(
                  imageVector = if (currentTab == "MEMORY") Icons.Filled.Psychology else Icons.Outlined.Psychology,
                  contentDescription = "Memory",
                  modifier = Modifier.size(22.dp)
                )
              },
              label = {
                Text(
                  "Memory",
                  style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = if (currentTab == "MEMORY") FontWeight.Black else FontWeight.Medium
                  )
                )
              },
              colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MemoryLavender,
                selectedTextColor = MemoryLavender,
                unselectedIconColor = WhoopTextMuted,
                unselectedTextColor = WhoopTextMuted,
                indicatorColor = WhoopSurfaceElevated
              ),
              modifier = Modifier
                .appTactile(0.92f)
                .testTag("nav_memory")
            )
          }
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
            onOpenDiagnostics = { isDiagnosticsOpen = true },
            onRefreshTelemetry = { viewModel.refreshRealTelemetry() }
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
