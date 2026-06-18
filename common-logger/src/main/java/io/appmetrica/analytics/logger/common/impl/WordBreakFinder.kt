package io.appmetrica.analytics.logger.common.impl

import java.util.regex.Matcher
import java.util.regex.Pattern

internal class WordBreakFinder {

    private val goodLineEndPattern: Pattern by lazy {
        Pattern.compile("[\\p{Space},;]")
    }

    fun find(input: String, startOffset: Int, endOffset: Int): Int {
        return findLastIndex(goodLineEndPattern.matcher(input), startOffset, endOffset)
    }

    private fun findLastIndex(matcher: Matcher, start: Int, end: Int): Int {
        if (end < start) {
            return -1
        }
        val baseLine = (end - start) / 2 + start
        matcher.region(baseLine, end)
        return if (matcher.find()) {
            findLastIndex(matcher, end)
        } else {
            matcher.region(start, baseLine)
            if (matcher.find()) {
                findLastIndex(matcher, baseLine)
            } else {
                -1
            }
        }
    }

    private fun findLastIndex(matcher: Matcher, end: Int): Int {
        val firstFound = matcher.start()
        val closerFound = findLastIndex(matcher, firstFound + 1, end)
        return if (closerFound == -1) firstFound else closerFound
    }
}
