package com.example.metaforge.presentation.screens.home

sealed interface HomeUiState {
    data object Ready : HomeUiState
    data class Error(val message: String) : HomeUiState
}

