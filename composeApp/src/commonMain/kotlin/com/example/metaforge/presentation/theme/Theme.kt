package com.example.metaforge.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// MetaForge Color Palette
object MetaForgeColors {
    val CyberBlue = Color(0xFF00BFFF)
    val CyberGold = Color(0xFFFFD700)
    val CyberRed = Color(0xFFFF4444)
    val DeepNavy = Color(0xFF0D0D1A)
    val DarkCard = Color(0xFF16213E)
    val DarkerCard = Color(0xFF0F3460)
    val SurfaceCard = Color(0xFF1A1A2E)
    val TextPrimary = Color(0xFFE0E0E0)
    val TextSecondary = Color(0xFF9E9E9E)
    val GreenSuccess = Color(0xFF00E676)
    val OrangeWarning = Color(0xFFFF9800)
}

private val MetaForgeDarkScheme = darkColorScheme(
    primary = MetaForgeColors.CyberBlue,
    secondary = MetaForgeColors.CyberGold,
    tertiary = MetaForgeColors.GreenSuccess,
    error = MetaForgeColors.CyberRed,
    background = MetaForgeColors.DeepNavy,
    surface = MetaForgeColors.DarkCard,
    surfaceVariant = MetaForgeColors.SurfaceCard,
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onBackground = MetaForgeColors.TextPrimary,
    onSurface = MetaForgeColors.TextPrimary,
    onSurfaceVariant = MetaForgeColors.TextSecondary,
    outline = MetaForgeColors.CyberBlue.copy(alpha = 0.3f)
)

@Composable
fun MetaForgeTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = MetaForgeDarkScheme,
        content = content
    )
}
