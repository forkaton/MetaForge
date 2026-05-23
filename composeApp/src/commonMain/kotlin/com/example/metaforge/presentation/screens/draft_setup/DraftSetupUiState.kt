package com.example.metaforge.presentation.screens.draft_setup

import com.example.metaforge.domain.model.HeroLane

/**
 * UI State untuk Draft Setup Screen
 * 
 * Menyimpan user's configuration choices untuk draft
 */
data class DraftSetupUiState(
    val rank: String = "Mythic",
    val partySize: Int = 1,
    val isFirstPick: Boolean = true,
    val pickPosition: Int = 1,
    val preferredLane: HeroLane = HeroLane.GOLD_LANE
)