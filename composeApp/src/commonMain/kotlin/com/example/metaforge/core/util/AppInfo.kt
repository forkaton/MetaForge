package com.example.metaforge.core.util

/**
 * Platform-supplied app metadata.
 *
 * Android: backed by `BuildConfig.VERSION_NAME` (set in composeApp/build.gradle.kts).
 * iOS:     backed by `CFBundleShortVersionString` from Info.plist.
 *
 * Surfaced in the Settings screen so demo audiences (and the user) can see
 * which build is running.
 */
expect object AppInfo {
    val versionName: String
}
