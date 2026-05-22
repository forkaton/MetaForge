package com.example.metaforge.data.repository

import com.example.metaforge.domain.model.Hero
import com.example.metaforge.domain.model.HeroRole
import com.example.metaforge.data.local.datastore.DraftPreferences
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

// FAKE untuk DraftPreferences agar test bisa jalan tanpa DataStore beneran
class FakeDraftPreferences : DraftPreferences(dataStore = TODO("Not needed for fakes")) {
    override suspend fun saveLastDraft(draftData: String) {}
    override fun getLastDraft() = flowOf(null)
    override suspend fun clearDraft() {}
}

class DraftRepositoryTest {

    // FIX: Gunakan Fake bukannya null agar kompilasi sukses
    private val repository = DraftRepositoryImpl(draftPreferences = FakeDraftPreferences())

    private val testHero = Hero(
        id = 11,
        name = "Fanny",
        role = HeroRole.ASSASSIN,
        imageUrl = "",
        specialty = "Mobility"
    )

    @Test
    fun `initial draft state should have empty slots`() = runTest {
        val state = repository.getDraftState().first()
        assertTrue(state.allySlots.all { it == null })
        assertTrue(state.enemySlots.all { it == null })
    }

    @Test
    fun `pick ally hero should fill correct slot`() = runTest {
        repository.pickHero(0, isAlly = true, isBan = false, hero = testHero)
        val state = repository.getDraftState().first()
        assertEquals(testHero, state.allySlots[0])
    }

    @Test
    fun `pick enemy hero should fill correct slot`() = runTest {
        repository.pickHero(2, isAlly = false, isBan = false, hero = testHero)
        val state = repository.getDraftState().first()
        assertEquals(testHero, state.enemySlots[2])
    }

    @Test
    fun `remove hero should set slot to null`() = runTest {
        repository.pickHero(0, isAlly = true, isBan = false, hero = testHero)
        repository.removeHero(0, isAlly = true, isBan = false)

        val state = repository.getDraftState().first()
        assertNull(state.allySlots[0])
    }

    @Test
    fun `clear draft should clear all slots`() = runTest {
        repository.pickHero(0, true, false, testHero)
        repository.pickHero(0, false, false, testHero)

        repository.resetDraft()

        val state = repository.getDraftState().first()
        assertTrue(state.allySlots.all { it == null })
        assertTrue(state.enemySlots.all { it == null })
    }

    @Test
    fun `getAllHeroes should return 30 heroes`() = runTest {
        val heroes = repository.getAllHeroes().first()
        assertEquals(30, heroes.size)
    }
}
