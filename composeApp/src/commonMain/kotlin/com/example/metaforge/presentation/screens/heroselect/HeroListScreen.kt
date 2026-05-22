package com.example.metaforge.presentation.screens.heroselect

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.metaforge.presentation.components.LoadingIndicator
import com.example.metaforge.presentation.theme.MetaForgeColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HeroListScreen(
    onNavigateToHeroInfo: (Int, String, String) -> Unit,
    viewModel: HeroSelectViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var isRefreshing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val pullRefreshState = rememberPullToRefreshState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "HERO ENCYCLOPEDIA",
                        fontWeight = FontWeight.Black,
                        color = MetaForgeColors.CyberGold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MetaForgeColors.DeepNavy
                )
            )
        },
        containerColor = MetaForgeColors.DeepNavy
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = {
                scope.launch {
                    isRefreshing = true
                    delay(1000) // Simulasi refresh
                    isRefreshing = false
                }
            },
            state = pullRefreshState,
            modifier = Modifier.padding(padding)
        ) {
            when (val state = uiState) {
                is HeroSelectUiState.Loading -> LoadingIndicator()

                is HeroSelectUiState.Ready -> {
                    val tierOrder = listOf(
                        "SS Tier (Ban Priority)",
                        "S Tier (Pick Priority)",
                        "A Tier (Situational)"
                    )

                    val groupedHeroes = state.allHeroes.groupBy { hero ->
                        when (hero.name) {
                            "Fanny", "Mathilda", "Joy", "Ling" ->
                                "SS Tier (Ban Priority)"
                            "Chou", "Khufra", "Beatrix",
                            "Novaria", "Valentina" ->
                                "S Tier (Pick Priority)"
                            else -> "A Tier (Situational)"
                        }
                    }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        // Stats header
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                TierStatChip(
                                    "SS", "4 heroes",
                                    MetaForgeColors.CyberRed,
                                    Modifier.weight(1f)
                                )
                                TierStatChip(
                                    "S", "5 heroes",
                                    MetaForgeColors.CyberGold,
                                    Modifier.weight(1f)
                                )
                                TierStatChip(
                                    "A", "${state.allHeroes.size - 9} heroes",
                                    MetaForgeColors.CyberBlue,
                                    Modifier.weight(1f)
                                )
                            }
                        }

                        tierOrder.forEach { tier ->
                            val heroesInTier = groupedHeroes[tier]
                            if (!heroesInTier.isNullOrEmpty()) {
                                item {
                                    TierHeader(tier)
                                }
                                items(heroesInTier) { hero ->
                                    HeroTierCard(
                                        heroName = hero.name,
                                        roleName = hero.role.displayName,
                                        specialty = hero.specialty,
                                        tier = tier,
                                        onClick = {
                                            onNavigateToHeroInfo(
                                                hero.id,
                                                hero.name,
                                                hero.role.displayName
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                else -> {}
            }
        }
    }
}

@Composable
private fun TierStatChip(
    tier: String,
    count: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                tier,
                color = color,
                fontWeight = FontWeight.Black,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                count,
                color = MetaForgeColors.TextSecondary,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

@Composable
private fun TierHeader(tier: String) {
    val color = when {
        tier.contains("SS") -> MetaForgeColors.CyberRed
        tier.contains("S Tier") -> MetaForgeColors.CyberGold
        else -> MetaForgeColors.CyberBlue
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        HorizontalDivider(
            modifier = Modifier.weight(0.3f),
            color = color.copy(alpha = 0.5f)
        )
        Text(
            tier,
            color = color,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.labelMedium
        )
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = color.copy(alpha = 0.5f)
        )
    }
}

@Composable
private fun HeroTierCard(
    heroName: String,
    roleName: String,
    specialty: String,
    tier: String,
    onClick: () -> Unit
) {
    val accentColor = when {
        tier.contains("SS") -> MetaForgeColors.CyberRed
        tier.contains("S Tier") -> MetaForgeColors.CyberGold
        else -> MetaForgeColors.CyberBlue
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MetaForgeColors.DarkCard
        ),
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(accentColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    heroName.take(2).uppercase(),
                    color = accentColor,
                    fontWeight = FontWeight.Black,
                    style = MaterialTheme.typography.labelMedium
                )
            }

            // Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    heroName,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    "$roleName • $specialty",
                    color = MetaForgeColors.TextSecondary,
                    style = MaterialTheme.typography.labelSmall
                )
            }

            // Tier Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(accentColor.copy(alpha = 0.15f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    tier.split(" ").first(),
                    color = accentColor,
                    fontWeight = FontWeight.Black,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}
