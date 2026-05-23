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
    private var initialized = false

    private val _uiState = MutableStateFlow<DraftUiState>(DraftUiState.Loading)
    val uiState: StateFlow<DraftUiState> = _uiState.asStateFlow()

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
        if (!initialized) {
            initialized = true
            viewModelScope.launch {
                try {
                    draftRepository.initializeDraft(isFirstPick)
                } catch (_: Exception) {}
            }
            loadAndObserve()
        }
    }

    private fun loadAndObserve() {
        viewModelScope.launch {
            try {
                allHeroMeta = heroMetaService.getAllHeroes()
                draftRepository.getDraftState().collect { state ->
                    val isTurn = state.isAllyTurn() && !state.isComplete
                    val suggestions = if (isTurn || state.isComplete) computeSuggestions(state) else emptyList()
                    _uiState.value = DraftUiState.Ready(
                        draftState   = state,
                        isUserTurn   = isTurn,
                        suggestions  = suggestions,
                        turnMessage  = state.getTurnMessage()
                    )
                }
            } catch (e: Exception) {
                _uiState.value = DraftUiState.Error(e.message ?: "Failed to load heroes")
            }
        }
    }

    private fun computeSuggestions(state: DraftState): List<HeroSuggestion> {
        if (allHeroMeta.isEmpty()) return emptyList()

        val allPickedNames   = (state.allySlots + state.enemySlots).filterNotNull().map { it.name }.toSet()
        val allBannedNames   = (state.allyBans + state.enemyBans).filterNotNull().map { it.name }.toSet()
        val unavailableNames = allPickedNames + allBannedNames

        val allyPickedNames  = state.allySlots.filterNotNull().map { it.name }.toSet()
        val enemyPickedNames = state.enemySlots.filterNotNull().map { it.name }.toSet()

        // Filter by preferred lane first; if none match, use all available
        val candidates = allHeroMeta
            .filter { it.name !in unavailableNames && it.lanes.contains(preferredLane) }
            .ifEmpty { allHeroMeta.filter { it.name !in unavailableNames } }

        return candidates.map { hero ->
            val tierScore = tierScore(hero.tier)

            val strongAgainstNames = hero.strongAgainst.map { it.name }.toSet()
            val counterBonus = enemyPickedNames.count { it in strongAgainstNames } * 20

            val weakAgainstNames = hero.weakAgainst.map { it.name }.toSet()
            val weaknessPenalty = enemyPickedNames.count { it in weakAgainstNames } * 15

            val synergyNames = hero.synergies.map { it.name }.toSet()
            val synergyBonus = allyPickedNames.count { it in synergyNames } * 10

            val laneBonus = if (hero.lanes.contains(preferredLane)) 10 else 0
            val total = tierScore + counterBonus - weaknessPenalty + synergyBonus + laneBonus

            val reasons = buildList {
                val countered = enemyPickedNames.filter { it in strongAgainstNames }
                if (countered.isNotEmpty()) add("Counters: ${countered.take(2).joinToString(", ")}")
                val synAlly = allyPickedNames.filter { it in synergyNames }
                if (synAlly.isNotEmpty()) add("Synergy with ${synAlly.take(2).joinToString(", ")}")
                if (hero.lanes.contains(preferredLane)) add("Fits ${preferredLane.displayName}")
                if (isEmpty()) add("${hero.tier.label} tier pick for ${preferredLane.displayName}")
            }

            val warnings = buildList {
                val alreadyPicked = hero.weakAgainst
                    .filter { it.name in enemyPickedNames }
                if (alreadyPicked.isNotEmpty()) add("Enemy picked: ${alreadyPicked.take(2).joinToString(", ") { it.name }}")

                val pickableCounters = hero.weakAgainst
                    .filter { it.name !in unavailableNames }
                    .take(2)
                if (pickableCounters.isNotEmpty()) add("Open counters: ${pickableCounters.joinToString(", ") { it.name }}")

                val bannedCounters = hero.weakAgainst
                    .filter { it.name in allBannedNames }
                    .take(2)
                if (bannedCounters.isNotEmpty()) add("Counters banned: ${bannedCounters.joinToString(", ") { it.name }}")
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

    fun resetDraft() {
        viewModelScope.launch { draftRepository.clearDraft() }
    }
}
