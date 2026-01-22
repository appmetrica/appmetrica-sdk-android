package io.appmetrica.analytics.impl.attribution

import io.appmetrica.analytics.impl.protobuf.backend.ExternalAttribution.ClientExternalAttribution

internal class ExternalAttributionFromModule(
    source: Int,
    value: String?
) : BaseExternalAttribution(toProto(source, value))

private fun toProto(source: Int, value: String?) = ClientExternalAttribution().apply {
    this.attributionType = source
    this.value = value?.toByteArray() ?: this.value
}
