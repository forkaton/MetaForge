package com.example.metaforge.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.metaforge.ui.theme.MFColors
import com.example.metaforge.ui.theme.MFThemeState

private val DarkColorScheme = darkColorScheme(
    background        = Color(0xFF08223A),
    surface           = Color(0xFF092B49),
    surfaceVariant    = Color(0xFF0E3A61),
    primary           = Color(0xFF4EADDF),
    secondary         = Color(0xFF2B6599),
    error             = Color(0xFFE84343),
    onBackground      = Color(0xFFE8F4FD),
    onSurface         = Color(0xFFE8F4FD),
    onPrimary         = Color(0xFF08223A),
    onSecondary       = Color.White,
    primaryContainer  = Color(0xFF0E3A61),
    onPrimaryContainer= Color(0xFF4EADDF)
)

private val LightColorScheme = lightColorScheme(
    background        = Color(0xFFEFF8FF),
    surface           = Color(0xFFDCF0FF),
    surfaceVariant    = Color(0xFFC4E5FA),
    primary           = Color(0xFF1A6FA8),
    secondary         = Color(0xFF194E7D),
    error             = Color(0xFFE84343),
    onBackground      = Color(0xFF08223A),
    onSurface         = Color(0xFF08223A),
    onPrimary         = Color.White,
    onSecondary       = Color.White,
    primaryContainer  = Color(0xFFC4E5FA),
    onPrimaryContainer= Color(0xFF08223A)
)

@Composable
fun MetaForgeTheme(
    isDark: Boolean = MFThemeState.isDark,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (isDark) DarkColorScheme else LightColorScheme,
        content = content
    )
}
