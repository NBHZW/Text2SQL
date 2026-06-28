package com.zealsinger.kotlin.agent.util


object MarkdownParserUtil {
    fun extractText(markdownCode: String): String {
        val code = extractRawText(markdownCode)
        // Correctly handle various newline character types: \r\n, \n, \r, but maintain
        // compatibility with NewLineParser.format()
        return code.replace("\r\n".toRegex(), " ").replace("\n".toRegex(), " ").replace("\r".toRegex(), " ")
    }

    fun extractRawText(markdownCode: String): String {
        // Find the start of a code block (3 or more backticks)
        var startIndex = -1
        var delimiterLength = 0

        for (i in 0..markdownCode.length - 3) {
            if (markdownCode.substring(i, i + 3) == "```") {
                startIndex = i
                delimiterLength = 3
                // Count additional backticks
                while (i + delimiterLength < markdownCode.length && markdownCode.get(i + delimiterLength) == '`') {
                    delimiterLength++
                }
                break
            }
        }

        if (startIndex == -1) {
            return markdownCode // No code block found
        }

        // Skip the opening delimiter and optional language specification
        var contentStart = startIndex + delimiterLength
        while (contentStart < markdownCode.length && markdownCode.get(contentStart) != '\n') {
            contentStart++
        }
        if (contentStart < markdownCode.length && markdownCode.get(contentStart) == '\n') {
            contentStart++ // Skip the newline after language spec
        }

        // Find the closing delimiter
        val closingDelimiter = "`".repeat(delimiterLength)
        val endIndex = markdownCode.indexOf(closingDelimiter, contentStart)

        if (endIndex == -1) {
            // No closing delimiter found, return from content start to end
            return markdownCode.substring(contentStart)
        }

        // Extract just the content between delimiters
        return markdownCode.substring(contentStart, endIndex)
    }
}