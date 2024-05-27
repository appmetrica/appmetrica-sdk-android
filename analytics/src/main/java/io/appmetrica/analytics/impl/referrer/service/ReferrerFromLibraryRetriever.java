package io.appmetrica.analytics.impl.referrer.service;

import android.content.Context;
import androidx.annotation.AnyThread;
import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import androidx.annotation.WorkerThread;
import com.android.installreferrer.api.InstallReferrerClient;
import com.android.installreferrer.api.InstallReferrerStateListener;
import com.android.installreferrer.api.ReferrerDetails;
import io.appmetrica.analytics.coreapi.internal.executors.ICommonExecutor;
import io.appmetrica.analytics.impl.referrer.common.ReferrerInfo;
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger;

public class ReferrerFromLibraryRetriever implements IReferrerRetriever {

    private static final String TAG = "[ReferrerFromLibraryRetriever]";

    @NonNull
    private final InstallReferrerClient mClient;
    @NonNull
    private final ICommonExecutor executor;

    public ReferrerFromLibraryRetriever(@NonNull Context context, @NonNull ICommonExecutor executor) throws Throwable {
        this(InstallReferrerClient.newBuilder(context).build(), executor);
    }

    @VisibleForTesting
    ReferrerFromLibraryRetriever(@NonNull InstallReferrerClient client, @NonNull ICommonExecutor executor) {
        mClient = client;
        this.executor = executor;
    }

    @Override
    public void retrieveReferrer(@NonNull final ReferrerReceivedListener referrerListener) throws Throwable {
        DebugLogger.INSTANCE.info(TAG, "try to retrieve referrer via Google Play referrer library");
        InstallReferrerStateListener listener = new InstallReferrerStateListener() {
            @Override
            @MainThread
            public void onInstallReferrerSetupFinished(int i) {
                if (i == 0) {
                    try {
                        ReferrerDetails referrerDetails = mClient.getInstallReferrer();
                        final ReferrerInfo referrerInfo = new ReferrerInfo(
                                referrerDetails.getInstallReferrer(),
                                referrerDetails.getReferrerClickTimestampSeconds(),
                                referrerDetails.getInstallBeginTimestampSeconds(),
                                ReferrerInfo.Source.GP
                        );
                        executor.execute(new Runnable() {
                            @WorkerThread
                            @Override
                            public void run() {
                                referrerListener.onReferrerReceived(referrerInfo);
                            }
                        });
                    } catch (Throwable ex) {
                        notifyListenerOnError(referrerListener, ex);
                    }
                } else {
                    notifyListenerOnError(
                            referrerListener,
                            new IllegalStateException("Referrer check failed with error " + i)
                    );
                }
                try {
                    mClient.endConnection();
                } catch (Throwable ex) {
                    DebugLogger.INSTANCE.error(TAG, ex);
                }
            }

            @Override
            @MainThread
            public void onInstallReferrerServiceDisconnected() {
                // do nothing
            }
        };
        mClient.startConnection(listener);
    }

    @AnyThread
    private void notifyListenerOnError(@NonNull final ReferrerReceivedListener referrerListener,
                                       @NonNull final Throwable ex) {
        executor.execute(new Runnable() {
            @WorkerThread
            @Override
            public void run() {
                referrerListener.onReferrerRetrieveError(ex);
            }
        });
    }
}
