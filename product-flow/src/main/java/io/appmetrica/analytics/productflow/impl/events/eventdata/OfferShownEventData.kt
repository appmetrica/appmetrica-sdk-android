package io.appmetrica.analytics.productflow.impl.events.eventdata

import io.appmetrica.analytics.coreapi.internal.event.AppMetricaEventData
import io.appmetrica.analytics.coreutils.internal.limitation.hierarchical.HierarchicalStringTrimmer
import io.appmetrica.analytics.coreutils.internal.limitation.hierarchical.HierarchicalValueSizeOrderBasedWithBytesLimitStringMapTrimmer
import io.appmetrica.analytics.productflow.impl.ProductFlowConstants
import io.appmetrica.analytics.productflow.impl.ProductFlowConstants.CUSTOM_EVENT_TYPE
import io.appmetrica.analytics.productflow.impl.converter.OfferPriceConverter
import io.appmetrica.analytics.productflow.impl.converter.OfferReferrerConverter
import io.appmetrica.analytics.productflow.impl.converter.OfferShownEventConverter
import io.appmetrica.analytics.productflow.impl.converter.PayloadConverter
import io.appmetrica.analytics.productflow.impl.events.model.OfferShownEvent
import io.appmetrica.analytics.protobuf.nano.MessageNano

internal class OfferShownEventData(
    event: OfferShownEvent
) : AppMetricaEventData() {

    private val stringTrimmer =
        HierarchicalStringTrimmer(ProductFlowConstants.PRODUCT_FLOW_GENERIC_STRING_MAX_SIZE)

    private val payloadTrimmer =
        HierarchicalValueSizeOrderBasedWithBytesLimitStringMapTrimmer(
            ProductFlowConstants.PRODUCT_FLOW_PAYLOAD_MAX_SIZE,
            ProductFlowConstants.PRODUCT_FLOW_GENERIC_STRING_MAX_SIZE,
            ProductFlowConstants.PRODUCT_FLOW_GENERIC_STRING_MAX_SIZE
        )

    private val converter = OfferShownEventConverter(
        offerPriceConverter = OfferPriceConverter(stringTrimmer),
        payloadConverter = PayloadConverter(payloadTrimmer),
        offerReferrerConverter = OfferReferrerConverter(stringTrimmer),
        stringTrimmer = stringTrimmer
    )

    private val convertedResult = converter.convert(event)

    override val description =
        "OfferShownEvent(" +
            "productOfferId=${event.productOfferId}, " +
            "offerType=${event.offerType}, " +
            "productId=${event.productId}, " +
            "benefitType=${event.benefitType}, " +
            "price=${event.price}, " +
            "payload=${event.payload}, " +
            "referrer=${event.referrer}" +
            ")"

    override val type = CUSTOM_EVENT_TYPE

    override val data: ByteArray
        get() = MessageNano.toByteArray(convertedResult.value)

    override val bytesTruncated: Int
        get() = convertedResult.bytesTruncated
}
