package com.example.metaforge.presentation.screens.draft_arena

import android.content.res.Configuration
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.example.metaforge.domain.model.Hero
import com.example.metaforge.domain.model.HeroTier
import com.example.metaforge.ui.theme.MFColors
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DraftScreen(
    rank: String,
    party: Int,
    pick: Int,
    isFirst: Boolean,
    lane: String,
    onNavigateBack: () -> Unit,
    onNavigateToHeroSelect: (slot: Int, isAlly: Boolean, isBan: Boolean) -> Unit,
    viewModel: DraftViewModel = koinViewModel()
) {
    viewModel.setupDraft(pick, isFirst, lane)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

    Scaffold(
        containerColor = MFColors.Bg,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "DRAFT SIMULATOR  •  $rank",
                            color = MFColors.TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            "Pick order #$pick  •  ${if (isFirst) "Blue (1st)" else "Red (2nd)"}  •  $lane",
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
    ) { padding ->
        when (val state = uiState) {
            is DraftUiState.Loading -> Box(
                Modifier.fillMaxSize().padding(padding), Alignment.Center
            ) { CircularProgressIndicator(color = MFColors.Accent) }

            is DraftUiState.Error -> Box(
                Modifier.fillMaxSize().padding(padding), Alignment.Center
            ) { Text(state.message, color = MFColors.EnemyRed, textAlign = TextAlign.Center, modifier = Modifier.padding(16.dp)) }

            is DraftUiState.Ready -> {
                if (isLandscape) {
                    DraftLandscapeContent(state, isFirst, pick, onNavigateToHeroSelect, Modifier.padding(padding))
                } else {
                    DraftPortraitContent(state, isFirst, pick, onNavigateToHeroSelect, Modifier.padding(padding))
                }
            }
        }
    }
}

// ─── PORTRAIT ────────────────────────────────────────────────────────────────

@Composable
private fun DraftPortraitContent(
    state: DraftUiState.Ready,
    isFirst: Boolean,
    pick: Int,
    onNavigateToHeroSelect: (Int, Boolean, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MFColors.Bg)
            .verticalScroll(rememberScrollState())
    ) {
        // Phase banner
        PhaseBanner(state.turnMessage, state.isUserTurn)

        // Ban section
        BanSection(
            allyBans = state.draftState.allyBans,
            enemyBans = state.draftState.enemyBans,
            onAllyBanClick = { idx -> onNavigateToHeroSelect(idx, true, true) },
            onEnemyBanClick = { idx -> onNavigateToHeroSelect(idx, false, true) }
        )

        Spacer(Modifier.height(4.dp))
        HorizontalDivider(color = MFColors.BgElevated, thickness = 1.dp)
        Spacer(Modifier.height(4.dp))

        // Pick section
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Ally picks
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                TeamLabel("BLUE TEAM", MFColors.AllyBlue, isLeft = true)
                state.draftState.allySlots.forEachIndexed { idx, hero ->
                    val unlocked = state.draftState.isPickUnlocked(idx, true, isFirst)
                    val isUserSlot = idx == pick - 1
                    PickSlot(
                        hero = hero,
                        isUnlocked = unlocked,
                        isUserSlot = isUserSlot,
                        isAlly = true,
                        onClick = { if (unlocked) onNavigateToHeroSelect(idx, true, false) }
                    )
                }
            }
            // Enemy picks
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                TeamLabel("RED TEAM", MFColors.EnemyRed, isLeft = false)
                state.draftState.enemySlots.forEachIndexed { idx, hero ->
                    val unlocked = state.draftState.isPickUnlocked(idx, false, isFirst)
                    PickSlot(
                        hero = hero,
                        isUnlocked = unlocked,
                        isUserSlot = false,
                        isAlly = false,
                        onClick = { if (unlocked) onNavigateToHeroSelect(idx, false, false) }
                    )
                }
            }
        }

        // Suggestions
        if (state.suggestions.isNotEmpty()) {
            SuggestionsPanel(state.suggestions)
        }

        Spacer(Modifier.height(16.dp))
    }
}

// ─── LANDSCAPE ───────────────────────────────────────────────────────────────

