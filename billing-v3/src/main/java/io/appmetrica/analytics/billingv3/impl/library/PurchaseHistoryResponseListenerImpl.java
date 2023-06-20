package io.appmetrica.analytics.billingv3.impl.library;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import androidx.annotation.VisibleForTesting;
import androidx.annotation.WorkerThread;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.PurchaseHistoryRecord;
import com.android.billingclient.api.PurchaseHistoryResponseListener;
import com.android.billingclient.api.SkuDetailsParams;
import io.appmetrica.analytics.billinginterface.internal.BillingInfo;
import io.appmetrica.analytics.billinginterface.internal.config.BillingConfig;
import io.appmetrica.analytics.billinginterface.internal.library.UtilsProvider;
import io.appmetrica.analytics.billinginterface.internal.storage.BillingInfoManager;
import io.appmetrica.analytics.billingv3.impl.BillingUtils;
import io.appmetrica.analytics.billingv3.impl.ProductTypeParser;
import io.appmetrica.analytics.coreutils.internal.executors.SafeRunnable;
import io.appmetrica.analytics.coreutils.internal.logger.YLogger;
import io.appmetrica.analytics.coreutils.internal.time.SystemTimeProvider;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;

class PurchaseHistoryResponseListenerImpl implements PurchaseHistoryResponseListener {

    private static final String TAG = "[PurchaseHistoryResponseListenerImpl]";

    @NonNull
    private final BillingConfig config;
    @NonNull
    private final Executor workerExecutor;
    @NonNull
    private final Executor uiExecutor;
    @NonNull
    private final BillingClient billingClient;
    @NonNull
    private final UtilsProvider utilsProvider;
    @NonNull
    private final String type;
    @NonNull
    private final BillingLibraryConnectionHolder billingLibraryConnectionHolder;
    @NonNull
    private final SystemTimeProvider systemTimeProvider;

    PurchaseHistoryResponseListenerImpl(@NonNull final BillingConfig config,
                                        @NonNull final Executor workerExecutor,
                                        @NonNull final Executor uiExecutor,
                                        @NonNull final BillingClient billingClient,
                                        @NonNull final UtilsProvider utilsProvider,
                                        @NonNull final String type,
                                        @NonNull final BillingLibraryConnectionHolder billingLibraryConnectionHolder) {
        this(config, workerExecutor, uiExecutor, billingClient, utilsProvider, type,
                billingLibraryConnectionHolder, new SystemTimeProvider());
    }

    @VisibleForTesting
    PurchaseHistoryResponseListenerImpl(@NonNull final BillingConfig config,
                                        @NonNull final Executor workerExecutor,
                                        @NonNull final Executor uiExecutor,
                                        @NonNull final BillingClient billingClient,
                                        @NonNull final UtilsProvider utilsProvider,
                                        @NonNull final String type,
                                        @NonNull final BillingLibraryConnectionHolder billingLibraryConnectionHolder,
                                        @NonNull final SystemTimeProvider systemTimeProvider) {
        this.config = config;
        this.workerExecutor = workerExecutor;
        this.uiExecutor = uiExecutor;
        this.billingClient = billingClient;
        this.utilsProvider = utilsProvider;
        this.type = type;
        this.billingLibraryConnectionHolder = billingLibraryConnectionHolder;
        this.systemTimeProvider = systemTimeProvider;
    }

    @UiThread
    @Override
    public void onPurchaseHistoryResponse(@NonNull final BillingResult billingResult,
                                          @Nullable final List<PurchaseHistoryRecord> list) {
        workerExecutor.execute(new SafeRunnable() {
            @Override
            public void runSafety() {
                processResponse(billingResult, list);
                billingLibraryConnectionHolder.removeListener(PurchaseHistoryResponseListenerImpl.this);
            }
        });
    }

    @WorkerThread
    private void processResponse(@NonNull final BillingResult billingResult,
                                 @Nullable final List<PurchaseHistoryRecord> list) {
        YLogger.info(TAG, "onPurchaseHistoryResponse type=%s, result=%s, list=%s",
                type, BillingUtils.toString(billingResult), list
        );
        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && list != null) {
            final Map<String, BillingInfo> history = extractHistory(list);
            final Map<String, BillingInfo> newBillingInfo =
                    utilsProvider.getUpdatePolicy().getBillingInfoToUpdate(
                            config, history, utilsProvider.getBillingInfoManager()
                    );

            if (newBillingInfo.isEmpty()) {
                updateStorage(history, newBillingInfo);
            } else {
                querySkuDetails(newBillingInfo, new Callable<Void>() {
                    @Override
                    public Void call() throws Exception {
                        updateStorage(history, newBillingInfo);
                        return null;
                    }
                });
            }
        }
    }

    @NonNull
    private Map<String, BillingInfo> extractHistory(@NonNull final List<PurchaseHistoryRecord> list) {
        final Map<String, BillingInfo> history = new HashMap<>();
        for (final PurchaseHistoryRecord record : list) {
            final BillingInfo info = new BillingInfo(
                    ProductTypeParser.parse(type),
                    record.getSku(),
                    record.getPurchaseToken(),
                    record.getPurchaseTime(),
                    0
            );
            YLogger.debug(TAG, "Billing info from history %s", info);
            history.put(info.sku, info);
        }
        return history;
    }

    private void querySkuDetails(@NonNull final Map<String, BillingInfo> newBillingInfo,
                                 @NonNull final Callable<Void> billingInfoSentListener) {
        final SkuDetailsParams params = SkuDetailsParams.newBuilder()
                .setType(type)
                .setSkusList(new ArrayList<>(newBillingInfo.keySet()))
                .build();
        final SkuDetailsResponseListenerImpl listener =
                new SkuDetailsResponseListenerImpl(
                        type,
                        workerExecutor,
                        billingClient,
                        utilsProvider,
                        billingInfoSentListener,
                        newBillingInfo,
                        billingLibraryConnectionHolder
                );
        billingLibraryConnectionHolder.addListener(listener);
        uiExecutor.execute(new SafeRunnable() {
            @Override
            public void runSafety() {
                if (billingClient.isReady()) {
                    billingClient.querySkuDetailsAsync(params, listener);
                } else {
                    workerExecutor.execute(new SafeRunnable() {
                        @Override
                        public void runSafety() {
                            billingLibraryConnectionHolder.removeListener(listener);
                        }
                    });
                }
            }
        });
    }

    @VisibleForTesting
    protected void updateStorage(@NonNull final Map<String, BillingInfo> history,
                                 @NonNull final Map<String, BillingInfo> newBillingInfo) {
        YLogger.info(TAG, "updating storage");
        final BillingInfoManager billingInfoManager = utilsProvider.getBillingInfoManager();
        final long now = systemTimeProvider.currentTimeMillis();
        for (final BillingInfo billingInfo: history.values()) {
            if (newBillingInfo.containsKey(billingInfo.sku)) {
                billingInfo.sendTime = now;
            } else {
                final BillingInfo billingInfoFromStorage = billingInfoManager.get(billingInfo.sku);
                if (billingInfoFromStorage != null) {
                    billingInfo.sendTime = billingInfoFromStorage.sendTime;
                }
            }
        }
        billingInfoManager.update(history);
        if (!billingInfoManager.isFirstInappCheckOccurred() && BillingClient.SkuType.INAPP.equals(type)) {
            YLogger.info(TAG, "marking markFirstInappCheckOccurred");
            billingInfoManager.markFirstInappCheckOccurred();
        }
    }
}
