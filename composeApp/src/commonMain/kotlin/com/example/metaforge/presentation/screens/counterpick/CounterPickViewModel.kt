package com.example.metaforge.presentation.screens.counterpick

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.metaforge.domain.model.Hero
import com.example.metaforge.domain.model.HeroLane
import com.example.metaforge.domain.repository.DraftRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CounterPickViewModel(
    private val draftRepository: DraftRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<CounterPickUiState>(CounterPickUiState.Loading)
    val uiState: StateFlow<CounterPickUiState> = _uiState.asStateFlow()

    private var allHeroes: List<Hero> = emptyList()

    init {
        loadHeroes()
    }

    private fun loadHeroes() {
        viewModelScope.launch {
            try {
                draftRepository.getAllHeroes().collect { heroes ->
                    allHeroes = heroes
                    val current = _uiState.value
                    if (current is CounterPickUiState.Idle) {
                        _uiState.value = current.copy(
                            heroes = heroes,
                            filteredHeroes = applyFilters(heroes, current.selectedLane, current.searchQuery)
                        )
                    } else {
                        _uiState.value = CounterPickUiState.Idle(
                            heroes = heroes,
                            filteredHeroes = heroes
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = CounterPickUiState.Error(
                    e.message ?: "Failed to load heroes"
                )
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        val current = _uiState.value as? CounterPickUiState.Idle ?: return
        _uiState.value = current.copy(
            searchQuery = query,
            filteredHeroes = applyFilters(allHeroes, current.selectedLane, query)
        )
    }

    fun filterByLane(lane: HeroLane?) {
        val current = _uiState.value as? CounterPickUiState.Idle ?: return
        _uiState.value = current.copy(
            selectedLane = lane,
            filteredHeroes = applyFilters(allHeroes, lane, current.searchQuery)
        )
    }

    private fun applyFilters(heroes: List<Hero>, lane: HeroLane?, query: String): List<Hero> {
        var result = if (lane == null) heroes else heroes.filter { it.lane == lane }
        if (query.isNotEmpty()) {
            result = result.filter { it.name.contains(query, ignoreCase = true) }
        }
        return result
    }

    fun analyzeCounter(heroId: Int) {
        viewModelScope.launch {
            _uiState.value = CounterPickUiState.Analyzing
            // TODO: Sprint 3 - Gemini API integration
        }
    }

    fun reset() {
        _uiState.value = CounterPickUiState.Idle(
            heroes = allHeroes,
            filteredHeroes = allHeroes
        )
    }
}
