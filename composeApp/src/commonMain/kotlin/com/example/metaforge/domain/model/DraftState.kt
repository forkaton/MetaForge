package com.example.metaforge.domain.model

data class DraftState(
    val allySlots: List<Hero?> = List(5) { null },
    val enemySlots: List<Hero?> = List(5) { null },
    val allyBans: List<Hero?> = List(5) { null },
    val enemyBans: List<Hero?> = List(5) { null },
    val isUserFirstPick: Boolean = true
) {
    // Ban phase: free selection, count-based
    val totalAllyBans = allyBans.count { it != null }
    val totalEnemyBans = enemyBans.count { it != null }
    val isBanPhaseComplete = totalAllyBans >= 5 && totalEnemyBans >= 5

    val allyCount = allySlots.count { it != null }
    val enemyCount = enemySlots.count { it != null }
    val isComplete = isBanPhaseComplete && allyCount >= 5 && enemyCount >= 5
    val isReadyToAnalyze = allySlots.any { it != null } && enemySlots.any { it != null }

    companion object {
        // Snake pick waves: 1-2-2-2-2-1, (isAlly, slotIndex)
        fun pickWaves(isUserFirstPick: Boolean): List<List<Pair<Boolean, Int>>> =
            if (isUserFirstPick) listOf(
                listOf(true to 0),
                listOf(false to 0, false to 1),
                listOf(true to 1, true to 2),
                listOf(false to 2, false to 3),
                listOf(true to 3, true to 4),
                listOf(false to 4)
            ) else listOf(
                listOf(false to 0),
                listOf(true to 0, true to 1),
                listOf(false to 1, false to 2),
                listOf(true to 2, true to 3),
                listOf(false to 3, false to 4),
                listOf(true to 4)
            )
    }

    // First incomplete wave index, -1 if ban not done, waves.size if all done
    fun currentPickWave(): Int {
        if (!isBanPhaseComplete) return -1
        val waves = pickWaves(isUserFirstPick)
        waves.forEachIndexed { idx, wave ->
            val complete = wave.all { (isAlly, slot) ->
                if (isAlly) allySlots.getOrNull(slot) != null
                else enemySlots.getOrNull(slot) != null
            }
            if (!complete) return idx
        }
        return waves.size
    }

    fun activePickSlots(): List<Pair<Boolean, Int>> {
        val waves = pickWaves(isUserFirstPick)
        val wave = currentPickWave()
        return if (wave in waves.indices) waves[wave] else emptyList()
    }

    fun isCurrentPickSlot(index: Int, isAlly: Boolean): Boolean =
        activePickSlots().any { (a, s) -> a == isAlly && s == index }

    fun isAllyPickWave(): Boolean = activePickSlots().any { (isAlly, _) -> isAlly }

    fun canBan(index: Int, isAlly: Boolean): Boolean {
        if (isBanPhaseComplete) return false
        return if (isAlly) allyBans.getOrNull(index) == null
        else enemyBans.getOrNull(index) == null
    }

    fun getAllPickedAndBannedHeroes(): List<Hero> =
        (allySlots + enemySlots + allyBans + enemyBans).filterNotNull()

    fun getAllPickedHeroes(): List<Hero> = (allySlots + enemySlots).filterNotNull()

    fun pickAlly(index: Int, hero: Hero) = copy(allySlots = allySlots.replace(index, hero))
    fun pickEnemy(index: Int, hero: Hero) = copy(enemySlots = enemySlots.replace(index, hero))
    fun removeAlly(index: Int) = copy(allySlots = allySlots.replace(index, null))
    fun removeEnemy(index: Int) = copy(enemySlots = enemySlots.replace(index, null))

    fun banAlly(index: Int, hero: Hero) = copy(allyBans = allyBans.replace(index, hero))
    fun banEnemy(index: Int, hero: Hero) = copy(enemyBans = enemyBans.replace(index, hero))
    fun removeAllyBan(index: Int) = copy(allyBans = allyBans.replace(index, null))
    fun removeEnemyBan(index: Int) = copy(enemyBans = enemyBans.replace(index, null))

    fun resetAll() = DraftState(isUserFirstPick = isUserFirstPick)

    fun getTurnMessage(): String = when {
        isComplete -> "Draft Complete! Ready for battle."
        !isBanPhaseComplete -> "Ban Phase — ${totalAllyBans + totalEnemyBans}/10 bans"
        else -> {
            val slots = activePickSlots()
            val team = if (slots.any { it.first }) "BLUE" else "RED"
            "Pick Phase — $team picking (Wave ${currentPickWave() + 1}/6)"
        }
    }

    private fun <T> List<T>.replace(index: Int, value: T): List<T> =
        toMutableList().also { if (index in indices) it[index] = value }
}
