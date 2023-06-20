package io.appmetrica.analytics.impl.component;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.coreutils.internal.logger.YLogger;
import io.appmetrica.analytics.coreutils.internal.services.PackageManagerUtils;
import io.appmetrica.analytics.coreutils.internal.time.TimePassedChecker;
import io.appmetrica.analytics.impl.CounterConfigurationReporterType;
import io.appmetrica.analytics.impl.CounterReport;
import io.appmetrica.analytics.impl.GlobalServiceLocator;
import io.appmetrica.analytics.impl.InternalEvents;
import io.appmetrica.analytics.impl.StatisticsRestrictionControllerImpl;
import io.appmetrica.analytics.impl.billing.BillingMonitorWrapper;
import io.appmetrica.analytics.impl.component.processor.EventProcessingStrategyFactory;
import io.appmetrica.analytics.impl.component.processor.factory.RegularMainReporterFactory;
import io.appmetrica.analytics.impl.referrer.common.ReferrerInfo;
import io.appmetrica.analytics.impl.referrer.service.IReferrerHandledNotifier;
import io.appmetrica.analytics.impl.referrer.service.IReferrerHandledProvider;
import io.appmetrica.analytics.impl.referrer.service.ReferrerHolder;
import io.appmetrica.analytics.impl.referrer.service.ReferrerListenerNotifier;
import io.appmetrica.analytics.impl.startup.StartupState;
import io.appmetrica.analytics.impl.startup.executor.ComponentStartupExecutorFactory;

import static io.appmetrica.analytics.impl.InternalEvents.EVENT_TYPE_REGULAR;

public class MainReporterComponentUnit extends ComponentUnit
    implements IReferrerHandledProvider, IReferrerHandledNotifier {

    private static final String TAG = "[MainMasterComponentUnit]";

    @NonNull
    private final ReferrerHolder mReferrerHolder;
    @NonNull
    private final ReferrerListenerNotifier mReferrerListener;
    @NonNull
    private final StatisticsRestrictionControllerImpl mStatisticsRestrictionController;
    @NonNull
    private final BillingMonitorWrapper billingMonitorWrapper;

    public MainReporterComponentUnit(@NonNull Context context,
                                     @NonNull StartupState startupState,
                                     @NonNull ComponentId componentId,
                                     @NonNull CommonArguments.ReporterArguments sdkConfig,
                                     @NonNull ReferrerHolder referrerHolder,
                                     @NonNull StatisticsRestrictionControllerImpl controller,
                                     @NonNull ComponentStartupExecutorFactory executorFactory) {
        this(
            context,
            componentId,
            startupState,
            sdkConfig,
            new AppEnvironmentProvider(),
            new TimePassedChecker(),
            new MainReporterComponentUnitFieldsFactory(
                context,
                componentId,
                sdkConfig,
                executorFactory,
                startupState,
                new MainReporterArgumentsFactory(controller),
                GlobalServiceLocator.getInstance().getServiceExecutorProvider()
                    .getNetworkTaskProcessorExecutor(),
                PackageManagerUtils.getAppVersionCodeInt(context),
                GlobalServiceLocator.getInstance().getServiceExecutorProvider(),
                GlobalServiceLocator.getInstance().getLifecycleDependentComponentManager()
            ),
            referrerHolder,
            controller
        );
    }

    @VisibleForTesting
    MainReporterComponentUnit(@NonNull Context context,
                              @NonNull ComponentId componentId,
                              @NonNull StartupState startupState,
                              @NonNull CommonArguments.ReporterArguments sdkConfig,
                              @NonNull AppEnvironmentProvider appEnvironmentProvider,
                              @NonNull TimePassedChecker timePassedChecker,
                              @NonNull MainReporterComponentUnitFieldsFactory fieldsFactory,
                              @NonNull ReferrerHolder referrerHolder,
                              @NonNull StatisticsRestrictionControllerImpl controller) {
        super(
            context,
            componentId,
            appEnvironmentProvider,
            timePassedChecker,
            fieldsFactory
        );
        mReferrerHolder = referrerHolder;
        EventProcessingStrategyFactory factory = getEventProcessingStrategyFactory();
        factory.mutateHandlers(EVENT_TYPE_REGULAR, new RegularMainReporterFactory(factory.getHandlersProvider()));
        mReferrerListener = fieldsFactory.createReferrerListener(this);
        mStatisticsRestrictionController = controller;
        billingMonitorWrapper = fieldsFactory.createBillingMonitorWrapper(this);
        billingMonitorWrapper.maybeStartWatching(startupState, sdkConfig.revenueAutoTrackingEnabled);
    }

    @Override
    public boolean wasReferrerHandled() {
        return getVitalComponentDataProvider().getReferrerHandled();
    }

    @Override
    public void onReferrerHandled() {
        getVitalComponentDataProvider().setReferrerHandled(true);
    }

    public class MainReporterListener implements ReferrerHolder.Listener {

        @Override
        public void handleReferrer(@Nullable ReferrerInfo referrer) {
            // referrer is NonNull due to used filter
            if (referrer == null) {
                YLogger.error(TAG, "Unexpected null referrer");
                return;
            }
            final CounterReport referrerReport = new CounterReport();
            referrerReport.setValueBytes(referrer.toProto());
            referrerReport.setType(InternalEvents.EVENT_TYPE_SEND_REFERRER.getTypeId());
            handleReport(referrerReport);
        }

    }

    @Override
    public synchronized void updateSdkConfig(@NonNull CommonArguments.ReporterArguments sdkConfig) {
        super.updateSdkConfig(sdkConfig);
        mStatisticsRestrictionController.setEnabledFromMainReporter(
            sdkConfig.statisticsSending
        );
    }

    @Override
    @NonNull
    public CounterConfigurationReporterType getReporterType() {
        return CounterConfigurationReporterType.MAIN;
    }

    @Override
    public void subscribeForReferrer() {
        mReferrerHolder.subscribe(mReferrerListener);
    }

    @Override
    public void onStartupChanged(@NonNull StartupState newState) {
        super.onStartupChanged(newState);
        billingMonitorWrapper.onStartupStateChanged(newState);
    }
}
