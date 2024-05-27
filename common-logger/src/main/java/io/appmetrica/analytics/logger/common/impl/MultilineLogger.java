package io.appmetrica.analytics.logger.common.impl;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.util.List;

public class MultilineLogger {

    @NonNull
    private final SystemLogger logger;
    @NonNull
    private final LogMessageConstructor constructor;
    @NonNull
    private final LogMessageSplitter splitter;

    public MultilineLogger(
        @NonNull SystemLogger logger,
        @NonNull LogMessageConstructor constructor,
        @NonNull LogMessageSplitter splitter
    ) {
        this.logger = logger;
        this.constructor = constructor;
        this.splitter = splitter;
    }

    public void info(@NonNull String tag, @Nullable String message, @Nullable Object... args) {
        final String constructedMessage = constructor.construct(tag, message, args);
        final List<String> messageLines = splitter.split(constructedMessage);
        for (String messageLine: messageLines) {
            logger.info(messageLine);
        }
    }

    public void warning(@NonNull String tag, @Nullable String message, @Nullable Object... args) {
        final String constructedMessage = constructor.construct(tag, message, args);
        final List<String> messageLines = splitter.split(constructedMessage);
        for (String messageLine: messageLines) {
            logger.warning(messageLine);
        }
    }

    public void error(@NonNull String tag, @Nullable String message, @Nullable Object... args) {
        final String constructedMessage = constructor.construct(tag, message, args);
        final List<String> messageLines = splitter.split(constructedMessage);
        for (String messageLine: messageLines) {
            logger.error(messageLine);
        }
    }

    public void error(@NonNull String tag, @Nullable Throwable e, @Nullable String message, @Nullable Object... args) {
        final String constructedMessage = constructor.construct(tag, e, message, args);
        final List<String> messageLines = splitter.split(constructedMessage);
        for (String messageLine: messageLines) {
            logger.error(messageLine);
        }
    }
}
