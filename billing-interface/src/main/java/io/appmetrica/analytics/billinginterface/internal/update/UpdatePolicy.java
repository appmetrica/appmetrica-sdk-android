package io.appmetrica.analytics.billinginterface.internal.update;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.billinginterface.internal.BillingInfo;
import io.appmetrica.analytics.billinginterface.internal.config.BillingConfig;
import io.appmetrica.analytics.billinginterface.internal.storage.BillingInfoManager;
import java.util.Map;

public interface UpdatePolicy {

    @NonNull
    Map<String, BillingInfo> getBillingInfoToUpdate(@NonNull final BillingConfig config,
                                                    @NonNull final Map<String, BillingInfo> history,
                                                    @NonNull final BillingInfoManager storage);
}
