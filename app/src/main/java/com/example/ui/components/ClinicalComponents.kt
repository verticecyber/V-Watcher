package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
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
 * Interactive tactile bounce physics modifier for native app feel
 */
@Composable
fun Modifier.appTactile(
  scaleDown: Float = 0.96f
): Modifier {
  val interactionSource = remember { MutableInteractionSource() }
  val isPressed by interactionSource.collectIsPressedAsState()
  val scale by animateFloatAsState(
    targetValue = if (isPressed) scaleDown else 1f,
    animationSpec = spring(
      dampingRatio = Spring.DampingRatioMediumBouncy,
      stiffness = Spring.StiffnessLow
    ),
    label = "app_tactile_scale"
  )
  return this.graphicsLayer {
    scaleX = scale
    scaleY = scale
  }
}

/**
 * Animated real-time bio-signal telemetry heartbeat radar strip
 */
@Composable
fun BioSignalPulseStrip(
  isObserving: Boolean = true,
  condition: DeviceCondition = DeviceCondition.HEALTHY,
  modifier: Modifier = Modifier
) {
  val infiniteTransition = rememberInfiniteTransition(label = "ecg_wave")
  val phaseOffset by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(
      animation = tween(2600, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "ecg_phase"
  )

  val lineColor = when (condition) {
    DeviceCondition.HEALTHY -> WhoopRecoveryGreen
    DeviceCondition.ATTENTION, DeviceCondition.UNDER_REVIEW -> WhoopWarningAmber
    DeviceCondition.CONTAINING -> WhoopStrainBlue
    DeviceCondition.RECOVERING -> WhoopSleepTeal
  }

  Surface(
    shape = RoundedCornerShape(14.dp),
    color = WhoopSurface,
    border = BorderStroke(1.dp, Color(0x28FFFFFF)),
    shadowElevation = 2.dp,
    modifier = modifier
      .fillMaxWidth()
      .height(36.dp)
      .shadow(
        elevation = 4.dp,
        shape = RoundedCornerShape(14.dp),
        ambientColor = Color.Black.copy(alpha = 0.5f),
        spotColor = Color.Black.copy(alpha = 0.5f)
      )
  ) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(
          Brush.horizontalGradient(
            colors = listOf(
              Color(0xFF141720),
              Color(0xFF191D28),
              Color(0xFF141720)
            )
          )
        )
    ) {
      Row(
        modifier = Modifier
          .fillMaxSize()
          .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          val pulseDotAlpha by infiniteTransition.animateFloat(
            initialValue = 0.4f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
              animation = tween(900, easing = FastOutSlowInEasing),
              repeatMode = RepeatMode.Reverse
            ),
            label = "pulse_dot_alpha"
          )
          Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(12.dp)
          ) {
            Box(
              modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(lineColor.copy(alpha = pulseDotAlpha * 0.35f))
            )
            Box(
              modifier = Modifier
                .size(5.dp)
                .clip(CircleShape)
                .background(lineColor)
            )
          }
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "TELEMETRIA AO VIVO",
            style = MaterialTheme.typography.labelSmall.copy(
              fontSize = 9.sp,
              fontWeight = FontWeight.Black,
              letterSpacing = 1.1.sp,
              color = WhoopTextSecondary
            )
          )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Canvas(
          modifier = Modifier
            .weight(1f)
            .height(20.dp)
        ) {
          val w = size.width
          val h = size.height
          val midY = h / 2f
          val path = Path()
          val segments = 24
          for (i in 0..segments) {
            val x = (i.toFloat() / segments) * w
            val normalized = ((i.toFloat() / segments) + phaseOffset) % 1f
            val y = when {
              normalized in 0.38f..0.42f -> midY - h * 0.44f
              normalized in 0.42f..0.47f -> midY + h * 0.46f
              normalized in 0.47f..0.52f -> midY - h * 0.30f
              else -> midY + (sin((normalized * 2.0 * Math.PI).toFloat()) * h * 0.08f)
            }
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
          }
          // Ambient glow behind line
          drawPath(
            path = path,
            color = lineColor.copy(alpha = 0.25f),
            style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
          )
          // Crisp foreground line
          drawPath(
            path = path,
            color = lineColor.copy(alpha = 0.95f),
            style = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round)
          )
        }
      }
    }
  }
}

