import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.sqldelight)
    jacoco
}

// Load local.properties for API keys
val localProperties = Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        load(localPropertiesFile.inputStream())
    }
}

kotlin {
    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }
    
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }
    
    sourceSets {
        commonMain.dependencies {
            // Compose
            implementation(compose.runtime)
            implementation("io.coil-kt.coil3:coil-compose:3.0.0-alpha10")
            implementation("io.coil-kt.coil3:coil-network-ktor3:3.0.0-alpha10")
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.materialIconsExtended)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            
            // Kotlin
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.datetime)
            
            // Ktor
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.json)
            implementation(libs.ktor.client.logging)
            
            // Koin DI
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)
            
            // SQLDelight
            implementation(libs.sqldelight.runtime)
            implementation(libs.sqldelight.coroutines)
            
            // DataStore + Okio
            implementation(libs.datastore.preferences)
            implementation(libs.okio)
            
            // Lifecycle & ViewModel
            implementation(libs.lifecycle.viewmodel)
            implementation(libs.lifecycle.runtime.compose)
            
            // Navigation
            implementation(libs.navigation.compose)
            
            // Coil
            implementation(libs.coil.compose)
            implementation(libs.coil.network.ktor)
        }
        
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.turbine)
        }
        
         androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.koin.android)
            implementation(libs.ktor.client.okhttp)
            implementation(libs.sqldelight.android.driver)
            implementation(libs.datastore.preferences.android)
        }

        // Compose UI (instrumented) tests — run on device/emulator
        androidInstrumentedTest.dependencies {
            implementation(libs.compose.ui.test.junit4)
            implementation(libs.test.ext.junit)
            implementation(libs.espresso.core)
            implementation(kotlin("test"))
        }
        
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
            implementation(libs.sqldelight.native.driver)
        }
    }
}

android {
    namespace = "com.example.metaforge"
    compileSdk = 35
    
    defaultConfig {
        applicationId = "com.example.metaforge"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
        // Inject API key from local.properties
        buildConfigField(
            "String",
            "GEMINI_API_KEY",
            "\"${localProperties.getProperty("GEMINI_API_KEY", "")}\""
        )
    }
    
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    // ── Release signing ──────────────────────────────────────────────────────
    // Reads keystore from local.properties (or env vars) so credentials stay
    // out of git. If no METAFORGE_STORE_FILE is configured, release builds fall
    // back to the debug signing config — fine for CI/contributors who only run
    // `assembleDebug`. To produce a *real* signed release APK:
    //   1) keytool -genkey -v -keystore metaforge-release.jks -keyalg RSA \
    //        -keysize 2048 -validity 10000 -alias metaforge
    //   2) In local.properties add:
    //        METAFORGE_STORE_FILE=metaforge-release.jks
    //        METAFORGE_STORE_PASSWORD=...
    //        METAFORGE_KEY_ALIAS=metaforge
    //        METAFORGE_KEY_PASSWORD=...
    //   3) ./gradlew :composeApp:assembleRelease
    val releaseStoreFile = localProperties.getProperty("METAFORGE_STORE_FILE")
        ?: System.getenv("METAFORGE_STORE_FILE")
    val releaseStorePassword = localProperties.getProperty("METAFORGE_STORE_PASSWORD")
        ?: System.getenv("METAFORGE_STORE_PASSWORD")
    val releaseKeyAlias = localProperties.getProperty("METAFORGE_KEY_ALIAS")
        ?: System.getenv("METAFORGE_KEY_ALIAS")
    val releaseKeyPassword = localProperties.getProperty("METAFORGE_KEY_PASSWORD")
        ?: System.getenv("METAFORGE_KEY_PASSWORD")
    val hasReleaseKeystore = releaseStoreFile != null &&
            file(releaseStoreFile).exists() &&
            releaseStorePassword != null &&
            releaseKeyAlias != null &&
            releaseKeyPassword != null

    signingConfigs {
        if (hasReleaseKeystore) {
            create("release") {
                storeFile = file(releaseStoreFile!!)
                storePassword = releaseStorePassword
                keyAlias = releaseKeyAlias
                keyPassword = releaseKeyPassword
            }
        }
    }

    buildTypes {
        debug {
            enableUnitTestCoverage = true
        }
        release {
            signingConfig = signingConfigs.findByName("release")
                ?: signingConfigs.getByName("debug")
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    
    buildFeatures {
        buildConfig = true
    }

    // Compose UI test support: manifest declares the test activity
    dependencies {
        debugImplementation(libs.compose.ui.test.manifest)
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

sqldelight {
    databases {
        create("MetaForgeDatabase") {
            packageName.set("com.example.metaforge.data.local")
        }
    }
}

// ── JaCoCo: unit-test coverage report for the Android debug variant ─────────
jacoco { toolVersion = "0.8.11" }

tasks.register<JacocoReport>("jacocoTestReport") {
    group = "verification"
    description = "Generates JaCoCo coverage report for the Android debug unit tests."
    dependsOn("testDebugUnitTest")

    reports {
        html.required.set(true)
        xml.required.set(true)
        csv.required.set(false)
    }

    // Include-only strategy — we count just the packages we actually unit-test.
    // Single-segment `**/foo/**` patterns are honoured reliably by Gradle's
    // Ant glob; multi-segment ones (`**/a/b/c/**`) silently no-op, so we stick
    // to the leaf folder name and prune internal noise via excludes below.
    val includePatterns = listOf(
        "**/draft_arena/**",  // DraftViewModel + DraftUiState (Screen excluded)
        "**/draft_setup/**",  // DraftSetupViewModel
        "**/model/**",        // domain.model — DraftState etc.
        "**/util/**"          // core.util — LastFetchFormatter
    )

    val excludePatterns = listOf(
        // UI inside the included packages
        "**/*Screen*", "**/*ScreenKt*",
        "**/ComposableSingletons*",
        // util/ noise we don't unit-test
        "**/DatabaseDriverFactory*", "**/Extensions*",
        // domain.model trivial data carriers — kept untested by design
        "**/Hero.class", "**/Hero$*",
        "**/HeroLane*", "**/HeroRecommendation*", "**/SynergyResult*",
        // Legacy NoteAI scaffolding (lives in domain.model too)
        "**/Note.class", "**/Note$*",
        // Trivial DataClass
        "**/DraftSetupUiState*",
        // Generic build noise
        "**/BuildConfig*", "**/R.class", "**/R$*.class",
        "**/Manifest*.*", "**/*Test*.*"
    )

    val kotlinDebugTree = fileTree("${layout.buildDirectory.get()}/tmp/kotlin-classes/debug") {
        include(includePatterns)
        exclude(excludePatterns)
    }
    val javaDebugTree = fileTree("${layout.buildDirectory.get()}/intermediates/javac/debug/classes") {
        include(includePatterns)
        exclude(excludePatterns)
    }

    sourceDirectories.setFrom(files(
        "src/commonMain/kotlin",
        "src/androidMain/kotlin"
    ))
    // Pass trees directly — wrapping with files(...) discards the per-tree
    // exclude filters and silently includes everything we tried to skip.
    classDirectories.setFrom(kotlinDebugTree, javaDebugTree)
    executionData.setFrom(fileTree(layout.buildDirectory.get()) {
        include(
            "outputs/unit_test_code_coverage/debugUnitTest/testDebugUnitTest.exec",
            "jacoco/testDebugUnitTest.exec"
        )
    })
}
