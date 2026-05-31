package com.example.metaforge.presentation.screens.draft_arena

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.example.metaforge.domain.model.Hero
import com.example.metaforge.domain.model.HeroLane
import com.example.metaforge.presentation.components.pressScale
import com.example.metaforge.ui.theme.MFColors
import org.koin.compose.viewmodel.koinViewModel

/** Material 3 spacing scale used across the draft screen for consistent rhythm. */
private object DraftDimens {
    val Screen = 12.dp
    val Section = 8.dp
    val Slot = 6.dp
    val Inner = 4.dp
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DraftScreen(
    rank: String,
    party: Int,
    picksArg: String,
    isFirst: Boolean,
    lanesArg: String,
    banCount: Int,
    onNavigateBack: () -> Unit,
    onNavigateToHeroSelect: (slot: Int, isAlly: Boolean, isBan: Boolean) -> Unit,
    viewModel: DraftViewModel = koinViewModel()
) {
    // Decode set args once. "none" sentinel = empty (Squad).
    val initialPicks = remember(picksArg) {
        if (picksArg == "none" || picksArg.isEmpty()) emptySet()
        else picksArg.split("-").mapNotNull { it.toIntOrNull() }.toSet()
    }
    val initialLanes = remember(lanesArg) {
        if (lanesArg == "none" || lanesArg.isEmpty()) emptySet()
        else lanesArg.split("-").mapNotNull { runCatching { HeroLane.valueOf(it) }.getOrNull() }.toSet()
    }

    LaunchedEffect(picksArg, isFirst, lanesArg, banCount, party) {
        viewModel.setupDraft(party, initialPicks, isFirst, initialLanes, banCount)
    }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showResetDialog by remember { mutableStateOf(false) }
    var showLaneDialog by remember { mutableStateOf(false) }
    var showPickPosDialog by remember { mutableStateOf(false) }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            containerColor = MFColors.BgCard,
            title = { Text("Reset Draft?", color = MFColors.TextPrimary, fontWeight = FontWeight.Bold) },
            text = { Text("All picks and bans will be cleared.", color = MFColors.TextSecondary, fontSize = 13.sp) },
            confirmButton = {
                TextButton(onClick = { viewModel.resetDraft(); showResetDialog = false }) {
                    Text("Reset", color = MFColors.BanRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) { Text("Cancel", color = MFColors.TextHint) }
            }
        )
    }

    val readyState = uiState as? DraftUiState.Ready

    if (showLaneDialog && readyState != null && !readyState.isSquad) {
        MultiSelectDialog(
            title = "Preferred Lane(s) — pick ${readyState.partySize}",
            options = HeroLane.entries.map { it to it.displayName },
            selected = readyState.currentLanes,
            limit = readyState.partySize,
            onDismiss = { showLaneDialog = false },
            onConfirm = { viewModel.updateLanes(it); showLaneDialog = false }
        )
    }

    if (showPickPosDialog && readyState != null && !readyState.isSquad) {
        MultiSelectDialog(
            title = "Pick Order — pick ${readyState.partySize}",
            options = (1..5).map { it to "Slot $it" },
            selected = readyState.currentPickPositions,
            limit = readyState.partySize,
            onDismiss = { showPickPosDialog = false },
            onConfirm = { viewModel.updatePickPositions(it); showPickPosDialog = false }
        )
    }