/**
 * Ultra-refined clinical top bar with live status heartbeat & quick app controls
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClinicalTopBar(
  onNotificationsClick: () -> Unit,
  notificationCount: Int,
  onDiagnosticsClick: () -> Unit = {},
  onRefreshTelemetry: () -> Unit = {},
  modifier: Modifier = Modifier
) {
  val infiniteTransition = rememberInfiniteTransition(label = "top_bar_pulse")
  val pulseAlpha by infiniteTransition.animateFloat(
    initialValue = 0.35f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(
      animation = tween(1800, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "status_dot_alpha"
  )

  var refreshRotation by remember { mutableFloatStateOf(0f) }
  val animatedRotation by animateFloatAsState(
    targetValue = refreshRotation,
    animationSpec = spring(
      dampingRatio = Spring.DampingRatioMediumBouncy,
      stiffness = Spring.StiffnessLow
    ),
    label = "refresh_rotation"
  )

  Surface(
    color = WhoopBlackBg,
    border = BorderStroke(1.dp, Color(0x1AFFFFFF)),
    shadowElevation = 0.dp,
    modifier = modifier.fillMaxWidth()
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .background(
          Brush.verticalGradient(
            colors = listOf(
              Color(0xFF161922),
              Color(0xFF0F1117)
            )
          )
        )
        .padding(horizontal = 16.dp, vertical = 12.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
          contentAlignment = Alignment.Center,
          modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(13.dp))
            .background(Color(0x331F2937))
            .border(1.dp, Color(0x2EFFFFFF), RoundedCornerShape(13.dp))
            .shadow(4.dp, RoundedCornerShape(13.dp), ambientColor = Color.Black.copy(alpha = 0.4f), spotColor = Color.Black.copy(alpha = 0.4f))
        ) {
          Box(
            modifier = Modifier
              .size(28.dp)
              .clip(CircleShape)
              .background(WhoopRecoveryGreen.copy(alpha = pulseAlpha * 0.25f))
          )
          Icon(
            imageVector = Icons.Filled.Shield,
            contentDescription = null,
            tint = WhoopRecoveryGreen,
            modifier = Modifier.size(20.dp)
          )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = "V-WATCHER",
              style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Black,
                letterSpacing = 1.4.sp,
                color = WhoopTextPrimary
              )
            )
            Spacer(modifier = Modifier.width(6.dp))
            Surface(
              color = Color(0x2800B4D8),
              border = BorderStroke(1.dp, WhoopStrainBlue.copy(alpha = 0.4f)),
              shape = RoundedCornerShape(6.dp)
            ) {
              Text(
                text = "PRO SENSOR",
                style = MaterialTheme.typography.labelSmall.copy(
                  fontSize = 8.5.sp,
                  fontWeight = FontWeight.Bold,
                  color = WhoopStrainBlue,
                  letterSpacing = 0.6.sp
                ),
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
              )
            }
          }
          Text(
            text = "Autonomous Device Telemetry",
            style = MaterialTheme.typography.bodySmall.copy(
              color = WhoopTextSecondary,
              fontSize = 11.sp
            )
          )
        }
      }

      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        // Quick Telemetry Refresh Button with 360 rotation animation
        Surface(
          shape = CircleShape,
          color = Color(0x331E2330),
          border = BorderStroke(1.dp, Color(0x26FFFFFF)),
          modifier = Modifier
            .appTactile(0.92f)
            .shadow(2.dp, CircleShape, ambientColor = Color.Black.copy(alpha = 0.4f), spotColor = Color.Black.copy(alpha = 0.4f))
        ) {
          IconButton(
            onClick = {
              refreshRotation += 360f
              onRefreshTelemetry()
            },
            modifier = Modifier
              .size(38.dp)
              .graphicsLayer { rotationZ = animatedRotation }
              .testTag("refresh_telemetry_top_button")
          ) {
            Icon(
              imageVector = Icons.Outlined.Refresh,
              contentDescription = "Atualizar Telemetria",
              tint = WhoopTextPrimary,
              modifier = Modifier.size(19.dp)
            )
          }
        }

        // Technical Diagnostics Button
        Surface(
          shape = CircleShape,
          color = Color(0x331E2330),
          border = BorderStroke(1.dp, Color(0x26FFFFFF)),
          modifier = Modifier
            .appTactile(0.92f)
            .shadow(2.dp, CircleShape, ambientColor = Color.Black.copy(alpha = 0.4f), spotColor = Color.Black.copy(alpha = 0.4f))
        ) {
          IconButton(
            onClick = onDiagnosticsClick,
            modifier = Modifier
              .size(38.dp)
              .testTag("diagnostics_top_button")
          ) {
            Icon(
              imageVector = Icons.Outlined.Analytics,
              contentDescription = "Technical Diagnostics",
              tint = WhoopStrainBlue,
              modifier = Modifier.size(19.dp)
            )
          }
        }

        // Notifications Button
        Surface(
          shape = CircleShape,
          color = Color(0x331E2330),
          border = BorderStroke(1.dp, Color(0x26FFFFFF)),
          modifier = Modifier
            .appTactile(0.92f)
            .shadow(2.dp, CircleShape, ambientColor = Color.Black.copy(alpha = 0.4f), spotColor = Color.Black.copy(alpha = 0.4f))
        ) {
          IconButton(
            onClick = onNotificationsClick,
            modifier = Modifier
              .size(38.dp)
              .testTag("notifications_button")
          ) {
            BadgedBox(
              badge = {
                if (notificationCount > 0) {
                  Badge(
                    containerColor = WhoopAlertCoral,
                    contentColor = Color.White
                  ) {
                    Text(text = notificationCount.toString(), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                  }
                }
              }
            ) {
              Icon(
                imageVector = Icons.Outlined.Notifications,
                contentDescription = "Clinical notifications",
                tint = WhoopTextPrimary,
                modifier = Modifier.size(19.dp)
              )
            }
          }
        }
      }
    }
  }
}

/**
 * Premium Clinical Circular Health Score Gauge with ECG waveform & concentric aura
 */
