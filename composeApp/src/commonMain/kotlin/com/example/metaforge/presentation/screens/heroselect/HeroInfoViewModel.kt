package com.example.metaforge.presentation.screens.heroselect

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class HeroInfoViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(HeroInfoUiState())
    val uiState: StateFlow<HeroInfoUiState> = _uiState.asStateFlow()

    fun loadHeroInfo(heroId: Int, heroName: String, heroRole: String) {
        // Sprint 3: akan fetch dari Gemini API
        // Untuk sekarang pakai data statis
        _uiState.update {
            it.copy(
                heroId = heroId,
                heroName = heroName,
                heroRole = heroRole
            )
        }
    }
}
