import org.jetbrains.kotlin.gradle.dsl.JvmTarget

// Pure Kotlin/JVM: development board pinouts, alternate functions, pin limits and the pin planner. No Android
// dependency, so reference data cannot import android.* (constitution, Principle III).
plugins {
    alias(libs.plugins.kotlin.jvm)
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

dependencies {
    api(project(":shared:si-format"))

    testImplementation(libs.junit)
}