package io.appmetrica.analytics.productflow.impl.converter

import io.appmetrica.analytics.coreutils.internal.StringUtils.getUTF8Bytes
import io.appmetrica.analytics.coreutils.internal.limitation.BytesTruncatedInfo
import io.appmetrica.analytics.coreutils.internal.limitation.TrimmingResult
import io.appmetrica.analytics.coreutils.internal.limitation.hierarchical.HierarchicalStringTrimmer
import io.appmetrica.analytics.coreutils.internal.proto.DecimalProtoModel
import io.appmetrica.analytics.productflow.OfferPrice
import io.appmetrica.gradle.testutils.CommonTest
import io.appmetrica.gradle.testutils.assertions.ProtoObjectPropertyAssertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.math.BigDecimal

internal class OfferPriceConverterTest : CommonTest() {

    private val stringTrimmer: HierarchicalStringTrimmer = mock()
    private val converter = OfferPriceConverter(stringTrimmer)

    @Test
    fun `convert maps decimal amount and trimmed unit`() {
        val amount = BigDecimal("99.99")
        val unit = "USD"
        val unitBytesTruncated = 7
        whenever(stringTrimmer.trim(unit)).thenReturn(
            TrimmingResult(
                unit,
                BytesTruncatedInfo(unitBytesTruncated)
            )
        )

        val result = converter.convert(OfferPrice(amount, unit))
        val decimal = DecimalProtoModel.fromDecimal(amount)

        assertThat(result.bytesTruncated).isEqualTo(unitBytesTruncated)
        ProtoObjectPropertyAssertions(result.value)
            .checkField("mantissa", decimal.mantissa)
            .checkField("exponent", decimal.exponent)
            .checkField("unit", getUTF8Bytes(unit))
            .checkAll()
    }
}
