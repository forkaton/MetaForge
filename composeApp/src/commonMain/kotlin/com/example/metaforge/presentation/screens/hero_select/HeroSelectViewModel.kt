package com.example.metaforge.presentation.screens.hero_select

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.metaforge.data.local.HeroMetaService
import com.example.metaforge.data.local.toHero
import com.example.metaforge.domain.model.Hero
import com.example.metaforge.domain.model.HeroLane
import com.example.metaforge.domain.repository.DraftRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HeroSelectViewModel(
    private val draftRepository: DraftRepository,
    private val heroMetaService: HeroMetaService
) : ViewModel() {

    private val _uiState = MutableStateFlow<HeroSelectUiState>(HeroSelectUiState.Loading)
    val uiState: StateFlow<HeroSelectUiState> = _uiState.asStateFlow()

    private var allHeroes: List<Hero> = emptyList()

    init {
        loadHeroes()
    }

    private fun loadHeroes() {
        viewModelScope.launch {
            try {
                allHeroes = heroMetaService.getAllHeroes().map { it.toHero() }
                draftRepository.getDraftState().collect { draftState ->
                    val pickedNames = (draftState.allySlots + draftState.enemySlots)
                        .filterNotNull().map { it.name }.toHashSet()
                    val allyBanned  = draftState.allyBans.filterNotNull().map { it.name }.toHashSet()
                    val enemyBanned = draftState.enemyBans.filterNotNull().map { it.name }.toHashSet()
                    val allBanned   = allyBanned + enemyBanned
                    val current = _uiState.value
                    val filtered = if (current is HeroSelectUiState.Ready) {
                        applyFilters(allHeroes, current.selectedLane, current.searchQuery)
                    } else allHeroes
                    _uiState.value = HeroSelectUiState.Ready(
                        allHeroes = allHeroes,
                        filteredHeroes = filtered,
                        pickedHeroNames = pickedNames,
                        bannedHeroNames = allBanned,
                        allyBannedHeroNames = allyBanned,
                        enemyBannedHeroNames = enemyBanned
                    )
                }
            } catch (e: Exception) {
                _uiState.value = HeroSelectUiState.Error(e.message ?: "Failed to load heroes")
            }
        }
    }

    fun filterByLane(lane: HeroLane?) {
        val current = _uiState.value as? HeroSelectUiState.Ready ?: return
        _uiState.value = current.copy(
            filteredHeroes = applyFilters(allHeroes, lane, current.searchQuery),
            selectedLane = lane,
            searchQuery = ""
        )
    }

    fun onSearchQueryChange(query: String) {
        val current = _uiState.value as? HeroSelectUiState.Ready ?: return
        _uiState.value = current.copy(
            filteredHeroes = applyFilters(allHeroes, current.selectedLane, query),
            searchQuery = query
        )
    }

    private fun applyFilters(heroes: List<Hero>, lane: HeroLane?, query: String): List<Hero> {
        var result = if (lane == null) heroes else heroes.filter { it.lane == lane }
        if (query.isNotEmpty()) result = result.filter { it.name.contains(query, ignoreCase = true) }
        return result
    }

    fun pickHero(slotIndex: Int, isAlly: Boolean, isBan: Boolean, hero: Hero) {
        viewModelScope.launch {
            if (isBan) draftRepository.banHero(slotIndex, isAlly, hero)
            else draftRepository.pickHero(slotIndex, isAlly, hero)
        }
    }
}
