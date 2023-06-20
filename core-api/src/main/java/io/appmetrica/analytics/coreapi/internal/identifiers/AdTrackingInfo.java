package io.appmetrica.analytics.coreapi.internal.identifiers;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class AdTrackingInfo {

    @NonNull
    public final Provider provider;
    @Nullable
    public final String advId;
    @Nullable
    public final Boolean limitedAdTracking;

    public AdTrackingInfo(@NonNull Provider provider, @Nullable String advId, @Nullable Boolean limitedAdTracking) {
        this.provider = provider;
        this.advId = advId;
        this.limitedAdTracking = limitedAdTracking;
    }

    @Override
    public String toString() {
        return "AdTrackingInfo{" +
                "provider=" + provider +
                ", advId='" + advId + '\'' +
                ", limitedAdTracking=" + limitedAdTracking +
                '}';
    }

    public enum Provider {
        GOOGLE, HMS, YANDEX
    }
}