@Composable
private fun DraftLandscapeContent(
    state: DraftUiState.Ready,
    isFirst: Boolean,
    pick: Int,
    onNavigateToHeroSelect: (Int, Boolean, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxSize()
            .background(MFColors.Bg)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Ally column
        Column(
            modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            TeamLabel("BLUE TEAM", MFColors.AllyBlue, isLeft = true)
            BanRow(
                bans = state.draftState.allyBans,
                color = MFColors.AllyBlue,
                onBanClick = { idx -> onNavigateToHeroSelect(idx, true, true) }
            )
            state.draftState.allySlots.forEachIndexed { idx, hero ->
                val unlocked = state.draftState.isPickUnlocked(idx, true, isFirst)
                PickSlot(
                    hero = hero,
                    isUnlocked = unlocked,
                    isUserSlot = idx == pick - 1,
                    isAlly = true,
                    onClick = { if (unlocked) onNavigateToHeroSelect(idx, true, false) },
                    compact = true
                )
            }
        }
        // Center: phase + suggestions
        Column(
            modifier = Modifier.weight(1.4f).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            PhaseBanner(state.turnMessage, state.isUserTurn)
            if (state.suggestions.isNotEmpty()) SuggestionsPanel(state.suggestions)
        }
        // Enemy column
        Column(
            modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            TeamLabel("RED TEAM", MFColors.EnemyRed, isLeft = false)
            BanRow(
                bans = state.draftState.enemyBans,
                color = MFColors.EnemyRed,
                onBanClick = { idx -> onNavigateToHeroSelect(idx, false, true) }
            )
            state.draftState.enemySlots.forEachIndexed { idx, hero ->
                val unlocked = state.draftState.isPickUnlocked(idx, false, isFirst)
                PickSlot(
                    hero = hero,
                    isUnlocked = unlocked,
                    isUserSlot = false,
                    isAlly = false,
                    onClick = { if (unlocked) onNavigateToHeroSelect(idx, false, false) },
                    compact = true
                )
            }
        }
    }
}

// ─── COMPONENTS ──────────────────────────────────────────────────────────────

@Composable
private fun PhaseBanner(message: String, isUserTurn: Boolean) {
    val isBanPhase = message.contains("BAN", ignoreCase = true)
    val isDone = message.contains("COMPLETE", ignoreCase = true)
    val bgColor = when {
        isDone     -> MFColors.Success.copy(alpha = 0.2f)
        isUserTurn -> MFColors.Accent.copy(alpha = 0.15f)
        isBanPhase -> MFColors.BanRed.copy(alpha = 0.15f)
        else       -> MFColors.BgElevated
    }
    val textColor = when {
        isDone     -> MFColors.Success
        isUserTurn -> MFColors.Accent
        isBanPhase -> MFColors.BanRed
        else       -> MFColors.TextSecondary
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(bgColor)
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Text(
            text = if (isUserTurn) "YOUR TURN — $message" else message,
            color = textColor,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun BanSection(
    allyBans: List<Hero?>,
    enemyBans: List<Hero?>,
    onAllyBanClick: (Int) -> Unit,
    onEnemyBanClick: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MFColors.BgCard)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        // Ally bans row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                "BLUE",
                color = MFColors.AllyBlue,
                fontWeight = FontWeight.Black,
                fontSize = 9.sp,
                modifier = Modifier.width(28.dp)
            )
            repeat(5) { idx ->
                BanSlot(
                    hero = allyBans.getOrNull(idx),
                    color = MFColors.AllyBlue,
                    modifier = Modifier.weight(1f).aspectRatio(1f),
                    onClick = { onAllyBanClick(idx) }
                )
            }
        }
        // Enemy bans row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                "RED",
                color = MFColors.EnemyRed,
                fontWeight = FontWeight.Black,
                fontSize = 9.sp,
                modifier = Modifier.width(28.dp)
            )
            repeat(5) { idx ->
                BanSlot(
                    hero = enemyBans.getOrNull(idx),
                    color = MFColors.EnemyRed,
                    modifier = Modifier.weight(1f).aspectRatio(1f),
                    onClick = { onEnemyBanClick(idx) }
                )
            }
        }
    }
}

@Composable
private fun BanRow(
    bans: List<Hero?>,
    color: Color,
    modifier: Modifier = Modifier,
    onBanClick: (Int) -> Unit
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        repeat(5) { idx ->
            BanSlot(
                hero = bans.getOrNull(idx),
                color = color,
                modifier = Modifier.weight(1f).aspectRatio(1f),
                onClick = { onBanClick(idx) }
            )
        }
    }
}

@Composable
private fun BanSlot(hero: Hero?, color: Color, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (hero != null) color.copy(alpha = 0.15f) else MFColors.BgElevated)
            .border(1.dp, if (hero != null) color.copy(alpha = 0.6f) else MFColors.TextHint.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (hero != null) {
            AsyncImage(
                model = hero.imageUrl,
                contentDescription = hero.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(6.dp))
            )
            // Red X overlay for ban
            Box(
                modifier = Modifier.fillMaxSize().background(MFColors.BanRed.copy(alpha = 0.35f), RoundedCornerShape(6.dp))
            )
            Text("✕", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Black)
        } else {
            Text("+", color = MFColors.TextHint, fontSize = 16.sp)
        }
    }
}

@Composable
private fun TeamLabel(label: String, color: Color, isLeft: Boolean) {
    Text(
        text = label,
        color = color,
        fontWeight = FontWeight.Black,
        fontSize = 11.sp,
        letterSpacing = 1.sp,
        textAlign = if (isLeft) TextAlign.Start else TextAlign.End,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 2.dp)
    )
}

