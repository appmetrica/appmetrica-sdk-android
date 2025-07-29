package io.appmetrica.analytics.impl;

import android.annotation.SuppressLint;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.impl.crash.jvm.client.TechnicalCrashProcessorFactory;
import io.appmetrica.analytics.impl.db.preferences.PreferencesClientDbStorage;
import io.appmetrica.analytics.impl.db.storage.DatabaseStorageFactory;
import io.appmetrica.analytics.impl.modules.ModuleEntryPointsRegister;
import io.appmetrica.analytics.impl.modules.client.ClientModulesController;
import io.appmetrica.analytics.impl.proxy.AppMetricaFacadeProvider;
import io.appmetrica.analytics.impl.reporter.ReporterLifecycleListener;
import io.appmetrica.analytics.impl.startup.uuid.MultiProcessSafeUuidProvider;
import io.appmetrica.analytics.impl.startup.uuid.UuidFromClientPreferencesImporter;
import io.appmetrica.analytics.impl.utils.FirstLaunchDetector;
import io.appmetrica.analytics.impl.utils.AppMetricaServiceProcessDetector;
import io.appmetrica.analytics.impl.utils.process.CurrentProcessDetector;
import io.appmetrica.analytics.impl.utils.executors.ClientExecutorProvider;
import io.appmetrica.analytics.impl.utils.process.ProcessNameProvider;

public class ClientServiceLocator {

    @SuppressLint("StaticFieldLeak")
    private volatile static ClientServiceLocator sHolder;

    public static void init() {
        if (sHolder == null) {
            synchronized (ClientServiceLocator.class) {
                if (sHolder == null) {
                    sHolder = new ClientServiceLocator();
                }
            }
        }
    }

    public static ClientServiceLocator getInstance() {
        init();
        return sHolder;
    }

    @NonNull
    private final CurrentProcessDetector currentProcessDetector;
    @NonNull
    private final DefaultOneShotMetricaConfig defaultOneShotConfig;
    @NonNull
    private final ClientExecutorProvider clientExecutorProvider;
    @NonNull
    private final AppMetricaServiceDelayHandler appMetricaServiceDelayHandler;
    @NonNull
    private final ActivityLifecycleManager activityLifecycleManager;
    @NonNull
    private final SessionsTrackingManager sessionsTrackingManager;
    @NonNull
    private final ContextAppearedListener contextAppearedListener;
    @NonNull
    private final ActivityAppearedListener activityAppearedListener;
    @Nullable
    private volatile ReporterLifecycleListener reporterLifecycleListener;
    @NonNull
    private final TechnicalCrashProcessorFactory crashProcessorFactory;
    @Nullable
    private MultiProcessSafeUuidProvider multiProcessSafeUuidProvider;
    @NonNull
    private final AppMetricaCoreComponentsProvider appMetricaCoreComponentsProvider;
    @Nullable
    private volatile ClientModulesController modulesController;
    @NonNull
    private final ModuleEntryPointsRegister moduleEntryPointsRegister = new ModuleEntryPointsRegister();
    @Nullable
    private volatile PreferencesClientDbStorage preferencesClientDbStorage;
    @Nullable
    private ScreenInfoRetriever screenInfoRetriever;
    @NonNull
    private final AppMetricaFacadeProvider appMetricaFacadeProvider = new AppMetricaFacadeProvider();
    @NonNull
    private final AppMetricaServiceProcessDetector appMetricaServiceProcessDetector =
        new AppMetricaServiceProcessDetector();
    @NonNull
    private final FirstLaunchDetector firstLaunchDetector = new FirstLaunchDetector();
    @NonNull
    private final ClientConfigSerializer clientConfigSerializer = new ClientConfigSerializer();
    @Nullable
    private volatile AnonymousClientActivator anonymousClientActivator;
    @Nullable
    private volatile ExtraMetaInfoRetriever extraMetaInfoRetriever;

    private ClientServiceLocator() {
        this(new CurrentProcessDetector(), new ActivityLifecycleManager(), new ClientExecutorProvider());
    }

