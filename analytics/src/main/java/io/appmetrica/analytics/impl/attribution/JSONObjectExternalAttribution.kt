package io.appmetrica.analytics.impl.attribution

import io.appmetrica.analytics.impl.protobuf.backend.ExternalAttribution.ClientExternalAttribution
import org.json.JSONObject

class JSONObjectExternalAttribution(
    provider: ExternalAttributionType,
    value: JSONObject
) : BaseExternalAttribution(
    toProto(provider, value)
) {

    companion object {
        private fun toProto(
            provider: ExternalAttributionType,
            value: JSONObject
        ) = ClientExternalAttribution().also {
            it.attributionType = ExternalAttributionTypeConverter.fromModel(provider)
            it.value = value.toString().toByteArray()
        }
    }
}
