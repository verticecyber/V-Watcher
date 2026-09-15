package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.theme.*
import com.example.viewmodel.VWatcherUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemoryScreen(
  uiState: VWatcherUiState,
  modifier: Modifier = Modifier
) {
  var searchQuery by remember { mutableStateOf("") }
  var selectedPatternDetail by remember { mutableStateOf<ImmuneMemoryPattern?>(null) }

  val filteredPatterns = remember(uiState.memoryPatterns, searchQuery) {
    if (searchQuery.isBlank()) uiState.memoryPatterns
    else {
      uiState.memoryPatterns.filter {
        it.patternCode.contains(searchQuery, ignoreCase = true) ||
          it.name.contains(searchQuery, ignoreCase = true) ||
          it.category.contains(searchQuery, ignoreCase = true)
      }
    }
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(ClinicalBackground)
  ) {
    // Header
    Surface(
      color = ClinicalBackground,
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 20.dp, vertical = 14.dp)
      ) {
        Text(
          text = "IMMUNE MEMORY",
          style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.2.sp,
            color = MemoryLavender
          )
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
          text = "Your device remembers.",
          style = MaterialTheme.typography.headlineSmall.copy(
            fontWeight = FontWeight.Bold,
            color = ClinicalTextPrimary
          )
        )
        Text(
          text = "Previously observed patterns let V-Watcher short-circuit expensive inference. Match latency is not measured in this build.",
          style = MaterialTheme.typography.bodySmall.copy(color = ClinicalTextSecondary)
        )
      }
    }

    // Search bar
    OutlinedTextField(
      value = searchQuery,
      onValueChange = { searchQuery = it },
      placeholder = { Text("Search remembered behavioral patterns...", fontSize = 13.sp) },
      leadingIcon = {
        Icon(
          Icons.Outlined.Search,
          contentDescription = null,
          tint = ClinicalTextSecondary,
          modifier = Modifier.size(20.dp)
        )
      },
      trailingIcon = {
        if (searchQuery.isNotEmpty()) {
          IconButton(
            onClick = { searchQuery = "" },
            modifier = Modifier.size(24.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Close,
              contentDescription = "Clear search",
              tint = ClinicalTextSecondary,
              modifier = Modifier.size(16.dp)
            )
          }
        }
      },
      singleLine = true,
      shape = RoundedCornerShape(14.dp),
      colors = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = ClinicalSurface,
        unfocusedContainerColor = ClinicalSurface,
        focusedBorderColor = MemoryLavender,
        unfocusedBorderColor = ClinicalOutline
      ),
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp, vertical = 4.dp)
        .testTag("memory_search_input")
    )

    LazyColumn(
      modifier = Modifier.fillMaxSize(),
      contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      // Explanatory Banner Card
      item {
        Card(
          shape = RoundedCornerShape(18.dp),
          colors = CardDefaults.cardColors(containerColor = MemoryLavenderLight.copy(alpha = 0.5f)),
          border = BorderStroke(1.dp, MemoryLavender.copy(alpha = 0.3f)),
          elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(MemoryLavender),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Outlined.Psychology,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(22.dp)
              )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column {
              Text(
                text = "ADAPTIVE REFLEX MEMORY",
                style = MaterialTheme.typography.labelSmall.copy(
                  fontWeight = FontWeight.Bold,
                  letterSpacing = 0.8.sp,
                  color = MemoryLavender
                )
              )
              Text(
                text = "If a known pattern appears again, it is recognized from the local repository instead of re-running inference. No latency benefit has been measured.",
                style = MaterialTheme.typography.bodySmall.copy(
                  color = ClinicalTextPrimary,
                  fontSize = 12.sp
                )
              )
            }
          }
        }
      }

      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "KNOWN BEHAVIORAL PATTERNS",
            style = MaterialTheme.typography.labelMedium.copy(
              fontWeight = FontWeight.Bold,
              letterSpacing = 1.sp,
              color = ClinicalTextSecondary
            )
          )
          Text(
            text = "${filteredPatterns.size} active",
            style = MaterialTheme.typography.labelSmall.copy(color = ClinicalTextMuted)
          )
        }
      }

      if (filteredPatterns.isEmpty()) {
        item {
          Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = ClinicalSurface),
            border = BorderStroke(1.dp, ClinicalOutline),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 32.dp),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Box(
                modifier = Modifier
                  .size(52.dp)
                  .clip(CircleShape)
                  .background(MemoryLavenderLight),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Outlined.SearchOff,
                  contentDescription = null,
                  tint = MemoryLavender,
                  modifier = Modifier.size(26.dp)
                )
              }
              Spacer(modifier = Modifier.height(14.dp))
              Text(
                text = "No matching patterns found",
                style = MaterialTheme.typography.titleMedium.copy(
                  fontWeight = FontWeight.Bold,
                  color = ClinicalTextPrimary
                ),
                textAlign = TextAlign.Center
              )
              Spacer(modifier = Modifier.height(6.dp))
              Text(
                text = "No remembered behavioral patterns match \"$searchQuery\". Try checking the spelling or search by category.",
                style = MaterialTheme.typography.bodySmall.copy(
                  color = ClinicalTextSecondary,
                  fontSize = 12.5.sp,
                  lineHeight = 17.sp
                ),
                textAlign = TextAlign.Center
              )
              Spacer(modifier = Modifier.height(16.dp))
              OutlinedButton(
                onClick = { searchQuery = "" },
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MemoryLavender.copy(alpha = 0.6f)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MemoryLavender),
                modifier = Modifier.height(44.dp)
              ) {
                Text(
                  text = "Clear Search",
                  style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )
              }
            }
          }
        }
      } else {
        items(filteredPatterns) { pattern ->
          ImmuneMemoryPatternCard(
            pattern = pattern,
            onClick = { selectedPatternDetail = pattern }
          )
        }
      }

      item {
        Spacer(modifier = Modifier.height(72.dp))
      }
    }
  }

  // Pattern Detail Modal
  selectedPatternDetail?.let { pattern ->
    ModalBottomSheet(
      onDismissRequest = { selectedPatternDetail = null },
      containerColor = ClinicalSurface,
      shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 24.dp, vertical = 16.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = pattern.patternCode,
              style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = MemoryLavender
              )
            )
            Text(
              text = pattern.name,
              style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = ClinicalTextPrimary
              )
            )
          }
          Surface(
            color = MemoryLavenderLight,
            shape = RoundedCornerShape(8.dp)
          ) {
            Text(
              text = pattern.category,
              style = MaterialTheme.typography.labelSmall.copy(
                color = MemoryLavender,
                fontWeight = FontWeight.Bold
              ),
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
            )
          }
        }

        Spacer(modifier = Modifier.height(14.dp))
        Text(
          text = pattern.description,
          style = MaterialTheme.typography.bodyMedium.copy(color = ClinicalTextPrimary)
        )
        Spacer(modifier = Modifier.height(14.dp))
        HorizontalDivider(color = ClinicalOutline)
        Spacer(modifier = Modifier.height(14.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Column {
            Text("Observations", style = MaterialTheme.typography.labelSmall.copy(color = ClinicalTextMuted))
            Text("${pattern.observedCount} times", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
          }
          Column {
            Text("Last Seen", style = MaterialTheme.typography.labelSmall.copy(color = ClinicalTextMuted))
            Text(pattern.lastSeen, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
          }
          Column {
            Text("Confidence", style = MaterialTheme.typography.labelSmall.copy(color = ClinicalTextMuted))
            Text(pattern.confidenceScore, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = ClinicalGreenHealthy))
          }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
          text = "Clinical Reflex Impact:",
          style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = pattern.causalImpact,
          style = MaterialTheme.typography.bodySmall.copy(
            color = ClinicalTextSecondary,
            lineHeight = 18.sp
          )
        )
        Spacer(modifier = Modifier.height(32.dp))
      }
    }
  }
}

