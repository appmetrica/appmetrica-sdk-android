package io.appmetrica.analytics.coreapi.internal.executors;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;

public class InterruptionSafeThread extends Thread implements IInterruptionSafeThread {

    private volatile boolean mRunning = true;

    public InterruptionSafeThread() {}

    public InterruptionSafeThread(@NonNull Runnable r, @NonNull String name) {
        super(r, name);
    }

    public InterruptionSafeThread(@NonNull String name) {
        super(name);
    }

    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    public InterruptionSafeThread(@NonNull Runnable runnable) {
        super(runnable);
    }

    @Override
    public synchronized boolean isRunning() {
        return mRunning;
    }

    @Override
    public synchronized void stopRunning() {
        mRunning = false;
        interrupt();
    }
}
