package com.example.metaforge.data.repository

import com.example.metaforge.data.local.MetaForgeDatabaseWrapper
import com.example.metaforge.data.local.datastore.DraftPreferences
import com.example.metaforge.data.remote.api.MLBBApiService
import com.example.metaforge.domain.model.DraftState
import com.example.metaforge.domain.model.Hero
import com.example.metaforge.domain.model.HeroLane
import com.example.metaforge.domain.repository.DraftRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class DraftRepositoryImpl(
    private val prefs: DraftPreferences?,
    private val database: MetaForgeDatabaseWrapper,
    private val api: MLBBApiService
) : DraftRepository {

    private val _draftState = MutableStateFlow(DraftState())
    override fun getDraftState(): Flow<DraftState> = _draftState.asStateFlow()

    // ── SSOT: UI always reads from the local DB Flow ──────────────────────────
    override fun getAllHeroes(): Flow<List<Hero>> = database.getAllHeroesFlow()

    // ── Background sync: fetch API → overwrite DB ─────────────────────────────
    override suspend fun syncHeroes() {
        try {
            val response = api.fetchHeroesPositions()
            val heroes = response.data?.records?.mapNotNull { record ->
                val heroData = record.data?.hero?.data
                val heroId   = record.data?.hero_id ?: return@mapNotNull null
                heroData?.let { data ->
                    val laneRaw = data.roadsort?.firstOrNull()?.data?.road_sort_title?.lowercase()?.trim() ?: ""
                    val lane = when {
                        laneRaw.contains("exp")                           -> HeroLane.EXP_LANE
                        laneRaw.contains("gold")                          -> HeroLane.GOLD_LANE
                        laneRaw.contains("mid")                           -> HeroLane.MID_LANE
                        laneRaw.contains("jungle") || laneRaw.contains("jng") -> HeroLane.JUNGLE
                        laneRaw.contains("roam") || laneRaw.contains("support") -> HeroLane.ROAM
                        else                                              -> HeroLane.MID_LANE
                    }
                    Hero(
                        id       = heroId,
                        name     = data.name ?: "Unknown",
                        lane     = lane,
                        role     = laneRaw,
                        imageUrl = data.smallmap ?: "",
                        iconUrl  = data.smallmap ?: ""
                    )
                }
            } ?: emptyList()

            if (heroes.isNotEmpty()) {
                database.saveHeroes(heroes)
                println("[Sync] Saved ${heroes.size} heroes to DB")
            }
        } catch (e: Exception) {
            // Silently fail — UI continues to show cached data
            println("[Sync] Failed: ${e.message}")
            throw e   // re-throw so caller can detect offline state
        }
    }

    override suspend fun pickHero(slotIndex: Int, isAlly: Boolean, hero: Hero?) {
        _draftState.update { state ->
            if (isAlly) state.copy(
                allySlots = state.allySlots.toMutableList().apply { if (slotIndex in indices) this[slotIndex] = hero }
            ) else state.copy(
                enemySlots = state.enemySlots.toMutableList().apply { if (slotIndex in indices) this[slotIndex] = hero }
            )
        }
    }

    override suspend fun banHero(slotIndex: Int, isAlly: Boolean, hero: Hero?) {
        _draftState.update { state ->
            if (isAlly) state.copy(
                allyBans = state.allyBans.toMutableList().apply { if (slotIndex in indices) this[slotIndex] = hero }
            ) else state.copy(
                enemyBans = state.enemyBans.toMutableList().apply { if (slotIndex in indices) this[slotIndex] = hero }
            )
        }
    }

    override suspend fun clearDraft() { _draftState.value = DraftState() }
}
