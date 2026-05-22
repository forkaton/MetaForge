package com.example.metaforge.presentation.screens.heroselect

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class HeroInfoViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(HeroInfoUiState())
    val uiState: StateFlow<HeroInfoUiState> = _uiState.asStateFlow()
}
