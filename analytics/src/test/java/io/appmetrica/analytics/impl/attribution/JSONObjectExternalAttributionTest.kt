package io.appmetrica.analytics.impl.attribution

import io.appmetrica.analytics.impl.protobuf.backend.ExternalAttribution.ClientExternalAttribution
import io.appmetrica.analytics.protobuf.nano.MessageNano
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.MockedStaticRule
import org.assertj.core.api.SoftAssertions
import org.json.JSONObject
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.whenever

class JSONObjectExternalAttributionTest : CommonTest() {

    private val provider = ExternalAttributionType.AIRBRIDGE
    private val value = JSONObject().also {
        it.put("key", "value")
    }

    @get:Rule
    val messageNanoRule = MockedStaticRule(MessageNano::class.java)

    @Before
    fun setUp() {
        whenever(MessageNano.toByteArray(any())).thenReturn("".toByteArray())
    }

    @Test
    fun constructor() {
        JSONObjectExternalAttribution(provider, value).toBytes()

        val captor = argumentCaptor<ClientExternalAttribution>()
        messageNanoRule.staticMock.verify {
            MessageNano.toByteArray(captor.capture())
        }

        val proto = captor.firstValue
        SoftAssertions().apply {
            assertThat(proto.attributionType).isEqualTo(ClientExternalAttribution.AIRBRIDGE)
            assertThat(JSONObject(String(proto.value))).isEqualToComparingFieldByField(value)
            assertAll()
        }
    }
}
