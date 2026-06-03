package io.appmetrica.analytics.adrevenue.applovin.v12.auto.impl

import android.os.Bundle
import io.appmetrica.analytics.modulesapi.internal.client.adrevenue.AdRevenueConstants
import io.appmetrica.analytics.modulesapi.internal.client.adrevenue.ModuleAdRevenue
import io.appmetrica.analytics.modulesapi.internal.client.adrevenue.ModuleAdType
import java.math.BigDecimal
import java.util.Currency

internal class AppLovinIlrdConverter {

    fun convert(data: Bundle): ModuleAdRevenue {
        val rawAdFormat = data.getString("ad_format")
        val (revenue, originalAdRevenue) = extractRevenue(data)

        val payload = mutableMapOf(
            AdRevenueConstants.SOURCE_KEY to Constants.AD_REVENUE_SOURCE_IDENTIFIER,
            AdRevenueConstants.ORIGINAL_SOURCE_KEY to Constants.MODULE_ID,
            AdRevenueConstants.ORIGINAL_AD_TYPE_KEY to (rawAdFormat ?: "null"),
        )
        if (originalAdRevenue != null) {
            payload[Constants.Payload.ORIGINAL_AD_REVENUE_KEY] = originalAdRevenue
        }

        return ModuleAdRevenue(
            adRevenue = BigDecimal.valueOf(revenue),
            currency = Currency.getInstance("USD"),
            adType = convertAdType(rawAdFormat),
            adNetwork = data.getString("network_name"),
            adUnitId = data.getString("max_ad_unit_id"),
            adUnitName = null,
            adPlacementId = data.getString("third_party_ad_placement_id"),
            adPlacementName = null,
            precision = data.getString("precision"),
            payload = payload,
            autoCollected = true,
        )
    }

    private fun extractRevenue(data: Bundle): Pair<Double, String?> {
        if (!data.containsKey("revenue")) {
            return 0.0 to Constants.Payload.ORIGINAL_AD_REVENUE_NO_VALUE
        }
        val raw = data.getDouble("revenue")
        if (!raw.isFinite() || raw < 0) {
            return 0.0 to raw.toString()
        }
        return raw to null
    }

    private fun convertAdType(adFormat: String?): ModuleAdType? = when (adFormat) {
        "BANNER" -> ModuleAdType.BANNER
        "MREC" -> ModuleAdType.MREC
        "NATIVE" -> ModuleAdType.NATIVE
        "INTER" -> ModuleAdType.INTERSTITIAL
        // AppLovin MAX 13.x sends "REWARDED"; older SDK versions may send "REWARD"
        "REWARDED", "REWARD" -> ModuleAdType.REWARDED
        "APPOPEN" -> ModuleAdType.APP_OPEN
        null -> null
        else -> ModuleAdType.OTHER
    }
}
