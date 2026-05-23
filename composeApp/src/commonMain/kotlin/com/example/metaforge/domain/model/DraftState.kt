package com.example.metaforge.domain.model

data class DraftState(
    val allySlots: List<Hero?> = List(5) { null },
    val enemySlots: List<Hero?> = List(5) { null },
    val allyBans: List<Hero?> = List(5) { null },
    val enemyBans: List<Hero?> = List(5) { null },
    val currentTurn: Int = 0 // 0-19 untuk urutan ban/pick
) {
    val isReadyToAnalyze: Boolean
        get() = allySlots.any { it != null } && enemySlots.any { it != null }

    val isBanPhaseComplete: Boolean
        get() = allyBans.count { it != null } >= 5 &&
                enemyBans.count { it != null } >= 5

    val allyCount: Int get() = allySlots.count { it != null }
    val enemyCount: Int get() = enemySlots.count { it != null }

    // Cek apakah slot pick sudah bisa diisi
    // Pick hanya bisa setelah ban phase selesai
    fun isPickUnlocked(index: Int, isAlly: Boolean, isUserFirstPick: Boolean): Boolean {
        if (!isBanPhaseComplete) return false
        return if (isAlly) {
            index <= allyCount
        } else {
            index <= enemyCount
        }
    }

    fun getAllPickedAndBannedHeroes(): List<Hero> =
        (allySlots + enemySlots + allyBans + enemyBans).filterNotNull()

    // Pick functions
    fun pickAlly(index: Int, hero: Hero): DraftState {
        val newSlots = allySlots.toMutableList()
        newSlots[index] = hero
        return copy(allySlots = newSlots)
    }

    fun pickEnemy(index: Int, hero: Hero): DraftState {
        val newSlots = enemySlots.toMutableList()
        newSlots[index] = hero
        return copy(enemySlots = newSlots)
    }

    fun removeAlly(index: Int): DraftState {
        val newSlots = allySlots.toMutableList()
        newSlots[index] = null
        return copy(allySlots = newSlots)
    }

    fun removeEnemy(index: Int): DraftState {
        val newSlots = enemySlots.toMutableList()
        newSlots[index] = null
        return copy(enemySlots = newSlots)
    }

    // Ban functions
    fun banAlly(index: Int, hero: Hero): DraftState {
        val newBans = allyBans.toMutableList()
        newBans[index] = hero
        return copy(allyBans = newBans)
    }

    fun banEnemy(index: Int, hero: Hero): DraftState {
        val newBans = enemyBans.toMutableList()
        newBans[index] = hero
        return copy(enemyBans = newBans)
    }

    fun removeAllyBan(index: Int): DraftState {
        val newBans = allyBans.toMutableList()
        newBans[index] = null
        return copy(allyBans = newBans)
    }

    fun removeEnemyBan(index: Int): DraftState {
        val newBans = enemyBans.toMutableList()
        newBans[index] = null
        return copy(enemyBans = newBans)
    }
    fun getTurnMessage(isUserFirstPick: Boolean): String {
        return when {
            !isBanPhaseComplete -> "Ban Phase: Please select heroes to ban."
            allyCount == 5 && enemyCount == 5 -> "Draft Complete! Ready for battle."
            else -> "Pick Phase: Please select your heroes."
        }
    }
    fun getAllPickedHeroes(): List<Hero> =
        (allySlots + enemySlots).filterNotNull()

}
