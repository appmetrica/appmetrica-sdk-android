package io.appmetrica.analytics.impl.adrevenue

import io.appmetrica.analytics.AdRevenue
import io.appmetrica.analytics.AdType
import io.appmetrica.analytics.modulesapi.internal.client.adrevenue.ModuleAdRevenue
import io.appmetrica.analytics.modulesapi.internal.client.adrevenue.ModuleAdType

internal class ModuleAdRevenueConverter {

    private val enricher = NativeLayerPayloadEnricher()

    fun convert(adRevenue: ModuleAdRevenue): AdRevenue {
        return AdRevenue.newBuilder(adRevenue.adRevenue, adRevenue.currency)
            .withAdType(convert(adRevenue.adType))
            .withAdNetwork(adRevenue.adNetwork)
            .withAdUnitId(adRevenue.adUnitId)
            .withAdUnitName(adRevenue.adUnitName)
            .withAdPlacementId(adRevenue.adPlacementId)
            .withAdPlacementName(adRevenue.adPlacementName)
            .withPrecision(adRevenue.precision)
            .withPayload(enrichPayload(adRevenue.payload))
            .build()
    }

    private fun enrichPayload(payload: Map<String, String>?): Map<String, String> {
        val mutatedPayload = payload?.toMutableMap() ?: mutableMapOf()
        return enricher.enrich(mutatedPayload)
    }

    private fun convert(adType: ModuleAdType?): AdType? {
        ModuleAdType.values()
        return when (adType) {
            null -> null
            ModuleAdType.NATIVE -> AdType.NATIVE
            ModuleAdType.BANNER -> AdType.BANNER
            ModuleAdType.REWARDED -> AdType.REWARDED
            ModuleAdType.INTERSTITIAL -> AdType.INTERSTITIAL
            ModuleAdType.MREC -> AdType.MREC
            ModuleAdType.APP_OPEN -> AdType.APP_OPEN
            ModuleAdType.OTHER -> AdType.OTHER
        }
    }
}
