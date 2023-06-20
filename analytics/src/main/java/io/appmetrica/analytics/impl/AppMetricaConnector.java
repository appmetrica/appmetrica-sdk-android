package io.appmetrica.analytics.impl;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.IAppMetricaService;
import io.appmetrica.analytics.coreapi.internal.executors.ICommonExecutor;
import io.appmetrica.analytics.coreutils.internal.logger.YLogger;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class AppMetricaConnector {

    private static final String TAG = "[AppMetricaConnector]";

    public final static long SERVICE_UNBIND_DELAY_MILLIS = TimeUnit.SECONDS.toMillis(10);

    public Context getContext() {
        return mContext;
    }

    private final Context mContext;
    private final ICommonExecutor mUnbindExecutor;
    private boolean mShouldNeverDisconnect;

    private IAppMetricaService mService = null;

    @Nullable
    private CountDownLatch bindCountDown;
    private final Object disconnectLock = new Object();

    @NonNull
    private final AppMetricaServiceDelayHandler appMetricaServiceDelayHandler;

    public AppMetricaConnector(Context context, ICommonExecutor executor) {
        this(
            context,
            executor,
            ClientServiceLocator.getInstance().getAppMetricaServiceDelayHandler()
        );
    }

    @VisibleForTesting
    AppMetricaConnector(@NonNull Context context,
                        @NonNull ICommonExecutor executor,
                        @NonNull AppMetricaServiceDelayHandler appMetricaServiceDelayHandler) {
        mContext = context.getApplicationContext();
        mUnbindExecutor = executor;
        mShouldNeverDisconnect = false;
        this.appMetricaServiceDelayHandler = appMetricaServiceDelayHandler;
    }

    public void bindService() {
        YLogger.d(TAG + " Binding to service ... Application: %s", mContext.getPackageName());
        synchronized (this) {
            if (mService != null) {
                return;
            }
            bindCountDown = new CountDownLatch(1);
        }

        Intent intent = ServiceUtils.getOwnMetricaServiceIntent(mContext);
        try {
            YLogger.info(TAG, "May be delay");
            appMetricaServiceDelayHandler.maybeDelay(mContext);
            boolean status = mContext.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
            YLogger.d("%s bind to service with status: %b", TAG, status);
        } catch (Throwable exception) {
            YLogger.e(TAG + " Exception while binding ...\n%s", exception);
        }
    }

    public void scheduleDisconnect() {
        scheduleDisconnect(mUnbindExecutor);
    }

    @VisibleForTesting
    void scheduleDisconnect(@NonNull ICommonExecutor executor) {
        synchronized (disconnectLock) {
            YLogger.info(TAG, "remove unbind runnable");
            executor.remove(mUnbindRunnable);
            if (mShouldNeverDisconnect == false) {
                YLogger.info(TAG, "plan unbind runnable in %d", SERVICE_UNBIND_DELAY_MILLIS);
                executor.executeDelayed(mUnbindRunnable, SERVICE_UNBIND_DELAY_MILLIS);
            }
        }
    }

    void removeScheduleDisconnect() {
        synchronized (disconnectLock) {
            YLogger.info(TAG, "removeScheduleDisconnect");
            mUnbindExecutor.remove(mUnbindRunnable);
        }
    }

    public synchronized boolean isConnected() {
        YLogger.info(TAG, "isConnected");
        return null != mService;
    }

    public synchronized IAppMetricaService getService() {
        return mService;
    }

    @VisibleForTesting
    @NonNull
    Runnable getUnbindRunnable() {
        return mUnbindRunnable;
    }

    private final Runnable mUnbindRunnable = new Runnable() {

        @Override
        public void run() {
            fullUnbind();
        }

    };

    private synchronized void fullUnbind() {
        if (null != mContext && isConnected()) {
            YLogger.d(TAG + " Unbinding from service ... Application: %s", mContext.getPackageName());

            try {
                mService = null;
                mContext.unbindService(mConnection);
            } catch (Throwable exception) {
                YLogger.e(TAG + " Exception while unbinding ...\n%s", exception);
            }
        }

        mService = null;
    }

    private final ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            YLogger.d(TAG + " Service connected. Component name: %s", name);

            synchronized (AppMetricaConnector.this) {
                mService = IAppMetricaService.Stub.asInterface(service);
                YLogger.info(TAG, "onServiceConnected - countDown");
                bindCountDown.countDown();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            YLogger.d(TAG + " Service disconnected. Component name: %s", name);

            synchronized (AppMetricaConnector.this) {
                mService = null;
            }
        }

    };

    public void forbidDisconnect() {
        YLogger.info(TAG, "forbidDisconnect");
        synchronized (disconnectLock) {
            mShouldNeverDisconnect = true;
            removeScheduleDisconnect();
        }
    }

    public void allowDisconnect() {
        YLogger.info(TAG, "allowDisconnect");
        synchronized (disconnectLock) {
            mShouldNeverDisconnect = false;
            scheduleDisconnect();
        }
    }

    public boolean waitForConnect(@NonNull final Long timeout) {
        YLogger.info(TAG, "waitForConnect with timeout = %s", timeout);
        try {
            synchronized (this) {
                if (bindCountDown == null) {
                    YLogger.info(TAG, "Bind to service has not started yet. Ignore waiting.");
                    return false;
                }
            }
            return bindCountDown.await(timeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            YLogger.error(TAG, e);
        }
        return false;
    }
}
