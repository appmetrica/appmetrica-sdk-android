package io.appmetrica.analytics.impl.utils.executors;

import android.os.HandlerThread;
import androidx.annotation.NonNull;
import io.appmetrica.analytics.coreapi.internal.executors.IInterruptionSafeThread;

public class InterruptionSafeHandlerThread extends HandlerThread implements IInterruptionSafeThread {

    private volatile boolean mRunning = true;

    public InterruptionSafeHandlerThread(@NonNull String name) {
        super(name);
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
