package com.example.metaforge.data.remote

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.metaforge.data.local.datastore.DraftPreferences
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.flow.first
import kotlinx.datetime.Clock

class HeroMetaFetcher(
    private val httpClient: HttpClient,
    private val dataStore: DataStore<Preferences>,
    private val draftPrefs: DraftPreferences
) {
    companion object {
        private const val HERO_META_URL =
            "https://raw.githubusercontent.com/p3hndrx/MLBB-API/main/v1/hero-meta-final.json"
        private val CACHED_JSON_KEY = stringPreferencesKey("hero_meta_json_cache")
        /** Cooldown between network fetches. Re-uses the cached JSON within. */
        private const val FETCH_THROTTLE_MS = 15L * 60 * 1000   // 15 minutes
    }

    /**
     * Returns the hero-meta JSON. On a fresh install or when the last
     * successful fetch is older than 6 hours, hits the network and writes
     * a new "last fetch" timestamp. Otherwise serves the cached payload —
     * this is what stops the home label from staying on "—" while also
     * preventing spam.
     */
    suspend fun fetchJson(): String {
        val cached = dataStore.data.first()[CACHED_JSON_KEY]
        val lastAt = draftPrefs.getLastFetchedAt().first()
        val now = Clock.System.now().toEpochMilliseconds()
        val isStale = lastAt == null || (now - lastAt) >= FETCH_THROTTLE_MS

        // Fast path: cache is still fresh — skip the network entirely.
        if (!isStale && !cached.isNullOrBlank()) return cached

        return try {
            val json = httpClient.get(HERO_META_URL).bodyAsText()
            if (json.isNotBlank()) {
                dataStore.edit { it[CACHED_JSON_KEY] = json }
                draftPrefs.saveLastFetchedAt(now)
            }
            json
        } catch (e: Exception) {
            cached ?: throw Exception("No hero data available. Please connect to the internet and restart the app.")
        }
    }
}
