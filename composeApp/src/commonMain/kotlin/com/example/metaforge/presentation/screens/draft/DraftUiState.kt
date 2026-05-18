package com.example.metaforge.presentation.screens.draft

import com.example.metaforge.domain.model.DraftState
import com.example.metaforge.domain.model.HeroRecommendation

sealed interface DraftUiState {
    data object Loading : DraftUiState
    data class Ready(
        val draftState: DraftState,
        val isAnalyzing: Boolean = false,
        // Properti baru yang dibutuhkan DraftScreen
        val turnMessage: String = "SET UP YOUR DRAFT",
        val isUserTurn: Boolean = false,
        val recommendation: HeroRecommendation? = null
    ) : DraftUiState
    data class Error(val message: String) : DraftUiState
}