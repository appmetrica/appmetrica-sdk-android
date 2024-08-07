package io.appmetrica.analytics.coreutils.internal.executors;

import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger;

public abstract class SafeRunnable implements Runnable {

    private static final String TAG = "[SafeRunnable]";

    public void run() {
        try {
            runSafety();
        } catch (Throwable e) {
            DebugLogger.INSTANCE.error(TAG, e, e.getMessage());
        }
    }

    public abstract void runSafety() throws Throwable;
}
