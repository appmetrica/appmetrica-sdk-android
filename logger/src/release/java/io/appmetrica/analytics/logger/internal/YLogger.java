package io.appmetrica.analytics.logger.internal;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import org.json.JSONObject;

public final class YLogger {

    public static final boolean DEBUG = false;

    private YLogger() {
    }

    public static void debug(@NonNull String tag, @NonNull String message, Object... args) {
        // Do nothing
    }

    public static void d(@NonNull String msg, Object... args) {
        // Do nothing
    }

    public static void i(@NonNull String msg, Object... args) {
        // Do nothing
    }

    public static void info(@NonNull String tag, @NonNull String message, Object... args) {
        // Do nothing
    }

    public static void w(@NonNull String msg, final Object... args) {
        // Do nothing
    }

    public static void warning(@NonNull String tag, @NonNull String msg, Object... args) {
        // Do nothing
    }

    public static void e(@NonNull String msg, final Object... args) {
        // Do nothing
    }

    public static void error(@NonNull String tag, @NonNull String msg, Object... args) {
        // Do nothing
    }

    public static void e(@NonNull Throwable e, @Nullable String msg, final Object... args) {
        // Do nothing
    }

    public static void error(@NonNull String tag, @Nullable Throwable e, @Nullable String msg, Object... args) {
        // Do nothing
    }

    public static void error(@NonNull String tag, @Nullable Throwable e) {
        // Do nothing
    }

    public static void dumpJson(@NonNull String tag, @NonNull JSONObject jsonObject) {
        // Do nothing
    }
}
