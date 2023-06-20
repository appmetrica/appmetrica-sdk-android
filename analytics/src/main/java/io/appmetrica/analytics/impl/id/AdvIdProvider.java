package io.appmetrica.analytics.impl.id;

import android.content.Context;
import androidx.annotation.NonNull;
import io.appmetrica.analytics.coreapi.internal.identifiers.AdTrackingInfoResult;

public interface AdvIdProvider {

    @NonNull
    AdTrackingInfoResult getAdTrackingInfo(@NonNull Context context);

    @NonNull
    AdTrackingInfoResult getAdTrackingInfo(@NonNull Context context, @NonNull RetryStrategy retryStrategy);
}
