package io.appmetrica.analytics.impl;

import android.annotation.SuppressLint;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.coreapi.internal.data.ProtobufStateStorage;
import io.appmetrica.analytics.coreapi.internal.identifiers.PlatformIdentifiers;
import io.appmetrica.analytics.coreapi.internal.servicecomponents.FirstExecutionConditionService;
import io.appmetrica.analytics.coreapi.internal.system.PermissionExtractor;
import io.appmetrica.analytics.coreutils.internal.services.UtilityServiceProvider;
import io.appmetrica.analytics.coreutils.internal.services.WaitForActivationDelayBarrier;
import io.appmetrica.analytics.impl.clids.ClidsCandidatesHelper;
import io.appmetrica.analytics.impl.clids.ClidsDataAwaiter;
import io.appmetrica.analytics.impl.clids.ClidsInfo;
import io.appmetrica.analytics.impl.clids.ClidsPriorityProvider;
import io.appmetrica.analytics.impl.clids.ClidsSatelliteCheckedProvider;
import io.appmetrica.analytics.impl.clids.ClidsStateProvider;
import io.appmetrica.analytics.impl.clids.SatelliteClidsInfoProvider;
import io.appmetrica.analytics.impl.crash.ndk.NativeCrashService;
import io.appmetrica.analytics.impl.db.VitalDataProviderStorage;
import io.appmetrica.analytics.impl.db.preferences.PreferencesServiceDbStorage;
import io.appmetrica.analytics.impl.db.state.factory.StorageFactory;
import io.appmetrica.analytics.impl.db.storage.DatabaseStorageFactory;
import io.appmetrica.analytics.impl.id.AdvertisingIdGetter;
import io.appmetrica.analytics.impl.id.AppSetIdGetter;
import io.appmetrica.analytics.impl.location.LocationApi;
import io.appmetrica.analytics.impl.location.LocationApiProvider;
import io.appmetrica.analytics.impl.location.LocationClientApi;
import io.appmetrica.analytics.impl.modules.ModuleEntryPointsRegister;
import io.appmetrica.analytics.impl.modules.ModuleEventHandlersHolder;
import io.appmetrica.analytics.impl.modules.service.ServiceModulesController;
import io.appmetrica.analytics.impl.network.http.BaseSslSocketFactoryProvider;
import io.appmetrica.analytics.impl.network.http.SslSocketFactoryProviderImpl;
import io.appmetrica.analytics.impl.permissions.SimplePermissionExtractor;
import io.appmetrica.analytics.impl.preloadinfo.PreloadInfoCandidatesHelper;
import io.appmetrica.analytics.impl.preloadinfo.PreloadInfoData;
import io.appmetrica.analytics.impl.preloadinfo.PreloadInfoDataAwaiter;
import io.appmetrica.analytics.impl.preloadinfo.PreloadInfoFromSatelliteProvider;
import io.appmetrica.analytics.impl.preloadinfo.PreloadInfoPriorityProvider;
import io.appmetrica.analytics.impl.preloadinfo.PreloadInfoSatelliteCheckedProvider;
import io.appmetrica.analytics.impl.preloadinfo.PreloadInfoStateProvider;
import io.appmetrica.analytics.impl.referrer.service.ReferrerHolder;
import io.appmetrica.analytics.impl.service.ServiceDataReporterHolder;
import io.appmetrica.analytics.impl.startup.uuid.MultiProcessSafeUuidProvider;
import io.appmetrica.analytics.impl.startup.uuid.UuidFromStartupStateImporter;
import io.appmetrica.analytics.impl.telephony.TelephonyDataProvider;
import io.appmetrica.analytics.impl.utils.executors.ServiceExecutorProvider;
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger;
import io.appmetrica.analytics.modulesapi.internal.service.LocationServiceApi;
import io.appmetrica.analytics.networktasks.internal.NetworkCore;
import io.appmetrica.analytics.networktasks.internal.NetworkServiceLocator;

public final class GlobalServiceLocator {

    private static final String TAG = "[GlobalServiceLocator]";

    @SuppressLint("StaticFieldLeak")
    private volatile static GlobalServiceLocator sHolder;

