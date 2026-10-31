package io.github.kolod.circuitopedia.shared.colorcode

import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import org.w3c.dom.Element

/**
 * The contract between a band drawing SVG and the shared BandDrawing (spec 002 research R10):
 * - the root has a viewBox;
 * - `band-1` … `band-N` are paths, left to right;
 * - band fills are presentation attributes, because render-time CSS does not override an inline style;
 * - no element has a transform, so band bounds can be read straight from their path data.
 */
object SvgContract {
    /** The problems found in [svg]; empty when it conforms with [bandCount] bands. */
    fun check(svg: File, bandCount: Int): List<String> {
        val document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(svg)
        val elements = document.getElementsByTagName("*").let { nodes -> (0 until nodes.length).map { nodes.item(it) as Element } }
        fun byId(id: String) = elements.firstOrNull { it.getAttribute("id") == id }

        val problems = mutableListOf<String>()
        if (document.documentElement.getAttribute("viewBox").trim().split(Regex("[\\s,]+")).size != 4) {
            problems += "the root has no viewBox"
        }
        for (band in 1..bandCount) {
            val element = byId("band-$band")
            when {
                element == null -> problems += "band-$band is missing"
                element.tagName != "path" -> problems += "band-$band must be a path"
                element.getAttribute("d").isBlank() -> problems += "band-$band has no path data"
                "fill" in element.getAttribute("style") -> problems += "band-$band sets fill in style"
            }
        }
        if (byId("band-${bandCount + 1}") != null) problems += "band-${bandCount + 1} is not expected"
        elements.filter { it.hasAttribute("transform") }.forEach { problems += "${it.getAttribute("id")} has a transform" }
        return problems
    }
}
