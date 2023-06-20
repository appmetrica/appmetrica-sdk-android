package io.appmetrica.analytics.billingv3.impl;

import androidx.annotation.NonNull;
import com.android.billingclient.api.BillingResult;

public class BillingUtils {

    public static String toString(@NonNull final BillingResult result) {
        return result.getResponseCode() + " : " + result.getDebugMessage();
    }
}