    public static void init(@NonNull Context context) {
        if (sHolder == null) {
            synchronized (GlobalServiceLocator.class) {
                if (sHolder == null) {
                    sHolder = new GlobalServiceLocator(context.getApplicationContext());
                }
            }
        }
    }

    public static GlobalServiceLocator getInstance() {
        return sHolder;
    }

    @NonNull
    private final Context mContext;
    private volatile ReferrerHolder mReferrerHolder;
    @Nullable
    private volatile DataSendingRestrictionControllerImpl dataSendingRestrictionController;
    @NonNull
    private final ServiceExecutorProvider mServiceExecutorProvider;
    @Nullable
    private volatile BatteryInfoProvider batteryInfoProvider;
    @Nullable
    private volatile SelfDiagnosticReporterStorage mSelfDiagnosticReporterStorage;
    @Nullable
    private volatile AdvertisingIdGetter serviceInternalAdvertisingIdGetter;
    @Nullable
    private volatile AppSetIdGetter appSetIdGetter;
    @Nullable
    private volatile PlatformIdentifiers platformIdentifiers;
    @Nullable
    private volatile PreloadInfoStorage mPreloadInfoStorage;
    @Nullable
    private volatile ClidsInfoStorage clidsStorage;
    @Nullable
    private volatile PreferencesServiceDbStorage servicePreferences;
    @Nullable
    private volatile VitalDataProviderStorage vitalDataProviderStorage;
    @Nullable
    private volatile SdkEnvironmentHolder sdkEnvironmentHolder;
    @Nullable
    private volatile LifecycleDependentComponentManager lifecycleDependentComponentManager;
    @Nullable
    private BaseSslSocketFactoryProvider sslSocketFactoryProvider;
    @NonNull
    private final ServiceLifecycleObserver networkServiceLifecycleObserver = new ServiceLifecycleObserver() {
        @Override
        public void onCreate() {
            NetworkServiceLocator.getInstance().onCreate();
        }

        @Override
        public void onDestroy() {
            NetworkServiceLocator.getInstance().onDestroy();
        }
    };
    @Nullable
    private volatile ServiceModulesController modulesController;
    @NonNull
    private final ModuleEntryPointsRegister moduleEntryPointsRegister = new ModuleEntryPointsRegister();
    @NonNull
    private final ModuleEventHandlersHolder moduleEventHandlersHolder = new ModuleEventHandlersHolder();
    @NonNull
    private final StartupStateHolder startupStateHolder = new StartupStateHolder();
    @NonNull
    private final ServiceDataReporterHolder serviceDataReporterHolder = new ServiceDataReporterHolder();
    @Nullable
    private volatile LocationApi locationApi;
    @Nullable
    private volatile TelephonyDataProvider telephonyDataProvider;
    @Nullable
    private volatile PermissionExtractor generalPermissionExtractor;
    @Nullable
    private volatile MultiProcessSafeUuidProvider multiProcessSafeUuidProvider;
    @NonNull
    private final NativeCrashService nativeCrashService = new NativeCrashService();
    @NonNull
    private final UtilityServiceProvider utilityServiceProvider = new UtilityServiceProvider();

    private GlobalServiceLocator(@NonNull Context applicationContext) {
        mContext = applicationContext;
        mServiceExecutorProvider = new ServiceExecutorProvider();
    }

    public synchronized void initAsync() {
        DebugLogger.INSTANCE.info(TAG, "Init async");
        utilityServiceProvider.initAsync();
        startupStateHolder.init(mContext);
        startupStateHolder.registerObserver(new UtilityServiceStartupStateObserver(utilityServiceProvider));
        NetworkServiceLocator.init();
        getLifecycleDependentComponentManager().addLifecycleObserver(networkServiceLifecycleObserver);
        initPreloadInfoStorage();
    }

    @NonNull
    public Context getContext() {
        return mContext;
    }

    @NonNull
    public ReferrerHolder getReferrerHolder() {
        if (mReferrerHolder == null) {
            synchronized (this) {
                if (mReferrerHolder == null) {
                    mReferrerHolder = new ReferrerHolder(mContext);
                }
            }
        }
        return mReferrerHolder;
    }

