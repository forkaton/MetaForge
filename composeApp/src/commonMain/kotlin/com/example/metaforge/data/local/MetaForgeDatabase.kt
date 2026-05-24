package com.example.metaforge.data.local

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.db.SqlDriver
import com.example.metaforge.domain.model.Hero
import com.example.metaforge.domain.model.HeroLane
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class MetaForgeDatabaseWrapper(driver: SqlDriver) {
    private val database: MetaForgeDatabase = MetaForgeDatabase(driver)
    private val heroQueries = database.heroQueries

    // ── SSOT: reactive Flow observed by UI ────────────────────────────────────
    fun getAllHeroesFlow(): Flow<List<Hero>> =
        heroQueries.selectAllHeroes()
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { list ->
                list.map { e ->
                    Hero(
                        id        = e.id.toInt(),
                        name      = e.name,
                        lane      = safeLane(e.lane),
                        role      = e.role,
                        imageUrl  = e.imageUrl,
                        iconUrl   = e.iconUrl,
                        specialty = e.specialty
                    )
                }
            }

    // ── Write helpers (called by background sync) ─────────────────────────────
    suspend fun saveHeroes(heroes: List<Hero>) {
        heroQueries.deleteAllHeroes()
        heroes.forEach { h ->
            heroQueries.insertHero(
                id        = h.id.toLong(),
                name      = h.name,
                lane      = h.lane.name,
                role      = h.role,
                imageUrl  = h.imageUrl,
                iconUrl   = h.iconUrl,
                specialty = h.specialty
            )
        }
    }

    suspend fun getAllHeroes(): List<Hero> =
        heroQueries.selectAllHeroes().executeAsList().map { e ->
            Hero(
                id        = e.id.toInt(),
                name      = e.name,
                lane      = safeLane(e.lane),
                role      = e.role,
                imageUrl  = e.imageUrl,
                iconUrl   = e.iconUrl,
                specialty = e.specialty
            )
        }

    suspend fun getHeroCount(): Int = heroQueries.selectAllHeroes().executeAsList().size

    suspend fun clearHeroes() = heroQueries.deleteAllHeroes()

    private fun safeLane(raw: String): HeroLane =
        runCatching { HeroLane.valueOf(raw) }.getOrDefault(HeroLane.MID_LANE)
}
