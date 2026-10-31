# Modules

Each subdirectory here is one calculator or reference page. The rules are in
[specs/001-app-shell-navigation/contracts/module-contract.md](../specs/001-app-shell-navigation/contracts/module-contract.md).
In short:

- `settings.gradle.kts` includes every `modules/<name>/` that has a `build.gradle.kts` as
  `:modules:<name>`, and `app` depends on all of them automatically.
- A module depends on `:module-api` (plus official AndroidX/Compose artifacts) and, if it needs
  them, on shared libraries in `shared/<name>` (`:shared:<name>`), never on `:app` or another
  module. Code moves to `shared/` once a second module needs it.
- Calculation logic and reference data go in `modules/<name>/core`, a Kotlin/JVM subproject with
  no Android imports, which the module depends on.
- A module exposes exactly one `val <name>Module: AppModule` and is registered with one line in
  `app/src/main/kotlin/io/github/kolod/circuitopedia/ModuleRegistry.kt`.
- Titles and descriptions exist in `values`, `values-de`, `values-fr`, `values-es`, `values-ja`
  and `values-uk`; Lint fails the build otherwise.

## Minimal `modules/<name>/build.gradle.kts`

```kotlin
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "io.github.kolod.circuitopedia.modules.<name>"   // dashes removed
    compileSdk = 37
    defaultConfig { minSdk = 26 }
    buildFeatures { compose = true }
    resourcePrefix = "<name_underscored>_"
    lint {
        error += setOf("MissingTranslation", "MissingDefaultResource", "ExtraTranslation")
    }
}

dependencies {
    implementation(project(":module-api"))
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.material3)
}
```

## Minimal descriptor

```kotlin
package io.github.kolod.circuitopedia.modules.<name>

val <name>Module = AppModule(
    id = ModuleId("<name>"),
    category = Category.Calculations,
    titleRes = R.string.<name_underscored>_title,
    descriptionRes = R.string.<name_underscored>_description,
    iconRes = R.drawable.<name_underscored>_icon,
    content = { <Name>Screen() },
)
```
