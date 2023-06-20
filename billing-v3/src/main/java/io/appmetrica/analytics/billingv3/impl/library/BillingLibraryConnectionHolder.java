package io.appmetrica.analytics.billingv3.impl.library;

import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;
import com.android.billingclient.api.BillingClient;
import io.appmetrica.analytics.coreutils.internal.executors.SafeRunnable;
import io.appmetrica.analytics.coreutils.internal.logger.YLogger;
import java.util.HashSet;
import java.util.Set;

class BillingLibraryConnectionHolder {

    private static final String TAG = "[BillingLibraryConnectionHolder]";

    @NonNull
    private final Handler mainHandler;
    @NonNull
    private final BillingClient billingClient;
    private final Set<Object> waitingListeners;

    BillingLibraryConnectionHolder(@NonNull final BillingClient billingClient) {
        this(billingClient, new Handler(Looper.getMainLooper()));
    }

    BillingLibraryConnectionHolder(@NonNull final BillingClient billingClient,
                                   @NonNull final Handler handler) {
        this.billingClient = billingClient;
        this.waitingListeners = new HashSet<>();
        this.mainHandler = handler;
    }

    @WorkerThread
    void addListener(@NonNull final Object listener) {
        waitingListeners.add(listener);
    }

    @WorkerThread
    void removeListener(@NonNull final Object listener) {
        waitingListeners.remove(listener);
        endConnection();
    }

    @WorkerThread
    private void endConnection() {
        if (waitingListeners.size() == 0) {
            YLogger.info(TAG, "endConnection");
            mainHandler.post(new SafeRunnable() {
                @Override
                public void runSafety() {
                    billingClient.endConnection();
                }
            });
        } else {
            YLogger.info(TAG, "Listeners remaining: %d", waitingListeners.size());
        }
    }
}
