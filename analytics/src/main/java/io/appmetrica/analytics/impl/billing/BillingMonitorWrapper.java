package io.appmetrica.analytics.impl.billing;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.billinginterface.internal.BillingType;
import io.appmetrica.analytics.billinginterface.internal.monitor.BillingMonitor;
import io.appmetrica.analytics.billinginterface.internal.storage.BillingInfoSender;
import io.appmetrica.analytics.billinginterface.internal.storage.BillingInfoStorage;
import io.appmetrica.analytics.coreapi.internal.servicecomponents.applicationstate.ApplicationState;
import io.appmetrica.analytics.coreapi.internal.servicecomponents.applicationstate.ApplicationStateObserver;
import io.appmetrica.analytics.coreapi.internal.servicecomponents.applicationstate.ApplicationStateProvider;
import io.appmetrica.analytics.coreutils.internal.WrapUtils;
import io.appmetrica.analytics.impl.DefaultValues;
import io.appmetrica.analytics.impl.GlobalServiceLocator;
import io.appmetrica.analytics.impl.StartupStateObserver;
import io.appmetrica.analytics.impl.startup.StartupState;
import io.appmetrica.analytics.logger.internal.YLogger;
import java.util.concurrent.Executor;

public class BillingMonitorWrapper implements StartupStateObserver {

    private static final String TAG = "[BillingMonitorWrapper]";

    @Nullable
    private BillingMonitor billingMonitor;
    @NonNull
    private final Context context;
    @NonNull
    private final Executor workerExecutor;
    @NonNull
    private final Executor uiExecutor;
    @NonNull
    private final BillingType billingType;
    @NonNull
    private final BillingInfoStorage billingInfoStorage;
    @NonNull
    private final BillingInfoSender billingInfoSender;
    @NonNull
    private final ApplicationStateProvider applicationStateProvider;
    @NonNull
    private final BillingMonitorProvider billingMonitorProvider;

    public BillingMonitorWrapper(@NonNull final Context context,
                                 @NonNull final Executor workerExecutor,
                                 @NonNull final Executor uiExecutor,
                                 @NonNull BillingType billingType,
                                 @NonNull final BillingInfoStorage billingInfoStorage,
                                 @NonNull final BillingInfoSender billingInfoSender) {
        this(
                context,
                workerExecutor,
                uiExecutor,
                billingType,
                billingInfoStorage,
                billingInfoSender,
                GlobalServiceLocator.getInstance().getApplicationStateProvider(),
                new BillingMonitorProvider()
        );
    }

    public BillingMonitorWrapper(@NonNull final Context context,
                                 @NonNull final Executor workerExecutor,
                                 @NonNull final Executor uiExecutor,
                                 @NonNull BillingType billingType,
                                 @NonNull final BillingInfoStorage billingInfoStorage,
                                 @NonNull final BillingInfoSender billingInfoSender,
                                 @NonNull ApplicationStateProvider applicationStateProvider,
                                 @NonNull BillingMonitorProvider billingMonitorProvider) {
        this.context = context;
        this.workerExecutor = workerExecutor;
        this.uiExecutor = uiExecutor;
        this.billingType = billingType;
        this.billingInfoStorage = billingInfoStorage;
        this.billingInfoSender = billingInfoSender;
        this.applicationStateProvider = applicationStateProvider;
        this.billingMonitorProvider = billingMonitorProvider;
    }

    public void maybeStartWatching(@NonNull StartupState startupState, @Nullable Boolean revenueAutoTrackingEnabled) {
        if (WrapUtils.getOrDefault(revenueAutoTrackingEnabled, DefaultValues.DEFAULT_REVENUE_AUTO_TRACKING_ENABLED)) {
            synchronized (this) {
                billingMonitor = billingMonitorProvider.get(
                        context,
                        workerExecutor,
                        uiExecutor,
                        billingType,
                        billingInfoStorage,
                        billingInfoSender
                );
            }
            billingMonitor.onBillingConfigChanged(startupState.getAutoInappCollectingConfig());
            final ApplicationStateObserver observer = new ApplicationStateObserver() {
                @Override
                public void onApplicationStateChanged(@NonNull final ApplicationState state) {
                    checkStateAndCollectAutoInapp(state);
                }
            };
            final ApplicationState currentState =
                    applicationStateProvider.registerStickyObserver(observer);
            checkStateAndCollectAutoInapp(currentState);
        }
    }

    @Override
    public synchronized void onStartupStateChanged(@NonNull StartupState startupState) {
        final BillingMonitor billingMonitorCopy;
        synchronized (this) {
            billingMonitorCopy = billingMonitor;
        }
        if (billingMonitorCopy != null) {
            billingMonitorCopy.onBillingConfigChanged(startupState.getAutoInappCollectingConfig());
        }
    }

    private void checkStateAndCollectAutoInapp(@NonNull final ApplicationState state) {
        YLogger.info(TAG, "checkStateAndCollectAutoInapp " + state.getStringValue());
        if (state == ApplicationState.VISIBLE) {
            try {
                final BillingMonitor billingMonitorCopy = billingMonitor;
                if (billingMonitorCopy != null) {
                    billingMonitorCopy.onSessionResumed();
                }
            } catch (Throwable e) {
                YLogger.error(TAG, "Error occurred during billing library call " + e);
            }
        }
    }
}
