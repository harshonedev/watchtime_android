plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.gms.google-services")
    id("kotlin-kapt")
}

android {
    namespace = "com.app.watchtime.tv"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.app.watchtime.tv"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
        isCoreLibraryDesugaringEnabled = true
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    // Core library desugaring for Java 8+ APIs
    coreLibraryDesugaring(libs.desugar.jdk.libs)

    // Core modules (shared with mobile)
    implementation(project(":core:ui"))
    implementation(project(":core:navigation"))
    implementation(project(":core:network"))
    implementation(project(":core:room"))
    implementation(project(":core:utils"))

    // TV-specific UI modules
    implementation(project(":core:tv-ui"))
    implementation(project(":auth:tv-ui"))
    implementation(project(":popular:tv-ui"))
    implementation(project(":discover:tv-ui"))
    implementation(project(":media:tv-ui"))
    implementation(project(":collections:tv-ui"))

    // Data modules (shared with mobile)
    implementation(project(":auth:data"))
    implementation(project(":auth:domain"))
    implementation(project(":popular:data"))
    implementation(project(":popular:domain"))
    implementation(project(":discover:data"))
    implementation(project(":discover:domain"))
    implementation(project(":media:data"))
    implementation(project(":media:domain"))
    implementation(project(":collections:data"))
    implementation(project(":collections:domain"))
    implementation(project(":profile:data"))
    implementation(project(":profile:domain"))

    // Android TV dependencies (optional - using standard Material3 which works on TV)
    // Leanback can be added later for advanced TV features
    // implementation(libs.androidx.leanback)
    // implementation(libs.androidx.tv.foundation)
    // implementation(libs.androidx.tv.material)

    // Standard Android dependencies
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.foundation)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.runtime)

    // Firebase dependencies
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)

    // Koin dependencies
    implementation(platform(libs.koin.bom))
    implementation(libs.koin.android)
    implementation(libs.koin.core)
    implementation(libs.koin.androidx.compose)
    implementation(libs.koin.androidx.viewmodel)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}

