// The contract between the app shell and every calculator or reference module. It must not
// depend on :app or on any module.
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "io.github.kolod.circuitopedia.module"
    compileSdk = 37

    defaultConfig {
        minSdk = 26
    }

    buildFeatures {
        compose = true
    }

    lint {
        error += setOf("MissingTranslation", "MissingDefaultResource", "ExtraTranslation")
        abortOnError = true
    }
}

dependencies {
    api(platform(libs.androidx.compose.bom))
    // Brings the Compose runtime (@Composable) and androidx.annotation (@StringRes, @DrawableRes).
    api(libs.androidx.compose.ui)

    testImplementation(libs.junit)
}
