package io.appmetrica.analytics.impl;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.coreapi.internal.executors.ICommonExecutor;
import io.appmetrica.analytics.coreutils.internal.executors.SafeRunnable;
import io.appmetrica.analytics.coreutils.internal.logger.YLogger;
import io.appmetrica.analytics.impl.service.MetricaServiceCallback;

public class AppAppMetricaServiceCoreExecutionDispatcher implements AppMetricaServiceCore {
    private static final String TAG = "[MetricaCoreExecutionDispatcher]";

    private ICommonExecutor mCoreExecutor;
    private AppMetricaServiceCore mAppMetricaServiceCore;
    @NonNull
    private final LifecycleDependentComponentManager lifecycleDependentComponentManager;
    private boolean shouldExecuteOnCreate = false;

    public AppAppMetricaServiceCoreExecutionDispatcher(@NonNull AppMetricaServiceCore appMetricaServiceCore) {
        this(
                GlobalServiceLocator.getInstance().getServiceExecutorProvider().getMetricaCoreExecutor(),
            appMetricaServiceCore,
                GlobalServiceLocator.getInstance().getLifecycleDependentComponentManager()
        );
    }

    @Override
    public synchronized void onCreate() {
        YLogger.d("%sPost onCreate", TAG);
        shouldExecuteOnCreate = true;
        mCoreExecutor.execute(new SafeRunnable() {
            @Override
            public void runSafety() {
                synchronized (AppAppMetricaServiceCoreExecutionDispatcher.this) {
                    if (!shouldExecuteOnCreate) {
                        return;
                    }
                    lifecycleDependentComponentManager.onCreate();
                }
                mAppMetricaServiceCore.onCreate();
            }
        });
    }

    @Override
    public void onStart(final Intent intent, final int startId) {
        YLogger.d("%sPost onStart", TAG);
        mCoreExecutor.execute(new SafeRunnable() {
            @Override
            public void runSafety() {
                mAppMetricaServiceCore.onStart(intent, startId);
            }
        });
    }

    @Override
    public void onStartCommand(final Intent intent, final int flags, final int startId) {
        YLogger.d("%sPost onStartCommand", TAG);
        mCoreExecutor.execute(new SafeRunnable() {
            @Override
            public void runSafety() {
                mAppMetricaServiceCore.onStartCommand(intent, flags, startId);
            }
        });
    }

    @Override
    public void onBind(final Intent intent) {
        YLogger.d("%sPost onBind", TAG);
        mCoreExecutor.execute(new SafeRunnable() {
            @Override
            public void runSafety() {
                mAppMetricaServiceCore.onBind(intent);
            }
        });
    }

    @Override
    public void onRebind(final Intent intent) {
        YLogger.d("%sPost onRebind", TAG);
        mCoreExecutor.execute(new SafeRunnable() {
            @Override
            public void runSafety() {
                mAppMetricaServiceCore.onRebind(intent);
            }
        });
    }

    @Override
    public void onUnbind(final Intent intent) {
        YLogger.d("%sPost onUnbind", TAG);
        mCoreExecutor.execute(new SafeRunnable() {
            @Override
            public void runSafety() {
                mAppMetricaServiceCore.onUnbind(intent);
            }
        });
    }

    @Override
    public void onDestroy() {
        YLogger.d("%sPost onDestroy on the same thread", TAG);
        mCoreExecutor.removeAll();
        synchronized (this) {
            lifecycleDependentComponentManager.onDestroy();
            shouldExecuteOnCreate = false;
        }
        mAppMetricaServiceCore.onDestroy();
    }

    @Override
    public void reportData(final int type, final Bundle data) {
        YLogger.d("%sPost reportEvent. Type: %d", TAG, type);
        mCoreExecutor.execute(new SafeRunnable() {
            @Override
            public void runSafety() throws Exception {
                mAppMetricaServiceCore.reportData(type, data);
            }
        });
    }

    @Override
    public void resumeUserSession(@NonNull final Bundle data) {
        YLogger.d("%sPost resumeUserSession", TAG);
        mCoreExecutor.execute(new SafeRunnable() {
            @Override
            public void runSafety() throws Exception {
                mAppMetricaServiceCore.resumeUserSession(data);
            }
        });
    }

    @Override
    public void pauseUserSession(@NonNull final Bundle data) {
        YLogger.d("%sPost pauseUserSession", TAG);
        mCoreExecutor.execute(new SafeRunnable() {
            @Override
            public void runSafety() throws Exception {
                mAppMetricaServiceCore.pauseUserSession(data);
            }
        });
    }

    @Override
    public void updateCallback(@NonNull MetricaServiceCallback callback) {
        mAppMetricaServiceCore.updateCallback(callback);
    }

    @Override
    public void onConfigurationChanged(@NonNull final Configuration newConfig) {
        mCoreExecutor.execute(new SafeRunnable() {
            @Override
            public void runSafety() throws Exception {
                mAppMetricaServiceCore.onConfigurationChanged(newConfig);
            }
        });
    }

    @VisibleForTesting
    AppAppMetricaServiceCoreExecutionDispatcher(
        @NonNull ICommonExecutor coreExecutor,
        @NonNull AppMetricaServiceCore appMetricaServiceCore,
        @NonNull LifecycleDependentComponentManager lifecycleDependentComponentManager
    ) {
        mCoreExecutor = coreExecutor;
        mAppMetricaServiceCore = appMetricaServiceCore;
        this.lifecycleDependentComponentManager = lifecycleDependentComponentManager;
    }
}
