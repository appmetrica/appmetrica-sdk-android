package io.appmetrica.analytics.impl;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.impl.component.IComponent;
import io.appmetrica.analytics.impl.events.EventsFlusher;
import io.appmetrica.analytics.impl.startup.executor.StartupExecutor;
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger;
import io.appmetrica.analytics.networktasks.internal.NetworkTask;

public class TaskProcessor<C extends IComponent> implements ServiceLifecycleObserver, EventsFlusher {

    private static final String TAG = "[TaskProcessor]";

    @NonNull
    private C mComponent;

    final Object mSync = new Object();

    boolean mIsShuttingDown = false;

    @NonNull
    private final StartupExecutor mStartupExecutor;

    public TaskProcessor(@NonNull C component, @NonNull StartupExecutor executor) {
        mComponent = component;
        mStartupExecutor = executor;
    }

    @Override
    public void onCreate() {
        synchronized (mSync) {
            if (mIsShuttingDown) {
                mIsShuttingDown = false;
                DebugLogger.INSTANCE.info(TAG, "start");
            }
        }
    }

    @Override
    public void onDestroy() {
        synchronized (mSync) {
            if (!mIsShuttingDown) {
                cancelFlushTask();

                mIsShuttingDown = true;

                DebugLogger.INSTANCE.info(TAG, "stop");
            }
        }
    }

    void cancelFlushTask() {

    }

    @Override
    public void flushAllTasks() {
        DebugLogger.INSTANCE.info(TAG, "flushAllTasks");
        synchronized (mSync) {
            if (!mIsShuttingDown) {
                runAllTasks();
                cancelFlushTask();
            }
        }
    }

    void runAllTasks() {
        synchronized (mSync) {
            if (!mIsShuttingDown) {
                runTasks();
            }
        }
    }

    void startTask(@NonNull NetworkTask networkTask) {
        GlobalServiceLocator.getInstance().getNetworkCore().startTask(networkTask);
    }

    void runTasks() {
        DebugLogger.INSTANCE.info(TAG, "sendStartupIfRequired");
        mStartupExecutor.sendStartupIfRequired();
    }

    @NonNull
   public C getComponent() {
        return mComponent;
    }

    @NonNull
    @VisibleForTesting
    public StartupExecutor getStartupExecutor() {
        return mStartupExecutor;
    }
}
