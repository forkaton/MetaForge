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
import com.example.metaforge.data.local.HeroMetaRepository
import com.example.metaforge.domain.model.*
import com.example.metaforge.ui.theme.MFColors
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HeroInfoScreen(
    heroId: Int,
    onNavigateBack: () -> Unit,
    viewModel: TierListViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val hero = (uiState as? TierListUiState.Ready)?.heroes?.find { it.id == heroId }

    Scaffold(
        containerColor = MFColors.Bg,
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    Row(
                        modifier = Modifier.clickable { onNavigateBack() }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = MFColors.Accent)
                        Spacer(Modifier.width(4.dp))
                        Text("Back to Heroes", color = MFColors.Accent, fontSize = 14.sp)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MFColors.Bg)
            )
        }
    ) { padding ->
        if (hero == null) {
            Box(Modifier.fillMaxSize(), Alignment.Center) {
                CircularProgressIndicator(color = MFColors.Accent)
            }
        } else {
            HeroDetailContent(hero = hero, modifier = Modifier.padding(padding))
        }
    }
}

@Composable
private fun HeroDetailContent(hero: HeroMetaEntry, modifier: Modifier = Modifier) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Overview", "Matchups")

    LazyColumn(modifier = modifier.fillMaxSize()) {
        item { HeroDetailHeader(hero) }
        item {
            TabRow(selectedTabIndex = selectedTab,
                containerColor = MFColors.BgCard, contentColor = MFColors.Accent) {
                tabs.forEachIndexed { i, tab ->
                    Tab(selected = selectedTab == i, onClick = { selectedTab = i },
                        text = {
                            Text(tab,
                                color = if (selectedTab == i) MFColors.Accent else MFColors.TextSecondary,
                                fontWeight = if (selectedTab == i) FontWeight.Bold else FontWeight.Normal)
                        })
                }
            }
        }
        item { when (selectedTab) { 0 -> OverviewTab(hero); 1 -> MatchupsTab(hero) } }
    }
}

