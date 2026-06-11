package com.example.metaforge.core.util

import platform.Foundation.NSBundle

actual object AppInfo {
    actual val versionName: String
        get() = (NSBundle.mainBundle.objectForInfoDictionaryKey("CFBundleShortVersionString") as? String)
            ?: "1.0.0"
}