    Scaffold(
        containerColor = MFColors.Bg,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("DRAFT SIMULATOR  •  $rank", color = MFColors.TextPrimary,
                            fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text(
                            "${if (isFirst) "Blue (1st)" else "Red (2nd)"}  •  ${partyLabel(party)}  •  $banCount bans",
                            color = MFColors.TextSecondary, fontSize = 11.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = MFColors.Accent)
                    }
                },
                actions = {
                    IconButton(onClick = { showResetDialog = true }) {
                        Icon(Icons.Default.RestartAlt, "Reset", tint = MFColors.Warning)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MFColors.BgCard)
            )
        }
    ) { padding ->
        val onRemove: (Int, Boolean, Boolean) -> Unit =
            remember(viewModel) { { idx, isAlly, isBan -> viewModel.removeHero(idx, isAlly, isBan) } }
        val openLaneDialog = remember { { showLaneDialog = true } }
        val openPickPosDialog = remember { { showPickPosDialog = true } }

        val phaseKey = when (uiState) {
            is DraftUiState.Loading -> 0
            is DraftUiState.Error -> 1
            is DraftUiState.Ready -> 2
        }
        Crossfade(targetState = phaseKey, label = "draftPhase", modifier = Modifier.padding(padding)) { key ->
            when (key) {
                0 -> LoadingView(Modifier.fillMaxSize())
                1 -> ErrorView(
                    message = (uiState as? DraftUiState.Error)?.message ?: "Something went wrong",
                    onRetry = viewModel::retry,
                    modifier = Modifier.fillMaxSize()
                )
                else -> {
                    val state = uiState as? DraftUiState.Ready ?: return@Crossfade
                    BoxWithConstraints(Modifier.fillMaxSize()) {
                        val isLandscape = maxWidth > maxHeight
                        if (isLandscape) {
                            DraftLandscapeContent(state, onNavigateToHeroSelect, onRemove,
                                openLaneDialog, openPickPosDialog)
                        } else {
                            DraftPortraitContent(state, onNavigateToHeroSelect, onRemove,
                                openLaneDialog, openPickPosDialog)
                        }
                    }
                }
            }
        }
    }
}

private fun partyLabel(size: Int): String = when (size) {
    1 -> "Solo"; 2 -> "Duo"; 3 -> "Trio"; 5 -> "Squad"; else -> "${size}-Stack"
}

// ─── DIALOGS ─────────────────────────────────────────────────────────────────

@Composable
private fun <T> MultiSelectDialog(
    title: String,
    options: List<Pair<T, String>>,
    selected: Set<T>,
    limit: Int,
    onDismiss: () -> Unit,
    onConfirm: (Set<T>) -> Unit
) {
    var working by remember { mutableStateOf(selected) }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MFColors.BgCard,
        title = {
            Column {
                Text(title, color = MFColors.TextPrimary, fontWeight = FontWeight.Bold)
                Text(
                    "${working.size}/$limit selected",
                    color = if (working.size == limit) MFColors.Accent else MFColors.TextHint,
                    fontSize = 11.sp
                )
            }
        },
        text = {
            Column {
                options.forEach { (value, label) ->
                    val isChecked = value in working
                    val canSelectMore = working.size < limit
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = isChecked || canSelectMore) {
                                working = if (isChecked) working - value else working + value
                            }
                            .padding(vertical = 8.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isChecked,
                            onCheckedChange = {
                                if (isChecked) working = working - value
                                else if (canSelectMore) working = working + value
                            },
                            colors = CheckboxDefaults.colors(
                                checkedColor = MFColors.Accent,
                                uncheckedColor = MFColors.TextHint
                            )
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            label,
                            color = when {
                                isChecked -> MFColors.Accent
                                canSelectMore -> MFColors.TextPrimary
                                else -> MFColors.TextHint
                            },
                            fontWeight = if (isChecked) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(working) },
                enabled = working.size == limit
            ) { Text("Apply", color = if (working.size == limit) MFColors.Accent else MFColors.TextHint) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = MFColors.TextHint) }
        }
    )
}

// ─── STATE VIEWS ─────────────────────────────────────────────────────────────

@Composable
private fun LoadingView(modifier: Modifier = Modifier) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center) {
        CircularProgressIndicator(color = MFColors.Accent)
        Spacer(Modifier.height(DraftDimens.Section))
        Text("Loading hero meta…", color = MFColors.TextSecondary, fontSize = 13.sp)
    }
}

