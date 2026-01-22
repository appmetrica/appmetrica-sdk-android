package io.appmetrica.analytics.idsync.impl.model

import io.appmetrica.analytics.idsync.impl.protobuf.client.IdSyncProtobuf
import io.appmetrica.analytics.idsync.internal.model.IdSyncConfig
import io.appmetrica.analytics.protobuf.nano.MessageNano
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.on
import io.appmetrica.analytics.testutils.staticRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

internal class IdSyncConfigToProtoBytesConverterTest : CommonTest() {

    private val byteArray: ByteArray = ByteArray(15) { it.toByte() }
    private val idSyncConfig: IdSyncConfig = mock()
    private val protoConfig: IdSyncProtobuf.IdSyncConfig = mock()

    private val protoConverter: IdSyncConfigToProtoConverter = mock {
        on { toModel(protoConfig) } doReturn idSyncConfig
        on { fromModel(idSyncConfig) } doReturn protoConfig
    }

    @get:Rule
    val messageNanoRule = staticRule<MessageNano> {
        on { MessageNano.toByteArray(protoConfig) } doReturn byteArray
    }

    @get:Rule
    val idSyncConfigRule = staticRule<IdSyncProtobuf.IdSyncConfig> {
        on { IdSyncProtobuf.IdSyncConfig.parseFrom(byteArray) } doReturn protoConfig
    }

    private val converter by setUp { IdSyncConfigToProtoBytesConverter(protoConverter) }

    @Test
    fun fromModel() {
        assertThat(converter.fromModel(idSyncConfig)).isEqualTo(byteArray)
    }

    @Test
    fun toModel() {
        assertThat(converter.toModel(byteArray)).isEqualTo(idSyncConfig)
    }
}
