package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.Biotech
import androidx.compose.material.icons.outlined.Troubleshoot
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

/**
 * Sovereign Tactical Command Panel
 * Consolidates diagnostic exam & drift simulation into an authoritative, clean security console
 * that eliminates generic floating action cards.
 */
@Composable
fun TacticalDefenseCommandPanel(
  isSimulationActive: Boolean,
  onRunCheck: () -> Unit,
  onSimulateAnomaly: () -> Unit,
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
              Color(0xFF161922),
              Color(0xFF101217)
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
          text = "TACTICAL DEFENSE COMMAND",
          style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = FontWeight.Black,
            letterSpacing = 1.2.sp,
            color = WhoopTextSecondary,
            fontSize = 10.5.sp
          )
        )
        Surface(
          color = Color(0x2822C55E),
          border = BorderStroke(1.dp, WhoopRecoveryGreen.copy(alpha = 0.35f)),
          shape = RoundedCornerShape(6.dp)
        ) {
          Text(
            text = "READY",
            style = MaterialTheme.typography.labelSmall.copy(
              color = WhoopRecoveryGreen,
              fontWeight = FontWeight.Black,
              fontSize = 9.sp,
              letterSpacing = 0.6.sp
            ),
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Main Action: Run 7-Point Security Check
      Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color(0x221E2330),
        border = BorderStroke(1.dp, Color(0x3300B4D8)),
        modifier = Modifier
          .fillMaxWidth()
          .clickable { onRunCheck() }
          .testTag("run_device_check_button")
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 12.dp),
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
                .size(38.dp)
                .clip(RoundedCornerShape(11.dp))
                .background(Color(0x3300B4D8))
                .border(1.dp, WhoopStrainBlue.copy(alpha = 0.45f), RoundedCornerShape(11.dp))
            ) {
              Icon(
                imageVector = Icons.Outlined.Troubleshoot,
                contentDescription = null,
                tint = WhoopStrainBlue,
                modifier = Modifier.size(20.dp)
              )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
              Text(
                text = "EXECUTE BIO-DEFENSE SCAN",
                style = MaterialTheme.typography.titleSmall.copy(
                  fontWeight = FontWeight.Black,
                  color = WhoopTextPrimary,
                  letterSpacing = 0.5.sp,
                  fontSize = 13.5.sp
                )
              )
              Text(
                text = "7-point local telemetry & process verification",
                style = MaterialTheme.typography.bodySmall.copy(
                  color = WhoopTextSecondary,
                  fontSize = 11.5.sp
                )
              )
            }
          }

          Surface(
            shape = RoundedCornerShape(10.dp),
            color = WhoopStrainBlue,
            modifier = Modifier.size(32.dp)
          ) {
            Box(contentAlignment = Alignment.Center) {
              Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = null,
                tint = WhoopBlackBg,
                modifier = Modifier.size(16.dp)
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      // Secondary Action: Test Stress / Simulate Anomaly
      Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color(0x181E2330),
        border = BorderStroke(1.dp, Color(0x18FFFFFF)),
        modifier = Modifier
          .fillMaxWidth()
          .clickable(enabled = !isSimulationActive) { onSimulateAnomaly() }
          .testTag("simulate_unusual_activity_button")
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 10.dp),
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
                .size(32.dp)
                .clip(RoundedCornerShape(9.dp))
                .background(Color(0x282DD4BF))
                .border(1.dp, WhoopSleepTeal.copy(alpha = 0.35f), RoundedCornerShape(9.dp))
            ) {
              Icon(
                imageVector = Icons.Outlined.Biotech,
                contentDescription = null,
                tint = WhoopSleepTeal,
                modifier = Modifier.size(16.dp)
              )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text(
                text = "SIMULATE CELLULAR DRIFT",
                style = MaterialTheme.typography.labelSmall.copy(
                  fontWeight = FontWeight.Bold,
                  letterSpacing = 0.5.sp,
                  color = WhoopTextPrimary,
                  fontSize = 11.5.sp
                )
              )
              Text(
                text = "Tests local reflex, containment & resolution",
                style = MaterialTheme.typography.bodySmall.copy(
                  color = WhoopTextMuted,
                  fontSize = 10.5.sp
                )
              )
            }
          }

          Icon(
            imageVector = Icons.Default.PlayArrow,
            contentDescription = null,
            tint = WhoopSleepTeal,
            modifier = Modifier.size(18.dp)
          )
        }
      }
    }
  }
}
