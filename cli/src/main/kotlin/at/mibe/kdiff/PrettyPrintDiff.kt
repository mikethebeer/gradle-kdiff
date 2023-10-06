package at.mibe.kdiff

import com.github.difflib.DiffUtils
import com.github.difflib.patch.Patch

fun findTextDifferences(text1: String, text2: String): Patch<String> {
    val text1Lines = text1.lines()
    val text2Lines = text2.lines()

    return DiffUtils.diff(text1Lines, text2Lines)
}

fun generateInlineDiff(text1: String, patch: Patch<String>): String {
    val lines1 = text1.lines()
    val highlightedLines = mutableListOf<String>()

    var currentLine = 0

    for (delta in patch.deltas) {
        // Process lines before the diff
        while (currentLine < delta.source.position) {
            highlightedLines.add(lines1[currentLine])
            currentLine++
        }

        // Process the deleted lines (red color)
        for (i in delta.source.position until delta.source.position + delta.source.size()) {
            val deletedLine = "\u001B[31m-${lines1[i]}\u001B[0m" // Red for deletions
            highlightedLines.add(deletedLine)
        }

        // Process the inserted lines (green color)
        for (i in delta.target.position until delta.target.position + delta.target.size()) {
            val insertedLine =
                "\u001B[32m+${delta.target.lines[i - delta.target.position]}\u001B[0m" // Green for insertions
            highlightedLines.add(insertedLine)
        }

        currentLine = delta.source.position + delta.source.size()
    }

    // Add remaining lines after the last diff
    while (currentLine < lines1.size) {
        highlightedLines.add(lines1[currentLine])
        currentLine++
    }

    return highlightedLines.joinToString("\n")
}
