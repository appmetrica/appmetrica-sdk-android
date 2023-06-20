package io.appmetrica.analytics.impl.billing;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.billinginterface.internal.BillingInfo;
import java.util.List;

public class AutoInappCollectingInfo {

    @NonNull
    public final List<BillingInfo> billingInfos;
    public final boolean firstInappCheckOccurred;

    public AutoInappCollectingInfo(@NonNull final List<BillingInfo> billingInfos,
                                   final boolean firstInappCheckOccurred) {
        this.billingInfos = billingInfos;
        this.firstInappCheckOccurred = firstInappCheckOccurred;
    }
}
