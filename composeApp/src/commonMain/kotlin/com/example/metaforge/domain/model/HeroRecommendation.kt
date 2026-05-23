package com.example.metaforge.domain.model

data class HeroRecommendation(
    val hero: Hero,
    val reason: String,
    val score: Int = 0
)
