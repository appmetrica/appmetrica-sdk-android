package io.appmetrica.analytics.impl.adrevenue

import io.appmetrica.analytics.impl.ClientServiceLocator
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger
import org.json.JSONArray

internal class AdRevenueSupportedSourcesNativeProvider : AdRevenueSupportedSourcesProvider {

    private val tag = "[AdRevenueSupportedSourcesMetaInfoNativeProvider]"

    private val yandexAdNetworkSourceId = "yandex"

    override val metaInfo: String?
        get() {
            try {
                val autoCollectorsSources =
                    ClientServiceLocator.getInstance().modulesController.adRevenueCollectorsSourceIds
                val adNetworkSourceIds = listOf(yandexAdNetworkSourceId) + autoCollectorsSources
                return JSONArray(adNetworkSourceIds).toString()
            } catch (e: Throwable) {
                DebugLogger.error(tag, e)
            }
            return null
        }
}
