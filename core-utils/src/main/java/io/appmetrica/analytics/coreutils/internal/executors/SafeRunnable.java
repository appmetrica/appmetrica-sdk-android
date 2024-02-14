package io.appmetrica.analytics.coreutils.internal.executors;

import io.appmetrica.analytics.logger.internal.YLogger;

public abstract class SafeRunnable implements Runnable {

    public void run() {
        try {
            runSafety();
        } catch (Throwable e) {
            YLogger.e(e, e.getMessage());
        }
    }

    public abstract void runSafety() throws Throwable;
}
