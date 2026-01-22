package io.appmetrica.analytics.impl.component.sessionextras

import io.appmetrica.analytics.impl.protobuf.client.SessionExtrasProtobuf
import io.appmetrica.analytics.protobuf.nano.MessageNano
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.MockedStaticRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

internal class SessionExtrasSerializerTest : CommonTest() {

    private val data = ByteArray(50) { it.toByte() }
    private val proto = mock<SessionExtrasProtobuf.SessionExtras>()

    @get:Rule
    val sessionExtrasMockedStaticRule = MockedStaticRule(SessionExtrasProtobuf.SessionExtras::class.java)

    @get:Rule
    val messageNanoMockedStaticRule = MockedStaticRule(MessageNano::class.java)

    private lateinit var serializer: SessionExtrasSerializer

    @Before
    fun setUp() {
        whenever(MessageNano.toByteArray(proto)).thenReturn(data)
        whenever(SessionExtrasProtobuf.SessionExtras.parseFrom(data)).thenReturn(proto)

        serializer = SessionExtrasSerializer()
    }

    @Test
    fun toByteArray() {
        assertThat(serializer.toByteArray(proto)).isEqualTo(data)
    }

    @Test
    fun toState() {
        assertThat(serializer.toState(data)).isEqualTo(proto)
    }

    @Test
    fun defaultValue() {
        assertThat(serializer.defaultValue())
            .isEqualToComparingFieldByField(SessionExtrasProtobuf.SessionExtras())
    }
}
