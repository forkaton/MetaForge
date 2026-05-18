package com.example.metaforge.domain.model

data class SynergyResult(
    val overallScore: Int,
    val crowdControl: Int,
    val burstDamage: Int,
    val sustainability: Int,
    val mobility: Int,
    val recommendedHeroes: List<HeroRecommendation>,
    val winCondition: String
)

data class HeroRecommendation(
    val heroName: String,
    val role: String,
    val reason: String,
    val counterScore: Int,
    val warning: String? = null  // ← tambah ini, dipakai DraftScreen
)