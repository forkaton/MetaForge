package com.example.metaforge.domain.model

enum class HeroLane(val displayName: String, val order: Int) {
    GOLD_LANE("Gold Lane", 1),
    MID_LANE("Mid Lane", 2),
    JUNGLE("Jungle", 3),
    ROAM("Roam", 4),
    EXP("Exp Lane", 5)
}

