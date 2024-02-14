package io.appmetrica.analytics.impl.billing;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.billinginterface.internal.BillingInfo;
import io.appmetrica.analytics.billinginterface.internal.storage.BillingInfoStorage;
import io.appmetrica.analytics.coreapi.internal.data.ProtobufStateStorage;
import io.appmetrica.analytics.impl.db.state.factory.StorageFactory;
import io.appmetrica.analytics.logger.internal.YLogger;
import java.util.List;

public class BillingInfoStorageImpl implements BillingInfoStorage {

    private static final String TAG = "[BillingInfoSaverImpl]";

    @NonNull
    private final ProtobufStateStorage<AutoInappCollectingInfo> storage;
    @NonNull
    private AutoInappCollectingInfo autoInappCollectingInfo;

    public BillingInfoStorageImpl(@NonNull final Context context) {
        this(StorageFactory.Provider.get(AutoInappCollectingInfo.class).create(context));
    }

    @VisibleForTesting
    BillingInfoStorageImpl(@NonNull ProtobufStateStorage<AutoInappCollectingInfo> storage) {
        this.storage = storage;
        this.autoInappCollectingInfo = storage.read();
    }

    @Override
    public void saveInfo(@NonNull final List<BillingInfo> billingInfos,
                         final boolean firstInappCheckOccurred) {
        YLogger.info(TAG, "saveInfo");
        for (final BillingInfo info: billingInfos) {
            YLogger.info(TAG, info.toString());
        }
        autoInappCollectingInfo = new AutoInappCollectingInfo(billingInfos, firstInappCheckOccurred);
        storage.save(autoInappCollectingInfo);
    }

    @NonNull
    @Override
    public List<BillingInfo> getBillingInfo() {
        YLogger.info(TAG, "loadInfo");
        return autoInappCollectingInfo.billingInfos;
    }

    @Override
    public boolean isFirstInappCheckOccurred() {
        return autoInappCollectingInfo.firstInappCheckOccurred;
    }
}
