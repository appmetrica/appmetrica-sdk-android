package io.appmetrica.analytics.modulesapi.internal.client.adrevenue

import java.math.BigDecimal
import java.util.Currency

class ModuleAdRevenue(
    val adRevenue: BigDecimal,
    val currency: Currency,
    val adType: ModuleAdType? = null,
    val adNetwork: String? = null,
    val adUnitId: String? = null,
    val adUnitName: String? = null,
    val adPlacementId: String? = null,
    val adPlacementName: String? = null,
    val precision: String? = null,
    val payload: Map<String, String>? = null,
    val autoCollected: Boolean = true,
)