@Composable
fun CircularHealthScoreGauge(
  score: Int,
  condition: DeviceCondition,
  supportingSentence: String,
  modifier: Modifier = Modifier
) {
  val animatedScore by animateFloatAsState(
    targetValue = score.toFloat(),
    animationSpec = tween(durationMillis = 1100, easing = FastOutSlowInEasing),
    label = "score_anim"
  )

  val infiniteTransition = rememberInfiniteTransition(label = "gauge_breathe")
  val breathScale by infiniteTransition.animateFloat(
    initialValue = 0.985f,
    targetValue = 1.015f,
    animationSpec = infiniteRepeatable(
      animation = tween(3200, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "gauge_breath_scale"
  )

  val gaugeColor by animateColorAsState(
    targetValue = when (condition) {
      DeviceCondition.HEALTHY -> WhoopRecoveryGreen
      DeviceCondition.ATTENTION, DeviceCondition.UNDER_REVIEW -> WhoopWarningAmber
      DeviceCondition.CONTAINING -> WhoopStrainBlue
      DeviceCondition.RECOVERING -> WhoopSleepTeal
    },
    label = "gauge_color_anim"
  )

  val gaugeGlowColor = when (condition) {
    DeviceCondition.HEALTHY -> WhoopRecoveryGreenGlow
    DeviceCondition.ATTENTION, DeviceCondition.UNDER_REVIEW -> WhoopWarningAmberGlow
    DeviceCondition.CONTAINING -> WhoopStrainBlueGlow
    DeviceCondition.RECOVERING -> WhoopSleepTeal
  }

  // WHOOP & Apple Signature Biometric Card Container
  Card(
    shape = RoundedCornerShape(24.dp),
    colors = CardDefaults.cardColors(containerColor = WhoopSurface),
    border = BorderStroke(1.dp, Color(0x2EFFFFFF)),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    modifier = modifier
      .fillMaxWidth()
      .shadow(
        elevation = 8.dp,
        shape = RoundedCornerShape(24.dp),
        ambientColor = Color.Black.copy(alpha = 0.65f),
        spotColor = Color.Black.copy(alpha = 0.65f)
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
        .padding(horizontal = 20.dp, vertical = 22.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      // WHOOP-style metric header with status pill
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "PHYSIOLOGICAL STATUS",
            style = MaterialTheme.typography.labelSmall.copy(
              fontWeight = FontWeight.Bold,
              letterSpacing = 1.4.sp,
              color = WhoopTextSecondary,
              fontSize = 11.sp
            )
          )
          Text(
            text = "IMMUNE RECOVERY DIAL",
            style = MaterialTheme.typography.titleSmall.copy(
              fontWeight = FontWeight.Black,
              letterSpacing = 0.5.sp,
              color = WhoopTextPrimary,
              fontSize = 14.sp
            )
          )
        }

        Surface(
          color = gaugeColor.copy(alpha = 0.15f),
          border = BorderStroke(1.dp, gaugeColor.copy(alpha = 0.4f)),
          shape = RoundedCornerShape(20.dp)
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
          ) {
            Box(
              modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(gaugeColor)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = condition.displayName.uppercase(),
              style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = gaugeColor,
                fontSize = 10.5.sp,
                letterSpacing = 0.6.sp
              )
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(20.dp))

      // WHOOP Precision Circular Dial with Dark Matte Track and High-Luminance Glow
      Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(210.dp)
      ) {
        Canvas(modifier = Modifier.size(195.dp)) {
          val strokeWidth = 16.dp.toPx()
          val radius = (size.minDimension - strokeWidth) / 2f
          val centerOffset = Offset(size.width / 2f, size.height / 2f)

          // 1. Precise calibration markers (WHOOP tick marks)
          val tickCount = 60
          for (i in 0 until tickCount) {
            val angle = (i * (2.0 * Math.PI / tickCount)).toFloat()
            val outerRadius = radius + 12.dp.toPx()
            val isMajor = i % 5 == 0
            val innerRadius = outerRadius - if (isMajor) 6.dp.toPx() else 3.dp.toPx()
            val startX = centerOffset.x + innerRadius * cos(angle)
            val startY = centerOffset.y + innerRadius * sin(angle)
            val endX = centerOffset.x + outerRadius * cos(angle)
            val endY = centerOffset.y + outerRadius * sin(angle)

            drawLine(
              color = if (isMajor) WhoopSurfaceBorder.copy(alpha = 0.9f) else WhoopSurfaceBorderSubtle.copy(alpha = 0.6f),
              start = Offset(startX, startY),
              end = Offset(endX, endY),
              strokeWidth = if (isMajor) 1.5.dp.toPx() else 1.dp.toPx()
            )
          }

          // 2. Ambient neon glow behind the progress arc (WHOOP style glow on dark surface)
          drawCircle(
            color = gaugeColor.copy(alpha = 0.08f),
            radius = radius * breathScale,
            center = centerOffset,
            style = Stroke(width = strokeWidth * 1.8f)
          )

          // 3. Dark Matte Base Track
          drawCircle(
            color = WhoopSurfaceElevated,
            radius = radius,
            center = centerOffset,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
          )

          // 4. Vibrant Biometric Recovery Arc
          val sweepAngle = (animatedScore / 100f) * 360f
          val gradient = Brush.sweepGradient(
            colors = listOf(
              gaugeColor.copy(alpha = 0.75f),
              gaugeGlowColor,
              gaugeColor
            ),
            center = centerOffset
          )

          drawArc(
            brush = gradient,
            startAngle = -90f,
            sweepAngle = sweepAngle,
            useCenter = false,
            topLeft = Offset(centerOffset.x - radius, centerOffset.y - radius),
            size = Size(radius * 2f, radius * 2f),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
          )

          // 5. Subtle concentric inner track divider
          drawCircle(
            color = WhoopSurfaceBorderSubtle,
            radius = radius - strokeWidth - 5.dp.toPx(),
            center = centerOffset,
            style = Stroke(width = 1.dp.toPx())
          )
        }

        // Central Score & Units with bold athletic display typography
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          modifier = Modifier.padding(bottom = 4.dp)
        ) {
          Row(
            verticalAlignment = Alignment.Bottom,
            modifier = Modifier.padding(top = 4.dp)
          ) {
            Text(
              text = "${animatedScore.toInt()}",
              style = MaterialTheme.typography.displayLarge.copy(
                fontWeight = FontWeight.Black,
                color = WhoopTextPrimary,
                fontSize = 54.sp,
                letterSpacing = (-2).sp
              )
            )
            Text(
              text = "%",
              style = MaterialTheme.typography.titleLarge.copy(
                color = gaugeColor,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
              ),
              modifier = Modifier.padding(bottom = 10.dp, start = 3.dp)
            )
          }

          // Mini ECG pulse line in accent color
          Canvas(
            modifier = Modifier
              .width(68.dp)
              .height(16.dp)
              .padding(vertical = 1.dp)
          ) {
            val path = Path().apply {
              moveTo(0f, size.height / 2f)
              lineTo(size.width * 0.25f, size.height / 2f)
              lineTo(size.width * 0.35f, size.height * 0.15f)
              lineTo(size.width * 0.45f, size.height * 0.85f)
              lineTo(size.width * 0.55f, size.height * 0.05f)
              lineTo(size.width * 0.65f, size.height * 0.75f)
              lineTo(size.width * 0.75f, size.height / 2f)
              lineTo(size.width, size.height / 2f)
            }
            drawPath(
              path = path,
              color = gaugeColor,
              style = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round)
            )
          }

          Text(
            text = "EQUILIBRIUM",
            style = MaterialTheme.typography.labelSmall.copy(
              color = WhoopTextMuted,
              fontWeight = FontWeight.Bold,
              fontSize = 9.5.sp,
              letterSpacing = 1.8.sp
            )
          )
        }
      }

      Spacer(modifier = Modifier.height(18.dp))

      // WHOOP-Style 3-Metric Pillar Strip (Strain / Recovery / Barrier)
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(14.dp))
          .background(WhoopSurfaceElevated)
          .border(1.dp, WhoopSurfaceBorderSubtle, RoundedCornerShape(14.dp))
          .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
          Text(
            text = "SYSTEM STRAIN",
            style = MaterialTheme.typography.labelSmall.copy(
              color = WhoopTextMuted,
              fontSize = 8.5.sp,
              fontWeight = FontWeight.Bold,
              letterSpacing = 0.8.sp
            )
          )
          Text(
            text = "0.2 / 21.0",
            style = MaterialTheme.typography.titleSmall.copy(
              color = WhoopStrainBlue,
              fontSize = 13.sp,
              fontWeight = FontWeight.Black
            )
          )
        }

        Box(modifier = Modifier.width(1.dp).height(24.dp).background(WhoopSurfaceBorder))

        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
          Text(
            text = "RECOVERY",
            style = MaterialTheme.typography.labelSmall.copy(
              color = WhoopTextMuted,
              fontSize = 8.5.sp,
              fontWeight = FontWeight.Bold,
              letterSpacing = 0.8.sp
            )
          )
          Text(
            text = "${score}%",
            style = MaterialTheme.typography.titleSmall.copy(
              color = gaugeColor,
              fontSize = 13.sp,
              fontWeight = FontWeight.Black
            )
          )
        }

        Box(modifier = Modifier.width(1.dp).height(24.dp).background(WhoopSurfaceBorder))

        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
          Text(
            text = "SANDBOX",
            style = MaterialTheme.typography.labelSmall.copy(
              color = WhoopTextMuted,
              fontSize = 8.5.sp,
              fontWeight = FontWeight.Bold,
              letterSpacing = 0.8.sp
            )
          )
          Text(
            text = "ARMED",
            style = MaterialTheme.typography.titleSmall.copy(
              color = WhoopSleepTeal,
              fontSize = 13.sp,
              fontWeight = FontWeight.Black
            )
          )
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Direct supporting clinical verdict pill
      Surface(
        color = gaugeColor.copy(alpha = 0.10f),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, gaugeColor.copy(alpha = 0.25f)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 14.dp, vertical = 11.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.Center
        ) {
          Icon(
            imageVector = if (condition == DeviceCondition.HEALTHY) Icons.Default.CheckCircle else Icons.Default.Info,
            contentDescription = null,
            tint = gaugeColor,
            modifier = Modifier.size(17.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = supportingSentence,
            style = MaterialTheme.typography.bodyMedium.copy(
              color = WhoopTextPrimary,
              fontWeight = FontWeight.Medium,
              fontSize = 13.sp,
              lineHeight = 18.sp
            ),
            textAlign = TextAlign.Center
          )
        }
      }
    }
  }
}

