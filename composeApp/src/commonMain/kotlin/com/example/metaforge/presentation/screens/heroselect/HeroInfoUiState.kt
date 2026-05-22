package com.example.metaforge.presentation.screens.heroselect

data class HeroInfoUiState(
    val winRate: String = "54.1%",
    val pickRate: String = "18.5%",
    val banRate: String = "62.3%",
    val strongAgainst: List<String> = listOf("Layla", "Miya", "Pharsa"),
    val weakAgainst: List<String> = listOf("Khufra", "Chou", "Kaja"),
    val synergy: List<String> = listOf("Angela", "Tigreal", "Atlas")
)
