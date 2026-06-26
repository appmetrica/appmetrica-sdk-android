package io.appmetrica.analytics.productflow;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.coreapi.event.AppMetricaEvent;
import io.appmetrica.analytics.productflow.impl.ProductFlowAppMetricaEvent;
import io.appmetrica.analytics.productflow.impl.ProductFlowConstants;
import io.appmetrica.analytics.productflow.impl.events.eventdata.FlowResultEventData;
import io.appmetrica.analytics.productflow.impl.events.model.FlowResultEvent;
import io.appmetrica.analytics.productflow.impl.validation.Validation;
import java.util.Map;

/** Builder for a flow result event. Obtain via {@link ProductFlowEvent#flowResultForProduct} or {@link ProductFlowEvent#flowResultForOffer}. */
public class FlowResultEventBuilder {

    @NonNull
    private final ProductFlowStatus status;
    @Nullable
    private String productId;
    @Nullable
    private String productOfferId;
    @Nullable
    private OfferPrice price;
    @Nullable
    private Map<String, String> payload;

    FlowResultEventBuilder(
        @NonNull ProductFlowStatus status,
        @Nullable String productId,
        @Nullable String productOfferId
    ) {
        this.status = status;
        this.productId = productId;
        this.productOfferId = productOfferId;

        Validation.validateProductFlowStatus(status);
        Validation.validateProductIdAndProductOfferId(productId, productOfferId);
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
    public FlowResultEventBuilder withProductId(@Nullable String productId) {
        Validation.validateProductIdAndProductOfferId(productId, productOfferId);

        this.productId = productId;
        return this;
    }

    /**
     * Sets the offer variant identifier.
     *
     * @param productOfferId Offer variant identifier; truncated to
     *     {@link ProductFlowConstants#PRODUCT_FLOW_GENERIC_STRING_MAX_SIZE}
     *     characters if longer.
     * @return this instance for method chaining.
     */
    @NonNull
    public FlowResultEventBuilder withProductOfferId(@Nullable String productOfferId) {
        Validation.validateProductIdAndProductOfferId(productId, productOfferId);

        this.productOfferId = productOfferId;
        return this;
    }

    /**
     * Sets the price paid or agreed upon.
     *
     * @param price Price.
     * @return this instance for method chaining.
     */
    @NonNull
    public FlowResultEventBuilder withPrice(@Nullable OfferPrice price) {
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
    public FlowResultEventBuilder withPayload(@Nullable Map<String, String> payload) {
        this.payload = payload;
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
            new FlowResultEventData(
                new FlowResultEvent(
                    status,
                    productId,
                    productOfferId,
                    price,
                    payload
                )
            )
        );
    }
}
