package io.appmetrica.analytics.impl.referrer.service;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.impl.referrer.common.ReferrerInfo;
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger;

public class ReferrerListenerNotifier {

    private static final String TAG = "[ReferrerListenerNotifier]";

    @NonNull
    private final IReferrerNotificationFilter mFilter;
    @NonNull
    private final ReferrerHolder.Listener mListener;
    @NonNull
    private final IReferrerHandledNotifier mReferrerHandledNotifier;

    public ReferrerListenerNotifier(@NonNull IReferrerNotificationFilter filter,
                                    @NonNull ReferrerHolder.Listener listener,
                                    @NonNull IReferrerHandledNotifier referrerHandledNotifier) {
        mFilter = filter;
        mListener = listener;
        mReferrerHandledNotifier = referrerHandledNotifier;
    }

    public void notifyIfNeeded(@Nullable ReferrerInfo referrerInfo) {
        DebugLogger.INSTANCE.info(TAG, "notifyListenerIfNeeded with referrer %s, listener: %s",
                referrerInfo, mListener);
        if (mFilter.shouldNotify(referrerInfo)) {
            DebugLogger.INSTANCE.info(TAG, "should notify listener");
            mListener.handleReferrer(referrerInfo);
            mReferrerHandledNotifier.onReferrerHandled();
        }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    @NonNull
    public IReferrerNotificationFilter getFilter() {
        return mFilter;
    }

    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    @NonNull
    public IReferrerHandledNotifier getReferrerHandledNotifier() {
        return mReferrerHandledNotifier;
    }

    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    @NonNull
    public ReferrerHolder.Listener getListener() {
        return mListener;
    }
}
