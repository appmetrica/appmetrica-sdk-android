package io.appmetrica.analytics.productflow.impl.converter

import io.appmetrica.analytics.coreutils.internal.StringUtils.getUTF8Bytes
import io.appmetrica.analytics.coreutils.internal.limitation.CollectionTrimInfo
import io.appmetrica.analytics.coreutils.internal.limitation.TrimmingResult
import io.appmetrica.analytics.coreutils.internal.limitation.hierarchical.HierarchicalValueSizeOrderBasedWithBytesLimitStringMapTrimmer
import io.appmetrica.gradle.testutils.CommonTest
import io.appmetrica.gradle.testutils.assertions.ProtoObjectPropertyAssertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import io.appmetrica.analytics.productflow.impl.protobuf.backend.ProductFlowEvent as ProductFlowEventProto

internal class PayloadConverterTest : CommonTest() {

    private val payloadTrimmer: HierarchicalValueSizeOrderBasedWithBytesLimitStringMapTrimmer = mock()
    private val converter = PayloadConverter(payloadTrimmer)

    @Test
    fun `convert maps payload pairs and truncation metadata`() {
        val input = linkedMapOf("key1" to "value1", "key2" to "value2")
        val pairsTruncated = 3
        val bytesTruncated = 42

        whenever(payloadTrimmer.trim(input)).thenReturn(
            TrimmingResult(input, CollectionTrimInfo(pairsTruncated, bytesTruncated))
        )

        val result = converter.convert(input)

        assertThat(result.bytesTruncated).isEqualTo(bytesTruncated)
        ProtoObjectPropertyAssertions(result.value)
            .checkField("truncatedPairsCount", pairsTruncated)
            .checkField(
                "pairs",
                arrayOf(
                    ProductFlowEventProto.Payload.Pair().apply {
                        key = getUTF8Bytes("key1")
                        value = getUTF8Bytes("value1")
                    },
                    ProductFlowEventProto.Payload.Pair().apply {
                        key = getUTF8Bytes("key2")
                        value = getUTF8Bytes("value2")
                    },
                ),
            )
            .checkAll()
    }

    @Test
    fun `convert handles empty and null trimmed payload`() {
        whenever(payloadTrimmer.trim(emptyMap())).thenReturn(
            TrimmingResult(emptyMap(), CollectionTrimInfo(0, 0))
        )
        whenever(payloadTrimmer.trim(mapOf("key" to "value"))).thenReturn(
            TrimmingResult(null, CollectionTrimInfo(0, 0))
        )

        val emptyResult = converter.convert(emptyMap())
        assertThat(emptyResult.bytesTruncated).isZero()
        ProtoObjectPropertyAssertions(emptyResult.value)
            .checkField("pairs", emptyArray<ProductFlowEventProto.Payload.Pair>())
            .checkField("truncatedPairsCount", 0)
            .checkAll()

        val nullResult = converter.convert(mapOf("key" to "value"))
        assertThat(nullResult.bytesTruncated).isZero()
        ProtoObjectPropertyAssertions(nullResult.value)
            .checkField("pairs", emptyArray<ProductFlowEventProto.Payload.Pair>())
            .checkField("truncatedPairsCount", 0)
            .checkAll()
    }
}
