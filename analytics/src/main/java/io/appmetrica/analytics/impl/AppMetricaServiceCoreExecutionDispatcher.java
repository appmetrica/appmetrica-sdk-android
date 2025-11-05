package io.appmetrica.analytics.impl;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.coreapi.internal.executors.ICommonExecutor;
import io.appmetrica.analytics.coreutils.internal.executors.SafeRunnable;
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger;

public class AppMetricaServiceCoreExecutionDispatcher implements AppMetricaServiceCore {
    private static final String TAG = "[AppMetricaCoreExecutionDispatcher]";

    private ICommonExecutor mCoreExecutor;
    private AppMetricaServiceCore mAppMetricaServiceCore;
    private boolean shouldExecuteOnCreate = false;

    public AppMetricaServiceCoreExecutionDispatcher(@NonNull AppMetricaServiceCore appMetricaServiceCore) {
        this(
            GlobalServiceLocator.getInstance().getServiceExecutorProvider().getMetricaCoreExecutor(),
            appMetricaServiceCore
        );
    }

    @Override
    public synchronized void onCreate() {
        DebugLogger.INSTANCE.info(TAG, "Post onCreate");
        shouldExecuteOnCreate = true;
        mCoreExecutor.execute(new SafeRunnable() {
            @Override
            public void runSafety() {
                synchronized (AppMetricaServiceCoreExecutionDispatcher.this) {
                    if (!shouldExecuteOnCreate) {
                        return;
                    }
                }
                mAppMetricaServiceCore.onCreate();
            }
        });
    }

    @Override
    public void onStart(final Intent intent, final int startId) {
        DebugLogger.INSTANCE.info(TAG, "Post onStart");
        mCoreExecutor.execute(new SafeRunnable() {
            @Override
            public void runSafety() {
                mAppMetricaServiceCore.onStart(intent, startId);
            }
        });
    }

    @Override
    public void onStartCommand(final Intent intent, final int flags, final int startId) {
        DebugLogger.INSTANCE.info(TAG, "Post onStartCommand");
        mCoreExecutor.execute(new SafeRunnable() {
            @Override
            public void runSafety() {
                mAppMetricaServiceCore.onStartCommand(intent, flags, startId);
            }
        });
    }

    @Override
    public void onBind(final Intent intent) {
        DebugLogger.INSTANCE.info(TAG, "Post onBind");
        mCoreExecutor.execute(new SafeRunnable() {
            @Override
            public void runSafety() {
                mAppMetricaServiceCore.onBind(intent);
            }
        });
    }

    @Override
    public void onRebind(final Intent intent) {
        DebugLogger.INSTANCE.info(TAG, "Post onRebind");
        mCoreExecutor.execute(new SafeRunnable() {
            @Override
            public void runSafety() {
                mAppMetricaServiceCore.onRebind(intent);
            }
        });
    }

    @Override
    public void onUnbind(final Intent intent) {
        DebugLogger.INSTANCE.info(TAG, "Post onUnbind");
        mCoreExecutor.execute(new SafeRunnable() {
            @Override
            public void runSafety() {
                mAppMetricaServiceCore.onUnbind(intent);
            }
        });
    }

    @Override
    public void onDestroy() {
        DebugLogger.INSTANCE.info(TAG, "Post onDestroy on the same thread");
        mCoreExecutor.removeAll();
        synchronized (this) {
            shouldExecuteOnCreate = false;
        }
        mAppMetricaServiceCore.onDestroy();
    }

    @Override
    public void reportData(final int type, final Bundle data) {
        DebugLogger.INSTANCE.info(TAG, "Post reportEvent. Type: %d", type);
        mCoreExecutor.execute(new SafeRunnable() {
            @Override
            public void runSafety() throws Exception {
                mAppMetricaServiceCore.reportData(type, data);
            }
        });
    }

    @Override
    public void resumeUserSession(@NonNull final Bundle data) {
        DebugLogger.INSTANCE.info(TAG, "Post resumeUserSession");
        mCoreExecutor.execute(new SafeRunnable() {
            @Override
            public void runSafety() throws Exception {
                mAppMetricaServiceCore.resumeUserSession(data);
            }
        });
    }

    @Override
    public void pauseUserSession(@NonNull final Bundle data) {
        DebugLogger.INSTANCE.info(TAG, "Post pauseUserSession");
        mCoreExecutor.execute(new SafeRunnable() {
            @Override
            public void runSafety() throws Exception {
                mAppMetricaServiceCore.pauseUserSession(data);
            }
        });
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
    AppMetricaServiceCoreExecutionDispatcher(
        @NonNull ICommonExecutor coreExecutor,
        @NonNull AppMetricaServiceCore appMetricaServiceCore
    ) {
        mCoreExecutor = coreExecutor;
        mAppMetricaServiceCore = appMetricaServiceCore;
    }
}
