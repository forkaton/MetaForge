package com.example.metaforge.presentation.screens.draft_setup

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
    onStartDraft: (String, Int, Int, Boolean, String) -> Unit,
    viewModel: DraftSetupViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

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

            // 1. TARGET TIER
            SetupSection("1. TARGET TIER") {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Epic", "Legend", "Mythic").forEach { rank ->
                        val enabled = rank == "Mythic"
                        SetupChip(
                            text = rank,
                            selected = uiState.rank == rank,
                            enabled = enabled,
                            modifier = Modifier.weight(1f),
                            onClick = { if (enabled) viewModel.setRank(rank) }
                        )
                    }
                }
            }

            // 2. PARTY SIZE
            SetupSection("2. PARTY SIZE") {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(1 to "Solo", 2 to "Duo", 3 to "Trio", 5 to "Squad").forEach { (size, label) ->
                        val enabled = size == 1
                        SetupChip(
                            text = label,
                            selected = uiState.partySize == size,
                            enabled = enabled,
                            modifier = Modifier.weight(1f),
                            onClick = { if (enabled) viewModel.setPartySize(size) }
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

            // 4. PICK ORDER
            SetupSection("4. YOUR PICK ORDER  —  #${uiState.pickPosition}") {
                Slider(
                    value = uiState.pickPosition.toFloat(),
                    onValueChange = { viewModel.setPickPosition(it.toInt()) },
                    valueRange = 1f..5f,
                    steps = 3,
                    colors = SliderDefaults.colors(
                        thumbColor = MFColors.Accent,
                        activeTrackColor = MFColors.Accent,
                        inactiveTrackColor = MFColors.BgElevated
                    )
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    (1..5).forEach { n ->
                        Text(
                            "$n",
                            color = if (uiState.pickPosition == n) MFColors.Accent else MFColors.TextHint,
                            fontSize = 12.sp,
                            fontWeight = if (uiState.pickPosition == n) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            // 5. PREFERRED LANE
            SetupSection("5. PREFERRED LANE") {
                var expanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                    OutlinedTextField(
                        value = uiState.preferredLane.displayName,
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryEditable).fillMaxWidth(),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MFColors.Accent,
                            unfocusedBorderColor = MFColors.BgElevated,
                            focusedTextColor = MFColors.TextPrimary,
                            unfocusedTextColor = MFColors.TextPrimary,
                            unfocusedContainerColor = MFColors.BgCard,
                            focusedContainerColor = MFColors.BgCard
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.background(MFColors.BgCard)
                    ) {
                        HeroLane.entries.forEach { lane ->
                            DropdownMenuItem(
                                text = { Text(lane.displayName, color = MFColors.TextPrimary) },
                                onClick = { viewModel.setPreferredLane(lane); expanded = false }
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(32.dp))

            // START BUTTON
            Button(
                onClick = {
                    onStartDraft(
                        uiState.rank,
                        uiState.partySize,
                        uiState.pickPosition,
                        uiState.isFirstPick,
                        uiState.preferredLane.name
                    )
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MFColors.Accent)
            ) {
                Text("START SIMULATOR", fontWeight = FontWeight.Bold, color = MFColors.Bg, fontSize = 15.sp)
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
