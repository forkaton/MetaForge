package com.example.metaforge.presentation.screens.home

import androidx.lifecycle.ViewModel
import com.example.metaforge.data.local.HeroDataSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class HomeViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadStats()
    }

    private fun loadStats() {
        try {
            val heroCount = HeroDataSource.allHeroes.size
            val roleCount = HeroDataSource.allHeroes.map { it.role }.distinct().size
            _uiState.value = HomeUiState.Ready(
                heroCount = heroCount,
                roleCount = roleCount
            )
        } catch (e: Exception) {
            _uiState.value = HomeUiState.Error(e.message ?: "Failed to load stats")
        }
    }
}
