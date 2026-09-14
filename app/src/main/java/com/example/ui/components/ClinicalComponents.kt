package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
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
 * Ultra-refined clinical top bar with live status heartbeat
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClinicalTopBar(
  onNotificationsClick: () -> Unit,
  notificationCount: Int,
  onDiagnosticsClick: () -> Unit = {},
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

  Surface(
    color = ClinicalBackground,
    modifier = modifier.fillMaxWidth()
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp, vertical = 12.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(16.dp)
          ) {
            Box(
              modifier = Modifier
                .size(14.dp)
                .clip(CircleShape)
                .background(ClinicalGreenHealthy.copy(alpha = pulseAlpha * 0.35f))
            )
            Box(
              modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(ClinicalGreenHealthy)
            )
          }
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "V-WATCHER",
            style = MaterialTheme.typography.titleMedium.copy(
              fontWeight = FontWeight.Bold,
              letterSpacing = 1.4.sp,
              color = ClinicalTextPrimary
            )
          )
          Spacer(modifier = Modifier.width(8.dp))
          Surface(
            color = MedicalBlueLight.copy(alpha = 0.8f),
            shape = RoundedCornerShape(6.dp)
          ) {
            Text(
              text = "PRO",
              style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = MedicalBlueDark,
                letterSpacing = 0.5.sp
              ),
              modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
            )
          }
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
          text = "Your phone's immune system • Clinical grade",
          style = MaterialTheme.typography.bodySmall.copy(
            color = ClinicalTextSecondary,
            fontSize = 11.5.sp
          )
        )
      }

      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        // Technical Diagnostics Button
        Surface(
          shape = CircleShape,
          color = ClinicalSurface,
          border = BorderStroke(1.dp, ClinicalOutline),
          shadowElevation = 1.dp
        ) {
          IconButton(
            onClick = onDiagnosticsClick,
            modifier = Modifier
              .size(42.dp)
              .testTag("diagnostics_top_button")
          ) {
            Icon(
              imageVector = Icons.Outlined.Analytics,
              contentDescription = "Technical Diagnostics",
              tint = MedicalBluePrimary,
              modifier = Modifier.size(20.dp)
            )
          }
        }

        // Notifications Button
        Surface(
          shape = CircleShape,
          color = ClinicalSurface,
          border = BorderStroke(1.dp, ClinicalOutline),
          shadowElevation = 1.dp
        ) {
          IconButton(
            onClick = onNotificationsClick,
            modifier = Modifier
              .size(42.dp)
              .testTag("notifications_button")
          ) {
            BadgedBox(
              badge = {
                if (notificationCount > 0) {
                  Badge(
                    containerColor = MedicalBluePrimary,
                    contentColor = Color.White
                  ) {
                    Text(text = notificationCount.toString(), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                  }
                }
              }
            ) {
              Icon(
                imageVector = Icons.Outlined.Notifications,
                contentDescription = "Clinical notifications",
                tint = ClinicalTextSecondary,
                modifier = Modifier.size(20.dp)
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
      DeviceCondition.HEALTHY -> ClinicalGreenHealthy
      DeviceCondition.ATTENTION, DeviceCondition.UNDER_REVIEW -> ClinicalAmberAttention
      DeviceCondition.CONTAINING -> MedicalBluePrimary
      DeviceCondition.RECOVERING -> MedicalTealSecondary
    },
    label = "gauge_color_anim"
  )

  val gaugeGlowColor = when (condition) {
    DeviceCondition.HEALTHY -> ClinicalGreenGlow
    DeviceCondition.ATTENTION, DeviceCondition.UNDER_REVIEW -> ClinicalAmberGlow
    DeviceCondition.CONTAINING -> MedicalBlueGlow
    DeviceCondition.RECOVERING -> MedicalTealGlow
  }

  Card(
    shape = RoundedCornerShape(24.dp),
    colors = CardDefaults.cardColors(containerColor = ClinicalSurface),
    border = BorderStroke(1.dp, ClinicalOutline),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    modifier = modifier
      .fillMaxWidth()
      .shadow(6.dp, shape = RoundedCornerShape(24.dp), ambientColor = ClinicalOutline, spotColor = MedicalBlueLight.copy(alpha = 0.5f))
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 24.dp, vertical = 22.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      // Header Label with clinical badge
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.MonitorHeart,
            contentDescription = null,
            tint = MedicalBluePrimary,
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "DEVICE PHYSIOLOGICAL SCORE",
            style = MaterialTheme.typography.labelSmall.copy(
              fontWeight = FontWeight.Bold,
              letterSpacing = 1.2.sp,
              color = ClinicalTextSecondary,
              fontSize = 11.sp
            )
          )
        }

        Surface(
          color = gaugeColor.copy(alpha = 0.12f),
          border = BorderStroke(0.8.dp, gaugeColor.copy(alpha = 0.35f)),
          shape = RoundedCornerShape(12.dp)
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
          ) {
            Box(
              modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(gaugeColor)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
              text = condition.displayName.uppercase(),
              style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = gaugeColor,
                fontSize = 10.sp,
                letterSpacing = 0.5.sp
              )
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(18.dp))

      // Concentric Multi-Layer Clinical Radial Dial
      Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(190.dp)
      ) {
        Canvas(modifier = Modifier.size(175.dp)) {
          val strokeWidth = 14.dp.toPx()
          val radius = (size.minDimension - strokeWidth) / 2f
          val centerOffset = Offset(size.width / 2f, size.height / 2f)

          // 1. Outermost subtle calibration tick ring
          val tickCount = 48
          for (i in 0 until tickCount) {
            val angle = (i * (2.0 * Math.PI / tickCount)).toFloat()
            val outerRadius = radius + 11.dp.toPx()
            val innerRadius = outerRadius - if (i % 6 == 0) 5.dp.toPx() else 3.dp.toPx()
            val startX = centerOffset.x + innerRadius * cos(angle)
            val startY = centerOffset.y + innerRadius * sin(angle)
            val endX = centerOffset.x + outerRadius * cos(angle)
            val endY = centerOffset.y + outerRadius * sin(angle)

            drawLine(
              color = ClinicalOutline,
              start = Offset(startX, startY),
              end = Offset(endX, endY),
              strokeWidth = 1.2.dp.toPx()
            )
          }

          // 2. Faint ambient glow behind progress track
          drawCircle(
            color = gaugeColor.copy(alpha = 0.05f),
            radius = radius * breathScale,
            center = centerOffset,
            style = Stroke(width = strokeWidth * 1.6f)
          )

          // 3. Track Channel background
          drawCircle(
            color = ClinicalSurfaceVariant,
            radius = radius,
            center = centerOffset,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
          )

          // 4. Progress Arc with gradient
          val sweepAngle = (animatedScore / 100f) * 360f
          val gradient = Brush.sweepGradient(
            colors = listOf(
              gaugeColor.copy(alpha = 0.85f),
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

          // 5. Subtle Inner ring
          drawCircle(
            color = ClinicalOutlineSoft,
            radius = radius - strokeWidth - 6.dp.toPx(),
            center = centerOffset,
            style = Stroke(width = 1.dp.toPx())
          )
        }

        // Central Score & Units with Medical ECG Sparkline
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          modifier = Modifier.padding(bottom = 4.dp)
        ) {
          Row(
            verticalAlignment = Alignment.Bottom,
            modifier = Modifier.padding(top = 8.dp)
          ) {
            Text(
              text = "${animatedScore.toInt()}",
              style = MaterialTheme.typography.displayLarge.copy(
                fontWeight = FontWeight.Bold,
                color = ClinicalTextPrimary,
                fontSize = 46.sp,
                letterSpacing = (-1.5).sp
              )
            )
            Text(
              text = " /100",
              style = MaterialTheme.typography.titleMedium.copy(
                color = ClinicalTextMuted,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp
              ),
              modifier = Modifier.padding(bottom = 6.dp, start = 2.dp)
            )
          }

          // Mini ECG pulse line canvas
          Canvas(
            modifier = Modifier
              .width(72.dp)
              .height(18.dp)
              .padding(vertical = 2.dp)
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
              color = ClinicalTextMuted,
              fontWeight = FontWeight.Bold,
              fontSize = 9.sp,
              letterSpacing = 1.5.sp
            )
          )
        }
      }

      Spacer(modifier = Modifier.height(18.dp))

      // Direct visual connection between score anchor and supporting statement
      Surface(
        color = if (condition == DeviceCondition.HEALTHY) ClinicalGreenLight.copy(alpha = 0.55f) else gaugeColor.copy(alpha = 0.12f),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(
          1.dp,
          if (condition == DeviceCondition.HEALTHY) ClinicalGreenHealthy.copy(alpha = 0.35f) else gaugeColor.copy(alpha = 0.4f)
        ),
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
          Spacer(modifier = Modifier.width(9.dp))
          Text(
            text = supportingSentence,
            style = MaterialTheme.typography.bodyMedium.copy(
              color = ClinicalTextPrimary,
              fontWeight = FontWeight.SemiBold,
              fontSize = 13.5.sp,
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
  val infiniteTransition = rememberInfiniteTransition(label = "vital_pulse_${vital.id}")
  val pulseAlpha by infiniteTransition.animateFloat(
    initialValue = 0.4f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(
      animation = tween(2000, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "pulse_alpha"
  )

  val statusColor = if (vital.isNormal) ClinicalGreenHealthy else ClinicalAmberAttention

  Card(
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(containerColor = ClinicalSurface),
    border = BorderStroke(1.dp, ClinicalOutline),
    elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
    modifier = modifier.fillMaxWidth()
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
          text = vital.title,
          style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.9.sp,
            color = ClinicalTextSecondary,
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
              .background(statusColor.copy(alpha = pulseAlpha * 0.4f))
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
            fontWeight = FontWeight.Bold,
            color = statusColor,
            fontSize = 15.5.sp
          )
        )
        Surface(
          color = ClinicalSurfaceVariant,
          shape = RoundedCornerShape(6.dp)
        ) {
          Text(
            text = vital.trendLabel,
            style = MaterialTheme.typography.labelSmall.copy(
              color = ClinicalTextSecondary,
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
          color = ClinicalTextSecondary,
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
          color = statusColor.copy(alpha = 0.5f),
          style = Stroke(width = 1.6.dp.toPx(), cap = StrokeCap.Round)
        )
      }

      Spacer(modifier = Modifier.height(8.dp))

      Text(
        text = "Updated ${vital.updatedAgo}",
        style = MaterialTheme.typography.labelSmall.copy(
          color = ClinicalTextMuted,
          fontSize = 10.5.sp
        )
      )
    }
  }
}

/**
 * Clinical summary written with physician-grade calm authority
 */
@Composable
fun DoctorClinicalSummarySection(
  clinicalAssessment: String,
  lastAssessment: String,
  modifier: Modifier = Modifier
) {
  Card(
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(containerColor = MedicalBlueContainer.copy(alpha = 0.7f)),
    border = BorderStroke(1.dp, MedicalBlueLight),
    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    modifier = modifier.fillMaxWidth()
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
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
              .size(28.dp)
              .clip(RoundedCornerShape(8.dp))
              .background(MedicalBluePrimary),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.HealthAndSafety,
              contentDescription = null,
              tint = Color.White,
              modifier = Modifier.size(16.dp)
            )
          }
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Text(
              text = "CLINICAL SUMMARY",
              style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = MedicalBlueDeep,
                fontSize = 11.sp
              )
            )
            Text(
              text = "Attending Device Physician",
              style = MaterialTheme.typography.labelSmall.copy(
                color = ClinicalTextMuted,
                fontSize = 9.5.sp
              )
            )
          }
        }

        Surface(
          color = Color.White.copy(alpha = 0.8f),
          border = BorderStroke(0.8.dp, MedicalBlueLight),
          shape = RoundedCornerShape(8.dp)
        ) {
          Text(
            text = "Assessed $lastAssessment",
            style = MaterialTheme.typography.labelSmall.copy(
              color = MedicalBlueDark,
              fontSize = 10.5.sp,
              fontWeight = FontWeight.Medium
            ),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      Text(
        text = "\"$clinicalAssessment\"",
        style = MaterialTheme.typography.bodyMedium.copy(
          color = ClinicalTextPrimary,
          lineHeight = 22.sp,
          fontWeight = FontWeight.Normal,
          fontSize = 13.5.sp
        )
      )

      Spacer(modifier = Modifier.height(12.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Outlined.Shield,
            contentDescription = null,
            tint = MedicalBluePrimary,
            modifier = Modifier.size(13.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = "Deterministic On-Device Core",
            style = MaterialTheme.typography.labelSmall.copy(
              color = ClinicalTextSecondary,
              fontSize = 10.sp,
              fontWeight = FontWeight.Medium
            )
          )
        }

        Text(
          text = "Zero Cloud Leakage",
          style = MaterialTheme.typography.labelSmall.copy(
            color = ClinicalGreenHealthy,
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
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MedicalBlueLight),
        colors = ButtonDefaults.outlinedButtonColors(
          containerColor = MedicalBlueContainer.copy(alpha = 0.4f),
          contentColor = MedicalBluePrimary
        ),
        modifier = Modifier
          .fillMaxWidth()
          .height(42.dp)
          .testTag("view_network_health_button")
      ) {
        Icon(Icons.Outlined.Dns, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "Inspect Network Telemetry & Conduits",
          style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
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
    colors = CardDefaults.cardColors(containerColor = ClinicalSurface),
    border = BorderStroke(1.dp, ClinicalOutline),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    modifier = modifier
      .fillMaxWidth()
      .shadow(4.dp, shape = RoundedCornerShape(24.dp), ambientColor = ClinicalOutline, spotColor = MedicalTealLight.copy(alpha = 0.4f))
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
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
              tint = MedicalBluePrimary,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "IMMUNE TOPOLOGY",
              style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp,
                color = ClinicalTextSecondary
              )
            )
          }
          Text(
            text = "Live Biological Agent Network",
            style = MaterialTheme.typography.bodySmall.copy(color = ClinicalTextMuted, fontSize = 11.5.sp)
          )
        }

        Surface(
          color = ClinicalGreenLight,
          border = BorderStroke(0.8.dp, ClinicalGreenHealthy.copy(alpha = 0.3f)),
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
                .background(ClinicalGreenHealthy)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "${cells.size} CELLS ENGAGED",
              style = MaterialTheme.typography.labelSmall.copy(
                color = ClinicalGreenHealthy,
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
            shape = RoundedCornerShape(8.dp),
            color = if (isSelected) MedicalBluePrimary else ClinicalSurfaceVariant,
            border = BorderStroke(1.dp, if (isSelected) MedicalBluePrimary else ClinicalOutlineSoft),
            modifier = Modifier.clickable { filterCategory = key }
          ) {
            Text(
              text = label,
              style = MaterialTheme.typography.labelSmall.copy(
                color = if (isSelected) Color.White else ClinicalTextSecondary,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
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
              "RECEPTOR" -> Icons.Outlined.Sensors
              "CONTEXT CELL" -> Icons.Outlined.Psychology
              "DECISION CELL" -> Icons.Outlined.FactCheck
              "RESPONSE CELL" -> Icons.Outlined.Shield
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
            .height(46.dp)
            .testTag("resolve_case_button")
        ) {
          Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "Mark Case Resolved & Restore Normalcy",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
          )
        }
      }
    }
  }
}