@Composable
private fun ErrorView(message: String, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier.padding(DraftDimens.Screen),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.ErrorOutline, contentDescription = null,
            tint = MFColors.EnemyRed, modifier = Modifier.size(48.dp))
        Spacer(Modifier.height(DraftDimens.Section))
        Text("Failed to load draft", color = MFColors.TextPrimary,
            fontWeight = FontWeight.Bold, fontSize = 15.sp, textAlign = TextAlign.Center)
        Spacer(Modifier.height(DraftDimens.Inner))
        Text(message, color = MFColors.TextSecondary, fontSize = 12.sp, textAlign = TextAlign.Center)
        Spacer(Modifier.height(DraftDimens.Section))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(containerColor = MFColors.Accent)
        ) { Text("Retry", fontWeight = FontWeight.Bold) }
    }
}

// ─── CONFIG ROW ──────────────────────────────────────────────────────────────

@Composable
private fun DraftConfigRow(
    state: DraftUiState.Ready,
    onLaneClick: () -> Unit,
    onPickPosClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MFColors.BgCard)
            .padding(horizontal = DraftDimens.Screen, vertical = DraftDimens.Slot),
        horizontalArrangement = Arrangement.spacedBy(DraftDimens.Section),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Setup:", color = MFColors.TextHint, fontSize = 10.sp)
        if (state.isSquad) {
            ConfigChip(label = "Squad — all slots", onClick = {}, dimmed = true)
        } else {
            val laneLabel = if (state.currentLanes.isEmpty()) "Lane: —"
            else "Lane: " + state.currentLanes.joinToString(", ") { it.displayName }
            val pickLabel = if (state.currentPickPositions.isEmpty()) "Pick: —"
            else "Pick: " + state.currentPickPositions.sorted().joinToString(",") { "#$it" }
            ConfigChip(label = laneLabel, onClick = onLaneClick)
            ConfigChip(label = pickLabel, onClick = onPickPosClick)
        }
    }
}

