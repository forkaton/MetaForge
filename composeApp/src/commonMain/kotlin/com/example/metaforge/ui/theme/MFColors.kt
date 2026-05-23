package com.example.metaforge.ui.theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

object MFThemeState {
    var isDark: Boolean by mutableStateOf(true)
}

object MFColors {
    // ── Dynamic (dark/light) ──────────────────────────────────────────────────
    val Bg         get() = if (MFThemeState.isDark) Color(0xFF08223A) else Color(0xFFEFF8FF)
    val BgCard     get() = if (MFThemeState.isDark) Color(0xFF092B49) else Color(0xFFDCF0FF)
    val BgElevated get() = if (MFThemeState.isDark) Color(0xFF0E3A61) else Color(0xFFC4E5FA)
    val Primary    get() = Color(0xFF194E7D)
    val PrimaryLt  get() = Color(0xFF2B6599)
    val Accent     get() = if (MFThemeState.isDark) Color(0xFF4EADDF) else Color(0xFF1A6FA8)
    val TextPrimary    get() = if (MFThemeState.isDark) Color(0xFFE8F4FD) else Color(0xFF08223A)
    val TextSecondary  get() = if (MFThemeState.isDark) Color(0xFF8BADC5) else Color(0xFF1A4A6D)
    val TextHint       get() = if (MFThemeState.isDark) Color(0xFF4F6E87) else Color(0xFF5A7B9A)

    // ── Static functional ─────────────────────────────────────────────────────
    val AllyBlue = Color(0xFF3B9FE3)
    val EnemyRed = Color(0xFFE84343)
    val BanRed   = Color(0xFFDC2626)
    val Success  = Color(0xFF4CAF50)
    val Warning  = Color(0xFFFFB300)

    // ── Tier ──────────────────────────────────────────────────────────────────
    val TierSS = Color(0xFF9C27B0)
    val TierS  = Color(0xFFF44336)
    val TierA  = Color(0xFFFF9800)
    val TierB  = Color(0xFFFFEB3B)
    val TierC  = Color(0xFF4CAF50)
    val TierD  = Color(0xFF2196F3)
}
