package com.example.metaforge.data.local.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

open class DraftPreferences(
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        val LAST_DRAFT_KEY = stringPreferencesKey("last_draft")
        val LAST_FETCH_TS_KEY = longPreferencesKey("last_fetch_ts")
        val RANK_DATA_JSON_KEY = stringPreferencesKey("rank_data_json_cache")
    }

    open suspend fun saveLastDraft(draftData: String) {
        dataStore.edit { prefs ->
            prefs[LAST_DRAFT_KEY] = draftData
        }
    }

    open fun getLastDraft(): Flow<String?> = dataStore.data.map { prefs ->
        prefs[LAST_DRAFT_KEY]
    }

    open suspend fun clearDraft() {
        dataStore.edit { prefs ->
            prefs.remove(LAST_DRAFT_KEY)
        }
    }

    /** Epoch millis of the last successful online hero-data sync. */
    open suspend fun saveLastFetchedAt(epochMillis: Long) {
        dataStore.edit { prefs ->
            prefs[LAST_FETCH_TS_KEY] = epochMillis
        }
    }

    open fun getLastFetchedAt(): Flow<Long?> = dataStore.data.map { prefs ->
        prefs[LAST_FETCH_TS_KEY]
    }

    /** Cache the raw rank API JSON so it survives process death. */
    open suspend fun saveRankDataJson(json: String) {
        dataStore.edit { prefs ->
            prefs[RANK_DATA_JSON_KEY] = json
        }
    }

    open fun getRankDataJson(): Flow<String?> = dataStore.data.map { prefs ->
        prefs[RANK_DATA_JSON_KEY]
    }
}