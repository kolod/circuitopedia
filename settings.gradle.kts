pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "circuitopedia"
include(":app")
include(":module-api")

// Every calculator or reference page lives in its own modules/<name> directory and is picked up
// here automatically, so adding a module never requires editing this file. A module's pure-Kotlin
// logic lives in modules/<name>/core and is included as :modules:<name>:core.
file("modules").listFiles()
    ?.filter { it.isDirectory && it.resolve("build.gradle.kts").isFile }
    ?.sortedBy { it.name }
    ?.forEach { module ->
        include(":modules:${module.name}")
        if (module.resolve("core/build.gradle.kts").isFile) include(":modules:${module.name}:core")
    }

// Libraries shared by several modules live in shared/<name> and are included as :shared:<name>.
// They are not app modules: the app never depends on them automatically, modules declare what they use.
file("shared").listFiles()
    ?.filter { it.isDirectory && it.resolve("build.gradle.kts").isFile }
    ?.sortedBy { it.name }
    ?.forEach { include(":shared:${it.name}") }
