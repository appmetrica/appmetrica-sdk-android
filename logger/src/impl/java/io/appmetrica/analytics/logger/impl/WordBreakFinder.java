package io.appmetrica.analytics.logger.impl;

import androidx.annotation.NonNull;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class WordBreakFinder {

    private final Pattern goodLineEndPattern = Pattern.compile("[\\p{Space},;]");

    public int find(@NonNull String input, int startOffset, int endOffset) {
        Matcher matcher = goodLineEndPattern.matcher(input);
        return findLastIndex(matcher, startOffset, endOffset);
    }

    private int findLastIndex(@NonNull Matcher matcher, int start, int end) {
        int result = -1;

        if (end >= start) {
            int baseLine = (end - start) / 2 + start;
            matcher.region(baseLine, end);
            if (matcher.find()) {
                result = findLastIndex(matcher, end);
            } else {
                matcher.region(start, baseLine);
                if (matcher.find()) {
                    result = findLastIndex(matcher, baseLine);
                }
            }
        }

        return result;
    }

    private int findLastIndex(@NonNull Matcher matcher, int end) {
        int firstFound = matcher.start();
        int closerFound = findLastIndex(matcher, firstFound + 1, end);
        return closerFound == -1 ? firstFound : closerFound;
    }
}
