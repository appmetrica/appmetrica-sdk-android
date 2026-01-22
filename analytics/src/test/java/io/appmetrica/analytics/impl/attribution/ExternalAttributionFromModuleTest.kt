package io.appmetrica.analytics.impl.attribution

import io.appmetrica.analytics.assertions.ProtoObjectPropertyAssertions
import io.appmetrica.analytics.impl.protobuf.backend.ExternalAttribution.ClientExternalAttribution
import io.appmetrica.analytics.testutils.CommonTest
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.junit.runners.Parameterized.Parameters

@RunWith(Parameterized::class)
internal class ExternalAttributionFromModuleTest(
    private val inputSource: Int,
    private val value: String?,
    private val expectedSource: Int,
    private val expectedValue: ByteArray
) : CommonTest() {

    companion object {
        @Parameters(name = "for input source = {0} and value = {1}")
        @JvmStatic
        fun data(): Collection<Array<Any?>> = listOf(
            arrayOf(ClientExternalAttribution.ADJUST, "value", ClientExternalAttribution.ADJUST, "value".toByteArray()),
            arrayOf(100500, "value", ClientExternalAttribution.UNKNOWN, "value".toByteArray()),
            arrayOf(ClientExternalAttribution.AIRBRIDGE, "", ClientExternalAttribution.AIRBRIDGE, ByteArray(0)),
            arrayOf(ClientExternalAttribution.TENJIN, null, ClientExternalAttribution.TENJIN, ByteArray(0))
        )
    }

    @Test
    fun toBytesArray() {
        val bytes = ExternalAttributionFromModule(inputSource, value).toBytes()
        val proto = ClientExternalAttribution.parseFrom(bytes)
        ProtoObjectPropertyAssertions(proto)
            .checkField("attributionType", expectedSource)
            .checkField("value", expectedValue)
            .checkAll()
    }
}
