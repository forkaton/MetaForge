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
        containerColor = Color(0xFF0D0D0D),
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    Row(
                        modifier = Modifier
                            .clickable { onNavigateBack() }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFF00BCD4)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "Back to Heroes",
                            color = Color(0xFF00BCD4),
                            fontSize = 14.sp
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0D0D0D))
            )
        }
    ) { padding ->
        if (hero == null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFF00BCD4))
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
        item { HeroDetailHeader(hero = hero) }
        item {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color(0xFF141428),
                contentColor = Color(0xFF00BCD4)
            ) {
                tabs.forEachIndexed { i, tab ->
                    Tab(
                        selected = selectedTab == i,
                        onClick = { selectedTab = i },
                        text = {
                            Text(
                                text = tab,
                                color = if (selectedTab == i) Color(0xFF00BCD4) else Color.Gray,
                                fontWeight = if (selectedTab == i) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }
        }
        item {
            when (selectedTab) {
                0 -> OverviewTab(hero = hero)
                1 -> MatchupsTab(hero = hero)
            }
        }
    }
}

@Composable
private fun HeroDetailHeader(hero: HeroMetaEntry) {
    val tierColor = hero.tier.color()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF141428))
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box {
                AsyncImage(
                    model = hero.portraitUrl,
                    contentDescription = hero.name,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF222244))
                        .border(2.dp, tierColor, CircleShape)
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .background(Color(0xFF333333), RoundedCornerShape(4.dp))
                        .padding(horizontal = 4.dp, vertical = 1.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = hero.heroClass, color = Color.White, fontSize = 9.sp)
                }
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = hero.name,
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 24.sp
                )
                Spacer(Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TierBadge(tier = hero.tier)
                    Text(text = "—", color = Color.Gray)
                    Text(
                        text = "Score: ${"%.1f".format(hero.overallScore)}",
                        color = Color(0xFFFF5722),
                        fontSize = 13.sp
                    )
                }
                Spacer(Modifier.height(6.dp))
                if (hero.lanes.isNotEmpty()) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        hero.lanes.take(3).forEach { lane ->
                            MetaTag(
                                text = lane.displayName,
                                color = Color(0xFF1A2A3A),
                                borderColor = Color(0xFF334455)
                            )
                        }
                    }
                }
                if (hero.speciality.isNotEmpty()) {
                    Spacer(Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        hero.speciality.take(3).forEach { spec ->
                            MetaTag(
                                text = spec,
                                color = Color(0xFF1A2A1A),
                                borderColor = Color(0xFF334433)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TierBadge(tier: HeroTier) {
    Box(
        modifier = Modifier
            .background(tier.color(), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = tier.label,
            color = Color.White,
            fontWeight = FontWeight.Black,
            fontSize = 13.sp
        )
    }
}

@Composable
private fun MetaTag(text: String, color: Color, borderColor: Color) {
    Box(
        modifier = Modifier
            .background(color, RoundedCornerShape(4.dp))
            .border(1.dp, borderColor, RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(text = text, color = Color.White, fontSize = 10.sp)
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
        Text(
            text = "Statistics",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
        Spacer(Modifier.height(12.dp))

        // Stat type selector
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF141428), RoundedCornerShape(8.dp))
        ) {
            StatType.entries.forEach { statType ->
                val selected = selectedStatType == statType
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(
                            color = if (selected) Color(0xFF00BCD4) else Color.Transparent,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable { selectedStatType = statType }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = statType.name.replace("_", " ")
                            .split(" ")
                            .joinToString(" ") { w ->
                                w.lowercase().replaceFirstChar { c -> c.uppercase() }
                            },
                        color = if (selected) Color.Black else Color.Gray,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 12.sp
                    )
                }
            }
        }
        Spacer(Modifier.height(12.dp))

        // Time period filter
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(text = "Time:", color = Color.Gray, fontSize = 12.sp)
            Spacer(Modifier.width(4.dp))
            TimePeriod.entries.forEach { period ->
                val selected = selectedPeriod == period
                Box(
                    modifier = Modifier
                        .background(
                            color = if (selected) Color(0xFF1A2A3A) else Color.Transparent,
                            shape = RoundedCornerShape(4.dp)
                        )
                        .border(
                            width = 1.dp,
                            color = if (selected) Color(0xFF00BCD4) else Color(0xFF333344),
                            shape = RoundedCornerShape(4.dp)
                        )
                        .clickable { selectedPeriod = period }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = period.label,
                        color = if (selected) Color(0xFF00BCD4) else Color.Gray,
                        fontSize = 10.sp
                    )
                }
            }
        }
        Spacer(Modifier.height(12.dp))

        // Rank stats bars
        stats.forEach { rankStat ->
            val value = when (selectedStatType) {
                StatType.WIN_RATE  -> rankStat.winRate
                StatType.PICK_RATE -> rankStat.pickRate
                StatType.BAN_RATE  -> rankStat.banRate
            }
            val maxVal = when (selectedStatType) {
                StatType.WIN_RATE  -> 65f
                StatType.PICK_RATE -> 40f
                StatType.BAN_RATE  -> 90f
            }
            val barColor = when {
                selectedStatType == StatType.WIN_RATE && value >= 52f -> Color(0xFFFFEB3B)
                selectedStatType == StatType.WIN_RATE && value >= 50f -> Color(0xFF4CAF50)
                selectedStatType == StatType.BAN_RATE && value >= 30f -> Color(0xFFFF5722)
                else -> Color(0xFFEF5350)
            }
            val textColor = when {
                selectedStatType == StatType.WIN_RATE && value >= 52f -> Color(0xFFFFEB3B)
                selectedStatType == StatType.WIN_RATE && value >= 50f -> Color(0xFF4CAF50)
                else -> Color(0xFFEF5350)
            }
            RankStatRow(
                rankName = rankStat.rankName,
                value = value,
                maxValue = maxVal,
                barColor = barColor,
                textColor = textColor,
                isPercent = true
            )
            Spacer(Modifier.height(10.dp))
        }
    }
}