@Composable
private fun ImmuneMemoryPatternCard(
  pattern: ImmuneMemoryPattern,
  onClick: () -> Unit
) {
  Card(
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = ClinicalSurface),
    border = BorderStroke(1.dp, ClinicalOutline),
    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    modifier = Modifier
      .fillMaxWidth()
      .clickable { onClick() }
      .testTag("pattern_${pattern.id}")
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
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = pattern.patternCode,
            style = MaterialTheme.typography.labelMedium.copy(
              fontWeight = FontWeight.Bold,
              fontFamily = FontFamily.Monospace,
              color = MemoryLavender,
              letterSpacing = 1.sp
            )
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "• ${pattern.category}",
            style = MaterialTheme.typography.bodySmall.copy(color = ClinicalTextMuted, fontSize = 11.sp)
          )
        }
        Surface(
          color = ClinicalGreenLight,
          shape = RoundedCornerShape(8.dp)
        ) {
          Text(
            text = pattern.confidenceScore,
            style = MaterialTheme.typography.labelSmall.copy(
              color = ClinicalGreenHealthy,
              fontWeight = FontWeight.Bold
            ),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      Text(
        text = pattern.name,
        style = MaterialTheme.typography.titleSmall.copy(
          fontWeight = FontWeight.Bold,
          color = ClinicalTextPrimary
        )
      )

      Spacer(modifier = Modifier.height(4.dp))

      Text(
        text = pattern.description,
        style = MaterialTheme.typography.bodySmall.copy(
          color = ClinicalTextSecondary,
          fontSize = 12.sp
        )
      )

      Spacer(modifier = Modifier.height(10.dp))
      HorizontalDivider(color = ClinicalOutline, thickness = 0.8.dp)
      Spacer(modifier = Modifier.height(8.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Observed: ${pattern.observedCount} times • Last seen: ${pattern.lastSeen}",
          style = MaterialTheme.typography.labelSmall.copy(
            color = ClinicalTextSecondary,
            fontSize = 11.sp
          )
        )
        Text(
          text = "Response: ${pattern.typicalResponse}",
          style = MaterialTheme.typography.labelSmall.copy(
            color = MedicalBluePrimary,
            fontWeight = FontWeight.SemiBold
          )
        )
      }
    }
  }
}
