package com.example.metaforge.data.repository

import com.example.metaforge.domain.model.DraftState
import com.example.metaforge.domain.model.Hero
import com.example.metaforge.domain.model.HeroLane
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.test.assertFalse

// Tests for DraftState logic — no platform dependencies required
class DraftRepositoryTest {

    private val testHero = Hero(
        id = 11, name = "Fanny", lane = HeroLane.JUNGLE,
        imageUrl = "", specialty = "Mobility"
    )

    @Test
    fun `initial draft state should have empty slots`() {
        val state = DraftState()
        assertTrue(state.allySlots.all { it == null })
        assertTrue(state.enemySlots.all { it == null })
        assertTrue(state.allyBans.all { it == null })
        assertTrue(state.enemyBans.all { it == null })
    }

    @Test
    fun `pick ally hero should fill correct slot`() {
        val state = DraftState().pickAlly(0, testHero)
        assertEquals(testHero, state.allySlots[0])
    }

    @Test
    fun `pick enemy hero should fill correct slot`() {
        val state = DraftState().pickEnemy(2, testHero)
        assertEquals(testHero, state.enemySlots[2])
    }

    @Test
    fun `remove ally hero should clear slot`() {
        val state = DraftState().pickAlly(0, testHero).removeAlly(0)
        assertNull(state.allySlots[0])
    }

    @Test
    fun `remove enemy hero should clear slot`() {
        val state = DraftState().pickEnemy(1, testHero).removeEnemy(1)
        assertNull(state.enemySlots[1])
    }

    @Test
    fun `reset all should clear all slots and bans`() {
        val state = DraftState()
            .pickAlly(0, testHero).pickEnemy(0, testHero)
            .banAlly(0, testHero).banEnemy(0, testHero)
            .resetAll()
        assertTrue(state.allySlots.all { it == null })
        assertTrue(state.enemySlots.all { it == null })
        assertTrue(state.allyBans.all { it == null })
        assertTrue(state.enemyBans.all { it == null })
    }

    @Test
    fun `ban phase complete when all 10 bans filled`() {
        var state = DraftState()
        assertFalse(state.isBanPhaseComplete)
        for (i in 0..4) {
            state = state.banAlly(i, testHero).banEnemy(i, testHero)
        }
        assertTrue(state.isBanPhaseComplete)
    }

    @Test
    fun `ban phase not complete with partial bans`() {
        val state = DraftState().banAlly(0, testHero).banAlly(1, testHero)
        assertFalse(state.isBanPhaseComplete)
    }

    @Test
    fun `first pick wave activates ally slot 0`() {
        // Fill all bans to enter pick phase
        var state = DraftState(isUserFirstPick = true)
        for (i in 0..4) state = state.banAlly(i, testHero).banEnemy(i, testHero)
        // Wave 0 for first pick = ally slot 0
        assertTrue(state.isCurrentPickSlot(0, true))
        assertFalse(state.isCurrentPickSlot(0, false))
    }

    @Test
    fun `second pick wave activates enemy slot 0 first`() {
        var state = DraftState(isUserFirstPick = false)
        for (i in 0..4) state = state.banAlly(i, testHero).banEnemy(i, testHero)
        // Wave 0 for second pick = enemy slot 0 (Blue picks first)
        assertTrue(state.isCurrentPickSlot(0, false))
        assertFalse(state.isCurrentPickSlot(0, true))
    }

    @Test
    fun `wave advances after slot filled`() {
        var state = DraftState(isUserFirstPick = true)
        for (i in 0..4) state = state.banAlly(i, testHero).banEnemy(i, testHero)
        // Wave 0: ally[0]
        assertEquals(0, state.currentPickWave())
        state = state.pickAlly(0, testHero)
        // Wave 1: enemy[0,1]
        assertEquals(1, state.currentPickWave())
    }

    @Test
    fun `removing pick reactivates that wave`() {
        var state = DraftState(isUserFirstPick = true)
        for (i in 0..4) state = state.banAlly(i, testHero).banEnemy(i, testHero)
        state = state.pickAlly(0, testHero)
        assertEquals(1, state.currentPickWave())
        state = state.removeAlly(0)
        // Wave 0 is incomplete again
        assertEquals(0, state.currentPickWave())
    }

    @Test
    fun `removing ban does not affect pick wave`() {
        var state = DraftState(isUserFirstPick = true)
        for (i in 0..4) state = state.banAlly(i, testHero).banEnemy(i, testHero)
        state = state.pickAlly(0, testHero)
        val waveBefore = state.currentPickWave()
        state = state.removeAllyBan(0)
        // Pick wave should not change when a ban is removed
        assertEquals(waveBefore, state.currentPickWave())
    }

    @Test
    fun `draft is complete when all picks and bans filled`() {
        var state = DraftState(isUserFirstPick = true)
        for (i in 0..4) state = state.banAlly(i, testHero).banEnemy(i, testHero)
        for (i in 0..4) state = state.pickAlly(i, testHero).pickEnemy(i, testHero)
        assertTrue(state.isComplete)
    }
}
