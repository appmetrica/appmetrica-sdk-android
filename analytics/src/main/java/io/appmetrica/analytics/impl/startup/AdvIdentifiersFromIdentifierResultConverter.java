package io.appmetrica.analytics.impl.startup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.AdvIdentifiersResult;
import io.appmetrica.analytics.internal.IdentifiersResult;

public class AdvIdentifiersFromIdentifierResultConverter {

    @NonNull
    public AdvIdentifiersResult convert(@Nullable IdentifiersResult internalGoogle,
                                        @Nullable IdentifiersResult internalHuawei,
                                        @Nullable IdentifiersResult internalYandex) {
        return new AdvIdentifiersResult(
                convertAdvId(internalGoogle),
                convertAdvId(internalHuawei),
                convertAdvId(internalYandex)
        );
    }

    @NonNull
    private AdvIdentifiersResult.AdvId convertAdvId(@Nullable IdentifiersResult internalId) {
        return new AdvIdentifiersResult.AdvId(
                internalId == null ? null : internalId.id,
                convertStatus(internalId),
                internalId == null ? null : internalId.errorExplanation
        );
    }

    @NonNull
    private AdvIdentifiersResult.Details convertStatus(@Nullable IdentifiersResult internalId) {
        if (internalId == null) {
            return AdvIdentifiersResult.Details.INTERNAL_ERROR;
        }
        switch (internalId.status) {
            case OK:
                return AdvIdentifiersResult.Details.OK;
            case NO_STARTUP:
                return AdvIdentifiersResult.Details.NO_STARTUP;
            case FEATURE_DISABLED:
                return AdvIdentifiersResult.Details.FEATURE_DISABLED;
            case IDENTIFIER_PROVIDER_UNAVAILABLE:
                return AdvIdentifiersResult.Details.IDENTIFIER_PROVIDER_UNAVAILABLE;
            case INVALID_ADV_ID:
                return AdvIdentifiersResult.Details.INVALID_ADV_ID;
            case FORBIDDEN_BY_CLIENT_CONFIG:
                return AdvIdentifiersResult.Details.FORBIDDEN_BY_CLIENT_CONFIG;
            default:
                return AdvIdentifiersResult.Details.INTERNAL_ERROR;
        }
    }

}
