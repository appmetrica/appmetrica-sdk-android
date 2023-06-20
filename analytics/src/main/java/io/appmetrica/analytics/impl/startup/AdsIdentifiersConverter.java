package io.appmetrica.analytics.impl.startup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.AdsIdentifiersResult;
import io.appmetrica.analytics.IdentifiersResult;

public class AdsIdentifiersConverter {

    @NonNull
    public AdsIdentifiersResult convert(@Nullable IdentifiersResult internalGoogle,
                                        @Nullable IdentifiersResult internalHuawei,
                                        @Nullable IdentifiersResult internalYandex) {
        return new AdsIdentifiersResult(
                convertAdvId(internalGoogle),
                convertAdvId(internalHuawei),
                convertAdvId(internalYandex)
        );
    }

    @NonNull
    private AdsIdentifiersResult.AdvId convertAdvId(@Nullable IdentifiersResult internalId) {
        return new AdsIdentifiersResult.AdvId(
                internalId == null ? null : internalId.id,
                convertStatus(internalId),
                internalId == null ? null : internalId.errorExplanation
        );
    }

    @NonNull
    private AdsIdentifiersResult.Details convertStatus(@Nullable IdentifiersResult internalId) {
        if (internalId == null) {
            return AdsIdentifiersResult.Details.INTERNAL_ERROR;
        }
        switch (internalId.status) {
            case OK:
                return AdsIdentifiersResult.Details.OK;
            case NO_STARTUP:
                return AdsIdentifiersResult.Details.NO_STARTUP;
            case FEATURE_DISABLED:
                return AdsIdentifiersResult.Details.FEATURE_DISABLED;
            case IDENTIFIER_PROVIDER_UNAVAILABLE:
                return AdsIdentifiersResult.Details.IDENTIFIER_PROVIDER_UNAVAILABLE;
            case INVALID_ADV_ID:
                return AdsIdentifiersResult.Details.INVALID_ADV_ID;
            default:
                return AdsIdentifiersResult.Details.INTERNAL_ERROR;
        }
    }

}
