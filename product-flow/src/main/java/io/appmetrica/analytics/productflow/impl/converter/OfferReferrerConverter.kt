package io.appmetrica.analytics.productflow.impl.converter

import io.appmetrica.analytics.coreutils.internal.StringUtils
import io.appmetrica.analytics.coreutils.internal.limitation.BytesTruncatedInfo
import io.appmetrica.analytics.coreutils.internal.limitation.BytesTruncatedProvider
import io.appmetrica.analytics.coreutils.internal.limitation.TrimmingResult
import io.appmetrica.analytics.coreutils.internal.limitation.hierarchical.HierarchicalStringTrimmer
import io.appmetrica.analytics.productflow.OfferReferrer
import io.appmetrica.analytics.productflow.impl.protobuf.backend.ProductFlowEvent as ProductFlowEventProto

internal class OfferReferrerConverter(
    private val stringTrimmer: HierarchicalStringTrimmer
) {

    fun convert(offerReferrer: OfferReferrer): TrimmingResult<ProductFlowEventProto.Referrer, BytesTruncatedProvider> {
        val proto = ProductFlowEventProto.Referrer()

        val typeTrimmingResult = stringTrimmer.trim(offerReferrer.type)
        proto.type = StringUtils.getUTF8Bytes(typeTrimmingResult.value)

        val idTrimmingResult = stringTrimmer.trim(offerReferrer.identifier)
        proto.identifier = StringUtils.getUTF8Bytes(idTrimmingResult.value)

        val screenTrimmingResult = stringTrimmer.trim(offerReferrer.screen)
        proto.screen = StringUtils.getUTF8Bytes(screenTrimmingResult.value)

        val bytesTruncated = BytesTruncatedInfo.total(
            typeTrimmingResult,
            idTrimmingResult,
            screenTrimmingResult
        )

        return TrimmingResult(proto, bytesTruncated)
    }
}
