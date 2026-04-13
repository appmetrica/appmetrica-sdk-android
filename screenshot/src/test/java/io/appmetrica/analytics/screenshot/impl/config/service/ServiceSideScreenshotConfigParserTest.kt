package io.appmetrica.analytics.screenshot.impl.config.service

import io.appmetrica.analytics.screenshot.impl.config.service.model.ServiceSideScreenshotConfig
import io.appmetrica.analytics.screenshot.impl.config.service.parser.ScreenshotConfigJsonParser
import io.appmetrica.gradle.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.json.JSONObject
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

internal class ServiceSideScreenshotConfigParserTest : CommonTest() {

    private val rawData = JSONObject()
    private val serviceSideScreenshotConfig: ServiceSideScreenshotConfig = mock()

    private val jsonParser: ScreenshotConfigJsonParser = mock {
        on { parse(rawData) } doReturn serviceSideScreenshotConfig
    }

    private val parser = ServiceSideScreenshotConfigParser(parser = jsonParser)

    @Test
    fun parse() {
        assertThat(parser.parse(rawData).config).isSameAs(serviceSideScreenshotConfig)
    }
}
