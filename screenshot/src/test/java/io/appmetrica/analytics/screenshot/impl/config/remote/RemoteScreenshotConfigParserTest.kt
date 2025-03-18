package io.appmetrica.analytics.screenshot.impl.config.remote

import io.appmetrica.analytics.assertions.ObjectPropertyAssertions
import io.appmetrica.analytics.coreutils.internal.parsing.RemoteConfigJsonUtils
import io.appmetrica.analytics.screenshot.impl.Constants
import io.appmetrica.analytics.screenshot.impl.ScreenshotConfigProto
import io.appmetrica.analytics.screenshot.impl.config.remote.converter.ScreenshotConfigProtoConverter
import io.appmetrica.analytics.screenshot.impl.config.remote.model.ScreenshotConfig
import io.appmetrica.analytics.screenshot.impl.config.remote.parser.ScreenshotConfigJsonParser
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.on
import io.appmetrica.analytics.testutils.staticRule
import org.json.JSONObject
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

class RemoteScreenshotConfigParserTest : CommonTest() {

    private val rawData = JSONObject()
    private val screenshotConfigProto: ScreenshotConfigProto = mock()
    private val screenshotConfig: ScreenshotConfig = mock()

    private val protoConverter: ScreenshotConfigProtoConverter = mock {
        on { toModel(screenshotConfigProto) } doReturn screenshotConfig
    }
    private val jsonParser: ScreenshotConfigJsonParser = mock {
        on { parse(rawData) } doReturn  screenshotConfigProto
    }

    @get:Rule
    val remoteConfigJsonUtils = staticRule<RemoteConfigJsonUtils> {
        on {
            RemoteConfigJsonUtils.extractFeature(
                rawData,
                "screenshot",
                true
            )
        } doReturn true
    }

    private val parser = RemoteScreenshotConfigParser(
        converter = protoConverter,
        parser = jsonParser
    )

    @Test
    fun parse() {
        ObjectPropertyAssertions(parser.parse(rawData))
            .checkField("enabled", true)
            .checkField("config", screenshotConfig)
            .checkAll()
    }
}
