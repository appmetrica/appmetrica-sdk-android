package io.appmetrica.analytics.logger.impl;

import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class SystemLogger {

    @NonNull
    private final String tag;

    public SystemLogger(@NonNull final String tag) {
        this.tag = tag;
    }

    public void info(@Nullable String msg) {
        log(Log.INFO, msg);
    }

    public void warning(@Nullable String msg) {
        log(Log.WARN, msg);
    }

    public void error(@Nullable String msg) {
        log(Log.ERROR, msg);
    }

    private void log(final int priority, @Nullable final String message) {
        Log.println(priority, tag, message == null ? "" : message);
    }
}
