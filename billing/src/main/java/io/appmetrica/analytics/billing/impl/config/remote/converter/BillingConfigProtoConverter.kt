package io.appmetrica.analytics.billing.impl.config.remote.converter

import io.appmetrica.analytics.billing.impl.BillingConfigProto
import io.appmetrica.analytics.billing.internal.config.BillingConfig
import io.appmetrica.analytics.coreapi.internal.data.Converter

class BillingConfigProtoConverter : Converter<BillingConfig, BillingConfigProto> {

    override fun fromModel(value: BillingConfig): BillingConfigProto {
        return BillingConfigProto().also { proto ->
            proto.sendFrequencySeconds = value.sendFrequencySeconds
            proto.firstCollectingInappMaxAgeSeconds = value.firstCollectingInappMaxAgeSeconds
        }
    }

    override fun toModel(value: BillingConfigProto): BillingConfig {
        return BillingConfig(
            sendFrequencySeconds = value.sendFrequencySeconds,
            firstCollectingInappMaxAgeSeconds = value.firstCollectingInappMaxAgeSeconds,
        )
    }
}
