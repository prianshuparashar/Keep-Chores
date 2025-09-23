plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.chores.keepchores"
    compileSdk = 36

    buildFeatures {
        viewBinding = true
    }

    defaultConfig {
        applicationId = "com.chores.keepchores"
        minSdk = 23
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlin {
        jvmToolchain(17)
    }
}

dependencies {
    // Core UI + AndroidX
    implementation(libs.bundles.uiCore)

    // Persistence (Room runtime + KTX)
    implementation(libs.bundles.room)

    // Animation
    implementation(libs.lottie)

    // KSP compiler artifact for Room
    ksp(libs.room.compiler)

    // Unit tests
    testImplementation(libs.bundles.testUnit)

    // Instrumented Android tests
    androidTestImplementation(libs.bundles.androidTest)
}