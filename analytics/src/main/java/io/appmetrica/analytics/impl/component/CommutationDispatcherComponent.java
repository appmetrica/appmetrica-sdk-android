package io.appmetrica.analytics.impl.component;

import android.content.Context;
import android.os.ResultReceiver;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.impl.ClientIdentifiersChangedListener;
import io.appmetrica.analytics.impl.ClientIdentifiersHolder;
import io.appmetrica.analytics.impl.ClientIdentifiersProvider;
import io.appmetrica.analytics.impl.ClientIdentifiersProviderFactory;
import io.appmetrica.analytics.impl.utils.BooleanUtils;
import io.appmetrica.analytics.internal.CounterConfigurationReporterType;
import io.appmetrica.analytics.impl.CounterReport;
import io.appmetrica.analytics.impl.DataResultReceiver;
import io.appmetrica.analytics.impl.GlobalServiceLocator;
import io.appmetrica.analytics.impl.IdentifiersData;
import io.appmetrica.analytics.impl.TaskProcessor;
import io.appmetrica.analytics.impl.component.clients.CommutationClientUnit;
import io.appmetrica.analytics.impl.component.processor.commutation.CommutationHandler;
import io.appmetrica.analytics.impl.component.processor.commutation.CommutationReportProcessor;
import io.appmetrica.analytics.impl.id.AdvertisingIdGetter;
import io.appmetrica.analytics.impl.referrer.common.ReferrerChosenListener;
import io.appmetrica.analytics.impl.referrer.common.ReferrerInfo;
import io.appmetrica.analytics.impl.referrer.common.ReferrerResultReceiver;
import io.appmetrica.analytics.impl.referrer.service.ReferrerHolder;
import io.appmetrica.analytics.impl.referrer.service.ReferrerManager;
import io.appmetrica.analytics.impl.startup.StartupCenter;
import io.appmetrica.analytics.impl.startup.StartupError;
import io.appmetrica.analytics.impl.startup.StartupListener;
import io.appmetrica.analytics.impl.startup.StartupState;
import io.appmetrica.analytics.impl.startup.StartupUnit;
import io.appmetrica.analytics.impl.utils.StartupUtils;
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * Does not share handling interface with ComponentUnit.
 * It's better to say, that this class process 'signals' from client. In comparison with all other Components, that
 * handle 'reports'.
 * This signals inform service, that it must to store some data, send startup or return some data to client using IPC
 * (via android.os.ResultReceiver for example).
 */
public class CommutationDispatcherComponent implements IComponent, StartupListener, IDispatcherComponent {

    private static final String TAG = "[CommutationComponentUnit]";

    @NonNull private final Context mContext;
    @NonNull private final ComponentId mComponentId;

    @NonNull private final StartupCenter mStartupCenter;
    @NonNull private final StartupUnit mStartupUnit;
    @NonNull
    private final ReporterArgumentsHolder mReporterArgumentsHolder;
    @NonNull
    private final AdvertisingIdGetter mAdvertisingIdGetter;

    @NonNull
    private final CommutationReportProcessor<CommutationHandler, CommutationDispatcherComponent> mReportProcessor;
    @NonNull private final TaskProcessor<CommutationDispatcherComponent> mTaskProcessor;

    @NonNull
    private List<IdentifiersData> mStartupEventReceivers = new ArrayList<IdentifiersData>();

    @NonNull
    private final ComponentLifecycleManager<CommutationClientUnit> mLifecycleManager;
    @NonNull
    private final ReferrerHolder mReferrerHolder;
    @NonNull
    private final ClientIdentifiersProvider mClientIdentifiersProvider;
    @NonNull
    private final ReferrerManager referrerManager;

    private final Object mStartupLock = new Object();

    public CommutationDispatcherComponent(@NonNull Context context,
                                          @NonNull StartupCenter startupCenter,
                                          @NonNull ComponentId componentId,
                                          @NonNull CommonArguments clientConfiguration,
                                          @NonNull ReferrerHolder referrerHolder) {
        this(
                context,
                startupCenter,
                componentId,
                clientConfiguration,
                new ReporterArgumentsHolder(clientConfiguration.componentArguments),
                referrerHolder,
                new ComponentLifecycleManager<CommutationClientUnit>(),
                new CommutationDispatcherComponentFieldsFactory(),
                new ClientIdentifiersProviderFactory(),
                new ReferrerManager()
        );
    }

