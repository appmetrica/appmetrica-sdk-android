package io.appmetrica.analytics.productflow;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.productflow.impl.ProductFlowConstants;

/**
 * Source of an offer display.
 *
 * <p>Use {@link #newBuilder()} to obtain a {@link Builder}.
 */
public class OfferReferrer {

    @Nullable
    private final String type;
    @Nullable
    private final String identifier;
    @Nullable
    private final String screen;

    private OfferReferrer(
        @Nullable String type,
        @Nullable String identifier,
        @Nullable String screen
    ) {
        this.type = type;
        this.identifier = identifier;
        this.screen = screen;
    }

    /**
     * Creates a new {@link Builder}.
     *
     * @return a new builder.
     */
    @NonNull
    public static Builder newBuilder() {
        return new Builder();
    }

    /**
     * Returns the referrer type (e.g. {@code push}, {@code banner}).
     * Truncated to
     * {@link ProductFlowConstants#PRODUCT_FLOW_GENERIC_STRING_MAX_SIZE}
     * characters if longer.
     *
     * @return referrer type, or {@code null} if not set.
     */
    @Nullable
    public String getType() {
        return type;
    }

    /**
     * Returns the identifier of the referral source (e.g. campaign ID).
     * Truncated to
     * {@link ProductFlowConstants#PRODUCT_FLOW_GENERIC_STRING_MAX_SIZE}
     * characters if longer.
     *
     * @return identifier, or {@code null} if not set.
     */
    @Nullable
    public String getIdentifier() {
        return identifier;
    }

    /**
     * Returns the screen from which the offer was opened.
     * Truncated to
     * {@link ProductFlowConstants#PRODUCT_FLOW_GENERIC_STRING_MAX_SIZE}
     * characters if longer.
     *
     * @return screen name, or {@code null} if not set.
     */
    @Nullable
    public String getScreen() {
        return screen;
    }

    @NonNull
    @Override
    public String toString() {
        return "OfferReferrer{" +
            "type='" + type + '\'' +
            ", identifier='" + identifier + '\'' +
            ", screen='" + screen + '\'' +
            '}';
    }

    /** Builder for {@link OfferReferrer}. Obtain via {@link OfferReferrer#newBuilder()}. */
    public static class Builder {

        @Nullable
        private String type;
        @Nullable
        private String identifier;
        @Nullable
        private String screen;

        Builder() {}

        /**
         * Sets the referrer type (e.g. {@code push}, {@code banner}).
         *
         * @param type Referrer type; truncated to
         *     {@link ProductFlowConstants#PRODUCT_FLOW_GENERIC_STRING_MAX_SIZE}
         *     characters if longer.
         * @return this instance for method chaining.
         */
        @NonNull
        public Builder withType(@Nullable String type) {
            this.type = type;
            return this;
        }

        /**
         * Sets the identifier of the referral source (e.g. campaign ID).
         *
         * @param identifier Referral source identifier; truncated to
         *     {@link ProductFlowConstants#PRODUCT_FLOW_GENERIC_STRING_MAX_SIZE}
         *     characters if longer.
         * @return this instance for method chaining.
         */
        @NonNull
        public Builder withIdentifier(@Nullable String identifier) {
            this.identifier = identifier;
            return this;
        }

        /**
         * Sets the screen from which the offer was opened.
         *
         * @param screen Screen name; truncated to
         *     {@link ProductFlowConstants#PRODUCT_FLOW_GENERIC_STRING_MAX_SIZE}
         *     characters if longer.
         * @return this instance for method chaining.
         */
        @NonNull
        public Builder withScreen(@Nullable String screen) {
            this.screen = screen;
            return this;
        }

        /**
         * Builds the {@link OfferReferrer} with the configured fields.
         *
         * @return a new {@link OfferReferrer} instance.
         */
        @NonNull
        public OfferReferrer build() {
            return new OfferReferrer(
                type,
                identifier,
                screen
            );
        }
    }
}
