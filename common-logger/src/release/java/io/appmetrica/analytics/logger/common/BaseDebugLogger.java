package io.appmetrica.analytics.logger.common;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Class for debug logs that does nothing
 */
public abstract class BaseDebugLogger {

    /**
     * Constructor
     *
     * @param loggerTag String for tag
     */
    public BaseDebugLogger(@NonNull String loggerTag) {
        // Do nothing
    }

    /**
     * Does nothing
     *
     * @param tag String identifying caller
     * @param message String with placeholders
     * @param args arguments
     */
    public void info(@NonNull String tag, @Nullable String message, @Nullable Object... args) {
        // Do nothing
    }

    /**
     * Does nothing
     *
     * @param tag String identifying caller
     * @param message String with placeholders
     * @param args arguments
     */
    public void warning(@NonNull String tag, @Nullable String message, @Nullable Object... args) {
        // Do nothing
    }

    /**
     * Does nothing
     *
     * @param tag String identifying caller
     * @param message String with placeholders
     * @param args arguments
     */
    public void error(@NonNull String tag, @Nullable String message, @Nullable Object... args) {
        // Do nothing
    }

    /**
     * Does nothing
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
        // Do nothing
    }

    /**
     * Does nothing
     *
     * @param tag String identifying caller
     * @param throwable Throwable to log
     */
    public void error(@NonNull String tag, @Nullable Throwable throwable) {
        // Do nothing
    }
}
