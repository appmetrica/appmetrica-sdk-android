package io.appmetrica.analytics.impl;

import android.annotation.SuppressLint;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.coreapi.internal.data.ProtobufStateStorage;
import io.appmetrica.analytics.coreapi.internal.system.PermissionExtractor;
import io.appmetrica.analytics.coreutils.internal.logger.YLogger;
import io.appmetrica.analytics.coreutils.internal.services.UtilityServiceLocator;
import io.appmetrica.analytics.impl.clids.ClidsCandidatesHelper;
import io.appmetrica.analytics.impl.clids.ClidsDataAwaiter;
import io.appmetrica.analytics.impl.clids.ClidsInfo;
import io.appmetrica.analytics.impl.clids.ClidsPriorityProvider;
import io.appmetrica.analytics.impl.clids.ClidsSatelliteCheckedProvider;
import io.appmetrica.analytics.impl.clids.ClidsStateProvider;
import io.appmetrica.analytics.impl.db.VitalDataProviderStorage;
import io.appmetrica.analytics.impl.db.preferences.PreferencesServiceDbStorage;
import io.appmetrica.analytics.impl.db.state.factory.StorageFactory;
import io.appmetrica.analytics.impl.db.storage.DatabaseStorageFactory;
import io.appmetrica.analytics.impl.id.AdvertisingIdGetter;
import io.appmetrica.analytics.impl.id.AppSetIdGetter;
import io.appmetrica.analytics.impl.location.LocationApi;
import io.appmetrica.analytics.impl.location.LocationApiProvider;
import io.appmetrica.analytics.impl.location.LocationClientApi;
import io.appmetrica.analytics.impl.modules.ModuleEventHandlersHolder;
import io.appmetrica.analytics.impl.modules.ModulesController;
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
import io.appmetrica.analytics.modulesapi.internal.LocationServiceApi;
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
    private volatile StatisticsRestrictionControllerImpl mStatisticsRestrictionController;
    @NonNull
    private final ServiceExecutorProvider mServiceExecutorProvider;
    @NonNull
    private volatile BatteryInfoProvider mBatteryInfoProvider;
    @Nullable
    private volatile SelfDiagnosticReporterStorage mSelfDiagnosticReporterStorage;
    @Nullable
    private volatile AdvertisingIdGetter mServiceInternalAdvertisingIdGetter;
    @Nullable
    private volatile AppSetIdGetter appSetIdGetter;
    @Nullable
    private volatile PreloadInfoStorage mPreloadInfoStorage;
    @Nullable
    private volatile ClidsInfoStorage clidsStorage;
    @Nullable
    private volatile PreferencesServiceDbStorage servicePreferences;
    @Nullable
    private volatile VitalDataProviderStorage vitalDataProviderStorage;
    @NonNull
    private ScreenInfoHolder screenInfoHolder;
    @NonNull
    private LifecycleDependentComponentManager lifecycleDependentComponentManager;
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
    @NonNull
    private ModulesController modulesController;
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

    private GlobalServiceLocator(@NonNull Context applicationContext) {
        mContext = applicationContext;
        mServiceExecutorProvider = new ServiceExecutorProvider();
        lifecycleDependentComponentManager = new LifecycleDependentComponentManager(
                applicationContext,
                mServiceExecutorProvider.getDefaultExecutor()
        );
        mBatteryInfoProvider = new BatteryInfoProvider(
                mServiceExecutorProvider.getDefaultExecutor(),
                lifecycleDependentComponentManager.getBatteryChargeTypeListener()
        );
        screenInfoHolder = new ScreenInfoHolder();
        modulesController = new ModulesController();
        NetworkServiceLocator.init();
    }

    public synchronized void initAsync() {
        YLogger.info(TAG, "Init async");
        UtilityServiceLocator.getInstance().initAsync();
        startupStateHolder.init(mContext);
        startupStateHolder.registerObserver(new UtilityServiceStartupStateObserver());
        NetworkServiceLocator.getInstance().initAsync(new NetworkAppContextProvider().getNetworkAppContext());
        lifecycleDependentComponentManager.addLifecycleObserver(networkServiceLifecycleObserver);
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
    public StatisticsRestrictionControllerImpl getStatisticsRestrictionController() {
        if (mStatisticsRestrictionController == null) {
            synchronized (this) {
                if (mStatisticsRestrictionController == null) {
                    mStatisticsRestrictionController = new StatisticsRestrictionControllerImpl(
                            new StatisticsRestrictionControllerImpl.StorageImpl(getServicePreferences())
                    );
                }
            }
        }
        return mStatisticsRestrictionController;
    }

    @NonNull
    public ServiceExecutorProvider getServiceExecutorProvider() {
        return mServiceExecutorProvider;
    }

    @NonNull
    public AdvertisingIdGetter getServiceInternalAdvertisingIdGetter() {
        if (mServiceInternalAdvertisingIdGetter == null) {
            synchronized (this) {
                if (mServiceInternalAdvertisingIdGetter == null) {
                    mServiceInternalAdvertisingIdGetter = new AdvertisingIdGetter(
                            new AdvertisingIdGetter.ServiceInternalGaidRestrictionProvider(),
                            new AdvertisingIdGetter.InternalHoaidRestrictionProvider(),
                            new AdvertisingIdGetter.AlwaysAllowedRestrictionsProvider(),
                            getServiceExecutorProvider().getDefaultExecutor(),
                            "ServiceInternal"
                    );
                    startupStateHolder.registerObserver(mServiceInternalAdvertisingIdGetter);
                }
            }
        }
        return mServiceInternalAdvertisingIdGetter;
    }

    @NonNull
    public AppSetIdGetter getAppSetIdGetter() {
        if (appSetIdGetter == null) {
            synchronized (this) {
                if (appSetIdGetter == null) {
                    appSetIdGetter = new AppSetIdGetter(mContext);
                }
            }
        }
        return appSetIdGetter;
    }

    @NonNull
    public BatteryInfoProvider getBatteryInfoProvider() {
        return mBatteryInfoProvider;
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
        return lifecycleDependentComponentManager;
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
        return lifecycleDependentComponentManager.getApplicationStateProvider();
    }

    @NonNull
    public ScreenInfoHolder getScreenInfoHolder() {
        return screenInfoHolder;
    }

    @NonNull
    public ModuleEntryPointsRegister getModuleEntryPointsRegister() {
        return moduleEntryPointsRegister;
    }

    @NonNull
    public ModulesController getModulesController() {
        return modulesController;
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
        mBatteryInfoProvider = batteryInfoProvider;
    }

    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    public void setAdvertisingIdGetter(@NonNull AdvertisingIdGetter getter) {
        mServiceInternalAdvertisingIdGetter = getter;
    }

    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    public void setLifecycleDependentComponentManager(
            @NonNull LifecycleDependentComponentManager lifecycleDependentComponentManager
    ) {
        this.lifecycleDependentComponentManager = lifecycleDependentComponentManager;
    }

    @VisibleForTesting
    public void setModulesController(@NonNull ModulesController modulesController) {
        this.modulesController = modulesController;
    }

    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    public static void setInstance(@NonNull GlobalServiceLocator instance) {
        sHolder = instance;
    }
}