    private ClientServiceLocator(
        @NonNull CurrentProcessDetector currentProcessDetector,
        @NonNull ActivityLifecycleManager activityLifecycleManager,
        @NonNull ClientExecutorProvider clientExecutorProvider
    ) {
        this(
            currentProcessDetector,
            activityLifecycleManager,
            clientExecutorProvider,
            new ActivityAppearedListener(activityLifecycleManager)
        );
    }

    private ClientServiceLocator(
        @NonNull CurrentProcessDetector currentProcessDetector,
        @NonNull ActivityLifecycleManager activityLifecycleManager,
        @NonNull ClientExecutorProvider clientExecutorProvider,
        @NonNull ActivityAppearedListener activityAppearedListener
    ) {
        this(
            currentProcessDetector,
            new DefaultOneShotMetricaConfig(),
            clientExecutorProvider,
            activityAppearedListener,
            new AppMetricaServiceDelayHandler(currentProcessDetector),
            activityLifecycleManager,
            new SessionsTrackingManager(
                activityLifecycleManager,
                activityAppearedListener
            ),
            new ContextAppearedListener(activityLifecycleManager),
            new TechnicalCrashProcessorFactory(),
            new AppMetricaCoreComponentsProvider()
        );
    }

    @VisibleForTesting
    ClientServiceLocator(
        @NonNull CurrentProcessDetector currentProcessDetector,
        @NonNull DefaultOneShotMetricaConfig defaultOneShotMetricaConfig,
        @NonNull ClientExecutorProvider clientExecutorProvider,
        @NonNull ActivityAppearedListener activityAppearedListener,
        @NonNull AppMetricaServiceDelayHandler appMetricaServiceDelayHandler,
        @NonNull ActivityLifecycleManager activityLifecycleManager,
        @NonNull SessionsTrackingManager sessionsTrackingManager,
        @NonNull ContextAppearedListener contextAppearedListener,
        @NonNull TechnicalCrashProcessorFactory crashProcessorFactory,
        @NonNull AppMetricaCoreComponentsProvider appMetricaCoreComponentsProvider
    ) {
        this.currentProcessDetector = currentProcessDetector;
        this.defaultOneShotConfig = defaultOneShotMetricaConfig;
        this.clientExecutorProvider = clientExecutorProvider;
        this.activityAppearedListener = activityAppearedListener;
        this.appMetricaServiceDelayHandler = appMetricaServiceDelayHandler;
        this.activityLifecycleManager = activityLifecycleManager;
        this.sessionsTrackingManager = sessionsTrackingManager;
        this.contextAppearedListener = contextAppearedListener;
        this.crashProcessorFactory = crashProcessorFactory;
        this.appMetricaCoreComponentsProvider = appMetricaCoreComponentsProvider;
    }

    @NonNull
    public ProcessNameProvider getProcessNameProvider() {
        return currentProcessDetector;
    }

    @NonNull
    public CurrentProcessDetector getCurrentProcessDetector() {
        return currentProcessDetector;
    }

    @NonNull
    public DefaultOneShotMetricaConfig getDefaultOneShotConfig() {
        return defaultOneShotConfig;
    }

    @NonNull
    public ClientExecutorProvider getClientExecutorProvider() {
        return clientExecutorProvider;
    }

    @NonNull
    public AppMetricaServiceDelayHandler getAppMetricaServiceDelayHandler() {
        return appMetricaServiceDelayHandler;
    }

    @NonNull
    public ActivityLifecycleManager getActivityLifecycleManager() {
        return activityLifecycleManager;
    }

    @NonNull
    public SessionsTrackingManager getSessionsTrackingManager() {
        return sessionsTrackingManager;
    }

    @NonNull
    public ContextAppearedListener getContextAppearedListener() {
        return contextAppearedListener;
    }

    @NonNull
    public ActivityAppearedListener getActivityAppearedListener() {
        return activityAppearedListener;
    }

