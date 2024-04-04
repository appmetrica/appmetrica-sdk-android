package io.appmetrica.analytics.modulesapi.internal.client.adrevenue

import java.math.BigDecimal
import java.util.Currency

class AutoAdRevenue(
    val adRevenue: BigDecimal,
    val currency: Currency,
    val adType: AutoAdType? = null,
    val adNetwork: String? = null,
    val adUnitId: String? = null,
    val adUnitName: String? = null,
    val adPlacementId: String? = null,
    val adPlacementName: String? = null,
    val precision: String? = null,
    val payload: Map<String, String>? = null,
)
