package com.example.metaforge.presentation.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.metaforge.presentation.components.ErrorMessage
import com.example.metaforge.presentation.components.LoadingIndicator
import com.example.metaforge.presentation.theme.MetaForgeColors
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun HomeScreen(
    onNavigateToDraftSetup: () -> Unit,
    onNavigateToHeroList: () -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: HomeViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when (val state = uiState) {
        is HomeUiState.Loading -> LoadingIndicator()
        is HomeUiState.Error -> ErrorMessage(
            message = state.message,
            onRetry = null
        )
        is HomeUiState.Ready -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MetaForgeColors.DeepNavy)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header Banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    MetaForgeColors.DarkerCard,
                                    MetaForgeColors.DeepNavy
                                )
                            )
                        )
                        .padding(24.dp),
                    contentAlignment = Alignment.BottomStart
                ) {
                    Column {
                        Text(
                            text = "META",
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Black,
                            color = MetaForgeColors.CyberBlue,
                            letterSpacing = 4.sp,
                            lineHeight = 48.sp
                        )
                        Text(
                            text = "FORGE",
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            letterSpacing = 4.sp,
                            lineHeight = 48.sp
                        )
                        Text(
                            text = "MLBB Draft Intelligence System",
                            color = MetaForgeColors.TextSecondary,
                            style = MaterialTheme.typography.bodySmall,
                            letterSpacing = 1.sp
                        )
                    }
                }

                // Stats Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        "${state.heroCount}",
                        "META HEROES",
                        MetaForgeColors.CyberBlue,
                        Modifier.weight(1f)
                    )
                    StatCard(
                        "${state.roleCount}",
                        "ROLES",
                        MetaForgeColors.CyberGold,
                        Modifier.weight(1f)
                    )
                    StatCard("AI", "POWERED", MetaForgeColors.GreenSuccess, Modifier.weight(1f))
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Section Label
                Text(
                    text = "FEATURES",
                    color = MetaForgeColors.TextSecondary,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Feature Cards
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FeatureCard(
                        title = "DRAFT SIMULATOR",
                        subtitle = "5v5 Pick & Ban with real-time AI recommendations",
                        icon = Icons.Default.AutoAwesome,
                        accentColor = MetaForgeColors.CyberBlue,
                        tag = "CORE FEATURE",
                        onClick = onNavigateToDraftSetup
                    )
                    FeatureCard(
                        title = "HERO ENCYCLOPEDIA",
                        subtitle = "Tier list, counter picks & synergy analysis",
                        icon = Icons.Default.Leaderboard,
                        accentColor = MetaForgeColors.CyberGold,
                        tag = "30 HEROES",
                        onClick = onNavigateToHeroList
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun StatCard(
    value: String,
    label: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MetaForgeColors.DarkCard),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = color
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MetaForgeColors.TextSecondary,
                textAlign = TextAlign.Center,
                fontSize = 9.sp,
                letterSpacing = 0.5.sp
            )
        }
    }
}

@Composable
private fun FeatureCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    tag: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(
                        accentColor.copy(alpha = 0.15f),
                        MetaForgeColors.DarkCard
                    )
                )
            )
            .border(1.dp, accentColor.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(20.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Icon Box
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(accentColor.copy(alpha = 0.2f))
                    .border(1.dp, accentColor.copy(alpha = 0.5f), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = accentColor, modifier = Modifier.size(28.dp))
            }

            // Text
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        title,
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        style = MaterialTheme.typography.titleSmall,
                        letterSpacing = 0.5.sp
                    )
                    // Tag Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(accentColor.copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            tag,
                            color = accentColor,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    subtitle,
                    color = MetaForgeColors.TextSecondary,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                null,
                tint = accentColor.copy(alpha = 0.6f)
            )
        }
    }
}
