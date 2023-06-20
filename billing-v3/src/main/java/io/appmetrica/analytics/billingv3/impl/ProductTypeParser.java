package io.appmetrica.analytics.billingv3.impl;

import androidx.annotation.NonNull;
import com.android.billingclient.api.BillingClient;
import io.appmetrica.analytics.billinginterface.internal.ProductType;

public class ProductTypeParser {

    @NonNull
    public static ProductType parse(@NonNull final String type) {
        if (BillingClient.SkuType.INAPP.equals(type)) {
            return ProductType.INAPP;
        }
        if (BillingClient.SkuType.SUBS.equals(type)) {
            return ProductType.SUBS;
        }
        return ProductType.UNKNOWN;
    }
}
