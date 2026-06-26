package io.appmetrica.analytics.productflow.impl.converter

import io.appmetrica.analytics.coreutils.internal.StringUtils
import io.appmetrica.analytics.coreutils.internal.limitation.BytesTruncatedInfo
import io.appmetrica.analytics.coreutils.internal.limitation.BytesTruncatedProvider
import io.appmetrica.analytics.coreutils.internal.limitation.TrimmingResult
import io.appmetrica.analytics.coreutils.internal.limitation.hierarchical.HierarchicalStringTrimmer
import io.appmetrica.analytics.coreutils.internal.proto.DecimalProtoModel
import io.appmetrica.analytics.productflow.OfferPrice
import io.appmetrica.analytics.productflow.impl.protobuf.backend.ProductFlowEvent as ProductFlowEventProto

internal class OfferPriceConverter(
    private val stringTrimmer: HierarchicalStringTrimmer
) {

    fun convert(offerPrice: OfferPrice): TrimmingResult<ProductFlowEventProto.Amount, BytesTruncatedProvider> {
        val proto = ProductFlowEventProto.Amount()

        val decimal = DecimalProtoModel.fromDecimal(offerPrice.amount)
        proto.mantissa = decimal.mantissa
        proto.exponent = decimal.exponent

        val unitTrimmingResult = stringTrimmer.trim(offerPrice.unit)
        proto.unit = StringUtils.getUTF8Bytes(unitTrimmingResult.value)

        val bytesTruncated = BytesTruncatedInfo.total(
            unitTrimmingResult
        ).bytesTruncated

        return TrimmingResult(
            proto,
            BytesTruncatedInfo(bytesTruncated)
        )
    }
}
