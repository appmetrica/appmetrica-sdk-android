package io.appmetrica.analytics.impl.attribution

import io.appmetrica.analytics.impl.protobuf.backend.ExternalAttribution.ClientExternalAttribution
import org.json.JSONObject

class ObjectExternalAttribution(
    provider: ExternalAttributionType,
    value: Any
) : BaseExternalAttribution(
    toProto(provider, value)
) {

    companion object {
        private fun toProto(
            provider: ExternalAttributionType,
            value: Any
        ) = ClientExternalAttribution().also {
            it.attributionType = ExternalAttributionTypeConverter.fromModel(provider)
            it.value = value.toJsonObject().toString().toByteArray()
        }

        private fun Any.toJsonObject() = JSONObject().also { jsonObject ->
            javaClass.fields.forEach {
                jsonObject.put(it.name, it.get(this))
            }
        }
    }
}
