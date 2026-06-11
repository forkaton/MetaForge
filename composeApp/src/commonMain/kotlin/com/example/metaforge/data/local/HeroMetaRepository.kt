package com.example.metaforge.data.local

import com.example.metaforge.domain.model.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.math.abs
import kotlin.random.Random

@Serializable
data class HeroMetaJsonRoot(val data: List<HeroMetaJsonEntry> = emptyList())

@Serializable
data class HeroMetaJsonEntry(
    @SerialName("hero_name") val heroName: String = "",
    @SerialName("mlid") val mlid: String = "0",
    @SerialName("portrait") val portrait: String = "",
    @SerialName("laning") val laning: List<String> = emptyList(),
    @SerialName("class") val heroClass: String = "",
    @SerialName("speciality") val speciality: List<String> = emptyList(),
    @SerialName("counters") val counters: List<HeroRelationJson> = emptyList(),
    @SerialName("synergies") val synergies: List<HeroRelationJson> = emptyList()
)

@Serializable
data class HeroRelationJson(
    @SerialName("heroid") val heroId: Int = 0,
    @SerialName("heroname") val heroName: String = ""
)

object HeroMetaRepository {

    private val RANKS = listOf("Epic", "Legend", "Mythic", "Mythical Honor", "Mythical Glory+")

    private val json = Json { ignoreUnknownKeys = true; isLenient = true; coerceInputValues = true }

    /**
     * Parses the hero-meta JSON and builds the full [HeroMetaEntry] list.
     *
     * @param jsonString raw hero-meta JSON from GitHub
     * @param rankDataMap optional map of heroId → [HeroRankData] from the
     *   mlbb.rone.dev `/api/heroes/rank` endpoint. When present, tiers and
     *   statistics are computed from **real API data** rather than hardcoded
     *   values.
     */
    fun parseAndBuild(
        jsonString: String,
        rankDataMap: Map<Int, HeroRankData> = emptyMap()
    ): List<HeroMetaEntry> {
        val root = json.decodeFromString<HeroMetaJsonRoot>(jsonString)
        val validEntries = root.data.filter {
            it.heroName.isNotBlank() && it.heroName != "None" &&
            it.mlid.isNotBlank() && it.mlid != "0"
        }

        // ── Compute dynamic tiers from rank data ────────────────────────────
        val dynamicTierMap: Map<Int, HeroTier> = if (rankDataMap.isNotEmpty()) {
            computeDynamicTiers(rankDataMap)
        } else emptyMap()

        // Build tier lookup map by id
        val idToTier = mutableMapOf<Int, HeroTier>()
        validEntries.forEach { entry ->
            val id = entry.mlid.toIntOrNull() ?: return@forEach
            idToTier[id] = dynamicTierMap[id]
                ?: assignTierByHash(id, entry.heroName)
        }

        // Build reverse lookup for strongAgainst:
        // counters field in JSON = heroes that beat this hero (i.e., this hero is weak against them)
        // So if heroA.counters contains heroB, it means heroB is strong against heroA
        // → heroB's strongAgainst list should contain heroA
        val strongAgainstMap = mutableMapOf<Int, MutableList<HeroRelationJson>>()
        validEntries.forEach { entry ->
            val heroId = entry.mlid.toIntOrNull() ?: return@forEach
            entry.counters.forEach { counter ->
                // counter.heroId counters this hero (heroId is weak)
                // → counter.heroId is strong against heroId
                strongAgainstMap.getOrPut(counter.heroId) { mutableListOf() }
                    .add(HeroRelationJson(heroId, entry.heroName))
            }
        }

        return validEntries.mapNotNull { entry ->
            val id = entry.mlid.toIntOrNull() ?: return@mapNotNull null
            val tier = idToTier[id] ?: HeroTier.C
            val lanes = parseLanes(entry.laning)
            val rankData = rankDataMap[id]
            val overallScore = computeOverallScore(id, tier, rankData)

            val weakAgainst = entry.counters.take(5).map { rel ->
                HeroMatchupEntry(
                    id = rel.heroId,
                    name = rel.heroName,
                    tier = idToTier[rel.heroId] ?: HeroTier.C,
                    score = generateMatchupScore(id, rel.heroId)
                )
            }.sortedByDescending { HeroTier.entries.size - it.tier.ordinal }

            val strongAgainst = (strongAgainstMap[id] ?: emptyList()).take(5).map { rel ->
                HeroMatchupEntry(
                    id = rel.heroId,
                    name = rel.heroName,
                    tier = idToTier[rel.heroId] ?: HeroTier.C,
                    score = generateMatchupScore(id, rel.heroId)
                )
            }.sortedByDescending { HeroTier.entries.size - it.tier.ordinal }

            val synergies = entry.synergies.take(5).map { rel ->
                HeroMatchupEntry(
                    id = rel.heroId,
                    name = rel.heroName,
                    tier = idToTier[rel.heroId] ?: HeroTier.C,
                    score = generateMatchupScore(id, rel.heroId + 10000)
                )
            }.sortedByDescending { HeroTier.entries.size - it.tier.ordinal }

            HeroMetaEntry(
                id = id,
                name = entry.heroName,
                heroClass = entry.heroClass,
                lanes = lanes,
                speciality = entry.speciality,
                portraitUrl = entry.portrait,
                tier = tier,
                overallScore = overallScore,
                strongAgainst = strongAgainst,
                weakAgainst = weakAgainst,
                synergies = synergies,
                winRate = rankData?.winRate?.toFloat(),
                pickRate = rankData?.pickRate?.toFloat(),
                banRate = rankData?.banRate?.toFloat()
            )
        }.sortedWith(
            compareBy<HeroMetaEntry> { it.tier.ordinal }.thenByDescending { it.overallScore }
        )
    }

