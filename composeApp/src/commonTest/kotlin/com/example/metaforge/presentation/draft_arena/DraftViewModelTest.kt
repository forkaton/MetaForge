package com.example.metaforge.presentation.draft_arena

import app.cash.turbine.test
import com.example.metaforge.data.local.HeroMetaService
import com.example.metaforge.domain.model.DraftState
import com.example.metaforge.domain.model.Hero
import com.example.metaforge.domain.model.HeroLane
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

    /** In-memory fake driving the same StateFlow the real repository exposes. */
    private class FakeDraftRepository : DraftRepository {
        val state = MutableStateFlow(DraftState())

        /** Test helper to push an arbitrary draft state and trigger recomputation. */
        fun emit(newState: DraftState) { state.value = newState }

        override fun getDraftState(): Flow<DraftState> = state.asStateFlow()
        override fun getAllHeroes(): Flow<List<Hero>> = flowOf(emptyList())
        override suspend fun syncHeroes() {}
        override suspend fun pickHero(slotIndex: Int, isAlly: Boolean, hero: Hero?) {}
        override suspend fun banHero(slotIndex: Int, isAlly: Boolean, hero: Hero?) {}
        override suspend fun clearDraft() { state.value = DraftState() }
        override suspend fun setFirstPick(isFirstPick: Boolean) {
            state.update { it.copy(isUserFirstPick = isFirstPick) }
        }
    }

    // Crafted meta JSON in the exact shape HeroMetaRepository.parseAndBuild expects.
    // "Layla".counters = [Bruno] means Bruno is *strong against* Layla (reverse mapping).
    private val metaJson = """
        {"data":[
          {"hero_name":"Bruno","mlid":"15","portrait":"bruno.png","laning":["Gold Lane"],"class":"Marksman","speciality":["Damage"],"counters":[],"synergies":[]},
          {"hero_name":"Miya","mlid":"14","portrait":"miya.png","laning":["Gold Lane"],"class":"Marksman","speciality":["Damage"],"counters":[],"synergies":[]},
          {"hero_name":"Layla","mlid":"13","portrait":"layla.png","laning":["Gold Lane"],"class":"Marksman","speciality":["Damage"],"counters":[{"heroid":15,"heroname":"Bruno"}],"synergies":[]},
          {"hero_name":"Fanny","mlid":"11","portrait":"fanny.png","laning":["Jungle"],"class":"Assassin","speciality":["Mobility"],"counters":[],"synergies":[]},
          {"hero_name":"Estes","mlid":"12","portrait":"estes.png","laning":["Roam"],"class":"Support","speciality":["Heal"],"counters":[],"synergies":[]}
        ]}
    """.trimIndent()

    private fun heroMetaService(loader: suspend () -> String = { metaJson }) = HeroMetaService(loader)

    private val dummy = Hero(id = 999, name = "Dummy", lane = HeroLane.MID_LANE)

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

    // ─── 2. Success state + lane-filtered ban suggestions (Turbine) ───────────

    @Test
    fun `setupDraft during ban phase emits Ready with lane-filtered ban suggestions`() = runTest {
        val viewModel = DraftViewModel(FakeDraftRepository(), heroMetaService())

        viewModel.uiState.test {
            assertIs<DraftUiState.Loading>(awaitItem()) // initial emission

            viewModel.setupDraft(pickPos = 1, isFirst = true, lane = "GOLD_LANE")

            val ready = awaitItem()
            assertIs<DraftUiState.Ready>(ready)
            // Ban phase is active, so ban suggestions are computed and pick suggestions are not.
            assertTrue(ready.banSuggestions.isNotEmpty(), "ban suggestions should be present in ban phase")
            assertTrue(ready.pickSuggestions.isEmpty(), "no pick suggestions before ban phase completes")
            // Every suggestion must fit the requested Gold lane.
            assertTrue(ready.banSuggestions.all { HeroLane.GOLD_LANE in it.hero.lanes })
            // Results are ranked highest-score first.
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

        viewModel.setupDraft(pickPos = 1, isFirst = true, lane = "GOLD_LANE")

        val state = viewModel.uiState.value
        assertIs<DraftUiState.Error>(state)
        assertEquals("network down", state.message)
    }

    // ─── 4. Snake-pick business logic (1-2-2-2-2-1) + counter suggestion ──────

    @Test
    fun `during ally pick wave the counter hero is suggested and turn is flagged`() = runTest {
        val repo = FakeDraftRepository()
        val viewModel = DraftViewModel(repo, heroMetaService())

        viewModel.setupDraft(pickPos = 2, isFirst = true, lane = "GOLD_LANE")

        // Build a wave-2 state for a first-pick user (snake order 1-2-2-2-2-1):
        //   wave 0 -> ally[0] (done) | wave 1 -> enemy[0,1] (done) | wave 2 -> ally[1,2] (active)
        // Enemy has picked Layla, whom Bruno hard-counters.
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
        // It is the user's (ally) turn during wave 2.
        assertTrue(ready.isUserTurn, "wave 2 belongs to the ally team for a first-pick user")
        // Bruno counters the enemy Layla, so he must appear with a positive counter bonus.
        val bruno = ready.pickSuggestions.firstOrNull { it.hero.name == "Bruno" }
        assertTrue(bruno != null, "Bruno should be suggested as a counter to Layla")
        assertTrue(bruno.counterBonus >= 20, "counter bonus should reward beating an enemy pick")
        assertTrue(bruno.reasons.any { it.contains("Counters", ignoreCase = true) })
    }

    // ─── 5. Re-computation reacts to lane change ──────────────────────────────

    @Test
    fun `updateLane recomputes ban suggestions for the newly selected lane`() = runTest {
        val viewModel = DraftViewModel(FakeDraftRepository(), heroMetaService())
        viewModel.setupDraft(pickPos = 1, isFirst = true, lane = "GOLD_LANE")

        // Switch to Jungle; only Fanny sits in that lane in our fixture.
        viewModel.updateLane(HeroLane.JUNGLE)

        val ready = viewModel.uiState.value
        assertIs<DraftUiState.Ready>(ready)
        assertEquals(HeroLane.JUNGLE, ready.currentLane)
        assertTrue(ready.banSuggestions.isNotEmpty())
        assertTrue(ready.banSuggestions.all { HeroLane.JUNGLE in it.hero.lanes })
    }

    // ─── 6. Second-pick snake order (0→enemy, 1→ally) + synergy bonus ────────

    @Test
    fun `second-pick wave 1 is ally turn and synergy with ally pick is rewarded`() = runTest {
        // Bruno declares Estes as a synergy partner — picking Estes on our team
        // should boost Bruno's synergyBonus and surface a "Synergy" reason.
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

        viewModel.setupDraft(pickPos = 2, isFirst = false, lane = "GOLD_LANE")

        // Second-pick snake order (1-2-2-2-2-1):
        //   wave 0 -> enemy[0] (done) | wave 1 -> ally[0,1] (active, user's first wave)
        // Ally already locked Estes at slot 0 → Bruno (who synergises with Estes) should
        // appear in suggestions with synergyBonus >= 10.
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
        val bruno = ready.pickSuggestions.firstOrNull { it.hero.name == "Bruno" }
        assertTrue(bruno != null, "Bruno should be suggested in the ally pick wave")
        assertTrue(bruno.synergyBonus >= 10, "synergy bonus must reward picking alongside Estes")
        assertTrue(bruno.reasons.any { it.contains("Synergy", ignoreCase = true) },
            "reasons should explain the synergy")
    }
}
