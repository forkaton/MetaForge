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

/**
 * A labelled bucket of pick suggestions. One group per preferred lane in
 * Solo/Duo/Trio mode, one per missing-lane ally slot in Squad mode.
 */
data class PickSuggestionGroup(
    val label: String,
    val lane: HeroLane?,
    val suggestions: List<HeroSuggestion>,
    /** Ally slot this group's recommendations fill when clicked. Null = no active target. */
    val targetSlotIndex: Int? = null
)

sealed interface DraftUiState {
    data object Loading : DraftUiState
    data class Ready(
        val draftState: DraftState,
        val isUserTurn: Boolean = false,
        val banSuggestions: List<HeroSuggestion> = emptyList(),
        val pickSuggestionGroups: List<PickSuggestionGroup> = emptyList(),
        val turnMessage: String = "",
        val currentPickPositions: Set<Int> = emptySet(),
        val currentLanes: Set<HeroLane> = emptySet(),
        val partySize: Int = 1
    ) : DraftUiState {
        val isSquad: Boolean get() = partySize >= 5
    }
    data class Error(val message: String) : DraftUiState
}
