package com.example.metaforge.presentation.screens.draft

import com.example.metaforge.domain.model.HeroRole

data class DraftSetupUiState(
    val rank: String = "Mythic",
    val partySize: Int = 1,
    val isFirstPick: Boolean = true,
    val pickPosition: Int = 1,
    val preferredRole: HeroRole = HeroRole.MARKSMAN
)
