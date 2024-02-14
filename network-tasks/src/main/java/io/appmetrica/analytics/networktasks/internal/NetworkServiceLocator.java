package io.appmetrica.analytics.networktasks.internal;

import androidx.annotation.AnyThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.annotation.WorkerThread;
import io.appmetrica.analytics.logger.internal.YLogger;

public final class NetworkServiceLocator implements NetworkServiceLifecycleObserver {

    @Nullable
    private NetworkCore networkCore;

    @NonNull
    private static volatile NetworkServiceLocator INSTANCE;

    private static final String TAG = "[NetworkServiceLocator]";

    @AnyThread
    public static void init() {
        if (INSTANCE == null) {
            synchronized (NetworkServiceLocator.class) {
                if (INSTANCE == null) {
                    INSTANCE = new NetworkServiceLocator();
                }
            }
        }
    }

    @WorkerThread
    public void initAsync() {
        YLogger.info(TAG, "initAsync");
        if (networkCore == null) {
            synchronized (this) {
                if (networkCore == null) {
                    networkCore = new NetworkCore();
                    networkCore.setName("YMM-NC");
                    networkCore.start();
                }
            }
        }
    }

    @NonNull
    public static NetworkServiceLocator getInstance() {
        return INSTANCE;
    }

    @AnyThread
    private NetworkServiceLocator() {
        YLogger.info(TAG, "inited NetworkServiceLocator");
    }

    @NonNull
    public NetworkCore getNetworkCore() {
        return networkCore;
    }

    @Override
    public void onCreate() {
        // do nothing
    }

    @Override
    public void onDestroy() {
        YLogger.info(TAG, "onDestroy");
        if (networkCore != null) {
            networkCore.onDestroy();
        }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    public static void destroy() {
        INSTANCE = null;
    }
}
