package com.mobilelens.mobilelens.reviews.ui

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.ui.text.TextRange

// Markdown source editing behind the review editor's toolbar. The pure functions work on plain
// text + selection so they can be unit tested; the TextFieldState extensions apply them.
//
// Bold and italic use asterisks, so "***text***" is both. CommonMark has no underline, so it is
// written as inline HTML; the in-app Markdown renderer drops the tags and shows plain text.

enum class MarkdownStyle { Bold, Italic, Underline }

data class MarkdownStyles(
    val bold: Boolean = false,
    val italic: Boolean = false,
    val underline: Boolean = false,
)

/** Replaces `[start, end)` of the original text with [text]. */
internal data class Replacement(val start: Int, val end: Int, val text: String)

/** Replacements are ordered from the end of the text, so each one's offsets stay valid. */
internal data class MarkdownEdit(val replacements: List<Replacement>, val selection: TextRange)

internal fun MarkdownEdit.applyTo(text: String): String =
    replacements.fold(text) { acc, r -> acc.replaceRange(r.start, r.end, r.text) }

/**
 * The text a style applies to, `[innerStart, innerEnd)`, and the markers wrapping it,
 * `[outerStart, innerStart)` and `[innerEnd, outerEnd)`.
 */
internal data class StyledSpan(
    val outerStart: Int,
    val innerStart: Int,
    val innerEnd: Int,
    val outerEnd: Int,
    val styles: MarkdownStyles,
)

private const val UNDERLINE_OPEN = "<u>"
private const val UNDERLINE_CLOSE = "</u>"
private const val MAX_STARS = 3

internal fun stylesAt(text: CharSequence, selection: TextRange): MarkdownStyles =
    styledSpanAt(text, selection).styles

internal fun styledSpanAt(text: CharSequence, selection: TextRange): StyledSpan {
    val (start, end) = targetRange(text, selection)
    var innerStart = start
    var innerEnd = end
    var stars = 0
    var underline = false

    // The selection includes its own markers, e.g. all of "**bold**" is selected
    while (true) {
        if (!underline &&
            innerEnd - innerStart >= UNDERLINE_OPEN.length + UNDERLINE_CLOSE.length &&
            text.startsWith(UNDERLINE_OPEN, innerStart) &&
            text.endsWithAt(UNDERLINE_CLOSE, innerEnd) &&
            !text.subSequence(innerStart + UNDERLINE_OPEN.length, innerEnd - UNDERLINE_CLOSE.length)
                .contains(UNDERLINE_OPEN)
        ) {
            underline = true
            innerStart += UNDERLINE_OPEN.length
            innerEnd -= UNDERLINE_CLOSE.length
            continue
        }
        if (stars == 0) {
            val n = minOf(starsAfter(text, innerStart), starsBefore(text, innerEnd), MAX_STARS)
            // Skip when other emphasis is inside, e.g. "*a* and *b*" isn't one italic span
            if (n > 0 && innerEnd - innerStart > 2 * n &&
                !text.subSequence(innerStart + n, innerEnd - n).contains('*')
            ) {
                stars = n
                innerStart += n
                innerEnd -= n
                continue
            }
        }
        break
    }

    var outerStart = start
    var outerEnd = end
    while (true) {
        if (!underline &&
            text.endsWithAt(UNDERLINE_OPEN, outerStart) &&
            text.startsWith(UNDERLINE_CLOSE, outerEnd)
        ) {
            underline = true
            outerStart -= UNDERLINE_OPEN.length
            outerEnd += UNDERLINE_CLOSE.length
            continue
        }
        if (stars == 0) {
            val n = minOf(starsBefore(text, outerStart), starsAfter(text, outerEnd), MAX_STARS)
            if (n > 0) {
                stars = n
                outerStart -= n
                outerEnd += n
                continue
            }
        }
        break
    }

    return StyledSpan(
        outerStart = outerStart,
        innerStart = innerStart,
        innerEnd = innerEnd,
        outerEnd = outerEnd,
        styles = MarkdownStyles(
            bold = stars >= 2,
            italic = stars % 2 == 1,
            underline = underline,
        ),
    )
}

/**
 * Adds or removes [style] around the selection. A cursor inside a word styles the whole word;
 * a cursor elsewhere inserts empty markers and puts the cursor between them.
 */
