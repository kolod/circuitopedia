package io.github.kolod.circuitopedia

import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import org.junit.Assert.assertTrue
import org.junit.Test
import org.w3c.dom.Element

/**
 * Every translatable string of every project (app, module-api, modules/ *) must exist in all
 * supported locales. Lint's MissingTranslation only checks locales a project already has, so a
 * module without, say, a values-ja directory would otherwise pass.
 */
class LocaleCoverageTest {

    private val locales = listOf("de", "fr", "es", "ja", "uk")

    @Test
    fun everyStringIsTranslatedInEveryLocale() {
        val root = File(checkNotNull(System.getProperty("circuitopedia.rootDir")))
        val problems = mainResDirs(root).flatMap { res ->
            val base = stringNames(res.resolve("values"))
            locales.mapNotNull { locale ->
                val missing = base - stringNames(res.resolve("values-$locale"))
                if (missing.isEmpty()) null else "${res.relativeTo(root)}/values-$locale is missing ${missing.sorted()}"
            }
        }
        assertTrue(problems.joinToString("\n"), problems.isEmpty())
    }

    private fun mainResDirs(root: File): List<File> =
        root.walk()
            .onEnter { it.name != "build" && !it.name.startsWith(".") }
            .filter { it.isDirectory && it.name == "res" && it.parentFile?.name == "main" && it.parentFile?.parentFile?.name == "src" }
            .toList()

    private fun stringNames(valuesDir: File): Set<String> =
        valuesDir.listFiles { f -> f.extension == "xml" }.orEmpty().flatMap { file ->
            val resources = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file).documentElement
            listOf("string", "plurals", "string-array").flatMap { tag ->
                val nodes = resources.getElementsByTagName(tag)
                (0 until nodes.length).map { nodes.item(it) as Element }
                    .filter { it.getAttribute("translatable") != "false" }
                    .map { it.getAttribute("name") }
            }
        }.toSet()
}
