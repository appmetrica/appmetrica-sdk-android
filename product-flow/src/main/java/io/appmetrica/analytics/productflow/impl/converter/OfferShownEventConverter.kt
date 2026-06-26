package io.appmetrica.analytics.productflow.impl.converter

import io.appmetrica.analytics.coreutils.internal.StringUtils
import io.appmetrica.analytics.coreutils.internal.limitation.BytesTruncatedInfo
import io.appmetrica.analytics.coreutils.internal.limitation.BytesTruncatedProvider
import io.appmetrica.analytics.coreutils.internal.limitation.TrimmingResult
import io.appmetrica.analytics.coreutils.internal.limitation.hierarchical.HierarchicalStringTrimmer
import io.appmetrica.analytics.productflow.impl.events.model.OfferShownEvent
import io.appmetrica.analytics.productflow.impl.protobuf.backend.ProductFlowEvent as ProductFlowEventProto

internal class OfferShownEventConverter(
    private val offerPriceConverter: OfferPriceConverter,
    private val payloadConverter: PayloadConverter,
    private val offerReferrerConverter: OfferReferrerConverter,
    private val stringTrimmer: HierarchicalStringTrimmer
) {

    fun convert(event: OfferShownEvent): TrimmingResult<ProductFlowEventProto, BytesTruncatedProvider> {
        val proto = ProductFlowEventProto()
        proto.eventType = ProductFlowEventProto.EVENT_TYPE_OFFER_SHOWN

        val offerIdResult = stringTrimmer.trim(event.productOfferId)
        proto.productOfferId = StringUtils.getUTF8Bytes(offerIdResult.value)

        val productIdResult = stringTrimmer.trim(event.productId)
        proto.productId = StringUtils.getUTF8Bytes(productIdResult.value)

        proto.shownInfo = ProductFlowEventProto.ShownInfo()

        val offerTypeResult = stringTrimmer.trim(event.offerType)
        proto.shownInfo.offerType = StringUtils.getUTF8Bytes(offerTypeResult.value)

        val benefitTypeResult = stringTrimmer.trim(event.benefitType)
        proto.shownInfo.benefitType = StringUtils.getUTF8Bytes(benefitTypeResult.value)

        val amountResult = event.price?.let {
            offerPriceConverter.convert(it)
        }?.also {
            proto.price = it.value
        }

        val payloadResult = event.payload?.let {
            payloadConverter.convert(it)
        }?.also {
            proto.payload = it.value
        }

        val referrerResult = event.referrer?.let { referrer ->
            offerReferrerConverter.convert(referrer)
        }?.also {
            proto.shownInfo.referrer = it.value
        }

        val bytesTruncated = BytesTruncatedInfo.total(
            offerIdResult,
            productIdResult,
            offerTypeResult,
            benefitTypeResult,
            amountResult,
            payloadResult,
            referrerResult,
        )

        return TrimmingResult(proto, bytesTruncated)
    }
}
