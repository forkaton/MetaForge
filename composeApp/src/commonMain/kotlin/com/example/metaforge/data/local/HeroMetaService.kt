package com.example.metaforge.data.local

import com.example.metaforge.domain.model.Hero
import com.example.metaforge.domain.model.HeroLane
import com.example.metaforge.domain.model.HeroMetaEntry
import com.example.metaforge.domain.model.HeroRankData
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class HeroMetaService(
    private val jsonLoader: suspend () -> String,
    private val rankDataLoader: suspend () -> Map<Int, HeroRankData>
) {
    private var cache: List<HeroMetaEntry>? = null
    private val mutex = Mutex()

    suspend fun getAllHeroes(): List<HeroMetaEntry> {
        cache?.let { return it }
        return mutex.withLock {
            cache ?: run {
                val rankData = try { rankDataLoader() } catch (_: Exception) { emptyMap() }
                HeroMetaRepository.parseAndBuild(jsonLoader(), rankData).also { cache = it }
            }
        }
    }

    /** Get the cached rank data map for per-hero stat lookups. */
    suspend fun getRankDataMap(): Map<Int, HeroRankData> {
        return try { rankDataLoader() } catch (_: Exception) { emptyMap() }
    }

    suspend fun getHeroesAsDomainModel(): List<Hero> = getAllHeroes().map { it.toHero() }

    /** Clear the in-memory cache so the next [getAllHeroes] call re-fetches
     *  and re-parses from the network/DataStore. */
    fun invalidateCache() {
        cache = null
    }
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
