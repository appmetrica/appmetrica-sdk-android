package io.appmetrica.analytics.billing.impl.config.service.converter

import io.appmetrica.analytics.billing.impl.RemoteBillingConfigProto
import io.appmetrica.analytics.billing.impl.config.service.model.ServiceSideRemoteBillingConfig
import io.appmetrica.analytics.coreapi.internal.data.Converter

internal class RemoteBillingConfigProtoConverter(
    private val converter: BillingConfigProtoConverter = BillingConfigProtoConverter(),
) : Converter<ServiceSideRemoteBillingConfig, RemoteBillingConfigProto> {

    override fun fromModel(value: ServiceSideRemoteBillingConfig): RemoteBillingConfigProto {
        return RemoteBillingConfigProto().also { proto ->
            proto.enabled = value.enabled
            proto.config = value.config?.let { converter.fromModel(it) }
        }
    }

    override fun toModel(value: RemoteBillingConfigProto): ServiceSideRemoteBillingConfig {
        return ServiceSideRemoteBillingConfig(
            enabled = value.enabled,
            config = converter.toModel(value.config),
        )
    }
}
