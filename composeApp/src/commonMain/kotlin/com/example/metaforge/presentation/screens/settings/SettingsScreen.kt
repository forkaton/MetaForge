package com.example.metaforge.presentation.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.metaforge.presentation.theme.MetaForgeColors
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "SETTINGS",
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MetaForgeColors.DeepNavy
                )
            )
        },
        containerColor = MetaForgeColors.DeepNavy
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // App Info Section
            SettingsSection(title = "APP INFO") {
                SettingRow("Version", uiState.appVersion,
                    MetaForgeColors.CyberBlue)
                HorizontalDivider(color = MetaForgeColors.DarkCard)
                SettingRow("Hero Database",
                    "${uiState.heroCount} Meta Heroes",
                    MetaForgeColors.CyberGold)
                HorizontalDivider(color = MetaForgeColors.DarkCard)
                SettingRow("AI Engine", uiState.aiEngine,
                    MetaForgeColors.GreenSuccess)
            }

            // About Section
            SettingsSection(title = "ABOUT") {
                SettingRow("Project", "Sprint 2 — Core Features",
                    Color.White)
                HorizontalDivider(color = MetaForgeColors.DarkCard)
                SettingRow("Platform",
                    "Kotlin Multiplatform",
                    Color.White)
                HorizontalDivider(color = MetaForgeColors.DarkCard)
                SettingRow("Architecture",
                    "Clean Architecture + MVVM",
                    Color.White)
            }

            // Coming Soon Section
            SettingsSection(title = "COMING IN SPRINT 3") {
                ComingSoonRow("Gemini AI Integration")
                HorizontalDivider(color = MetaForgeColors.DarkCard)
                ComingSoonRow("Real-time Counter Pick")
                HorizontalDivider(color = MetaForgeColors.DarkCard)
                ComingSoonRow("Team Synergy Analyzer")
            }
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Text(
        title,
        color = MetaForgeColors.CyberBlue,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp
    )
    Spacer(Modifier.height(8.dp))
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MetaForgeColors.DarkCard
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            content = content
        )
    }
}

@Composable
private fun SettingRow(label: String, value: String, valueColor: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            color = MetaForgeColors.TextSecondary,
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            value,
            color = valueColor,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun ComingSoonRow(feature: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            feature,
            color = MetaForgeColors.TextSecondary,
            style = MaterialTheme.typography.bodyMedium
        )
        Box(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
        ) {
            Text(
                "SOON",
                color = MetaForgeColors.OrangeWarning,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                fontSize = 9.sp
            )
        }
    }
}