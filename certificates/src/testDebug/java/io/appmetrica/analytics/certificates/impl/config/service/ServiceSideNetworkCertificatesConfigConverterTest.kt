package io.appmetrica.analytics.certificates.impl.config.service

import io.appmetrica.analytics.certificates.impl.RemoteNetworkCertificatesConfigProto
import io.appmetrica.analytics.certificates.impl.config.service.converter.NetworkCertificatesConfigProtoConverter
import io.appmetrica.analytics.certificates.impl.config.service.model.ServiceSideNetworkCertificatesConfig
import io.appmetrica.analytics.protobuf.nano.MessageNano
import io.appmetrica.gradle.testutils.CommonTest
import io.appmetrica.gradle.testutils.rules.MockedStaticRule.Companion.on
import io.appmetrica.gradle.testutils.rules.MockedStaticRule.Companion.staticRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

internal class ServiceSideNetworkCertificatesConfigConverterTest : CommonTest() {

    private val config: ServiceSideNetworkCertificatesConfig = mock()
    private val proto: RemoteNetworkCertificatesConfigProto = mock()
    private val byteArray = "byteArray".toByteArray()

    private val protoConverter: NetworkCertificatesConfigProtoConverter = mock {
        on { fromModel(config) } doReturn proto
        on { toModel(proto) } doReturn config
    }

    @get:Rule
    val messageNanoRule = staticRule<MessageNano> {
        on { MessageNano.toByteArray(proto) } doReturn byteArray
    }

    @get:Rule
    val remoteNetworkCertificatesConfigProtoRule = staticRule<RemoteNetworkCertificatesConfigProto> {
        on { RemoteNetworkCertificatesConfigProto.parseFrom(byteArray) } doReturn proto
    }

    private val converter = ServiceSideNetworkCertificatesConfigConverter(protoConverter)

    @Test
    fun fromModel() {
        assertThat(converter.fromModel(config)).isSameAs(byteArray)
    }

    @Test
    fun toModel() {
        assertThat(converter.toModel(byteArray)).isSameAs(config)
    }
}
