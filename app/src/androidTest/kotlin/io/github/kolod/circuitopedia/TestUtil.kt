package io.github.kolod.circuitopedia

import androidx.activity.ComponentActivity
import androidx.annotation.StringRes
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.test.ext.junit.rules.ActivityScenarioRule

typealias ComposeRule = AndroidComposeTestRule<ActivityScenarioRule<ComponentActivity>, ComponentActivity>

fun ComposeRule.str(@StringRes id: Int): String = activity.getString(id)

fun hasTestTagPrefix(prefix: String) = SemanticsMatcher("TestTag starts with \"$prefix\"") {
    it.config.getOrNull(SemanticsProperties.TestTag)?.startsWith(prefix) == true
}

/** Test tags of all composed list entries, in on-screen order. */
fun ComposeRule.entryTags(): List<String> =
    onAllNodes(hasTestTagPrefix(ENTRY_TAG_PREFIX)).fetchSemanticsNodes()
        .sortedBy { it.boundsInRoot.top }
        .map { it.config[SemanticsProperties.TestTag] }

const val ENTRY_TAG_PREFIX = "module-entry:"
