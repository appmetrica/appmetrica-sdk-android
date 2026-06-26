package io.appmetrica.analytics.productflow.impl.converter

import io.appmetrica.analytics.coreutils.internal.StringUtils
import io.appmetrica.analytics.coreutils.internal.limitation.BytesTruncatedInfo
import io.appmetrica.analytics.coreutils.internal.limitation.BytesTruncatedProvider
import io.appmetrica.analytics.coreutils.internal.limitation.TrimmingResult
import io.appmetrica.analytics.coreutils.internal.limitation.hierarchical.HierarchicalValueSizeOrderBasedWithBytesLimitStringMapTrimmer
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger
import io.appmetrica.analytics.productflow.impl.ProductFlowConstants
import io.appmetrica.analytics.productflow.impl.protobuf.backend.ProductFlowEvent as ProductFlowEventProto

internal class PayloadConverter(
    private val payloadTrimmer: HierarchicalValueSizeOrderBasedWithBytesLimitStringMapTrimmer
) {

    private val tag = "[PayloadConverter]"

    fun convert(value: Map<String, String>): TrimmingResult<ProductFlowEventProto.Payload, BytesTruncatedProvider> {
        val proto = ProductFlowEventProto.Payload()

        val truncatedPayload = payloadTrimmer.trim(value)

        proto.truncatedPairsCount = truncatedPayload.metaInfo.itemsDropped
        val payload = truncatedPayload.value
        if (payload != null) {
            proto.pairs = arrayOfNulls<ProductFlowEventProto.Payload.Pair>(payload.size)
            for ((i, pair) in payload.entries.withIndex()) {
                proto.pairs[i] = ProductFlowEventProto.Payload.Pair()
                proto.pairs[i].key = StringUtils.getUTF8Bytes(pair.key)
                proto.pairs[i].value = StringUtils.getUTF8Bytes(pair.value)
            }
        }

        if (truncatedPayload.metaInfo.getBytesTruncated() > 0) {
            DebugLogger.info(
                ProductFlowConstants.FEATURE_TAG + tag,
                "Truncate payload $value -> ${truncatedPayload.value} with " +
                    "dropped ${truncatedPayload.metaInfo.itemsDropped} pairs and " +
                    "bytesTruncated = ${truncatedPayload.metaInfo.getBytesTruncated()}"
            )
        }

        val bytesTruncated = BytesTruncatedInfo.total(
            truncatedPayload
        ).bytesTruncated

        return TrimmingResult(
            proto,
            BytesTruncatedInfo(bytesTruncated)
        )
    }
}
