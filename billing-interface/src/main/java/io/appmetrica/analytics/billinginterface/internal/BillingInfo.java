package io.appmetrica.analytics.billinginterface.internal;

import androidx.annotation.NonNull;

public class BillingInfo {

    @NonNull
    public final ProductType type;
    @NonNull
    public final String productId;
    @NonNull
    public final String purchaseToken;
    public final long purchaseTime;
    public long sendTime;

    public BillingInfo(@NonNull final ProductType type,
                       @NonNull final String productId,
                       @NonNull final String purchaseToken,
                       final long purchaseTime,
                       final long sendTime) {
        this.type = type;
        this.productId = productId;
        this.purchaseToken = purchaseToken;
        this.purchaseTime = purchaseTime;
        this.sendTime = sendTime;
    }

    @Override
    @NonNull
    public String toString() {
        return "BillingInfo{" +
                "type=" + type +
                "productId='" + productId + "'" +
                "purchaseToken='" + purchaseToken + "'" +
                "purchaseTime=" + purchaseTime +
                "sendTime=" + sendTime +
                "}";
    }
}
