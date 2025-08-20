plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    // 'kotlin.compose' non è uno standard, di solito è 'org.jetbrains.kotlin.plugin.compose'
    // ma se funziona con i tuoi alias, va bene. Verificheremo che sia corretto.
    // Se hai problemi, sostituiscilo con id("org.jetbrains.kotlin.plugin.compose")
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.creativeideas.batterymindai"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.creativeideas.batterymindai"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false // Considera di abilitarlo per la produzione
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8" // Assicurati che sia compatibile con la tua versione di Kotlin
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Core Android & Jetpack Compose (Invariato)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.compose.material:material-icons-extended:1.6.8")
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)

    // Hilt - Dependency Injection (Pulito e corretto)
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)
    // La dipendenza kapt("androidx.hilt:hilt-compiler:1.2.0") è specifica per WorkManager e va tenuta lì.

    // Room - Database (Invariato)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    kapt(libs.androidx.room.compiler)

    // Coroutines (Invariato)
    implementation(libs.kotlinx.coroutines.android)

    // Kotlinx Serialization (Invariato)
    implementation(libs.kotlinx.serialization.json) // Già presente, ottimo.

    // --- ELIMINAZIONE DEL DEBITO ONLINE ---
    // Le seguenti librerie sono state rimosse perché legate all'architettura online.
    // implementation("com.squareup.retrofit2:retrofit:2.9.0")
    // implementation("com.jakewharton.retrofit:retrofit2-kotlinx-serialization-converter:1.0.0")
    // implementation(platform("com.squareup.okhttp3:okhttp-bom:4.12.0"))
    // implementation("com.squareup.okhttp3:okhttp")
    // implementation("com.squareup.okhttp3:logging-interceptor")
    // implementation("com.google.ai.client.generativeai:generativeai:0.3.0")

    // Grafici (Invariato)
    implementation(libs.vico.compose)
    implementation(libs.vico.compose.m3)
    implementation(libs.vico.core)

    // Shizuku (Invariato)
    implementation("dev.rikka.shizuku:api:13.1.5")

    // WorkManager (Pulito e corretto)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.hilt.work)
    kapt("androidx.hilt:hilt-compiler:1.2.0") // Kapt specifico per l'integrazione di Hilt con WorkManager

    // Ktor - Il nostro nuovo client di rete
    implementation("io.ktor:ktor-client-android:2.3.10")
    implementation("io.ktor:ktor-client-content-negotiation:2.3.10")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.10")

    // --- NUOVA DIPENDENZA FONDAMENTALE ---
    // MediaPipe - Il nostro motore di IA on-device
    implementation("com.google.mediapipe:tasks-genai:0.10.14")

    // Test (Invariato)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}

kapt {
    correctErrorTypes = true
}