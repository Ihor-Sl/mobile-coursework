plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    // kotlinx.serialization — читання каталогу meditations.json
    alias(libs.plugins.kotlin.serialization)
    // KSP — генерація коду Room (DAO та Database)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.example.meditationapplication"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.example.meditationapplication"
        // Мінімальна версія Android 8.0 (API 26) — потрібна для java.time
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
    androidResources {
        // mp3 і webp вже стиснуті
        noCompress += listOf("mp3", "webp")
    }
}

// Схема бази даних Room експортується в app/schemas для майбутніх міграцій
ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    // Навігація між екранами
    implementation(libs.androidx.navigation.compose)
    // Локальна БД: таблиці sessions і favorites
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    // Налаштування користувача
    implementation(libs.androidx.datastore.preferences)
    // Відтворення аудіо
    implementation(libs.androidx.media3.exoplayer)
    // Щоденне нагадування
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.android)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
