package com.example.metaforge.data.local

import com.example.metaforge.domain.model.Hero
import com.example.metaforge.domain.model.HeroLane
import com.example.metaforge.domain.model.HeroMetaEntry
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class HeroMetaService(private val jsonLoader: suspend () -> String) {
    private var cache: List<HeroMetaEntry>? = null
    private val mutex = Mutex()

    suspend fun getAllHeroes(): List<HeroMetaEntry> {
        cache?.let { return it }
        return mutex.withLock {
            cache ?: HeroMetaRepository.parseAndBuild(jsonLoader()).also { cache = it }
        }
    }

    suspend fun getHeroesAsDomainModel(): List<Hero> = getAllHeroes().map { it.toHero() }
}

fun HeroMetaEntry.toHero(): Hero = Hero(
    id = id,
    name = name,
    lane = lanes.firstOrNull() ?: HeroLane.MID_LANE,
    role = heroClass,
    imageUrl = portraitUrl,
    iconUrl = portraitUrl,
    specialty = speciality.joinToString(", ")
)
