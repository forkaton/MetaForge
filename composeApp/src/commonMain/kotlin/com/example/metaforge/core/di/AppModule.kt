package com.example.metaforge.core.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.example.metaforge.core.network.HttpClientFactory
import com.example.metaforge.core.util.DatabaseDriverFactory
import com.example.metaforge.data.local.HeroMetaService
import com.example.metaforge.data.remote.HeroMetaFetcher
import com.example.metaforge.data.local.MetaForgeDatabaseWrapper
import com.example.metaforge.data.local.datastore.DraftPreferences
import com.example.metaforge.data.local.datastore.ThemePreferences
import com.example.metaforge.data.local.datastore.UserPreferences
import com.example.metaforge.data.remote.api.MLBBApiService
import com.example.metaforge.data.remote.api.MlbbRankRecord
import com.example.metaforge.data.repository.DraftRepositoryImpl
import com.example.metaforge.domain.model.HeroRankData
import com.example.metaforge.domain.repository.DraftRepository
import com.example.metaforge.presentation.screens.counterpick.CounterPickViewModel
import com.example.metaforge.presentation.screens.draft_arena.DraftViewModel
import com.example.metaforge.presentation.screens.draft_setup.DraftSetupViewModel
import com.example.metaforge.presentation.screens.hero_encyclopedia.TierListViewModel
import com.example.metaforge.presentation.screens.hero_select.HeroSelectViewModel
import kotlinx.coroutines.flow.first
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

private val rankJson = Json { ignoreUnknownKeys = true; isLenient = true; coerceInputValues = true }

val appModule = module {
    // Network
    single { HttpClientFactory.create() }
    single { MLBBApiService(get()) }

    // Database
    single { MetaForgeDatabaseWrapper(get<DatabaseDriverFactory>().createDriver()) }

    // DataStore preferences
    single { UserPreferences(get<DataStore<Preferences>>()) }
    single { DraftPreferences(get<DataStore<Preferences>>()) }
    single { ThemePreferences(get<DataStore<Preferences>>()) }

    // Fetches hero meta JSON from GitHub, caches in DataStore for offline use.
    // Also stamps DraftPreferences.lastFetchedAt on every successful network call.
    single { HeroMetaFetcher(get(), get<DataStore<Preferences>>(), get<DraftPreferences>()) }
    single<suspend () -> String> { { get<HeroMetaFetcher>().fetchJson() } }

    // Rank data loader — reads the cached rank JSON from DataStore and parses
    // it into a Map<heroId, HeroRankData>.  Falls back to an empty map on
    // first install before any sync has run.
    single<suspend () -> Map<Int, HeroRankData>>(named("rankDataLoader")) {
        {
            val prefs = get<DraftPreferences>()
            val cached = prefs.getRankDataJson().first()
            if (cached.isNullOrBlank()) {
                // Try fetching directly from API for first run
                try {
                    val api = get<MLBBApiService>()
                    val response = api.fetchHeroesRank()
                    val records = response.data?.records ?: emptyList()
                    val json = rankJson.encodeToString(
                        ListSerializer(MlbbRankRecord.serializer()), records
                    )
                    prefs.saveRankDataJson(json)
                    parseRankRecords(records)
                } catch (_: Exception) {
                    emptyMap()
                }
            } else {
                try {
                    val records = rankJson.decodeFromString(
                        ListSerializer(MlbbRankRecord.serializer()), cached
                    )
                    parseRankRecords(records)
                } catch (_: Exception) {
                    emptyMap()
                }
            }
        }
    }

    // Hero meta service — caches parsed heroes in memory for the session
    single {
        HeroMetaService(
            jsonLoader = get<suspend () -> String>(),
            rankDataLoader = get<suspend () -> Map<Int, HeroRankData>>(named("rankDataLoader"))
        )
    }

    // Repository (single = stateful draft state)
    single<DraftRepository> {
        DraftRepositoryImpl(
            prefs    = getOrNull<DraftPreferences>(),
            database = get(),
            api      = get()
        )
    }

    // ViewModels
    factory { DraftSetupViewModel() }
    factory { DraftViewModel(get<DraftRepository>(), get<HeroMetaService>()) }
    factory { HeroSelectViewModel(get<DraftRepository>(), get<HeroMetaService>()) }
    factory { CounterPickViewModel(get<DraftRepository>()) }
    factory {
        TierListViewModel(
            jsonLoader = get<suspend () -> String>(),
            rankDataLoader = get<suspend () -> Map<Int, HeroRankData>>(named("rankDataLoader"))
        )
    }
}

/** Helper to convert API rank records into a hero-id-keyed map. */
private fun parseRankRecords(records: List<MlbbRankRecord>): Map<Int, HeroRankData> {
    return records.mapNotNull { rec ->
        val data = rec.data ?: return@mapNotNull null
        val heroId = data.main_heroid ?: return@mapNotNull null
        val heroName = data.main_hero?.data?.name ?: "Unknown"
        heroId to HeroRankData(
            heroId = heroId,
            heroName = heroName,
            winRate = data.main_hero_win_rate ?: 0.0,
            pickRate = data.main_hero_appearance_rate ?: 0.0,
            banRate = data.main_hero_ban_rate ?: 0.0
        )
    }.toMap()
}

fun initKoin(platformModules: List<Module>, appDeclaration: KoinAppDeclaration = {}) {
    startKoin {
        appDeclaration()
        modules(appModule, *platformModules.toTypedArray())
    }
}
