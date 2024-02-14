package io.appmetrica.analytics.logger.impl;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import java.util.ArrayList;
import java.util.List;

class LogMessageByLineLimitSplitter implements ILogMessageSplitter {

    static final int SINGLE_LOG_ANDROID_LIMIT = 4000;
    static final int LOG_CLASS_AND_METHOD_TAG_RESERVE = 200;
    static final int DEFAULT_SINGLE_LOG_LIMIT = SINGLE_LOG_ANDROID_LIMIT - LOG_CLASS_AND_METHOD_TAG_RESERVE;

    private final int lineLimit;
    @NonNull
    private final WordBreakFinder wordBreakFinder;

    public LogMessageByLineLimitSplitter() {
        this(new WordBreakFinder(), DEFAULT_SINGLE_LOG_LIMIT);
    }

    @VisibleForTesting
    LogMessageByLineLimitSplitter(@NonNull WordBreakFinder wordBreakFinder, int lineLimit) {
        this.wordBreakFinder = wordBreakFinder;
        this.lineLimit = lineLimit;
    }

    @Override
    public List<String> split(@NonNull String input) {
        List<String> result = new ArrayList<String>();
        int currentOffset = 0;
        while (input.length() > currentOffset) {
            int chunkEndOffset = input.length();
            int maxOffset = Math.min(chunkEndOffset, currentOffset + lineLimit);
            if (chunkEndOffset > currentOffset + lineLimit) {
                int wordBreakPosition = wordBreakFinder.find(input, currentOffset, maxOffset);
                chunkEndOffset = wordBreakPosition == -1 ? maxOffset : wordBreakPosition + 1;
            }
            result.add(input.substring(currentOffset, chunkEndOffset));
            currentOffset = chunkEndOffset;
        }

        return result;
    }
}
