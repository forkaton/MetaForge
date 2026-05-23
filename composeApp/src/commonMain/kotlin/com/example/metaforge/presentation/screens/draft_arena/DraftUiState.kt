package com.example.metaforge.presentation.screens.draft_arena

import com.example.metaforge.domain.model.DraftState
import com.example.metaforge.domain.model.HeroLane
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
        val banSuggestions: List<HeroSuggestion> = emptyList(),
        val pickSuggestions: List<HeroSuggestion> = emptyList(),
        val turnMessage: String = "",
        val currentPickPosition: Int = 1,
        val currentLane: HeroLane = HeroLane.GOLD_LANE
    ) : DraftUiState
    data class Error(val message: String) : DraftUiState
}
