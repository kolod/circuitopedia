package io.github.kolod.circuitopedia.samples

import androidx.annotation.StringRes
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import io.github.kolod.circuitopedia.R
import io.github.kolod.circuitopedia.module.AppModule
import io.github.kolod.circuitopedia.module.Category
import io.github.kolod.circuitopedia.module.ModuleId

// Debug-only sample modules for instrumented tests (FR-016). ModuleRegistry never lists them.

val sampleModules: List<AppModule> = listOf(
    sample("sample-zener", Category.Calculations, R.string.sample_zener_title, R.string.sample_zener_description),
    sample("sample-ohm", Category.Calculations, R.string.sample_ohm_title, R.string.sample_ohm_description),
    sample("sample-alpha", Category.Calculations, R.string.sample_alpha_title, R.string.sample_alpha_description),
    sample("sample-long", Category.Calculations, R.string.sample_long_title, R.string.sample_long_description),
    sample("sample-pinouts", Category.Reference, R.string.sample_pinouts_title, R.string.sample_pinouts_description),
)

/** [n] calculation samples with identical titles, ids `sample-n-0` … `sample-n-<n-1>`. */
fun manySampleCalculations(n: Int): List<AppModule> = List(n) { i ->
    sample("sample-n-$i", Category.Calculations, R.string.sample_many_title, R.string.sample_many_description)
}

private fun sample(id: String, category: Category, @StringRes title: Int, @StringRes description: Int) =
    AppModule(
        id = ModuleId(id),
        category = category,
        titleRes = title,
        descriptionRes = description,
        iconRes = R.drawable.sample_icon,
        content = { SampleContent(id) },
    )

@Composable
private fun SampleContent(id: String) {
    // Test-only text; never shipped.
    Text(text = id, modifier = Modifier.testTag("content-$id"))
}
