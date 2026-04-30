package io.appmetrica.analytics.adrevenue.other.impl.fb

import android.os.Bundle
import io.appmetrica.analytics.adrevenue.other.impl.BundleToJsonConverter
import io.appmetrica.analytics.adrevenue.other.impl.Constants
import io.appmetrica.analytics.modulesapi.internal.client.adrevenue.AdRevenueConstants
import io.appmetrica.analytics.modulesapi.internal.client.adrevenue.ModuleAdRevenue
import java.math.BigDecimal
import java.util.Currency

internal class FBAdRevenueConverter {

    fun convert(bundle: Bundle): ModuleAdRevenue {
        return ModuleAdRevenue(
            adRevenue = BigDecimal.ZERO,
            currency = Currency.getInstance("USD"),
            adType = null,
            adNetwork = FBConstants.AD_NETWORK_NAME,
            adUnitId = null,
            adUnitName = null,
            adPlacementId = null,
            adPlacementName = null,
            precision = null,
            payload = hashMapOf(
                AdRevenueConstants.SOURCE_KEY to FBConstants.AD_NETWORK_NAME,
                AdRevenueConstants.ORIGINAL_SOURCE_KEY to Constants.MODULE_ID,
                AdRevenueConstants.ORIGINAL_AD_TYPE_KEY to "null",
                "raw_payload" to BundleToJsonConverter.convert(bundle),
            ),
            autoCollected = true,
        )
    }
}
