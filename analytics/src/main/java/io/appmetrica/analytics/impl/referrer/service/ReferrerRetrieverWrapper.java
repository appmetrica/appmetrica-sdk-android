package io.appmetrica.analytics.impl.referrer.service;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.coreapi.internal.executors.ICommonExecutor;
import io.appmetrica.analytics.coreutils.internal.logger.YLogger;
import io.appmetrica.analytics.coreutils.internal.reflection.ReflectionUtils;

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
        YLogger.d("%s try to retriever referrer", TAG);
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
                YLogger.e(ex, TAG);
            }
        } else {
            YLogger.d("%s Install Referrer Client class was not detected", TAG);
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
