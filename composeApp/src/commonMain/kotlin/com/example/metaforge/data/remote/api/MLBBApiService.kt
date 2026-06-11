package com.example.metaforge.data.remote.api

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import kotlinx.serialization.Serializable

/**
 * Data classes untuk parsing response dari MLBB Public API
 * 
 * Source: https://mlbb.rone.dev/
 * Endpoint: GET /api/heroes/positions?size=150
 */

@Serializable
data class MlbbResponse(val data: MlbbData? = null)

@Serializable
data class MlbbData(val records: List<MlbbRecord>? = null)

@Serializable
data class MlbbRecord(val data: RecordData? = null)

@Serializable
data class RecordData(
    val hero_id: Int? = null,
    val hero: HeroNode? = null
)

@Serializable
data class HeroNode(val data: HeroDataNode? = null)

@Serializable
data class HeroDataNode(
    val name: String? = null,
    val smallmap: String? = null,
    val roadsort: List<RoadSortNode>? = null
)

@Serializable
data class RoadSortNode(val data: RoadSortData? = null)

@Serializable
data class RoadSortData(val road_sort_title: String? = null)

// ─── Rank endpoint DTOs ──────────────────────────────────────────────────────

@Serializable
data class MlbbRankResponse(val code: Int? = null, val data: MlbbRankData? = null)

@Serializable
data class MlbbRankData(val records: List<MlbbRankRecord>? = null, val total: Int? = null)

@Serializable
data class MlbbRankRecord(val data: RankRecordData? = null)

@Serializable
data class RankRecordData(
    val main_heroid: Int? = null,
    val main_hero: RankHeroNode? = null,
    val main_hero_win_rate: Double? = null,
    val main_hero_appearance_rate: Double? = null,
    val main_hero_ban_rate: Double? = null
)

@Serializable
data class RankHeroNode(val data: RankHeroDataNode? = null)

@Serializable
data class RankHeroDataNode(
    val name: String? = null,
    val head: String? = null
)

/**
 * Ktor HTTP client service untuk MLBB Public API
 * 
 * Bertanggung jawab untuk:
 * - Fetch heroes list dengan lane positions
 * - Fetch heroes rank data (win rate, pick rate, ban rate)
 * - Handle network errors dan timeouts
 * - Deserialize JSON response ke Kotlin data classes
 */
class MLBBApiService(private val client: HttpClient) {
    
    companion object {
        private const val BASE_URL = "https://mlbb.rone.dev/api"
        private const val ENDPOINT_HEROES_POSITIONS = "$BASE_URL/heroes/positions"
        private const val ENDPOINT_HEROES_RANK = "$BASE_URL/heroes/rank"
    }
    
    /**
     * Fetch semua heroes dengan lane positions dari MLBB API
     * 
     * Endpoint: GET /api/heroes/positions?size=150
     * 
     * @return MlbbResponse containing list of heroes
     * @throws Exception jika API call gagal
     */
    suspend fun fetchHeroesPositions(): MlbbResponse {
        return try {
            val response: HttpResponse = client.get("$ENDPOINT_HEROES_POSITIONS?size=150")
            response.body()
        } catch (e: Exception) {
            throw Exception("Failed to fetch heroes positions from MLBB API: ${e.message}", e)
        }
    }

    /**
     * Fetch hero rank data (win rate, pick rate, ban rate) dari MLBB API.
     *
     * Endpoint: GET /api/heroes/rank?size=150
     *
     * @return MlbbRankResponse containing rank stats for all heroes
     * @throws Exception jika API call gagal
     */
    suspend fun fetchHeroesRank(): MlbbRankResponse {
        return try {
            val response: HttpResponse = client.get("$ENDPOINT_HEROES_RANK?size=150")
            response.body()
        } catch (e: Exception) {
            throw Exception("Failed to fetch heroes rank from MLBB API: ${e.message}", e)
        }
    }
}