@Composable
private fun PickSlot(
    hero: Hero?,
    isUnlocked: Boolean,
    isUserSlot: Boolean,
    isAlly: Boolean,
    onClick: () -> Unit,
    compact: Boolean = false
) {
    val slotColor = when {
        isAlly  -> MFColors.AllyBlue
        else    -> MFColors.EnemyRed
    }
    val height = if (compact) 52.dp else 68.dp
    val borderColor = when {
        hero != null && isUserSlot -> MFColors.Accent
        hero != null               -> slotColor.copy(alpha = 0.5f)
        isUserSlot && isUnlocked   -> MFColors.Accent
        isUnlocked                 -> slotColor.copy(alpha = 0.6f)
        else                       -> MFColors.TextHint.copy(alpha = 0.2f)
    }
    val bgColor = when {
        hero != null    -> slotColor.copy(alpha = 0.08f)
        isUnlocked      -> slotColor.copy(alpha = 0.05f)
        else            -> MFColors.BgCard
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .border(
                if (isUserSlot && isUnlocked) 2.dp else 1.dp,
                borderColor,
                RoundedCornerShape(8.dp)
            )
            .clickable(enabled = isUnlocked || hero == null) { onClick() },
        contentAlignment = Alignment.CenterStart
    ) {
        if (hero != null) {
            Row(
                modifier = Modifier.fillMaxSize().padding(horizontal = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = hero.imageUrl,
                    contentDescription = hero.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(if (compact) 40.dp else 52.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(MFColors.BgElevated)
                )
                Spacer(Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        hero.name,
                        color = MFColors.TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = if (compact) 11.sp else 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        hero.role.ifEmpty { hero.lane.displayName },
                        color = MFColors.TextSecondary,
                        fontSize = 9.sp
                    )
                }
            }
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                if (isUserSlot && isUnlocked) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("YOUR PICK", color = MFColors.Accent, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                    }
                } else if (isUnlocked) {
                    Text("+", color = slotColor.copy(alpha = 0.6f), fontSize = 20.sp)
                } else {
                    Text("—", color = MFColors.TextHint, fontSize = 14.sp)
                }
            }
        }

        // "YOUR PICK" indicator strip
        if (isUserSlot && hero == null && isUnlocked) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .width(3.dp)
                    .fillMaxHeight()
                    .background(MFColors.Accent, RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp))
            )
        }
    }
}

@Composable
private fun SuggestionsPanel(suggestions: List<HeroSuggestion>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MFColors.BgCard)
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(8.dp).background(MFColors.Accent, CircleShape))
            Spacer(Modifier.width(6.dp))
            Text(
                "HERO SUGGESTIONS",
                color = MFColors.Accent,
                fontWeight = FontWeight.Black,
                fontSize = 12.sp,
                letterSpacing = 1.sp
            )
            Spacer(Modifier.width(8.dp))
            Text(
                "Based on tier, counters & synergy",
                color = MFColors.TextHint,
                fontSize = 10.sp
            )
        }
        Spacer(Modifier.height(10.dp))
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            suggestions.forEachIndexed { idx, sug ->
                SuggestionCard(rank = idx + 1, suggestion = sug)
            }
        }
    }
}

@Composable
private fun SuggestionCard(rank: Int, suggestion: HeroSuggestion) {
    val tierColor = Color(suggestion.hero.tier.colorValue)
    val hasWarning = suggestion.warnings.isNotEmpty()
    val borderColor = if (hasWarning) MFColors.Warning.copy(alpha = 0.7f) else tierColor.copy(alpha = 0.5f)

    Column(
        modifier = Modifier
            .width(130.dp)
            .background(MFColors.BgElevated, RoundedCornerShape(10.dp))
            .border(1.dp, borderColor, RoundedCornerShape(10.dp))
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Rank badge + portrait
        Box {
            AsyncImage(
                model = suggestion.hero.portraitUrl,
                contentDescription = suggestion.hero.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MFColors.BgCard)
            )
            // Tier badge
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .background(tierColor, RoundedCornerShape(topStart = 8.dp, bottomEnd = 5.dp))
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            ) {
                Text(suggestion.hero.tier.label, color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Black)
            }
            // Rank number
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(20.dp)
                    .background(MFColors.BgCard.copy(alpha = 0.9f), CircleShape)
                    .border(1.dp, MFColors.Accent.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("#$rank", color = MFColors.Accent, fontSize = 8.sp, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(
            suggestion.hero.name,
            color = MFColors.TextPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
        // Score
        Text(
            "Score: ${suggestion.totalScore}",
            color = MFColors.Accent,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold
        )
        // Reason
        if (suggestion.reasons.isNotEmpty()) {
            Spacer(Modifier.height(4.dp))
            Text(
                suggestion.reasons.first(),
                color = MFColors.TextSecondary,
                fontSize = 9.sp,
                maxLines = 2,
                textAlign = TextAlign.Center,
                lineHeight = 12.sp
            )
        }
        // Warning
        if (hasWarning) {
            Spacer(Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MFColors.Warning.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            ) {
                Text(
                    "⚠ ${suggestion.warnings.first()}",
                    color = MFColors.Warning,
                    fontSize = 8.sp,
                    maxLines = 2,
                    lineHeight = 11.sp
                )
            }
        }
    }
}