@Composable
private fun ConfigChip(label: String, onClick: () -> Unit, dimmed: Boolean = false) {
    val accent = if (dimmed) MFColors.TextHint else MFColors.Accent
    Box(
        modifier = Modifier
            .pressScale(enabled = !dimmed, onClick = onClick)
            .clip(RoundedCornerShape(16.dp))
            .background(MFColors.BgElevated)
            .border(1.dp, accent.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .padding(horizontal = 10.dp, vertical = DraftDimens.Inner)
    ) {
        Text(label, color = accent, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
    }
}

// ─── PORTRAIT ────────────────────────────────────────────────────────────────

@Composable
private fun DraftPortraitContent(
    state: DraftUiState.Ready,
    onNavigateToHeroSelect: (Int, Boolean, Boolean) -> Unit,
    onRemoveHero: (Int, Boolean, Boolean) -> Unit,
    onLaneClick: () -> Unit,
    onPickPosClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val userSlotIndices = userOwnedSlots(state)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MFColors.Bg)
            .verticalScroll(rememberScrollState())
            .animateContentSize()
    ) {
        PhaseBanner(state.turnMessage, state.isUserTurn, !state.draftState.isBanPhaseComplete)
        DraftConfigRow(state, onLaneClick, onPickPosClick)

        BanSection(
            allyBans = state.draftState.allyBans,
            enemyBans = state.draftState.enemyBans,
            banCountPerSide = state.draftState.banCountPerSide,
            banPhaseComplete = state.draftState.isBanPhaseComplete,
            onAllyBanClick = { idx -> onNavigateToHeroSelect(idx, true, true) },
            onEnemyBanClick = { idx -> onNavigateToHeroSelect(idx, false, true) },
            onRemoveAllyBan = { idx -> onRemoveHero(idx, true, true) },
            onRemoveEnemyBan = { idx -> onRemoveHero(idx, false, true) }
        )

        AnimatedSuggestions(
            visible = state.banSuggestions.isNotEmpty(),
            title = "BAN SUGGESTIONS",
            subtitle = banSubtitleFor(state),
            suggestions = state.banSuggestions,
            accentColor = MFColors.BanRed
        )

        Spacer(Modifier.height(DraftDimens.Inner))
        HorizontalDivider(color = MFColors.BgElevated, thickness = 1.dp)
        Spacer(Modifier.height(DraftDimens.Inner))

        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = DraftDimens.Section),
            horizontalArrangement = Arrangement.spacedBy(DraftDimens.Slot)) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(DraftDimens.Slot)) {
                TeamLabel("BLUE TEAM", MFColors.AllyBlue, isLeft = true)
                state.draftState.allySlots.forEachIndexed { idx, hero ->
                    val isActive = state.draftState.isCurrentPickSlot(idx, true)
                    val isUserSlot = idx in userSlotIndices
                    val isAwaiting = state.draftState.isBanPhaseComplete &&
                            !state.draftState.isAllyPickWave() && !state.draftState.isComplete && hero == null
                    PickSlot(hero = hero, isActiveSlot = isActive, isUserSlot = isUserSlot,
                        isAlly = true, isAwaitingTurn = isAwaiting,
                        onClick = { if (isActive) onNavigateToHeroSelect(idx, true, false) },
                        onRemove = { onRemoveHero(idx, true, false) })
                }
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(DraftDimens.Slot)) {
                TeamLabel("RED TEAM", MFColors.EnemyRed, isLeft = false)
                state.draftState.enemySlots.forEachIndexed { idx, hero ->
                    val isActive = state.draftState.isCurrentPickSlot(idx, false)
                    PickSlot(hero = hero, isActiveSlot = isActive, isUserSlot = false,
                        isAlly = false, isAwaitingTurn = false,
                        onClick = { if (isActive) onNavigateToHeroSelect(idx, false, false) },
                        onRemove = { onRemoveHero(idx, false, false) })
                }
            }
        }

        state.pickSuggestionGroups.forEach { group ->
            AnimatedSuggestions(
                visible = group.suggestions.isNotEmpty(),
                title = "PICK • ${group.label.uppercase()}",
                subtitle = pickGroupSubtitle(state, group),
                suggestions = group.suggestions,
                accentColor = MFColors.Accent,
                topSpacing = DraftDimens.Section
            )
        }
        Spacer(Modifier.height(16.dp))
    }
}

// ─── LANDSCAPE ───────────────────────────────────────────────────────────────

