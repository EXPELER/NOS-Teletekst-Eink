package nl.expeler.einkteletext.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import nl.expeler.einkteletext.ui.theme.*
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.nodes.TextNode

data class TeletextLine(
    val text: AnnotatedString,
    val page: String? = null,
    val isTitle: Boolean = false,
)

fun parseTeletextContent(html: String): List<TeletextLine> {
    val lines = html.split("\n")
        .filterNot { isHeaderLine(it) || isFooterLine(it) }
        .dropWhile { isBlankRawLine(it) }
        .map { parseOneLine(it) }
        .filterNot { it.text.isBlank() }
    if (lines.isEmpty()) return lines
    val first = lines.first()
    return if (first.page == null) {
        val start = first.text.text.indexOfFirst { !it.isWhitespace() }.coerceAtLeast(0)
        val trimmed = first.text.subSequence(start, first.text.length)
        listOf(first.copy(isTitle = true, text = trimmed)) + lines.drop(1)
    } else {
        lines
    }
}

private fun parseOneLine(rawLine: String): TeletextLine {
    val doc = Jsoup.parseBodyFragment(rawLine)
    val body = doc.body()

    // Extract the first page link before removing anchors
    val page = body.selectFirst("a[href]")
        ?.attr("href")
        ?.removePrefix("#")
        ?.trim()
        ?.takeIf { it.isNotBlank() }

    // Remove all anchor elements so page numbers don't appear in the text
    body.select("a").forEach { it.remove() }

    val text = buildAnnotatedString {
        processElement(body, EinkInk)
    }

    return TeletextLine(text = text.trimEnd(), page = page)
}

private fun AnnotatedString.trimEnd(): AnnotatedString {
    val trimmed = text.trimEnd()
    if (trimmed.length == text.length) return this
    return subSequence(0, trimmed.length)
}

private fun AnnotatedString.Builder.processElement(element: Element, textColor: Color) {
    for (child in element.childNodes()) {
        when (child) {
            is TextNode -> {
                val text = cleanPrivateUseChars(child.wholeText)
                withStyle(SpanStyle(color = textColor)) { append(text) }
            }
            is Element -> when (child.tagName()) {
                "br" -> append("\n")
                else -> processElement(child, resolveTextColor(child.classNames(), textColor))
            }
        }
    }
}

private fun resolveTextColor(classes: Set<String>, default: Color): Color = when {
    "cyan" in classes -> TeletextCyan
    "yellow" in classes -> TeletextYellow
    "green" in classes -> TeletextGreen
    "white" in classes -> TeletextWhite
    "red" in classes -> TeletextRed
    "blue" in classes -> TeletextBlue
    "magenta" in classes -> TeletextMagenta
    else -> default
}

private fun isHeaderLine(rawLine: String): Boolean {
    if (rawLine.contains("&#xF0")) return true
    val plain = Jsoup.parse(rawLine).text().trim()
    if (plain.contains("NOS Teletekst", ignoreCase = true)) return true
    val letters = plain.replace(" ", "")
    return letters.isNotEmpty()
        && letters.length in 2..20
        && letters.all { it.isLetter() && it.isUpperCase() }
        && plain.contains("  ")
}

private fun isFooterLine(rawLine: String): Boolean {
    val plain = Jsoup.parse(rawLine).text()
    return plain.contains("www.nos.nl", ignoreCase = true)
        || plain.contains("nieuwsoverzicht", ignoreCase = true)
}

private fun isBlankRawLine(rawLine: String): Boolean =
    Jsoup.parse(rawLine).text().isBlank()

private fun cleanPrivateUseChars(text: String): String = buildString {
    for (ch in text) {
        append(if (ch.code in 0xF000..0xF0FF) ' ' else ch)
    }
}
