package com.example.metaforge.core.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.example.metaforge.core.connectivity.AndroidConnectivityObserver
import com.example.metaforge.core.connectivity.ConnectivityObserver
import com.example.metaforge.core.util.DatabaseDriverFactory
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val androidModule = module {
    // Database driver
    single { DatabaseDriverFactory(androidContext()) }

    // DataStore
    single<DataStore<Preferences>> {
        val context = androidContext()
        @Suppress("DEPRECATION")
        context.dataStore
    }

    // Connectivity observer
    single<ConnectivityObserver> { AndroidConnectivityObserver(androidContext()) }
}

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "metaforge_preferences")
