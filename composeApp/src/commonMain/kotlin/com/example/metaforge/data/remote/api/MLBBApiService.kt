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

/**
 * Ktor HTTP client service untuk MLBB Public API
 * 
 * Bertanggung jawab untuk:
 * - Fetch heroes list dengan lane positions
 * - Handle network errors dan timeouts
 * - Deserialize JSON response ke Kotlin data classes
 */
class MLBBApiService(private val client: HttpClient) {
    
    companion object {
        private const val BASE_URL = "https://mlbb.rone.dev/api"
        private const val ENDPOINT_HEROES_POSITIONS = "$BASE_URL/heroes/positions"
    }
    
    /**
     * Fetch semua heroes dengan lane positions dari MLBB API
     * 
     * Endpoint: GET /api/heroes/positions?size=150
     * 
     * Response format:
     * {
     *   "data": {
     *     "records": [
     *       {
     *         "data": {
     *           "hero_id": 101,
     *           "hero": {
     *             "data": {
     *               "name": "Fanny",
     *               "smallmap": "https://cdn...",
     *               "roadsort": [
     *                 {"data": {"road_sort_title": "Gold"}}
     *               ]
     *             }
     *           }
     *         }
     *       }
     *     ]
     *   }
     * }
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
}