package io.appmetrica.analytics.logger.common.impl;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import java.util.ArrayList;
import java.util.List;

public class LogMessageSplitter {

    private static final String DEFAULT_REGEX = "\\n";
    private static final int SINGLE_LOG_ANDROID_LIMIT = 4000;
    private static final int LOG_CLASS_AND_METHOD_TAG_RESERVE = 200;
    private static final int DEFAULT_SINGLE_LOG_LIMIT = SINGLE_LOG_ANDROID_LIMIT - LOG_CLASS_AND_METHOD_TAG_RESERVE;

    private final int lineLimit;
    @NonNull
    private final WordBreakFinder wordBreakFinder;

    public LogMessageSplitter() {
        this(new WordBreakFinder(), DEFAULT_SINGLE_LOG_LIMIT);
    }

    @VisibleForTesting
    LogMessageSplitter(@NonNull WordBreakFinder wordBreakFinder, int lineLimit) {
        this.wordBreakFinder = wordBreakFinder;
        this.lineLimit = lineLimit;
    }

    public List<String> split(@NonNull String input) {
        String[] newLineSplit = input.split(DEFAULT_REGEX);

        List<String> result = new ArrayList<>();

        for (String line : newLineSplit) {
            int currentOffset = 0;
            while (line.length() > currentOffset) {
                int chunkEndOffset = line.length();
                int maxOffset = Math.min(chunkEndOffset, currentOffset + lineLimit);
                if (chunkEndOffset > currentOffset + lineLimit) {
                    int wordBreakPosition = wordBreakFinder.find(line, currentOffset, maxOffset);
                    chunkEndOffset = wordBreakPosition == -1 ? maxOffset : wordBreakPosition + 1;
                }
                result.add(line.substring(currentOffset, chunkEndOffset));
                currentOffset = chunkEndOffset;
            }
        }

        return result;
    }
}
