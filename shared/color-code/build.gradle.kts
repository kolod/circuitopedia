import org.jetbrains.kotlin.gradle.dsl.JvmTarget

// Pure Kotlin/JVM parts shared by the color code calculators: band colors, tolerance unit,
// contrast and value formatting. The test fixtures hold the SVG drawing contract check.
plugins {
    alias(libs.plugins.kotlin.jvm)
    `java-test-fixtures`
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
    testFixturesImplementation(libs.junit)
}
