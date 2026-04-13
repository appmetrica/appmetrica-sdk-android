package io.appmetrica.analytics.screenshot.impl.config.service

import io.appmetrica.analytics.protobuf.nano.MessageNano
import io.appmetrica.analytics.screenshot.impl.RemoteScreenshotConfigProto
import io.appmetrica.analytics.screenshot.impl.config.service.converter.ScreenshotConfigProtoConverter
import io.appmetrica.analytics.screenshot.impl.config.service.model.ServiceSideScreenshotConfig
import io.appmetrica.gradle.testutils.CommonTest
import io.appmetrica.gradle.testutils.rules.MockedStaticRule.Companion.on
import io.appmetrica.gradle.testutils.rules.MockedStaticRule.Companion.staticRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

internal class ServiceSideScreenshotConfigConverterTest : CommonTest() {

    private val config: ServiceSideScreenshotConfig = mock()
    private val proto: RemoteScreenshotConfigProto = mock()
    private val byteArray = "byteArray".toByteArray()

    private val protoConverter: ScreenshotConfigProtoConverter = mock {
        on { fromModel(config) } doReturn proto
        on { toModel(proto) } doReturn config
    }

    @get:Rule
    val messageNanoRule = staticRule<MessageNano> {
        on { MessageNano.toByteArray(proto) } doReturn byteArray
    }
    @get:Rule
    val remoteScreenshotConfigProtoRule = staticRule<RemoteScreenshotConfigProto> {
        on { RemoteScreenshotConfigProto.parseFrom(byteArray) } doReturn proto
    }

    private val converter = ServiceSideScreenshotConfigConverter(
        protoConverter = protoConverter
    )

    @Test
    fun fromModel() {
        assertThat(converter.fromModel(config)).isSameAs(byteArray)
    }

    @Test
    fun toModel() {
        assertThat(converter.toModel(byteArray)).isSameAs(config)
    }
}
