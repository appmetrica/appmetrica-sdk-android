package io.appmetrica.analytics.adrevenue.other.impl.config.service.parser

import io.appmetrica.analytics.adrevenue.other.impl.Constants
import io.appmetrica.analytics.coreutils.internal.parsing.RemoteConfigJsonUtils
import io.appmetrica.gradle.testutils.CommonTest
import io.appmetrica.gradle.testutils.rules.MockedStaticRule.Companion.on
import io.appmetrica.gradle.testutils.rules.MockedStaticRule.Companion.staticRule
import org.assertj.core.api.Assertions.assertThat
import org.json.JSONObject
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.doReturn

internal class AdRevenueOtherConfigJsonParserTest : CommonTest() {

    private val rawData = JSONObject()

    @get:Rule
    val remoteConfigJsonUtils = staticRule<RemoteConfigJsonUtils> {
        on {
            RemoteConfigJsonUtils.extractFeature(rawData, Constants.RemoteConfig.FEATURE_NAME, false)
        } doReturn true
        on {
            RemoteConfigJsonUtils.extractFeature(rawData, Constants.RemoteConfig.INCLUDE_SOURCE_NAME, false)
        } doReturn true
    }

    private val parser = AdRevenueOtherConfigJsonParser()

    @Test
    fun parse() {
        val result = parser.parse(rawData)

        assertThat(result.enabled).isTrue()
        assertThat(result.includeSource).isTrue()
    }

    @Test
    fun parseDefaults() {
        val emptyData = JSONObject()
        val result = parser.parse(emptyData)

        assertThat(result.enabled).isFalse()
        assertThat(result.includeSource).isFalse()
    }
}
