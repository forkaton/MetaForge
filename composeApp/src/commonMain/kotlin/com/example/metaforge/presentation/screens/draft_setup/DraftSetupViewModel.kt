package com.example.metaforge.presentation.screens.draft_setup

import androidx.lifecycle.ViewModel
import com.example.metaforge.domain.model.HeroLane
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * ViewModel untuk Draft Setup Screen
 * 
 * Menangani user's draft configuration:
 * - Select rank tier
 * - Select party size
 * - Select first/second pick
 * - Set pick order
 * - Select preferred lane
 */
class DraftSetupViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(DraftSetupUiState())
    val uiState: StateFlow<DraftSetupUiState> = _uiState.asStateFlow()

    fun setRank(rank: String) { 
        _uiState.update { it.copy(rank = rank) } 
    }
    
    fun setPartySize(size: Int) { 
        _uiState.update { it.copy(partySize = size) } 
    }
    
    fun setFirstPick(isFirstPick: Boolean) { 
        _uiState.update { it.copy(isFirstPick = isFirstPick) } 
    }
    
    fun setPickPosition(position: Int) { 
        _uiState.update { it.copy(pickPosition = position) } 
    }
    
    fun setPreferredLane(lane: HeroLane) { 
        _uiState.update { it.copy(preferredLane = lane) } 
    }
}