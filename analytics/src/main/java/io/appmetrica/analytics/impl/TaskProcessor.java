package io.appmetrica.analytics.impl;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.impl.component.IComponent;
import io.appmetrica.analytics.impl.startup.executor.StartupExecutor;
import io.appmetrica.analytics.logger.internal.DebugLogger;
import io.appmetrica.analytics.networktasks.internal.NetworkTask;

public class TaskProcessor<C extends IComponent> implements ServiceLifecycleObserver {

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
                DebugLogger.info(TAG, "start");
            }
        }
    }

    @Override
    public void onDestroy() {
        synchronized (mSync) {
            if (!mIsShuttingDown) {
                cancelFlushTask();

                mIsShuttingDown = true;

                DebugLogger.info(TAG, "stop");
            }
        }
    }

    void cancelFlushTask() {

    }

    public void flushAllTasks() {
        DebugLogger.info(TAG, "flushAllTasks");
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
        DebugLogger.info(TAG, "sendStartupIfRequired");
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
