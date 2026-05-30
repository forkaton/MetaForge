package com.example.metaforge.presentation.screens.draft_setup

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.metaforge.domain.model.HeroLane
import com.example.metaforge.presentation.components.SectionCard
import com.example.metaforge.presentation.components.pressScale
import com.example.metaforge.ui.theme.MFColors
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DraftSetupScreen(
    onNavigateBack: () -> Unit,
    onStartDraft: (
        rank: String,
        partySize: Int,
        pickPositions: List<Int>,
        isFirstPick: Boolean,
        preferredLanes: List<HeroLane>,
        banCount: Int
    ) -> Unit,
    viewModel: DraftSetupViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val canStart = uiState.isSquad ||
            (uiState.pickPositions.size == uiState.partySize &&
                    uiState.preferredLanes.size == uiState.partySize)

    Scaffold(
        containerColor = MFColors.Bg,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "DRAFT CONFIGURATION",
                        color = MFColors.TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        letterSpacing = 0.5.sp
                    )
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .widthIn(max = 480.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. TARGET TIER
                SectionCard("1. TARGET TIER — ${uiState.banCountPerSide} BANS / TEAM") {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Epic", "Legend", "Mythic").forEach { rank ->
                            SetupChip(
                                text = rank,
                                selected = uiState.rank == rank,
                                enabled = true,
                                modifier = Modifier.weight(1f),
                                onClick = { viewModel.setRank(rank) }
                            )
                        }
                    }
                }

                // 2. PARTY SIZE
                SectionCard("2. PARTY SIZE") {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(1 to "Solo", 2 to "Duo", 3 to "Trio", 5 to "Squad").forEach { (size, label) ->
                            SetupChip(
                                text = label,
                                selected = uiState.partySize == size,
                                enabled = true,
                                modifier = Modifier.weight(1f),
                                onClick = { viewModel.setPartySize(size) }
                            )
                        }
                    }
                }

                // 3. TEAM SIDE
                SectionCard("3. TEAM SIDE") {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SetupChip(
                            text = "Blue (1st)",
                            selected = uiState.isFirstPick,
                            enabled = true,
                            modifier = Modifier.weight(1f),
                            accentColor = MFColors.AllyBlue,
                            onClick = { viewModel.setFirstPick(true) }
                        )
                        SetupChip(
                            text = "Red (2nd)",
                            selected = !uiState.isFirstPick,
                            enabled = true,
                            modifier = Modifier.weight(1f),
                            accentColor = MFColors.EnemyRed,
                            onClick = { viewModel.setFirstPick(false) }
                        )
                    }
                }

                // 4 & 5: HIDDEN for Squad
                AnimatedVisibility(visible = !uiState.isSquad, enter = fadeIn(), exit = fadeOut()) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        // 4. PICK ORDER
                        SectionCard(
                            "4. YOUR PICK ORDER — (${uiState.pickPositions.size}/${uiState.partySize})"
                        ) {
                            Column {
                                (1..5).forEach { pos ->
                                    val isSelected = pos in uiState.pickPositions
                                    val canSelectMore = uiState.pickPositions.size < uiState.partySize
                                    RadioOption(
                                        label = "Slot $pos",
                                        selected = isSelected,
                                        enabled = isSelected || canSelectMore,
                                        onClick = { viewModel.togglePickPosition(pos) }
                                    )
                                }
                            }
                        }

                        // 5. PREFERRED LANES
                        SectionCard(
                            "5. PREFERRED LANE — (${uiState.preferredLanes.size}/${uiState.partySize})"
                        ) {
                            Column {
                                HeroLane.entries.forEach { lane ->
                                    val isSelected = lane in uiState.preferredLanes
                                    val canSelectMore = uiState.preferredLanes.size < uiState.partySize
                                    RadioOption(
                                        label = lane.displayName,
                                        selected = isSelected,
                                        enabled = isSelected || canSelectMore,
                                        onClick = { viewModel.togglePreferredLane(lane) }
                                    )
                                }
                            }
                        }
                    }
                }

                // Squad mode info
                AnimatedVisibility(visible = uiState.isSquad, enter = fadeIn(), exit = fadeOut()) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MFColors.BgCard.copy(alpha = 0.5f),
                        border = borderStroke(1.dp, MFColors.Accent.copy(alpha = 0.2f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "Squad mode — recommendations cover all 5 ally slots, prioritising the strongest meta picks.",
                            color = MFColors.TextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            modifier = Modifier.padding(14.dp)
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                // START BUTTON
                Button(
                    onClick = {
                        onStartDraft(
                            uiState.rank,
                            uiState.partySize,
                            uiState.pickPositions.toList().sorted(),
                            uiState.isFirstPick,
                            uiState.preferredLanes.toList(),
                            uiState.banCountPerSide
                        )
                    },
                    enabled = canStart,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MFColors.Primary,
                        disabledContainerColor = MFColors.BgCard
                    )
                ) {
                    Text(
                        if (canStart) "START SIMULATOR"
                        else "Select pick order(s) and lane(s)",
                        fontWeight = FontWeight.ExtraBold,
                        color = if (canStart) Color.White else MFColors.TextHint,
                        fontSize = 15.sp,
                        letterSpacing = 1.sp
                    )
                }

                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

private fun borderStroke(width: androidx.compose.ui.unit.Dp, color: Color) =
    androidx.compose.foundation.BorderStroke(width, color)

@Composable
private fun RadioOption(
    label: String,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val textColor by animateColorAsState(
        targetValue = when {
            selected -> MFColors.Accent
            enabled -> MFColors.TextPrimary
            else -> MFColors.TextHint
        },
        animationSpec = tween(200),
        label = "radioText"
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .pressScale(enabled = enabled, onClick = onClick)
            .padding(vertical = 6.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = null,
            enabled = enabled,
            colors = RadioButtonDefaults.colors(
                selectedColor = MFColors.Accent,
                unselectedColor = MFColors.TextHint,
                disabledSelectedColor = MFColors.Accent.copy(alpha = 0.5f),
                disabledUnselectedColor = MFColors.TextHint.copy(alpha = 0.3f)
            )
        )
        Spacer(Modifier.width(4.dp))
        Text(
            label,
            color = textColor,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            fontSize = 14.sp
        )
    }
}

@Composable
private fun SetupChip(
    text: String,
    selected: Boolean,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    accentColor: Color = MFColors.Accent,
    onClick: () -> Unit
) {
    val targetBg = when {
        selected && enabled -> accentColor.copy(alpha = 0.12f)
        !enabled -> MFColors.BgCard.copy(alpha = 0.4f)
        else -> MFColors.Bg.copy(alpha = 0.5f)
    }
    val targetBorder = when {
        selected -> accentColor
        !enabled -> MFColors.TextHint.copy(alpha = 0.3f)
        else -> MFColors.BgElevated
    }
    val targetText = when {
        selected && enabled -> accentColor
        !enabled -> MFColors.TextHint
        else -> MFColors.TextSecondary
    }
    val bg by animateColorAsState(targetBg, tween(220), label = "chipBg")
    val border by animateColorAsState(targetBorder, tween(220), label = "chipBorder")
    val textColor by animateColorAsState(targetText, tween(220), label = "chipText")

    Box(
        modifier = modifier
            .height(44.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(10.dp))
            .pressScale(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text,
            color = textColor,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            fontSize = 13.sp
        )
    }
}