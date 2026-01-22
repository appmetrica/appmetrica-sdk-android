package io.appmetrica.analytics.impl.attribution

import io.appmetrica.analytics.ExternalAttribution
import io.appmetrica.analytics.impl.protobuf.backend.ExternalAttribution.ClientExternalAttribution
import io.appmetrica.analytics.protobuf.nano.MessageNano

internal open class BaseExternalAttribution(
    private val proto: ClientExternalAttribution
) : ExternalAttribution {

    override fun toBytes(): ByteArray {
        return MessageNano.toByteArray(proto)
    }

    override fun toString(): String {
        return "ExternalAttribution(" +
            "type=`${ExternalAttributionTypeConverter.toString(proto.attributionType)}`" +
            "value=`${String(proto.value)}`" +
            ")"
    }
}
