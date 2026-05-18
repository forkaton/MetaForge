package com.example.metaforge.presentation.screens.draft

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.metaforge.domain.model.HeroRecommendation
import com.example.metaforge.domain.repository.DraftRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class DraftViewModel(
    private val draftRepository: DraftRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<DraftUiState>(DraftUiState.Loading)
    val uiState: StateFlow<DraftUiState> = _uiState.asStateFlow()

    private var userPickPosition: Int = 1
    private var isUserFirstPick: Boolean = true
    private var preferredRole: String = "MARKSMAN"

    init { observeDraftState() }

    // Fungsi baru yang dipanggil AppNavHost
    fun setupDraft(pickPosition: Int, isFirstPick: Boolean, role: String) {
        userPickPosition = pickPosition
        isUserFirstPick = isFirstPick
        preferredRole = role
        updateTurnMessage()
    }

    private fun observeDraftState() {
        viewModelScope.launch {
            draftRepository.getDraftState()
                .catch { e ->
                    _uiState.value = DraftUiState.Error(e.message ?: "Terjadi kesalahan")
                }
                .collect { draftState ->
                    val currentReady = _uiState.value as? DraftUiState.Ready
                    _uiState.value = DraftUiState.Ready(
                        draftState = draftState,
                        turnMessage = currentReady?.turnMessage ?: "SET UP YOUR DRAFT",
                        isUserTurn = currentReady?.isUserTurn ?: false,
                        recommendation = currentReady?.recommendation
                    )
                }
        }
    }

    private fun updateTurnMessage() {
        val current = _uiState.value as? DraftUiState.Ready ?: return
        val banCount = current.draftState.allyBans.count { it != null } +
                current.draftState.enemyBans.count { it != null }
        val message = when {
            banCount < 10 -> "BAN PHASE — ${10 - banCount} bans remaining"
            else -> "PICK PHASE — Select your heroes"
        }
        _uiState.value = current.copy(
            turnMessage = message,
            isUserTurn = true,
            recommendation = HeroRecommendation(
                heroName = "Khufra",
                role = "Tank",
                reason = "Strong initiator vs current enemy lineup",
                counterScore = 85,
                warning = null
            )
        )
    }

    // Update signature removeHero — tambah isBan parameter
    fun removeHero(slotIndex: Int, isAlly: Boolean, isBan: Boolean = false) {
        viewModelScope.launch {
            draftRepository.removeHero(slotIndex, isAlly, isBan)
        }
        updateTurnMessage()
    }

    fun resetDraft() {
        viewModelScope.launch {
            draftRepository.resetDraft()
        }
    }

    fun saveDraft() {
        viewModelScope.launch {
            draftRepository.saveDraft()
        }
    }
}