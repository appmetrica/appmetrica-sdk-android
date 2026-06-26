package io.appmetrica.analytics.productflow;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.coreapi.event.AppMetricaEvent;
import io.appmetrica.analytics.productflow.impl.ProductFlowAppMetricaEvent;
import io.appmetrica.analytics.productflow.impl.ProductFlowConstants;
import io.appmetrica.analytics.productflow.impl.events.eventdata.OfferShownEventData;
import io.appmetrica.analytics.productflow.impl.events.model.OfferShownEvent;
import io.appmetrica.analytics.productflow.impl.validation.Validation;
import java.util.Map;

/** Builder for an offer shown event. Obtain via {@link ProductFlowEvent#offerShown}. */
public class OfferShownEventBuilder {

    @NonNull
    private final String productOfferId;
    @NonNull
    private final String offerType;
    @Nullable
    private String productId;
    @Nullable
    private String benefitType;
    @Nullable
    private OfferPrice price;
    @Nullable
    private Map<String, String> payload;
    @Nullable
    private OfferReferrer referrer;

    OfferShownEventBuilder(@NonNull String productOfferId, @NonNull String offerType) {
        this.productOfferId = productOfferId;
        this.offerType = offerType;

        Validation.validateProductOfferId(productOfferId);
        Validation.validateOfferType(offerType);
    }

    /**
     * Sets the product type identifier.
     *
     * @param productId Product type identifier; truncated to
     *     {@link ProductFlowConstants#PRODUCT_FLOW_GENERIC_STRING_MAX_SIZE}
     *     characters if longer.
     * @return this instance for method chaining.
     */
    @NonNull
    public OfferShownEventBuilder withProductId(@Nullable String productId) {
        this.productId = productId;
        return this;
    }

    /**
     * Sets the benefit type offered (e.g. {@code cashback}).
     *
     * @param benefitType Benefit type; truncated to
     *     {@link ProductFlowConstants#PRODUCT_FLOW_GENERIC_STRING_MAX_SIZE}
     *     characters if longer.
     * @return this instance for method chaining.
     */
    @NonNull
    public OfferShownEventBuilder withBenefitType(@Nullable String benefitType) {
        this.benefitType = benefitType;
        return this;
    }

    /**
     * Sets the offer price.
     *
     * @param price Offer price.
     * @return this instance for method chaining.
     */
    @NonNull
    public OfferShownEventBuilder withPrice(@Nullable OfferPrice price) {
        this.price = price;
        return this;
    }

    /**
     * Sets the arbitrary key-value payload attached to the event.
     *
     * @param payload Key-value payload; each key and value is truncated to
     *     {@link ProductFlowConstants#PRODUCT_FLOW_GENERIC_STRING_MAX_SIZE}
     *     characters if longer.
     * @return this instance for method chaining.
     */
    @NonNull
    public OfferShownEventBuilder withPayload(@Nullable Map<String, String> payload) {
        this.payload = payload;
        return this;
    }

    /**
     * Sets the source that triggered the offer display.
     *
     * @param referrer Referral source.
     * @return this instance for method chaining.
     */
    @NonNull
    public OfferShownEventBuilder withReferrer(@Nullable OfferReferrer referrer) {
        this.referrer = referrer;
        return this;
    }

    /**
     * Builds the event.
     *
     * @return the constructed {@link io.appmetrica.analytics.coreapi.event.AppMetricaEvent}.
     */
    @NonNull
    public AppMetricaEvent build() {
        return new ProductFlowAppMetricaEvent(
            new OfferShownEventData(
                new OfferShownEvent(
                    productOfferId,
                    offerType,
                    productId,
                    benefitType,
                    price,
                    payload,
                    referrer
                )
            )
        );
    }
}
