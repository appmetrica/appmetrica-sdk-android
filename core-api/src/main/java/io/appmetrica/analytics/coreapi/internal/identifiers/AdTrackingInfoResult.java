package io.appmetrica.analytics.coreapi.internal.identifiers;

import android.text.TextUtils;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class AdTrackingInfoResult {

    private static final String INITIAL_STATUS = "identifier info has never been updated";

    @Nullable
    public final AdTrackingInfo mAdTrackingInfo;
    @NonNull
    public final IdentifierStatus mStatus;
    @Nullable
    public final String mErrorExplanation;

    public AdTrackingInfoResult() {
        this(null, IdentifierStatus.UNKNOWN, INITIAL_STATUS);
    }

    public AdTrackingInfoResult(@Nullable AdTrackingInfo adTrackingInfo,
                                @NonNull IdentifierStatus status,
                                @Nullable String errorExplanation) {
        mAdTrackingInfo = adTrackingInfo;
        mStatus = status;
        mErrorExplanation = errorExplanation;
    }

    public boolean isValid() {
        return mAdTrackingInfo != null && TextUtils.isEmpty(mAdTrackingInfo.advId) == false;
    }

    @NonNull
    public static AdTrackingInfoResult getProviderUnavailableResult(@NonNull String errorMessage) {
        return new AdTrackingInfoResult(null,
                IdentifierStatus.IDENTIFIER_PROVIDER_UNAVAILABLE,
                errorMessage
        );
    }

    @Override
    public String toString() {
        return "AdTrackingInfoResult{" +
                "mAdTrackingInfo=" + mAdTrackingInfo +
                ", mStatus=" + mStatus +
                ", mErrorExplanation='" + mErrorExplanation + '\'' +
                '}';
    }
}
