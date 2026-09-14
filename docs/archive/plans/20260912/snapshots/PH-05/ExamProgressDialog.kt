package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
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

  Dialog(onDismissRequest = { if (isComplete) onDismiss() }) {
    Card(
      shape = RoundedCornerShape(24.dp),
      colors = CardDefaults.cardColors(containerColor = ClinicalSurface),
      border = BorderStroke(1.dp, ClinicalOutline),
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
        .testTag("exam_progress_dialog")
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        if (!isComplete) {
          Box(
            modifier = Modifier
              .size(54.dp)
              .clip(CircleShape)
              .background(MedicalBlueLight),
            contentAlignment = Alignment.Center
          ) {
            CircularProgressIndicator(
              progress = { animatedProgress },
              modifier = Modifier.size(36.dp),
              strokeWidth = 3.5.dp,
              color = MedicalBluePrimary
            )
          }

          Spacer(modifier = Modifier.height(18.dp))

          Text(
            text = "CLINICAL EXAMINATION",
            style = MaterialTheme.typography.labelSmall.copy(
              fontWeight = FontWeight.Bold,
              letterSpacing = 1.1.sp,
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
              fontSize = 12.sp
            )
          )

          Spacer(modifier = Modifier.height(20.dp))

          LinearProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier
              .fillMaxWidth()
              .height(6.dp)
              .clip(RoundedCornerShape(3.dp)),
            color = MedicalBluePrimary,
            trackColor = ClinicalOutlineSoft
          )
        } else {
          Box(
            modifier = Modifier
              .size(60.dp)
              .clip(CircleShape)
              .background(ClinicalGreenLight),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.CheckCircle,
              contentDescription = null,
              tint = ClinicalGreenHealthy,
              modifier = Modifier.size(36.dp)
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

          Spacer(modifier = Modifier.height(10.dp))

          Row(verticalAlignment = Alignment.Bottom) {
            Text(
              text = "$healthScore",
              style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Bold,
                color = ClinicalTextPrimary,
                fontSize = 42.sp
              )
            )
            Text(
              text = " / 100",
              style = MaterialTheme.typography.titleMedium.copy(
                color = ClinicalTextMuted
              )
            )
          }

          Surface(
            color = ClinicalGreenLight,
            shape = RoundedCornerShape(8.dp)
          ) {
            Text(
              text = "Condition: Healthy",
              style = MaterialTheme.typography.labelSmall.copy(
                color = ClinicalGreenHealthy,
                fontWeight = FontWeight.Bold
              ),
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
            )
          }

          Spacer(modifier = Modifier.height(12.dp))

          Text(
            text = "\"$doctorSummary\"",
            style = MaterialTheme.typography.bodySmall.copy(
              color = ClinicalTextSecondary,
              textAlign = TextAlign.Center,
              fontSize = 12.sp
            )
          )

          Spacer(modifier = Modifier.height(20.dp))

          Button(
            onClick = onDismiss,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MedicalBluePrimary),
            modifier = Modifier
              .fillMaxWidth()
              .height(44.dp)
              .testTag("dismiss_exam_button")
          ) {
            Text("Done / View Diagnosis", fontWeight = FontWeight.Bold)
          }
        }
      }
    }
  }
}
