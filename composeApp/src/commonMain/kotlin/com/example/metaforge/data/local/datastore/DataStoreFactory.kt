package com.example.metaforge.data.local.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import okio.Path.Companion.toPath

// Platform-specific DataStore factory (android=filesDir, ios=NSDocumentDirectory)
expect class DataStoreFactory {
    fun producePath(): String
}

internal const val DATA_STORE_FILE_NAME = "noteai.preferences_pb"

fun DataStoreFactory.create(): DataStore<Preferences> =
    PreferenceDataStoreFactory.createWithPath(
        produceFile = { "${producePath()}/$DATA_STORE_FILE_NAME".toPath() }
    )
