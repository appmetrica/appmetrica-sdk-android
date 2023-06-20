package io.appmetrica.analytics.impl.referrer.service;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.impl.referrer.common.ReferrerInfo;

public class OnlyOnceReferrerNotificationFilter implements IReferrerNotificationFilter {

    @NonNull
    private final IReferrerHandledProvider mReferrerHandledProvider;

    public OnlyOnceReferrerNotificationFilter(@NonNull IReferrerHandledProvider referrerHandledProvider) {
        mReferrerHandledProvider = referrerHandledProvider;
    }

    @Override
    public boolean shouldNotify(@Nullable ReferrerInfo referrerInfo) {
        return referrerInfo != null && !mReferrerHandledProvider.wasReferrerHandled();
    }
}
