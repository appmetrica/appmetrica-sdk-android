package io.appmetrica.analytics.impl.adrevenue

class YandexSourcePayloadEnricher : AdRevenuePayloadEnricher {
    override fun enrich(payload: MutableMap<String, String>): MutableMap<String, String> {
        payload["source"] = "yandex"
        return payload
    }
}
