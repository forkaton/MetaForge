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

    private val _pickPositions = MutableStateFlow<Set<Int>>(setOf(1))
    private val _preferredLanes = MutableStateFlow<Set<HeroLane>>(setOf(HeroLane.GOLD_LANE))
    private val _partySize = MutableStateFlow(1)
    private var allHeroMeta: List<HeroMetaEntry> = emptyList()
    private var initialized = false
    private var isUserFirstPick = true
    private var banCountPerSide = 5

    private val _uiState = MutableStateFlow<DraftUiState>(DraftUiState.Loading)
    val uiState: StateFlow<DraftUiState> = _uiState.asStateFlow()

    /**
     * Initialise the draft once from navigation args.
     *
     * @param picks user-owned pick positions (1..5). Empty for Squad — recommendations
     *   then apply to every ally slot.
     * @param lanes preferred lanes, sized to [partySize]. Empty for Squad.
     * @param banCount per-side ban quota (3/4/5) derived from target tier.
     */
    fun setupDraft(
        partySize: Int,
        picks: Set<Int>,
        isFirst: Boolean,
        lanes: Set<HeroLane>,
        banCount: Int
    ) {
        if (initialized) return
        initialized = true
        isUserFirstPick = isFirst
        banCountPerSide = banCount.coerceIn(3, 5)
        _partySize.value = partySize.coerceIn(1, 5)
        _pickPositions.value = picks
        _preferredLanes.value = lanes
        loadAndObserve()
    }

    /** Re-runs the load/observe pipeline; used by the Error state's retry action. */
    fun retry() {
        if (!initialized) return
        _uiState.value = DraftUiState.Loading
        loadAndObserve()
    }

    /** Mid-draft updaters (Duo/Trio): caps multi-select at partySize. */
    fun updateLanes(lanes: Set<HeroLane>) {
        _preferredLanes.value = lanes.take(_partySize.value).toSet()
    }

    fun updatePickPositions(positions: Set<Int>) {
        _pickPositions.value = positions.take(_partySize.value).toSet()
    }

    private fun loadAndObserve() {
        viewModelScope.launch {
            try {
                // Apply first-pick + banCount BEFORE collecting so the first emission
                // reflects the chosen tier instead of the default Mythic shape.
                draftRepository.setFirstPick(isUserFirstPick)
                draftRepository.setBanCountPerSide(banCountPerSide)
                allHeroMeta = heroMetaService.getAllHeroes()
                combine(
                    draftRepository.getDraftState(),
                    _pickPositions,
                    _preferredLanes,
                    _partySize
                ) { s, p, l, ps -> Quad(s, p, l, ps) }.collect { (state, picks, lanes, partySize) ->
                    val isSquad = partySize >= 5
                    val userOwnedSlotIndices = if (isSquad) (0..4).toSet()
                                               else picks.map { it - 1 }.toSet()
                    val activeAllyUserSlots = state.activePickSlots()
                        .filter { (isAlly, idx) -> isAlly && idx in userOwnedSlotIndices }
                    val isUserTurn = state.isBanPhaseComplete &&
                            !state.isComplete && activeAllyUserSlots.isNotEmpty()

                    val banSuggestions = if (!state.isBanPhaseComplete)
                        computeBanSuggestions(state) else emptyList()
                    val groups = if (isUserTurn)
                        computePickGroups(state, lanes, partySize, activeAllyUserSlots.map { it.second })
                    else emptyList()

                    _uiState.value = DraftUiState.Ready(
                        draftState = state,
                        isUserTurn = isUserTurn,
                        banSuggestions = banSuggestions,
                        pickSuggestionGroups = groups,
                        turnMessage = state.getTurnMessage(),
                        currentPickPositions = picks,
                        currentLanes = lanes,
                        partySize = partySize
                    )
                }
            } catch (e: Exception) {
                _uiState.value = DraftUiState.Error(e.message ?: "Failed to load heroes")
            }
        }
    }

    // ─── Ban suggestions ─────────────────────────────────────────────────────

    /**
     * Always lane-agnostic. Real ranked drafts ban the strongest meta threats
     * regardless of which lane the user plays — bans are a team-wide decision,
     * not a per-role preference.
     */
    private fun computeBanSuggestions(state: DraftState): List<HeroSuggestion> {
        if (allHeroMeta.isEmpty()) return emptyList()
        val unavailable = state.getAllPickedAndBannedHeroes().map { it.name }.toSet()

        val candidates = allHeroMeta.filter { it.name !in unavailable }

        return candidates.map { hero ->
            val tierScore = tierScore(hero.tier)
            val banPriority = when (hero.tier) {
                HeroTier.SS -> 40; HeroTier.S -> 30; HeroTier.A -> 20
                HeroTier.B -> 10; HeroTier.C -> 5; HeroTier.D -> 0
            }
            val total = tierScore + banPriority
            HeroSuggestion(
                hero = hero, totalScore = total, tierScore = tierScore,
                counterBonus = banPriority, weaknessPenalty = 0, synergyBonus = 0,
                reasons = listOf("${hero.tier.label} tier — high ban priority"),
                warnings = emptyList()
            )
        }.sortedByDescending { it.totalScore }.take(5)
    }

    // ─── Pick suggestion groups ──────────────────────────────────────────────

    /**
     * Solo/Duo/Trio: one group per preferred lane that ally hasn't covered yet.
     *   Once an ally hero is locked into a lane, that lane's group disappears
     *   so we never re-suggest a role the team already filled.
     *
     * Squad: meta-first. Every active ally slot gets a group whose candidates
     *   exclude any hero whose lanes are fully covered by:
     *     - lanes already locked in by [DraftState.allySlots], plus
     *     - the lane intended by previous slots in the same wave (so two
     *       simultaneously-active slots don't both top-pick Jungle).
     */
    private fun computePickGroups(
        state: DraftState,
        preferredLanes: Set<HeroLane>,
        partySize: Int,
        activeAllyIndices: List<Int>
    ): List<PickSuggestionGroup> {
        if (allHeroMeta.isEmpty()) return emptyList()
        val isSquad = partySize >= 5
        val coveredLanes = state.allySlots.filterNotNull()
            .flatMap { listOfNotNull(it.lane) }
            .toSet()

        return if (isSquad) {
            val excluded = coveredLanes.toMutableSet()
            activeAllyIndices.map { slotIdx ->
                val suggestions = computeLanePickSuggestions(state, lane = null, excludedLanes = excluded)
                suggestions.firstOrNull()?.hero?.lanes
                    ?.firstOrNull { it !in excluded }
                    ?.let { excluded.add(it) }
                PickSuggestionGroup(
                    label = "Slot ${slotIdx + 1}",
                    lane = null,
                    suggestions = suggestions
                )
            }
        } else {
            preferredLanes
                .filter { it !in coveredLanes }
                .map { lane ->
                    PickSuggestionGroup(
                        label = lane.displayName,
                        lane = lane,
                        suggestions = computeLanePickSuggestions(state, lane)
                    )
                }
        }
    }

    private fun computeLanePickSuggestions(
        state: DraftState,
        lane: HeroLane?,
        excludedLanes: Set<HeroLane> = emptySet()
    ): List<HeroSuggestion> {
        val unavailable = (state.allySlots + state.enemySlots +
                state.allyBans.take(state.banCountPerSide) +
                state.enemyBans.take(state.banCountPerSide))
            .filterNotNull().map { it.name }.toSet()
        val allyPickedNames = state.allySlots.filterNotNull().map { it.name }.toSet()
        val enemyPickedNames = state.enemySlots.filterNotNull().map { it.name }.toSet()
        val allBannedNames = (state.allyBans + state.enemyBans).filterNotNull()
            .map { it.name }.toSet()

        val candidates = allHeroMeta
            .filter { it.name !in unavailable }
            .filter { lane == null || it.lanes.contains(lane) }
            .filter { hero ->
                // For Squad: drop heroes whose every playable lane is already covered.
                excludedLanes.isEmpty() || hero.lanes.any { it !in excludedLanes }
            }
            .ifEmpty { allHeroMeta.filter { it.name !in unavailable } }

        return candidates.map { hero ->
            val tierScore = tierScore(hero.tier)
            val strongAgainstNames = hero.strongAgainst.map { it.name }.toSet()
            val counterBonus = enemyPickedNames.count { it in strongAgainstNames } * 20
            val weakAgainstNames = hero.weakAgainst.map { it.name }.toSet()
            val weaknessPenalty = enemyPickedNames.count { it in weakAgainstNames } * 15
            val synergyNames = hero.synergies.map { it.name }.toSet()
            val synergyBonus = allyPickedNames.count { it in synergyNames } * 10
            val laneBonus = if (lane != null && hero.lanes.contains(lane)) 10 else 0
            val total = tierScore + counterBonus - weaknessPenalty + synergyBonus + laneBonus

            val reasons = buildList {
                val countered = enemyPickedNames.filter { it in strongAgainstNames }
                if (countered.isNotEmpty()) add("Counters: ${countered.take(2).joinToString(", ")}")
                val synAlly = allyPickedNames.filter { it in synergyNames }
                if (synAlly.isNotEmpty()) add("Synergy with ${synAlly.take(2).joinToString(", ")}")
                if (lane != null && hero.lanes.contains(lane)) add("Fits ${lane.displayName}")
                if (isEmpty()) add("${hero.tier.label} tier pick")
            }
            val warnings = buildList {
                val alreadyCountered = hero.weakAgainst.filter { it.name in enemyPickedNames }
                if (alreadyCountered.isNotEmpty())
                    add("Enemy has: ${alreadyCountered.take(2).joinToString(", ") { it.name }}")
                val openCounters = hero.weakAgainst.filter { it.name !in unavailable }.take(2)
                if (openCounters.isNotEmpty())
                    add("Open counters: ${openCounters.joinToString(", ") { it.name }}")
                val bannedCounters = hero.weakAgainst.filter { it.name in allBannedNames }.take(2)
                if (bannedCounters.isNotEmpty())
                    add("Counters banned: ${bannedCounters.joinToString(", ") { it.name }}")
            }
            HeroSuggestion(hero, total, tierScore, counterBonus, weaknessPenalty, synergyBonus, reasons, warnings)
        }.sortedByDescending { it.totalScore }.take(5)
    }

    private fun tierScore(tier: HeroTier) = when (tier) {
        HeroTier.SS -> 100; HeroTier.S -> 80; HeroTier.A -> 60
        HeroTier.B -> 40; HeroTier.C -> 20; HeroTier.D -> 10
    }

    fun removeHero(slotIndex: Int, isAlly: Boolean, isBan: Boolean) {
        viewModelScope.launch {
            if (isBan) draftRepository.banHero(slotIndex, isAlly, null)
            else draftRepository.pickHero(slotIndex, isAlly, null)
        }
    }

    fun resetDraft() { viewModelScope.launch { draftRepository.clearDraft() } }

    /** Local 4-tuple for combine() — kotlin.Triple only gets us to 3. */
    private data class Quad<A, B, C, D>(val a: A, val b: B, val c: C, val d: D)
}