    @NonNull
    public DataSendingRestrictionControllerImpl getDataSendingRestrictionController() {
        if (dataSendingRestrictionController == null) {
            synchronized (this) {
                if (dataSendingRestrictionController == null) {
                    dataSendingRestrictionController = new DataSendingRestrictionControllerImpl(
                            new DataSendingRestrictionControllerImpl.StorageImpl(getServicePreferences())
                    );
                }
            }
        }
        return dataSendingRestrictionController;
    }

    @NonNull
    public ServiceExecutorProvider getServiceExecutorProvider() {
        return mServiceExecutorProvider;
    }

    @NonNull
    public AppSetIdGetter getAppSetIdGetter() {
        AppSetIdGetter local = appSetIdGetter;
        if (local == null) {
            synchronized (this) {
                local = appSetIdGetter;
                if (local == null) {
                    local = new AppSetIdGetter(mContext);
                    appSetIdGetter = local;
                }
            }
        }
        return local;
    }

    @NonNull
    public AdvertisingIdGetter getServiceInternalAdvertisingIdGetter() {
        AdvertisingIdGetter local = serviceInternalAdvertisingIdGetter;
        if (local == null) {
            synchronized (this) {
                local = serviceInternalAdvertisingIdGetter;
                if (local == null) {
                    local = new AdvertisingIdGetter(
                        new AdvertisingIdGetter.ServiceInternalGaidRestrictionProvider(),
                        new AdvertisingIdGetter.InternalHoaidRestrictionProvider(),
                        new AdvertisingIdGetter.AlwaysAllowedRestrictionsProvider(),
                        getServiceExecutorProvider().getDefaultExecutor(),
                        "ServiceInternal"
                    );
                    startupStateHolder.registerObserver(local);
                    serviceInternalAdvertisingIdGetter = local;
                }
            }
        }
        return local;
    }

    @NonNull
    public PlatformIdentifiers getPlatformIdentifiers() {
        PlatformIdentifiers local = platformIdentifiers;
        if (local == null) {
            synchronized (this) {
                local = platformIdentifiers;
                if (local == null) {
                    local = new PlatformIdentifiers(getServiceInternalAdvertisingIdGetter(), getAppSetIdGetter());
                    platformIdentifiers = local;
                }
            }
        }
        return local;
    }

    @NonNull
    public BatteryInfoProvider getBatteryInfoProvider() {
        BatteryInfoProvider local = batteryInfoProvider;
        if (local == null) {
            synchronized (this) {
                local = batteryInfoProvider;
                if (local == null) {
                    local = new BatteryInfoProvider(
                        getServiceExecutorProvider().getDefaultExecutor(),
                        getLifecycleDependentComponentManager().getBatteryChargeTypeListener()
                    );
                    batteryInfoProvider = local;
                }
            }
        }
        return local;
    }

    @NonNull
    public PreloadInfoStorage getPreloadInfoStorage() {
        initPreloadInfoStorage();
        return mPreloadInfoStorage;
    }

    @NonNull
    public ClidsInfoStorage getClidsStorage() {
        if (clidsStorage == null) {
            synchronized (this) {
                if (clidsStorage == null) {
                    ProtobufStateStorage<ClidsInfo> storage =
                            StorageFactory.Provider.get(ClidsInfo.class).create(mContext);
                    clidsStorage = new ClidsInfoStorage(
                            mContext,
                            storage,
                            new ClidsPriorityProvider(),
                            new ClidsCandidatesHelper(),
                            new ClidsStateProvider(),
                            new SatelliteClidsInfoProvider(mContext),
                            new ClidsSatelliteCheckedProvider(getServicePreferences()),
                            new ClidsDataAwaiter(),
                            storage.read(),
                            "[ClidsInfoStorage]"
                    );
                }
            }
        }
        return clidsStorage;
    }

