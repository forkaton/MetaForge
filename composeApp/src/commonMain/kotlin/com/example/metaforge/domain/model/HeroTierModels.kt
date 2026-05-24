package com.example.metaforge.domain.model

enum class HeroTier(val label: String, val colorValue: Long, val description: String) {
    SS("SS", 0xFF9C27B0L, "God-tier picks that define the meta"),
    S("S", 0xFFF44336L, "Top-tier picks that dominate the meta"),
    A("A", 0xFFFF9800L, "Strong heroes that consistently perform well"),
    B("B", 0xFFFFEB3BL, "Balanced heroes with situational strengths"),
    C("C", 0xFF4CAF50L, "Niche heroes with specific use cases"),
    D("D", 0xFF2196F3L, "Underperforming in current meta")
}

data class HeroMatchupEntry(
    val id: Int,
    val name: String,
    val tier: HeroTier,
    val score: Float
)

data class HeroMetaEntry(
    val id: Int,
    val name: String,
    val heroClass: String,
    val lanes: List<HeroLane>,
    val speciality: List<String>,
    val portraitUrl: String,
    val tier: HeroTier,
    val overallScore: Float,
    val strongAgainst: List<HeroMatchupEntry>,
    val weakAgainst: List<HeroMatchupEntry>,
    val synergies: List<HeroMatchupEntry>
)

data class RankStats(
    val rankName: String,
    val winRate: Float,
    val pickRate: Float,
    val banRate: Float
)

enum class StatType { WIN_RATE, PICK_RATE, BAN_RATE }

enum class TimePeriod(val label: String) {
    ALL("ALL"),
    LAST_3_DAYS("Last 3 Days"),
    LAST_7_DAYS("Last 7 Days"),
    LAST_30_DAYS("Last 30 Days")
}
