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
import io.appmetrica.analytics.logger.internal.DebugLogger;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class ANRMonitor {
    private static final String TAG = "[ANRMonitor]";
    private static final long TIME_TO_WAIT = TimeUnit.SECONDS.toMillis(1);
    private static final String THREAD_NAME = NamedThreadFactory.adoptThreadName("WatchDog");

    @NonNull
    private final List<Listener> mListeners = new CopyOnWriteArrayList<>();
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
        DebugLogger.info(TAG, "Create ANRMonitor with listener %s", listener);
        mListeners.add(listener);
        this.anrTicksCount = getAnrTicksCount(anrMonitoringTimeout);
    }

    public void startMonitoring() {
        try {
            mMonitorThread.setName(THREAD_NAME);
        } catch (SecurityException e) { /* do nothing */ }

        mMonitorThread.start();
    }

    public void subscribe(@NonNull Listener listener) {
        mListeners.add(listener);
        DebugLogger.info(TAG, "Subscribe listener %s. Actual listeners: %s", listener, mListeners);
    }

    @VisibleForTesting
    public void handleAppNotResponding() {
        DebugLogger.info(TAG, "Notify %d listeners about ANR", mListeners.size());
        for (Listener listener : mListeners) {
            DebugLogger.info(TAG, "Notify listener %s about ANR", listener);
            listener.onAppNotResponding();
        }
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
            while (!isInterrupted()) {
                // Post task and start waiting for it to complete.
                mCompleted.set(false);
                mUiHandler.postAtFrontOfQueue(mUiRunnable);

                // Poll until task completes or anrTicksCount passes.
                int counter = anrTicksCount;
                while (counter > 0) {
                    try {
                        Thread.sleep(TIME_TO_WAIT);
                    } catch (InterruptedException e) {
                        return;
                    }

                    if (mCompleted.get()) {
                        break;
                    }

                    --counter;
                }

                // If still is not completed and not under debugger, report ANR.
                if (counter == 0 && !Debug.isDebuggerConnected()) {
                    handleAppNotResponding();
                }

                // If not complete, wait until completion to avoid double ANR report.
                while (!mCompleted.get()) {
                    try {
                        Thread.sleep(TIME_TO_WAIT);
                    } catch (InterruptedException e) {
                        return;
                    }
                }
            }
        }
    }

}
