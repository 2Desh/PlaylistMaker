plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("kotlin-parcelize")
}

android {
    namespace = "com.practicum.playlistmaker"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.practicum.playlistmaker"
        minSdk = 29
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    buildFeatures {
        viewBinding = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {

    // UI и графический интерфейс (AndroidX + Material)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.google.material) // Основная библиотека для Material Design компонентов
    implementation(libs.androidx.navigation.fragment.ktx) // Навигация во фрагментах
    implementation(libs.androidx.navigation.ui.ktx)       // Интеграция навигации с UI

    implementation(libs.koin.android)

    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.viewpager2)

    // Сеть и JSON (Retrofit + Gson)
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.gson)

    // Картинки (Glide)
    implementation(libs.glide)
    annotationProcessor(libs.glide.compiler)

    // Тестирование
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    testImplementation(libs.junit)
}