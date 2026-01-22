package io.appmetrica.analytics.impl.attribution

import io.appmetrica.analytics.impl.protobuf.backend.ExternalAttribution.ClientExternalAttribution
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger
import org.json.JSONObject

internal class ObjectExternalAttribution(
    provider: ExternalAttributionType,
    value: Any
) : BaseExternalAttribution(
    toProto(provider, value)
) {

    companion object {
        private const val TAG = "[ObjectExternalAttribution]"

        private fun toProto(
            provider: ExternalAttributionType,
            value: Any
        ) = ClientExternalAttribution().also {
            it.attributionType = ExternalAttributionTypeConverter.fromModel(provider)
            it.value = value.toJsonObject().toString().toByteArray()
        }

        private fun Any.toJsonObject() = JSONObject().also { jsonObject ->
            javaClass.fields.forEach {
                try {
                    jsonObject.put(it.name, it.get(this))
                } catch (e: Throwable) {
                    DebugLogger.error(TAG, e, "Found field `${it.name}` with illegal value for JSONObject")
                }
            }
        }
    }
}
