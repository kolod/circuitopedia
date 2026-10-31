# Contract: Module Integration

**Feature**: [../spec.md](../spec.md) | **Applies to**: every calculator and reference page module

This contract is the only interface between the app shell and a module. The Kotlin
signatures below are normative. Their bodies are an implementation detail.

## Public API of `:module-api`

Package `io.github.kolod.circuitopedia.module`:

```kotlin
enum class Category { Calculations, Reference }

@JvmInline
value class ModuleId(val value: String)   // init: require(value matches [a-z0-9]+(-[a-z0-9]+)*, 1..64 chars)

class AppModule(
    val id: ModuleId,
    val category: Category,
    @StringRes val titleRes: Int,
    @StringRes val descriptionRes: Int,
    @DrawableRes val iconRes: Int,
    val content: @Composable () -> Unit,
)
```

`:module-api` depends only on Compose runtime and `androidx.annotation`. It MUST NOT depend on
`:app` or on any module.

## What a module MUST provide

1. **Location**: `modules/<name>/` with its own `build.gradle.kts` (Android library), where
   `<name>` is the kebab-case module id. Settings auto-include it as `:modules:<name>`.
2. **Dependencies**: `implementation(project(":module-api"))` plus official AndroidX/Compose
   artifacts. It MUST NOT depend on another `:modules:*` project or on `:app`. Any calculation
   logic goes in its own `modules/<name>/core` Kotlin/JVM subproject (no Android imports), which
   the module depends on.
3. **Descriptor**: exactly one public top-level `val <name>Module: AppModule` in package
   `io.github.kolod.circuitopedia.modules.<name>` (with dashes removed from `<name>`).
4. **Resources**:
   - `titleRes` and `descriptionRes` in `res/values/strings.xml` (English), and in
     `values-de`, `values-fr`, `values-es`, `values-ja`, `values-uk`.
   - The description fits on one line on a phone in English (about 60 characters or fewer).
   - `iconRes` is a 24dp vector drawable that uses `?attr/colorControlNormal` or a single tint
     color so it works in both light and dark themes.
   - Resource names start with the module prefix (`<name_underscored>_`) to avoid merge
     collisions (enforced by `resourcePrefix` in the module's build file).
5. **Screen**: `content` draws only the module's body. It MUST NOT draw its own top app bar or
   back button and MUST NOT intercept system back. The shell provides the title bar (using
   `titleRes`) and the "up" control. The body handles its own scrolling and insets below the bar.

## What the shell guarantees to a module

- The module appears in exactly the tab of its `category`, sorted by localized title.
- `content` runs inside the app's Material 3 theme, below a top bar showing `titleRes`.
- The module is opened by `id`. Its screen is restored after rotation and process death as long
  as the same `id` is still registered.
- The shell never calls into a module except through `AppModule` fields.

## Registering a module

The only change outside `modules/<name>/` is one line in
`app/src/main/kotlin/io/github/kolod/circuitopedia/ModuleRegistry.kt`:

```kotlin
val modules: List<AppModule> = listOf(
    ohmsLawModule,          // ← one line per module
)
```

Removing a module means deleting that line and the `modules/<name>/` directory.

## Enforcement

| Rule                                   | Check                                         | Runs in CI |
|----------------------------------------|-----------------------------------------------|------------|
| Unique ids                             | `ModuleRegistryTest` (JVM unit test, `:app`)  | `./gradlew test` |
| Well-formed id                         | `ModuleId` constructor + `ModuleIdTest`       | `./gradlew test` |
| English text present                   | Lint `MissingDefaultResource` (error)         | `./gradlew lint` |
| All six locales present                | Lint `MissingTranslation` (error)             | `./gradlew lint` |
| No stray locales/keys                  | Lint `ExtraTranslation` (error)               | `./gradlew lint` |
| Resource name prefix                   | `resourcePrefix` + Lint `ResourceName`        | `./gradlew lint` |
| No module→module / module→app deps     | Code review against this contract. A build check can be added once two modules exist | — |
