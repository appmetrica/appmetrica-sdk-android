package io.appmetrica.analytics.impl.referrer.service;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.coreapi.internal.executors.ICommonExecutor;
import io.appmetrica.analytics.coreutils.internal.reflection.ReflectionUtils;
import io.appmetrica.analytics.logger.internal.YLogger;

public class ReferrerRetrieverWrapper {

    private static final String TAG = "[ReferrerRetrieverWrapper]";
    private static final String sInstallReferrerClientClass = "com.android.installreferrer.api.InstallReferrerClient";

    @NonNull
    private final IReferrerRetriever mReferrerRetriever;

    public ReferrerRetrieverWrapper(@NonNull Context context, @NonNull ICommonExecutor executor) {
        this(createReferrerRetriever(context, executor));
    }

    @VisibleForTesting
    ReferrerRetrieverWrapper(@NonNull IReferrerRetriever referrerRetriever) {
        mReferrerRetriever = referrerRetriever;
    }

    public void retrieveReferrer(@NonNull final ReferrerReceivedListener referrerListener) {
        YLogger.debug(TAG, "try to retriever referrer");
        try {
            mReferrerRetriever.retrieveReferrer(referrerListener);
        } catch (Throwable ex) {
            referrerListener.onReferrerRetrieveError(ex);
        }
    }

    @NonNull
    private static IReferrerRetriever createReferrerRetriever(@NonNull Context context,
                                                              @NonNull ICommonExecutor executor) {
        IReferrerRetriever referrerRetriever = null;
        if (ReflectionUtils.detectClassExists(sInstallReferrerClientClass)) {
            try {
                referrerRetriever = new ReferrerFromLibraryRetriever(context, executor);
            } catch (Throwable ex) {
                YLogger.error(TAG, ex);
            }
        } else {
            YLogger.debug(TAG, "Install Referrer Client class was not detected");
        }
        if (referrerRetriever == null) {
            referrerRetriever = new IReferrerRetriever() {
                @Override
                public void retrieveReferrer(@NonNull ReferrerReceivedListener referrerListener) throws Throwable {
                    throw new IllegalStateException("No class: " + sInstallReferrerClientClass);
                }
            };
        }
        return referrerRetriever;
    }

    @VisibleForTesting
    @NonNull
    IReferrerRetriever getReferrerRetriever() {
        return mReferrerRetriever;
    }
}
