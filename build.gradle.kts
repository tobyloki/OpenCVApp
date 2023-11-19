// Top-level build file where you can add configuration options common to all sub-projects/modules.

buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.1.4")
    }
}

plugins {
    id("com.android.application").version("8.1.1").apply(false)
    id("org.jetbrains.kotlin.android").version("1.8.10").apply(false)
    id("de.undercouch.download").version("4.1.2").apply(false)
    // https://github.com/google/secrets-gradle-plugin
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin").version("2.0.1").apply(false)
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}