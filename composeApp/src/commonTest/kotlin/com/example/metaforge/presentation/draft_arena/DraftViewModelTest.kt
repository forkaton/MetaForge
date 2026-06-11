package com.example.metaforge.presentation.draft_arena

import app.cash.turbine.test
import com.example.metaforge.data.local.HeroMetaService
import com.example.metaforge.domain.model.DraftState
import com.example.metaforge.domain.model.Hero
import com.example.metaforge.domain.model.HeroLane
import com.example.metaforge.domain.model.HeroRankData
import com.example.metaforge.domain.repository.DraftRepository
import com.example.metaforge.presentation.screens.draft_arena.DraftUiState
import com.example.metaforge.presentation.screens.draft_arena.DraftViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * Unit tests for [DraftViewModel].
 *
 * KMP note: MockK is a JVM-only library and does not work in `commonTest`, so we use
 * hand-written fakes ([FakeDraftRepository]) plus a real [HeroMetaService] fed with crafted JSON.
 * Flow assertions use Turbine; coroutine timing is controlled with an unconfined test dispatcher
 * installed as Dispatchers.Main so that `viewModelScope` runs eagerly and deterministically.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class DraftViewModelTest {

    // ─── Test doubles ────────────────────────────────────────────────────────

    private class FakeDraftRepository : DraftRepository {
        val state = MutableStateFlow(DraftState())

        fun emit(newState: DraftState) { state.value = newState }

        override fun getDraftState(): Flow<DraftState> = state.asStateFlow()
        override fun getAllHeroes(): Flow<List<Hero>> = flowOf(emptyList())
        override suspend fun syncHeroes() {}
        override suspend fun pickHero(slotIndex: Int, isAlly: Boolean, hero: Hero?) {}
        override suspend fun banHero(slotIndex: Int, isAlly: Boolean, hero: Hero?) {}
        override suspend fun clearDraft() { state.value = DraftState(banCountPerSide = state.value.banCountPerSide) }
        override suspend fun setFirstPick(isFirstPick: Boolean) {
            state.update { it.copy(isUserFirstPick = isFirstPick) }
        }
        override suspend fun setBanCountPerSide(count: Int) {
            state.update { it.copy(banCountPerSide = count.coerceIn(3, 5)) }
        }
    }

    private val metaJson = """
        {"data":[
          {"hero_name":"Bruno","mlid":"15","portrait":"bruno.png","laning":["Gold Lane"],"class":"Marksman","speciality":["Damage"],"counters":[],"synergies":[]},
          {"hero_name":"Miya","mlid":"14","portrait":"miya.png","laning":["Gold Lane"],"class":"Marksman","speciality":["Damage"],"counters":[],"synergies":[]},
          {"hero_name":"Layla","mlid":"13","portrait":"layla.png","laning":["Gold Lane"],"class":"Marksman","speciality":["Damage"],"counters":[{"heroid":15,"heroname":"Bruno"}],"synergies":[]},
          {"hero_name":"Fanny","mlid":"11","portrait":"fanny.png","laning":["Jungle"],"class":"Assassin","speciality":["Mobility"],"counters":[],"synergies":[]},
          {"hero_name":"Estes","mlid":"12","portrait":"estes.png","laning":["Roam"],"class":"Support","speciality":["Heal"],"counters":[],"synergies":[]}
        ]}
    """.trimIndent()

    /**
     * Rank data crafted so [computeDynamicTiers] orders the fixture heroes deterministically:
     * Fanny composite-tops the chart → SS, then Bruno, Layla, Miya, Estes descending.
     * Tests that hinge on tier-driven sorting (bans, meta-first picks) depend on this order.
     */
    private val rankData: Map<Int, HeroRankData> = mapOf(
        11 to HeroRankData(11, "Fanny",  winRate = 0.58, pickRate = 0.30, banRate = 0.80),
        15 to HeroRankData(15, "Bruno",  winRate = 0.55, pickRate = 0.20, banRate = 0.40),
        13 to HeroRankData(13, "Layla",  winRate = 0.52, pickRate = 0.15, banRate = 0.20),
        14 to HeroRankData(14, "Miya",   winRate = 0.50, pickRate = 0.10, banRate = 0.10),
        12 to HeroRankData(12, "Estes",  winRate = 0.48, pickRate = 0.05, banRate = 0.05)
    )

    private fun heroMetaService(loader: suspend () -> String = { metaJson }) =
        HeroMetaService(loader, rankDataLoader = { rankData })

    private val dummy = Hero(id = 999, name = "Dummy", lane = HeroLane.MID_LANE)

    /** Mythic-tier (5-ban) Solo defaults shared by most tests. */
    private fun DraftViewModel.setupSolo(lane: HeroLane = HeroLane.GOLD_LANE, isFirst: Boolean = true) =
        setupDraft(partySize = 1, picks = setOf(1), isFirst = isFirst, lanes = setOf(lane), banCount = 5)

    @BeforeTest
    fun setUp() { Dispatchers.setMain(UnconfinedTestDispatcher()) }

    @AfterTest
    fun tearDown() { Dispatchers.resetMain() }

    // ─── 1. Loading state ────────────────────────────────────────────────────

    @Test
    fun `uiState is Loading before setupDraft is called`() = runTest {
        val viewModel = DraftViewModel(FakeDraftRepository(), heroMetaService())
        assertIs<DraftUiState.Loading>(viewModel.uiState.value)
    }

    // ─── 2. Success state + meta-ranked ban suggestions (Turbine) ────────────

    @Test
    fun `setupDraft during ban phase emits Ready with meta-ranked ban suggestions`() = runTest {
        val viewModel = DraftViewModel(FakeDraftRepository(), heroMetaService())

        viewModel.uiState.test {
            assertIs<DraftUiState.Loading>(awaitItem())

            viewModel.setupSolo()

            val ready = awaitItem()
            assertIs<DraftUiState.Ready>(ready)
            assertTrue(ready.banSuggestions.isNotEmpty(), "ban suggestions should be present in ban phase")
            assertTrue(ready.pickSuggestionGroups.isEmpty(), "no pick suggestions before ban phase completes")
            // Bans are lane-agnostic — the strongest meta hero (Fanny, SS) must lead
            // even though the user's preferred lane is Gold (and Fanny is Jungle).
            assertEquals("Fanny", ready.banSuggestions.first().hero.name,
                "ban list must be tier-sorted regardless of user's preferred lane")
            assertEquals(
                ready.banSuggestions.map { it.totalScore }.sortedDescending(),
                ready.banSuggestions.map { it.totalScore }
            )
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ─── 3. Error state ───────────────────────────────────────────────────────

    @Test
    fun `setupDraft emits Error when hero meta fails to load`() = runTest {
        val failing = heroMetaService { throw RuntimeException("network down") }
        val viewModel = DraftViewModel(FakeDraftRepository(), failing)

        viewModel.setupSolo()

        val state = viewModel.uiState.value
        assertIs<DraftUiState.Error>(state)
        assertEquals("network down", state.message)
    }

    // ─── 4. Snake-pick business logic (1-2-2-2-2-1) + counter suggestion ──────

    @Test
    fun `during ally pick wave the counter hero is suggested and turn is flagged`() = runTest {
        val repo = FakeDraftRepository()
        val viewModel = DraftViewModel(repo, heroMetaService())

        viewModel.setupDraft(
            partySize = 1, picks = setOf(2), isFirst = true,
            lanes = setOf(HeroLane.GOLD_LANE), banCount = 5
        )

        val layla = Hero(id = 13, name = "Layla", lane = HeroLane.GOLD_LANE)
        val wave2State = DraftState(
            isUserFirstPick = true,
            allyBans = List(5) { dummy },
            enemyBans = List(5) { dummy },
            allySlots = listOf(dummy, null, null, null, null),
            enemySlots = listOf(layla, dummy, null, null, null)
        )
        repo.emit(wave2State)

        val ready = viewModel.uiState.value
        assertIs<DraftUiState.Ready>(ready)
        assertTrue(ready.isUserTurn, "wave 2 belongs to the ally team for a first-pick user")
        val goldGroup = ready.pickSuggestionGroups.firstOrNull { it.lane == HeroLane.GOLD_LANE }
        assertTrue(goldGroup != null, "should produce a group for Gold Lane")
        val bruno = goldGroup.suggestions.firstOrNull { it.hero.name == "Bruno" }
        assertTrue(bruno != null, "Bruno should be suggested as a counter to Layla")
        assertTrue(bruno.counterBonus >= 20, "counter bonus should reward beating an enemy pick")
        assertTrue(bruno.reasons.any { it.contains("Counters", ignoreCase = true) })
    }

    // ─── 5. updateLanes reflects in state and pick-group lane filter ─────────

    @Test
    fun `updateLanes swaps the active pick-lane group when ally turn begins`() = runTest {
        val repo = FakeDraftRepository()
        val viewModel = DraftViewModel(repo, heroMetaService())
        viewModel.setupSolo()
        viewModel.updateLanes(setOf(HeroLane.JUNGLE))

        // Drive into a state where the user's slot 0 is the active ally pick.
        val readyDuringBan = viewModel.uiState.value
        assertIs<DraftUiState.Ready>(readyDuringBan)
        assertEquals(setOf(HeroLane.JUNGLE), readyDuringBan.currentLanes,
            "currentLanes must reflect the in-draft lane change")

        repo.emit(DraftState(
            isUserFirstPick = true,
            allyBans = List(5) { dummy },
            enemyBans = List(5) { dummy }
        ))
        val ready = viewModel.uiState.value
        assertIs<DraftUiState.Ready>(ready)
        assertTrue(ready.isUserTurn, "wave 0 of a first-pick Solo draft is the user's turn")
        val group = ready.pickSuggestionGroups.singleOrNull()
        assertTrue(group != null, "a single group should be produced for Solo with one preferred lane")
        assertEquals(HeroLane.JUNGLE, group.lane)
        assertTrue(group.suggestions.all { HeroLane.JUNGLE in it.hero.lanes })
    }

    // ─── 6. Second-pick snake order (0→enemy, 1→ally) + synergy bonus ────────

    @Test
    fun `second-pick wave 1 is ally turn and synergy with ally pick is rewarded`() = runTest {
        val synergyJson = """
            {"data":[
              {"hero_name":"Bruno","mlid":"15","portrait":"bruno.png","laning":["Gold Lane"],"class":"Marksman","speciality":["Damage"],"counters":[],"synergies":[{"heroid":12,"heroname":"Estes"}]},
              {"hero_name":"Miya","mlid":"14","portrait":"miya.png","laning":["Gold Lane"],"class":"Marksman","speciality":["Damage"],"counters":[],"synergies":[]},
              {"hero_name":"Layla","mlid":"13","portrait":"layla.png","laning":["Gold Lane"],"class":"Marksman","speciality":["Damage"],"counters":[],"synergies":[]},
              {"hero_name":"Fanny","mlid":"11","portrait":"fanny.png","laning":["Jungle"],"class":"Assassin","speciality":["Mobility"],"counters":[],"synergies":[]},
              {"hero_name":"Estes","mlid":"12","portrait":"estes.png","laning":["Roam"],"class":"Support","speciality":["Heal"],"counters":[],"synergies":[]}
            ]}
        """.trimIndent()

        val repo = FakeDraftRepository()
        val viewModel = DraftViewModel(repo, heroMetaService { synergyJson })

        viewModel.setupDraft(
            partySize = 1, picks = setOf(2), isFirst = false,
            lanes = setOf(HeroLane.GOLD_LANE), banCount = 5
        )

        val estes = Hero(id = 12, name = "Estes", lane = HeroLane.ROAM)
        val wave1State = DraftState(
            isUserFirstPick = false,
            allyBans = List(5) { dummy },
            enemyBans = List(5) { dummy },
            allySlots = listOf(estes, null, null, null, null),
            enemySlots = listOf(dummy, null, null, null, null)
        )
        repo.emit(wave1State)

        val ready = viewModel.uiState.value
        assertIs<DraftUiState.Ready>(ready)
        assertTrue(ready.isUserTurn, "wave 1 of a second-pick draft must belong to ally team")
        val goldGroup = ready.pickSuggestionGroups.firstOrNull { it.lane == HeroLane.GOLD_LANE }
        assertTrue(goldGroup != null, "Gold Lane group should be produced")
        val bruno = goldGroup.suggestions.firstOrNull { it.hero.name == "Bruno" }
        assertTrue(bruno != null, "Bruno should be suggested in the ally pick wave")
        assertTrue(bruno.synergyBonus >= 10, "synergy bonus must reward picking alongside Estes")
        assertTrue(bruno.reasons.any { it.contains("Synergy", ignoreCase = true) })
    }

    // ─── 7. Epic-tier (3-ban) shortens the ban phase ─────────────────────────

    @Test
    fun `Epic-tier 3 bans complete after 3 per side and pick phase begins`() = runTest {
        val repo = FakeDraftRepository()
        val viewModel = DraftViewModel(repo, heroMetaService())

        viewModel.setupDraft(
            partySize = 1, picks = setOf(1), isFirst = true,
            lanes = setOf(HeroLane.GOLD_LANE), banCount = 3
        )

        val epicBans = DraftState(
            banCountPerSide = 3,
            isUserFirstPick = true,
            // 3 bans on each side, slots 3 & 4 still null — those are inactive at Epic tier.
            allyBans = listOf(dummy, dummy, dummy, null, null),
            enemyBans = listOf(dummy, dummy, dummy, null, null)
        )
        repo.emit(epicBans)

        val ready = viewModel.uiState.value
        assertIs<DraftUiState.Ready>(ready)
        assertTrue(ready.draftState.isBanPhaseComplete,
            "3 bans per side should be sufficient at Epic tier (banCountPerSide=3)")
        assertTrue(ready.banSuggestions.isEmpty(),
            "ban suggestions must clear once the (shortened) ban phase is complete")
    }

    // ─── 8. Squad mode is meta-first and excludes covered lanes per slot ─────

    @Test
    fun `Squad suggestions are meta-first and skip already-covered ally lanes`() = runTest {
        val repo = FakeDraftRepository()
        val viewModel = DraftViewModel(repo, heroMetaService())

        viewModel.setupDraft(
            partySize = 5, picks = emptySet(), isFirst = true,
            lanes = emptySet(), banCount = 5
        )

        // Ally[0] = Bruno (Gold). Wave 2 active: ally[1, 2] still open.
        // Expected: two meta-first groups; neither may top-suggest a Gold-only hero
        // (Bruno/Miya/Layla), AND the two slots must target different lanes so the
        // team naturally diversifies (Fanny=Jungle → Estes=Roam).
        val bruno = Hero(id = 15, name = "Bruno", lane = HeroLane.GOLD_LANE)
        val state = DraftState(
            isUserFirstPick = true,
            banCountPerSide = 5,
            allyBans = List(5) { dummy },
            enemyBans = List(5) { dummy },
            allySlots = listOf(bruno, null, null, null, null),
            enemySlots = listOf(dummy, dummy, null, null, null)
        )
        repo.emit(state)

        val ready = viewModel.uiState.value
        assertIs<DraftUiState.Ready>(ready)
        assertTrue(ready.isSquad, "partySize=5 should flip into Squad mode")
        assertTrue(ready.isUserTurn, "Squad → every ally slot is user-owned")
        assertEquals(2, ready.pickSuggestionGroups.size, "wave 2 has two active ally slots")
        ready.pickSuggestionGroups.forEachIndexed { i, group ->
            assertEquals(null, group.lane, "Squad groups are not lane-targeted; meta-first instead")
            assertTrue(group.label.startsWith("Slot "),
                "label should be a plain 'Slot N' (no extra suffix)")
            assertTrue(!group.label.contains("meta", ignoreCase = true),
                "label should not include 'Best meta' suffix (group $i: ${group.label})")
        }

        // Top hero of the first slot must not be a Gold-only hero (Bruno already locked it).
        val topFirst = ready.pickSuggestionGroups[0].suggestions.first().hero
        assertTrue(
            topFirst.lanes.any { it != HeroLane.GOLD_LANE },
            "first slot must avoid the already-covered Gold Lane (top=${topFirst.name})"
        )

        // Two simultaneously-active slots must not both top-pick the same lane.
        val topFirstLane = topFirst.lanes.firstOrNull()
        val topSecond = ready.pickSuggestionGroups[1].suggestions.first().hero
        assertTrue(
            topSecond.lanes.none { it == topFirstLane },
            "second slot (${topSecond.name}) must avoid the lane the first slot already claims"
        )
    }

    // ─── 9. Duo/Trio drop preferred lanes that ally has already filled ───────

    @Test
    fun `Duo preferred lane group disappears once ally locks a hero in that lane`() = runTest {
        val repo = FakeDraftRepository()
        val viewModel = DraftViewModel(repo, heroMetaService())

        viewModel.setupDraft(
            partySize = 2,
            picks = setOf(1, 2),
            isFirst = true,
            lanes = setOf(HeroLane.GOLD_LANE, HeroLane.JUNGLE),
            banCount = 5
        )

        // Ally[0] = Fanny (Jungle). Wave 2 active: ally[1,2]. The Jungle group
        // must drop out because the team already has Jungle — only Gold remains.
        val fanny = Hero(id = 11, name = "Fanny", lane = HeroLane.JUNGLE)
        repo.emit(DraftState(
            isUserFirstPick = true,
            banCountPerSide = 5,
            allyBans = List(5) { dummy },
            enemyBans = List(5) { dummy },
            allySlots = listOf(fanny, null, null, null, null),
            enemySlots = listOf(dummy, dummy, null, null, null)
        ))

        val ready = viewModel.uiState.value
        assertIs<DraftUiState.Ready>(ready)
        assertTrue(ready.isUserTurn, "ally wave should be the user's turn in Duo")
        assertEquals(1, ready.pickSuggestionGroups.size,
            "covered Jungle group must be dropped, leaving only Gold (${ready.pickSuggestionGroups.map { it.lane }})")
        assertEquals(HeroLane.GOLD_LANE, ready.pickSuggestionGroups.single().lane)
    }
}
