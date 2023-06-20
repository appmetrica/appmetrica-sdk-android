package io.appmetrica.analytics.coreutils.internal.logger;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.coreutils.DebugProvider;
import org.json.JSONObject;

public final class YLogger {

    public static final boolean DEBUG = DebugProvider.DEBUG;

    private static YLoggerImpl impl = new YLoggerImpl(new DebugLogger(DEBUG), DEBUG);

    private YLogger() {
    }

    public static void debug(@NonNull String tag, @NonNull String message, Object... args) {
        if (DEBUG) {
            impl.debug(tag, message, args);
        }
    }

    public static void d(@NonNull String msg, Object... args) {
        if (DEBUG) {
            impl.d(msg, args);
        }
    }

    public static void i(@NonNull String msg, Object... args) {
        if (DEBUG) {
            impl.i(msg, args);
        }
    }

    public static void info(@NonNull String tag, @NonNull String message, Object... args) {
        if (DEBUG) {
            impl.info(tag, message, args);
        }
    }

    public static void w(@NonNull String msg, final Object... args) {
        if (DEBUG) {
            impl.w(msg, args);
        }
    }

    public static void warning(@NonNull String tag, @NonNull String msg, Object... args) {
        if (DEBUG) {
            impl.warning(tag, msg, args);
        }
    }

    public static void e(@NonNull String msg, final Object... args) {
        if (DEBUG) {
            impl.e(msg, args);
        }
    }

    public static void error(@NonNull String tag, @NonNull String msg, Object... args) {
        if (DEBUG) {
            impl.error(tag, msg, args);
        }
    }

    public static void e(@NonNull Throwable e, @Nullable String msg, final Object... args) {
        if (DEBUG) {
            impl.e(e, msg, args);
        }
    }

    public static void error(@NonNull String tag, @Nullable Throwable e, @Nullable String msg, Object... args) {
        if (DEBUG) {
            impl.error(tag, e, msg, args);
        }
    }

    public static void error(@NonNull String tag, @Nullable Throwable e) {
        error(tag, e, null);
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
    static YLoggerImpl getImpl() {
        return impl;
    }
}
