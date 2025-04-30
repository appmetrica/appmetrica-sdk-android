package io.appmetrica.analytics.adrevenue.applovin.v12.impl

import com.applovin.mediation.MaxAd
import com.applovin.mediation.MaxAdFormat
import com.applovin.sdk.AppLovinSdk
import io.appmetrica.analytics.coreutils.internal.WrapUtils
import io.appmetrica.analytics.modulesapi.internal.client.adrevenue.AdRevenueConstants
import io.appmetrica.analytics.modulesapi.internal.client.adrevenue.ModuleAdRevenue
import io.appmetrica.analytics.modulesapi.internal.client.adrevenue.ModuleAdType
import java.math.BigDecimal
import java.util.Currency

class AdRevenueConverter {

    fun convert(maxAd: MaxAd, appLovinSdk: AppLovinSdk): ModuleAdRevenue {
        val adType = convert(maxAd.format)
        val payload = mapOf(
            "countryCode" to appLovinSdk.configuration.countryCode,
            AdRevenueConstants.ORIGINAL_SOURCE_KEY to Constants.MODULE_ID,
            AdRevenueConstants.ORIGINAL_AD_TYPE_KEY to (maxAd.format?.label ?: "null"),
            AdRevenueConstants.SOURCE_KEY to Constants.AD_REVENUE_SOURCE_IDENTIFIER
        )

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
            autoCollected = false
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
