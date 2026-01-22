package io.appmetrica.analytics.billing.impl.config.remote

import io.appmetrica.analytics.billing.impl.RemoteBillingConfigProto
import io.appmetrica.analytics.billing.impl.config.remote.converter.RemoteBillingConfigProtoConverter
import io.appmetrica.analytics.billing.internal.config.RemoteBillingConfig
import io.appmetrica.analytics.protobuf.nano.MessageNano
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.on
import io.appmetrica.analytics.testutils.staticRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

internal class RemoteBillingConfigConverterTest : CommonTest() {

    private val remoteBillingConfig: RemoteBillingConfig = mock()
    private val remoteBillingConfigProto: RemoteBillingConfigProto = mock()
    private val byteArray = "byteArray".toByteArray()

    private val protoConverter: RemoteBillingConfigProtoConverter = mock {
        on { fromModel(remoteBillingConfig) } doReturn remoteBillingConfigProto
        on { toModel(remoteBillingConfigProto) } doReturn remoteBillingConfig
    }

    @get:Rule
    val messageNanoRule = staticRule<MessageNano> {
        on { MessageNano.toByteArray(remoteBillingConfigProto) } doReturn byteArray
    }

    @get:Rule
    val remoteBillingConfigProtoRule = staticRule<RemoteBillingConfigProto> {
        on { RemoteBillingConfigProto.parseFrom(byteArray) } doReturn remoteBillingConfigProto
    }

    private val converter = RemoteBillingConfigConverter(protoConverter)

    @Test
    fun fromModel() {
        assertThat(converter.fromModel(remoteBillingConfig)).isSameAs(byteArray)
    }

    @Test
    fun toModel() {
        assertThat(converter.toModel(byteArray)).isSameAs(remoteBillingConfig)
    }
}
