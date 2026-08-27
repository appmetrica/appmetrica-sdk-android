package io.appmetrica.analytics.remotepermissions.impl.config.service

import io.appmetrica.analytics.protobuf.nano.MessageNano
import io.appmetrica.analytics.remotepermissions.impl.config.service.converter.RemotePermissionsConfigProtoConverter
import io.appmetrica.analytics.remotepermissions.impl.config.service.model.ServiceSideRemotePermissionsConfig
import io.appmetrica.analytics.remotepermissions.impl.protobuf.client.RemotePermissionsProtobuf
import io.appmetrica.analytics.remotepermissions.internal.ServiceSideRemotePermissionsConfigWrapper
import io.appmetrica.gradle.testutils.CommonTest
import io.appmetrica.gradle.testutils.rules.MockedStaticRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

internal class ServiceSideRemotePermissionsConfigConverterTest : CommonTest() {

    private val config = mock<ServiceSideRemotePermissionsConfig>()
    private val wrapper = mock<ServiceSideRemotePermissionsConfigWrapper> {
        on { config } doReturn config
    }
    private val configProto = mock<RemotePermissionsProtobuf.RemotePermissions>()
    private val configBytes = ByteArray(6) { it.toByte() }

    private val remotePermissionsConfigProtoConverter =
        mock<RemotePermissionsConfigProtoConverter> {
            on { fromModel(config) } doReturn configProto
            on { toModel(configProto) } doReturn config
        }

    @get:Rule
    val messageNanoMockedStaticRule = MockedStaticRule(MessageNano::class.java)

    @get:Rule
    val remotePermissionsMockedStaticRule = MockedStaticRule(RemotePermissionsProtobuf.RemotePermissions::class.java)

    private val converter = ServiceSideRemotePermissionsConfigConverter(remotePermissionsConfigProtoConverter)

    @Before
    fun setUp() {
        whenever(MessageNano.toByteArray(configProto)).thenReturn(configBytes)
        whenever(RemotePermissionsProtobuf.RemotePermissions.parseFrom(configBytes)).thenReturn(configProto)
    }

    @Test
    fun fromModel() {
        assertThat(converter.fromModel(wrapper)).isEqualTo(configBytes)
    }

    @Test
    fun toModel() {
        val result = converter.toModel(configBytes)
        assertThat(result.config.permittedPermissions).isEqualTo(config.permittedPermissions)
    }
}