internal fun toggleStyle(text: CharSequence, selection: TextRange, style: MarkdownStyle): MarkdownEdit {
    val span = styledSpanAt(text, selection)
    val current = span.styles
    val styles = when (style) {
        MarkdownStyle.Bold -> current.copy(bold = !current.bold)
        MarkdownStyle.Italic -> current.copy(italic = !current.italic)
        MarkdownStyle.Underline -> current.copy(underline = !current.underline)
    }

    // Markers are rewritten in one canonical order: stars outside, underline inside
    val stars = "*".repeat((if (styles.bold) 2 else 0) + (if (styles.italic) 1 else 0))
    val prefix = stars + if (styles.underline) UNDERLINE_OPEN else ""
    val suffix = (if (styles.underline) UNDERLINE_CLOSE else "") + stars

    val newInnerStart = span.outerStart + prefix.length
    val newSelection = if (selection.collapsed) {
        TextRange(selection.start + newInnerStart - span.innerStart)
    } else {
        TextRange(newInnerStart, newInnerStart + span.innerEnd - span.innerStart)
    }

    return MarkdownEdit(
        replacements = listOf(
            Replacement(span.innerEnd, span.outerEnd, suffix),
            Replacement(span.outerStart, span.innerStart, prefix),
        ),
        selection = newSelection,
    )
}

/** Replaces the selection with an image in a paragraph of its own, with the cursor in the next one. */
internal fun insertImage(text: CharSequence, selection: TextRange, url: String): MarkdownEdit {
    val start = selection.min.coerceIn(0, text.length)
    val end = selection.max.coerceIn(0, text.length)
    val before = text.subSequence(0, start)
    val after = text.subSequence(end, text.length)

    val leading = when {
        before.isEmpty() || before.endsWith("\n\n") -> ""
        before.endsWith("\n") -> "\n"
        else -> "\n\n"
    }
    // Line breaks already after the selection are reused, and the cursor goes past them too
    val existingBreaks = after.takeWhile { it == '\n' }.length.coerceAtMost(2)
    val inserted = "$leading![]($url)" + "\n".repeat(2 - existingBreaks)

    return MarkdownEdit(
        replacements = listOf(Replacement(start, end, inserted)),
        selection = TextRange(start + inserted.length + existingBreaks),
    )
}

fun TextFieldState.markdownStyles(): MarkdownStyles = stylesAt(text, selection)

fun TextFieldState.toggleMarkdownStyle(style: MarkdownStyle) =
    applyMarkdownEdit(toggleStyle(text, selection, style))

fun TextFieldState.insertMarkdownImage(url: String) =
    applyMarkdownEdit(insertImage(text, selection, url))

private fun TextFieldState.applyMarkdownEdit(markdownEdit: MarkdownEdit) {
    edit {
        markdownEdit.replacements.forEach { replace(it.start, it.end, it.text) }
        selection = markdownEdit.selection
    }
}

/**
 * The range a style applies to: the selection without surrounding whitespace (emphasis can't
 * start or end with a space), or, for a cursor, the word it touches.
 */
private fun targetRange(text: CharSequence, selection: TextRange): Pair<Int, Int> {
    val start = selection.min.coerceIn(0, text.length)
    val end = selection.max.coerceIn(0, text.length)

    if (start == end) {
        var wordStart = start
        while (wordStart > 0 && text[wordStart - 1].isWordChar()) wordStart--
        var wordEnd = end
        while (wordEnd < text.length && text[wordEnd].isWordChar()) wordEnd++
        return wordStart to wordEnd
    }

    var trimmedStart = start
    var trimmedEnd = end
    while (trimmedStart < trimmedEnd && text[trimmedStart].isWhitespace()) trimmedStart++
    while (trimmedEnd > trimmedStart && text[trimmedEnd - 1].isWhitespace()) trimmedEnd--
    return trimmedStart to trimmedEnd
}

private fun Char.isWordChar() = isLetterOrDigit() || this == '\'' || this == '’'

private fun starsBefore(text: CharSequence, index: Int): Int {
    var i = index
    while (i > 0 && text[i - 1] == '*') i--
    return index - i
}

private fun starsAfter(text: CharSequence, index: Int): Int {
    var i = index
    while (i < text.length && text[i] == '*') i++
    return i - index
}

private fun CharSequence.endsWithAt(suffix: String, endIndex: Int): Boolean =
    endIndex >= suffix.length && regionMatches(endIndex - suffix.length, suffix, 0, suffix.length)
