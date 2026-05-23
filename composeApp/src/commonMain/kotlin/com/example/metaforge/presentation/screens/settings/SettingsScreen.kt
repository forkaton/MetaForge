package com.example.metaforge.presentation.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.metaforge.data.local.datastore.ThemePreferences
import com.example.metaforge.ui.theme.MFColors
import com.example.metaforge.ui.theme.MFThemeState
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onNavigateBack: () -> Unit) {
    val themePrefs: ThemePreferences = koinInject()
    val isDark by themePrefs.isDarkTheme().collectAsStateWithLifecycle(initialValue = MFThemeState.isDark)
    val scope = rememberCoroutineScope()

    Scaffold(
        containerColor = MFColors.Bg,
        topBar = {
            TopAppBar(
                title = { Text("SETTINGS", color = MFColors.TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = MFColors.Accent)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MFColors.BgCard)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MFColors.Bg)
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(Modifier.height(4.dp))

            SectionLabel("APPEARANCE")

            // Dark / Light toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MFColors.BgCard, RoundedCornerShape(14.dp))
                    .border(1.dp, MFColors.BgElevated, RoundedCornerShape(14.dp))
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isDark) Icons.Default.DarkMode else Icons.Default.LightMode,
                    contentDescription = null,
                    tint = MFColors.Accent,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Dark Mode", color = MFColors.TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Text(
                        if (isDark) "Using dark theme" else "Using light theme",
                        color = MFColors.TextHint,
                        fontSize = 11.sp
                    )
                }
                Switch(
                    checked = isDark,
                    onCheckedChange = { scope.launch { themePrefs.setDarkTheme(it) } },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor   = MFColors.Bg,
                        checkedTrackColor   = MFColors.Accent,
                        uncheckedThumbColor = MFColors.TextHint,
                        uncheckedTrackColor = MFColors.BgElevated
                    )
                )
            }

            // Preview chips
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ThemePreviewChip(
                    label = "Dark",
                    selected = isDark,
                    bg = Color(0xFF08223A),
                    text = Color(0xFFE8F4FD),
                    modifier = Modifier.weight(1f),
                    onClick = { scope.launch { themePrefs.setDarkTheme(true) } }
                )
                ThemePreviewChip(
                    label = "Light",
                    selected = !isDark,
                    bg = Color(0xFFEFF8FF),
                    text = Color(0xFF08223A),
                    modifier = Modifier.weight(1f),
                    onClick = { scope.launch { themePrefs.setDarkTheme(false) } }
                )
            }

            Spacer(Modifier.height(4.dp))
            HorizontalDivider(color = MFColors.BgElevated)
            Spacer(Modifier.height(4.dp))

            SectionLabel("DATA")

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MFColors.BgCard, RoundedCornerShape(14.dp))
                    .border(1.dp, MFColors.BgElevated, RoundedCornerShape(14.dp))
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Offline Mode", color = MFColors.TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Spacer(Modifier.height(3.dp))
                    Text(
                        "Hero data is cached locally. App works offline using the last synced data from the MLBB API.",
                        color = MFColors.TextHint,
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun ThemePreviewChip(
    label: String,
    selected: Boolean,
    bg: Color,
    text: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(bg)
            .border(2.dp, if (selected) MFColors.Accent else MFColors.BgElevated, RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = text, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal, fontSize = 13.sp)
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(text, color = MFColors.TextHint, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
}
