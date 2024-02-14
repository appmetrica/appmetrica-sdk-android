package io.appmetrica.analytics.impl.utils;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.coreutils.internal.StringUtils;
import io.appmetrica.analytics.impl.SdkUtils;
import io.appmetrica.analytics.impl.Utils;
import java.util.Locale;

public abstract class LoggerWithApiKey {

    private volatile static String sLogPrefix = StringUtils.EMPTY;
    @NonNull
    private final String mPartialApiKey;
    private volatile boolean enabled = false;

    public LoggerWithApiKey(@Nullable String fullApiKey) {
        mPartialApiKey = "[" + Utils.createPartialApiKey(fullApiKey) + "] ";
    }

    @NonNull
    public String getPrefix() {
        String part1 = StringUtils.ifIsNullToDef(sLogPrefix, StringUtils.EMPTY);
        String part2 = StringUtils.ifIsNullToDef(mPartialApiKey, StringUtils.EMPTY);
        return part1 + part2;
    }

    public static void init(final Context context) {
        sLogPrefix = "[" + context.getPackageName() + "] : ";
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void i(String message) {
        log(Log.INFO, message);
    }

    public void fi(String message, Object... params) {
        log(Log.INFO, message, params);
    }

    public void w(String message) {
        log(Log.WARN, message);
    }

    public void fw(String message, Object... params) {
        log(Log.WARN, message, params);
    }

    public void e(Throwable throwable, String message) {
        log(Log.ERROR, throwable, message);
    }

    public void fe(Throwable throwable, String message, Object... params) {
        log(Log.ERROR, throwable, message, params);
    }

    public void e(String message) {
        log(Log.ERROR, message);
    }

    public void fe(String message, Object... params) {
        log(Log.ERROR, message, params);
    }

    void log(int priority, @Nullable String message) {
        if (enabled) {
            Log.println(priority, SdkUtils.APPMETRICA_TAG, getPrefix() + wrapMessage(message));
        }
    }

    void log(int priority, @Nullable String message, Object... parameters) {
        if (enabled) {
            Log.println(
                priority,
                SdkUtils.APPMETRICA_TAG,
                getPrefix()  + formatMessage(wrapMessage(message), parameters)
            );
        }
    }

    void log(int priority, @Nullable Throwable throwable, @Nullable String message, Object... params) {
        if (enabled) {
            Log.println(
                priority,
                SdkUtils.APPMETRICA_TAG,
                getPrefix() + formatMessage(wrapMessage(message), params) + "\n" +
                    Log.getStackTraceString(throwable)
            );
        }
    }

    void log(int priority, @Nullable Throwable throwable, @Nullable String message) {
        if (enabled) {
            Log.println(
                priority,
                SdkUtils.APPMETRICA_TAG,
                getPrefix() + wrapMessage(message) + "\n" + Log.getStackTraceString(throwable)
            );
        }
    }

    @NonNull
    private String wrapMessage(@Nullable String message) {
        return message == null ? "" : message;
    }

    private String formatMessage(final String message, final Object[] params) {
        return String.format(Locale.US, message, params);
    }
    
    @VisibleForTesting
    static void reset() {
        sLogPrefix = StringUtils.EMPTY;
    }
}