@Composable
private fun RankStatRow(
    rankName: String,
    value: Float,
    maxValue: Float,
    barColor: Color,
    textColor: Color,
    isPercent: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = rankName,
            color = Color.Gray,
            fontSize = 12.sp,
            modifier = Modifier.width(110.dp)
        )
        Spacer(Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .weight(1f)
                .height(20.dp)
                .background(Color(0xFF1A1A2E), RoundedCornerShape(10.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth((value / maxValue).coerceIn(0f, 1f))
                    .background(barColor, RoundedCornerShape(10.dp))
            )
        }
        Spacer(Modifier.width(8.dp))
        Text(
            text = if (isPercent) "${"%.1f".format(value)}%" else "%.2f".format(value),
            color = textColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(44.dp),
            textAlign = TextAlign.End
        )
    }
}

@Composable
private fun MatchupsTab(hero: HeroMetaEntry) {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        MatchupSection(
            title = "Counters",
            subtitle = "Heroes ${hero.name} is strong against",
            heroes = hero.strongAgainst,
            accentColor = Color(0xFF4CAF50)
        )
        MatchupSection(
            title = "Weak Against",
            subtitle = "Heroes that counter ${hero.name}",
            heroes = hero.weakAgainst,
            accentColor = Color(0xFFF44336)
        )
        MatchupSection(
            title = "Synergies",
            subtitle = "Best team combo with ${hero.name}",
            heroes = hero.synergies,
            accentColor = Color(0xFF2196F3)
        )
    }
}

@Composable
private fun MatchupSection(
    title: String,
    subtitle: String,
    heroes: List<HeroMatchupEntry>,
    accentColor: Color
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF141428), RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(accentColor, CircleShape)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = title,
                color = accentColor,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(text = subtitle, color = Color(0xFF888888), fontSize = 10.sp)
        Spacer(Modifier.height(10.dp))

        if (heroes.isEmpty()) {
            Text(text = "No data available", color = Color.Gray, fontSize = 12.sp)
        } else {
            heroes.forEach { matchup ->
                MatchupHeroRow(matchup = matchup)
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun MatchupHeroRow(matchup: HeroMatchupEntry) {
    val maxScore = 7f
    val matchupTierColor = matchup.tier.color()
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(Color(0xFF222244), CircleShape)
                .border(1.5.dp, matchupTierColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = matchup.name.take(2).uppercase(),
                color = Color.White,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = matchup.name,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
                Text(
                    text = "Score: ${"%.2f".format(matchup.score)}",
                    color = Color.Gray,
                    fontSize = 11.sp
                )
            }
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp)
                        .background(Color(0xFF222233), RoundedCornerShape(3.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth((matchup.score / maxScore).coerceIn(0f, 1f))
                            .background(matchupTierColor, RoundedCornerShape(3.dp))
                    )
                }
                Spacer(Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .background(matchupTierColor, RoundedCornerShape(4.dp))
                        .padding(horizontal = 5.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = matchup.tier.label,
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
