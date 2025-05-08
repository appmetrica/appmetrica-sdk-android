package io.appmetrica.analytics.impl.adrevenue

import android.content.Context
import io.appmetrica.analytics.modulesapi.internal.client.adrevenue.AdRevenueConstants
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.constructionRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

class SupportedAdNetworksPayloadEnricherTest : CommonTest() {

    private val context: Context = mock()
    private val nativeSupportedSources = "Native supported sources"
    private val pluginSupportedSources = "Plugin supported sources"

    @get:Rule
    val adRevenueSupportedSourcesNativeProvider = constructionRule<AdRevenueSupportedSourcesNativeProvider> {
        on { metaInfo } doReturn nativeSupportedSources
    }

    @get:Rule
    val adRevenueSupportedSourcesPluginProvider = constructionRule<AdRevenueSupportedSourcesPluginProvider> {
        on { metaInfo } doReturn pluginSupportedSources
    }

    private val inputPayloadKey = "Input payload key"
    private val inputPayloadValue = "Input payload value"
    private val inputPayload = mutableMapOf(inputPayloadKey to inputPayloadValue)

    private val enricher by setUp { SupportedAdNetworksPayloadEnricher(context) }

    @Test
    fun enrich() {
        val outputPayload = enricher.enrich(inputPayload)
        assertThat(outputPayload)
            .isSameAs(inputPayload)
            .isEqualTo(
                mutableMapOf(
                    inputPayloadKey to inputPayloadValue,
                    AdRevenueConstants.NATIVE_SUPPORTED_SOURCES_KEY to nativeSupportedSources,
                    AdRevenueConstants.PLUGIN_SUPPORTED_SOURCES_KEY to pluginSupportedSources
                )
            )
    }
}
