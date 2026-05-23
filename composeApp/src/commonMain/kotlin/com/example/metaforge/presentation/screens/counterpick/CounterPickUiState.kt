package com.example.metaforge.presentation.screens.counterpick

import com.example.metaforge.domain.model.Hero
import com.example.metaforge.domain.model.HeroLane
import com.example.metaforge.domain.model.HeroRecommendation

sealed interface CounterPickUiState {
    data object Loading : CounterPickUiState
    data class Idle(
        val heroes: List<Hero> = emptyList(),
        val filteredHeroes: List<Hero> = emptyList(),
        val selectedLane: HeroLane? = null,
        val searchQuery: String = ""
    ) : CounterPickUiState
    data object Analyzing : CounterPickUiState
    data class Success(
        val targetHero: Hero,
        val counters: List<HeroRecommendation>
    ) : CounterPickUiState
    data class Error(val message: String) : CounterPickUiState
}
