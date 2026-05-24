package com.example.metaforge.data.local.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ThemePreferences(private val dataStore: DataStore<Preferences>) {
    companion object {
        private val IS_DARK_KEY = booleanPreferencesKey("is_dark_theme")
    }

    fun isDarkTheme(): Flow<Boolean> = dataStore.data.map { it[IS_DARK_KEY] ?: true }

    suspend fun setDarkTheme(isDark: Boolean) {
        dataStore.edit { it[IS_DARK_KEY] = isDark }
    }
}
