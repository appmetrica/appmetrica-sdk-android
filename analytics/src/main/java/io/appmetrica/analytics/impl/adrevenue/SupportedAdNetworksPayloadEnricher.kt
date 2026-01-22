package io.appmetrica.analytics.impl.adrevenue

import android.content.Context
import io.appmetrica.analytics.modulesapi.internal.client.adrevenue.AdRevenueConstants

internal class SupportedAdNetworksPayloadEnricher(context: Context) : AdRevenuePayloadEnricher {

    private val nativeSupportedSourcesProvider: AdRevenueSupportedSourcesProvider =
        AdRevenueSupportedSourcesNativeProvider()

    private val pluginSupportedSourcesProvider: AdRevenueSupportedSourcesProvider =
        AdRevenueSupportedSourcesPluginProvider(context)

    private val payloadDelta: Map<String, String> by lazy {
        HashMap<String, String>().apply {
            nativeSupportedSourcesProvider.metaInfo?.let {
                put(AdRevenueConstants.NATIVE_SUPPORTED_SOURCES_KEY, it)
            }
            pluginSupportedSourcesProvider.metaInfo?.let {
                put(AdRevenueConstants.PLUGIN_SUPPORTED_SOURCES_KEY, it)
            }
        }
    }

    override fun enrich(payload: MutableMap<String, String>): MutableMap<String, String> {
        payload.putAll(payloadDelta)
        return payload
    }
}
