package io.appmetrica.analytics.impl.id;

import android.content.Context;
import androidx.annotation.NonNull;
import io.appmetrica.analytics.coreapi.internal.backport.Provider;
import io.appmetrica.analytics.coreapi.internal.identifiers.AdTrackingInfoResult;
import io.appmetrica.analytics.coreapi.internal.identifiers.IdentifierStatus;
import io.appmetrica.analytics.coreutils.internal.logger.YLogger;

public class AdvIdProviderWrapper implements AdvIdProvider {

    private static final String TAG = "[AdvIdProviderWrapper]";
    private final AdvIdProvider originalProvider;

    public AdvIdProviderWrapper(@NonNull AdvIdProvider originalProvider) {
        this.originalProvider = originalProvider;
    }

    @NonNull
    @Override
    public AdTrackingInfoResult getAdTrackingInfo(@NonNull final Context context) {
        return getCorrectedAdTrackingInfo(new Provider<AdTrackingInfoResult>() {
            @Override
            public AdTrackingInfoResult get() {
                return originalProvider.getAdTrackingInfo(context);
            }
        });
    }

    @NonNull
    @Override
    public AdTrackingInfoResult getAdTrackingInfo(@NonNull final Context context,
                                                  @NonNull final RetryStrategy retryStrategy) {
        return getCorrectedAdTrackingInfo(new Provider<AdTrackingInfoResult>() {
            @Override
            public AdTrackingInfoResult get() {
                return originalProvider.getAdTrackingInfo(context, retryStrategy);
            }
        });
    }

    @NonNull
    private AdTrackingInfoResult getCorrectedAdTrackingInfo(@NonNull Provider<AdTrackingInfoResult> provider) {
        AdTrackingInfoResult result = provider.get();
        YLogger.info(TAG, "Original result: %s", result);
        if (result.mAdTrackingInfo != null && Constants.INVALID_ADV_ID.equals(result.mAdTrackingInfo.advId)) {
            YLogger.info(TAG, "AdvId is invalid");
            result = new AdTrackingInfoResult(
                    null,
                    IdentifierStatus.INVALID_ADV_ID,
                    "AdvId is invalid: " + Constants.INVALID_ADV_ID
            );
        }
        return result;
    }
}
