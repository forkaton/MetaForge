package com.example.metaforge.presentation.screens.home

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Ready(
        val heroCount: Int = 30,
        val roleCount: Int = 6
    ) : HomeUiState
    data class Error(val message: String) : HomeUiState
}