@Composable
private fun DraftLandscapeContent(
    state: DraftUiState.Ready,
    onNavigateToHeroSelect: (Int, Boolean, Boolean) -> Unit,
    onRemoveHero: (Int, Boolean, Boolean) -> Unit,
    onLaneClick: () -> Unit,
    onPickPosClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val userSlotIndices = userOwnedSlots(state)
    Row(modifier = modifier.fillMaxSize().background(MFColors.Bg)
        .padding(horizontal = DraftDimens.Section, vertical = DraftDimens.Inner),
        horizontalArrangement = Arrangement.spacedBy(DraftDimens.Section)) {

        Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(DraftDimens.Inner)) {
            TeamLabel("BLUE TEAM", MFColors.AllyBlue, isLeft = true)
            BanRow(state.draftState.allyBans, state.draftState.banCountPerSide,
                MFColors.AllyBlue, state.draftState.isBanPhaseComplete,
                modifier = Modifier.fillMaxWidth(),
                onBanClick = { idx -> onNavigateToHeroSelect(idx, true, true) },
                onRemoveBan = { idx -> onRemoveHero(idx, true, true) })
            state.draftState.allySlots.forEachIndexed { idx, hero ->
                val isActive = state.draftState.isCurrentPickSlot(idx, true)
                val isAwaiting = state.draftState.isBanPhaseComplete &&
                        !state.draftState.isAllyPickWave() && !state.draftState.isComplete && hero == null
                PickSlot(hero, isActive, idx in userSlotIndices, true, isAwaiting,
                    onClick = { if (isActive) onNavigateToHeroSelect(idx, true, false) },
                    onRemove = { onRemoveHero(idx, true, false) }, compact = true)
            }
        }

        Column(modifier = Modifier.weight(1.4f).verticalScroll(rememberScrollState()).animateContentSize(),
            verticalArrangement = Arrangement.spacedBy(DraftDimens.Slot)) {
            PhaseBanner(state.turnMessage, state.isUserTurn, !state.draftState.isBanPhaseComplete)
            DraftConfigRow(state, onLaneClick, onPickPosClick)
            AnimatedSuggestions(
                visible = state.banSuggestions.isNotEmpty(),
                title = "BAN SUGGESTIONS",
                subtitle = banSubtitleFor(state),
                suggestions = state.banSuggestions, accentColor = MFColors.BanRed
            )
            state.pickSuggestionGroups.forEach { group ->
                AnimatedSuggestions(
                    visible = group.suggestions.isNotEmpty(),
                    title = "PICK • ${group.label.uppercase()}",
                    subtitle = pickGroupSubtitle(state, group),
                    suggestions = group.suggestions, accentColor = MFColors.Accent
                )
            }
        }

        Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(DraftDimens.Inner)) {
            TeamLabel("RED TEAM", MFColors.EnemyRed, isLeft = false)
            BanRow(state.draftState.enemyBans, state.draftState.banCountPerSide,
                MFColors.EnemyRed, state.draftState.isBanPhaseComplete,
                modifier = Modifier.fillMaxWidth(),
                onBanClick = { idx -> onNavigateToHeroSelect(idx, false, true) },
                onRemoveBan = { idx -> onRemoveHero(idx, false, true) })
            state.draftState.enemySlots.forEachIndexed { idx, hero ->
                val isActive = state.draftState.isCurrentPickSlot(idx, false)
                PickSlot(hero, isActive, false, false, false,
                    onClick = { if (isActive) onNavigateToHeroSelect(idx, false, false) },
                    onRemove = { onRemoveHero(idx, false, false) }, compact = true)
            }
        }
    }
}

// ─── COMPONENTS ──────────────────────────────────────────────────────────────

private fun userOwnedSlots(state: DraftUiState.Ready): Set<Int> =
    if (state.isSquad) (0..4).toSet()
    else state.currentPickPositions.map { it - 1 }.toSet()

private fun banSubtitleFor(state: DraftUiState.Ready): String =
    if (state.currentLanes.isEmpty()) "High-priority bans, all lanes"
    else "High-priority bans for " + state.currentLanes.joinToString(", ") { it.displayName }

private fun pickGroupSubtitle(state: DraftUiState.Ready, group: PickSuggestionGroup): String =
    if (state.isSquad) "Top meta picks (excluding already-covered lanes)"
    else "Best meta + counters & synergy for ${group.lane?.displayName ?: "this lane"}"

@Composable
private fun AnimatedSuggestions(
    visible: Boolean,
    title: String,
    subtitle: String,
    suggestions: List<HeroSuggestion>,
    accentColor: Color,
    topSpacing: androidx.compose.ui.unit.Dp = 0.dp
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(220)) + expandVertically(tween(220)),
        exit = fadeOut(tween(160)) + shrinkVertically(tween(160))
    ) {
        Column {
            if (topSpacing > 0.dp) Spacer(Modifier.height(topSpacing))
            SuggestionsPanel(title, subtitle, suggestions, accentColor)
        }
    }
}

