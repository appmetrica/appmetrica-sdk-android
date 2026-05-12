package io.appmetrica.analytics.billing.impl.config.service.converter

import io.appmetrica.analytics.billing.impl.BillingConfigProto
import io.appmetrica.analytics.billing.impl.config.service.model.ServiceSideBillingConfig
import io.appmetrica.analytics.coreapi.internal.data.Converter

internal class BillingConfigProtoConverter : Converter<ServiceSideBillingConfig, BillingConfigProto> {

    override fun fromModel(value: ServiceSideBillingConfig): BillingConfigProto {
        return BillingConfigProto().also { proto ->
            proto.sendFrequencySeconds = value.sendFrequencySeconds
            proto.firstCollectingInappMaxAgeSeconds = value.firstCollectingInappMaxAgeSeconds
        }
    }

    override fun toModel(value: BillingConfigProto): ServiceSideBillingConfig {
        return ServiceSideBillingConfig(
            sendFrequencySeconds = value.sendFrequencySeconds,
            firstCollectingInappMaxAgeSeconds = value.firstCollectingInappMaxAgeSeconds,
        )
    }
}
