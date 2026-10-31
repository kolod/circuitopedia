import org.jetbrains.kotlin.gradle.dsl.JvmTarget

// Pure Kotlin/JVM: exact rationals, the IEC 60063 E-series, resistor connections, the resistance
// parser and resistance formatting, shared by the combination finder and the voltage divider.
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