package io.appmetrica.analytics.productflow;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.productflow.impl.ProductFlowConstants;

/**
 * Base type for product flow events.
 *
 * <p>Use static factory methods to obtain a builder, then optional {@code with*} methods
 * for nullable fields, and finally {@code build()} before calling
 * {@code AppMetrica.reportEvent(AppMetricaEvent)}.
 *
 * <p>All string fields are truncated to
 * {@link ProductFlowConstants#PRODUCT_FLOW_GENERIC_STRING_MAX_SIZE}
 * characters if longer.
 */
public abstract class ProductFlowEvent {

    /** For subclassing only; use factory methods to create events. */
    protected ProductFlowEvent() {}

    /**
     * Creates a builder for an offer shown event.
     *
     * @param productOfferId Offer variant identifier; truncated to
     *     {@link ProductFlowConstants#PRODUCT_FLOW_GENERIC_STRING_MAX_SIZE}
     *     characters if longer.
     * @param offerType Offer type, for example {@code financial_product}; truncated to
     *     {@link ProductFlowConstants#PRODUCT_FLOW_GENERIC_STRING_MAX_SIZE}
     *     characters if longer.
     * @return a new {@link OfferShownEventBuilder}.
     */
    @NonNull
    public static OfferShownEventBuilder offerShown(
        @NonNull String productOfferId,
        @NonNull String offerType
    ) {
        return new OfferShownEventBuilder(productOfferId, offerType);
    }

    /**
     * Creates a builder for a flow start event.
     *
     * @param productId Product type identifier; truncated to
     *     {@link ProductFlowConstants#PRODUCT_FLOW_GENERIC_STRING_MAX_SIZE}
     *     characters if longer.
     * @return a new {@link FlowStartEventBuilder}.
     */
    @NonNull
    public static FlowStartEventBuilder flowStart(@NonNull String productId) {
        return new FlowStartEventBuilder(productId);
    }

    /**
     * Creates a builder for a flow step event identified by product type.
     *
     * @param productId Product type identifier; truncated to
     *     {@link ProductFlowConstants#PRODUCT_FLOW_GENERIC_STRING_MAX_SIZE}
     *     characters if longer.
     * @param stepType Step type, for example {@code documents}; truncated to
     *     {@link ProductFlowConstants#PRODUCT_FLOW_GENERIC_STRING_MAX_SIZE}
     *     characters if longer.
     * @return a new {@link FlowStepEventBuilder}.
     */
    @NonNull
    public static FlowStepEventBuilder flowStepForProduct(
        @NonNull String productId,
        @NonNull String stepType
    ) {
        return new FlowStepEventBuilder(stepType, productId, null);
    }

    /**
     * Creates a builder for a flow step event identified by offer variant.
     *
     * @param productOfferId Offer variant identifier; truncated to
     *     {@link ProductFlowConstants#PRODUCT_FLOW_GENERIC_STRING_MAX_SIZE}
     *     characters if longer.
     * @param stepType Step type, for example {@code documents}; truncated to
     *     {@link ProductFlowConstants#PRODUCT_FLOW_GENERIC_STRING_MAX_SIZE}
     *     characters if longer.
     * @return a new {@link FlowStepEventBuilder}.
     */
    @NonNull
    public static FlowStepEventBuilder flowStepForOffer(
        @NonNull String productOfferId,
        @NonNull String stepType
    ) {
        return new FlowStepEventBuilder(stepType, null, productOfferId);
    }

    /**
     * Creates a builder for a flow result event identified by product type.
     *
     * @param status    Final status.
     * @param productId Product type identifier; truncated to
     *     {@link ProductFlowConstants#PRODUCT_FLOW_GENERIC_STRING_MAX_SIZE}
     *     characters if longer.
     * @return a new {@link FlowResultEventBuilder}.
     */
    @NonNull
    public static FlowResultEventBuilder flowResultForProduct(
        @NonNull ProductFlowStatus status,
        @NonNull String productId
    ) {
        return new FlowResultEventBuilder(status, productId, null);
    }

    /**
     * Creates a builder for a flow result event identified by offer variant.
     *
     * @param status         Final status.
     * @param productOfferId Offer variant identifier; truncated to
     *     {@link ProductFlowConstants#PRODUCT_FLOW_GENERIC_STRING_MAX_SIZE}
     *     characters if longer.
     * @return a new {@link FlowResultEventBuilder}.
     */
    @NonNull
    public static FlowResultEventBuilder flowResultForOffer(
        @NonNull ProductFlowStatus status,
        @NonNull String productOfferId
    ) {
        return new FlowResultEventBuilder(status, null, productOfferId);
    }
}
