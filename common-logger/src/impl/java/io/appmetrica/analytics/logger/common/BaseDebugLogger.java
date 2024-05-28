package io.appmetrica.analytics.logger.common;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.logger.common.impl.LogMessageConstructor;
import io.appmetrica.analytics.logger.common.impl.LogMessageSplitter;
import io.appmetrica.analytics.logger.common.impl.MultilineLogger;
import io.appmetrica.analytics.logger.common.impl.SystemLogger;

/**
 * Class for debug logs that logs messages
 */
public abstract class BaseDebugLogger {

    @NonNull
    private final MultilineLogger impl;

    /**
     * Constructor
     *
     * @param loggerTag String that is passed to system logger as tag
     */
    public BaseDebugLogger(@NonNull String loggerTag) {
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

    /**
     * Logs message with arguments to warning log in format `$tag $message`
     *
     * @param tag String identifying caller
     * @param message String with placeholders
     * @param args arguments
     */
    public void warning(@NonNull String tag, @Nullable String message, @Nullable Object... args) {
        impl.warning(tag, message, args);
    }

    /**
     * Logs message with arguments to error log in format `$tag $message`
     *
     * @param tag String identifying caller
     * @param message String with placeholders
     * @param args arguments
     */
    public void error(@NonNull String tag, @Nullable String message, @Nullable Object... args) {
        impl.error(tag, message, args);
    }

    /**
     * Logs throwable and message with arguments to error log in format `$tag $message $throwable`
     *
     * @param tag String identifying caller
     * @param throwable Throwable to log
     * @param message String with placeholders
     * @param args arguments
     */
    public void error(
        @NonNull String tag,
        @Nullable Throwable throwable,
        @Nullable String message,
        @Nullable Object... args
    ) {
        impl.error(tag, throwable, message, args);
    }

    /**
     * Logs throwable to error log in format `$tag $throwable`
     *
     * @param tag String identifying caller
     * @param throwable Throwable to log
     */
    public void error(@NonNull String tag, @Nullable Throwable throwable) {
        impl.error(tag, throwable, null);
    }
}