@Composable
private fun PhaseBanner(message: String, isUserTurn: Boolean, isBanPhase: Boolean) {
    val isDone = message.contains("Complete", ignoreCase = true)
    val targetBg = when {
        isDone -> MFColors.Success.copy(alpha = 0.2f)
        isUserTurn -> MFColors.Accent.copy(alpha = 0.15f)
        isBanPhase -> MFColors.BanRed.copy(alpha = 0.15f)
        else -> MFColors.BgElevated
    }
    val textColor = when {
        isDone -> MFColors.Success
        isUserTurn -> MFColors.Accent
        isBanPhase -> MFColors.BanRed
        else -> MFColors.TextSecondary
    }
    val bgColor by animateColorAsState(targetBg, tween(300), label = "bannerBg")
    Box(modifier = Modifier.fillMaxWidth().background(bgColor).padding(horizontal = 16.dp, vertical = 10.dp)) {
        Text(
            text = if (isUserTurn) "YOUR TURN — $message" else message,
            color = textColor, fontWeight = FontWeight.Bold, fontSize = 13.sp,
            textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun BanSection(
    allyBans: List<Hero?>, enemyBans: List<Hero?>, banCountPerSide: Int, banPhaseComplete: Boolean,
    onAllyBanClick: (Int) -> Unit, onEnemyBanClick: (Int) -> Unit,
    onRemoveAllyBan: (Int) -> Unit, onRemoveEnemyBan: (Int) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().background(MFColors.BgCard)
        .padding(horizontal = DraftDimens.Section, vertical = DraftDimens.Slot),
        verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(DraftDimens.Inner)) {
            Text("BLUE", color = MFColors.AllyBlue, fontWeight = FontWeight.Black, fontSize = 9.sp,
                modifier = Modifier.width(28.dp))
            repeat(banCountPerSide) { idx ->
                BanSlot(allyBans.getOrNull(idx), MFColors.AllyBlue, !banPhaseComplete,
                    Modifier.weight(1f).aspectRatio(1f),
                    onClick = { onAllyBanClick(idx) }, onRemove = { onRemoveAllyBan(idx) })
            }
        }
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(DraftDimens.Inner)) {
            Text("RED", color = MFColors.EnemyRed, fontWeight = FontWeight.Black, fontSize = 9.sp,
                modifier = Modifier.width(28.dp))
            repeat(banCountPerSide) { idx ->
                BanSlot(enemyBans.getOrNull(idx), MFColors.EnemyRed, !banPhaseComplete,
                    Modifier.weight(1f).aspectRatio(1f),
                    onClick = { onEnemyBanClick(idx) }, onRemove = { onRemoveEnemyBan(idx) })
            }
        }
    }
}

@Composable
private fun BanRow(
    bans: List<Hero?>, banCountPerSide: Int, color: Color, banPhaseComplete: Boolean,
    modifier: Modifier = Modifier, onBanClick: (Int) -> Unit, onRemoveBan: (Int) -> Unit
) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(DraftDimens.Inner)) {
        repeat(banCountPerSide) { idx ->
            BanSlot(bans.getOrNull(idx), color, !banPhaseComplete,
                Modifier.weight(1f).aspectRatio(1f),
                onClick = { onBanClick(idx) }, onRemove = { onRemoveBan(idx) })
        }
    }
}

@Composable
private fun BanSlot(
    hero: Hero?, color: Color, canBan: Boolean,
    modifier: Modifier = Modifier, onClick: () -> Unit, onRemove: () -> Unit
) {
    val clickMod = if (canBan && hero == null) Modifier.pressScale(onClick = onClick) else Modifier
    Box(
        modifier = modifier
            .then(clickMod)
            .clip(RoundedCornerShape(6.dp))
            .background(if (hero != null) color.copy(alpha = 0.15f) else MFColors.BgElevated)
            .border(1.dp, if (hero != null) color.copy(alpha = 0.6f) else
                if (canBan) color.copy(alpha = 0.4f) else MFColors.TextHint.copy(alpha = 0.3f),
                RoundedCornerShape(6.dp)),
        contentAlignment = Alignment.Center
    ) {
        if (hero != null) {
            AsyncImage(model = hero.imageUrl, contentDescription = hero.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(6.dp)))
            Box(modifier = Modifier.fillMaxSize()
                .background(Color.Black.copy(alpha = 0.25f), RoundedCornerShape(6.dp)))
            Box(
                modifier = Modifier.align(Alignment.TopEnd).size(16.dp)
                    .clip(CircleShape).background(MFColors.BanRed).clickable { onRemove() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Close, "Remove ban", tint = Color.White, modifier = Modifier.size(10.dp))
            }
        } else if (canBan) {
            Text("+", color = color, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        } else {
            Text("—", color = MFColors.TextHint, fontSize = 12.sp)
        }
    }
}

