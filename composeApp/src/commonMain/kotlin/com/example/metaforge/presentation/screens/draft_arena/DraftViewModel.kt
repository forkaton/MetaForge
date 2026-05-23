package com.example.metaforge.presentation.screens.draft_arena

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.metaforge.data.local.HeroMetaService
import com.example.metaforge.domain.model.DraftState
import com.example.metaforge.domain.model.HeroLane
import com.example.metaforge.domain.model.HeroMetaEntry
import com.example.metaforge.domain.model.HeroTier
import com.example.metaforge.domain.repository.DraftRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DraftViewModel(
    private val draftRepository: DraftRepository,
    private val heroMetaService: HeroMetaService
) : ViewModel() {

    private var pickPosition = 1
    private var isFirstPick = true
    private var preferredLane: HeroLane = HeroLane.GOLD_LANE
    private var allHeroMeta: List<HeroMetaEntry> = emptyList()

    private val _uiState = MutableStateFlow<DraftUiState>(DraftUiState.Loading)
    val uiState: StateFlow<DraftUiState> = _uiState.asStateFlow()

    init { loadAndObserve() }

    fun setupDraft(pickPos: Int, isFirst: Boolean, lane: String) {
        pickPosition = pickPos
        isFirstPick = isFirst
        preferredLane = when (lane) {
            "EXP_LANE"  -> HeroLane.EXP_LANE
            "GOLD_LANE" -> HeroLane.GOLD_LANE
            "MID_LANE"  -> HeroLane.MID_LANE
            "JUNGLE"    -> HeroLane.JUNGLE
            "ROAM"      -> HeroLane.ROAM
            else        -> HeroLane.GOLD_LANE
        }
    }

    private fun loadAndObserve() {
        viewModelScope.launch {
            try {
                allHeroMeta = heroMetaService.getAllHeroes()
                draftRepository.getDraftState().collect { state ->
                    val isTurn = isUserTurn(state)
                    val suggestions = if (isTurn) computeSuggestions(state) else emptyList()
                    _uiState.value = DraftUiState.Ready(
                        draftState = state,
                        isUserTurn = isTurn,
                        suggestions = suggestions,
                        turnMessage = state.getTurnMessage(isFirstPick)
                    )
                }
            } catch (e: Exception) {
                _uiState.value = DraftUiState.Error(e.message ?: "Failed to load heroes")
            }
        }
    }

    private fun isUserTurn(state: DraftState): Boolean {
        val idx = pickPosition - 1
        return state.isPickUnlocked(idx, true, isFirstPick) &&
               state.allySlots.getOrNull(idx) == null
    }

    private fun computeSuggestions(state: DraftState): List<HeroSuggestion> {
        if (allHeroMeta.isEmpty()) return emptyList()

        val allPickedNames = (state.allySlots + state.enemySlots).filterNotNull().map { it.name }.toSet()
        val allBannedNames = (state.allyBans + state.enemyBans).filterNotNull().map { it.name }.toSet()
        val unavailableNames = allPickedNames + allBannedNames

        val allyPickedNames  = state.allySlots.filterNotNull().map { it.name }.toSet()
        val enemyPickedNames = state.enemySlots.filterNotNull().map { it.name }.toSet()

        val candidates = allHeroMeta.filter {
            it.name !in unavailableNames && it.lanes.contains(preferredLane)
        }.ifEmpty {
            // Fallback: any hero in preferred lane regardless of availability filter
            allHeroMeta.filter { it.name !in unavailableNames && it.lanes.isNotEmpty() }
        }

        return candidates.map { hero ->
            val tierScore = tierScore(hero.tier)

            val strongAgainstNames = hero.strongAgainst.map { it.name }.toSet()
            val counterBonus = enemyPickedNames.count { it in strongAgainstNames } * 20

            val weakAgainstNames = hero.weakAgainst.map { it.name }.toSet()
            val weaknessPenalty = enemyPickedNames.count { it in weakAgainstNames } * 15

            val synergyNames = hero.synergies.map { it.name }.toSet()
            val synergyBonus = allyPickedNames.count { it in synergyNames } * 10

            val total = tierScore + counterBonus - weaknessPenalty + synergyBonus

            val reasons = buildList {
                val countered = enemyPickedNames.filter { it in strongAgainstNames }
                if (countered.isNotEmpty()) add("Counters: ${countered.take(2).joinToString(", ")}")
                val synAlly = allyPickedNames.filter { it in synergyNames }
                if (synAlly.isNotEmpty()) add("Synergy with ${synAlly.take(2).joinToString(", ")}")
                if (isEmpty()) add("${hero.tier.label} tier – best pick for ${preferredLane.displayName}")
            }

            val warnings = buildList {
                val threats = enemyPickedNames.filter { it in weakAgainstNames }
                if (threats.isNotEmpty()) add("Enemy has: ${threats.take(2).joinToString(", ")}")
                val openCounters = hero.weakAgainst
                    .filter { it.name !in unavailableNames && it.name !in allyPickedNames }
                    .take(2)
                if (openCounters.isNotEmpty()) add("Open counters: ${openCounters.joinToString(", ") { it.name }}")
            }

            HeroSuggestion(hero, total, tierScore, counterBonus, weaknessPenalty, synergyBonus, reasons, warnings)
        }.sortedByDescending { it.totalScore }.take(5)
    }

    private fun tierScore(tier: HeroTier) = when (tier) {
        HeroTier.SS -> 100; HeroTier.S -> 80; HeroTier.A -> 60
        HeroTier.B  -> 40;  HeroTier.C -> 20; HeroTier.D -> 10
    }

    fun removeHero(slotIndex: Int, isAlly: Boolean, isBan: Boolean) {
        viewModelScope.launch {
            if (isBan) draftRepository.banHero(slotIndex, isAlly, null)
            else draftRepository.pickHero(slotIndex, isAlly, null)
        }
    }
}
