import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

// Written by .github/workflows/release.yml; never committed (see .gitignore).
val keystorePropertiesFile = file("keystore.properties")

android {
    namespace = "io.github.kolod.circuitopedia"
    compileSdk = 37

    defaultConfig {
        applicationId = "io.github.kolod.circuitopedia"
        minSdk = 26
        targetSdk = 37
        versionCode = providers.gradleProperty("appVersionCode").orNull?.toInt() ?: 1
        versionName = providers.gradleProperty("appVersionName").orNull ?: "0.1.0-dev"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        if (keystorePropertiesFile.isFile) {
            val keystore = Properties().apply { keystorePropertiesFile.inputStream().use(::load) }
            create("release") {
                storeFile = file(keystore.getProperty("storeFile"))
                storePassword = keystore.getProperty("storePassword")
                keyAlias = keystore.getProperty("keyAlias")
                keyPassword = keystore.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            signingConfigs.findByName("release")?.let { signingConfig = it }
        }
    }

    buildFeatures {
        compose = true
    }

    lint {
        error += setOf("MissingTranslation", "MissingDefaultResource", "ExtraTranslation")
        abortOnError = true
        // Also checks the resources of every :modules:* project.
        checkDependencies = true
    }

    testOptions.unitTests.all { test ->
        // LocaleCoverageTest scans the string resources of every project.
        test.systemProperty("circuitopedia.rootDir", rootDir.absolutePath)
        test.inputs.files(
            fileTree(rootDir) {
                include("**/src/main/res/values*/*.xml")
                exclude("**/build/**")
            },
        ).withPathSensitivity(PathSensitivity.RELATIVE)
    }
}

dependencies {
    implementation(project(":module-api"))
    // Every module included by settings.gradle.kts; registering it in ModuleRegistry is the only
    // other step needed.
    rootProject.childProjects["modules"]?.childProjects?.values?.forEach {
        implementation(project(it.path))
    }

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui.tooling.preview)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    testImplementation(libs.junit)

    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.runner)
}
