package io.appmetrica.analytics.productflow.impl.converter

import io.appmetrica.analytics.coreutils.internal.StringUtils
import io.appmetrica.analytics.coreutils.internal.limitation.BytesTruncatedInfo
import io.appmetrica.analytics.coreutils.internal.limitation.BytesTruncatedProvider
import io.appmetrica.analytics.coreutils.internal.limitation.TrimmingResult
import io.appmetrica.analytics.coreutils.internal.limitation.hierarchical.HierarchicalStringTrimmer
import io.appmetrica.analytics.productflow.ProductFlowStatus
import io.appmetrica.analytics.productflow.impl.events.model.FlowResultEvent
import io.appmetrica.analytics.productflow.impl.protobuf.backend.ProductFlowEvent as ProductFlowEventProto

internal class FlowResultEventConverter(
    private val offerPriceConverter: OfferPriceConverter,
    private val payloadConverter: PayloadConverter,
    private val stringTrimmer: HierarchicalStringTrimmer
) {

    fun convert(event: FlowResultEvent): TrimmingResult<ProductFlowEventProto, BytesTruncatedProvider> {
        val proto = ProductFlowEventProto()
        proto.eventType = ProductFlowEventProto.EVENT_TYPE_FLOW_RESULT

        val productIdResult = stringTrimmer.trim(event.productId)
        proto.productId = StringUtils.getUTF8Bytes(productIdResult.value)

        val offerIdResult = stringTrimmer.trim(event.productOfferId)
        proto.productOfferId = StringUtils.getUTF8Bytes(offerIdResult.value)

        proto.resultInfo = ProductFlowEventProto.ResultInfo()
        proto.resultInfo.status = toProtoStatus(event.status)

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

        val bytesTruncated = BytesTruncatedInfo.total(
            productIdResult,
            offerIdResult,
            amountResult,
            payloadResult,
        )

        return TrimmingResult(proto, bytesTruncated)
    }

    private fun toProtoStatus(status: ProductFlowStatus): Int = when (status) {
        ProductFlowStatus.SUCCESS -> ProductFlowEventProto.ResultInfo.STATUS_SUCCESS
        ProductFlowStatus.DECLINED -> ProductFlowEventProto.ResultInfo.STATUS_DECLINED
        ProductFlowStatus.PENDING -> ProductFlowEventProto.ResultInfo.STATUS_PENDING
        ProductFlowStatus.CANCELLED -> ProductFlowEventProto.ResultInfo.STATUS_CANCELLED
        ProductFlowStatus.EXPIRED -> ProductFlowEventProto.ResultInfo.STATUS_EXPIRED
        ProductFlowStatus.FAIL -> ProductFlowEventProto.ResultInfo.STATUS_FAIL
    }
}
