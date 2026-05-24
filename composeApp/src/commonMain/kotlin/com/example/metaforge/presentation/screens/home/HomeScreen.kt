package com.example.metaforge.presentation.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.example.metaforge.domain.model.HeroMetaEntry
import com.example.metaforge.domain.model.HeroTier
import com.example.metaforge.presentation.screens.hero_encyclopedia.TierListUiState
import com.example.metaforge.presentation.screens.hero_encyclopedia.TierListViewModel
import com.example.metaforge.presentation.screens.hero_encyclopedia.color
import com.example.metaforge.ui.theme.MFColors
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun HomeScreen(
    onNavigateToDraftSetup: () -> Unit,
    onNavigateToHeroList: () -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: TierListViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val heroes      = (uiState as? TierListUiState.Ready)?.heroes ?: emptyList()
    val topWinHero  = heroes.firstOrNull { it.tier == HeroTier.SS }
    val topBanHero  = heroes.filter { it.tier == HeroTier.SS }.getOrNull(1)
    val topPickHero = heroes.firstOrNull { it.tier == HeroTier.S }
    val metaHeroes  = (heroes.filter { it.tier == HeroTier.SS } +
                       heroes.filter { it.tier == HeroTier.S }).take(12)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MFColors.Bg)
            .verticalScroll(rememberScrollState())
    ) {
        // ── HEADER ────────────────────────────────────────────────────────────
        HomeHeader(onNavigateToSettings = onNavigateToSettings)

        // ── META SNAPSHOT ─────────────────────────────────────────────────────
        MetaSnapshotRow(topWinHero, topBanHero, topPickHero)

        Spacer(Modifier.height(4.dp))

        // ── TOOLS ─────────────────────────────────────────────────────────────
        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SectionLabel("TOOLS")
            ToolCard(
                title       = "DRAFT SIMULATOR",
                subtitle    = "AI-powered 5v5 draft — counters, synergies & hero suggestions",
                icon        = Icons.Default.AutoAwesome,
                accentColor = MFColors.Accent,
                tagLabel    = "AI PICKS",
                onClick     = onNavigateToDraftSetup
            )
            ToolCard(
                title       = "HERO TIER LIST",
                subtitle    = "SS/S/A/B/C/D meta rankings • Win, Pick & Ban rates by rank",
                icon        = Icons.Default.Leaderboard,
                accentColor = MFColors.PrimaryLt,
                tagLabel    = "UPDATED",
                onClick     = onNavigateToHeroList
            )
        }

        Spacer(Modifier.height(16.dp))

        // ── META HIGHLIGHTS ───────────────────────────────────────────────────
        if (metaHeroes.isNotEmpty()) {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                SectionLabel("META HIGHLIGHTS")
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    metaHeroes.forEach { hero ->
                        MetaHeroCard(hero = hero, onClick = onNavigateToHeroList)
                    }
                }
            }
            Spacer(Modifier.height(20.dp))
        }

        // ── FOOTER ────────────────────────────────────────────────────────────
        Text(
            "MetaForge • May 2026 Season • Mythic rank data",
            color = MFColors.TextHint,
            fontSize = 10.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp)
        )
    }
}

// ─── Header ──────────────────────────────────────────────────────────────────

@Composable
private fun HomeHeader(onNavigateToSettings: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brush.verticalGradient(listOf(MFColors.Primary, MFColors.BgCard)))
            .padding(horizontal = 20.dp, vertical = 20.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Live badge
                Box(
                    modifier = Modifier
                        .background(MFColors.Success.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                        .border(1.dp, MFColors.Success.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(5.dp).background(MFColors.Success, CircleShape))
                        Spacer(Modifier.width(4.dp))
                        Text("LIVE", color = MFColors.Success, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(Modifier.width(8.dp))
                Text("Season 29  •  Mythic Meta", color = MFColors.TextSecondary, fontSize = 11.sp)
                Spacer(Modifier.weight(1f))
                // Settings button
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MFColors.BgCard.copy(alpha = 0.6f))
                        .border(1.dp, MFColors.Accent.copy(alpha = 0.3f), CircleShape)
                        .clickable { onNavigateToSettings() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Settings, "Settings", tint = MFColors.Accent, modifier = Modifier.size(18.dp))
                }
            }
            Spacer(Modifier.height(6.dp))
            Text("METAFORGE", color = MFColors.TextPrimary, fontWeight = FontWeight.Black, fontSize = 30.sp, letterSpacing = 4.sp)
            Text("Mobile Legends Meta Analyzer", color = MFColors.Accent, fontSize = 12.sp, letterSpacing = 1.sp)
        }
    }
}

// ─── Meta Snapshot ───────────────────────────────────────────────────────────

