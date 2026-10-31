package io.github.kolod.circuitopedia.catalog

import io.github.kolod.circuitopedia.module.Category

/**
 * The items of [category], sorted by [titleOf] with [collator] (the current app locale's
 * collation, so that for example Ukrainian Ґ sorts between Г and Д). Items with equal titles
 * keep their input order.
 */
fun <T> entriesFor(
    category: Category,
    items: List<T>,
    categoryOf: (T) -> Category,
    titleOf: (T) -> String,
    collator: Comparator<in String>,
): List<T> = items.filter { categoryOf(it) == category }.sortedWith(compareBy(collator, titleOf))