    @VisibleForTesting
    CommutationDispatcherComponent(@NonNull Context context,
                                   @NonNull StartupCenter startupCenter,
                                   @NonNull ComponentId componentId,
                                   @NonNull CommonArguments clientConfiguration,
                                   @NonNull ReporterArgumentsHolder reporterArgumentsHolder,
                                   @NonNull ReferrerHolder referrerHolder,
                                   @NonNull ComponentLifecycleManager<CommutationClientUnit> componentLifecycleManager,
                                   @NonNull CommutationDispatcherComponentFieldsFactory fieldsFactory,
                                   @NonNull ClientIdentifiersProviderFactory clientIdentifiersProviderFactory,
                                   @NonNull ReferrerManager referrerManager) {
        mContext = context.getApplicationContext();
        mComponentId = componentId;
        mStartupCenter = startupCenter;
        mReporterArgumentsHolder = reporterArgumentsHolder;
        mLifecycleManager = componentLifecycleManager;
        mReportProcessor = fieldsFactory.createCommutationReportProcessor(this);
        updateAdvIdentifiersTracking(clientConfiguration);
        mStartupUnit = mStartupCenter.getOrCreateStartupUnit(
                mContext,
                mComponentId,
                clientConfiguration.startupArguments
        );
        mAdvertisingIdGetter = GlobalServiceLocator.getInstance().getAdvertisingIdGetter();
        mClientIdentifiersProvider = clientIdentifiersProviderFactory
                .createClientIdentifiersProvider(mStartupUnit, mAdvertisingIdGetter, mContext);
        mTaskProcessor = fieldsFactory.createTaskProcessor(this, mStartupUnit);

        DebugLogger.INSTANCE.info(
            TAG,
            "Create a new commutation component for package: %s",
            componentId.getPackage()
        );
        mReferrerHolder = referrerHolder;

        this.referrerManager = referrerManager;
        DebugLogger.INSTANCE.info(TAG, "Subscribe on referrer updates from commutation dispatcher component");
        mStartupCenter.registerStartupListener(mComponentId, this);
    }

    private void updateAdvIdentifiersTracking(@NonNull CommonArguments clientConfiguration) {
        boolean advIdentifiersTrackingStatus =
            !BooleanUtils.isFalse(clientConfiguration.componentArguments.advIdentifiersTrackingEnabled);
        DebugLogger.INSTANCE.info(
            TAG,
            "Update adv identifiers tracking status: %s (value from arguments: %s)",
            advIdentifiersTrackingStatus,
            clientConfiguration.componentArguments.advIdentifiersTrackingEnabled
        );
        GlobalServiceLocator.getInstance().getAdvertisingIdGetter().updateStateFromClientConfig(
            advIdentifiersTrackingStatus
        );
    }

    @Override
    public void updateSdkConfig(@NonNull CommonArguments.ReporterArguments clientConfiguration) {
        mReporterArgumentsHolder.updateArguments(clientConfiguration);
    }

    @NonNull
    @Override
    public CounterConfigurationReporterType getReporterType() {
        return CounterConfigurationReporterType.COMMUTATION;
    }

    public synchronized void connectClient(@NonNull CommutationClientUnit clientUnit) {
        mLifecycleManager.connectClient(clientUnit);
        DebugLogger.INSTANCE.info(
            TAG,
            "add client: %s. Clients count %d",
            mComponentId,
            mLifecycleManager.getConnectedClients().size()
        );
        notifyListener(
                clientUnit,
                StartupUtils.decodeClids(mStartupUnit.getStartupState().getLastClientClidsForStartupRequest())
        );
    }

    public synchronized void disconnectClient(@NonNull CommutationClientUnit clientUnit) {
        mLifecycleManager.disconnectClient(clientUnit);
        DebugLogger.INSTANCE.info(
            TAG,
            "remove client: %s. Clients count %d",
            mComponentId,
            mLifecycleManager.getConnectedClients().size()
        );
    }

    public void handleReport(@NonNull CounterReport reportData, @NonNull CommutationClientUnit clientUnit) {
        DebugLogger.INSTANCE.info(
            TAG,
            "handle report for componentId: %s; data: %s",
            mComponentId,
            reportData
        );
        mReportProcessor.process(reportData, clientUnit);
    }

    @NonNull
    public CommonArguments.ReporterArguments getConfiguration() {
        return mReporterArgumentsHolder.getArguments();
    }

    @NonNull
    @Override
    public ComponentId getComponentId() {
        return mComponentId;
    }

    @Override
    public void onStartupChanged(@NonNull StartupState newState) {
        notifyStartupUpdated(newState);
    }

    @Override
    public void onStartupError(@NonNull StartupError startupError, @Nullable StartupState existingState) {
        DebugLogger.INSTANCE.info(TAG, "error %s for component %s", startupError, mComponentId);
        synchronized (mStartupLock) {
            for (IdentifiersData identifiersData : mStartupEventReceivers) {
                DataResultReceiver.notifyOnStartupError(
                        identifiersData.getResultReceiver(),
                        startupError,
                        mClientIdentifiersProvider
                                .createClientIdentifiersHolder(identifiersData.getClidsFromClientForVerification())
                );
            }
            mStartupEventReceivers.clear();
        }
    }

