package io.appmetrica.analytics.logger.internal;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.logger.impl.DebugLogger;
import io.appmetrica.analytics.logger.impl.YLoggerImpl;
import org.json.JSONObject;

public final class YLogger {

    public static final boolean DEBUG = true;

    private static YLoggerImpl impl = new YLoggerImpl(new DebugLogger(DEBUG), DEBUG);

    private YLogger() {
    }

    public static void debug(@NonNull String tag, @NonNull String message, Object... args) {
        if (DEBUG) {
            impl.debug(tag, message, args);
        }
    }

    public static void info(@NonNull String tag, @NonNull String message, Object... args) {
        if (DEBUG) {
            impl.info(tag, message, args);
        }
    }

    public static void warning(@NonNull String tag, @NonNull String msg, Object... args) {
        if (DEBUG) {
            impl.warning(tag, msg, args);
        }
    }

    public static void error(@NonNull String tag, @NonNull String msg, Object... args) {
        if (DEBUG) {
            impl.error(tag, msg, args);
        }
    }

    public static void error(@NonNull String tag, @Nullable Throwable e, @Nullable String msg, Object... args) {
        if (DEBUG) {
            impl.error(tag, e, msg, args);
        }
    }

    public static void error(@NonNull String tag, @Nullable Throwable e) {
        if (DEBUG) {
            error(tag, e, null);
        }
    }

    public static void dumpJson(@NonNull String tag, @NonNull JSONObject jsonObject) {
        if (DEBUG) {
            impl.dumpJson(tag, jsonObject);
        }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    public static void setImpl(@NonNull YLoggerImpl loggerImpl) {
        impl = loggerImpl;
    }

    @VisibleForTesting
    public static YLoggerImpl getImpl() {
        return impl;
    }
}