    @NonNull
    public PreferencesServiceDbStorage getServicePreferences() {
        if (servicePreferences == null) {
            synchronized (this) {
                if (servicePreferences == null) {
                    servicePreferences = new PreferencesServiceDbStorage(
                            DatabaseStorageFactory.getInstance(mContext)
                                    .getPreferencesDbHelperForService()
                    );
                }
            }
        }
        return servicePreferences;
    }

    @Nullable
    public synchronized SelfDiagnosticReporterStorage getSelfDiagnosticReporterStorage() {
        return mSelfDiagnosticReporterStorage;
    }

    public synchronized void initSelfDiagnosticReporterStorage(@NonNull SelfProcessReporter selfProcessReporter) {
        mSelfDiagnosticReporterStorage = new SelfDiagnosticReporterStorage(mContext, selfProcessReporter);
    }

    @NonNull
    public LifecycleDependentComponentManager getLifecycleDependentComponentManager() {
        LifecycleDependentComponentManager localCopy = lifecycleDependentComponentManager;
        if (localCopy == null) {
            synchronized (this) {
                localCopy = lifecycleDependentComponentManager;
                if (localCopy == null) {
                    localCopy = new LifecycleDependentComponentManager(
                        getContext(),
                        getServiceExecutorProvider().getDefaultExecutor()
                    );
                    lifecycleDependentComponentManager = localCopy;
                }
            }
        }
        return localCopy;
    }

    @NonNull
    public synchronized VitalDataProviderStorage getVitalDataProviderStorage() {
        if (vitalDataProviderStorage == null) {
            vitalDataProviderStorage = new VitalDataProviderStorage(mContext);
        }
        return vitalDataProviderStorage;
    }

    @NonNull
    public synchronized BaseSslSocketFactoryProvider getSslSocketFactoryProvider() {
        if (sslSocketFactoryProvider == null) {
            sslSocketFactoryProvider = new SslSocketFactoryProviderImpl(mContext);
            startupStateHolder.registerObserver(sslSocketFactoryProvider);
        }
        return sslSocketFactoryProvider;
    }

    public synchronized void setSslSocketFactoryProvider(
        @NonNull BaseSslSocketFactoryProvider sslSocketFactoryProvider
    ) {
        if (this.sslSocketFactoryProvider != null) {
            startupStateHolder.removeObserver(this.sslSocketFactoryProvider);
        }
        this.sslSocketFactoryProvider = sslSocketFactoryProvider;
        startupStateHolder.registerObserver(sslSocketFactoryProvider);
    }

    @NonNull
    public ApplicationStateProviderImpl getApplicationStateProvider() {
        return getLifecycleDependentComponentManager().getApplicationStateProvider();
    }

    @NonNull
    public SdkEnvironmentHolder getSdkEnvironmentHolder() {
        SdkEnvironmentHolder local = sdkEnvironmentHolder;
        if (local == null) {
            synchronized (this) {
                local = sdkEnvironmentHolder;
                if (local == null) {
                    local = new SdkEnvironmentHolder(getContext());
                    sdkEnvironmentHolder = local;
                }
            }
        }
        return local;
    }

    @NonNull
    public ModuleEntryPointsRegister getModuleEntryPointsRegister() {
        return moduleEntryPointsRegister;
    }

    @NonNull
    public ServiceModulesController getModulesController() {
        ServiceModulesController local = modulesController;
        if (local == null) {
            synchronized (this) {
                local = modulesController;
                if (local == null) {
                    local = new ServiceModulesController();
                    modulesController = local;
                }
            }
        }
        return local;
    }

    @NonNull
    public LocationClientApi getLocationClientApi() {
        return getLocationApi();
    }

    @NonNull
    public LocationServiceApi getLocationServiceApi() {
        return getLocationApi();
    }

    @NonNull
    private LocationApi getLocationApi() {
        LocationApi local = locationApi;
        if (local == null) {
            synchronized (this) {
                local = locationApi;
                if (local == null) {
                    local = new LocationApiProvider().getLocationApi(mContext);
                    locationApi = local;
                }
            }
        }
        return local;
    }

