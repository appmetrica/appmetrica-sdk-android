package io.appmetrica.analytics.impl.adrevenue

interface AdRevenuePayloadEnricher {

    fun enrich(payload: MutableMap<String, String>): MutableMap<String, String>
}
