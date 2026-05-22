package com.example.metaforge.presentation.screens.draft

import androidx.compose.foundation.background
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
import com.example.metaforge.domain.model.HeroRole
import com.example.metaforge.presentation.components.PrimaryButton
import com.example.metaforge.presentation.theme.MetaForgeColors
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
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "DRAFT CONFIGURATION",
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MetaForgeColors.DeepNavy
                )
            )
        },
        containerColor = MetaForgeColors.DeepNavy
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(Modifier.height(16.dp))

            // SECTION 1 - RANK
            SectionHeader("1. TARGET TIER")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Epic", "Legend", "Mythic").forEach { rank ->
                    val isEnabled = rank == "Mythic"
                    OptionChip(
                        text = rank,
                        selected = uiState.rank == rank,
                        enabled = isEnabled,
                        modifier = Modifier.weight(1f),
                        onClick = { if (isEnabled) viewModel.setRank(rank) }
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // SECTION 2 - PARTY SIZE
            SectionHeader("2. PARTY SIZE")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(1 to "Solo", 2 to "Duo", 3 to "Trio", 5 to "Squad")
                    .forEach { (size, label) ->
                        val isEnabled = size == 1
                        OptionChip(
                            text = label,
                            selected = uiState.partySize == size,
                            enabled = isEnabled,
                            modifier = Modifier.weight(1f),
                            onClick = { if (isEnabled) viewModel.setPartySize(size) }
                        )
                    }
            }

            Spacer(Modifier.height(24.dp))

            // SECTION 3 - TEAM ORDER
            SectionHeader("3. TEAM ORDER")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OptionChip(
                    text = "First Pick (Blue)",
                    selected = uiState.isFirstPick,
                    enabled = true,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.setFirstPick(true) }
                )
                OptionChip(
                    text = "Second Pick (Red)",
                    selected = !uiState.isFirstPick,
                    enabled = true,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.setFirstPick(false) }
                )
            }

            Spacer(Modifier.height(24.dp))

            // SECTION 4 - PICK ORDER
            SectionHeader("4. YOUR PICK ORDER (1-5)")
            Slider(
                value = uiState.pickPosition.toFloat(),
                onValueChange = { viewModel.setPickPosition(it.toInt()) },
                valueRange = 1f..5f,
                steps = 3,
                colors = SliderDefaults.colors(
                    thumbColor = MetaForgeColors.CyberBlue,
                    activeTrackColor = MetaForgeColors.CyberBlue,
                    inactiveTrackColor = MetaForgeColors.DarkCard
                )
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                (1..5).forEach { pos ->
                    Text(
                        "#$pos",
                        color = if (uiState.pickPosition == pos)
                            MetaForgeColors.CyberBlue
                        else Color.Gray,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (uiState.pickPosition == pos)
                            FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
            Text(
                "You are Pick #${uiState.pickPosition} for your team.",
                color = MetaForgeColors.TextSecondary,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(Modifier.height(24.dp))

            // SECTION 5 - ROLE
            SectionHeader("5. PREFERRED ROLE")
            var expandedRole by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = expandedRole,
                onExpandedChange = { expandedRole = !expandedRole }
            ) {
                OutlinedTextField(
                    value = "${uiState.preferredRole.displayName}",
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = MetaForgeColors.CyberBlue.copy(alpha = 0.3f),
                        focusedBorderColor = MetaForgeColors.CyberBlue,
                        unfocusedTextColor = Color.White,
                        focusedTextColor = Color.White
                    ),
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedRole)
                    }
                )
                ExposedDropdownMenu(
                    expanded = expandedRole,
                    onDismissRequest = { expandedRole = false },
                    modifier = Modifier.background(MetaForgeColors.DarkCard)
                ) {
                    HeroRole.entries.forEach { role ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    role.displayName,
                                    color = if (uiState.preferredRole == role)
                                        MetaForgeColors.CyberBlue
                                    else Color.White
                                )
                            },
                            onClick = {
                                viewModel.setPreferredRole(role)
                                expandedRole = false
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(40.dp))

            // START BUTTON
            PrimaryButton(
                text = "START SIMULATOR",
                onClick = {
                    onStartDraft(
                        uiState.rank,
                        uiState.partySize,
                        uiState.pickPosition,
                        uiState.isFirstPick,
                        uiState.preferredRole.name
                    )
                }
            )

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        title,
        color = MetaForgeColors.CyberBlue,
        fontWeight = FontWeight.Black,
        style = MaterialTheme.typography.labelLarge,
        letterSpacing = 1.sp
    )
    Spacer(Modifier.height(12.dp))
}

@Composable
private fun OptionChip(
    text: String,
    selected: Boolean,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val bgColor = when {
        selected && enabled -> MetaForgeColors.CyberBlue.copy(alpha = 0.2f)
        else -> MetaForgeColors.DarkCard
    }
    val borderColor = when {
        selected && enabled -> MetaForgeColors.CyberBlue
        !enabled -> Color.DarkGray
        else -> MetaForgeColors.DarkCard
    }
    val textColor = when {
        selected && enabled -> MetaForgeColors.CyberBlue
        !enabled -> Color.DarkGray
        else -> Color.Gray
    }

    Surface(
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(10.dp),
        color = bgColor,
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
        onClick = onClick,
        enabled = enabled
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text,
                color = textColor,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}
