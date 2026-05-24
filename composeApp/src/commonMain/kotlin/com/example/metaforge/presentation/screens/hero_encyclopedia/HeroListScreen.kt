package com.example.metaforge.presentation.screens.hero_encyclopedia

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.example.metaforge.domain.model.*
import com.example.metaforge.ui.theme.MFColors
import org.koin.compose.viewmodel.koinViewModel

// Extension to convert domain Long color value to Compose Color
internal fun HeroTier.color(): Color = Color(colorValue)

@Composable
fun HeroListScreen(
    onNavigateToHeroInfo: (Int) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: TierListViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = MFColors.Bg,
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(
                title = { HeroListTopBar() },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = MFColors.Accent)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MFColors.BgCard)
            )
        }
    ) { padding ->
        when (val state = uiState) {
            is TierListUiState.Loading -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MFColors.Accent)
            }
            is TierListUiState.Error -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(state.message, color = MFColors.EnemyRed)
            }
            is TierListUiState.Ready -> TierListContent(
                state = state,
                onLaneFilter = viewModel::filterByLane,
                onHeroClick = { onNavigateToHeroInfo(it.id) },
                modifier = Modifier.padding(padding)
            )
        }
    }
}

@Composable
private fun HeroListTopBar() {
    Column {
        Text(
            text = "Hero Tier List",
            color = MFColors.Accent,
            fontWeight = FontWeight.Black,
            fontSize = 17.sp
        )
        Text(
            text = "Current meta rankings • May 2026",
            color = MFColors.TextHint,
            fontSize = 10.sp
        )
    }
}

@Composable
private fun TierListContent(
    state: TierListUiState.Ready,
    onLaneFilter: (HeroLane?) -> Unit,
    onHeroClick: (HeroMetaEntry) -> Unit,
    modifier: Modifier = Modifier
) {
    val herosByTier = state.filteredHeroes.groupBy { it.tier }
    val tierOrder = HeroTier.entries

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        item { LaneFilterRow(selected = state.selectedLane, onSelect = onLaneFilter) }
        item { Spacer(Modifier.height(8.dp)) }

        tierOrder.forEach { tier ->
            val heroes = herosByTier[tier]
            if (!heroes.isNullOrEmpty()) {
                item { TierSectionHeader(tier = tier, heroCount = heroes.size) }
                item {
                    HeroTierRow(heroes = heroes, onHeroClick = onHeroClick)
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun LaneFilterRow(selected: HeroLane?, onSelect: (HeroLane?) -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text(
            text = "Filter by Lane",
            color = MFColors.TextSecondary,
            fontSize = 11.sp,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            LaneChip(label = "All", selected = selected == null, onClick = { onSelect(null) })
            HeroLane.entries.forEach { lane ->
                LaneChip(
                    label = lane.displayName,
                    selected = selected == lane,
                    onClick = { onSelect(lane) }
                )
            }
        }
    }
}

@Composable
private fun LaneChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val bg = if (selected) MFColors.Accent else MFColors.BgCard
    val textColor = if (selected) MFColors.Bg else MFColors.TextPrimary
    val borderColor = if (selected) MFColors.Accent else MFColors.BgElevated
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bg)
            .border(1.dp, borderColor, RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
private fun TierSectionHeader(tier: HeroTier, heroCount: Int) {
    val tierColor = tier.color()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(width = 52.dp, height = 52.dp)
                .background(tierColor, RoundedCornerShape(6.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = tier.label,
                color = Color.White,
                fontWeight = FontWeight.Black,
                fontSize = 18.sp,
                textAlign = TextAlign.Center
            )
        }
        Spacer(Modifier.width(12.dp))
        Column {
            Text(
                text = "${tier.label} TIER",
                color = tierColor,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Text(text = tier.description, color = MFColors.TextSecondary, fontSize = 11.sp)
        }
        Spacer(Modifier.weight(1f))
        Text(text = "$heroCount heroes", color = MFColors.TextHint, fontSize = 11.sp)
    }
}

@Composable
private fun HeroTierRow(heroes: List<HeroMetaEntry>, onHeroClick: (HeroMetaEntry) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .background(MFColors.BgCard, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            heroes.forEach { hero ->
                HeroTierCard(hero = hero, onClick = { onHeroClick(hero) })
            }
        }
    }
}

@Composable
private fun HeroTierCard(hero: HeroMetaEntry, onClick: () -> Unit) {
    val tierColor = hero.tier.color()
    Column(
        modifier = Modifier
            .width(72.dp)
            .clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box {
            AsyncImage(
                model = hero.portraitUrl,
                contentDescription = hero.name,
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(MFColors.BgElevated)
                    .border(2.dp, tierColor.copy(alpha = 0.6f), CircleShape)
            )
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(18.dp)
                    .background(tierColor, CircleShape)
                    .border(1.dp, Color.Black, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = hero.tier.label,
                    color = Color.White,
                    fontSize = 7.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = hero.name,
            color = MFColors.TextPrimary,
            fontSize = 10.sp,
            maxLines = 1,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
