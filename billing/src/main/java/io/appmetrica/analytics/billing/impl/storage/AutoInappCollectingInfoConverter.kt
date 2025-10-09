package io.appmetrica.analytics.billing.impl.storage

import io.appmetrica.analytics.billing.impl.protobuf.client.AutoInappCollectingInfoProto
import io.appmetrica.analytics.coreapi.internal.data.ProtobufConverter

class AutoInappCollectingInfoConverter(
    private val billingInfoConverter: BillingInfoConverter = BillingInfoConverter()
) : ProtobufConverter<AutoInappCollectingInfo, AutoInappCollectingInfoProto.AutoInappCollectingInfo> {

    override fun fromModel(value: AutoInappCollectingInfo): AutoInappCollectingInfoProto.AutoInappCollectingInfo {
        return AutoInappCollectingInfoProto.AutoInappCollectingInfo().apply {
            entries = value.billingInfos.map { billingInfoConverter.fromModel(it) }.toTypedArray()
            firstInappCheckOccurred = value.firstInappCheckOccurred
        }
    }

    override fun toModel(value: AutoInappCollectingInfoProto.AutoInappCollectingInfo): AutoInappCollectingInfo {
        return AutoInappCollectingInfo(
            value.entries.map { billingInfoConverter.toModel(it) },
            value.firstInappCheckOccurred
        )
    }
}
