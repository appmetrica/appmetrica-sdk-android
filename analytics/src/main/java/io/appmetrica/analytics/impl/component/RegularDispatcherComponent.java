package io.appmetrica.analytics.impl.component;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.coreutils.internal.logger.YLogger;
import io.appmetrica.analytics.impl.CounterReport;
import io.appmetrica.analytics.impl.EventsManager;
import io.appmetrica.analytics.impl.component.clients.ClientUnit;
import io.appmetrica.analytics.impl.component.clients.ComponentUnitFactory;
import io.appmetrica.analytics.impl.startup.StartupCenter;
import io.appmetrica.analytics.impl.startup.StartupError;
import io.appmetrica.analytics.impl.startup.StartupListener;
import io.appmetrica.analytics.impl.startup.StartupState;
import io.appmetrica.analytics.impl.startup.StartupUnit;
import java.util.ArrayList;
import java.util.List;

public class RegularDispatcherComponent<COMPONENT extends IReportableComponent & IComponent>
        implements IClientConsumer, StartupListener, IDispatcherComponent {

    private static final String TAG = "[RegularDispatcherComponent]";

    @NonNull
    private final Context mContext;
    @NonNull
    private final ComponentId mComponentId;
    @NonNull
    private final ComponentUnitFactory<COMPONENT> mComponentUnitFactory;
    @NonNull
    private final StartupUnit startupUnit;
    @NonNull
    private final ReporterArgumentsHolder mReporterArgumentsHolder;

    @Nullable
    private COMPONENT mReportingComponent;

    private List<StartupListener> startupListeners = new ArrayList<StartupListener>();

    @NonNull
    private final ComponentLifecycleManager<ClientUnit> mLifecycleManager;

    public RegularDispatcherComponent(@NonNull Context context,
                                      @NonNull ComponentId componentId,
                                      @NonNull CommonArguments sdkConfig,
                                      @NonNull ComponentUnitFactory<COMPONENT> componentUnitFactory) {
        this(
                context,
                componentId,
                sdkConfig,
                new ReporterArgumentsHolder(sdkConfig.componentArguments),
                componentUnitFactory,
                new ComponentLifecycleManager<ClientUnit>(),
                StartupCenter.getInstance()
        );
    }

    public RegularDispatcherComponent(@NonNull Context context,
                                      @NonNull ComponentId componentId,
                                      @NonNull CommonArguments sdkConfig,
                                      @NonNull ReporterArgumentsHolder reporterArgumentsHolder,
                                      @NonNull ComponentUnitFactory<COMPONENT> componentUnitFactory,
                                      @NonNull ComponentLifecycleManager<ClientUnit> lifecycleManager,
                                      @NonNull StartupCenter startupCenter) {
        mContext = context;
        mComponentId = componentId;
        mReporterArgumentsHolder = reporterArgumentsHolder;
        mComponentUnitFactory = componentUnitFactory;
        mLifecycleManager = lifecycleManager;
        startupUnit = startupCenter.getOrCreateStartupUnit(mContext, mComponentId, sdkConfig.startupArguments);
        startupCenter.registerStartupListener(mComponentId, this);
    }

    public void handleReport(@NonNull CounterReport counterReport, @NonNull CommonArguments configuration) {
        YLogger.d(
                "%s handle report for componentId: %s; data: %s",
                TAG,
                mComponentId,
                counterReport
        );
        IReportableComponent component = getOrCreateReportingComponent();
        if (EventsManager.isEventWithoutAppConfigUpdate(counterReport.getType()) == false) {
            updateSdkConfig(configuration.componentArguments);
        }
        component.handleReport(counterReport);
    }

    public synchronized void updateSdkConfig(@NonNull CommonArguments.ReporterArguments sdkConfig) {
        YLogger.d(
                "%sUpdate sdk config for componentId: %s; config: %s",
                TAG,
                mComponentId,
                sdkConfig
        );
        mReporterArgumentsHolder.updateArguments(sdkConfig);
        if (mReportingComponent != null) {
            mReportingComponent.updateSdkConfig(sdkConfig);
        }
    }

    @Override
    public synchronized void connectClient(@NonNull final ClientUnit client) {
        mLifecycleManager.connectClient(client);
    }

    @Override
    public synchronized void disconnectClient(@NonNull final ClientUnit client) {
        mLifecycleManager.disconnectClient(client);
    }

    private COMPONENT getOrCreateReportingComponent() {
        if (mReportingComponent == null) {
            synchronized (this) {
                mReportingComponent = mComponentUnitFactory.createComponentUnit(
                        mContext,
                        mComponentId,
                        mReporterArgumentsHolder.getArguments(),
                        startupUnit
                );
                startupListeners.add(mReportingComponent);
            }
        }
        return mReportingComponent;
    }

    @NonNull
    @VisibleForTesting
    public Context getContext() {
        return mContext;
    }

    @NonNull
    @VisibleForTesting
    public final ComponentId getComponentId() {
        return mComponentId;
    }

    @NonNull
    @VisibleForTesting
    public ReporterArgumentsHolder getReporterArgumentsHolder() {
        return mReporterArgumentsHolder;
    }

    @NonNull
    @VisibleForTesting
    public ComponentUnitFactory<COMPONENT> getComponentUnitFactory() {
        return mComponentUnitFactory;
    }

    @NonNull
    @VisibleForTesting
    public ComponentLifecycleManager<ClientUnit> getLifecycleManager() {
        return mLifecycleManager;
    }

    @Override
    public synchronized void onStartupChanged(@NonNull StartupState newState) {
        for (StartupListener listener : startupListeners) {
            listener.onStartupChanged(newState);
        }
    }

    @Override
    public synchronized void onStartupError(@NonNull StartupError error, @Nullable StartupState existingState) {
        for (StartupListener listener : startupListeners) {
            listener.onStartupError(error, existingState);
        }
    }

    @Override
    public void updateConfig(@NonNull CommonArguments arguments) {
        startupUnit.updateConfiguration(arguments.startupArguments);
        updateSdkConfig(arguments.componentArguments);
    }
}
