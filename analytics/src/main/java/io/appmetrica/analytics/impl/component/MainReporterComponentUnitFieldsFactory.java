package io.appmetrica.analytics.impl.component;

import android.content.Context;
import androidx.annotation.NonNull;
import io.appmetrica.analytics.coreapi.internal.executors.ICommonExecutor;
import io.appmetrica.analytics.impl.LifecycleDependentComponentManager;
import io.appmetrica.analytics.impl.billing.BillingInfoSenderImpl;
import io.appmetrica.analytics.impl.billing.BillingInfoStorageImpl;
import io.appmetrica.analytics.impl.billing.BillingMonitorWrapper;
import io.appmetrica.analytics.impl.billing.BillingTypeDetector;
import io.appmetrica.analytics.impl.referrer.service.OnlyOnceReferrerNotificationFilter;
import io.appmetrica.analytics.impl.referrer.service.ReferrerListenerNotifier;
import io.appmetrica.analytics.impl.request.ReportRequestConfig;
import io.appmetrica.analytics.impl.startup.StartupState;
import io.appmetrica.analytics.impl.startup.executor.ComponentStartupExecutorFactory;
import io.appmetrica.analytics.impl.utils.executors.ServiceExecutorProvider;

public class MainReporterComponentUnitFieldsFactory extends ComponentUnitFieldsFactory {

    @NonNull
    private final ServiceExecutorProvider serviceExecutorProvider;

    public MainReporterComponentUnitFieldsFactory(
        @NonNull Context context,
        @NonNull ComponentId componentId,
        @NonNull CommonArguments.ReporterArguments sdkConfig,
        @NonNull ComponentStartupExecutorFactory startupExecutorFactory,
        @NonNull StartupState startupState,
        @NonNull ReportRequestConfig.DataSendingStrategy strategy,
        @NonNull ICommonExecutor taskExecutor,
        final int currentAppVersion,
        @NonNull final ServiceExecutorProvider serviceExecutorProvider,
        @NonNull LifecycleDependentComponentManager lifecycleDependentComponentManager,
        @NonNull EventTriggerProviderCreator eventTriggerProviderCreator
    ) {
        super(
            context,
            componentId,
            sdkConfig,
            startupExecutorFactory,
            startupState,
            strategy,
            taskExecutor,
            currentAppVersion,
            lifecycleDependentComponentManager,
            eventTriggerProviderCreator
        );
        this.serviceExecutorProvider = serviceExecutorProvider;
    }

    @NonNull
    public ReferrerListenerNotifier createReferrerListener(@NonNull MainReporterComponentUnit unit) {
        return new ReferrerListenerNotifier(
            new OnlyOnceReferrerNotificationFilter(unit),
            unit.new MainReporterListener(),
            unit
        );
    }

    @NonNull
    public BillingMonitorWrapper createBillingMonitorWrapper(@NonNull final MainReporterComponentUnit unit) {
        return new BillingMonitorWrapper(
            mContext,
            serviceExecutorProvider.getDefaultExecutor(),
            serviceExecutorProvider.getUiExecutor(),
            BillingTypeDetector.getBillingType(),
            new BillingInfoStorageImpl(mContext),
            new BillingInfoSenderImpl(unit)
        );
    }
}
