package io.appmetrica.analytics.productflow.impl.events.eventdata

import io.appmetrica.analytics.coreapi.internal.event.AppMetricaEventData
import io.appmetrica.analytics.coreutils.internal.limitation.hierarchical.HierarchicalStringTrimmer
import io.appmetrica.analytics.coreutils.internal.limitation.hierarchical.HierarchicalValueSizeOrderBasedWithBytesLimitStringMapTrimmer
import io.appmetrica.analytics.productflow.impl.ProductFlowConstants
import io.appmetrica.analytics.productflow.impl.ProductFlowConstants.CUSTOM_EVENT_TYPE
import io.appmetrica.analytics.productflow.impl.converter.FlowStartEventConverter
import io.appmetrica.analytics.productflow.impl.converter.OfferPriceConverter
import io.appmetrica.analytics.productflow.impl.converter.PayloadConverter
import io.appmetrica.analytics.productflow.impl.events.model.FlowStartEvent
import io.appmetrica.analytics.protobuf.nano.MessageNano

internal class FlowStartEventData(
    event: FlowStartEvent
) : AppMetricaEventData() {

    private val stringTrimmer =
        HierarchicalStringTrimmer(ProductFlowConstants.PRODUCT_FLOW_GENERIC_STRING_MAX_SIZE)

    private val payloadTrimmer =
        HierarchicalValueSizeOrderBasedWithBytesLimitStringMapTrimmer(
            ProductFlowConstants.PRODUCT_FLOW_PAYLOAD_MAX_SIZE,
            ProductFlowConstants.PRODUCT_FLOW_GENERIC_STRING_MAX_SIZE,
            ProductFlowConstants.PRODUCT_FLOW_GENERIC_STRING_MAX_SIZE
        )

    private val converter = FlowStartEventConverter(
        offerPriceConverter = OfferPriceConverter(stringTrimmer),
        payloadConverter = PayloadConverter(payloadTrimmer),
        stringTrimmer = stringTrimmer
    )

    private val convertedResult = converter.convert(event)

    override val description =
        "FlowStartEvent(" +
            "productId=${event.productId}, " +
            "productOfferId=${event.productOfferId}, " +
            "price=${event.price}, " +
            "payload=${event.payload}" +
            ")"

    override val type = CUSTOM_EVENT_TYPE

    override val data: ByteArray
        get() = MessageNano.toByteArray(convertedResult.value)

    override val bytesTruncated: Int
        get() = convertedResult.bytesTruncated
}
