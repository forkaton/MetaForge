package com.example.metaforge.core.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.example.metaforge.core.network.HttpClientFactory
import com.example.metaforge.core.util.DatabaseDriverFactory
import com.example.metaforge.data.local.HeroMetaService
import com.example.metaforge.data.local.MetaForgeDatabaseWrapper
import com.example.metaforge.data.local.datastore.DraftPreferences
import com.example.metaforge.data.local.datastore.ThemePreferences
import com.example.metaforge.data.local.datastore.UserPreferences
import com.example.metaforge.data.remote.api.MLBBApiService
import com.example.metaforge.data.repository.DraftRepositoryImpl
import com.example.metaforge.domain.repository.DraftRepository
import com.example.metaforge.presentation.screens.counterpick.CounterPickViewModel
import com.example.metaforge.presentation.screens.draft_arena.DraftViewModel
import com.example.metaforge.presentation.screens.draft_setup.DraftSetupViewModel
import com.example.metaforge.presentation.screens.hero_encyclopedia.TierListViewModel
import com.example.metaforge.presentation.screens.hero_select.HeroSelectViewModel
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

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

    // Hero meta service (loads hero_meta.json asset, caches in memory)
    single { HeroMetaService(get<suspend () -> String>()) }

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
    factory { TierListViewModel(get<suspend () -> String>()) }
}

fun initKoin(platformModules: List<Module>, appDeclaration: KoinAppDeclaration = {}) {
    startKoin {
        appDeclaration()
        modules(appModule, *platformModules.toTypedArray())
    }
}
