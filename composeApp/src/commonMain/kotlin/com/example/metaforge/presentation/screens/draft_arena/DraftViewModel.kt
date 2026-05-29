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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class DraftViewModel(
    private val draftRepository: DraftRepository,
    private val heroMetaService: HeroMetaService
) : ViewModel() {

    private val _pickPosition = MutableStateFlow(1)
    private val _preferredLane = MutableStateFlow(HeroLane.GOLD_LANE)
    private var allHeroMeta: List<HeroMetaEntry> = emptyList()
    private var initialized = false
    private var isUserFirstPick = true

    private val _uiState = MutableStateFlow<DraftUiState>(DraftUiState.Loading)
    val uiState: StateFlow<DraftUiState> = _uiState.asStateFlow()

    fun setupDraft(pickPos: Int, isFirst: Boolean, lane: String) {
        if (initialized) return
        // Set initial values from nav args only once; user changes persist after this
        initialized = true
        isUserFirstPick = isFirst
        _pickPosition.value = pickPos
        _preferredLane.value = laneFromString(lane)
        loadAndObserve()
    }

    /** Re-runs the load/observe pipeline; used by the Error state's retry action. */
    fun retry() {
        if (!initialized) return
        _uiState.value = DraftUiState.Loading
        loadAndObserve()
    }

    fun updateLane(lane: HeroLane) { _preferredLane.value = lane }
    fun updatePickPosition(pos: Int) { _pickPosition.value = pos }

    private fun loadAndObserve() {
        viewModelScope.launch {
            try {
                // Apply first-pick BEFORE collecting so the first combine emission is correct
                // (otherwise second-pick users briefly see a Blue-first board).
                draftRepository.setFirstPick(isUserFirstPick)
                allHeroMeta = heroMetaService.getAllHeroes()
                combine(draftRepository.getDraftState(), _pickPosition, _preferredLane) { s, p, l ->
                    Triple(s, p, l)
                }.collect { (state, pos, lane) ->
                    val isUserTurn = state.isBanPhaseComplete && state.isAllyPickWave() && !state.isComplete
                    val banSuggestions = if (!state.isBanPhaseComplete)
                        computeBanSuggestions(state, lane) else emptyList()
                    val pickSuggestions = if (isUserTurn)
                        computePickSuggestions(state, pos, lane) else emptyList()

                    _uiState.value = DraftUiState.Ready(
                        draftState = state,
                        isUserTurn = isUserTurn,
                        banSuggestions = banSuggestions,
                        pickSuggestions = pickSuggestions,
                        turnMessage = state.getTurnMessage(),
                        currentPickPosition = pos,
                        currentLane = lane
                    )
                }
            } catch (e: Exception) {
                _uiState.value = DraftUiState.Error(e.message ?: "Failed to load heroes")
            }
        }
    }

    // Ban suggestions: high-tier heroes in user's preferred lane (by ban rate)
    private fun computeBanSuggestions(state: DraftState, lane: HeroLane): List<HeroSuggestion> {
        if (allHeroMeta.isEmpty()) return emptyList()
        val unavailable = state.getAllPickedAndBannedHeroes().map { it.name }.toSet()

        val candidates = allHeroMeta
            .filter { it.name !in unavailable && it.lanes.contains(lane) }
            .ifEmpty { allHeroMeta.filter { it.name !in unavailable } }

        return candidates.map { hero ->
            val tierScore = tierScore(hero.tier)
            // Simulate ban rate priority: SS > S > A > ...
            val banPriority = when (hero.tier) {
                HeroTier.SS -> 40; HeroTier.S -> 30; HeroTier.A -> 20
                HeroTier.B -> 10; HeroTier.C -> 5; HeroTier.D -> 0
            }
            val laneBonus = if (hero.lanes.contains(lane)) 15 else 0
            val total = tierScore + banPriority + laneBonus
            HeroSuggestion(
                hero = hero, totalScore = total, tierScore = tierScore,
                counterBonus = banPriority, weaknessPenalty = 0, synergyBonus = laneBonus,
                reasons = buildList {
                    add("${hero.tier.label} tier — high ban priority")
                    if (hero.lanes.contains(lane)) add("Fits ${lane.displayName}")
                },
                warnings = emptyList()
            )
        }.sortedByDescending { it.totalScore }.take(5)
    }

    // Pick suggestions: counter/synergy/lane during user's pick wave
    private fun computePickSuggestions(state: DraftState, pickPos: Int, lane: HeroLane): List<HeroSuggestion> {
        if (allHeroMeta.isEmpty()) return emptyList()
        val allPickedNames = (state.allySlots + state.enemySlots).filterNotNull().map { it.name }.toSet()
        val allBannedNames = (state.allyBans + state.enemyBans).filterNotNull().map { it.name }.toSet()
        val unavailable = allPickedNames + allBannedNames
        val allyPickedNames = state.allySlots.filterNotNull().map { it.name }.toSet()
        val enemyPickedNames = state.enemySlots.filterNotNull().map { it.name }.toSet()

        val candidates = allHeroMeta
            .filter { it.name !in unavailable && it.lanes.contains(lane) }
            .ifEmpty { allHeroMeta.filter { it.name !in unavailable } }

        return candidates.map { hero ->
            val tierScore = tierScore(hero.tier)
            val strongAgainstNames = hero.strongAgainst.map { it.name }.toSet()
            val counterBonus = enemyPickedNames.count { it in strongAgainstNames } * 20
            val weakAgainstNames = hero.weakAgainst.map { it.name }.toSet()
            val weaknessPenalty = enemyPickedNames.count { it in weakAgainstNames } * 15
            val synergyNames = hero.synergies.map { it.name }.toSet()
            val synergyBonus = allyPickedNames.count { it in synergyNames } * 10
            val laneBonus = if (hero.lanes.contains(lane)) 10 else 0
            val total = tierScore + counterBonus - weaknessPenalty + synergyBonus + laneBonus

            val reasons = buildList {
                val countered = enemyPickedNames.filter { it in strongAgainstNames }
                if (countered.isNotEmpty()) add("Counters: ${countered.take(2).joinToString(", ")}")
                val synAlly = allyPickedNames.filter { it in synergyNames }
                if (synAlly.isNotEmpty()) add("Synergy with ${synAlly.take(2).joinToString(", ")}")
                if (hero.lanes.contains(lane)) add("Fits ${lane.displayName}")
                if (isEmpty()) add("${hero.tier.label} tier pick")
            }
            val warnings = buildList {
                val alreadyCountered = hero.weakAgainst.filter { it.name in enemyPickedNames }
                if (alreadyCountered.isNotEmpty()) add("Enemy has: ${alreadyCountered.take(2).joinToString(", ") { it.name }}")
                val openCounters = hero.weakAgainst.filter { it.name !in unavailable }.take(2)
                if (openCounters.isNotEmpty()) add("Open counters: ${openCounters.joinToString(", ") { it.name }}")
                val bannedCounters = hero.weakAgainst.filter { it.name in allBannedNames }.take(2)
                if (bannedCounters.isNotEmpty()) add("Counters banned: ${bannedCounters.joinToString(", ") { it.name }}")
            }
            HeroSuggestion(hero, total, tierScore, counterBonus, weaknessPenalty, synergyBonus, reasons, warnings)
        }.sortedByDescending { it.totalScore }.take(5)
    }

    private fun tierScore(tier: HeroTier) = when (tier) {
        HeroTier.SS -> 100; HeroTier.S -> 80; HeroTier.A -> 60
        HeroTier.B -> 40; HeroTier.C -> 20; HeroTier.D -> 10
    }

    private fun laneFromString(lane: String) = when (lane) {
        "EXP_LANE" -> HeroLane.EXP_LANE; "GOLD_LANE" -> HeroLane.GOLD_LANE
        "MID_LANE" -> HeroLane.MID_LANE; "JUNGLE" -> HeroLane.JUNGLE
        "ROAM" -> HeroLane.ROAM; else -> HeroLane.GOLD_LANE
    }

    fun removeHero(slotIndex: Int, isAlly: Boolean, isBan: Boolean) {
        viewModelScope.launch {
            if (isBan) draftRepository.banHero(slotIndex, isAlly, null)
            else draftRepository.pickHero(slotIndex, isAlly, null)
        }
    }

    fun resetDraft() { viewModelScope.launch { draftRepository.clearDraft() } }
}
