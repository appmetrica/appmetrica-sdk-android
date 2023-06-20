package io.appmetrica.analytics.billingv3.impl.storage;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import io.appmetrica.analytics.billinginterface.internal.BillingInfo;
import io.appmetrica.analytics.billinginterface.internal.storage.BillingInfoManager;
import io.appmetrica.analytics.billinginterface.internal.storage.BillingInfoStorage;
import io.appmetrica.analytics.coreutils.internal.logger.YLogger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class BillingInfoManagerImpl implements BillingInfoManager {

    private static final String TAG = "[BillingStorageImpl]";

    private boolean firstInappCheckOccurred;
    @NonNull
    private final BillingInfoStorage storage;
    @NonNull
    private final Map<String, BillingInfo> billingInfos;

    public BillingInfoManagerImpl(@NonNull final BillingInfoStorage billingInfoStorage) {
        billingInfos = new HashMap<>();
        for (final BillingInfo info: billingInfoStorage.getBillingInfo()) {
            billingInfos.put(info.sku, info);
        }
        this.firstInappCheckOccurred = billingInfoStorage.isFirstInappCheckOccurred();
        this.storage = billingInfoStorage;
    }

    @WorkerThread
    @Override
    public void update(@NonNull final Map<String, BillingInfo> history) {
        YLogger.info(TAG, "save");
        for (final BillingInfo billingInfo: history.values()) {
            billingInfos.put(billingInfo.sku, billingInfo);
            YLogger.info(TAG, "saving " + billingInfo.sku + " " + billingInfo);
        }
        storage.saveInfo(new ArrayList<>(billingInfos.values()), firstInappCheckOccurred);
    }

    @Nullable
    @Override
    public BillingInfo get(@NonNull String sku) {
        return billingInfos.get(sku);
    }

    @Override
    public void markFirstInappCheckOccurred() {
        if (!firstInappCheckOccurred) {
            firstInappCheckOccurred = true;
            storage.saveInfo(new ArrayList<>(billingInfos.values()), firstInappCheckOccurred);
        }
    }

    @Override
    public boolean isFirstInappCheckOccurred() {
        return firstInappCheckOccurred;
    }
}
