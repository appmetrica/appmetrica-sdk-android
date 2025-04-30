package io.appmetrica.analytics.impl.adrevenue

import io.appmetrica.analytics.modulesapi.internal.client.adrevenue.AdRevenueConstants

class NativeLayerPayloadEnricher : AdRevenuePayloadEnricher {

    override fun enrich(payload: MutableMap<String, String>): MutableMap<String, String> {
        payload[AdRevenueConstants.LAYER_KEY] = AdRevenueConstants.NATIVE_LAYER
        return payload
    }
}
