package com.example.metaforge.presentation.screens.counterpick

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CounterPickScreen(onNavigateBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("COUNTER PICK", fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp, color = Color.White)
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack,
                            "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0D0D1A)
                )
            )
        },
        containerColor = Color(0xFF0D0D1A)
    ) { padding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("⚔️", fontSize = 48.sp)
                Spacer(Modifier.height(16.dp))
                Text("Counter Pick Scanner",
                    color = Color.White, fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(8.dp))
                Text("AI Integration coming in Sprint 3",
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(24.dp))
                OutlinedButton(onClick = onNavigateBack) {
                    Text("Back to Draft")
                }
            }
        }
    }
}
