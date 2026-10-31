package io.github.kolod.circuitopedia

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import io.github.kolod.circuitopedia.catalog.CatalogScreen
import io.github.kolod.circuitopedia.module.AppModule
import io.github.kolod.circuitopedia.theme.CircuitopediaTheme

/**
 * Root of the UI. The whole back stack is [openModuleId]: `null` shows the catalog, a registered
 * id shows that module. An id that is no longer registered (after an update) shows the catalog.
 */
@Composable
fun CircuitopediaApp(modules: List<AppModule>) {
    CircuitopediaTheme {
        var openModuleId by rememberSaveable { mutableStateOf<String?>(null) }
        val openModule = modules.firstOrNull { it.id.value == openModuleId }
        // Keeps the catalog's tab and scroll positions while a module screen is shown.
        val stateHolder = rememberSaveableStateHolder()

        if (openModule != null) {
            BackHandler { openModuleId = null }
            ModuleScreen(openModule, onUp = { openModuleId = null })
        } else {
            stateHolder.SaveableStateProvider("catalog") {
                CatalogScreen(modules, onOpen = { openModuleId = it.value })
            }
        }
    }
}