    @NonNull
    public synchronized ScreenInfoRetriever getScreenInfoRetriever() {
        if (screenInfoRetriever == null) {
            screenInfoRetriever = new ScreenInfoRetriever();
            activityAppearedListener.registerListener(this.screenInfoRetriever);
        }
        return screenInfoRetriever;
    }

    @Nullable
    public ReporterLifecycleListener getReporterLifecycleListener() {
        return reporterLifecycleListener;
    }

    public void registerReporterLifecycleListener(
        @NonNull final ReporterLifecycleListener listener
    ) {
        this.reporterLifecycleListener = listener;
    }

    @NonNull
    public TechnicalCrashProcessorFactory getCrashProcessorFactory() {
        return crashProcessorFactory;
    }

    @NonNull
    public synchronized MultiProcessSafeUuidProvider getMultiProcessSafeUuidProvider(@NonNull Context context) {
        if (multiProcessSafeUuidProvider == null) {
            multiProcessSafeUuidProvider =
                new MultiProcessSafeUuidProvider(context, new UuidFromClientPreferencesImporter());
        }
        return multiProcessSafeUuidProvider;
    }

    @NonNull
    public AppMetricaCoreComponentsProvider getAppMetricaCoreComponentsProvider() {
        return appMetricaCoreComponentsProvider;
    }

    @NonNull
    public ModuleEntryPointsRegister getModuleEntryPointsRegister() {
        return moduleEntryPointsRegister;
    }

    @NonNull
    public ClientModulesController getModulesController() {
        ClientModulesController local = modulesController;
        if (local == null) {
            synchronized (this) {
                local = modulesController;
                if (local == null) {
                    local = new ClientModulesController();
                    modulesController = local;
                }
            }
        }
        return local;
    }

    @NonNull
    public PreferencesClientDbStorage getPreferencesClientDbStorage(@NonNull Context context) {
        PreferencesClientDbStorage local = preferencesClientDbStorage;
        if (local == null) {
            synchronized (this) {
                local = preferencesClientDbStorage;
                if (local == null) {
                    local = new PreferencesClientDbStorage(
                        DatabaseStorageFactory.getInstance(context).getClientDbHelper()
                    );
                    preferencesClientDbStorage = local;
                }
            }
        }
        return local;
    }

    @NonNull
    public AppMetricaFacadeProvider getAppMetricaFacadeProvider() {
        return appMetricaFacadeProvider;
    }

    @NonNull
    public AppMetricaServiceProcessDetector getAppMetricaServiceProcessDetector() {
        return appMetricaServiceProcessDetector;
    }

    @NonNull
    public FirstLaunchDetector getFirstLaunchDetector() {
        return firstLaunchDetector;
    }

    @NonNull
    public ClientConfigSerializer getClientConfigSerializer() {
        return clientConfigSerializer;
    }

    @NonNull
    public AnonymousClientActivator getAnonymousClientActivator() {
        AnonymousClientActivator local = anonymousClientActivator;
        if (local == null) {
            synchronized (this) {
                local = anonymousClientActivator;
                if (local == null) {
                    local = new AnonymousClientActivator(
                        appMetricaFacadeProvider,
                        sessionsTrackingManager,
                        clientExecutorProvider
                    );
                    anonymousClientActivator = local;
                }
            }
        }
        return local;
    }

    @NonNull
    public ExtraMetaInfoRetriever getExtraMetaInfoRetriever(@NonNull Context context) {
        ExtraMetaInfoRetriever local = extraMetaInfoRetriever;
        if (local == null) {
            synchronized (this) {
                local = extraMetaInfoRetriever;
                if (local == null) {
                    local = new ExtraMetaInfoRetriever(context);
                    extraMetaInfoRetriever = local;
                }
            }
        }
        return local;
    }

    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    public static void setInstance(@Nullable ClientServiceLocator clientServiceLocator) {
        sHolder = clientServiceLocator;
    }

    @VisibleForTesting
    public void setModulesController(@NonNull ClientModulesController modulesController) {
        this.modulesController = modulesController;
    }
}
