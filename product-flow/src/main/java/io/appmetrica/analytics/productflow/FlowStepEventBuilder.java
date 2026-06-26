package io.appmetrica.analytics.productflow;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.coreapi.event.AppMetricaEvent;
import io.appmetrica.analytics.productflow.impl.ProductFlowAppMetricaEvent;
import io.appmetrica.analytics.productflow.impl.ProductFlowConstants;
import io.appmetrica.analytics.productflow.impl.events.eventdata.FlowStepEventData;
import io.appmetrica.analytics.productflow.impl.events.model.FlowStepEvent;
import io.appmetrica.analytics.productflow.impl.validation.Validation;
import java.util.Map;

/** Builder for a flow step event. Obtain via {@link ProductFlowEvent#flowStepForProduct} or {@link ProductFlowEvent#flowStepForOffer}. */
public class FlowStepEventBuilder {

    @NonNull
    private final String stepType;
    @Nullable
    private String productId;
    @Nullable
    private String productOfferId;
    @Nullable
    private String stepOption;
    @Nullable
    private OfferPrice price;
    @Nullable
    private Map<String, String> payload;

    FlowStepEventBuilder(
        @NonNull String stepType,
        @Nullable String productId,
        @Nullable String productOfferId
    ) {
        this.stepType = stepType;
        this.productId = productId;
        this.productOfferId = productOfferId;

        Validation.validateStepType(stepType);
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
    public FlowStepEventBuilder withProductId(@Nullable String productId) {
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
    public FlowStepEventBuilder withProductOfferId(@Nullable String productOfferId) {
        Validation.validateProductIdAndProductOfferId(productId, productOfferId);

        this.productOfferId = productOfferId;
        return this;
    }

    /**
     * Sets the step option (additional qualifier for the step type).
     *
     * @param stepOption Step option; truncated to
     *     {@link ProductFlowConstants#PRODUCT_FLOW_GENERIC_STRING_MAX_SIZE}
     *     characters if longer.
     * @return this instance for method chaining.
     */
    @NonNull
    public FlowStepEventBuilder withStepOption(@Nullable String stepOption) {
        this.stepOption = stepOption;
        return this;
    }

    /**
     * Sets the current price at this step.
     *
     * @param price Current price.
     * @return this instance for method chaining.
     */
    @NonNull
    public FlowStepEventBuilder withPrice(@Nullable OfferPrice price) {
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
    public FlowStepEventBuilder withPayload(@Nullable Map<String, String> payload) {
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
            new FlowStepEventData(
                new FlowStepEvent(
                    productId,
                    productOfferId,
                    stepType,
                    stepOption,
                    price,
                    payload
                )
            )
        );
    }
}
