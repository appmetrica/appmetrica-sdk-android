package io.appmetrica.analytics.impl.attribution

import io.appmetrica.analytics.impl.protobuf.backend.ExternalAttribution.ClientExternalAttribution

internal class NullExternalAttribution(
    provider: ExternalAttributionType
) : BaseExternalAttribution(
    toProto(provider)
) {

    companion object {
        private fun toProto(
            provider: ExternalAttributionType,
        ) = ClientExternalAttribution().also {
            it.attributionType = ExternalAttributionTypeConverter.fromModel(provider)
        }
    }
}
