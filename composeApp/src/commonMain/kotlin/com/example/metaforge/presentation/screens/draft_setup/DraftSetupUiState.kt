package com.example.metaforge.presentation.screens.draft_setup

import com.example.metaforge.domain.model.HeroLane

data class DraftSetupUiState(
    val rank: String = "Mythic",
    val partySize: Int = 1,
    val isFirstPick: Boolean = true,
    // Default empty — user must consciously pick to avoid landing on a draft
    // they didn't intend. START stays disabled until both sets equal partySize
    // (Squad is exempt — sets stay empty).
    val pickPositions: Set<Int> = emptySet(),
    val preferredLanes: Set<HeroLane> = emptySet()
) {
    val banCountPerSide: Int = when (rank) {
        "Epic" -> 3
        "Legend" -> 4
        else -> 5
    }
    val isSquad: Boolean = partySize >= 5
}