@Composable
private fun HeroDetailHeader(hero: HeroMetaEntry) {
    val tierColor = hero.tier.color()
    Column(modifier = Modifier.fillMaxWidth().background(MFColors.BgCard).padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box {
                AsyncImage(model = hero.portraitUrl, contentDescription = hero.name,
                    modifier = Modifier.size(80.dp).clip(CircleShape)
                        .background(MFColors.BgElevated).border(2.dp, tierColor, CircleShape))
                Box(modifier = Modifier.align(Alignment.BottomCenter)
                    .background(MFColors.BgElevated, RoundedCornerShape(4.dp))
                    .padding(horizontal = 4.dp, vertical = 1.dp)) {
                    Text(hero.heroClass, color = MFColors.TextSecondary, fontSize = 9.sp)
                }
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(hero.name, color = MFColors.TextPrimary, fontWeight = FontWeight.Black, fontSize = 24.sp)
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TierBadge(hero.tier)
                    Text("—", color = MFColors.TextHint)
                    Text("Score: ${"%.1f".format(hero.overallScore)}", color = MFColors.Warning, fontSize = 13.sp)
                }
                Spacer(Modifier.height(6.dp))
                if (hero.lanes.isNotEmpty()) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        hero.lanes.take(3).forEach { lane ->
                            MetaTag(lane.displayName, MFColors.BgElevated, MFColors.Accent.copy(alpha = 0.5f))
                        }
                    }
                }
                if (hero.speciality.isNotEmpty()) {
                    Spacer(Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        hero.speciality.take(3).forEach { spec ->
                            MetaTag(spec, MFColors.BgElevated, MFColors.Success.copy(alpha = 0.4f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TierBadge(tier: HeroTier) {
    Box(modifier = Modifier.background(tier.color(), RoundedCornerShape(6.dp))
        .padding(horizontal = 8.dp, vertical = 3.dp)) {
        Text(tier.label, color = Color.White, fontWeight = FontWeight.Black, fontSize = 13.sp)
    }
}

@Composable
private fun MetaTag(text: String, bg: Color, borderColor: Color) {
    Box(modifier = Modifier.background(bg, RoundedCornerShape(4.dp))
        .border(1.dp, borderColor, RoundedCornerShape(4.dp))
        .padding(horizontal = 6.dp, vertical = 2.dp)) {
        Text(text, color = MFColors.TextPrimary, fontSize = 10.sp)
    }
}

@Composable
private fun OverviewTab(hero: HeroMetaEntry) {
    var selectedStatType by remember { mutableStateOf(StatType.WIN_RATE) }
    var selectedPeriod by remember { mutableStateOf(TimePeriod.ALL) }
    val stats = remember(selectedPeriod) {
        HeroMetaRepository.generateRankStats(hero.id, hero.tier, selectedPeriod)
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Statistics", color = MFColors.TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Spacer(Modifier.height(12.dp))

        // Stat type selector
        Row(modifier = Modifier.fillMaxWidth().background(MFColors.BgCard, RoundedCornerShape(8.dp))) {
            StatType.entries.forEach { statType ->
                val selected = selectedStatType == statType
                Box(
                    modifier = Modifier.weight(1f)
                        .background(if (selected) MFColors.Accent else Color.Transparent, RoundedCornerShape(8.dp))
                        .clickable { selectedStatType = statType }.padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        statType.name.replace("_", " ").split(" ")
                            .joinToString(" ") { it.lowercase().replaceFirstChar { c -> c.uppercase() } },
                        color = if (selected) MFColors.Bg else MFColors.TextSecondary,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal, fontSize = 12.sp
                    )
                }
            }
        }
        Spacer(Modifier.height(12.dp))

        // Time period filter
        Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically) {
            Text("Time:", color = MFColors.TextSecondary, fontSize = 12.sp)
            Spacer(Modifier.width(4.dp))
            TimePeriod.entries.forEach { period ->
                val selected = selectedPeriod == period
                Box(
                    modifier = Modifier
                        .background(if (selected) MFColors.BgElevated else Color.Transparent, RoundedCornerShape(4.dp))
                        .border(1.dp, if (selected) MFColors.Accent else MFColors.BgElevated, RoundedCornerShape(4.dp))
                        .clickable { selectedPeriod = period }.padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(period.label, color = if (selected) MFColors.Accent else MFColors.TextSecondary, fontSize = 10.sp)
                }
            }
        }
        Spacer(Modifier.height(12.dp))

        // Rank stat bars
        stats.forEach { rankStat ->
            val value = when (selectedStatType) {
                StatType.WIN_RATE -> rankStat.winRate
                StatType.PICK_RATE -> rankStat.pickRate
                StatType.BAN_RATE -> rankStat.banRate
            }
            val maxVal = when (selectedStatType) { StatType.WIN_RATE -> 65f; StatType.PICK_RATE -> 40f; StatType.BAN_RATE -> 90f }
            val barColor = when {
                selectedStatType == StatType.WIN_RATE && value >= 52f -> MFColors.Warning
                selectedStatType == StatType.WIN_RATE && value >= 50f -> MFColors.Success
                selectedStatType == StatType.BAN_RATE && value >= 30f -> MFColors.BanRed
                else -> MFColors.EnemyRed
            }
            RankStatRow(rankStat.rankName, value, maxVal, barColor, isPercent = true)
            Spacer(Modifier.height(10.dp))
        }
    }
}

@Composable
private fun RankStatRow(rankName: String, value: Float, maxValue: Float, barColor: Color, isPercent: Boolean) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(rankName, color = MFColors.TextSecondary, fontSize = 12.sp, modifier = Modifier.width(110.dp))
        Spacer(Modifier.width(8.dp))
        Box(modifier = Modifier.weight(1f).height(20.dp).background(MFColors.BgElevated, RoundedCornerShape(10.dp))) {
            Box(modifier = Modifier.fillMaxHeight()
                .fillMaxWidth((value / maxValue).coerceIn(0f, 1f))
                .background(barColor, RoundedCornerShape(10.dp)))
        }
        Spacer(Modifier.width(8.dp))
        Text(if (isPercent) "${"%.1f".format(value)}%" else "%.2f".format(value),
            color = barColor, fontSize = 12.sp, fontWeight = FontWeight.Bold,
            modifier = Modifier.width(44.dp), textAlign = TextAlign.End)
    }
}

@Composable
private fun MatchupsTab(hero: HeroMetaEntry) {
    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        MatchupSection("Counters", "Heroes ${hero.name} is strong against", hero.strongAgainst, MFColors.Success)
        MatchupSection("Weak Against", "Heroes that counter ${hero.name}", hero.weakAgainst, MFColors.EnemyRed)
        MatchupSection("Synergies", "Best team combo with ${hero.name}", hero.synergies, MFColors.AllyBlue)
    }
}

@Composable
private fun MatchupSection(title: String, subtitle: String, heroes: List<HeroMatchupEntry>, accentColor: Color) {
    Column(modifier = Modifier.fillMaxWidth().background(MFColors.BgCard, RoundedCornerShape(12.dp)).padding(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(8.dp).background(accentColor, CircleShape))
            Spacer(Modifier.width(8.dp))
            Text(title, color = accentColor, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
        Spacer(Modifier.height(4.dp))
        Text(subtitle, color = MFColors.TextHint, fontSize = 10.sp)
        Spacer(Modifier.height(10.dp))
        if (heroes.isEmpty()) {
            Text("No data available", color = MFColors.TextSecondary, fontSize = 12.sp)
        } else {
            heroes.forEach { matchup ->
                MatchupHeroRow(matchup)
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun MatchupHeroRow(matchup: HeroMatchupEntry) {
    val tierColor = matchup.tier.color()
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Box(modifier = Modifier.size(36.dp).background(MFColors.BgElevated, CircleShape)
            .border(1.5.dp, tierColor, CircleShape), contentAlignment = Alignment.Center) {
            Text(matchup.name.take(2).uppercase(), color = MFColors.TextPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(matchup.name, color = MFColors.TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Text("Score: ${"%.2f".format(matchup.score)}", color = MFColors.TextSecondary, fontSize = 11.sp)
            }
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.weight(1f).height(6.dp).background(MFColors.BgElevated, RoundedCornerShape(3.dp))) {
                    Box(modifier = Modifier.fillMaxHeight().fillMaxWidth((matchup.score / 7f).coerceIn(0f, 1f))
                        .background(tierColor, RoundedCornerShape(3.dp)))
                }
                Spacer(Modifier.width(8.dp))
                Box(modifier = Modifier.background(tierColor, RoundedCornerShape(4.dp)).padding(horizontal = 5.dp, vertical = 2.dp)) {
                    Text(matchup.tier.label, color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
