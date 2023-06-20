package io.appmetrica.analytics.impl.crash;

import android.os.Debug;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.annotation.WorkerThread;
import io.appmetrica.analytics.AppMetricaDefaultValues;
import io.appmetrica.analytics.impl.utils.executors.NamedThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class ANRMonitor {
    private static final long TIME_TO_WAIT = TimeUnit.SECONDS.toMillis(1);
    private static final String THREAD_NAME = NamedThreadFactory.adoptThreadName("WatchDog");

    @NonNull
    private final Listener mListener;
    private final int anrTicksCount;
    private final Handler mUiHandler = new Handler(Looper.getMainLooper());
    private final Thread mMonitorThread = new MonitorThread();
    private final AtomicBoolean mCompleted = new AtomicBoolean();

    private final Runnable mUiRunnable = new Runnable() {
        @Override
        public void run() {
            mCompleted.set(true);
        }
    };

    public interface Listener {
        @WorkerThread
        void onAppNotResponding();
    }

    public ANRMonitor(
        @NonNull final Listener listener,
        @Nullable final Integer anrMonitoringTimeout
    ) {
        mListener = listener;
        this.anrTicksCount = getAnrTicksCount(anrMonitoringTimeout);
    }

    public void startMonitoring() {
        try {
            mMonitorThread.setName(THREAD_NAME);
        } catch (SecurityException e) { /* do nothing */ }

        mMonitorThread.start();
    }

    @VisibleForTesting
    public void handleAppNotResponding() {
        mListener.onAppNotResponding();
    }

    private int getAnrTicksCount(@Nullable final Integer anrMonitoringTimeout) {
        if (anrMonitoringTimeout == null) {
            return AppMetricaDefaultValues.DEFAULT_ANR_TICKS_COUNT;
        }
        if (anrMonitoringTimeout < AppMetricaDefaultValues.DEFAULT_ANR_TICKS_COUNT) {
            return AppMetricaDefaultValues.DEFAULT_ANR_TICKS_COUNT;
        }
        return anrMonitoringTimeout;
    }

    private class MonitorThread extends Thread {
        public MonitorThread() {
            super();
        }

        @Override
        public void run() {
            boolean isWaiting = false;
            int counter = 1;
            while (!isInterrupted()) {
                if (!isWaiting) {
                    counter = 1;
                    mCompleted.set(false);
                    mUiHandler.post(mUiRunnable);
                }

                try {
                    Thread.sleep(TIME_TO_WAIT);
                } catch (InterruptedException e) {
                    return;
                }

                if (!mCompleted.get()) {
                    isWaiting = true;
                    counter++;
                    if (counter == anrTicksCount && !Debug.isDebuggerConnected()) {
                        handleAppNotResponding();
                    }
                    continue;
                }

                isWaiting = false;
            }
        }
    }

}
