package com.example.metaforge.presentation.draft_setup

import com.example.metaforge.domain.model.HeroLane
import com.example.metaforge.presentation.screens.draft_setup.DraftSetupViewModel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Unit tests for [DraftSetupViewModel]. The VM is a pure state machine —
 * no coroutines, no IO — so we can drive it synchronously and assert
 * directly on `uiState.value`.
 */
class DraftSetupViewModelTest {

    // ─── Initial state ──────────────────────────────────────────────────────

    @Test
    fun `initial state has empty sets and Mythic 5-ban default`() {
        val vm = DraftSetupViewModel()
        val s = vm.uiState.value

        assertEquals("Mythic", s.rank)
        assertEquals(1, s.partySize)
        assertEquals(true, s.isFirstPick)
        assertTrue(s.pickPositions.isEmpty(), "no slots picked yet")
        assertTrue(s.preferredLanes.isEmpty(), "no lanes picked yet")
        assertEquals(5, s.banCountPerSide)
        assertFalse(s.isSquad)
    }

    // ─── Rank → banCount ────────────────────────────────────────────────────

    @Test
    fun `setRank Epic maps to 3 bans per side`() {
        val vm = DraftSetupViewModel().apply { setRank("Epic") }
        assertEquals(3, vm.uiState.value.banCountPerSide)
    }

    @Test
    fun `setRank Legend maps to 4 bans per side`() {
        val vm = DraftSetupViewModel().apply { setRank("Legend") }
        assertEquals(4, vm.uiState.value.banCountPerSide)
    }

    @Test
    fun `setRank Mythic maps to 5 bans per side`() {
        val vm = DraftSetupViewModel().apply { setRank("Mythic") }
        assertEquals(5, vm.uiState.value.banCountPerSide)
    }

    // ─── Party size resizing ────────────────────────────────────────────────

    @Test
    fun `setPartySize Squad clears both multi-select sets`() {
        val vm = DraftSetupViewModel().apply {
            togglePickPosition(1)
            togglePreferredLane(HeroLane.GOLD_LANE)
            setPartySize(5)
        }
        val s = vm.uiState.value
        assertEquals(5, s.partySize)
        assertTrue(s.isSquad)
        assertTrue(s.pickPositions.isEmpty(), "Squad must clear pickPositions")
        assertTrue(s.preferredLanes.isEmpty(), "Squad must clear preferredLanes")
    }

    @Test
    fun `setPartySize trims existing selections to fit the new cap`() {
        val vm = DraftSetupViewModel().apply {
            setPartySize(3)
            togglePickPosition(1); togglePickPosition(2); togglePickPosition(3)
            togglePreferredLane(HeroLane.GOLD_LANE)
            togglePreferredLane(HeroLane.JUNGLE)
            togglePreferredLane(HeroLane.MID_LANE)
            setPartySize(2) // Trio → Duo: trim to 2
        }
        val s = vm.uiState.value
        assertEquals(2, s.partySize)
        assertEquals(2, s.pickPositions.size)
        assertEquals(2, s.preferredLanes.size)
    }

    @Test
    fun `setPartySize does not auto-seed when upsizing`() {
        val vm = DraftSetupViewModel().apply {
            setPartySize(1)
            togglePickPosition(2)
            setPartySize(3) // empty + one selection; upsize must NOT inject extras
        }
        val s = vm.uiState.value
        assertEquals(3, s.partySize)
        // User keeps their single selection — caller must consciously add more
        assertEquals(setOf(2), s.pickPositions)
        assertTrue(s.preferredLanes.isEmpty(),
            "upsizing must not auto-seed lanes the user didn't choose")
    }

    // ─── Toggle behaviour ───────────────────────────────────────────────────

    @Test
    fun `togglePickPosition adds when under cap and removes when re-toggled`() {
        val vm = DraftSetupViewModel().apply {
            setPartySize(2)
            togglePickPosition(3) // add
            togglePickPosition(4) // add
        }
        assertEquals(setOf(3, 4), vm.uiState.value.pickPositions)

        vm.togglePickPosition(3) // remove
        assertEquals(setOf(4), vm.uiState.value.pickPositions)
    }

    @Test
    fun `togglePickPosition refuses to exceed the partySize cap`() {
        val vm = DraftSetupViewModel().apply {
            setPartySize(2)
            togglePickPosition(1)
            togglePickPosition(2)
            togglePickPosition(3) // would be 3rd — must be ignored
        }
        val picks = vm.uiState.value.pickPositions
        assertEquals(2, picks.size, "cap=2 must hold; got $picks")
        assertTrue(3 !in picks, "third toggle attempt should not land in the set")
    }

    @Test
    fun `togglePreferredLane mirrors togglePickPosition semantics with the same cap`() {
        val vm = DraftSetupViewModel().apply {
            setPartySize(2)
            togglePreferredLane(HeroLane.GOLD_LANE)
            togglePreferredLane(HeroLane.JUNGLE)
            togglePreferredLane(HeroLane.MID_LANE) // capped
        }
        val lanes = vm.uiState.value.preferredLanes
        assertEquals(setOf(HeroLane.GOLD_LANE, HeroLane.JUNGLE), lanes)

        vm.togglePreferredLane(HeroLane.GOLD_LANE) // re-toggle removes
        assertEquals(setOf(HeroLane.JUNGLE), vm.uiState.value.preferredLanes)
    }

    // ─── First-pick toggle ──────────────────────────────────────────────────

    @Test
    fun `setFirstPick flips the team side flag`() {
        val vm = DraftSetupViewModel().apply { setFirstPick(false) }
        assertFalse(vm.uiState.value.isFirstPick)
        vm.setFirstPick(true)
        assertTrue(vm.uiState.value.isFirstPick)
    }
}
