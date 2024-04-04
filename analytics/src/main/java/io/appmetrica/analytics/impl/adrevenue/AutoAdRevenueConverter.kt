package io.appmetrica.analytics.impl.adrevenue

import io.appmetrica.analytics.AdRevenue
import io.appmetrica.analytics.AdType
import io.appmetrica.analytics.modulesapi.internal.client.adrevenue.AutoAdRevenue
import io.appmetrica.analytics.modulesapi.internal.client.adrevenue.AutoAdType

class AutoAdRevenueConverter {

    fun convert(autoAdRevenue: AutoAdRevenue): AdRevenue {
        return AdRevenue.newBuilder(autoAdRevenue.adRevenue, autoAdRevenue.currency)
            .withAdType(convert(autoAdRevenue.adType))
            .withAdNetwork(autoAdRevenue.adNetwork)
            .withAdUnitId(autoAdRevenue.adUnitId)
            .withAdUnitName(autoAdRevenue.adUnitName)
            .withAdPlacementId(autoAdRevenue.adPlacementId)
            .withAdPlacementName(autoAdRevenue.adPlacementName)
            .withPrecision(autoAdRevenue.precision)
            .withPayload(autoAdRevenue.payload)
            .build()
    }

    fun convert(autoAdType: AutoAdType?): AdType? {
        return when (autoAdType) {
            null -> null
            AutoAdType.NATIVE -> AdType.NATIVE
            AutoAdType.BANNER -> AdType.BANNER
            AutoAdType.REWARDED -> AdType.REWARDED
            AutoAdType.INTERSTITIAL -> AdType.INTERSTITIAL
            AutoAdType.MREC -> AdType.MREC
            AutoAdType.OTHER -> AdType.OTHER
        }
    }
}
