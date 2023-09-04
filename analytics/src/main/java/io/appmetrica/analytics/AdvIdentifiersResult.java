package io.appmetrica.analytics;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Advertising identifiers (google and huawei) information.
 * Information about one identifier is independent on information about others.
 */
public class AdvIdentifiersResult {

    /**
     * Information about google adv_id.
     */
    @NonNull
    public final AdvId googleAdvId;
    /**
     * Information about huawei oaid.
     */
    @NonNull
    public final AdvId huaweiAdvId;
    /**
     * Information about yandex adv_id.
     */
    @NonNull
    public final AdvId yandexAdvId;

    public AdvIdentifiersResult(@NonNull AdvId googleAdvId,
                                @NonNull AdvId huaweiAdvId,
                                @NonNull AdvId yandexAdvId) {
        this.googleAdvId = googleAdvId;
        this.huaweiAdvId = huaweiAdvId;
        this.yandexAdvId = yandexAdvId;
    }

    /**
     * Describes information about request status.
     * {@link Details#OK} means that the identifier was retrieved
     * and the value in {@link AdvId#advId} is not null and valid.
     * {@link Details#IDENTIFIER_PROVIDER_UNAVAILABLE} means that
     * the identifier could not be retrieved because providing services are either absent or unavailable.
     * {@link Details#INVALID_ADV_ID} means that
     * the identifier was retrieved successfully, but its value equals to default value,
     * so it can't be used to identify device.
     * {@link Details#FEATURE_DISABLED} means that
     * the identifier could not be retrieved because access to adv_id is forbidden by startup.
     * {@link Details#NO_STARTUP} means that
     * the identifier could not be retrieved because there was no startup yet so we cannot know
     * if access to adv_id is forbidden.
     * {@link Details#INTERNAL_ERROR} means that
     * the identifier could not be retrieved due to some unknown error.
     */
    public enum Details {
        OK, IDENTIFIER_PROVIDER_UNAVAILABLE, INVALID_ADV_ID, FEATURE_DISABLED, NO_STARTUP, INTERNAL_ERROR
    }

    /**
     * Object of this class holds information about one specific identifier.
     */
    public static class AdvId {

        /**
         * Value of advertising identifier.
         * Can be null if it could not be retrieved.
         * See {@link AdvId#details} and {@link AdvId#errorExplanation} for details.
         */
        @Nullable
        public final String advId;
        /**
         * Information about the request status. States if identifier was retrieved without problems or there were some errors.
         * See {@link Details} for details.
         */
        @NonNull
        public final Details details;
        /**
         * A string that explains what exactly went wrong while retrieving identifier.
         * It will be null if {@link AdvId#details} is {@link AdvIdentifiersResult.Details#OK}
         */
        @Nullable
        public final String errorExplanation;

        public AdvId(@Nullable String advId, @NonNull Details details, @Nullable String errorExplanation) {
            this.advId = advId;
            this.details = details;
            this.errorExplanation = errorExplanation;
        }
    }
}
