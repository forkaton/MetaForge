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

            // Single dark/light mode toggle button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(MFColors.BgCard)
                    .border(2.dp, MFColors.Accent, RoundedCornerShape(14.dp))
                    .clickable { scope.launch { themePrefs.setDarkTheme(!isDark) } }
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Icon(
                        imageVector = if (isDark) Icons.Default.DarkMode else Icons.Default.LightMode,
                        contentDescription = null,
                        tint = MFColors.Accent,
                        modifier = Modifier.size(24.dp)
                    )
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
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(text, color = MFColors.TextHint, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
}
