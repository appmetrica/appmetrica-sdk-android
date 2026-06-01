package io.appmetrica.analytics.adrevenue.applovin.v12.auto.impl.config.service.parser

import io.appmetrica.analytics.adrevenue.applovin.v12.auto.impl.Constants
import io.appmetrica.analytics.coreutils.internal.parsing.RemoteConfigJsonUtils
import io.appmetrica.gradle.testutils.CommonTest
import io.appmetrica.gradle.testutils.rules.MockedStaticRule.Companion.on
import io.appmetrica.gradle.testutils.rules.MockedStaticRule.Companion.staticRule
import org.assertj.core.api.Assertions.assertThat
import org.json.JSONObject
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.doReturn

internal class ApplovinConfigJsonParserTest : CommonTest() {

    private val rawData = JSONObject()

    @get:Rule
    val remoteConfigJsonUtils = staticRule<RemoteConfigJsonUtils> {
        on {
            RemoteConfigJsonUtils.extractFeature(
                rawData,
                Constants.RemoteConfig.FEATURE_NAME,
                Constants.Defaults.DEFAULT_ENABLED
            )
        } doReturn true
    }

    private val parser = ApplovinConfigJsonParser()

    @Test
    fun parse() {
        val result = parser.parse(rawData)

        assertThat(result.enabled).isTrue()
    }

    @Test
    fun parseDefaultsWhenExtractFeatureReturnsFalse() {
        val emptyData = JSONObject()
        val result = parser.parse(emptyData)

        assertThat(result.enabled).isFalse()
    }
}
