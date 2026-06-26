package io.appmetrica.analytics.productflow;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.coreapi.event.AppMetricaEvent;
import io.appmetrica.analytics.productflow.impl.ProductFlowAppMetricaEvent;
import io.appmetrica.analytics.productflow.impl.ProductFlowConstants;
import io.appmetrica.analytics.productflow.impl.events.eventdata.FlowStartEventData;
import io.appmetrica.analytics.productflow.impl.events.model.FlowStartEvent;
import io.appmetrica.analytics.productflow.impl.validation.Validation;
import java.util.Map;

/** Builder for a flow start event. Obtain via {@link ProductFlowEvent#flowStart}. */
public class FlowStartEventBuilder {

    @NonNull
    private final String productId;
    @Nullable
    private String productOfferId;
    @Nullable
    private OfferPrice price;
    @Nullable
    private Map<String, String> payload;

    FlowStartEventBuilder(@NonNull String productId) {
        this.productId = productId;

        Validation.validateProductId(productId);
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
    public FlowStartEventBuilder withProductOfferId(@Nullable String productOfferId) {
        this.productOfferId = productOfferId;
        return this;
    }

    /**
     * Sets the product price at the moment acquisition started.
     *
     * @param price Product price.
     * @return this instance for method chaining.
     */
    @NonNull
    public FlowStartEventBuilder withPrice(@Nullable OfferPrice price) {
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
    public FlowStartEventBuilder withPayload(@Nullable Map<String, String> payload) {
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
            new FlowStartEventData(
                new FlowStartEvent(
                    productId,
                    productOfferId,
                    price,
                    payload
                )
            )
        );
    }
}
