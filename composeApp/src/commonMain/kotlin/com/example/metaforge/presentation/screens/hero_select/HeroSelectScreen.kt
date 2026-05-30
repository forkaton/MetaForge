package com.example.metaforge.presentation.screens.hero_select

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items as lazyRowItems
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.example.metaforge.domain.model.HeroLane
import com.example.metaforge.presentation.components.pressScale
import com.example.metaforge.ui.theme.MFColors
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HeroSelectScreen(
    slotIndex: Int,
    isAlly: Boolean,
    isBan: Boolean = false,
    onNavigateBack: () -> Unit,
    viewModel: HeroSelectViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val accentColor = when {
        isBan  -> MFColors.BanRed
        isAlly -> MFColors.AllyBlue
        else   -> MFColors.EnemyRed
    }

    Scaffold(
        containerColor = MFColors.Bg,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            if (isBan) "BAN HERO" else "PICK HERO",
                            color = accentColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            "Slot ${slotIndex + 1}  •  ${if (isAlly) "Ally" else "Enemy"}",
                            color = MFColors.TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = MFColors.Accent)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MFColors.BgCard)
            )
        }
    ) { paddingValues ->
        when (val state = uiState) {
            is HeroSelectUiState.Loading -> Box(
                Modifier.fillMaxSize().padding(paddingValues), Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = MFColors.Accent)
                    Spacer(Modifier.height(12.dp))
                    Text("Loading heroes...", color = MFColors.TextSecondary)
                }
            }

            is HeroSelectUiState.Error -> Box(
                Modifier.fillMaxSize().padding(paddingValues), Alignment.Center
            ) { Text(state.message, color = MFColors.EnemyRed, textAlign = TextAlign.Center) }

            is HeroSelectUiState.Ready -> {
                Column(
                    modifier = Modifier.fillMaxSize().padding(paddingValues)
                ) {
                    // Search bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MFColors.BgCard)
                            .border(1.dp, MFColors.BgElevated, RoundedCornerShape(12.dp))
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Search, null, tint = MFColors.TextHint, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            BasicTextField(
                                value = state.searchQuery,
                                onValueChange = viewModel::onSearchQueryChange,
                                singleLine = true,
                                textStyle = LocalTextStyle.current.copy(color = MFColors.TextPrimary, fontSize = 14.sp),
                                cursorBrush = SolidColor(MFColors.Accent),
                                decorationBox = { inner ->
                                    if (state.searchQuery.isEmpty()) Text("Search hero...", color = MFColors.TextHint, fontSize = 14.sp)
                                    inner()
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    // Lane filter
                    LazyRow(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        item {
                            LaneChip("ALL", state.selectedLane == null, MFColors.Accent) { viewModel.filterByLane(null) }
                        }
                        lazyRowItems(HeroLane.entries) { lane ->
                            LaneChip(laneShortName(lane), state.selectedLane == lane, laneColor(lane)) { viewModel.filterByLane(lane) }
                        }
                    }

                    // Count + note for bans
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("${state.filteredHeroes.size} heroes", color = MFColors.TextHint, fontSize = 11.sp)
                        if (isBan) {
                            Spacer(Modifier.width(8.dp))
                            Text("• Diff. teams may ban the same hero", color = MFColors.Warning, fontSize = 10.sp)
                        }
                    }

                    // Hero grid
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(4),
                        modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        items(state.filteredHeroes, key = { it.id }) { hero ->
                            // Ban rule: same team can't ban the same hero twice.
                            // Pick rule: can't pick a hero already picked or banned by anyone.
                            val isUnavailable = if (isBan) {
                                if (isAlly) state.allyBannedHeroNames.contains(hero.name)
                                else        state.enemyBannedHeroNames.contains(hero.name)
                            } else {
                                state.pickedHeroNames.contains(hero.name) ||
                                        state.bannedHeroNames.contains(hero.name)
                            }

                            Column(
                                modifier = Modifier
                                    .pressScale(enabled = !isUnavailable) {
                                        viewModel.pickHero(slotIndex, isAlly, isBan, hero)
                                        onNavigateBack()
                                    }
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isUnavailable) MFColors.BgCard.copy(alpha = 0.5f) else MFColors.BgCard)
                                    .border(
                                        1.dp,
                                        if (isUnavailable) MFColors.TextHint.copy(alpha = 0.2f) else accentColor.copy(alpha = 0.15f),
                                        RoundedCornerShape(10.dp)
                                    ),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .aspectRatio(1f)
                                        .clip(RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp))
                                        .background(MFColors.BgElevated)
                                ) {
                                    AsyncImage(
                                        model = hero.imageUrl,
                                        contentDescription = hero.name,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .then(if (isUnavailable) Modifier.background(Color.Black.copy(alpha = 0.55f)) else Modifier)
                                    )

                                    if (isUnavailable) {
                                        Box(
                                            Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            val label = if (state.bannedHeroNames.contains(hero.name)) "BANNED" else "TAKEN"
                                            val labelColor = if (label == "BANNED") MFColors.BanRed else MFColors.EnemyRed
                                            Text(label, color = labelColor, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    // Lane badge
                                    if (!isUnavailable) {
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.BottomEnd)
                                                .padding(3.dp)
                                                .background(laneColor(hero.lane).copy(alpha = 0.85f), RoundedCornerShape(3.dp))
                                                .padding(horizontal = 3.dp, vertical = 1.dp)
                                        ) {
                                            Text(laneShortName(hero.lane), color = Color.White, fontSize = 7.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                                Text(
                                    hero.name,
                                    color = if (isUnavailable) MFColors.TextHint else MFColors.TextPrimary,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LaneChip(label: String, selected: Boolean, color: Color, onClick: () -> Unit) {
    val bg by animateColorAsState(
        if (selected) color else color.copy(alpha = 0.08f), tween(200), label = "laneBg"
    )
    val border by animateColorAsState(
        color.copy(alpha = if (selected) 1f else 0.3f), tween(200), label = "laneBorder"
    )
    val txt by animateColorAsState(
        if (selected) Color.White else color, tween(200), label = "laneTxt"
    )
    Box(
        modifier = Modifier
            .pressScale(onClick = onClick)
            .clip(RoundedCornerShape(20.dp))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(20.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            label,
            color = txt,
            fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

private fun laneShortName(lane: HeroLane) = when (lane) {
    HeroLane.EXP_LANE  -> "EXP"
    HeroLane.GOLD_LANE -> "GOLD"
    HeroLane.MID_LANE  -> "MID"
    HeroLane.JUNGLE    -> "JNG"
    HeroLane.ROAM      -> "ROAM"
}

private fun laneColor(lane: HeroLane) = when (lane) {
    HeroLane.EXP_LANE  -> Color(0xFFFF6B35)
    HeroLane.GOLD_LANE -> Color(0xFFFFD700)
    HeroLane.MID_LANE  -> Color(0xFF9B59B6)
    HeroLane.JUNGLE    -> Color(0xFF27AE60)
    HeroLane.ROAM      -> Color(0xFF2980B9)
}