@Composable
private fun TeamLabel(label: String, color: Color, isLeft: Boolean) {
    Text(label, color = color, fontWeight = FontWeight.Black, fontSize = 11.sp, letterSpacing = 1.sp,
        textAlign = if (isLeft) TextAlign.Start else TextAlign.End,
        modifier = Modifier.fillMaxWidth().padding(horizontal = DraftDimens.Inner, vertical = 2.dp))
}

@Composable
private fun PickSlot(
    hero: Hero?, isActiveSlot: Boolean, isUserSlot: Boolean,
    isAlly: Boolean, isAwaitingTurn: Boolean,
    onClick: () -> Unit, onRemove: () -> Unit, compact: Boolean = false
) {
    val slotColor = if (isAlly) MFColors.AllyBlue else MFColors.EnemyRed
    val height = if (compact) 52.dp else 68.dp
    val targetBorder = when {
        isUserSlot -> MFColors.Accent
        hero != null -> slotColor.copy(alpha = 0.5f)
        isActiveSlot -> slotColor
        else -> MFColors.TextHint.copy(alpha = 0.2f)
    }
    val borderColor by animateColorAsState(targetBorder, tween(250), label = "slotBorder")
    val clickMod = if (isActiveSlot && hero == null) Modifier.pressScale(onClick = onClick) else Modifier
    Box(
        modifier = Modifier.fillMaxWidth().height(height)
            .then(clickMod)
            .clip(RoundedCornerShape(8.dp))
            .background(if (isUserSlot || hero != null || isActiveSlot) slotColor.copy(alpha = 0.08f) else MFColors.BgCard)
            .border(if (isUserSlot) 2.dp else 1.dp, borderColor, RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.CenterStart
    ) {
        if (hero != null) {
            Row(modifier = Modifier.fillMaxSize().padding(horizontal = DraftDimens.Slot), verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(model = hero.imageUrl, contentDescription = hero.name, contentScale = ContentScale.Crop,
                    modifier = Modifier.size(if (compact) 40.dp else 52.dp).clip(RoundedCornerShape(6.dp))
                        .background(MFColors.BgElevated))
                Spacer(Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(hero.name, color = MFColors.TextPrimary, fontWeight = FontWeight.Bold,
                        fontSize = if (compact) 11.sp else 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(hero.role.ifEmpty { hero.lane.displayName }, color = MFColors.TextSecondary, fontSize = 9.sp)
                }
            }
            Box(
                modifier = Modifier.align(Alignment.TopEnd).padding(3.dp).size(18.dp)
                    .clip(CircleShape).background(MFColors.BanRed.copy(alpha = 0.9f)).clickable { onRemove() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Close, "Remove", tint = Color.White, modifier = Modifier.size(11.dp))
            }
            if (isUserSlot) {
                Box(modifier = Modifier.align(Alignment.CenterStart).width(3.dp).fillMaxHeight()
                    .background(MFColors.Accent, RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp)))
            }
        } else {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                when {
                    isUserSlot && isActiveSlot -> Text("YOUR PICK", color = MFColors.Accent,
                        fontWeight = FontWeight.Bold, fontSize = 10.sp)
                    isUserSlot -> Text("YOUR PICK", color = MFColors.Accent.copy(alpha = 0.4f),
                        fontWeight = FontWeight.Medium, fontSize = 10.sp)
                    isActiveSlot -> Text("+", color = slotColor.copy(alpha = 0.8f), fontSize = 20.sp)
                    isAwaitingTurn -> Text("Awaiting Turn", color = MFColors.TextHint, fontSize = 9.sp)
                    else -> Text("—", color = MFColors.TextHint, fontSize = 14.sp)
                }
            }
            if (isUserSlot) {
                Box(modifier = Modifier.align(Alignment.CenterStart).width(3.dp).fillMaxHeight()
                    .background(MFColors.Accent.copy(alpha = if (isActiveSlot) 1f else 0.4f),
                        RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp)))
            }
        }
    }
}

