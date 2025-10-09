package io.appmetrica.analytics.billing.impl.storage

import io.appmetrica.analytics.billing.impl.protobuf.client.AutoInappCollectingInfoProto
import io.appmetrica.analytics.billinginterface.internal.BillingInfo
import io.appmetrica.analytics.billinginterface.internal.ProductType
import io.appmetrica.analytics.coreapi.internal.data.ProtobufConverter

class BillingInfoConverter :
    ProtobufConverter<BillingInfo, AutoInappCollectingInfoProto.AutoInappCollectingInfo.BillingInfo> {

    override fun fromModel(value: BillingInfo): AutoInappCollectingInfoProto.AutoInappCollectingInfo.BillingInfo {
        val nano =
            AutoInappCollectingInfoProto.AutoInappCollectingInfo.BillingInfo()
        nano.type = toInappType(value.type)
        nano.sku = value.productId
        nano.purchaseToken = value.purchaseToken
        nano.purchaseTime = value.purchaseTime
        nano.sendTime = value.sendTime
        return nano
    }

    override fun toModel(nano: AutoInappCollectingInfoProto.AutoInappCollectingInfo.BillingInfo): BillingInfo {
        return BillingInfo(
            toProductType(nano.type),
            nano.sku,
            nano.purchaseToken,
            nano.purchaseTime,
            nano.sendTime
        )
    }

    private fun toInappType(type: ProductType): Int {
        return when (type) {
            ProductType.INAPP -> AutoInappCollectingInfoProto.AutoInappCollectingInfo.PURCHASE
            ProductType.SUBS -> AutoInappCollectingInfoProto.AutoInappCollectingInfo.SUBSCRIPTION
            else -> AutoInappCollectingInfoProto.AutoInappCollectingInfo.UNKNOWN
        }
    }

    private fun toProductType(type: Int): ProductType {
        return when (type) {
            AutoInappCollectingInfoProto.AutoInappCollectingInfo.PURCHASE -> ProductType.INAPP
            AutoInappCollectingInfoProto.AutoInappCollectingInfo.SUBSCRIPTION -> ProductType.SUBS
            else -> ProductType.UNKNOWN
        }
    }
}
