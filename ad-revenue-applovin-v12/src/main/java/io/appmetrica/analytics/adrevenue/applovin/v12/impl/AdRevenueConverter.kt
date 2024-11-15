package io.appmetrica.analytics.adrevenue.applovin.v12.impl

import com.applovin.mediation.MaxAd
import com.applovin.mediation.MaxAdFormat
import com.applovin.sdk.AppLovinSdk
import io.appmetrica.analytics.coreutils.internal.WrapUtils
import io.appmetrica.analytics.modulesapi.internal.client.adrevenue.ModuleAdRevenue
import io.appmetrica.analytics.modulesapi.internal.client.adrevenue.ModuleAdType
import java.math.BigDecimal
import java.util.Currency

class AdRevenueConverter {

    fun convert(maxAd: MaxAd, appLovinSdk: AppLovinSdk): ModuleAdRevenue {
        val adType = convert(maxAd.format)
        val payload = mutableMapOf(
            "countryCode" to appLovinSdk.configuration.countryCode,
        )
        if (adType == ModuleAdType.OTHER) {
            payload["adType"] = maxAd.format.label
        }

        return ModuleAdRevenue(
            adRevenue = BigDecimal.valueOf(WrapUtils.getFiniteDoubleOrDefault(maxAd.revenue, 0.0)),
            currency = Currency.getInstance("USD"),
            adType = adType,
            adNetwork = maxAd.networkName,
            adUnitId = maxAd.adUnitId,
            adPlacementName = maxAd.placement,
            adPlacementId = maxAd.networkPlacement,
            precision = maxAd.revenuePrecision,
            payload = payload,
            autoCollected = true
        )
    }

    private fun convert(type: MaxAdFormat?): ModuleAdType? {
        return when (type) {
            null -> null
            MaxAdFormat.NATIVE -> ModuleAdType.NATIVE
            MaxAdFormat.BANNER -> ModuleAdType.BANNER
            MaxAdFormat.REWARDED -> ModuleAdType.REWARDED
            MaxAdFormat.INTERSTITIAL -> ModuleAdType.INTERSTITIAL
            MaxAdFormat.MREC -> ModuleAdType.MREC
            else -> ModuleAdType.OTHER
        }
    }
}
