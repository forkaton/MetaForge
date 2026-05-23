package com.example.metaforge.presentation.screens.draft_arena

import com.example.metaforge.domain.model.DraftState
import com.example.metaforge.domain.model.HeroMetaEntry

data class HeroSuggestion(
    val hero: HeroMetaEntry,
    val totalScore: Int,
    val tierScore: Int,
    val counterBonus: Int,
    val weaknessPenalty: Int,
    val synergyBonus: Int,
    val reasons: List<String>,
    val warnings: List<String>
)

sealed interface DraftUiState {
    data object Loading : DraftUiState
    data class Ready(
        val draftState: DraftState,
        val isUserTurn: Boolean = false,
        val suggestions: List<HeroSuggestion> = emptyList(),
        val turnMessage: String = ""
    ) : DraftUiState
    data class Error(val message: String) : DraftUiState
}
