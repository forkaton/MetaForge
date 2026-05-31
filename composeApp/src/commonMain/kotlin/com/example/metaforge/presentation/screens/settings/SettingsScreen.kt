package com.example.metaforge.presentation.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.metaforge.core.util.formatLastFetched
import com.example.metaforge.data.local.datastore.DraftPreferences
import com.example.metaforge.data.local.datastore.ThemePreferences
import com.example.metaforge.presentation.components.pressScale
import com.example.metaforge.ui.theme.MFColors
import com.example.metaforge.ui.theme.MFThemeState
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onNavigateBack: () -> Unit) {
    val themePrefs: ThemePreferences = koinInject()
    val draftPrefs: DraftPreferences = koinInject()
    val isDark by themePrefs.isDarkTheme().collectAsStateWithLifecycle(initialValue = MFThemeState.isDark)
    val lastFetchedAt by draftPrefs.getLastFetchedAt().collectAsStateWithLifecycle(initialValue = null)
    val lastFetchLabel = formatLastFetched(lastFetchedAt)
    val scope = rememberCoroutineScope()

    Scaffold(
        containerColor = MFColors.Bg,
        topBar = {
            TopAppBar(
                title = { Text("SETTINGS", color = MFColors.TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp, letterSpacing = 0.5.sp) },
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

            // Toggle dark/light dengan gesture press-scale
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(MFColors.BgCard)
                    .border(2.dp, MFColors.Accent, RoundedCornerShape(14.dp))
                    .pressScale { scope.launch { themePrefs.setDarkTheme(!isDark) } }
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MFColors.Accent.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isDark) Icons.Default.DarkMode else Icons.Default.LightMode,
                            contentDescription = null,
                            tint = MFColors.Accent,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            if (isDark) "Dark Mode" else "Light Mode",
                            color = MFColors.TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            "Tap to switch to ${if (isDark) "Light" else "Dark"} mode",
                            color = MFColors.TextHint,
                            fontSize = 11.sp
                        )
                    }
                }
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
                        "Hero data is fetched from GitHub on each launch and cached locally. App works offline using the last synced data.",
                        color = MFColors.TextHint,
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )
                }
            }

            Spacer(Modifier.height(4.dp))
            HorizontalDivider(color = MFColors.BgElevated)
            Spacer(Modifier.height(4.dp))

            SectionLabel("ABOUT")

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MFColors.BgCard, RoundedCornerShape(14.dp))
                    .border(1.dp, MFColors.BgElevated, RoundedCornerShape(14.dp))
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier
                            .size(8.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(MFColors.Accent)
                    )
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text("MetaForge", color = MFColors.TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(Modifier.height(2.dp))
                        Text("Mobile Legends Meta Analyzer • Last fetch: $lastFetchLabel", color = MFColors.TextHint, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .size(width = 3.dp, height = 13.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(MFColors.Accent)
        )
        Spacer(Modifier.width(8.dp))
        Text(text, color = MFColors.TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
    }
}