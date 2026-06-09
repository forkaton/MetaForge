package com.example.metaforge.presentation.screens.hero_encyclopedia

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.metaforge.data.local.HeroMetaRepository
import com.example.metaforge.domain.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed interface TierListUiState {
    data object Loading : TierListUiState
    data class Ready(
        val heroes: List<HeroMetaEntry>,
        val filteredHeroes: List<HeroMetaEntry>,
        val selectedLane: HeroLane? = null,
        val selectedRole: String? = null,
        /** Hero rank data map for per-hero stat lookups in detail screens. */
        val rankDataMap: Map<Int, HeroRankData> = emptyMap()
    ) : TierListUiState
    data class Error(val message: String) : TierListUiState
}

class TierListViewModel(
    private val jsonLoader: suspend () -> String,
    private val rankDataLoader: suspend () -> Map<Int, HeroRankData> = { emptyMap() }
) : ViewModel() {

    private val _uiState = MutableStateFlow<TierListUiState>(TierListUiState.Loading)
    val uiState: StateFlow<TierListUiState> = _uiState.asStateFlow()

    init { loadHeroes() }

    private fun loadHeroes() {
        viewModelScope.launch {
            _uiState.value = TierListUiState.Loading
            try {
                val jsonString = withContext(Dispatchers.Default) { jsonLoader() }
                val rankData = try {
                    withContext(Dispatchers.Default) { rankDataLoader() }
                } catch (_: Exception) { emptyMap() }
                val heroes = withContext(Dispatchers.Default) {
                    HeroMetaRepository.parseAndBuild(jsonString, rankData)
                }
                _uiState.value = TierListUiState.Ready(
                    heroes = heroes,
                    filteredHeroes = heroes,
                    rankDataMap = rankData
                )
            } catch (e: Exception) {
                _uiState.value = TierListUiState.Error(e.message ?: "Failed to load hero data")
            }
        }
    }

    fun filterByLane(lane: HeroLane?) {
        val state = _uiState.value as? TierListUiState.Ready ?: return
        _uiState.update {
            state.copy(
                selectedLane = lane,
                filteredHeroes = if (lane == null) state.heroes
                else state.heroes.filter { it.lanes.contains(lane) }
            )
        }
    }

    fun filterByRole(role: String?) {
        val state = _uiState.value as? TierListUiState.Ready ?: return
        _uiState.update {
            state.copy(
                selectedRole = role,
                filteredHeroes = if (role == null) state.heroes
                else state.heroes.filter { it.heroClass.contains(role, ignoreCase = true) }
            )
        }
    }
}
