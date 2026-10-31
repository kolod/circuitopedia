import org.jetbrains.kotlin.gradle.dsl.JvmTarget

// Pure Kotlin/JVM: E-series data, exact arithmetic, target parsing, the combination search and
// result formatting. No Android dependency (constitution, Principle III).
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