package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.SimulatedNotification
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimulatedNotificationsSheet(
  notifications: List<SimulatedNotification>,
  onDismiss: () -> Unit
) {
  ModalBottomSheet(
    onDismissRequest = onDismiss,
    containerColor = ClinicalSurface,
    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 24.dp, vertical = 12.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "CLINICAL NOTIFICATIONS",
            style = MaterialTheme.typography.labelSmall.copy(
              fontWeight = FontWeight.Bold,
              letterSpacing = 1.1.sp,
              color = MedicalBluePrimary
            )
          )
          Text(
            text = "Doctor-to-Patient Alerts",
            style = MaterialTheme.typography.titleMedium.copy(
              fontWeight = FontWeight.Bold,
              color = ClinicalTextPrimary
            )
          )
        }
        Surface(
          color = MedicalBlueLight,
          shape = RoundedCornerShape(8.dp)
        ) {
          Text(
            text = "${notifications.size} updates",
            style = MaterialTheme.typography.labelSmall.copy(
              color = MedicalBlueDark,
              fontWeight = FontWeight.Bold
            ),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(bottom = 32.dp)
      ) {
        items(notifications) { notif ->
          Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = ClinicalBackground),
            border = BorderStroke(1.dp, ClinicalOutline),
            modifier = Modifier.fillMaxWidth().testTag("notif_item_${notif.id}")
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
              verticalAlignment = Alignment.Top
            ) {
              Box(
                modifier = Modifier
                  .size(36.dp)
                  .clip(CircleShape)
                  .background(MedicalBlueLight),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Outlined.Shield,
                  contentDescription = null,
                  tint = MedicalBlueDark,
                  modifier = Modifier.size(18.dp)
                )
              }
              Spacer(modifier = Modifier.width(12.dp))
              Column(modifier = Modifier.weight(1f)) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Text(
                    text = notif.title,
                    style = MaterialTheme.typography.titleSmall.copy(
                      fontWeight = FontWeight.SemiBold,
                      color = ClinicalTextPrimary
                    )
                  )
                  Text(
                    text = notif.timestamp,
                    style = MaterialTheme.typography.labelSmall.copy(
                      color = ClinicalTextMuted,
                      fontSize = 11.sp
                    )
                  )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                  text = notif.message,
                  style = MaterialTheme.typography.bodySmall.copy(
                    color = ClinicalTextSecondary,
                    fontSize = 12.sp
                  )
                )
              }
            }
          }
        }
      }
    }
  }
}
