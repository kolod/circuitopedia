// Compose parts shared by the color code calculators: tabs and pager, the fixed-header color
// table, the SVG band drawing and the common strings. Data-driven; knows no specific component.
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "io.github.kolod.circuitopedia.shared.colorcode.ui"
    compileSdk = 37

    defaultConfig {
        minSdk = 26
    }

    // Matches :shared:color-code so its Kotlin code can be inlined.
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
    }

    resourcePrefix = "color_code_"

    lint {
        error += setOf("MissingTranslation", "MissingDefaultResource", "ExtraTranslation")
        abortOnError = true
    }
}

dependencies {
    api(project(":shared:color-code"))

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidsvg)
}
