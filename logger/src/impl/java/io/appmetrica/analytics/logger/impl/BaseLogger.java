package io.appmetrica.analytics.logger.impl;

import android.util.Log;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

abstract class BaseLogger {

    private volatile boolean mIsEnabled = false;

    public void setEnabled() {
        mIsEnabled = true;
    }

    public void setDisabled() {
        mIsEnabled = false;
    }

    public boolean isEnabled() {
        return mIsEnabled;
    }

    public BaseLogger(final boolean isEnabled) {
        mIsEnabled = isEnabled;
    }

    public void d(String msg) {
        log(Log.DEBUG, wrapMsg(msg));
    }

    public void i(String msg) {
        log(Log.INFO, msg);
    }

    public void w(String msg) {
        log(Log.WARN, msg);
    }

    public void e(String msg) {
        log(Log.ERROR, msg);
    }

    public void e(Throwable throwable, String msg) {
        log(Log.ERROR, throwable, msg);
    }

    public void fd(String msg, Object ... params) {
        log(Log.DEBUG, wrapMsg(msg), params);
    }

    public void fi(String msg, Object ... params) {
        log(Log.INFO, msg, params);
    }

    public void fw(String msg, Object ... params) {
        log(Log.WARN, msg, params);
    }

    public void fe(String msg, Object ... params) {
        log(Log.ERROR, msg, params);
    }

    public void fe(Throwable throwable, String msg, Object ... params) {
        log(Log.ERROR, throwable, msg, params);
    }

    public void forceI(String msg, Object... params) {
        Log.println(Log.INFO, getTag(), getFormattedMessage(msg, params));
    }

    public void forceW(String msg, Object ... params) {
        Log.println(Log.WARN, getTag(), getFormattedMessage(msg, params));
    }

    public void forceE(Throwable throwable, String msg, Object... params) {
        Log.println(Log.ERROR, getTag(), getFormattedMessage(msg, params) + "\n" +
                Log.getStackTraceString(throwable));
    }

    void log(final int priority, final String message) {
        if (shouldLog()) {
            Log.println(priority, getTag(), getMessage(message));
        }
    }

    void log(final int priority, final String message, Object... params) {
        if (shouldLog()) {
            Log.println(priority, getTag(), getFormattedMessage(message, params));
        }
    }

    void log(final int priority, @Nullable final Throwable throwable,  final String message) {
        if (shouldLog()) {
            Log.println(priority, getTag(), getMessage(message) + "\n" + Log.getStackTraceString(throwable));
        }
    }

    void log(final int priority, @Nullable final Throwable throwable, final String message, Object... params) {
        if (shouldLog()) {
            Log.println(priority, getTag(), getFormattedMessage(message, params) + "\n" +
                    Log.getStackTraceString(throwable));
        }
    }

    protected boolean shouldLog() {
        return mIsEnabled;
    }

    private String getMessage(final String message) {
        return getPrefix() + wrapMsg(message);
    }

    private String getFormattedMessage(final String message, final Object[] params) {
        try {
            return getPrefix() + formatMessage(wrapMsg(message), params);
        } catch (Throwable ignored) {
            return onFormatException();
        }
    }

    @VisibleForTesting
    String onFormatException() {
        return getPrefix();
    }

    private static String wrapMsg(String msg) {
        return msg == null ? "" : msg;
    }

    protected abstract String getTag();

    protected abstract String getPrefix();

    protected abstract String formatMessage(final String message, final Object[] params);

}
