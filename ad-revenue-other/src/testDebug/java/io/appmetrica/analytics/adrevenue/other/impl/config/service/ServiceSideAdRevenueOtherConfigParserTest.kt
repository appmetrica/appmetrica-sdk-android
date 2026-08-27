package io.appmetrica.analytics.adrevenue.other.impl.config.service

import io.appmetrica.analytics.adrevenue.other.impl.config.service.model.ServiceSideAdRevenueOtherConfig
import io.appmetrica.analytics.adrevenue.other.impl.config.service.parser.AdRevenueOtherConfigJsonParser
import io.appmetrica.gradle.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.json.JSONObject
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

internal class ServiceSideAdRevenueOtherConfigParserTest : CommonTest() {

    private val rawData = JSONObject()
    private val serviceSideConfig: ServiceSideAdRevenueOtherConfig = mock()

    private val jsonParser: AdRevenueOtherConfigJsonParser = mock {
        on { parse(rawData) } doReturn serviceSideConfig
    }

    private val parser = ServiceSideAdRevenueOtherConfigParser(parser = jsonParser)

    @Test
    fun parse() {
        assertThat(parser.parse(rawData).config).isSameAs(serviceSideConfig)
    }
}
