// Top-level build file where you can add configuration options common to all sub-projects/modules.

plugins {
    // Android Gradle Plugin
    alias(libs.plugins.androidApplication) apply false
    // Kotlin Android
    alias(libs.plugins.kotlinAndroid) apply false
    // KSP
    alias(libs.plugins.ksp) apply false
}


// Clean task
tasks.register<Delete>("clean") {
    delete(rootProject.layout.buildDirectory)
}