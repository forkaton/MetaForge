package com.example.metaforge.domain.repository

import com.example.metaforge.domain.model.DraftState
import com.example.metaforge.domain.model.Hero
import kotlinx.coroutines.flow.Flow

interface DraftRepository {
    fun getDraftState(): Flow<DraftState>

    /** SSOT: emits from SQLDelight cache; auto-updates when background sync writes. */
    fun getAllHeroes(): Flow<List<Hero>>

    /** Fetch latest heroes from MLBB API and overwrite the local DB. */
    suspend fun syncHeroes()

    suspend fun pickHero(slotIndex: Int, isAlly: Boolean, hero: Hero?)
    suspend fun banHero(slotIndex: Int, isAlly: Boolean, hero: Hero?)
    suspend fun clearDraft()
}
