package com.example.metaforge.presentation.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.example.metaforge.core.util.formatLastFetched
import com.example.metaforge.data.local.datastore.DraftPreferences
import com.example.metaforge.domain.model.HeroMetaEntry
import com.example.metaforge.domain.model.HeroTier
import com.example.metaforge.presentation.components.LandOfDawnBanner
import com.example.metaforge.presentation.components.MetaforgeLogo
import com.example.metaforge.presentation.components.ToolCard
import com.example.metaforge.presentation.screens.hero_encyclopedia.TierListUiState
import com.example.metaforge.presentation.screens.hero_encyclopedia.TierListViewModel
import com.example.metaforge.presentation.screens.hero_encyclopedia.color
import com.example.metaforge.ui.theme.MFColors
import org.koin.compose.koinInject
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

    // No public API exposes the MLBB season number, so we surface the next
    // best signal: when the data was last refreshed online.
    val prefs: DraftPreferences = koinInject()
    val lastFetchedAt by prefs.getLastFetchedAt()
        .collectAsStateWithLifecycle(initialValue = null)
    val lastFetchLabel = formatLastFetched(lastFetchedAt)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MFColors.Bg)
            .verticalScroll(rememberScrollState())
    ) {
        // ── 1) HEADER: MetaForge logo + wordmark + Settings ──────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 12.dp, top = 18.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // M-Crystal logo — same geometry as the launcher icon
                MetaforgeLogo(size = 36.dp)
                Spacer(Modifier.width(10.dp))
                Text(
                    "METAFORGE",
                    color = MFColors.TextPrimary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 3.sp
                )
            }
            IconButton(
                onClick = onNavigateToSettings,
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(MFColors.BgCard)
            ) {
                Icon(
                    Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = MFColors.Accent,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        // ── 2) BANNER (atmosfer, tanpa teks judul) ────────────────────────────
        LandOfDawnBanner(
            modifier = Modifier.padding(horizontal = 16.dp),
            statusLabel = "LAST FETCH: $lastFetchLabel"
        )

        Spacer(Modifier.height(22.dp))

        // ── 3) DUA TOMBOL TOOL (tanpa emoji) ──────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ToolCard(
                title = "Draft Simulator",
                subtitle = "Simulasi ban & pick",
                icon = Icons.Default.Tune,
                onClick = onNavigateToDraftSetup,
                modifier = Modifier.weight(1f)
            )
            ToolCard(
                title = "Hero Tier List",
                subtitle = "Peringkat meta",
                icon = Icons.Default.Leaderboard,
                onClick = onNavigateToHeroList,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(28.dp))

        // ── 4) SECTION STATS ──────────────────────────────────────────────────
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            SectionLabel("SEASON STATISTICS")
            Spacer(Modifier.height(12.dp))
            MetaSnapshotRow(topWinHero, topBanHero, topPickHero)
        }

        Spacer(Modifier.height(40.dp))

        // ── FOOTER ────────────────────────────────────────────────────────────
        Text(
            "MetaForge • Last fetch: $lastFetchLabel • Mythic rank data",
            color = MFColors.TextHint,
            fontSize = 10.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        )
    }
}

// ─── Stat Row ───────────────────────────────────────────────────────────────

@Composable
private fun MetaSnapshotRow(
    topWin: HeroMetaEntry?,
    topBan: HeroMetaEntry?,
    topPick: HeroMetaEntry?
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(MFColors.BgCard)
            .border(1.dp, MFColors.BgElevated, RoundedCornerShape(18.dp))
            .padding(horizontal = 12.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SnapshotStat("TOP WIN", topWin?.name ?: "—",  "54.5%", topWin?.portraitUrl,  topWin?.tier?.color()  ?: MFColors.TextHint, Modifier.weight(1f))
        Box(Modifier.width(1.dp).height(40.dp).align(Alignment.CenterVertically).background(MFColors.BgElevated))
        SnapshotStat("TOP BAN",   topBan?.name ?: "—",  "65.2%", topBan?.portraitUrl,  topBan?.tier?.color()  ?: MFColors.TextHint, Modifier.weight(1f))
        Box(Modifier.width(1.dp).height(40.dp).align(Alignment.CenterVertically).background(MFColors.BgElevated))
        SnapshotStat("TOP PICK",   topPick?.name ?: "—", "22.1%", topPick?.portraitUrl, topPick?.tier?.color() ?: MFColors.TextHint, Modifier.weight(1f))
    }
}

@Composable
private fun SnapshotStat(
    label: String, heroName: String, value: String,
    portrait: String?, tierColor: Color, modifier: Modifier = Modifier
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = MFColors.TextHint, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
        Spacer(Modifier.height(8.dp))
        if (!portrait.isNullOrEmpty()) {
            AsyncImage(
                model = portrait, contentDescription = heroName, contentScale = ContentScale.Crop,
                modifier = Modifier.size(38.dp).clip(CircleShape).background(MFColors.BgElevated).border(1.5.dp, tierColor, CircleShape)
            )
        } else {
            Box(Modifier.size(38.dp).clip(CircleShape).background(MFColors.BgElevated).border(1.5.dp, tierColor, CircleShape), Alignment.Center) {
                Text(heroName.take(1), color = tierColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(heroName, color = MFColors.TextPrimary, fontWeight = FontWeight.Bold, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(value, color = tierColor, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
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

// Catatan: MetaHeroCard tetap ada namun tidak dipanggil di HomeScreen (mematuhi aturan: jangan hapus file/composable).
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
    }
}