package com.example.metaforge.presentation.screens.draft_setup

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.metaforge.domain.model.HeroLane
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
                        fontSize = 16.sp
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
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(Modifier.height(20.dp))

            // 1. TARGET TIER — controls ban count (Epic=3, Legend=4, Mythic=5)
            SetupSection("1. TARGET TIER  —  ${uiState.banCountPerSide} BANS / TEAM") {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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
            SetupSection("2. PARTY SIZE") {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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
            SetupSection("3. TEAM SIDE") {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SetupChip(
                        text = "Blue (1st Pick)",
                        selected = uiState.isFirstPick,
                        enabled = true,
                        modifier = Modifier.weight(1f),
                        accentColor = MFColors.AllyBlue,
                        onClick = { viewModel.setFirstPick(true) }
                    )
                    SetupChip(
                        text = "Red (2nd Pick)",
                        selected = !uiState.isFirstPick,
                        enabled = true,
                        modifier = Modifier.weight(1f),
                        accentColor = MFColors.EnemyRed,
                        onClick = { viewModel.setFirstPick(false) }
                    )
                }
            }

            // 4 & 5: HIDDEN for Squad — recommendations cover all 5 ally slots.
            AnimatedVisibility(visible = !uiState.isSquad, enter = fadeIn(), exit = fadeOut()) {
                Column {
                    // 4. PICK ORDER — radio buttons. Up to [partySize] selectable;
                    // once the cap is hit, unselected rows are disabled until the
                    // user clears one.
                    SetupSection(
                        "4. YOUR PICK ORDER  —  pick ${uiState.partySize} (${uiState.pickPositions.size}/${uiState.partySize})"
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

                    // 5. PREFERRED LANES — same radio pattern, vertical list.
                    SetupSection(
                        "5. PREFERRED LANE  —  pick ${uiState.partySize} (${uiState.preferredLanes.size}/${uiState.partySize})"
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

            AnimatedVisibility(visible = uiState.isSquad, enter = fadeIn(), exit = fadeOut()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MFColors.BgCard, RoundedCornerShape(10.dp))
                        .padding(14.dp)
                ) {
                    Text(
                        "Squad mode — recommendations cover all 5 ally slots, prioritising the lane your team is missing and the strongest meta picks.",
                        color = MFColors.TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            }

            Spacer(Modifier.height(32.dp))

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
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MFColors.Accent,
                    disabledContainerColor = MFColors.BgCard
                )
            ) {
                Text(
                    if (canStart) "START SIMULATOR"
                    else "Select ${uiState.partySize} pick order(s) and lane(s)",
                    fontWeight = FontWeight.Bold,
                    color = if (canStart) MFColors.Bg else MFColors.TextHint,
                    fontSize = 15.sp
                )
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SetupSection(title: String, content: @Composable () -> Unit) {
    Text(title, color = MFColors.Accent, fontWeight = FontWeight.Bold, fontSize = 13.sp, letterSpacing = 0.5.sp)
    Spacer(Modifier.height(10.dp))
    content()
    Spacer(Modifier.height(24.dp))
}

@Composable
private fun RadioOption(
    label: String,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled) { onClick() }
            .padding(vertical = 6.dp, horizontal = 2.dp),
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
        Spacer(Modifier.width(8.dp))
        Text(
            label,
            color = when {
                selected -> MFColors.Accent
                enabled -> MFColors.TextPrimary
                else -> MFColors.TextHint
            },
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
    val bg = when {
        selected && enabled -> accentColor.copy(alpha = 0.18f)
        !enabled -> MFColors.BgCard.copy(alpha = 0.4f)
        else -> MFColors.BgCard
    }
    val border = when {
        selected -> accentColor
        !enabled -> MFColors.TextHint.copy(alpha = 0.3f)
        else -> MFColors.BgElevated
    }
    val textColor = when {
        selected && enabled -> accentColor
        !enabled -> MFColors.TextHint
        else -> MFColors.TextSecondary
    }
    Box(
        modifier = modifier
            .height(48.dp)
            .background(bg, RoundedCornerShape(10.dp))
            .border(1.dp, border, RoundedCornerShape(10.dp))
            .clickable(enabled = enabled) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = textColor, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal, fontSize = 13.sp)
    }
}