@Composable
private fun SuggestionsPanel(
    title: String, subtitle: String,
    suggestions: List<HeroSuggestion>, accentColor: Color
) {
    Column(modifier = Modifier.fillMaxWidth().background(MFColors.BgCard).padding(DraftDimens.Screen)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(8.dp).background(accentColor, CircleShape))
            Spacer(Modifier.width(DraftDimens.Slot))
            Text(title, color = accentColor, fontWeight = FontWeight.Black, fontSize = 12.sp, letterSpacing = 1.sp)
            Spacer(Modifier.width(DraftDimens.Section))
            Text(subtitle, color = MFColors.TextHint, fontSize = 10.sp)
        }
        Spacer(Modifier.height(10.dp))
        Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            suggestions.forEachIndexed { idx, sug -> SuggestionCard(idx + 1, sug, accentColor) }
        }
    }
}

@Composable
private fun SuggestionCard(rank: Int, suggestion: HeroSuggestion, accentColor: Color) {
    val tierColor = Color(suggestion.hero.tier.colorValue)
    val hasWarning = suggestion.warnings.isNotEmpty()
    Column(
        modifier = Modifier.width(120.dp)
            .background(MFColors.BgElevated, RoundedCornerShape(10.dp))
            .border(1.dp, if (hasWarning) MFColors.Warning.copy(alpha = 0.7f) else tierColor.copy(alpha = 0.5f),
                RoundedCornerShape(10.dp))
            .padding(DraftDimens.Section),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box {
            AsyncImage(model = suggestion.hero.portraitUrl, contentDescription = suggestion.hero.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(60.dp).clip(RoundedCornerShape(8.dp)).background(MFColors.BgCard))
            Box(modifier = Modifier.align(Alignment.TopStart)
                .background(tierColor, RoundedCornerShape(topStart = 8.dp, bottomEnd = 5.dp))
                .padding(horizontal = DraftDimens.Inner, vertical = 2.dp)) {
                Text(suggestion.hero.tier.label, color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Black)
            }
            Box(modifier = Modifier.align(Alignment.TopEnd).size(18.dp)
                .background(MFColors.BgCard.copy(alpha = 0.9f), CircleShape)
                .border(1.dp, accentColor.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center) {
                Text("#$rank", color = accentColor, fontSize = 7.sp, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(Modifier.height(5.dp))
        Text(suggestion.hero.name, color = MFColors.TextPrimary, fontWeight = FontWeight.Bold, fontSize = 10.sp,
            maxLines = 1, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center)
        Text("${suggestion.totalScore}pt", color = accentColor, fontSize = 9.sp, fontWeight = FontWeight.SemiBold)
        if (suggestion.reasons.isNotEmpty()) {
            Spacer(Modifier.height(3.dp))
            Text(suggestion.reasons.first(), color = MFColors.TextSecondary, fontSize = 8.sp,
                maxLines = 2, textAlign = TextAlign.Center, lineHeight = 11.sp)
        }
        if (hasWarning) {
            Spacer(Modifier.height(3.dp))
            Row(
                modifier = Modifier.fillMaxWidth()
                    .background(MFColors.Warning.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 3.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.WarningAmber, contentDescription = null, tint = MFColors.Warning, modifier = Modifier.size(9.dp))
                Spacer(Modifier.width(2.dp))
                Text(suggestion.warnings.first(), color = MFColors.Warning, fontSize = 7.sp, maxLines = 2, lineHeight = 10.sp)
            }
        }
    }
}