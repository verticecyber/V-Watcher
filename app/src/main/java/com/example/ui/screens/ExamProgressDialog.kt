package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.ExamSequenceStep
import com.example.ui.theme.*

@Composable
fun ExamProgressDialog(
  currentStep: ExamSequenceStep?,
  progressFloat: Float,
  healthScore: Int,
  doctorSummary: String,
  onDismiss: () -> Unit
) {
  val animatedProgress by animateFloatAsState(
    targetValue = progressFloat,
    label = "exam_progress_anim"
  )

  val isComplete = currentStep == ExamSequenceStep.EXAMINATION_COMPLETE

  Dialog(onDismissRequest = onDismiss) {
    Card(
      shape = RoundedCornerShape(24.dp),
      colors = CardDefaults.cardColors(containerColor = ClinicalSurface),
      border = BorderStroke(1.dp, ClinicalOutline),
      elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp, vertical = 24.dp)
        .testTag("exam_progress_dialog")
    ) {
      Box(modifier = Modifier.fillMaxWidth()) {
        IconButton(
          onClick = onDismiss,
          modifier = Modifier
            .align(Alignment.TopEnd)
            .padding(12.dp)
            .size(36.dp)
            .testTag("close_exam_dialog_button")
        ) {
          Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Close examination",
            tint = ClinicalTextSecondary,
            modifier = Modifier.size(20.dp)
          )
        }

        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
        if (!isComplete) {
          Box(
            modifier = Modifier
              .size(64.dp)
              .clip(CircleShape)
              .background(MedicalBlueLight),
            contentAlignment = Alignment.Center
          ) {
            CircularProgressIndicator(
              progress = { animatedProgress },
              modifier = Modifier.size(44.dp),
              strokeWidth = 4.dp,
              color = MedicalBluePrimary,
              trackColor = MedicalBlueLight.copy(alpha = 0.5f)
            )
          }

          Spacer(modifier = Modifier.height(18.dp))

          Text(
            text = "CLINICAL EXAMINATION",
            style = MaterialTheme.typography.labelSmall.copy(
              fontWeight = FontWeight.Bold,
              letterSpacing = 1.2.sp,
              color = MedicalBluePrimary
            )
          )
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = currentStep?.title ?: "PREPARING EXAMINATION",
            style = MaterialTheme.typography.titleMedium.copy(
              fontWeight = FontWeight.Bold,
              color = ClinicalTextPrimary,
              textAlign = TextAlign.Center
            )
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = currentStep?.subtitle ?: "Gathering telemetry",
            style = MaterialTheme.typography.bodySmall.copy(
              color = ClinicalTextSecondary,
              textAlign = TextAlign.Center,
              fontSize = 12.5.sp,
              lineHeight = 17.sp
            )
          )

          Spacer(modifier = Modifier.height(20.dp))

          LinearProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier
              .fillMaxWidth()
              .height(8.dp)
              .clip(RoundedCornerShape(4.dp)),
            color = MedicalBluePrimary,
            trackColor = ClinicalOutlineSoft
          )

          Spacer(modifier = Modifier.height(10.dp))

          Text(
            text = "${(animatedProgress * 100).toInt()}% Completed",
            style = MaterialTheme.typography.labelSmall.copy(
              color = ClinicalTextMuted,
              fontWeight = FontWeight.Medium
            )
          )
        } else {
          Box(
            modifier = Modifier
              .size(64.dp)
              .clip(CircleShape)
              .background(ClinicalGreenLight),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.CheckCircle,
              contentDescription = null,
              tint = ClinicalGreenHealthy,
              modifier = Modifier.size(38.dp)
            )
          }

          Spacer(modifier = Modifier.height(16.dp))

          Text(
            text = "EXAMINATION COMPLETE",
            style = MaterialTheme.typography.labelMedium.copy(
              fontWeight = FontWeight.Bold,
              letterSpacing = 1.2.sp,
              color = ClinicalGreenHealthy
            )
          )

          Spacer(modifier = Modifier.height(8.dp))

          Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.Center
          ) {
            Text(
              text = "$healthScore",
              style = MaterialTheme.typography.displayMedium.copy(
                fontWeight = FontWeight.Bold,
                color = ClinicalTextPrimary,
                fontSize = 44.sp,
                letterSpacing = (-1).sp
              )
            )
            Text(
              text = " / 100",
              style = MaterialTheme.typography.titleMedium.copy(
                color = ClinicalTextMuted,
                fontWeight = FontWeight.SemiBold
              ),
              modifier = Modifier.padding(bottom = 6.dp, start = 2.dp)
            )
          }

          Spacer(modifier = Modifier.height(8.dp))

          val isHealthy = examConditionLabel(healthScore) == "Healthy"
          Surface(
            color = if (isHealthy) ClinicalGreenLight else ClinicalAmberLight,
            border = BorderStroke(1.dp, if (isHealthy) ClinicalGreenHealthy.copy(alpha = 0.35f) else ClinicalAmberAttention.copy(alpha = 0.35f)),
            shape = RoundedCornerShape(8.dp)
          ) {
            Text(
              text = "Condition: ${examConditionLabel(healthScore)}",
              style = MaterialTheme.typography.labelSmall.copy(
                color = if (isHealthy) ClinicalGreenHealthy else ClinicalAmberAttention,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.4.sp
              ),
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
            )
          }

          Spacer(modifier = Modifier.height(14.dp))

          Surface(
            color = ClinicalSurfaceVariant.copy(alpha = 0.6f),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(0.8.dp, ClinicalOutlineSoft),
            modifier = Modifier.fillMaxWidth()
          ) {
            Text(
              text = "\"$doctorSummary\"",
              style = MaterialTheme.typography.bodySmall.copy(
                color = ClinicalTextSecondary,
                textAlign = TextAlign.Center,
                fontSize = 12.5.sp,
                lineHeight = 18.sp
              ),
              modifier = Modifier.padding(12.dp)
            )
          }

          Spacer(modifier = Modifier.height(20.dp))

          Button(
            onClick = onDismiss,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MedicalBluePrimary),
            modifier = Modifier
              .fillMaxWidth()
              .height(48.dp)
              .testTag("dismiss_exam_button")
          ) {
            Text(
              text = "Done • View Clinical Assessment",
              style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
            )
          }
        }
      }
    }
  }
}
}
