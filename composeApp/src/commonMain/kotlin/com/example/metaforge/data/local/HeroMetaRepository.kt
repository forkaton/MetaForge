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

    private val TIER_MAP: Map<String, HeroTier> = mapOf(
        // SS Tier
        "Harley" to HeroTier.SS, "Gloo" to HeroTier.SS, "Sora" to HeroTier.SS,
        "Suyou" to HeroTier.SS, "Marcel" to HeroTier.SS, "Guinevere" to HeroTier.SS,
        "Fanny" to HeroTier.SS, "Ling" to HeroTier.SS, "Chou" to HeroTier.SS,
        "Joy" to HeroTier.SS, "Nolan" to HeroTier.SS, "Lancelot" to HeroTier.SS,
        // S Tier
        "Sun" to HeroTier.S, "Helcurt" to HeroTier.S, "Zhuxin" to HeroTier.S,
        "Zetian" to HeroTier.S, "Minsitthar" to HeroTier.S, "Freya" to HeroTier.S,
        "Hilda" to HeroTier.S, "Estes" to HeroTier.S, "Paquito" to HeroTier.S,
        "Aulus" to HeroTier.S, "Gusion" to HeroTier.S, "Benedetta" to HeroTier.S,
        "Kadita" to HeroTier.S, "Floryn" to HeroTier.S, "Atlas" to HeroTier.S,
        "Valentina" to HeroTier.S, "Beatrix" to HeroTier.S, "Mathilda" to HeroTier.S,
        "Novaria" to HeroTier.S, "Yi Sun-shin" to HeroTier.S, "Hayabusa" to HeroTier.S,
        "Silvanna" to HeroTier.S, "Julian" to HeroTier.S,
        // A Tier
        "Leomord" to HeroTier.A, "Fredrinn" to HeroTier.A, "Saber" to HeroTier.A,
        "Kaja" to HeroTier.A, "Alice" to HeroTier.A, "Hanabi" to HeroTier.A,
        "Irithel" to HeroTier.A, "Claude" to HeroTier.A, "Brody" to HeroTier.A,
        "Khufra" to HeroTier.A, "Esmeralda" to HeroTier.A, "Uranus" to HeroTier.A,
        "Barats" to HeroTier.A, "Baxia" to HeroTier.A, "Melissa" to HeroTier.A,
        "Xavier" to HeroTier.A, "Yin" to HeroTier.A, "Arlott" to HeroTier.A,
        "Cici" to HeroTier.A, "Lunox" to HeroTier.A, "Wanwan" to HeroTier.A,
        "Kimmy" to HeroTier.A, "Yu Zhong" to HeroTier.A, "Popol and Kupa" to HeroTier.A,
        "Chip" to HeroTier.A, "Lukas" to HeroTier.A,
        // B Tier
        "Miya" to HeroTier.B, "Tigreal" to HeroTier.B, "Franco" to HeroTier.B,
        "Grock" to HeroTier.B, "Harith" to HeroTier.B, "Diggie" to HeroTier.B,
        "Lesley" to HeroTier.B, "Angela" to HeroTier.B, "Karrie" to HeroTier.B,
        "Belerick" to HeroTier.B, "Phoveus" to HeroTier.B, "Gord" to HeroTier.B,
        "Aamon" to HeroTier.B, "Ruby" to HeroTier.B, "Martis" to HeroTier.B,
        "Thamuz" to HeroTier.B, "Selena" to HeroTier.B, "Lapu-Lapu" to HeroTier.B,
        "Cecilion" to HeroTier.B, "Carmilla" to HeroTier.B, "Lylia" to HeroTier.B,
        "Edith" to HeroTier.B, "Bruno" to HeroTier.B, "Moskov" to HeroTier.B,
        "Faramis" to HeroTier.B, "Kagura" to HeroTier.B, "Pharsa" to HeroTier.B,
        "Chang'e" to HeroTier.B, "Natan" to HeroTier.B, "Luo Yi" to HeroTier.B,
        "Popol & Kupa" to HeroTier.B,
        // C Tier
        "Balmond" to HeroTier.C, "Natalia" to HeroTier.C, "Johnson" to HeroTier.C,
        "Cyclops" to HeroTier.C, "Lolita" to HeroTier.C, "Hylos" to HeroTier.C,
        "Zhask" to HeroTier.C, "Aurora" to HeroTier.C, "Roger" to HeroTier.C,
        "Vexana" to HeroTier.C, "Alpha" to HeroTier.C, "Aldous" to HeroTier.C,
        "Odette" to HeroTier.C, "Vale" to HeroTier.C, "Valir" to HeroTier.C,
        "Gatotkaca" to HeroTier.C, "Jawhead" to HeroTier.C, "Badang" to HeroTier.C,
        "Masha" to HeroTier.C, "Eudora" to HeroTier.C, "Terizla" to HeroTier.C,
        "X.Borg" to HeroTier.C, "Karina" to HeroTier.C, "Minotaur" to HeroTier.C,
        "Argus" to HeroTier.C, "Hanzo" to HeroTier.C,
        // D Tier
        "Layla" to HeroTier.D, "Alucard" to HeroTier.D, "Zilong" to HeroTier.D,
        "Nana" to HeroTier.D, "Rafaela" to HeroTier.D, "Bane" to HeroTier.D,
        "Clint" to HeroTier.D
    )

    private val RANKS = listOf("Epic", "Legend", "Mythic", "Mythical Honor", "Mythical Glory+")

    private val json = Json { ignoreUnknownKeys = true; isLenient = true; coerceInputValues = true }

    fun parseAndBuild(jsonString: String): List<HeroMetaEntry> {
        val root = json.decodeFromString<HeroMetaJsonRoot>(jsonString)
        val validEntries = root.data.filter {
            it.heroName.isNotBlank() && it.heroName != "None" &&
            it.mlid.isNotBlank() && it.mlid != "0"
        }

        // Build tier lookup map by id
        val idToTier = mutableMapOf<Int, HeroTier>()
        validEntries.forEach { entry ->
            val id = entry.mlid.toIntOrNull() ?: return@forEach
            idToTier[id] = TIER_MAP[entry.heroName] ?: assignTierByHash(id, entry.heroName)
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
            val overallScore = computeOverallScore(id, tier)

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
                synergies = synergies
            )
        }.sortedWith(
            compareBy<HeroMetaEntry> { it.tier.ordinal }.thenByDescending { it.overallScore }
        )
    }

    fun generateRankStats(heroId: Int, tier: HeroTier, timePeriod: TimePeriod): List<RankStats> {
        return RANKS.map { rank ->
            val seed = abs(heroId * 7919L + rank.hashCode() * 31L + timePeriod.ordinal * 997L)
            val rng = Random(seed)
            val (baseWin, basePick, baseBan) = tierBaseStats(tier)
            val rankOffset = rankOffset(rank)
            RankStats(
                rankName = rank,
                winRate = (baseWin + rankOffset + (rng.nextFloat() - 0.5f) * 2.5f).coerceIn(41f, 62f),
                pickRate = (basePick * (0.8f + rng.nextFloat() * 0.4f)).coerceIn(0.2f, 38f),
                banRate = (baseBan * (0.8f + rng.nextFloat() * 0.4f)).coerceIn(0.1f, 88f)
            )
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

    private fun computeOverallScore(heroId: Int, tier: HeroTier): Float {
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
