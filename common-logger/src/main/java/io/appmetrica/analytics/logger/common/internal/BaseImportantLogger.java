package io.appmetrica.analytics.logger.common.internal;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.logger.common.impl.LogMessageConstructor;
import io.appmetrica.analytics.logger.common.impl.LogMessageSplitter;
import io.appmetrica.analytics.logger.common.impl.MultilineLogger;
import io.appmetrica.analytics.logger.common.impl.SystemLogger;

/**
 * Class for release logs that should always be printed
 */
public abstract class BaseImportantLogger {

    private final MultilineLogger impl;

    /**
     * Constructor
     *
     * @param loggerTag String that is passed to system logger as tag
     */
    public BaseImportantLogger(@NonNull String loggerTag) {
        this.impl = new MultilineLogger(
            new SystemLogger(loggerTag),
            new LogMessageConstructor(),
            new LogMessageSplitter()
        );
    }

    /**
     * Logs message with arguments to info log in format `$tag $message`
     *
     * @param tag String identifying caller
     * @param message String with placeholders
     * @param args arguments
     */
    public void info(@NonNull String tag, @Nullable String message, @Nullable Object... args) {
        impl.info(tag, message, args);
    }
}
