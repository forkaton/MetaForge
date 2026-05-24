package com.example.metaforge.domain.model

data class Hero(
    val id: Int = 0,
    val name: String,
    val lane: HeroLane,
    val role: String = "",
    val imageUrl: String = "",
    val iconUrl: String = "",
    val specialty: String = ""
)