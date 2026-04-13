package io.appmetrica.analytics.screenshot.impl.config.service.parser

import io.appmetrica.analytics.coreutils.internal.parsing.RemoteConfigJsonUtils
import io.appmetrica.analytics.screenshot.impl.config.service.model.ServiceSideApiCaptorConfig
import io.appmetrica.analytics.screenshot.impl.config.service.model.ServiceSideContentObserverCaptorConfig
import io.appmetrica.analytics.screenshot.impl.config.service.model.ServiceSideScreenshotConfig
import io.appmetrica.analytics.screenshot.impl.config.service.model.ServiceSideServiceCaptorConfig
import io.appmetrica.gradle.testutils.CommonTest
import io.appmetrica.gradle.testutils.rules.MockedStaticRule.Companion.on
import io.appmetrica.gradle.testutils.rules.MockedStaticRule.Companion.staticRule
import org.assertj.core.api.Assertions.assertThat
import org.json.JSONObject
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

internal class ScreenshotConfigJsonParserTest : CommonTest() {

    private val blockJson = JSONObject().put("block", "block")

    private val apiCaptorConfig: ServiceSideApiCaptorConfig = mock()
    private val apiCaptorConfigJsonParser: ApiCaptorConfigJsonParser = mock {
        on { parse(blockJson) } doReturn apiCaptorConfig
    }

    private val serviceCaptorConfig: ServiceSideServiceCaptorConfig = mock()
    private val serviceCaptorConfigJsonParser: ServiceCaptorConfigJsonParser = mock {
        on { parse(blockJson) } doReturn serviceCaptorConfig
    }

    private val contentObserverCaptorConfig: ServiceSideContentObserverCaptorConfig = mock()
    private val contentObserverConfigJsonParser: ContentObserverCaptorConfigJsonParser = mock {
        on { parse(blockJson) } doReturn contentObserverCaptorConfig
    }

    private val parser = ScreenshotConfigJsonParser(
        apiCaptorConfigJsonParser,
        serviceCaptorConfigJsonParser,
        contentObserverConfigJsonParser,
    )

    @get:Rule
    val remoteConfigJsonUtils = staticRule<RemoteConfigJsonUtils> {
        on { RemoteConfigJsonUtils.extractFeature(rawDataWithBlock, "screenshot", true) } doReturn true
    }

    private val rawDataWithBlock = JSONObject().put("screenshot", blockJson)

    @Test
    fun parse() {
        val result = parser.parse(rawDataWithBlock)

        assertThat(result.enabled).isTrue()
        assertThat(result.apiCaptorConfig).isSameAs(apiCaptorConfig)
        assertThat(result.serviceCaptorConfig).isSameAs(serviceCaptorConfig)
        assertThat(result.contentObserverCaptorConfig).isSameAs(contentObserverCaptorConfig)
    }

    @Test
    fun parseIfNoValues() {
        val rawData = JSONObject().put("screenshot", JSONObject())

        val result = parser.parse(rawData)

        assertThat(result.apiCaptorConfig).usingRecursiveComparison().isEqualTo(ServiceSideApiCaptorConfig())
        assertThat(result.serviceCaptorConfig).usingRecursiveComparison().isEqualTo(ServiceSideServiceCaptorConfig())
        assertThat(result.contentObserverCaptorConfig)
            .usingRecursiveComparison()
            .isEqualTo(ServiceSideContentObserverCaptorConfig())
    }

    @Test
    fun parseIfNoBlock() {
        assertThat(parser.parse(JSONObject()))
            .usingRecursiveComparison()
            .isEqualTo(
                ServiceSideScreenshotConfig(
                    false,
                    ServiceSideApiCaptorConfig(),
                    ServiceSideServiceCaptorConfig(),
                    ServiceSideContentObserverCaptorConfig()
                )
            )
    }
}