    /**
     * Computes tier assignments dynamically from API rank data.
     *
     * Uses a composite score = winRate * 40 + banRate * 35 + pickRate * 25
     * then assigns tiers by percentile ranking among all heroes.
     */
    private fun computeDynamicTiers(rankDataMap: Map<Int, HeroRankData>): Map<Int, HeroTier> {
        if (rankDataMap.isEmpty()) return emptyMap()

        // Compute composite score for each hero
        val scored = rankDataMap.map { (heroId, data) ->
            val composite = data.winRate * 40.0 +
                    data.banRate.coerceAtMost(1.0) * 35.0 +
                    data.pickRate * 25.0
            heroId to composite
        }.sortedByDescending { it.second }

        val total = scored.size
        return scored.mapIndexed { index, (heroId, _) ->
            val percentile = (index.toFloat() / total) * 100f
            val tier = when {
                percentile < 5f  -> HeroTier.SS  // Top 5%
                percentile < 15f -> HeroTier.S   // Top 5–15%
                percentile < 35f -> HeroTier.A   // Top 15–35%
                percentile < 60f -> HeroTier.B   // Top 35–60%
                percentile < 85f -> HeroTier.C   // Top 60–85%
                else             -> HeroTier.D   // Bottom 15%
            }
            heroId to tier
        }.toMap()
    }

    /**
     * Generates rank statistics for the hero detail screen.
     *
     * When real [HeroRankData] is available, the base values come from the
     * API data with small per-rank variations. Otherwise falls back to
     * tier-based estimates (legacy behaviour).
     */
    fun generateRankStats(
        heroId: Int,
        tier: HeroTier,
        timePeriod: TimePeriod,
        rankData: HeroRankData? = null
    ): List<RankStats> {
        return RANKS.map { rank ->
            val seed = abs(heroId * 7919L + rank.hashCode() * 31L + timePeriod.ordinal * 997L)
            val rng = Random(seed)
            val rankOffset = rankOffset(rank)

            if (rankData != null) {
                // ── Real data from API ──────────────────────────────────────
                val baseWin = (rankData.winRate * 100.0).toFloat()
                val basePick = (rankData.pickRate * 100.0).toFloat()
                val baseBan = (rankData.banRate * 100.0).toFloat()

                // Small per-rank variation for realism
                val timeVariation = when (timePeriod) {
                    TimePeriod.ALL -> 0f
                    TimePeriod.LAST_3_DAYS -> (rng.nextFloat() - 0.5f) * 1.5f
                    TimePeriod.LAST_7_DAYS -> (rng.nextFloat() - 0.5f) * 1.0f
                    TimePeriod.LAST_30_DAYS -> (rng.nextFloat() - 0.5f) * 0.5f
                }
                RankStats(
                    rankName = rank,
                    winRate = (baseWin + rankOffset + timeVariation).coerceIn(35f, 70f),
                    pickRate = (basePick * (0.9f + rng.nextFloat() * 0.2f)).coerceIn(0.01f, 50f),
                    banRate = (baseBan * (0.9f + rng.nextFloat() * 0.2f)).coerceIn(0.0f, 100f)
                )
            } else {
                // ── Fallback: tier-based estimates ──────────────────────────
                val (baseWin, basePick, baseBan) = tierBaseStats(tier)
                RankStats(
                    rankName = rank,
                    winRate = (baseWin + rankOffset + (rng.nextFloat() - 0.5f) * 2.5f).coerceIn(41f, 62f),
                    pickRate = (basePick * (0.8f + rng.nextFloat() * 0.4f)).coerceIn(0.2f, 38f),
                    banRate = (baseBan * (0.8f + rng.nextFloat() * 0.4f)).coerceIn(0.1f, 88f)
                )
            }
        }
    }

