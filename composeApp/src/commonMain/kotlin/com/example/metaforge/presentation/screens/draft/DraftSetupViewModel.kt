package com.example.metaforge.presentation.screens.draft

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

    fun setPartySize(size: Int) = _uiState.update { it.copy(partySize = size) }

    fun setFirstPick(isFirst: Boolean) =
        _uiState.update { it.copy(isFirstPick = isFirst) }

    fun setPickPosition(position: Int) =
        _uiState.update { it.copy(pickPosition = position) }

    fun setPreferredRole(role: HeroLane) =
        _uiState.update { it.copy(preferredRole = role) }
}
