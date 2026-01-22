package io.appmetrica.analytics.impl.adrevenue

internal interface AdRevenuePayloadEnricher {

    fun enrich(payload: MutableMap<String, String>): MutableMap<String, String>
}
