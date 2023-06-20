package io.appmetrica.analytics.billinginterface.internal.storage;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.billinginterface.internal.BillingInfo;
import java.util.Map;

public interface BillingInfoManager {

    void update(@NonNull final Map<String, BillingInfo> skuDetails);

    @Nullable
    BillingInfo get(@NonNull final String sku);

    void markFirstInappCheckOccurred();

    boolean isFirstInappCheckOccurred();
}
