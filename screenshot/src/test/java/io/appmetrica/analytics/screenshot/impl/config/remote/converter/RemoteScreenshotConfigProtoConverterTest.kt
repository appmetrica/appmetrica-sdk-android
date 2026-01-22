package io.appmetrica.analytics.screenshot.impl.config.remote.converter

import io.appmetrica.analytics.assertions.ObjectPropertyAssertions
import io.appmetrica.analytics.assertions.ProtoObjectPropertyAssertions
import io.appmetrica.analytics.screenshot.impl.RemoteScreenshotConfigProto
import io.appmetrica.analytics.screenshot.impl.ScreenshotConfigProto
import io.appmetrica.analytics.screenshot.impl.config.remote.model.ScreenshotConfig
import io.appmetrica.analytics.screenshot.internal.config.RemoteScreenshotConfig
import io.appmetrica.analytics.testutils.CommonTest
import org.junit.Test
import org.mockito.kotlin.mock

internal class RemoteScreenshotConfigProtoConverterTest : CommonTest() {

    private val screenshotConfig: ScreenshotConfig = mock()
    private val screenshotConfigProto: ScreenshotConfigProto = mock()
    private val screenshotConfigProtoConverter: ScreenshotConfigProtoConverter = mock {
        on { fromModel(screenshotConfig) }.thenReturn(screenshotConfigProto)
        on { toModel(screenshotConfigProto) }.thenReturn(screenshotConfig)
    }

    private val converter = RemoteScreenshotConfigProtoConverter(
        screenshotConfigProtoConverter
    )

    @Test
    fun fromModel() {
        val value = RemoteScreenshotConfig(
            enabled = true,
            config = screenshotConfig
        )
        ProtoObjectPropertyAssertions(converter.fromModel(value))
            .checkField("enabled", true)
            .checkField("config", screenshotConfigProto)
            .checkAll()
    }

    @Test
    fun toModel() {
        val value = RemoteScreenshotConfigProto().also {
            it.enabled = true
            it.config = screenshotConfigProto
        }
        ObjectPropertyAssertions(converter.toModel(value))
            .checkField("enabled", true)
            .checkField("config", screenshotConfig)
            .checkAll()
    }
}
