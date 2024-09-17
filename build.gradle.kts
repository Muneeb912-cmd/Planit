// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.jetbrains.kotlin.android) apply false
    id("com.google.gms.google-services") version "4.4.2" apply false
    id("com.google.dagger.hilt.android") version "2.52" apply false
    id("org.jlleitschuh.gradle.ktlint") version "11.5.1" apply false
}
buildscript {
    dependencies {
        classpath("com.google.dagger:hilt-android-gradle-plugin:2.52")
        classpath ("com.android.tools.build:gradle:8.0.2")
        classpath ("androidx.navigation:navigation-safe-args-gradle-plugin:2.8.0-beta04")
        classpath("com.google.gms:google-services:4.4.2")
        classpath ("org.jacoco:org.jacoco.core:0.8.10")
        classpath ("org.jlleitschuh.gradle:ktlint-gradle:7.1.0")
    }
    repositories {
        maven { url = uri("https://jitpack.io") }
        maven { url = uri("https://plugins.gradle.org/m2/") }
    }
}

