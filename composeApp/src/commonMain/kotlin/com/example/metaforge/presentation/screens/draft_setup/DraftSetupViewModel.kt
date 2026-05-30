package com.example.metaforge.presentation.screens.draft_setup

import androidx.lifecycle.ViewModel
import com.example.metaforge.domain.model.HeroLane
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class DraftSetupViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(DraftSetupUiState())
    val uiState: StateFlow<DraftSetupUiState> = _uiState.asStateFlow()

    fun setRank(rank: String) = _uiState.update { it.copy(rank = rank) }

    /**
     * Switches party size and trims (never expands) the multi-select sets.
     *  - Squad (5): clears both sets — recommendations cover all 5 ally slots.
     *  - Solo/Duo/Trio: keeps existing selections, trims to [size]. We do NOT
     *    auto-seed — the user always makes the conscious choice.
     */
    fun setPartySize(size: Int) = _uiState.update { state ->
        if (size >= 5) {
            state.copy(partySize = 5, pickPositions = emptySet(), preferredLanes = emptySet())
        } else {
            state.copy(
                partySize = size,
                pickPositions = state.pickPositions.take(size).toSet(),
                preferredLanes = state.preferredLanes.take(size).toSet()
            )
        }
    }

    fun setFirstPick(isFirst: Boolean) = _uiState.update { it.copy(isFirstPick = isFirst) }

    /** Toggles a pick position; caps total selected at [DraftSetupUiState.partySize]. */
    fun togglePickPosition(position: Int) = _uiState.update { state ->
        val current = state.pickPositions
        val next = when {
            position in current -> current - position
            current.size >= state.partySize -> current
            else -> current + position
        }
        state.copy(pickPositions = next)
    }

    /** Toggles a preferred lane; caps total selected at [DraftSetupUiState.partySize]. */
    fun togglePreferredLane(lane: HeroLane) = _uiState.update { state ->
        val current = state.preferredLanes
        val next = when {
            lane in current -> current - lane
            current.size >= state.partySize -> current
            else -> current + lane
        }
        state.copy(preferredLanes = next)
    }
}
