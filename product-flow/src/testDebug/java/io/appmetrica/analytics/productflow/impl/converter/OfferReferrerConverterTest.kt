package io.appmetrica.analytics.productflow.impl.converter

import io.appmetrica.analytics.coreutils.internal.StringUtils.getUTF8Bytes
import io.appmetrica.analytics.coreutils.internal.limitation.BytesTruncatedInfo
import io.appmetrica.analytics.coreutils.internal.limitation.TrimmingResult
import io.appmetrica.analytics.coreutils.internal.limitation.hierarchical.HierarchicalStringTrimmer
import io.appmetrica.analytics.productflow.OfferReferrer
import io.appmetrica.gradle.testutils.CommonTest
import io.appmetrica.gradle.testutils.assertions.ProtoObjectPropertyAssertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

internal class OfferReferrerConverterTest : CommonTest() {

    private val stringTrimmer: HierarchicalStringTrimmer = mock()
    private val converter = OfferReferrerConverter(stringTrimmer)

    @Test
    fun `convert maps referrer fields and sums bytes truncated`() {
        val referrer = OfferReferrer.newBuilder()
            .withType("banner")
            .withIdentifier("campaign-1")
            .withScreen("home")
            .build()

        whenever(stringTrimmer.trim("banner")).thenReturn(TrimmingResult("banner", BytesTruncatedInfo(1)))
        whenever(stringTrimmer.trim("campaign-1")).thenReturn(TrimmingResult("campaign-1", BytesTruncatedInfo(2)))
        whenever(stringTrimmer.trim("home")).thenReturn(TrimmingResult("home", BytesTruncatedInfo(3)))

        val result = converter.convert(referrer)

        assertThat(result.bytesTruncated).isEqualTo(6)
        ProtoObjectPropertyAssertions(result.value)
            .checkField("type", getUTF8Bytes("banner"))
            .checkField("identifier", getUTF8Bytes("campaign-1"))
            .checkField("screen", getUTF8Bytes("home"))
            .checkAll()
    }

    @Test
    fun `convert handles null referrer fields`() {
        val referrer = OfferReferrer.newBuilder().build()

        whenever(stringTrimmer.trim(null)).thenReturn(TrimmingResult(null, BytesTruncatedInfo(0)))

        val result = converter.convert(referrer)

        assertThat(result.bytesTruncated).isZero()
        ProtoObjectPropertyAssertions(result.value)
            .checkField("type", getUTF8Bytes(null as String?))
            .checkField("identifier", getUTF8Bytes(null as String?))
            .checkField("screen", getUTF8Bytes(null as String?))
            .checkAll()
    }
}
