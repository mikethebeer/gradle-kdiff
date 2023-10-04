package at.mibe.gradle.kdiff

import com.github.difflib.DiffUtils
import com.github.difflib.patch.Patch

fun findTextDifferences(text1: String, text2: String): Patch<String> {
    val text1Lines = text1.lines()
    val text2Lines = text2.lines()

    return DiffUtils.diff(text1Lines, text2Lines)
}

fun generateInlineDiff(text1: String, patch: Patch<String>, contextLines: Int): String {
    val lines1 = text1.lines()
    val highlightedLines = mutableListOf<String>()

    for (delta in patch.deltas) {
        // Calculate the start and end indices for the context lines
        val startLine = maxOf(delta.source.position - contextLines, 0)
        val endLine = minOf(delta.source.position + delta.source.size() + contextLines, lines1.size)

        // Add context lines before the diff
        for (i in startLine until delta.source.position) {
            val contextLine = lines1[i]
            highlightedLines.add(" $contextLine") // Space indicates context lines
        }

        // Process the deleted lines (red color)
        for (i in delta.source.position until delta.source.position + delta.source.size()) {
            val deletedLine = "\u001B[31m-${lines1[i]}\u001B[0m" // Red for deletions
            highlightedLines.add(deletedLine)
        }

        // Process the inserted lines (green color)
        for (i in delta.target.position until delta.target.position + delta.target.size()) {
            val insertedLine = "\u001B[32m+${delta.target.lines[i - delta.target.position]}\u001B[0m" // Green for insertions
            highlightedLines.add(insertedLine)
        }

        // Add context lines after the diff
        for (i in delta.source.position + delta.source.size() until endLine) {
            val contextLine = lines1[i]
            highlightedLines.add(" $contextLine") // Space indicates context lines
        }

        highlightedLines.add("=====================================")
    }

    return highlightedLines.joinToString("\n")
}
