package io.appmetrica.analytics.impl.adrevenue

import io.appmetrica.analytics.AdRevenue
import io.appmetrica.analytics.AdType
import io.appmetrica.analytics.modulesapi.internal.client.adrevenue.ModuleAdRevenue
import io.appmetrica.analytics.modulesapi.internal.client.adrevenue.ModuleAdType

class ModuleAdRevenueConverter {

    fun convert(adRevenue: ModuleAdRevenue): AdRevenue {
        return AdRevenue.newBuilder(adRevenue.adRevenue, adRevenue.currency)
            .withAdType(convert(adRevenue.adType))
            .withAdNetwork(adRevenue.adNetwork)
            .withAdUnitId(adRevenue.adUnitId)
            .withAdUnitName(adRevenue.adUnitName)
            .withAdPlacementId(adRevenue.adPlacementId)
            .withAdPlacementName(adRevenue.adPlacementName)
            .withPrecision(adRevenue.precision)
            .withPayload(adRevenue.payload)
            .build()
    }

    fun convert(adType: ModuleAdType?): AdType? {
        return when (adType) {
            null -> null
            ModuleAdType.NATIVE -> AdType.NATIVE
            ModuleAdType.BANNER -> AdType.BANNER
            ModuleAdType.REWARDED -> AdType.REWARDED
            ModuleAdType.INTERSTITIAL -> AdType.INTERSTITIAL
            ModuleAdType.MREC -> AdType.MREC
            ModuleAdType.OTHER -> AdType.OTHER
        }
    }
}
