package io.appmetrica.analytics.impl.telephony

class SimInfo(
    val simCountryCode: Int?,
    val simNetworkCode: Int?,
    val isNetworkRoaming: Boolean,
    val operatorName: String?,
)