    private void notifyStartupUpdated(@NonNull StartupState state) {
        DebugLogger.INSTANCE.info(TAG, "startupUpdated for component %s", mComponentId);
        synchronized (mStartupLock) {
            for (ClientIdentifiersChangedListener listener : mLifecycleManager.getConnectedClients()) {
                notifyListener(listener, StartupUtils.decodeClids(state.getLastClientClidsForStartupRequest()));
            }
            List<IdentifiersData> notNotifiedCallbacks = new ArrayList<IdentifiersData>();
            for (IdentifiersData identifiersData : mStartupEventReceivers) {
                if (identifiersData.isStartupConsistent(state)) {
                    sendStartupToClient(
                            identifiersData.getResultReceiver(),
                            identifiersData.getClidsFromClientForVerification()
                    );
                } else {
                    notNotifiedCallbacks.add(identifiersData);
                }
            }
            mStartupEventReceivers = new ArrayList<IdentifiersData>(notNotifiedCallbacks);
            if (notNotifiedCallbacks.isEmpty() == false) {
                mTaskProcessor.flushAllTasks();
            }

        }
    }

    private void notifyListener(@NonNull ClientIdentifiersChangedListener listener,
                                @Nullable Map<String, String> clientClids) {
        ClientIdentifiersHolder clientIdentifiersHolder = mClientIdentifiersProvider
                .createClientIdentifiersHolder(clientClids);
        DebugLogger.INSTANCE.info(
            TAG,
            "Notify listener: %s with client identifiers: %s",
            listener,
            clientIdentifiersHolder
        );
        listener.onClientIdentifiersChanged(clientIdentifiersHolder);
    }

    @NonNull
    @Override
    public Context getContext() {
        return mContext;
    }

    public void provokeStartupOrGetCurrentState(@Nullable IdentifiersData identifiersData) {
        List<String> identifiers = null;
        ResultReceiver resultReceiver = null;
        Map<String, String> clidsForVerification = new HashMap<String, String>();
        if (identifiersData != null) {
            identifiers = identifiersData.getIdentifiersList();
            resultReceiver = identifiersData.getResultReceiver();
            clidsForVerification = identifiersData.getClidsFromClientForVerification();
        }
        boolean startupRequiredForIdentifiers = mStartupUnit.isStartupRequired(identifiers, clidsForVerification);
        DebugLogger.INSTANCE.info(
            TAG,
            "startupRequiredForIdentifiers %s is %b",
            identifiers,
            startupRequiredForIdentifiers
        );
        if (startupRequiredForIdentifiers == false) {
            sendStartupToClient(resultReceiver, clidsForVerification);
        }
        if (mStartupUnit.isStartupRequired()) {
            DebugLogger.INSTANCE.info(TAG, "Startup is required.");
            synchronized (mStartupLock) {
                if (startupRequiredForIdentifiers && identifiersData != null) {
                    mStartupEventReceivers.add(identifiersData);
                }
            }
            //todo (avitenko) implicitly sends startup https://nda.ya.ru/t/s1pSFENn6Njj6K
            mTaskProcessor.flushAllTasks();
        // todo(ddzina) there was no if, just else. Maybe it was some workaround to deliver fresh startup to client,
            //  but it seems that there was a change that client would be notifies twice with same startup,
            //  so additional if was placed here in 3a6a5c36cd2
        } else if (startupRequiredForIdentifiers) {
            sendStartupToClient(resultReceiver, clidsForVerification);
        }
    }

    public void requestReferrer(@Nullable final ResultReceiver receiver) {
        DebugLogger.INSTANCE.info(TAG, "Request referrer. Receiver: %s", receiver);
        referrerManager.addOneShotListener(new ReferrerChosenListener() {
            @Override
            public void onReferrerChosen(@Nullable ReferrerInfo referrerInfo) {
                ReferrerResultReceiver.sendReferrer(receiver, referrerInfo);
            }
        });
    }

    @NonNull
    public ReferrerHolder getReferrerHolder() {
        return mReferrerHolder;
    }

    @NonNull
    public ClientIdentifiersProvider getClientIdentifiersProvider() {
        return mClientIdentifiersProvider;
    }

    @Override
    public void updateConfig(@NonNull CommonArguments arguments) {
        mStartupUnit.updateConfiguration(arguments.startupArguments);
        updateSdkConfig(arguments.componentArguments);
    }

    private void sendStartupToClient(@Nullable ResultReceiver resultReceiver,
                                     @Nullable Map<String, String> clientClids) {
        DataResultReceiver.notifyOnStartupUpdated(
                resultReceiver,
                mClientIdentifiersProvider.createClientIdentifiersHolder(clientClids)
        );
    }
}