@Composable
private fun MetaSnapshotRow(
    topWin: HeroMetaEntry?,
    topBan: HeroMetaEntry?,
    topPick: HeroMetaEntry?
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MFColors.BgCard)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SnapshotStat("TOP WIN RATE", topWin?.name ?: "—",  "54.5%", topWin?.portraitUrl,  topWin?.tier?.color()  ?: MFColors.TextHint, Modifier.weight(1f))
        Box(Modifier.width(1.dp).height(52.dp).background(MFColors.BgElevated))
        SnapshotStat("TOP BANNED",   topBan?.name ?: "—",  "65.2%", topBan?.portraitUrl,  topBan?.tier?.color()  ?: MFColors.TextHint, Modifier.weight(1f))
        Box(Modifier.width(1.dp).height(52.dp).background(MFColors.BgElevated))
        SnapshotStat("TOP PICKED",   topPick?.name ?: "—", "22.1%", topPick?.portraitUrl, topPick?.tier?.color() ?: MFColors.TextHint, Modifier.weight(1f))
    }
}

@Composable
private fun SnapshotStat(
    label: String, heroName: String, value: String,
    portrait: String?, tierColor: Color, modifier: Modifier = Modifier
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = MFColors.TextHint, fontSize = 8.sp, letterSpacing = 0.3.sp, textAlign = TextAlign.Center)
        Spacer(Modifier.height(4.dp))
        if (!portrait.isNullOrEmpty()) {
            AsyncImage(
                model = portrait, contentDescription = heroName, contentScale = ContentScale.Crop,
                modifier = Modifier.size(32.dp).clip(CircleShape).background(MFColors.BgElevated).border(1.5.dp, tierColor, CircleShape)
            )
        } else {
            Box(Modifier.size(32.dp).clip(CircleShape).background(MFColors.BgElevated).border(1.5.dp, tierColor, CircleShape), Alignment.Center) {
                Text(heroName.take(2), color = tierColor, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(Modifier.height(3.dp))
        Text(heroName, color = MFColors.TextPrimary, fontWeight = FontWeight.Bold, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center)
        Text(value, color = tierColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

// ─── Tool Card ───────────────────────────────────────────────────────────────

@Composable
private fun ToolCard(
    title: String, subtitle: String, icon: ImageVector,
    accentColor: Color, tagLabel: String, onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Brush.horizontalGradient(listOf(MFColors.BgCard, MFColors.BgElevated)))
            .border(1.dp, accentColor.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(48.dp).clip(RoundedCornerShape(12.dp))
                    .background(accentColor.copy(alpha = 0.12f))
                    .border(1.dp, accentColor.copy(alpha = 0.35f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) { Icon(icon, null, tint = accentColor, modifier = Modifier.size(24.dp)) }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(title, color = MFColors.TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(Modifier.width(6.dp))
                    Box(
                        modifier = Modifier.background(accentColor.copy(alpha = 0.18f), RoundedCornerShape(3.dp))
                            .padding(horizontal = 5.dp, vertical = 1.dp)
                    ) { Text(tagLabel, color = accentColor, fontSize = 8.sp, fontWeight = FontWeight.Bold) }
                }
                Spacer(Modifier.height(3.dp))
                Text(subtitle, color = MFColors.TextSecondary, fontSize = 11.sp, lineHeight = 15.sp)
            }
            Spacer(Modifier.width(8.dp))
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = MFColors.TextHint, modifier = Modifier.size(20.dp))
        }
    }
}

// ─── Meta Hero Card ──────────────────────────────────────────────────────────

@Composable
private fun MetaHeroCard(hero: HeroMetaEntry, onClick: () -> Unit) {
    val tierColor = hero.tier.color()
    Column(
        modifier = Modifier
            .width(72.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(MFColors.BgCard)
            .border(1.dp, tierColor.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box {
            AsyncImage(
                model = hero.portraitUrl, contentDescription = hero.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(52.dp).clip(CircleShape).background(MFColors.BgElevated)
                    .border(1.5.dp, tierColor.copy(alpha = 0.7f), CircleShape)
            )
            Box(
                modifier = Modifier.align(Alignment.TopEnd).background(tierColor, CircleShape).size(16.dp),
                contentAlignment = Alignment.Center
            ) { Text(hero.tier.label, color = Color.White, fontSize = 6.sp, fontWeight = FontWeight.Black) }
        }
        Spacer(Modifier.height(4.dp))
        Text(hero.name, color = MFColors.TextPrimary, fontSize = 9.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        Text(hero.heroClass, color = MFColors.TextHint, fontSize = 8.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
    }
}

// ─── Helpers ─────────────────────────────────────────────────────────────────

@Composable
private fun SectionLabel(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(3.dp, 14.dp).background(MFColors.Accent, RoundedCornerShape(2.dp)))
        Spacer(Modifier.width(8.dp))
        Text(text, color = MFColors.TextSecondary, fontWeight = FontWeight.Bold, fontSize = 11.sp, letterSpacing = 1.sp)
    }
}
