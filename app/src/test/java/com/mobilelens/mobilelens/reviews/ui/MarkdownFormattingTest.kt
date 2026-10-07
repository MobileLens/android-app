package com.mobilelens.mobilelens.reviews.ui

import androidx.compose.ui.text.TextRange
import org.junit.Assert.assertEquals
import org.junit.Test

class MarkdownFormattingTest {

    /** Text with "|" marking a cursor or "[" and "]" marking a selection. */
    private fun parse(marked: String): Pair<String, TextRange> {
        val cursor = marked.indexOf('|')
        if (cursor >= 0) return marked.removeRange(cursor, cursor + 1) to TextRange(cursor)
        val start = marked.indexOf('[')
        val end = marked.indexOf(']') - 1
        return marked.replace("[", "").replace("]", "") to TextRange(start, end)
    }

    private fun mark(text: String, selection: TextRange): String =
        if (selection.collapsed) {
            text.substring(0, selection.start) + "|" + text.substring(selection.start)
        } else {
            text.substring(0, selection.min) + "[" +
                text.substring(selection.min, selection.max) + "]" + text.substring(selection.max)
        }

    private fun toggle(marked: String, style: MarkdownStyle): String {
        val (text, selection) = parse(marked)
        val edit = toggleStyle(text, selection, style)
        return mark(edit.applyTo(text), edit.selection)
    }

    private fun styles(marked: String): MarkdownStyles {
        val (text, selection) = parse(marked)
        return stylesAt(text, selection)
    }

    @Test
    fun toggle_wrapsSelection() {
        assertEquals("a **[bold]** b", toggle("a [bold] b", MarkdownStyle.Bold))
        assertEquals("a *[it]* b", toggle("a [it] b", MarkdownStyle.Italic))
        assertEquals("a <u>[u]</u> b", toggle("a [u] b", MarkdownStyle.Underline))
    }

    @Test
    fun toggle_unwrapsStyledSelection() {
        assertEquals("a [bold] b", toggle("a **[bold]** b", MarkdownStyle.Bold))
        assertEquals("a [it] b", toggle("a *[it]* b", MarkdownStyle.Italic))
        assertEquals("a [u] b", toggle("a <u>[u]</u> b", MarkdownStyle.Underline))
    }

    @Test
    fun toggle_unwrapsSelectionThatIncludesMarkers() {
        assertEquals("a [bold] b", toggle("a [**bold**] b", MarkdownStyle.Bold))
        assertEquals("a [u] b", toggle("a [<u>u</u>] b", MarkdownStyle.Underline))
    }

    @Test
    fun toggle_trimsWhitespaceFromSelection() {
        assertEquals("a **[word]** b", toggle("a[ word ]b", MarkdownStyle.Bold))
    }

    @Test
    fun toggle_cursorInWordStylesWholeWord() {
        assertEquals("a **wo|rd** b", toggle("a wo|rd b", MarkdownStyle.Bold))
        assertEquals("a wo|rd b", toggle("a **wo|rd** b", MarkdownStyle.Bold))
        assertEquals("**don't|**", toggle("don't|", MarkdownStyle.Bold))
    }

    @Test
    fun toggle_cursorOutsideWordInsertsEmptyMarkers() {
        assertEquals("a **|** b", toggle("a | b", MarkdownStyle.Bold))
        assertEquals("a | b", toggle("a **|** b", MarkdownStyle.Bold))
        assertEquals("*|*", toggle("|", MarkdownStyle.Italic))
    }

    @Test
    fun toggle_combinesBoldAndItalic() {
        assertEquals("***[x]***", toggle("**[x]**", MarkdownStyle.Italic))
        assertEquals("***[x]***", toggle("*[x]*", MarkdownStyle.Bold))
        assertEquals("*[x]*", toggle("***[x]***", MarkdownStyle.Bold))
        assertEquals("**[x]**", toggle("***[x]***", MarkdownStyle.Italic))
    }

    @Test
    fun toggle_keepsUnderlineInsideStars() {
        assertEquals("**<u>[x]</u>**", toggle("<u>[x]</u>", MarkdownStyle.Bold))
        assertEquals("**<u>[x]</u>**", toggle("**[x]**", MarkdownStyle.Underline))
        // Markers in the other order are normalized
        assertEquals("<u>[x]</u>", toggle("<u>**[x]**</u>", MarkdownStyle.Bold))
    }

    @Test
    fun stylesAt_detectsEnclosingMarkers() {
        assertEquals(MarkdownStyles(), styles("plain wo|rd"))
        assertEquals(MarkdownStyles(bold = true), styles("**wo|rd**"))
        assertEquals(MarkdownStyles(italic = true), styles("*wo|rd*"))
        assertEquals(MarkdownStyles(bold = true, italic = true), styles("***wo|rd***"))
        assertEquals(
            MarkdownStyles(bold = true, italic = true, underline = true),
            styles("***<u>wo|rd</u>***"),
        )
    }

    @Test
    fun stylesAt_ignoresMarkersOnOneSideOnly() {
        assertEquals(MarkdownStyles(), styles("**bold**|"))
        assertEquals(MarkdownStyles(), styles("* list i|tem"))
    }

    @Test
    fun stylesAt_doesNotMergeSeparateSpans() {
        assertEquals(MarkdownStyles(), styles("[*a* and *b*]"))
    }

    @Test
    fun insertImage_putsImageInItsOwnParagraph() {
        val url = "https://x/i.jpg"
        val (text, selection) = parse("before|after")
        val edit = insertImage(text, selection, url)
        assertEquals("before\n\n![]($url)\n\n|after", mark(edit.applyTo(text), edit.selection))
    }

    @Test
    fun insertImage_reusesExistingLineBreaks() {
        val url = "u"
        val (text, selection) = parse("a\n\n|\n\nb")
        val edit = insertImage(text, selection, url)
        assertEquals("a\n\n![](u)\n\n|b", mark(edit.applyTo(text), edit.selection))

        val (empty, start) = parse("|")
        val emptyEdit = insertImage(empty, start, url)
        assertEquals("![](u)\n\n|", mark(emptyEdit.applyTo(empty), emptyEdit.selection))
    }

    @Test
    fun insertImage_replacesSelection() {
        val (text, selection) = parse("a\n[old]\nb")
        val edit = insertImage(text, selection, "u")
        assertEquals("a\n\n![](u)\n\n|b", mark(edit.applyTo(text), edit.selection))
    }
}
