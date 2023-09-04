package io.appmetrica.analytics.internal;

import android.app.Service;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.coreutils.internal.logger.YLogger;
import io.appmetrica.analytics.impl.AppMetricaServiceCoreExecutionDispatcher;
import io.appmetrica.analytics.impl.AppMetricaServiceCoreImpl;
import io.appmetrica.analytics.impl.AppMetricaServiceCore;
import io.appmetrica.analytics.impl.GlobalServiceLocator;
import io.appmetrica.analytics.impl.SelfProcessReporter;
import io.appmetrica.analytics.impl.service.AppMetricaServiceAction;
import io.appmetrica.analytics.impl.service.AppMetricaServiceCallback;
import io.appmetrica.analytics.impl.service.AppMetricaServiceDataReporter;
import io.appmetrica.analytics.impl.utils.PublicLogger;

public class AppMetricaService extends Service {

    static class WakeLockBinder extends Binder {

    }

    private static final String TAG = "[AppMetricaService]";
    private final AppMetricaServiceCallback mCallback = new AppMetricaServiceCallback() {
        public void onStartFinished(int startId) {
            stopSelfResult(startId);
        }
    };

    private static AppMetricaServiceCore SERVICE_CORE;

    @Override
    public void onCreate() {
        super.onCreate();
        GlobalServiceLocator.init(this.getApplicationContext());
        PublicLogger.init(getApplicationContext());
        YLogger.info(TAG, "Service was created for owner with package %s, this: %s", getPackageName(), this);
        if (SERVICE_CORE == null) {
            AppMetricaServiceCoreImpl appMetricaCoreImpl =
                new AppMetricaServiceCoreImpl(getApplicationContext(), mCallback);
            GlobalServiceLocator.getInstance()
                    .getServiceDataReporterHolder()
                    .registerServiceDataReporter(
                            AppMetricaServiceDataReporter.TYPE_CORE,
                            new AppMetricaServiceDataReporter(appMetricaCoreImpl)
                    );
            SERVICE_CORE = new AppMetricaServiceCoreExecutionDispatcher(appMetricaCoreImpl);
        } else {
            SERVICE_CORE.updateCallback(mCallback);
        }
        SERVICE_CORE.onCreate();
        GlobalServiceLocator.getInstance().initSelfDiagnosticReporterStorage(new SelfProcessReporter(SERVICE_CORE));
    }

    @Override
    public void onStart(Intent intent, int startId) {
        SERVICE_CORE.onStart(intent, startId);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        SERVICE_CORE.onStartCommand(intent, flags, startId);
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        YLogger.info(TAG, "Bind to the service with data: %s", intent);
        String action = intent.getAction();
        Binder binder = null;
        if (action != null && action.startsWith(AppMetricaServiceAction.ACTION_SERVICE_WAKELOCK)) {
            binder = new WakeLockBinder();
        } else {
            // The service in active state (client is connected with the service) therefore we want to listen updates
            binder = mBinder;
        }
        SERVICE_CORE.onBind(intent);

        return binder;
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
        YLogger.info(TAG, "Rebind to service with data: %s", intent);
        SERVICE_CORE.onRebind(intent);
    }

    @Override
    public void onDestroy() {
        YLogger.info(TAG, "[Service has been destroyed");
        SERVICE_CORE.onDestroy();
        super.onDestroy();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        YLogger.info(TAG, "Unbind from the service with data: %s", intent);
        SERVICE_CORE.onUnbind(intent);
        String action = intent.getAction();
        if (action != null && action.startsWith(AppMetricaServiceAction.ACTION_SERVICE_WAKELOCK)) {
            return false;
        } else {
            if (isInvalidIntentData(intent)) {
                YLogger.info(TAG, "Invalid intent data");
                return false;
            }

            return true;
        }
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        SERVICE_CORE.onConfigurationChanged(newConfig);
    }

    // Local-side IPC implementation stub class
    private final IAppMetricaService.Stub mBinder = new IAppMetricaService.Stub() {

        @Override
        public void reportData(final int type, final Bundle data) throws RemoteException {
            SERVICE_CORE.reportData(type, data);
        }

        @Override
        public void resumeUserSession(@NonNull Bundle bundle) throws RemoteException {
            SERVICE_CORE.resumeUserSession(bundle);
        }

        @Override
        public void pauseUserSession(@NonNull Bundle bundle) throws RemoteException {
            SERVICE_CORE.pauseUserSession(bundle);
        }
    };

    private boolean isInvalidIntentData(final Intent intent) {
        return (null == intent || null == intent.getData());
    }

    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    static void clearInstance() {
        SERVICE_CORE = null;
    }
}