/**
 * Modern Vital Sign Card with telemetry sparkline & live pulse dot
 */
@Composable
fun VitalSignCard(
  vital: DeviceVitalSign,
  modifier: Modifier = Modifier
) {
  val pulseAlpha = 0.8f
  val statusColor = if (vital.isNormal) WhoopRecoveryGreen else WhoopWarningAmber

  Card(
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(containerColor = WhoopSurface),
    border = BorderStroke(1.dp, Color(0x24FFFFFF)),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    modifier = modifier
      .fillMaxWidth()
      .shadow(
        elevation = 4.dp,
        shape = RoundedCornerShape(18.dp),
        ambientColor = Color.Black.copy(alpha = 0.5f),
        spotColor = Color.Black.copy(alpha = 0.5f)
      )
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .background(
          Brush.verticalGradient(
            colors = listOf(
              Color(0xFF191D28),
              Color(0xFF13151C)
            )
          )
        )
        .padding(16.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = vital.title,
          style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.9.sp,
            color = WhoopTextSecondary,
            fontSize = 11.5.sp
          )
        )
        Box(
          contentAlignment = Alignment.Center,
          modifier = Modifier.size(14.dp)
        ) {
          Box(
            modifier = Modifier
              .size(12.dp)
              .clip(CircleShape)
              .background(statusColor.copy(alpha = pulseAlpha * 0.35f))
          )
          Box(
            modifier = Modifier
              .size(6.dp)
              .clip(CircleShape)
              .background(statusColor)
          )
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = vital.status,
          style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.Black,
            color = statusColor,
            fontSize = 16.sp
          )
        )
        Surface(
          color = Color(0x331E2330),
          border = BorderStroke(1.dp, Color(0x20FFFFFF)),
          shape = RoundedCornerShape(6.dp)
        ) {
          Text(
            text = vital.trendLabel,
            style = MaterialTheme.typography.labelSmall.copy(
              color = WhoopTextSecondary,
              fontSize = 10.sp,
              fontWeight = FontWeight.SemiBold
            ),
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.5.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(4.dp))

      Text(
        text = vital.subtitle,
        style = MaterialTheme.typography.bodySmall.copy(
          color = WhoopTextSecondary,
          fontSize = 12.sp,
          lineHeight = 16.sp
        ),
        maxLines = 2
      )

      Spacer(modifier = Modifier.height(10.dp))

      // Minimal sparkline
      Canvas(
        modifier = Modifier
          .fillMaxWidth()
          .height(14.dp)
      ) {
        val path = Path().apply {
          moveTo(0f, size.height * 0.6f)
          lineTo(size.width * 0.2f, size.height * 0.6f)
          lineTo(size.width * 0.35f, size.height * 0.2f)
          lineTo(size.width * 0.5f, size.height * 0.8f)
          lineTo(size.width * 0.65f, size.height * 0.3f)
          lineTo(size.width * 0.8f, size.height * 0.6f)
          lineTo(size.width, size.height * 0.6f)
        }
        drawPath(
          path = path,
          color = statusColor.copy(alpha = 0.3f),
          style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )
        drawPath(
          path = path,
          color = statusColor.copy(alpha = 0.9f),
          style = Stroke(width = 1.6.dp.toPx(), cap = StrokeCap.Round)
        )
      }

      Spacer(modifier = Modifier.height(8.dp))

      Text(
        text = "Updated ${vital.updatedAgo}",
        style = MaterialTheme.typography.labelSmall.copy(
          color = WhoopTextMuted,
          fontSize = 10.5.sp
        )
      )
    }
  }
}

/**
 * Clinical summary written with physician-grade calm authority in WHOOP & Apple athletic precision
 */
@Composable
fun DoctorClinicalSummarySection(
  clinicalAssessment: String,
  lastAssessment: String,
  modifier: Modifier = Modifier
) {
  Card(
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(containerColor = WhoopSurface),
    border = BorderStroke(1.dp, Color(0x28FFFFFF)),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    modifier = modifier
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
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(34.dp)
              .clip(RoundedCornerShape(10.dp))
              .background(Color(0x3300B4D8))
              .border(1.dp, WhoopStrainBlue.copy(alpha = 0.4f), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.HealthAndSafety,
              contentDescription = null,
              tint = WhoopStrainBlue,
              modifier = Modifier.size(19.dp)
            )
          }
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Text(
              text = "INTELLIGENCE COACHING",
              style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Black,
                letterSpacing = 1.2.sp,
                color = WhoopStrainBlue,
                fontSize = 11.sp
              )
            )
            Text(
              text = "Autonomous Defense Advisor",
              style = MaterialTheme.typography.labelSmall.copy(
                color = WhoopTextMuted,
                fontSize = 9.5.sp
              )
            )
          }
        }

        Surface(
          color = Color(0x331E2330),
          border = BorderStroke(1.dp, Color(0x24FFFFFF)),
          shape = RoundedCornerShape(8.dp)
        ) {
          Text(
            text = "Assessed $lastAssessment",
            style = MaterialTheme.typography.labelSmall.copy(
              color = WhoopTextSecondary,
              fontSize = 10.5.sp,
              fontWeight = FontWeight.Medium
            ),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      Text(
        text = "\"$clinicalAssessment\"",
        style = MaterialTheme.typography.bodyMedium.copy(
          color = WhoopTextPrimary,
          lineHeight = 22.sp,
          fontWeight = FontWeight.Normal,
          fontSize = 13.5.sp
        )
      )

      Spacer(modifier = Modifier.height(14.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Outlined.Shield,
            contentDescription = null,
            tint = WhoopStrainBlue,
            modifier = Modifier.size(13.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = "Deterministic On-Device Core",
            style = MaterialTheme.typography.labelSmall.copy(
              color = WhoopTextSecondary,
              fontSize = 10.sp,
              fontWeight = FontWeight.Medium
            )
          )
        }

        Text(
          text = "Zero Cloud Leakage",
          style = MaterialTheme.typography.labelSmall.copy(
            color = WhoopRecoveryGreen,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold
          )
        )
      }
    }
  }
}

