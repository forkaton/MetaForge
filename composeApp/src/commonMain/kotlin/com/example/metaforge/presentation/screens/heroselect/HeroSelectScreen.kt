package com.example.metaforge.presentation.screens.heroselect

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.metaforge.domain.model.HeroRole
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
        isBan -> Color(0xFFFF4444)
        isAlly -> Color(0xFF00BFFF)
        else -> Color(0xFFFF6B35)
    }
    val headerText = when {
        isBan -> "BAN HERO — Slot #${slotIndex + 1}"
        isAlly -> "PICK HERO — Blue Slot #${slotIndex + 1}"
        else -> "PICK HERO — Red Slot #${slotIndex + 1}"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        headerText,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp,
                        color = accentColor
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
                    containerColor = Color(0xFF0D0D1A)
                )
            )
        },
        containerColor = Color(0xFF0D0D1A)
    ) { paddingValues ->
        when (val state = uiState) {
            is HeroSelectUiState.Loading -> Box(
                Modifier.fillMaxSize(),
                Alignment.Center
            ) {
                CircularProgressIndicator(color = accentColor)
            }

            is HeroSelectUiState.Error -> Box(
                Modifier.fillMaxSize(),
                Alignment.Center
            ) {
                Text(state.message, color = MaterialTheme.colorScheme.error)
            }

            is HeroSelectUiState.Ready -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    // Search Bar
                    SearchBar(
                        query = state.searchQuery,
                        onQueryChange = viewModel::onSearchQueryChange,
                        accentColor = accentColor
                    )

                    // Role Filter Chips
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            RoleFilterChip(
                                label = "ALL",
                                isSelected = state.selectedRole == null,
                                accentColor = accentColor,
                                onClick = { viewModel.filterByRole(null) }
                            )
                        }
                        lazyRowItems(HeroRole.entries) { role ->
                            RoleFilterChip(
                                label = role.displayName.uppercase(),
                                isSelected = state.selectedRole == role,
                                accentColor = accentColor,
                                onClick = { viewModel.filterByRole(role) }
                            )
                        }
                    }

                    // Hero Count
                    Text(
                        text = "${state.filteredHeroes.size} heroes available",
                        color = Color.Gray,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )

                    // Hero Grid
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(4),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        items(state.filteredHeroes) { hero ->
                            val isPicked = state.pickedHeroNames.contains(hero.name)
                            val cardColor by animateColorAsState(
                                targetValue = if (isPicked) Color(0xFF1A1A2E)
                                else Color(0xFF16213E),
                                animationSpec = tween(300),
                                label = "cardColor"
                            )

                            Box(
                                modifier = Modifier
                                    .aspectRatio(0.75f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(cardColor)
                                    .border(
                                        width = if (!isPicked) 1.dp else 0.dp,
                                        brush = Brush.verticalGradient(
                                            colors = listOf(
                                                accentColor.copy(alpha = 0.6f),
                                                Color.Transparent
                                            )
                                        ),
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .clickable(enabled = !isPicked) {
                                        viewModel.pickHero(slotIndex, isAlly, isBan, hero)
                                        onNavigateBack()
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(8.dp)
                                ) {
                                    // Hero Avatar Placeholder
                                    Box(
                                        modifier = Modifier
                                            .size(56.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                Brush.radialGradient(
                                                    colors = if (isPicked)
                                                        listOf(Color.DarkGray, Color.Black)
                                                    else
                                                        listOf(
                                                            accentColor.copy(alpha = 0.4f),
                                                            Color(0xFF0D0D1A)
                                                        )
                                                )
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = hero.role.displayName.take(3).uppercase(),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Black,
                                            color = if (isPicked) Color.Gray else accentColor,
                                            fontSize = 10.sp
                                        )
                                    }

                                    Spacer(Modifier.height(6.dp))

                                    Text(
                                        text = hero.name,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isPicked) Color.Gray else Color.White,
                                        maxLines = 1
                                    )

                                    Text(
                                        text = if (isPicked) "PICKED" else hero.role.displayName,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isPicked) Color(0xFFFF4444)
                                        else accentColor.copy(alpha = 0.8f),
                                        fontSize = 9.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    accentColor: Color
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF16213E))
            .border(1.dp, accentColor.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Search, null, tint = accentColor, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(10.dp))
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                singleLine = true,
                cursorBrush = SolidColor(accentColor),
                textStyle = MaterialTheme.typography.bodyMedium.copy(color = Color.White),
                modifier = Modifier.fillMaxWidth(),
                decorationBox = { innerTextField ->
                    if (query.isEmpty()) {
                        Text("Search hero...", color = Color.Gray,
                            style = MaterialTheme.typography.bodyMedium)
                    }
                    innerTextField()
                }
            )
        }
    }
}

@Composable
private fun RoleFilterChip(
    label: String,
    isSelected: Boolean,
    accentColor: Color,
    onClick: () -> Unit
) {
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) accentColor.copy(alpha = 0.2f)
        else Color(0xFF16213E),
        animationSpec = tween(200),
        label = "chipBg"
    )

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bgColor)
            .border(
                width = if (isSelected) 1.dp else 0.dp,
                color = if (isSelected) accentColor else Color.Transparent,
                shape = RoundedCornerShape(20.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = label,
                color = if (isSelected) accentColor else Color.Gray,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}
