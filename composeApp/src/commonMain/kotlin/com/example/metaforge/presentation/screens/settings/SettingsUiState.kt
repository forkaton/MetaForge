package com.example.metaforge.presentation.screens.settings

data class SettingsUiState(
    val appVersion: String = "1.0.0 Sprint 2",
    val heroCount: Int = 30,
    val aiEngine: String = "Gemini API"
)