/**
 * Biological Protective Barrier Card with concentric pulsating bio-membrane
 */
@Composable
fun ProtectiveBarrierCard(
  state: BarrierState,
  modifier: Modifier = Modifier,
  onViewNetworkClick: () -> Unit
) {
  val infiniteTransition = rememberInfiniteTransition(label = "barrier_membrane")
  val wavePulse1 by infiniteTransition.animateFloat(
    initialValue = 0.88f,
    targetValue = 1.12f,
    animationSpec = infiniteRepeatable(
      animation = tween(2800, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "wave_1"
  )
  val wavePulse2 by infiniteTransition.animateFloat(
    initialValue = 1.08f,
    targetValue = 0.92f,
    animationSpec = infiniteRepeatable(
      animation = tween(2200, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "wave_2"
  )

  val stateColor = when (state) {
    BarrierState.WATCHING -> MedicalTealSecondary
    BarrierState.DEFENDING -> ClinicalAmberAttention
    BarrierState.CONTAINING -> ClinicalCoralCritical
    BarrierState.STABLE -> ClinicalGreenHealthy
  }

  Card(
    shape = RoundedCornerShape(22.dp),
    colors = CardDefaults.cardColors(containerColor = ClinicalSurface),
    border = BorderStroke(1.dp, ClinicalOutline),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    modifier = modifier.fillMaxWidth()
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(20.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Outlined.Security,
              contentDescription = null,
              tint = stateColor,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "PROTECTIVE BARRIER",
              style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = ClinicalTextSecondary,
                fontSize = 11.5.sp
              )
            )
          }
          Text(
            text = "Biomimetic Defense Membrane",
            style = MaterialTheme.typography.bodySmall.copy(color = ClinicalTextMuted, fontSize = 11.sp)
          )
        }

        Surface(
          color = stateColor.copy(alpha = 0.12f),
          border = BorderStroke(0.8.dp, stateColor.copy(alpha = 0.35f)),
          shape = RoundedCornerShape(12.dp)
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
          ) {
            Box(
              modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(stateColor)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = state.displayName.uppercase(),
              style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = stateColor,
                fontSize = 10.sp,
                letterSpacing = 0.5.sp
              )
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Visual Biological Membrane Graphic
      Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
          .fillMaxWidth()
          .height(115.dp)
          .clip(RoundedCornerShape(16.dp))
          .background(ClinicalBackground)
      ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
          val centerX = size.width / 2f
          val centerY = size.height / 2f

          // Outer radiating wave 1
          drawCircle(
            color = stateColor.copy(alpha = 0.12f),
            radius = 52.dp.toPx() * wavePulse1,
            center = Offset(centerX, centerY),
            style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
          )

          // Outer radiating wave 2
          drawCircle(
            color = stateColor.copy(alpha = 0.22f),
            radius = 40.dp.toPx() * wavePulse2,
            center = Offset(centerX, centerY),
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
          )

          // Inner membrane boundary
          drawCircle(
            color = stateColor.copy(alpha = 0.4f),
            radius = 28.dp.toPx(),
            center = Offset(centerX, centerY),
            style = Stroke(width = 2.dp.toPx())
          )

          // Central Device Core Node with Gradient
          val coreGradient = Brush.radialGradient(
            colors = listOf(stateColor, MedicalBlueDark),
            center = Offset(centerX, centerY),
            radius = 18.dp.toPx()
          )
          drawCircle(
            brush = coreGradient,
            radius = 16.dp.toPx(),
            center = Offset(centerX, centerY)
          )
        }

        Text(
          text = "CORE",
          style = MaterialTheme.typography.labelSmall.copy(
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 9.sp,
            letterSpacing = 1.sp
          )
        )
      }

      Spacer(modifier = Modifier.height(12.dp))

      Text(
        text = state.clinicalDescription,
        style = MaterialTheme.typography.bodySmall.copy(
          color = ClinicalTextSecondary,
          fontSize = 12.sp,
          lineHeight = 17.sp
        )
      )

      Spacer(modifier = Modifier.height(12.dp))

      // Causal Flow Sequence Chips
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        listOf("Activity", "Detect", "Classify", "Isolate", "Memory").forEachIndexed { idx, step ->
          Surface(
            color = if (idx == 0) MedicalBlueLight else ClinicalSurfaceVariant,
            shape = RoundedCornerShape(6.dp)
          ) {
            Text(
              text = step,
              style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 9.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (idx == 0) MedicalBlueDark else ClinicalTextSecondary
              ),
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
            )
          }
          if (idx < 4) {
            Icon(
              imageVector = Icons.Default.ChevronRight,
              contentDescription = null,
              tint = ClinicalTextMuted,
              modifier = Modifier.size(12.dp)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      OutlinedButton(
        onClick = onViewNetworkClick,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, MedicalBlueLight),
        colors = ButtonDefaults.outlinedButtonColors(
          containerColor = MedicalBlueContainer.copy(alpha = 0.35f),
          contentColor = MedicalBluePrimary
        ),
        modifier = Modifier
          .fillMaxWidth()
          .height(48.dp)
          .testTag("view_network_health_button")
      ) {
        Icon(Icons.Outlined.Dns, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "Inspect Network Telemetry & Conduits",
          style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
        )
      }
    }
  }
}

/**
 * Biological Immune Network Topology Canvas
 */
