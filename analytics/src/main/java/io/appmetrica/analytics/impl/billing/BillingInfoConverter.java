package io.appmetrica.analytics.impl.billing;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.billinginterface.internal.BillingInfo;
import io.appmetrica.analytics.billinginterface.internal.ProductType;
import io.appmetrica.analytics.coreapi.internal.data.ProtobufConverter;
import io.appmetrica.analytics.impl.protobuf.client.AutoInappCollectingInfoProto;

class BillingInfoConverter implements
        ProtobufConverter<BillingInfo, AutoInappCollectingInfoProto.AutoInappCollectingInfo.BillingInfo> {

    @NonNull
    @Override
    public AutoInappCollectingInfoProto.AutoInappCollectingInfo.BillingInfo fromModel(@NonNull BillingInfo value) {
        final AutoInappCollectingInfoProto.AutoInappCollectingInfo.BillingInfo nano =
                new AutoInappCollectingInfoProto.AutoInappCollectingInfo.BillingInfo();
        nano.type = toInappType(value.type);
        nano.sku = value.sku;
        nano.purchaseToken = value.purchaseToken;
        nano.purchaseTime = value.purchaseTime;
        nano.sendTime = value.sendTime;
        return nano;
    }

    @NonNull
    @Override
    public BillingInfo toModel(@NonNull AutoInappCollectingInfoProto.AutoInappCollectingInfo.BillingInfo nano) {
        return new BillingInfo(
                toProductType(nano.type),
                nano.sku,
                nano.purchaseToken,
                nano.purchaseTime,
                nano.sendTime
        );
    }

    private int toInappType(@NonNull final ProductType type) {
        switch (type) {
            case INAPP: return AutoInappCollectingInfoProto.AutoInappCollectingInfo.PURCHASE;
            case SUBS: return AutoInappCollectingInfoProto.AutoInappCollectingInfo.SUBSCRIPTION;
            default: return AutoInappCollectingInfoProto.AutoInappCollectingInfo.UNKNOWN;
        }
    }

    @NonNull
    private ProductType toProductType(final int type) {
        switch (type) {
            case AutoInappCollectingInfoProto.AutoInappCollectingInfo.PURCHASE: return ProductType.INAPP;
            case AutoInappCollectingInfoProto.AutoInappCollectingInfo.SUBSCRIPTION: return ProductType.SUBS;
            default: return ProductType.UNKNOWN;
        }
    }
}
