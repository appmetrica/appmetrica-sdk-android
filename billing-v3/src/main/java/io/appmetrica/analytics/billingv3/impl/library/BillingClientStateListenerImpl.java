package io.appmetrica.analytics.billingv3.impl.library;

import androidx.annotation.NonNull;
import androidx.annotation.UiThread;
import androidx.annotation.VisibleForTesting;
import androidx.annotation.WorkerThread;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingResult;
import io.appmetrica.analytics.billinginterface.internal.config.BillingConfig;
import io.appmetrica.analytics.billinginterface.internal.library.UtilsProvider;
import io.appmetrica.analytics.billingv3.impl.BillingUtils;
import io.appmetrica.analytics.coreutils.internal.executors.SafeRunnable;
import io.appmetrica.analytics.coreutils.internal.logger.YLogger;
import java.util.Arrays;
import java.util.concurrent.Executor;

public class BillingClientStateListenerImpl implements BillingClientStateListener {

    private static final String TAG = "[BillingClientStateListenerImpl]";

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
    private final BillingLibraryConnectionHolder billingLibraryConnectionHolder;

    public BillingClientStateListenerImpl(@NonNull final BillingConfig config,
                                          @NonNull final Executor workerExecutor,
                                          @NonNull final Executor uiExecutor,
                                          @NonNull final BillingClient billingClient,
                                          @NonNull final UtilsProvider utilsProvider) {
        this(config, workerExecutor, uiExecutor, billingClient, utilsProvider,
                new BillingLibraryConnectionHolder(billingClient));
    }

    @VisibleForTesting
    BillingClientStateListenerImpl(@NonNull final BillingConfig config,
                                   @NonNull final Executor workerExecutor,
                                   @NonNull final Executor uiExecutor,
                                   @NonNull final BillingClient billingClient,
                                   @NonNull final UtilsProvider utilsProvider,
                                   @NonNull final BillingLibraryConnectionHolder billingLibraryConnectionHolder) {
        this.config = config;
        this.workerExecutor = workerExecutor;
        this.uiExecutor = uiExecutor;
        this.billingClient = billingClient;
        this.utilsProvider = utilsProvider;
        this.billingLibraryConnectionHolder = billingLibraryConnectionHolder;
    }

    @UiThread
    @Override
    public void onBillingSetupFinished(@NonNull final BillingResult billingResult) {
        workerExecutor.execute(new SafeRunnable() {
            @Override
            public void runSafety() {
                processResult(billingResult);
            }
        });
    }

    @WorkerThread
    private void processResult(@NonNull final BillingResult billingResult) {
        YLogger.info(TAG,"onBillingSetupFinished result=%s", BillingUtils.toString(billingResult));
        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
            for (final String type : Arrays.asList(BillingClient.SkuType.INAPP, BillingClient.SkuType.SUBS)) {
                final PurchaseHistoryResponseListenerImpl listener = new PurchaseHistoryResponseListenerImpl(
                        config,
                        workerExecutor,
                        uiExecutor,
                        billingClient,
                        utilsProvider,
                        type,
                        billingLibraryConnectionHolder
                );
                billingLibraryConnectionHolder.addListener(listener);
                uiExecutor.execute(new SafeRunnable() {
                    @Override
                    public void runSafety() {
                        if (billingClient.isReady()) {
                            billingClient.queryPurchaseHistoryAsync(type, listener);
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
        }
    }

    @UiThread
    @Override
    public void onBillingServiceDisconnected() {
        YLogger.info(TAG,"onBillingServiceDisconnected");
    }
}
