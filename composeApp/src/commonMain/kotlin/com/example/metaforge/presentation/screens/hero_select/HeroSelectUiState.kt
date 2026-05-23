package com.example.metaforge.presentation.screens.hero_select

import com.example.metaforge.domain.model.Hero
import com.example.metaforge.domain.model.HeroLane

sealed interface HeroSelectUiState {
    data object Loading : HeroSelectUiState
    data class Error(val message: String) : HeroSelectUiState
    data class Ready(
        val allHeroes: List<Hero>,
        val filteredHeroes: List<Hero>,
        val pickedHeroNames: Set<String>,        // ally + enemy picks only
        val bannedHeroNames: Set<String>,        // ally + enemy bans combined
        val allyBannedHeroNames: Set<String>,    // only ally team bans
        val enemyBannedHeroNames: Set<String>,   // only enemy team bans
        val selectedLane: HeroLane? = null,
        val searchQuery: String = ""
    ) : HeroSelectUiState
}
