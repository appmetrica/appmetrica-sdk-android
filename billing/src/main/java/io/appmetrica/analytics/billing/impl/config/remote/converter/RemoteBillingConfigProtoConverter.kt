package io.appmetrica.analytics.billing.impl.config.remote.converter

import io.appmetrica.analytics.billing.impl.RemoteBillingConfigProto
import io.appmetrica.analytics.billing.internal.config.RemoteBillingConfig
import io.appmetrica.analytics.coreapi.internal.data.Converter

internal class RemoteBillingConfigProtoConverter(
    private val converter: BillingConfigProtoConverter = BillingConfigProtoConverter(),
) : Converter<RemoteBillingConfig, RemoteBillingConfigProto> {

    override fun fromModel(value: RemoteBillingConfig): RemoteBillingConfigProto {
        return RemoteBillingConfigProto().also { proto ->
            proto.enabled = value.enabled
            proto.config = value.config?.let { converter.fromModel(it) }
        }
    }

    override fun toModel(value: RemoteBillingConfigProto): RemoteBillingConfig {
        return RemoteBillingConfig(
            enabled = value.enabled,
            config = converter.toModel(value.config),
        )
    }
}
