package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DeviceVitalSign
import com.example.ui.theme.*

/**
 * Sovereign Biometric Telemetry Matrix
 * Combines all device vitals into a single unified instrument panel with clean internal cards,
 * eliminating individual floating cards.
 */
@Composable
fun SovereignVitalsPanel(
  vitals: List<DeviceVitalSign>,
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
      // Header
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "PHYSIOLOGICAL VITALS MATRIX",
          style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = FontWeight.Black,
            letterSpacing = 1.2.sp,
            color = WhoopTextSecondary,
            fontSize = 10.5.sp
          )
        )
        Text(
          text = "Real-Time Telemetry",
          style = MaterialTheme.typography.labelSmall.copy(
            color = WhoopTextMuted,
            fontSize = 10.sp
          )
        )
      }

      Spacer(modifier = Modifier.height(14.dp))

      // 2-column clean grid
      val chunked = vitals.chunked(2)
      chunked.forEachIndexed { _, rowItems ->
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          rowItems.forEach { item ->
            val statusColor = if (item.isNormal) WhoopRecoveryGreen else WhoopWarningAmber
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = Color(0x181E2330),
              border = BorderStroke(1.dp, Color(0x18FFFFFF)),
              modifier = Modifier
                .weight(1f)
                .padding(vertical = 3.dp)
            ) {
              Column(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
              ) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text(
                    text = item.title.uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(
                      fontWeight = FontWeight.Black,
                      letterSpacing = 0.6.sp,
                      color = WhoopTextMuted,
                      fontSize = 9.sp
                    )
                  )
                  Box(
                    modifier = Modifier
                      .size(5.dp)
                      .clip(CircleShape)
                      .background(statusColor)
                  )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                  text = item.status,
                  style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Black,
                    color = statusColor,
                    fontSize = 15.sp
                  )
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                  text = item.subtitle,
                  style = MaterialTheme.typography.bodySmall.copy(
                    color = WhoopTextSecondary,
                    fontSize = 10.5.sp,
                    lineHeight = 14.sp
                  ),
                  maxLines = 1
                )
              }
            }
          }
          if (rowItems.size == 1) {
            Spacer(modifier = Modifier.weight(1f))
          }
        }
      }
    }
  }
}
