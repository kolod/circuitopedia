package io.github.kolod.circuitopedia.catalog

import io.github.kolod.circuitopedia.module.Category
import io.github.kolod.circuitopedia.module.Category.Calculations
import io.github.kolod.circuitopedia.module.Category.Reference
import java.text.Collator
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Test

class CatalogOrderingTest {

    private data class Item(val title: String, val category: Category = Calculations, val tag: Int = 0)

    private fun order(items: List<Item>, locale: String, category: Category = Calculations) =
        entriesFor(
            category = category,
            items = items,
            categoryOf = { it.category },
            titleOf = { it.title },
            collator = Collator.getInstance(Locale.forLanguageTag(locale)),
        )

    private fun titles(titles: List<String>, locale: String) =
        order(titles.map { Item(it) }, locale).map { it.title }

    @Test
    fun keepsOnlyTheRequestedCategory() {
        val items = listOf(Item("Pinouts", Reference), Item("Ohm"), Item("Codes", Reference))
        assertEquals(listOf("Ohm"), order(items, "en", Calculations).map { it.title })
        assertEquals(listOf("Codes", "Pinouts"), order(items, "en", Reference).map { it.title })
    }

    @Test
    fun emptyInputOrEmptyCategoryGivesEmptyResult() {
        assertEquals(emptyList<Item>(), order(emptyList(), "en"))
        assertEquals(emptyList<Item>(), order(listOf(Item("Ohm")), "en", Reference))
    }

    @Test
    fun englishSortIgnoresCase() {
        assertEquals(listOf("Attenuator", "ohm", "Zener"), titles(listOf("Zener", "ohm", "Attenuator"), "en"))
    }

    @Test
    fun ukrainianCollationPlacesGheWithUpturnBetweenGheAndDe() {
        // Code-point order would put Ґ (U+0490) after Я.
        assertEquals(listOf("Гасіння", "Ґрати", "Дільник"), titles(listOf("Дільник", "Ґрати", "Гасіння"), "uk"))
    }

    @Test
    fun ukrainianCollationOrdersIAndYiBeforeShortI() {
        // Code-point order would put Ї (U+0407) and І (U+0406) before А.
        assertEquals(listOf("Іскра", "Їжак", "Йод"), titles(listOf("Їжак", "Іскра", "Йод"), "uk"))
    }

    @Test
    fun germanUmlautSortsWithItsBaseLetter() {
        assertEquals(listOf("Ampere", "Ätzen", "Zähler"), titles(listOf("Zähler", "Ampere", "Ätzen"), "de"))
    }

    @Test
    fun spanishEnyeSortsAfterN() {
        assertEquals(listOf("nodo", "ñu", "oro"), titles(listOf("ñu", "nodo", "oro"), "es"))
    }

    @Test
    fun equalTitlesKeepRegistryOrder() {
        val items = listOf(Item("Same", tag = 1), Item("Same", tag = 2), Item("Same", tag = 3))
        assertEquals(listOf(1, 2, 3), order(items, "en").map { it.tag })
    }
}
