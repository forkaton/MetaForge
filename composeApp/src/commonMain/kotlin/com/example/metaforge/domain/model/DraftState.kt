package com.example.metaforge.domain.model

data class TurnAction(val isAlly: Boolean, val isBan: Boolean, val slot: Int)

data class DraftState(
    val allySlots: List<Hero?> = List(5) { null },
    val enemySlots: List<Hero?> = List(5) { null },
    val allyBans: List<Hero?> = List(5) { null },
    val enemyBans: List<Hero?> = List(5) { null },
    val currentTurn: Int = 0,
    val isUserFirstPick: Boolean = true
) {
    val isReadyToAnalyze: Boolean
        get() = allySlots.any { it != null } && enemySlots.any { it != null }

    val isBanPhaseComplete: Boolean
        get() = currentTurn >= 10

    val isComplete: Boolean
        get() = currentTurn >= 20

    val allyCount: Int get() = allySlots.count { it != null }
    val enemyCount: Int get() = enemySlots.count { it != null }

    companion object {
        // Snake pick order: 1-2-2-2-2-1 (Blue picks first overall)
        fun buildTurnSequence(isUserFirstPick: Boolean): List<TurnAction> = buildList {
            // Ban phase: alternating, Blue/first-pick bans first
            for (i in 0..4) {
                if (isUserFirstPick) {
                    add(TurnAction(isAlly = true,  isBan = true, slot = i))
                    add(TurnAction(isAlly = false, isBan = true, slot = i))
                } else {
                    add(TurnAction(isAlly = false, isBan = true, slot = i))
                    add(TurnAction(isAlly = true,  isBan = true, slot = i))
                }
            }
            // Pick phase snake: B1, R1R2, B2B3, R3R4, B4B5, R5
            if (isUserFirstPick) {
                // ally = Blue (picks first)
                add(TurnAction(true,  false, 0)) // B1
                add(TurnAction(false, false, 0)) // R1
                add(TurnAction(false, false, 1)) // R2
                add(TurnAction(true,  false, 1)) // B2
                add(TurnAction(true,  false, 2)) // B3
                add(TurnAction(false, false, 2)) // R3
                add(TurnAction(false, false, 3)) // R4
                add(TurnAction(true,  false, 3)) // B4
                add(TurnAction(true,  false, 4)) // B5
                add(TurnAction(false, false, 4)) // R5
            } else {
                // ally = Red (picks second)
                add(TurnAction(false, false, 0)) // B1 = enemy
                add(TurnAction(true,  false, 0)) // R1 = ally
                add(TurnAction(true,  false, 1)) // R2 = ally
                add(TurnAction(false, false, 1)) // B2 = enemy
                add(TurnAction(false, false, 2)) // B3 = enemy
                add(TurnAction(true,  false, 2)) // R3 = ally
                add(TurnAction(true,  false, 3)) // R4 = ally
                add(TurnAction(false, false, 3)) // B4 = enemy
                add(TurnAction(false, false, 4)) // B5 = enemy
                add(TurnAction(true,  false, 4)) // R5 = ally
            }
        }
    }

    private fun turnSequence(): List<TurnAction> = buildTurnSequence(isUserFirstPick)

    fun currentAction(): TurnAction? = if (currentTurn < 20) turnSequence().getOrNull(currentTurn) else null

    fun isCurrentSlot(index: Int, isAlly: Boolean, isBan: Boolean): Boolean {
        val action = currentAction() ?: return false
        return action.isBan == isBan && action.isAlly == isAlly && action.slot == index
    }

    fun isAllyTurn(): Boolean = currentAction()?.isAlly ?: false

    fun getAllPickedAndBannedHeroes(): List<Hero> =
        (allySlots + enemySlots + allyBans + enemyBans).filterNotNull()

    fun getAllPickedHeroes(): List<Hero> = (allySlots + enemySlots).filterNotNull()

    // Pick — advances turn
    fun pickAlly(index: Int, hero: Hero): DraftState {
        val slots = allySlots.toMutableList().also { it[index] = hero }
        return copy(allySlots = slots, currentTurn = minOf(currentTurn + 1, 20))
    }

    fun pickEnemy(index: Int, hero: Hero): DraftState {
        val slots = enemySlots.toMutableList().also { it[index] = hero }
        return copy(enemySlots = slots, currentTurn = minOf(currentTurn + 1, 20))
    }

    // Remove — rewinds turn to that slot's position in the sequence
    fun removeAlly(index: Int): DraftState {
        val slots = allySlots.toMutableList().also { it[index] = null }
        val pos = turnSequence().indexOfFirst { !it.isBan && it.isAlly && it.slot == index }
        return copy(allySlots = slots, currentTurn = if (pos in 0 until currentTurn) pos else currentTurn)
    }

    fun removeEnemy(index: Int): DraftState {
        val slots = enemySlots.toMutableList().also { it[index] = null }
        val pos = turnSequence().indexOfFirst { !it.isBan && !it.isAlly && it.slot == index }
        return copy(enemySlots = slots, currentTurn = if (pos in 0 until currentTurn) pos else currentTurn)
    }

    // Ban — advances turn
    fun banAlly(index: Int, hero: Hero): DraftState {
        val bans = allyBans.toMutableList().also { it[index] = hero }
        return copy(allyBans = bans, currentTurn = minOf(currentTurn + 1, 20))
    }

    fun banEnemy(index: Int, hero: Hero): DraftState {
        val bans = enemyBans.toMutableList().also { it[index] = hero }
        return copy(enemyBans = bans, currentTurn = minOf(currentTurn + 1, 20))
    }

    // Remove ban — rewinds turn
    fun removeAllyBan(index: Int): DraftState {
        val bans = allyBans.toMutableList().also { it[index] = null }
        val pos = turnSequence().indexOfFirst { it.isBan && it.isAlly && it.slot == index }
        return copy(allyBans = bans, currentTurn = if (pos in 0 until currentTurn) pos else currentTurn)
    }

    fun removeEnemyBan(index: Int): DraftState {
        val bans = enemyBans.toMutableList().also { it[index] = null }
        val pos = turnSequence().indexOfFirst { it.isBan && !it.isAlly && it.slot == index }
        return copy(enemyBans = bans, currentTurn = if (pos in 0 until currentTurn) pos else currentTurn)
    }

    fun resetAll(): DraftState = DraftState(isUserFirstPick = isUserFirstPick)

    fun getTurnMessage(): String {
        val action = currentAction()
        return when {
            isComplete -> "Draft Complete! Ready for battle."
            !isBanPhaseComplete && action != null -> {
                val team = if (action.isAlly) "BLUE" else "RED"
                "Ban Phase — $team banning (${currentTurn + 1}/10)"
            }
            action != null -> {
                val team = if (action.isAlly) "BLUE" else "RED"
                "Pick Phase — $team picking (${currentTurn - 9}/10)"
            }
            else -> "Draft Complete!"
        }
    }
}