    @NonNull
    public TelephonyDataProvider getTelephonyDataProvider() {
        TelephonyDataProvider local = telephonyDataProvider;
        if (local == null) {
            synchronized (this) {
                local = telephonyDataProvider;
                if (local == null) {
                    local = new TelephonyDataProvider(mContext);
                    telephonyDataProvider = local;
                }
            }
        }
        return local;
    }

    private void initPreloadInfoStorage() {
        if (mPreloadInfoStorage == null) {
            synchronized (this) {
                if (mPreloadInfoStorage == null) {
                    ProtobufStateStorage<PreloadInfoData> storage =
                            StorageFactory.Provider.get(PreloadInfoData.class).create(mContext);
                    PreloadInfoData stateFromDisk = storage.read();
                    mPreloadInfoStorage = new PreloadInfoStorage(
                            mContext,
                            storage,
                            new PreloadInfoPriorityProvider(),
                            new PreloadInfoCandidatesHelper(stateFromDisk),
                            new PreloadInfoStateProvider(),
                            new PreloadInfoFromSatelliteProvider(mContext),
                            new PreloadInfoSatelliteCheckedProvider(),
                            new PreloadInfoDataAwaiter(),
                            stateFromDisk,
                            "[PreloadInfoStorage]"
                    );
                }
            }
        }
    }

    @NonNull
    public StartupStateHolder getStartupStateHolder() {
        return startupStateHolder;
    }

    @NonNull
    public ServiceDataReporterHolder getServiceDataReporterHolder() {
        return serviceDataReporterHolder;
    }

    @NonNull
    public ModuleEventHandlersHolder getModuleEventHandlersHolder() {
        return moduleEventHandlersHolder;
    }

    @NonNull
    public PermissionExtractor getGeneralPermissionExtractor() {
        PermissionExtractor local = generalPermissionExtractor;
        if (local != null) {
            return local;
        }
        synchronized (this) {
            local = generalPermissionExtractor;
            if (local != null) {
                return local;
            }
            local = new SimplePermissionExtractor(getModulesController().getAskForPermissionStrategy());
            generalPermissionExtractor = local;
            return local;
        }
    }

    @NonNull
    public MultiProcessSafeUuidProvider getMultiProcessSafeUuidProvider() {
        MultiProcessSafeUuidProvider localCopy = multiProcessSafeUuidProvider;
        if (localCopy == null) {
            synchronized (this) {
                localCopy = multiProcessSafeUuidProvider;
                if (localCopy == null) {
                    localCopy = new MultiProcessSafeUuidProvider(mContext, new UuidFromStartupStateImporter());
                    multiProcessSafeUuidProvider = localCopy;
                }
            }
        }
        return localCopy;
    }

    @NonNull
    public NativeCrashService getNativeCrashService() {
        return nativeCrashService;
    }

    @NonNull
    public NetworkCore getNetworkCore() {
        return NetworkServiceLocator.getInstance().getNetworkCore();
    }

    @NonNull
    public FirstExecutionConditionService getFirstExecutionConditionService() {
        return utilityServiceProvider.getFirstExecutionService();
    }

    @NonNull
    public WaitForActivationDelayBarrier getActivationBarrier() {
        return utilityServiceProvider.getActivationBarrier();
    }

    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    public static synchronized void destroy() {
        GlobalServiceLocator instance = GlobalServiceLocator.getInstance();
        if (instance != null) {
            if (instance.mServiceExecutorProvider != null) {
                GlobalServiceLocator.getInstance().mServiceExecutorProvider.destroy();
            }
        }
        sHolder = null;
    }

    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    public void setBatteryInfoProvider(@Nullable BatteryInfoProvider batteryInfoProvider) {
        this.batteryInfoProvider = batteryInfoProvider;
    }

    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    public void setLifecycleDependentComponentManager(
            @NonNull LifecycleDependentComponentManager lifecycleDependentComponentManager
    ) {
        this.lifecycleDependentComponentManager = lifecycleDependentComponentManager;
    }

    @VisibleForTesting
    public void setModulesController(@NonNull ServiceModulesController modulesController) {
        this.modulesController = modulesController;
    }

    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    public static void setInstance(@NonNull GlobalServiceLocator instance) {
        sHolder = instance;
    }
}
