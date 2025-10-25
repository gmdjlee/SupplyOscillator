plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.chaquopy)
}

android {
    namespace = "com.stockoscillator"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.stockoscillator"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        // Chaquopy Python 설정
        ndk {
            abiFilters += listOf("arm64-v8a", "x86_64")
        }


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

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }
}

chaquopy {
    defaultConfig {
        version = "3.13"

        pip {
            install("pykrx")
            install("pandas")
            install("numpy")
            install("requests")
            install("beautifulsoup4")
        }
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2025.10.00")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    // Android Studio Preview support
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")

    // Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // Compose BOM
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.material3.window.size)

    // Navigation
    implementation(libs.androidx.navigation.compose)

    // ViewModel
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)

    // DataStore (설정 저장용) ⭐ 추가
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    // CardView for marker layouts
    implementation("androidx.cardview:cardview:1.0.0")

    // Charts
    implementation(libs.mpandroidchart)

    // Coroutines
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    // Accompanist
    implementation(libs.accompanist.systemuicontroller)

    // Debug
    debugImplementation(libs.androidx.ui.tooling)
}


