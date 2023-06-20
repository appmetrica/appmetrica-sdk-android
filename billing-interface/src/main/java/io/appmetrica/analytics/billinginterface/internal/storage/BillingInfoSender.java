package io.appmetrica.analytics.billinginterface.internal.storage;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.billinginterface.internal.ProductInfo;
import java.util.List;

public interface BillingInfoSender {

    void sendInfo(@NonNull final List<ProductInfo> productInfos);
}
