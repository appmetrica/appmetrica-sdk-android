package io.appmetrica.analytics.remotepermissions.impl

import io.appmetrica.analytics.protobuf.nano.MessageNano
import io.appmetrica.analytics.remotepermissions.impl.protobuf.client.RemotePermissionsProtobuf
import io.appmetrica.analytics.remotepermissions.internal.config.FeatureConfig
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.MockedConstructionRule
import io.appmetrica.analytics.testutils.MockedStaticRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

internal class FeatureConfigToProtoBytesConverterTest : CommonTest() {

    private val config = mock<FeatureConfig>()
    private val configProto = mock<RemotePermissionsProtobuf.RemotePermissions>()
    private val configBytes = ByteArray(6) { it.toByte() }

    @get:Rule
    val featureConfigToProtoConverterMockedConstructionRule =
        MockedConstructionRule(FeatureConfigToProtoConverter::class.java) { mock, _ ->
            whenever(mock.fromModel(config)).thenReturn(configProto)
            whenever(mock.toModel(configProto)).thenReturn(config)
        }

    @get:Rule
    val messageNanoMockedStaticRule = MockedStaticRule(MessageNano::class.java)

    @get:Rule
    val remotePermissionsMockedStaticRule = MockedStaticRule(RemotePermissionsProtobuf.RemotePermissions::class.java)

    private lateinit var featureConfigToProtoConverter: FeatureConfigToProtoBytesConverter

    @Before
    fun setUp() {
        whenever(MessageNano.toByteArray(configProto)).thenReturn(configBytes)
        whenever(RemotePermissionsProtobuf.RemotePermissions.parseFrom(configBytes)).thenReturn(configProto)

        featureConfigToProtoConverter = FeatureConfigToProtoBytesConverter()
    }

    @Test
    fun fromModel() {
        assertThat(featureConfigToProtoConverter.fromModel(config)).isEqualTo(configBytes)
    }

    @Test
    fun toModel() {
        assertThat(featureConfigToProtoConverter.toModel(configBytes)).isEqualTo(config)
    }
}
