import org.jetbrains.kotlin.gradle.dsl.JvmTarget

// Pure Kotlin/JVM: the jellybean parts catalog (parameters, pinouts, datasheet sources). No Android
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