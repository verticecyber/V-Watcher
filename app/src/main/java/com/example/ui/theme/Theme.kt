package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme =
  darkColorScheme(
    primary = MedicalBlueLight,
    onPrimary = MedicalBlueDark,
    primaryContainer = MedicalBlueDark,
    onPrimaryContainer = MedicalBlueLight,
    secondary = MedicalTealSecondary,
    onSecondary = Color.White,
    background = Color(0xFF0B132B),
    surface = Color(0xFF131E3A),
    onBackground = Color(0xFFE2E8F0),
    onSurface = Color(0xFFF1F5F9),
    surfaceVariant = Color(0xFF1E293B),
    onSurfaceVariant = Color(0xFF94A3B8),
    outline = Color(0xFF334155),
  )

private val LightColorScheme =
  lightColorScheme(
    primary = MedicalBluePrimary,
    onPrimary = Color.White,
    primaryContainer = MedicalBlueContainer,
    onPrimaryContainer = MedicalBlueDark,
    secondary = MedicalTealSecondary,
    onSecondary = Color.White,
    secondaryContainer = MedicalTealContainer,
    onSecondaryContainer = MedicalTealSecondary,
    tertiary = MemoryLavender,
    onTertiary = Color.White,
    background = ClinicalBackground,
    onBackground = ClinicalTextPrimary,
    surface = ClinicalSurface,
    onSurface = ClinicalTextPrimary,
    surfaceVariant = ClinicalSurfaceVariant,
    onSurfaceVariant = ClinicalTextSecondary,
    outline = ClinicalOutline,
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // For V-Watcher, preserve intentional medical clinical styling
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
