package io.appmetrica.analytics.logger.internal;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import org.json.JSONObject;

/**
 * Class for debug logs
 */
public final class DebugLogger {

    private static final boolean DEBUG = false;

    private DebugLogger() {}

    /**
     * Logs message with arguments to debug log
     *
     * @param tag String identifying caller
     * @param message String with placeholders
     * @param args arguments
     */
    public static void debug(@NonNull String tag, @NonNull String message, @Nullable Object... args) {
        // Do nothing
    }

    /**
     * Logs message with arguments to info log
     *
     * @param tag String identifying caller
     * @param message String with placeholders
     * @param args arguments
     */
    public static void info(@NonNull String tag, @NonNull String message, @Nullable Object... args) {
        // Do nothing
    }

    /**
     * Logs message with arguments to warning log
     *
     * @param tag String identifying caller
     * @param message String with placeholders
     * @param args arguments
     */
    public static void warning(@NonNull String tag, @NonNull String message, @Nullable Object... args) {
        // Do nothing
    }

    /**
     * Logs message with arguments to error log
     *
     * @param tag String identifying caller
     * @param message String with placeholders
     * @param args arguments
     */
    public static void error(@NonNull String tag, @NonNull String message, @Nullable Object... args) {
        // Do nothing
    }

    /**
     * Logs throwable and message with arguments to error log
     *
     * @param tag String identifying caller
     * @param throwable Throwable to log
     * @param message String with placeholders
     * @param args arguments
     */
    public static void error(
        @NonNull String tag,
        @Nullable Throwable throwable,
        @Nullable String message,
        @Nullable Object... args
    ) {
        // Do nothing
    }

    /**
     * Logs throwable to error log
     *
     * @param tag String identifying caller
     * @param throwable Throwable to log
     */
    public static void error(@NonNull String tag, @Nullable Throwable throwable) {
        // Do nothing
    }

    /**
     * Logs json to info log
     *
     * @param tag String identifying caller
     * @param jsonObject JSONObject to log
     */
    public static void dumpJson(@NonNull String tag, @NonNull JSONObject jsonObject) {
        // Do nothing
    }
}
