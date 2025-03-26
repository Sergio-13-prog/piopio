// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    //id ("com.android.application")
    id("com.google.gms.google-services") version "4.4.2" apply false
}


// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    repositories {
        google()  // Asegúrate de que este repositorio esté presente
        mavenCentral()
    }
    dependencies {
        // La versión del plugin de Google Services
        classpath("com.google.gms:google-services:4.4.2")  // O la versión más reciente
    }
}