@Composable
fun BiologicalImmuneNetworkCanvas(
  cells: List<ImmuneCell>,
  barrierState: BarrierState,
  onCellClick: (ImmuneCell) -> Unit,
  modifier: Modifier = Modifier
) {
  var selectedCell by remember { mutableStateOf<ImmuneCell?>(null) }
  var filterCategory by remember { mutableStateOf("ALL") }

  val infiniteTransition = rememberInfiniteTransition(label = "topology_network")
  val pulsePhase by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(
      animation = tween(3000, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "packet_pulse_phase"
  )

  val corePulseScale by infiniteTransition.animateFloat(
    initialValue = 0.95f,
    targetValue = 1.05f,
    animationSpec = infiniteRepeatable(
      animation = tween(2400, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "core_pulse_scale"
  )

  Card(
    shape = RoundedCornerShape(24.dp),
    colors = CardDefaults.cardColors(containerColor = WhoopSurface),
    border = BorderStroke(1.dp, Color(0x28FFFFFF)),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    modifier = modifier
      .fillMaxWidth()
      .shadow(6.dp, shape = RoundedCornerShape(24.dp), ambientColor = Color.Black.copy(alpha = 0.6f), spotColor = Color.Black.copy(alpha = 0.6f))
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
        .padding(20.dp)
    ) {
      // Header with Live Indicator
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Outlined.Hub,
              contentDescription = null,
              tint = WhoopStrainBlue,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "IMMUNE TOPOLOGY",
              style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Black,
                letterSpacing = 1.2.sp,
                color = WhoopTextPrimary
              )
            )
          }
          Text(
            text = "Live Biological Agent Network",
            style = MaterialTheme.typography.bodySmall.copy(color = WhoopTextSecondary, fontSize = 11.5.sp)
          )
        }

        Surface(
          color = Color(0x2822C55E),
          border = BorderStroke(1.dp, WhoopRecoveryGreen.copy(alpha = 0.4f)),
          shape = RoundedCornerShape(8.dp)
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
          ) {
            Box(
              modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(WhoopRecoveryGreen)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "${cells.size} CELLS ENGAGED",
              style = MaterialTheme.typography.labelSmall.copy(
                color = WhoopRecoveryGreen,
                fontSize = 9.5.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
              )
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Filter chips for cellular categories
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        listOf("ALL" to "All Cells", "SENSORY" to "Sensory", "COGNITIVE" to "Cognitive", "DEFENSIVE" to "Defense").forEach { (key, label) ->
          val isSelected = filterCategory == key
          Surface(
            shape = RoundedCornerShape(10.dp),
            color = if (isSelected) WhoopStrainBlue else Color(0x331E2330),
            border = BorderStroke(1.dp, if (isSelected) WhoopStrainBlueGlow.copy(alpha = 0.6f) else Color(0x20FFFFFF)),
            modifier = Modifier.clickable { filterCategory = key }
          ) {
            Text(
              text = label,
              style = MaterialTheme.typography.labelSmall.copy(
                color = if (isSelected) WhoopBlackBg else WhoopTextSecondary,
                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium,
                fontSize = 10.sp
              ),
              modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Main Interactive Biological Canvas with Synaptic Conduits & Flow
      Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
          .fillMaxWidth()
          .height(330.dp)
          .clip(RoundedCornerShape(20.dp))
          .background(ClinicalBackground)
          .border(1.dp, ClinicalOutlineSoft, RoundedCornerShape(20.dp))
      ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
          val centerX = size.width / 2f
          val centerY = size.height / 2f
          val orbitRadius = (minOf(size.width, size.height) / 2f) - 52.dp.toPx()

          // Outer faint orbit guide
          drawCircle(
            color = ClinicalOutlineSoft,
            radius = orbitRadius,
            center = Offset(centerX, centerY),
            style = Stroke(width = 1.2.dp.toPx())
          )

          // Concentric Bio-Shield Aura around Central Device
          drawCircle(
            color = MedicalBlueLight.copy(alpha = 0.45f),
            radius = 38.dp.toPx() * corePulseScale,
            center = Offset(centerX, centerY),
            style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
          )

          drawCircle(
            color = MedicalBlueContainer,
            radius = 30.dp.toPx(),
            center = Offset(centerX, centerY)
          )

          // Central Device Core Node
          val coreGradient = Brush.radialGradient(
            colors = listOf(MedicalBlueGlow, MedicalBluePrimary, MedicalBlueDeep),
            center = Offset(centerX, centerY),
            radius = 26.dp.toPx()
          )
          drawCircle(
            brush = coreGradient,
            radius = 24.dp.toPx(),
            center = Offset(centerX, centerY)
          )

          // Draw Synaptic Conduits and moving energy packets
          val nodeCount = cells.size
          for (i in 0 until nodeCount) {
            val angleRad = (i * (2.0 * Math.PI / nodeCount)) - (Math.PI / 2.0)
            val nodeX = centerX + (orbitRadius * cos(angleRad)).toFloat()
            val nodeY = centerY + (orbitRadius * sin(angleRad)).toFloat()

            // Synaptic conduit line
            drawLine(
              color = MedicalTealSecondary.copy(alpha = 0.28f),
              start = Offset(centerX, centerY),
              end = Offset(nodeX, nodeY),
              strokeWidth = 1.6.dp.toPx()
            )

            // Animated Energy Packet moving along conduit
            val packetProgress = (pulsePhase + (i.toFloat() / nodeCount)) % 1f
            val packetX = centerX + (nodeX - centerX) * packetProgress
            val packetY = centerY + (nodeY - centerY) * packetProgress

            drawCircle(
              color = MedicalTealGlow,
              radius = 2.5.dp.toPx(),
              center = Offset(packetX, packetY)
            )
          }
        }

        // Center Device Label
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.Center
        ) {
          Icon(
            imageVector = Icons.Default.PhoneAndroid,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(17.dp)
          )
          Text(
            text = "DEVICE",
            style = MaterialTheme.typography.labelSmall.copy(
              color = Color.White,
              fontWeight = FontWeight.Bold,
              fontSize = 8.5.sp,
              letterSpacing = 0.8.sp
            )
          )
        }

        // Overlay Interactive Cellular Nodes
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
          val density = LocalDensity.current
          val widthPx = constraints.maxWidth.toFloat()
          val heightPx = constraints.maxHeight.toFloat()
          val centerX = widthPx / 2f
          val centerY = heightPx / 2f
          val orbitRadius = (minOf(widthPx, heightPx) / 2f) - with(density) { 52.dp.toPx() }
          val nodeWidth = 92.dp
          val nodeHeight = 48.dp

          cells.forEachIndexed { index, cell ->
            val angleRad = (index * (2.0 * Math.PI / cells.size)) - (Math.PI / 2.0)
            val nodeX = centerX + (orbitRadius * cos(angleRad)).toFloat()
            val nodeY = centerY + (orbitRadius * sin(angleRad)).toFloat()

            val xDp = with(density) { nodeX.toDp() } - (nodeWidth / 2)
            val yDp = with(density) { nodeY.toDp() } - (nodeHeight / 2)

            val isElevated = cell.state != ImmuneCellState.MONITORING && cell.state != ImmuneCellState.IDLE
            val isSelected = selectedCell?.id == cell.id

            val nodeColor = when {
              isElevated -> ClinicalAmberAttention
              cell.name.contains("RESPONSE") -> MedicalTealSecondary
              cell.name.contains("MEMORY") -> MemoryLavender
              else -> MedicalBluePrimary
            }

            val iconVector = when (cell.name) {
              "SENTINEL" -> Icons.Outlined.Visibility
              "PATTERN RECOGNITION (PRR)" -> Icons.Outlined.Sensors
              "NEUTROPHIL" -> Icons.Outlined.Bolt
              "MACROPHAGE" -> Icons.Outlined.CleaningServices
              "DENDRITIC CELL" -> Icons.Outlined.Psychology
              "NATURAL KILLER CELL" -> Icons.Outlined.Security
              "T-HELPER CELL" -> Icons.Outlined.FactCheck
              "B-CELL" -> Icons.Outlined.Fingerprint
              "CYTOTOXIC CELL" -> Icons.Outlined.Shield
              "REGULATORY T-CELL" -> Icons.Outlined.Balance
              "RESOLUTION CELL" -> Icons.Outlined.CheckCircle
              "MEMORY CELL" -> Icons.Outlined.Memory
              else -> Icons.Outlined.Biotech
            }

            Surface(
              shape = RoundedCornerShape(14.dp),
              color = if (isSelected) nodeColor.copy(alpha = 0.12f) else ClinicalSurface,
              border = BorderStroke(
                if (isSelected) 2.dp else 1.2.dp,
                if (isSelected) nodeColor else ClinicalOutline
              ),
              shadowElevation = if (isSelected) 4.dp else 1.5.dp,
              modifier = Modifier
                .offset(x = xDp, y = yDp)
                .size(width = nodeWidth, height = nodeHeight)
                .clickable {
                  selectedCell = if (selectedCell?.id == cell.id) null else cell
                  onCellClick(cell)
                }
                .testTag("topology_node_${cell.id}")
            ) {
              Row(
                modifier = Modifier
                  .fillMaxSize()
                  .padding(horizontal = 7.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Box(
                  modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(nodeColor.copy(alpha = 0.15f)),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    imageVector = iconVector,
                    contentDescription = null,
                    tint = nodeColor,
                    modifier = Modifier.size(13.dp)
                  )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Column(
                  modifier = Modifier.weight(1f),
                  verticalArrangement = Arrangement.Center
                ) {
                  Text(
                    text = cell.name,
                    style = MaterialTheme.typography.labelSmall.copy(
                      fontSize = 9.5.sp,
                      fontWeight = FontWeight.Bold,
                      color = ClinicalTextPrimary
                    ),
                    maxLines = 1
                  )
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                      modifier = Modifier
                        .size(5.dp)
                        .clip(CircleShape)
                        .background(nodeColor)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                      text = cell.state.displayName,
                      style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 8.5.sp,
                        color = nodeColor,
                        fontWeight = FontWeight.SemiBold
                      ),
                      maxLines = 1
                    )
                  }
                }
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      Text(
        text = "Tap any cell node in the orbital network to inspect telemetry, confidence, and autonomous role.",
        style = MaterialTheme.typography.bodySmall.copy(
          color = ClinicalTextMuted,
          fontSize = 11.sp,
          textAlign = TextAlign.Center
        ),
        modifier = Modifier.fillMaxWidth()
      )
    }
  }
}

/**
 * Polished Android-Native Immune Cell Detail Surface (Material 3 ModalBottomSheet)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImmuneCellDetailBottomSheet(
  cell: ImmuneCell,
  onDismiss: () -> Unit
) {
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

  val stateColor = when (cell.state) {
    ImmuneCellState.MONITORING -> ClinicalGreenHealthy
    ImmuneCellState.ACTIVE -> MedicalBluePrimary
    ImmuneCellState.ASSESSING -> MemoryLavender
    ImmuneCellState.INVESTIGATING -> ClinicalAmberAttention
    ImmuneCellState.CONTAINING -> ClinicalCoralCritical
    ImmuneCellState.RECOVERING -> MedicalTealSecondary
    ImmuneCellState.IDLE -> ClinicalTextMuted
  }

  val iconVector = when (cell.name) {
    "SENTINEL" -> Icons.Outlined.Visibility
    "RECEPTOR" -> Icons.Outlined.Sensors
    "CONTEXT CELL" -> Icons.Outlined.Psychology
    "DECISION CELL" -> Icons.Outlined.FactCheck
    "RESPONSE CELL" -> Icons.Outlined.Shield
    "MEMORY CELL" -> Icons.Outlined.Memory
    else -> Icons.Outlined.Biotech
  }

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = ClinicalSurface,
    dragHandle = { BottomSheetDefaults.DragHandle(color = ClinicalOutline) },
    shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 24.dp)
        .padding(bottom = 36.dp)
    ) {
      // Header: Icon + Name + Role + State Chip
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(52.dp)
              .clip(RoundedCornerShape(16.dp))
              .background(stateColor.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = iconVector,
              contentDescription = null,
              tint = stateColor,
              modifier = Modifier.size(28.dp)
            )
          }
          Spacer(modifier = Modifier.width(14.dp))
          Column {
            Text(
              text = cell.name,
              style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = ClinicalTextPrimary,
                letterSpacing = 0.5.sp
              )
            )
            Text(
              text = cell.role,
              style = MaterialTheme.typography.bodyMedium.copy(
                color = ClinicalTextSecondary,
                fontSize = 13.sp
              )
            )
          }
        }

        Surface(
          color = stateColor.copy(alpha = 0.12f),
          border = BorderStroke(1.dp, stateColor.copy(alpha = 0.35f)),
          shape = RoundedCornerShape(10.dp)
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
          ) {
            Box(
              modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(stateColor)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = cell.state.displayName,
              style = MaterialTheme.typography.labelSmall.copy(
                color = stateColor,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
              )
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(18.dp))

      // Description
      Text(
        text = cell.description,
        style = MaterialTheme.typography.bodyMedium.copy(
          color = ClinicalTextSecondary,
          fontSize = 13.5.sp,
          lineHeight = 19.sp
        )
      )

      Spacer(modifier = Modifier.height(18.dp))
      HorizontalDivider(color = ClinicalOutline, thickness = 1.dp)
      Spacer(modifier = Modifier.height(18.dp))

      // Key Metrics Row: Events Handled & Confidence
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        Surface(
          color = ClinicalSurfaceVariant.copy(alpha = 0.6f),
          shape = RoundedCornerShape(14.dp),
          border = BorderStroke(1.dp, ClinicalOutline),
          modifier = Modifier.weight(1f)
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Text(
              text = "EVENTS HANDLED",
              style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp,
                color = ClinicalTextMuted,
                fontSize = 10.5.sp
              )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = String.format("%,d", cell.eventsHandled),
              style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                color = ClinicalTextPrimary
              )
            )
          }
        }

        Surface(
          color = ClinicalSurfaceVariant.copy(alpha = 0.6f),
          shape = RoundedCornerShape(14.dp),
          border = BorderStroke(1.dp, ClinicalOutline),
          modifier = Modifier.weight(1f)
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Text(
              text = "CONFIDENCE",
              style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp,
                color = ClinicalTextMuted,
                fontSize = 10.5.sp
              )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "${cell.confidence}%",
              style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                color = ClinicalGreenHealthy
              )
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Current Activity
      Surface(
        color = ClinicalSurfaceVariant.copy(alpha = 0.6f),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, ClinicalOutline),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Text(
            text = "CURRENT ACTIVITY",
            style = MaterialTheme.typography.labelSmall.copy(
              fontWeight = FontWeight.Bold,
              letterSpacing = 0.8.sp,
              color = ClinicalTextMuted,
              fontSize = 10.5.sp
            )
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = cell.activity,
            style = MaterialTheme.typography.bodyMedium.copy(
              color = ClinicalTextPrimary,
              fontWeight = FontWeight.Medium,
              fontSize = 13.5.sp
            )
          )
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Last Action
      Surface(
        color = ClinicalSurfaceVariant.copy(alpha = 0.6f),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, ClinicalOutline),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Text(
            text = "LAST AUTONOMOUS ACTION",
            style = MaterialTheme.typography.labelSmall.copy(
              fontWeight = FontWeight.Bold,
              letterSpacing = 0.8.sp,
              color = ClinicalTextMuted,
              fontSize = 10.5.sp
            )
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = cell.lastAction,
            style = MaterialTheme.typography.bodyMedium.copy(
              color = ClinicalTextPrimary,
              fontWeight = FontWeight.Medium,
              fontSize = 13.5.sp
            )
          )
        }
      }

      Spacer(modifier = Modifier.height(22.dp))

      Button(
        onClick = onDismiss,
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(containerColor = MedicalBluePrimary),
        modifier = Modifier
          .fillMaxWidth()
          .height(50.dp)
      ) {
        Text("Done", fontWeight = FontWeight.Bold, fontSize = 15.sp)
      }
    }
  }
}

/**
 * Transparent Clinical Explainability Report
 */
@Composable
fun ExplainabilityCard(
  incidentCase: IncidentCase,
  modifier: Modifier = Modifier,
  onResolveClick: (() -> Unit)? = null
) {
  Card(
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(containerColor = ClinicalSurface),
    border = BorderStroke(1.dp, ClinicalOutline),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    modifier = modifier.fillMaxWidth()
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(20.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Outlined.FactCheck,
            contentDescription = null,
            tint = MedicalBluePrimary,
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "EXPLAINABILITY AUDIT",
            style = MaterialTheme.typography.labelMedium.copy(
              fontWeight = FontWeight.Bold,
              letterSpacing = 1.sp,
              color = ClinicalTextSecondary
            )
          )
        }

        Surface(
          color = MedicalBlueLight,
          shape = RoundedCornerShape(8.dp)
        ) {
          Text(
            text = "Confidence ${incidentCase.confidencePercent}%",
            style = MaterialTheme.typography.labelSmall.copy(
              color = MedicalBlueDark,
              fontWeight = FontWeight.Bold,
              fontSize = 10.sp
            ),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      Text(
        text = "WHY DID V-WATCHER ACT?",
        style = MaterialTheme.typography.titleMedium.copy(
          fontWeight = FontWeight.Bold,
          color = ClinicalTextPrimary
        )
      )

      Spacer(modifier = Modifier.height(6.dp))

      Text(
        text = incidentCase.assessment,
        style = MaterialTheme.typography.bodyMedium.copy(
          color = ClinicalTextSecondary,
          fontSize = 13.sp,
          lineHeight = 19.sp
        )
      )

      Spacer(modifier = Modifier.height(14.dp))

      Text(
        text = "Clinical Evidence Matrix:",
        style = MaterialTheme.typography.labelMedium.copy(
          fontWeight = FontWeight.Bold,
          color = ClinicalTextPrimary
        )
      )

      Spacer(modifier = Modifier.height(8.dp))

      incidentCase.evidence.forEachIndexed { index, evidenceItem ->
        Surface(
          color = ClinicalSurfaceVariant.copy(alpha = 0.5f),
          shape = RoundedCornerShape(10.dp),
          border = BorderStroke(0.8.dp, ClinicalOutlineSoft),
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
        ) {
          Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.Top
          ) {
            Text(
              text = "${index + 1}.",
              style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.Bold,
                color = MedicalBluePrimary
              ),
              modifier = Modifier.width(20.dp)
            )
            Text(
              text = evidenceItem,
              style = MaterialTheme.typography.bodySmall.copy(
                color = ClinicalTextSecondary,
                lineHeight = 17.sp,
                fontSize = 12.sp
              )
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))
      HorizontalDivider(color = ClinicalOutline, thickness = 0.8.dp)
      Spacer(modifier = Modifier.height(12.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "Autonomous Action Taken:",
            style = MaterialTheme.typography.labelSmall.copy(color = ClinicalTextMuted)
          )
          Text(
            text = incidentCase.actionTaken,
            style = MaterialTheme.typography.bodySmall.copy(
              fontWeight = FontWeight.SemiBold,
              color = ClinicalTextPrimary
            )
          )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "Clinical Outcome:",
            style = MaterialTheme.typography.labelSmall.copy(color = ClinicalTextMuted)
          )
          Text(
            text = incidentCase.outcome,
            style = MaterialTheme.typography.bodySmall.copy(
              fontWeight = FontWeight.SemiBold,
              color = ClinicalGreenHealthy
            )
          )
        }
      }

      if (incidentCase.status != CaseStatus.RESOLVED && onResolveClick != null) {
        Spacer(modifier = Modifier.height(16.dp))
        Button(
          onClick = onResolveClick,
          shape = RoundedCornerShape(14.dp),
          colors = ButtonDefaults.buttonColors(containerColor = ClinicalGreenHealthy),
          modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .testTag("resolve_case_button")
        ) {
          Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "Mark Case Resolved & Restore Normalcy",
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
          )
        }
      }
    }
  }
}
