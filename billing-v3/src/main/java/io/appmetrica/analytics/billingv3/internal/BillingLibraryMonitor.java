package io.appmetrica.analytics.billingv3.internal;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import com.android.billingclient.api.BillingClient;
import io.appmetrica.analytics.billinginterface.internal.config.BillingConfig;
import io.appmetrica.analytics.billinginterface.internal.library.UtilsProvider;
import io.appmetrica.analytics.billinginterface.internal.monitor.BillingMonitor;
import io.appmetrica.analytics.billinginterface.internal.storage.BillingInfoManager;
import io.appmetrica.analytics.billinginterface.internal.storage.BillingInfoSender;
import io.appmetrica.analytics.billinginterface.internal.storage.BillingInfoStorage;
import io.appmetrica.analytics.billinginterface.internal.update.UpdatePolicy;
import io.appmetrica.analytics.billingv3.impl.library.BillingClientStateListenerImpl;
import io.appmetrica.analytics.billingv3.impl.library.PurchasesUpdatedListenerImpl;
import io.appmetrica.analytics.billingv3.impl.storage.BillingInfoManagerImpl;
import io.appmetrica.analytics.billingv3.impl.update.UpdatePolicyImpl;
import io.appmetrica.analytics.coreutils.internal.executors.SafeRunnable;
import io.appmetrica.analytics.coreutils.internal.logger.YLogger;
import java.util.concurrent.Executor;

public class BillingLibraryMonitor implements BillingMonitor, UtilsProvider {

    private static final String TAG = "[BillingLibraryMonitor]";

    @NonNull
    private final Context context;
    @NonNull
    private final Executor workerExecutor;
    @NonNull
    private final Executor uiExecutor;
    @NonNull
    private final BillingInfoManager billingInfoManager;
    @NonNull
    private final UpdatePolicy updatePolicy;
    @NonNull
    private final BillingInfoSender billingInfoSender;
    @Nullable
    private BillingConfig billingConfig;

    public BillingLibraryMonitor(@NonNull final Context context,
                                 @NonNull final Executor workerExecutor,
                                 @NonNull final Executor uiExecutor,
                                 @NonNull final BillingInfoStorage billingInfoStorage,
                                 @NonNull final BillingInfoSender billingInfoSender) {
        this(
                context,
                workerExecutor,
                uiExecutor,
                new BillingInfoManagerImpl(billingInfoStorage),
                new UpdatePolicyImpl(),
                billingInfoSender
        );
    }

    public BillingLibraryMonitor(@NonNull final Context context,
                                 @NonNull final Executor workerExecutor,
                                 @NonNull final Executor uiExecutor,
                                 @NonNull final BillingInfoManager billingInfoManager,
                                 @NonNull final UpdatePolicy updatePolicy,
                                 @NonNull final BillingInfoSender billingInfoSender) {
        this.context = context;
        this.workerExecutor = workerExecutor;
        this.uiExecutor = uiExecutor;
        this.billingInfoManager = billingInfoManager;
        this.updatePolicy = updatePolicy;
        this.billingInfoSender = billingInfoSender;
    }

    @WorkerThread
    @Override
    public void onSessionResumed() throws Throwable {
        YLogger.info(TAG, "onSessionResumed with billingConfig=%s", billingConfig);
        final BillingConfig localBillingConfig = billingConfig;
        if (localBillingConfig != null) {
            uiExecutor.execute(new SafeRunnable() {
                @Override
                public void runSafety() {
                    final BillingClient billingClient = BillingClient
                            .newBuilder(context)
                            .setListener(new PurchasesUpdatedListenerImpl())
                            .enablePendingPurchases()
                            .build();
                    billingClient.startConnection(new BillingClientStateListenerImpl(
                            localBillingConfig, workerExecutor, uiExecutor, billingClient, BillingLibraryMonitor.this
                    ));
                }
            });
        } else {
            YLogger.info(TAG, "billingConfig is null");
        }
    }

    @Override
    synchronized public void onBillingConfigChanged(@Nullable final BillingConfig billingConfig) {
        YLogger.info(TAG, "onBillingConfigChanged: %s", billingConfig);
        this.billingConfig = billingConfig;
    }

    @NonNull
    @Override
    public BillingInfoManager getBillingInfoManager() {
        return billingInfoManager;
    }

    @NonNull
    @Override
    public UpdatePolicy getUpdatePolicy() {
        return updatePolicy;
    }

    @NonNull
    @Override
    public BillingInfoSender getBillingInfoSender() {
        return billingInfoSender;
    }

    @NonNull
    @Override
    public Executor getUiExecutor() {
        return uiExecutor;
    }

    @NonNull
    @Override
    public Executor getWorkerExecutor() {
        return workerExecutor;
    }
}
