package io.appmetrica.analytics.impl.crash.jvm.client;

import android.os.Debug;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.annotation.WorkerThread;
import io.appmetrica.analytics.AppMetricaDefaultValues;
import io.appmetrica.analytics.impl.utils.executors.NamedThreadFactory;
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger;
import io.appmetrica.analytics.logger.appmetrica.internal.PublicLogger;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class ANRMonitor {
    private static final String TAG = "[ANRMonitor]";
    private static final long TIME_TO_WAIT = TimeUnit.SECONDS.toMillis(1);
    private static final String THREAD_NAME = NamedThreadFactory.adoptThreadName("WatchDog");

    @NonNull
    private final List<Listener> listeners = new CopyOnWriteArrayList<>();
    private final AtomicInteger anrTicksCount = new AtomicInteger();
    private final Handler uiHandler = new Handler(Looper.getMainLooper());
    @Nullable
    private MonitorThread monitorThread;
    private final AtomicBoolean completed = new AtomicBoolean();

    private final Runnable uiRunnable = () -> completed.set(true);

    public interface Listener {
        @WorkerThread
        void onAppNotResponding();
    }

    public ANRMonitor(@NonNull final Listener listener) {
        DebugLogger.INSTANCE.info(TAG, "Create ANRMonitor with listener %s", listener);
        listeners.add(listener);
    }

    public synchronized void startMonitoring(int anrMonitoringTimeout) {
        DebugLogger.INSTANCE.info(TAG, "Start monitoring with timeout: %s", anrMonitoringTimeout);

        anrTicksCount.set(getAnrTicksCount(anrMonitoringTimeout));

        if (monitorThread == null) {
            DebugLogger.INSTANCE.info(TAG, "Create and start monitor thread");
            monitorThread = new MonitorThread();
            try {
                monitorThread.setName(THREAD_NAME);
            } catch (SecurityException e) { /* do nothing */ }

            monitorThread.start();

            PublicLogger.getAnonymousInstance().info(
                "Start ANR monitoring with timeout: %s seconds",
                anrMonitoringTimeout
            );
        } else {
            DebugLogger.INSTANCE.info(
                TAG,
                "Anr monitoring has already been started. Just update timeout to %s seconds",
                anrMonitoringTimeout
            );
        }
    }

    public synchronized void stopMonitoring() {
        if (monitorThread != null) {
            monitorThread.stopMonitoring();
            monitorThread = null;
            PublicLogger.getAnonymousInstance().info("Stop ANR monitoring");
        }
    }

    public void subscribe(@NonNull Listener listener) {
        listeners.add(listener);
        DebugLogger.INSTANCE.info(TAG, "Subscribe listener %s. Actual listeners: %s", listener, listeners);
    }

    @VisibleForTesting
    public void handleAppNotResponding() {
        DebugLogger.INSTANCE.info(TAG, "Notify %d listeners about ANR", listeners.size());
        for (Listener listener : listeners) {
            DebugLogger.INSTANCE.info(TAG, "Notify listener %s about ANR", listener);
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

        @NonNull
        private final AtomicBoolean enabled = new AtomicBoolean(true);

        public MonitorThread() {
            super();
        }

        @Override
        public void run() {
            while (!isInterrupted() && enabled.get()) {
                // Post task and start waiting for it to complete.
                completed.set(false);
                uiHandler.postAtFrontOfQueue(uiRunnable);

                // Poll until task completes or anrTicksCount passes.
                int counter = anrTicksCount.get();
                while (counter > 0) {
                    try {
                        Thread.sleep(TIME_TO_WAIT);
                    } catch (InterruptedException e) {
                        return;
                    }

                    if (completed.get()) {
                        break;
                    }

                    --counter;
                }

                // If still is not completed and not under debugger, report ANR.
                if (counter == 0 && !Debug.isDebuggerConnected()) {
                    handleAppNotResponding();
                }

                // If not complete, wait until completion to avoid double ANR report.
                while (!completed.get()) {
                    try {
                        Thread.sleep(TIME_TO_WAIT);
                    } catch (InterruptedException e) {
                        return;
                    }
                }
            }
        }

        public void stopMonitoring() {
            this.enabled.set(false);
        }
    }
}
