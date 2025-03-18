package io.appmetrica.analytics.screenshot.impl.config.remote

import io.appmetrica.analytics.protobuf.nano.MessageNano
import io.appmetrica.analytics.screenshot.impl.RemoteScreenshotConfigProto
import io.appmetrica.analytics.screenshot.impl.config.remote.converter.RemoteScreenshotConfigProtoConverter
import io.appmetrica.analytics.screenshot.impl.config.remote.model.RemoteScreenshotConfig
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.on
import io.appmetrica.analytics.testutils.staticRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

class RemoteScreenshotConfigConverterTest : CommonTest() {

    private val remoteScreenshotConfig: RemoteScreenshotConfig = mock()
    private val remoteScreenshotConfigProto: RemoteScreenshotConfigProto = mock()
    private val byteArray = "byteArray".toByteArray()

    private val protoConverter: RemoteScreenshotConfigProtoConverter = mock {
        on { fromModel(remoteScreenshotConfig) } doReturn remoteScreenshotConfigProto
        on { toModel(remoteScreenshotConfigProto) } doReturn remoteScreenshotConfig
    }

    @get:Rule
    val messageNanoRule = staticRule<MessageNano> {
        on { MessageNano.toByteArray(remoteScreenshotConfigProto) } doReturn byteArray
    }
    @get:Rule
    val remoteScreenshotConfigProtoRule = staticRule<RemoteScreenshotConfigProto> {
        on { RemoteScreenshotConfigProto.parseFrom(byteArray) } doReturn remoteScreenshotConfigProto
    }

    private val converter = RemoteScreenshotConfigConverter(
        protoConverter = protoConverter
    )

    @Test
    fun fromModel() {
        assertThat(converter.fromModel(remoteScreenshotConfig)).isSameAs(byteArray)
    }

    @Test
    fun toModel() {
        assertThat(converter.toModel(byteArray)).isSameAs(remoteScreenshotConfig)
    }
}
