package io.appmetrica.analytics.impl.attribution

import io.appmetrica.analytics.impl.protobuf.backend.ExternalAttribution.ClientExternalAttribution
import io.appmetrica.analytics.protobuf.nano.MessageNano
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.MockedStaticRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.whenever

class BaseExternalAttributionTest : CommonTest() {

    private val fakeAttributionType = -1
    private val fakeAttributionTypeString = "fakeAttributionTypeString"
    private val fakeValue = "fakeValue"
    private val proto = ClientExternalAttribution().also {
        it.attributionType = fakeAttributionType
        it.value = fakeValue.toByteArray()
    }
    private val byteArray: ByteArray = "".toByteArray()

    @get:Rule
    val messageNanoRule = MockedStaticRule(MessageNano::class.java)
    @get:Rule
    val attributionTypeConverterRule = MockedStaticRule(ExternalAttributionTypeConverter::class.java)

    @Before
    fun setUp() {
        whenever(MessageNano.toByteArray(proto)).thenReturn(byteArray)
        whenever(ExternalAttributionTypeConverter.toString(fakeAttributionType)).thenReturn(fakeAttributionTypeString)
    }

    @Test
    fun toBytes() {
        val attribution = BaseExternalAttribution(proto)
        assertThat(attribution.toBytes()).isEqualTo(byteArray)
    }

    @Test
    fun toStringTest() {
        val attribution = BaseExternalAttribution(proto)
        assertThat(attribution.toString())
            .isEqualTo("ExternalAttribution(type=`fakeAttributionTypeString`value=`fakeValue`)")
    }
}
