package com.example.metaforge.presentation.screens.counterpick

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel

/**
 * Counter Pick Screen - Hero Encyclopedia dengan meta tier list dan counter guide
 * 
 * Menampilkan:
 * - Daftar semua heroes
 * - Info lane/role
 * - Meta tier
 * - Counter matchups
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CounterPickScreen(
    onNavigateBack: () -> Unit,
    viewModel: CounterPickViewModel = koinViewModel()
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "HERO ENCYCLOPEDIA",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
         when (uiState.value) {
             is CounterPickUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is CounterPickUiState.Idle -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Select a hero to analyze counters", color = Color.LightGray)
                }
            }
            is CounterPickUiState.Analyzing -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(Modifier.height(16.dp))
                        Text("Analyzing counters...", color = Color.LightGray)
                    }
                }
            }
            is CounterPickUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("⚠️ Error", color = Color.Red, fontWeight = FontWeight.Bold)
                        Text(
                            (uiState.value as CounterPickUiState.Error).message,
                            color = Color.LightGray
                        )
                    }
                }
            }
            is CounterPickUiState.Success -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        Text(
                            "Meta Tier List",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    items(listOf("S+ Tier", "S Tier", "A Tier", "B Tier")) { tier ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFF2A3F5F)
                            )
                        ) {
                            Text(
                                tier,
                                modifier = Modifier.padding(12.dp),
                                fontWeight = FontWeight.Bold,
                                color = when (tier) {
                                    "S+ Tier" -> Color.Red
                                    "S Tier" -> Color(0xFFFFB300)
                                    "A Tier" -> Color(0xFF00E676)
                                    else -> Color.LightGray
                                }
                            )
                        }
                    }
                    
                    item {
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "All Heroes",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    item {
                        Text(
                            "Loading heroes from API...",
                            color = Color.LightGray,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }
        }
    }
}

