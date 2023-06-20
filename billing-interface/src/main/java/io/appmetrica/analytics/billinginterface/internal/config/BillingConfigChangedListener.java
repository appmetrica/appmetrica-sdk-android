package io.appmetrica.analytics.billinginterface.internal.config;

import androidx.annotation.Nullable;

public interface BillingConfigChangedListener {

    void onBillingConfigChanged(@Nullable BillingConfig billingConfig);
}
