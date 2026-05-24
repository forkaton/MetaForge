package com.example.metaforge.data.local

import com.example.metaforge.domain.model.Hero
import com.example.metaforge.domain.model.HeroLane

object HeroDataSource {
    val allHeroes = listOf(
        Hero(1, "Khufra", HeroLane.ROAM, "Tank", "", "", "Crowd Control"),
        Hero(6, "Chou", HeroLane.EXP_LANE, "Fighter", "", "", "Control"),
        Hero(11, "Fanny", HeroLane.JUNGLE, "Assassin", "", "", "Mobility"),
        Hero(16, "Kagura", HeroLane.MID_LANE, "Mage", "", "", "Burst"),
        Hero(21, "Beatrix", HeroLane.GOLD_LANE, "Marksman", "", "", "Burst")
    )
    fun getById(id: Int) = allHeroes.find { it.id == id }
}