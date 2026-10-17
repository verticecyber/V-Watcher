package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// WHOOP Signature Precision Dark Color Scheme
private val WhoopColorScheme =
  darkColorScheme(
    primary = WhoopStrainBlue,
    onPrimary = WhoopBlackBg,
    primaryContainer = WhoopSurfaceElevated,
    onPrimaryContainer = WhoopStrainBlueGlow,
    secondary = WhoopSleepTeal,
    onSecondary = WhoopBlackBg,
    secondaryContainer = WhoopSurfaceElevated,
    onSecondaryContainer = WhoopSleepTeal,
    tertiary = WhoopRecoveryGreen,
    onTertiary = WhoopBlackBg,
    background = WhoopBlackBg,
    onBackground = WhoopTextPrimary,
    surface = WhoopSurface,
    onSurface = WhoopTextPrimary,
    surfaceVariant = WhoopSurfaceElevated,
    onSurfaceVariant = WhoopTextSecondary,
    outline = WhoopSurfaceBorder,
    outlineVariant = WhoopSurfaceBorderSubtle,
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  // WHOOP aesthetic is intrinsically dark mode for maximum contrast and biometric data clarity
  MaterialTheme(colorScheme = WhoopColorScheme, typography = Typography, content = content)
}

