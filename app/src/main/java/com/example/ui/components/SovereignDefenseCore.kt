package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin

/**
 * Sovereign Biological Defense Core
 * Unifies the Health Equilibrium Dial with the Live Biomimetic Immune Network & Barrier into a single,
 * authoritative security telemetry cockpit.
 */
@Composable
fun SovereignDefenseCore(
  healthScore: Int,
  condition: DeviceCondition,
  supportingSentence: String,
  cells: List<ImmuneCell>,
  barrierState: BarrierState,
  isObserving: Boolean,
  onCellClick: (ImmuneCell) -> Unit,
  onViewNetworkClick: () -> Unit = {},
  modifier: Modifier = Modifier
) {
  var activeTab by remember { mutableStateOf(0) } // 0 = Dial Overview, 1 = Live Immune Cellular Matrix

  val animatedScore by animateFloatAsState(
    targetValue = healthScore.toFloat(),
    animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
    label = "defense_core_score"
  )

  val infiniteTransition = rememberInfiniteTransition(label = "core_anim")
  val breathScale by infiniteTransition.animateFloat(
    initialValue = 0.98f,
    targetValue = 1.02f,
    animationSpec = infiniteRepeatable(
      animation = tween(2800, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "defense_breath"
  )

  val radarPhase by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(
      animation = tween(3600, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "radar_phase"
  )

  val statusColor by animateColorAsState(
    targetValue = when (condition) {
      DeviceCondition.HEALTHY -> WhoopRecoveryGreen
      DeviceCondition.ATTENTION, DeviceCondition.UNDER_REVIEW -> WhoopWarningAmber
      DeviceCondition.CONTAINING -> WhoopStrainBlue
      DeviceCondition.RECOVERING -> WhoopSleepTeal
    },
    label = "status_color_anim"
  )

  val activeCellsCount = cells.count { it.state != ImmuneCellState.IDLE }

  Card(
    shape = RoundedCornerShape(22.dp),
    colors = CardDefaults.cardColors(containerColor = WhoopSurface),
    border = BorderStroke(1.dp, Color(0x28FFFFFF)),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    modifier = modifier
      .fillMaxWidth()
      .shadow(
        elevation = 8.dp,
        shape = RoundedCornerShape(22.dp),
        ambientColor = Color.Black.copy(alpha = 0.7f),
        spotColor = Color.Black.copy(alpha = 0.7f)
      )
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .background(
          Brush.verticalGradient(
            colors = listOf(
              Color(0xFF161821),
              Color(0xFF0F1116)
            )
          )
        )
        .padding(18.dp)
    ) {
      // 1. Sovereign Header (Cockpit Status & Tab Switcher)
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(if (isObserving) statusColor else WhoopTextMuted)
            )
            Spacer(modifier = Modifier.width(7.dp))
            Text(
              text = "AUTONOMOUS BIO-CORE",
              style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Black,
                letterSpacing = 1.4.sp,
                color = WhoopTextPrimary,
                fontSize = 11.5.sp
              )
            )
          }
          Text(
            text = if (activeTab == 0) "Homeostatic Equilibrium" else "Cellular Agent Network",
            style = MaterialTheme.typography.bodySmall.copy(
              color = WhoopTextSecondary,
              fontSize = 12.sp
            )
          )
        }

        // Segmented Switcher (Dial vs Immune Matrix)
        Surface(
          shape = RoundedCornerShape(10.dp),
          color = Color(0x331E2330),
          border = BorderStroke(1.dp, Color(0x24FFFFFF))
        ) {
          Row(
            modifier = Modifier.padding(3.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
          ) {
            Surface(
              shape = RoundedCornerShape(7.dp),
              color = if (activeTab == 0) Color(0x33FFFFFF) else Color.Transparent,
              modifier = Modifier.clickable { activeTab = 0 }
            ) {
              Text(
                text = "INDEX",
                style = MaterialTheme.typography.labelSmall.copy(
                  fontWeight = if (activeTab == 0) FontWeight.Black else FontWeight.SemiBold,
                  color = if (activeTab == 0) WhoopTextPrimary else WhoopTextMuted,
                  fontSize = 10.sp
                ),
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
              )
            }
            Surface(
              shape = RoundedCornerShape(7.dp),
              color = if (activeTab == 1) Color(0x33FFFFFF) else Color.Transparent,
              modifier = Modifier.clickable { activeTab = 1 }
            ) {
              Text(
                text = "MATRIX",
                style = MaterialTheme.typography.labelSmall.copy(
                  fontWeight = if (activeTab == 1) FontWeight.Black else FontWeight.SemiBold,
                  color = if (activeTab == 1) WhoopTextPrimary else WhoopTextMuted,
                  fontSize = 10.sp
                ),
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // 2. Central Display (Dial or Matrix)
      if (activeTab == 0) {
        // Tab 0: Homeostatic Equilibrium Biometric Dial
        Box(
          contentAlignment = Alignment.Center,
          modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
        ) {
          Canvas(modifier = Modifier.size(190.dp)) {
            val strokeWidth = 14.dp.toPx()
            val radius = (size.minDimension - strokeWidth) / 2f
            val centerOffset = Offset(size.width / 2f, size.height / 2f)

            // Precision calibration tick marks
            val tickCount = 48
            for (i in 0 until tickCount) {
              val angle = (i * (2.0 * Math.PI / tickCount)).toFloat()
              val outerRadius = radius + 8.dp.toPx()
              val isQuarter = i % 12 == 0
              val innerRadius = outerRadius - if (isQuarter) 5.dp.toPx() else 2.5.dp.toPx()
              val startX = centerOffset.x + innerRadius * cos(angle)
              val startY = centerOffset.y + innerRadius * sin(angle)
              val endX = centerOffset.x + outerRadius * cos(angle)
              val endY = centerOffset.y + outerRadius * sin(angle)

              drawLine(
                color = if (isQuarter) Color(0x40FFFFFF) else Color(0x18FFFFFF),
                start = Offset(startX, startY),
                end = Offset(endX, endY),
                strokeWidth = if (isQuarter) 1.5.dp.toPx() else 1.dp.toPx()
              )
            }

            // Dark matte base track
            drawCircle(
              color = Color(0xFF1D212B),
              radius = radius,
              center = centerOffset,
              style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Dynamic progress sweep arc
            val sweepAngle = (animatedScore / 100f) * 360f
            val arcGradient = Brush.sweepGradient(
              colors = listOf(
                statusColor.copy(alpha = 0.5f),
                statusColor,
                statusColor.copy(alpha = 0.9f)
              ),
              center = centerOffset
            )
            drawArc(
              brush = arcGradient,
              startAngle = -90f,
              sweepAngle = sweepAngle,
              useCenter = false,
              style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Core bio-aura circle
            drawCircle(
              color = statusColor.copy(alpha = 0.05f),
              radius = radius * breathScale * 0.82f,
              center = centerOffset
            )
          }

          // Center Metric Readout
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
          ) {
            Text(
              text = "${animatedScore.toInt()}%",
              style = MaterialTheme.typography.displaySmall.copy(
                fontWeight = FontWeight.Black,
                color = WhoopTextPrimary,
                letterSpacing = (-1).sp,
                fontSize = 42.sp
              )
            )
            Surface(
              color = statusColor.copy(alpha = 0.16f),
              border = BorderStroke(1.dp, statusColor.copy(alpha = 0.4f)),
              shape = RoundedCornerShape(12.dp)
            ) {
              Text(
                text = condition.displayName.uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(
                  fontWeight = FontWeight.Black,
                  color = statusColor,
                  fontSize = 10.sp,
                  letterSpacing = 0.8.sp
                ),
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.5.dp)
              )
            }
          }
        }
      } else {
        // Tab 1: Live Biomimetic Immune Cellular Matrix Canvas
        Box(
          contentAlignment = Alignment.Center,
          modifier = Modifier
            .fillMaxWidth()
            .height(230.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF0C0E12))
            .border(1.dp, Color(0x1EFFFFFF), RoundedCornerShape(16.dp))
        ) {
          Canvas(modifier = Modifier.fillMaxSize()) {
            val centerX = size.width / 2f
            val centerY = size.height / 2f
            val orbitRadius = (minOf(size.width, size.height) / 2f) - 40.dp.toPx()

            // Concentric orbit lines
            drawCircle(
              color = Color(0x18FFFFFF),
              radius = orbitRadius,
              center = Offset(centerX, centerY),
              style = Stroke(width = 1.dp.toPx())
            )
            drawCircle(
              color = Color(0x0EFFFFFF),
              radius = orbitRadius * 0.55f,
              center = Offset(centerX, centerY),
              style = Stroke(width = 1.dp.toPx())
            )

            // Radar scan beam
            val scanAngle = radarPhase * (2.0 * Math.PI)
            val beamEndX = centerX + orbitRadius * cos(scanAngle).toFloat()
            val beamEndY = centerY + orbitRadius * sin(scanAngle).toFloat()
            drawLine(
              color = WhoopStrainBlue.copy(alpha = 0.35f),
              start = Offset(centerX, centerY),
              end = Offset(beamEndX, beamEndY),
              strokeWidth = 1.5.dp.toPx()
            )

            // Central Node
            drawCircle(
              color = WhoopSurfaceElevated,
              radius = 24.dp.toPx(),
              center = Offset(centerX, centerY)
            )
            drawCircle(
              color = WhoopStrainBlue.copy(alpha = 0.6f),
              radius = 24.dp.toPx(),
              center = Offset(centerX, centerY),
              style = Stroke(width = 1.5.dp.toPx())
            )
          }

          // Center Device Shield Label
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
          ) {
            Icon(
              imageVector = Icons.Default.Shield,
              contentDescription = null,
              tint = WhoopTextPrimary,
              modifier = Modifier.size(16.dp)
            )
            Text(
              text = "OS SHIELD",
              style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Black,
                color = WhoopTextPrimary,
                fontSize = 8.5.sp,
                letterSpacing = 0.8.sp
              )
            )
          }

          // Interactive Circular Cellular Nodes
          BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val density = LocalDensity.current
            val widthPx = constraints.maxWidth.toFloat()
            val heightPx = constraints.maxHeight.toFloat()
            val centerX = widthPx / 2f
            val centerY = heightPx / 2f
            val orbitRadius = (minOf(widthPx, heightPx) / 2f) - with(density) { 40.dp.toPx() }
            val nodeSize = 34.dp

            cells.take(8).forEachIndexed { index, cell ->
              val angleRad = (index * (2.0 * Math.PI / minOf(cells.size, 8))) - (Math.PI / 2.0)
              val nodeX = centerX + (orbitRadius * cos(angleRad)).toFloat()
              val nodeY = centerY + (orbitRadius * sin(angleRad)).toFloat()

              val xDp = with(density) { nodeX.toDp() } - (nodeSize / 2)
              val yDp = with(density) { nodeY.toDp() } - (nodeSize / 2)

              val isAlert = cell.state != ImmuneCellState.IDLE && cell.state != ImmuneCellState.MONITORING
              val cellColor = if (isAlert) WhoopAlertCoral else WhoopStrainBlue

              Surface(
                shape = CircleShape,
                color = Color(0x331E2330),
                border = BorderStroke(1.dp, cellColor.copy(alpha = 0.65f)),
                modifier = Modifier
                  .offset(x = xDp, y = yDp)
                  .size(nodeSize)
                  .clickable { onCellClick(cell) }
              ) {
                Box(contentAlignment = Alignment.Center) {
                  Text(
                    text = cell.name.take(2),
                    style = MaterialTheme.typography.labelSmall.copy(
                      fontWeight = FontWeight.Black,
                      color = cellColor,
                      fontSize = 10.sp
                    )
                  )
                }
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // 3. Biomimetic Telemetry Strip (Conduits, Barrier & Activity)
      Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color(0x221E2330),
        border = BorderStroke(1.dp, Color(0x18FFFFFF)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 10.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
          ) {
            val barrierColor = if (barrierState == BarrierState.STABLE || barrierState == BarrierState.WATCHING) WhoopRecoveryGreen else WhoopWarningAmber
            Icon(
              imageVector = Icons.Outlined.Security,
              contentDescription = null,
              tint = barrierColor,
              modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
              Text(
                text = "BARRIER: ${barrierState.displayName.uppercase()}",
                style = MaterialTheme.typography.labelSmall.copy(
                  fontWeight = FontWeight.Bold,
                  color = WhoopTextPrimary,
                  fontSize = 10.5.sp,
                  letterSpacing = 0.5.sp
                )
              )
              Text(
                text = "${cells.size} total cells • $activeCellsCount actively responding",
                style = MaterialTheme.typography.bodySmall.copy(
                  color = WhoopTextSecondary,
                  fontSize = 11.sp
                )
              )
            }
          }

          Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color(0x2800B4D8),
            border = BorderStroke(1.dp, WhoopStrainBlue.copy(alpha = 0.4f)),
            modifier = Modifier.clickable { onViewNetworkClick() }
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
              Text(
                text = "Topologia",
                style = MaterialTheme.typography.labelSmall.copy(
                  fontWeight = FontWeight.Black,
                  color = WhoopStrainBlue,
                  fontSize = 10.sp
                )
              )
              Spacer(modifier = Modifier.width(3.dp))
              Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = WhoopStrainBlue,
                modifier = Modifier.size(12.dp)
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // 4. Clinical Verdict Line
      Text(
        text = supportingSentence,
        style = MaterialTheme.typography.bodySmall.copy(
          color = WhoopTextSecondary,
          fontSize = 12.sp,
          lineHeight = 16.sp,
          textAlign = TextAlign.Start
        )
      )
    }
  }
}
