package io.appmetrica.analytics.impl.attribution

import io.appmetrica.analytics.impl.protobuf.backend.ExternalAttribution.ClientExternalAttribution
import io.appmetrica.analytics.impl.utils.JsonHelper

class MapExternalAttribution(
    provider: ExternalAttributionType,
    value: Map<String, Any?>
) : BaseExternalAttribution(
    toProto(provider, value)
) {

    companion object {
        private fun toProto(
            provider: ExternalAttributionType,
            value: Map<String, Any?>
        ) = ClientExternalAttribution().also {
            it.attributionType = ExternalAttributionTypeConverter.fromModel(provider)
            JsonHelper.mapToJson(value)?.toString()?.let { value ->
                it.value = value.toByteArray()
            }
        }
    }
}
