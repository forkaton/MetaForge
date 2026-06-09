package com.example.metaforge.core.util

import com.example.metaforge.BuildConfig

actual object AppInfo {
    actual val versionName: String = BuildConfig.VERSION_NAME
}
