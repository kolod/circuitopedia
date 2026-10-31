package io.github.kolod.circuitopedia.module

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable

/**
 * Describes one calculator or reference page to the app shell.
 *
 * - [titleRes] and [descriptionRes] must exist in English (default resources) and in every
 *   supported locale. The description fits on one line.
 * - [iconRes] is a 24dp vector drawable bundled with the module.
 * - [content] draws only the module's body. The shell provides the top bar with the title and the
 *   "up" control, so the module must not draw its own or intercept system back.
 *
 * See specs/001-app-shell-navigation/contracts/module-contract.md.
 */
class AppModule(
    val id: ModuleId,
    val category: Category,
    @StringRes val titleRes: Int,
    @StringRes val descriptionRes: Int,
    @DrawableRes val iconRes: Int,
    val content: @Composable () -> Unit,
)