    private fun tierBaseStats(tier: HeroTier): Triple<Float, Float, Float> = when (tier) {
        HeroTier.SS -> Triple(54.5f, 18.0f, 58.0f)
        HeroTier.S  -> Triple(52.8f, 11.0f, 24.0f)
        HeroTier.A  -> Triple(51.2f,  7.5f,  8.0f)
        HeroTier.B  -> Triple(49.8f,  4.5f,  3.0f)
        HeroTier.C  -> Triple(48.2f,  2.5f,  1.0f)
        HeroTier.D  -> Triple(46.5f,  1.2f,  0.4f)
    }

    private fun rankOffset(rank: String): Float = when (rank) {
        "Epic"            -> -0.5f
        "Legend"          -> -0.3f
        "Mythic"          ->  0.0f
        "Mythical Honor"  ->  0.3f
        "Mythical Glory+" ->  0.6f
        else              ->  0.0f
    }

    private fun assignTierByHash(id: Int, name: String): HeroTier {
        val hash = abs(id * 31 + name.hashCode()) % 100
        return when {
            hash >= 88 -> HeroTier.SS
            hash >= 72 -> HeroTier.S
            hash >= 52 -> HeroTier.A
            hash >= 32 -> HeroTier.B
            hash >= 15 -> HeroTier.C
            else       -> HeroTier.D
        }
    }

    /**
     * Computes the overall score. When real rank data is available the score
     * is derived from win rate and ban rate; otherwise falls back to
     * tier-based estimation.
     */
    private fun computeOverallScore(heroId: Int, tier: HeroTier, rankData: HeroRankData?): Float {
        if (rankData != null) {
            // Real data: winRate contributes most, banRate adds "meta threat" weight
            val winComponent = (rankData.winRate * 1000.0).toFloat()
            val banComponent = (rankData.banRate.coerceAtMost(1.0) * 500.0).toFloat()
            return (winComponent + banComponent).coerceIn(100f, 1500f)
        }
        // Fallback: tier-based estimation
        val base = when (tier) {
            HeroTier.SS -> 1150f
            HeroTier.S  ->  950f
            HeroTier.A  ->  750f
            HeroTier.B  ->  550f
            HeroTier.C  ->  350f
            HeroTier.D  ->  150f
        }
        val rng = Random(abs(heroId * 1337L))
        return (base + rng.nextFloat() * 200f).coerceIn(100f, 1500f)
    }

    private fun generateMatchupScore(heroId: Int, relId: Int): Float {
        val rng = Random(abs(heroId * 1000L + relId))
        return (rng.nextFloat() * 7f).coerceIn(0.1f, 7.0f)
    }

    private fun parseLanes(laning: List<String>): List<HeroLane> {
        val combined = laning.joinToString(",").lowercase()
        val result = mutableListOf<HeroLane>()
        if (combined.contains("gold")) result.add(HeroLane.GOLD_LANE)
        if (combined.contains("exp")) result.add(HeroLane.EXP_LANE)
        if (combined.contains("mid")) result.add(HeroLane.MID_LANE)
        if (combined.contains("jungle") || combined.contains("jng")) result.add(HeroLane.JUNGLE)
        if (combined.contains("roam") || combined.contains("support")) result.add(HeroLane.ROAM)
        return result.ifEmpty { listOf(HeroLane.MID_LANE) }
    }
}
