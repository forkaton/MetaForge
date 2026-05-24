package com.example.metaforge.data.remote

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.flow.first

class HeroMetaFetcher(
    private val httpClient: HttpClient,
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        private const val HERO_META_URL =
            "https://raw.githubusercontent.com/p3hndrx/MLBB-API/main/v1/hero-meta-final.json"
        private val CACHED_JSON_KEY = stringPreferencesKey("hero_meta_json_cache")
    }

    suspend fun fetchJson(): String {
        return try {
            val json = httpClient.get(HERO_META_URL).bodyAsText()
            if (json.isNotBlank()) {
                dataStore.edit { it[CACHED_JSON_KEY] = json }
            }
            json
        } catch (e: Exception) {
            val cached = dataStore.data.first()[CACHED_JSON_KEY]
            cached ?: throw Exception("No hero data available. Please connect to the internet and restart the app.")
        }
    }
}
