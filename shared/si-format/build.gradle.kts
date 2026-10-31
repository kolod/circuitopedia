import org.jetbrains.kotlin.gradle.dsl.JvmTarget

// Pure Kotlin/JVM: SI-prefix and locale-aware decimal formatting shared by several calculators.
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
    testImplementation(libs.junit)
}