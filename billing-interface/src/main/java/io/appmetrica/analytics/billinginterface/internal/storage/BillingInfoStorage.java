package io.appmetrica.analytics.billinginterface.internal.storage;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.billinginterface.internal.BillingInfo;
import java.util.List;

public interface BillingInfoStorage {

    void saveInfo(@NonNull final List<BillingInfo> billingInfos,
                  final boolean firstInappCheckOccurred);

    @NonNull
    List<BillingInfo> getBillingInfo();

    boolean isFirstInappCheckOccurred();
}
