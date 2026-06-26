package io.appmetrica.analytics.productflow.impl.converter

import io.appmetrica.analytics.coreutils.internal.StringUtils
import io.appmetrica.analytics.coreutils.internal.limitation.BytesTruncatedInfo
import io.appmetrica.analytics.coreutils.internal.limitation.BytesTruncatedProvider
import io.appmetrica.analytics.coreutils.internal.limitation.TrimmingResult
import io.appmetrica.analytics.coreutils.internal.limitation.hierarchical.HierarchicalStringTrimmer
import io.appmetrica.analytics.productflow.impl.events.model.FlowStepEvent
import io.appmetrica.analytics.productflow.impl.protobuf.backend.ProductFlowEvent as ProductFlowEventProto

internal class FlowStepEventConverter(
    private val offerPriceConverter: OfferPriceConverter,
    private val payloadConverter: PayloadConverter,
    private val stringTrimmer: HierarchicalStringTrimmer
) {

    fun convert(event: FlowStepEvent): TrimmingResult<ProductFlowEventProto, BytesTruncatedProvider> {
        val proto = ProductFlowEventProto()
        proto.eventType = ProductFlowEventProto.EVENT_TYPE_STEP

        val productIdResult = stringTrimmer.trim(event.productId)
        proto.productId = StringUtils.getUTF8Bytes(productIdResult.value)

        val offerIdResult = stringTrimmer.trim(event.productOfferId)
        proto.productOfferId = StringUtils.getUTF8Bytes(offerIdResult.value)

        proto.stepInfo = ProductFlowEventProto.StepInfo()

        val stepTypeResult = stringTrimmer.trim(event.stepType)
        proto.stepInfo.stepType = StringUtils.getUTF8Bytes(stepTypeResult.value)

        val stepOptionResult = stringTrimmer.trim(event.stepOption)
        proto.stepInfo.stepOption = StringUtils.getUTF8Bytes(stepOptionResult.value)

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
            stepTypeResult,
            stepOptionResult,
            amountResult,
            payloadResult,
        )

        return TrimmingResult(proto, bytesTruncated)
    }
}
