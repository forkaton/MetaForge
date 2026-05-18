package com.example.metaforge.presentation.screens.home

sealed interface HomeUiState {
    data object Ready : HomeUiState
}