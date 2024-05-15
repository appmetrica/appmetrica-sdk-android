package io.appmetrica.analytics.logger.internal;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.logger.impl.SystemLogger;
import io.appmetrica.analytics.logger.impl.MultilineLogger;
import io.appmetrica.analytics.logger.impl.LogMessageConstructor;
import io.appmetrica.analytics.logger.impl.LogMessageSplitter;
import org.json.JSONObject;

/**
 * Class for debug logs
 */
public final class DebugLogger {

    private static final boolean DEBUG = true;
    private static final String LOGGER_TAG = "AppMetricaDebug";

    private static final MultilineLogger impl = new MultilineLogger(
        new SystemLogger(LOGGER_TAG),
        new LogMessageConstructor(),
        new LogMessageSplitter()
    );

    private DebugLogger() {}

    /**
     * Logs message with arguments to info log
     *
     * @param tag String identifying caller
     * @param message String with placeholders
     * @param args arguments
     */
    public static void info(@NonNull String tag, @NonNull String message, @Nullable Object... args) {
        if (DEBUG) {
            impl.info(tag, message, args);
        }
    }

    /**
     * Logs message with arguments to warning log
     *
     * @param tag String identifying caller
     * @param message String with placeholders
     * @param args arguments
     */
    public static void warning(@NonNull String tag, @NonNull String message, @Nullable Object... args) {
        if (DEBUG) {
            impl.warning(tag, message, args);
        }
    }

    /**
     * Logs message with arguments to error log
     *
     * @param tag String identifying caller
     * @param message String with placeholders
     * @param args arguments
     */
    public static void error(@NonNull String tag, @NonNull String message, @Nullable Object... args) {
        if (DEBUG) {
            impl.error(tag, message, args);
        }
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
        if (DEBUG) {
            impl.error(tag, throwable, message, args);
        }
    }

    /**
     * Logs throwable to error log
     *
     * @param tag String identifying caller
     * @param throwable Throwable to log
     */
    public static void error(@NonNull String tag, @Nullable Throwable throwable) {
        if (DEBUG) {
            impl.error(tag, throwable, null);
        }
    }

    /**
     * Logs json to info log
     *
     * @param tag String identifying caller
     * @param jsonObject JSONObject to log
     */
    public static void dumpJson(@NonNull String tag, @NonNull JSONObject jsonObject) {
        if (DEBUG) {
            impl.dumpJson(tag, jsonObject);
        }
    }
}
