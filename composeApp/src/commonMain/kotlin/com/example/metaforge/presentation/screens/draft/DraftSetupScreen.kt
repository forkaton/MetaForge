package com.example.metaforge.presentation.screens.draft

import androidx.compose.foundation.BorderStroke
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
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            SectionHeader("1. TARGET TIER")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OptionCard("Epic (6)", uiState.rank == "Epic", false, Modifier.weight(1f)) {}
                OptionCard("Legend (8)", uiState.rank == "Legend", false, Modifier.weight(1f)) {}
                OptionCard("Mythic (10)", uiState.rank == "Mythic", true, Modifier.weight(1f)) {
                    viewModel.setRank("Mythic")
                }
            }
            Spacer(modifier = Modifier.height(24.dp))

            SectionHeader("2. PARTY SIZE")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OptionCard("Solo", uiState.partySize == 1, true, Modifier.weight(1f)) {
                    viewModel.setPartySize(1)
                }
                OptionCard("Duo", uiState.partySize == 2, false, Modifier.weight(1f)) {}
                OptionCard("Trio", uiState.partySize == 3, false, Modifier.weight(1f)) {}
                OptionCard("Squad", uiState.partySize == 5, false, Modifier.weight(1f)) {}
            }
            Spacer(modifier = Modifier.height(24.dp))

            SectionHeader("3. TEAM ORDER")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OptionCard("First Pick (Blue)", uiState.isFirstPick, true, Modifier.weight(1f)) {
                    viewModel.setFirstPick(true)
                }
                OptionCard("Second Pick (Red)", !uiState.isFirstPick, true, Modifier.weight(1f)) {
                    viewModel.setFirstPick(false)
                }
            }
            Spacer(modifier = Modifier.height(24.dp))

            if (uiState.partySize == 1) {
                SectionHeader("4. YOUR PICK ORDER (1-5)")
                Slider(
                    value = uiState.pickPosition.toFloat(),
                    onValueChange = { viewModel.setPickPosition(it.toInt()) },
                    valueRange = 1f..5f,
                    steps = 3,
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.secondary
                    )
                )
                Text(
                    "You are Pick #${uiState.pickPosition} for your team.",
                    color = Color.LightGray,
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            SectionHeader(if (uiState.partySize == 1) "5. PREFERRED ROLE" else "4. PREFERRED ROLE")
            var expandedRole by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = expandedRole,
                onExpandedChange = { expandedRole = !expandedRole }
            ) {
                OutlinedTextField(
                    value = uiState.preferredRole.displayName,
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant,
                        focusedBorderColor = MaterialTheme.colorScheme.primary
                    ),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedRole) }
                )
                ExposedDropdownMenu(
                    expanded = expandedRole,
                    onDismissRequest = { expandedRole = false },
                    modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                ) {
                    HeroRole.entries.forEach { role ->
                        DropdownMenuItem(
                            text = { Text(role.displayName, color = Color.White) },
                            onClick = {
                                viewModel.setPreferredRole(role)
                                expandedRole = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.height(32.dp))

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
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        title,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Black,
        style = MaterialTheme.typography.labelLarge
    )
    Spacer(modifier = Modifier.height(12.dp))
}

@Composable
fun OptionCard(
    text: String,
    selected: Boolean,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val bgColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
    val textColor = if (selected) Color.White else Color.Gray
    val borderColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    Surface(
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(8.dp),
        color = if (enabled) bgColor else bgColor.copy(alpha = 0.3f),
        border = BorderStroke(1.dp, borderColor),
        onClick = onClick,
        enabled = enabled
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = text,
                color = if (enabled) textColor else textColor.copy(alpha = 0.3f),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}