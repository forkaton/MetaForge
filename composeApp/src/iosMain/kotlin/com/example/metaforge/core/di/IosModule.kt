package com.example.metaforge.core.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.example.metaforge.core.util.DatabaseDriverFactory
import kotlinx.coroutines.flow.MutableStateFlow
import org.koin.dsl.module

/**
 * iOS-specific Koin module.
 *
 * Menyediakan dependencies platform yang dipakai di shared modules.
 */

// Dummy DataStore untuk iOS (akan diimplementasi lebih baik di masa depan)
class DummyDataStore : DataStore<Preferences> {
    override val data = MutableStateFlow(Preferences.empty())
    override suspend fun updateData(transform: suspend (Preferences) -> Preferences): Preferences {
        return transform(Preferences.empty())
    }
}

val iosModule = module {
    single { DatabaseDriverFactory() }
    single<DataStore<Preferences>> { DummyDataStore() }
}

/** Helper untuk dipanggil dari Swift code. */
fun initKoinIOS() {
    initKoin(platformModules = listOf(iosModule))
}

