package io.appmetrica.analytics.impl.adrevenue

internal class YandexSourcePayloadEnricher : AdRevenuePayloadEnricher {
    override fun enrich(payload: MutableMap<String, String>): MutableMap<String, String> {
        payload["source"] = "yandex"
        return payload
    }
}
