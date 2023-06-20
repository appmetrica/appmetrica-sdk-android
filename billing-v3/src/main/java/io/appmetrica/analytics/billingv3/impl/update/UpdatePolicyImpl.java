package io.appmetrica.analytics.billingv3.impl.update;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.billinginterface.internal.BillingInfo;
import io.appmetrica.analytics.billinginterface.internal.ProductType;
import io.appmetrica.analytics.billinginterface.internal.config.BillingConfig;
import io.appmetrica.analytics.billinginterface.internal.storage.BillingInfoManager;
import io.appmetrica.analytics.billinginterface.internal.update.UpdatePolicy;
import io.appmetrica.analytics.coreutils.internal.logger.YLogger;
import io.appmetrica.analytics.coreutils.internal.time.SystemTimeProvider;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class UpdatePolicyImpl implements UpdatePolicy {

    private static final String TAG = "[UpdatePolicyImpl]";

    @NonNull
    private final SystemTimeProvider systemTimeProvider;

    public UpdatePolicyImpl() {
        this(new SystemTimeProvider());
    }

    UpdatePolicyImpl(@NonNull final SystemTimeProvider systemTimeProvider) {
        this.systemTimeProvider = systemTimeProvider;
    }

    @NonNull
    @Override
    public Map<String, BillingInfo> getBillingInfoToUpdate(@NonNull final BillingConfig config,
                                                           @NonNull final Map<String, BillingInfo> history,
                                                           @NonNull final BillingInfoManager storage) {
        YLogger.info(TAG, "getNewBillingInfo");
        final Map<String, BillingInfo> newBillingInfo = new HashMap<>();
        for (final String sku: history.keySet()) {
            final BillingInfo historyEntry = history.get(sku);
            if (shouldUpdateBillingInfo(config, historyEntry, storage)) {
                YLogger.info(TAG, "Product %s should be updated", historyEntry.sku);
                newBillingInfo.put(sku, historyEntry);
            } else {
                YLogger.info(TAG, "Product %s should be ignored", historyEntry.sku);
            }
        }
        return newBillingInfo;
    }

    private boolean shouldUpdateBillingInfo(@NonNull final BillingConfig config,
                                            @NonNull final BillingInfo historyEntry,
                                            @NonNull final BillingInfoManager storage) {
        final long now = systemTimeProvider.currentTimeMillis();
        YLogger.info(TAG, "isFirstInappCheckOccurred " + storage.isFirstInappCheckOccurred());
        if (historyEntry.type == ProductType.INAPP && !storage.isFirstInappCheckOccurred()) {
            return now - historyEntry.purchaseTime <=
                    TimeUnit.SECONDS.toMillis(config.firstCollectingInappMaxAgeSeconds);
        }
        final BillingInfo storageEntry = storage.get(historyEntry.sku);
        if (storageEntry == null) {
            return true;
        }
        if (!storageEntry.purchaseToken.equals(historyEntry.purchaseToken)) {
            return true;
        }
        if (historyEntry.type == ProductType.SUBS) {
            return now - storageEntry.sendTime >= TimeUnit.SECONDS.toMillis(config.sendFrequencySeconds);
        }
        return false;
    }
}
