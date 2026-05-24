package com.example.metaforge.core.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.example.metaforge.core.connectivity.ConnectivityObserver
import com.example.metaforge.core.util.DatabaseDriverFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.koin.dsl.module

class DummyDataStore : DataStore<Preferences> {
    override val data = MutableStateFlow(Preferences.empty())
    override suspend fun updateData(transform: suspend (Preferences) -> Preferences): Preferences {
        return transform(Preferences.empty())
    }
}

class IosConnectivityObserver : ConnectivityObserver {
    override val isConnected: StateFlow<Boolean> = MutableStateFlow(true)
}

val iosModule = module {
    single { DatabaseDriverFactory() }
    single<DataStore<Preferences>> { DummyDataStore() }
    single<ConnectivityObserver> { IosConnectivityObserver() }
}

fun initKoinIOS() {
    initKoin(platformModules = listOf(iosModule))
}
