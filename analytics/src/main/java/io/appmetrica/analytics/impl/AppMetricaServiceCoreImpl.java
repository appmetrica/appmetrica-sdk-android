package io.appmetrica.analytics.impl;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.annotation.WorkerThread;
import io.appmetrica.analytics.impl.client.ProcessConfiguration;
import io.appmetrica.analytics.impl.component.clients.ClientRepository;
import io.appmetrica.analytics.impl.component.clients.ComponentsRepository;
import io.appmetrica.analytics.impl.core.CoreImplFirstCreateTaskLauncherProvider;
import io.appmetrica.analytics.impl.crash.service.ServiceCrashController;
import io.appmetrica.analytics.impl.modules.ModuleServiceLifecycleControllerImpl;
import io.appmetrica.analytics.impl.modules.ServiceContextFacade;
import io.appmetrica.analytics.impl.modules.service.ServiceModulesController;
import io.appmetrica.analytics.impl.selfreporting.AppMetricaSelfReportFacade;
import io.appmetrica.analytics.impl.service.AppMetricaConnectionConstants;
import io.appmetrica.analytics.impl.service.AppMetricaServiceCallback;
import io.appmetrica.analytics.impl.startup.StartupState;
import io.appmetrica.analytics.impl.utils.JsonHelper;
import io.appmetrica.analytics.impl.utils.ServerTime;
import io.appmetrica.analytics.internal.CounterConfiguration;
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger;
import java.util.Objects;

public class AppMetricaServiceCoreImpl implements AppMetricaServiceCore, AppMetricaCoreReporter {
    private static final String TAG = "[AppMetricaServiceCoreImpl]";

    private boolean created = false;
    @NonNull
    private final Context mContext;
    @NonNull
    private final AppMetricaServiceCallback mCallback;

    @NonNull
    private ClientRepository mClientRepository;
    @NonNull
    private final AppMetricaServiceLifecycle mAppMetricaServiceLifecycle;
    @Nullable
    private ReportConsumer mReportConsumer;
    @NonNull
    private final FirstServiceEntryPointManager firstServiceEntryPointManager;
    @NonNull
    private final ApplicationStateProviderImpl applicationStateProvider;
    @NonNull
    private final AppMetricaServiceCoreImplFieldsFactory fieldsFactory;
    @NonNull
    private final ReportProxy reportProxy;

    /** @noinspection FieldCanBeLocal*/
    // Attention!! Do not convert to local variable or it will broken CrashFileObserver.
    // See warning block from description of https://developer.android.com/reference/android/os/FileObserver
    @Nullable
    private ServiceCrashController serviceCrashController;

    @MainThread
    public AppMetricaServiceCoreImpl(@NonNull Context context,
                                     @NonNull AppMetricaServiceCallback callback) {
        this(context, callback, new ComponentsRepository(context));
    }

    @MainThread
    private AppMetricaServiceCoreImpl(@NonNull Context context,
                                      @NonNull AppMetricaServiceCallback callback,
                                      @NonNull ComponentsRepository componentsRepository) {
        this(
            context,
            callback,
            new ClientRepository(context, componentsRepository),
            new AppMetricaServiceLifecycle(),
            FirstServiceEntryPointManager.INSTANCE,
            GlobalServiceLocator.getInstance().getApplicationStateProvider(),
            new AppMetricaServiceCoreImplFieldsFactory()
        );
    }

    @MainThread
    @VisibleForTesting
    AppMetricaServiceCoreImpl(@NonNull Context context,
                              @NonNull AppMetricaServiceCallback callback,
                              @NonNull ClientRepository clientRepository,
                              @NonNull AppMetricaServiceLifecycle appMetricaServiceLifecycle,
                              @NonNull FirstServiceEntryPointManager firstServiceEntryPointManager,
                              @NonNull ApplicationStateProviderImpl applicationStateProvider,
                              @NonNull AppMetricaServiceCoreImplFieldsFactory fieldsFactory) {
        mContext = context;
        mCallback = callback;
        mClientRepository = clientRepository;
        mAppMetricaServiceLifecycle = appMetricaServiceLifecycle;
        this.firstServiceEntryPointManager = firstServiceEntryPointManager;
        this.applicationStateProvider = applicationStateProvider;
        this.fieldsFactory = fieldsFactory;
        this.reportProxy = new ReportProxy();
    }

    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    void setClientRepository(ClientRepository repository) {
        mClientRepository = repository;
    }

    @WorkerThread
    @Override
    public void onCreate() {
        DebugLogger.INSTANCE.info(TAG, "onCreate");
        if (!created) {
            DebugLogger.INSTANCE.info(TAG, "onFirstCreate()");
            onFirstCreate();
            created = true;
        } else {
            DebugLogger.INSTANCE.info(TAG, "onNonFirstCreate()");
            loadLocaleFromConfiguration(mContext.getResources().getConfiguration());
        }
        GlobalServiceLocator.getInstance().getLifecycleDependentComponentManager().onCreate();
    }

    @WorkerThread
    private void onFirstCreate() {
        DebugLogger.INSTANCE.info(TAG, "onFirstCreate... discover modules");
        firstServiceEntryPointManager.onPossibleFirstEntry(mContext);

        GlobalServiceLocator.getInstance().initAsync();
        ServerTime.getInstance().init();
        StartupStateHolder startupStateHolder = GlobalServiceLocator.getInstance().getStartupStateHolder();

        StartupState startupState = startupStateHolder.getStartupState();
        initModules(startupStateHolder);

        GlobalServiceLocator.getInstance().getSslSocketFactoryProvider().onStartupStateChanged(startupState);
        initMetricaServiceLifecycleObservers();

        DebugLogger.INSTANCE.info(TAG, "Init location service API");
        GlobalServiceLocator.getInstance().getLocationClientApi().init();
        DebugLogger.INSTANCE.info(TAG, "Init serviceInternalAdvertisingIdGetter");
        GlobalServiceLocator.getInstance().getAdvertisingIdGetter().init();

        mReportConsumer = fieldsFactory.createReportConsumer(mContext, mClientRepository);

        DebugLogger.INSTANCE.info(TAG, "Warm up self reporter");
        AppMetricaSelfReportFacade.warmupForSelfProcess(mContext);
        DebugLogger.INSTANCE.info(TAG, "Setup service crash controller");
        serviceCrashController = new ServiceCrashController(mContext, mReportConsumer);
        serviceCrashController.init();
        DebugLogger.INSTANCE.info(TAG, "Run scheduler on first create additional tasks");
        new CoreImplFirstCreateTaskLauncherProvider().getLauncher().run();
        DebugLogger.INSTANCE.info(TAG, "Finish onFirstCreate");
    }

    private void initModules(@NonNull StartupStateHolder startupStateHolder) {
        DebugLogger.INSTANCE.info(TAG, "Load and init modules");
        StartupState startupState = startupStateHolder.getStartupState();
        ServiceModulesController modulesController = GlobalServiceLocator.getInstance().getModulesController();
        modulesController.initServiceSide(
            new ServiceContextFacade(
                new ModuleServiceLifecycleControllerImpl(mAppMetricaServiceLifecycle)
            ),
            startupState
        );
        startupStateHolder.registerObserver(modulesController);
    }

    @WorkerThread
    private void initMetricaServiceLifecycleObservers() {
        DebugLogger.INSTANCE.info(TAG, "initMetricaServiceLifecycleObservers");
        mAppMetricaServiceLifecycle.addNewClientConnectObserver(intent -> {
            DebugLogger.INSTANCE.info(TAG, "onNewClientConnect");
            onNewClientConnected(intent);
        });
    }

    @WorkerThread
    @Override
    public void onStart(Intent intent, int startId) {
        DebugLogger.INSTANCE.info(TAG, "onStart");
        handleStart(intent, startId);
    }

    @WorkerThread
    @Override
    public void onStartCommand(Intent intent, int flags, int startId) {
        DebugLogger.INSTANCE.info(TAG, "onStartCommand");
        handleStart(intent, startId);
    }

    @WorkerThread
    @Override
    public void onBind(Intent intent) {
        DebugLogger.INSTANCE.info(TAG, "onBind with intent:%s", intent);
        mAppMetricaServiceLifecycle.onBind(intent);
    }

    @WorkerThread
    @Override
    public void onRebind(Intent intent) {
        DebugLogger.INSTANCE.info(TAG, "onRebind()");
        mAppMetricaServiceLifecycle.onRebind(intent);
    }

    @WorkerThread
    @Override
    public void onUnbind(Intent intent) {
        DebugLogger.INSTANCE.info(TAG, "onUnbind()");
        mAppMetricaServiceLifecycle.onUnbind(intent);
        if (intent != null) {
            String action = intent.getAction();
            Uri intentData = intent.getData();
            String packageName = intentData == null ? null : intentData.getEncodedAuthority();
            DebugLogger.INSTANCE.info(
                TAG,
                "Unbind from the service with data: %s and action: %s and package: %s",
                intent,
                action,
                packageName
            );

            if (AppMetricaConnectionConstants.ACTION_CLIENT_CONNECTION.equals(action)) {
                if (packageName == null) {
                    DebugLogger.INSTANCE.warning(TAG, "Package name is null");
                } else {
                    removeClients(intentData, packageName);
                }
            }
        }
    }

    @WorkerThread
    private void onNewClientConnected(@NonNull Intent intent) {
        DebugLogger.INSTANCE.info(TAG, "remove scheduled disconnect from onBind()");
        updateScreenInfo(intent);
    }

    private void updateScreenInfo(@NonNull Intent intent) {
        GlobalServiceLocator.getInstance().getSdkEnvironmentHolder().mayBeUpdateScreenInfo(
            JsonHelper.screenInfoFromJsonString(intent.getStringExtra(AppMetricaConnectionConstants.EXTRA_SCREEN_SIZE))
        );
    }

    @WorkerThread
    @VisibleForTesting
    void removeClients(@Nullable Uri intentData, @NonNull String packageName) {
        String pathClient = "/" + AppMetricaConnectionConstants.PATH_CLIENT;
        if (intentData != null && Objects.equals(intentData.getPath(), pathClient)) {
            int pid = Integer.parseInt(intentData.getQueryParameter(AppMetricaConnectionConstants.PARAMETER_PID));
            String psid = intentData.getQueryParameter(AppMetricaConnectionConstants.PARAMETER_PSID);
            DebugLogger.INSTANCE.info(TAG, "unbounded client pid %d and psid %s", pid, psid);
            mClientRepository.remove(packageName, pid, psid);
            DebugLogger.INSTANCE.info(
                TAG,
                "Remains clients after unbind: %d",
                mClientRepository.getClientsCount()
            );
            applicationStateProvider.notifyProcessDisconnected(pid);
        }
    }

    @WorkerThread
    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        DebugLogger.INSTANCE.info(TAG, "onConfigurationChanged()");
        loadLocaleFromConfiguration(newConfig);
    }

    private void loadLocaleFromConfiguration(Configuration configuration) {
        GlobalServiceLocator.getInstance().getSdkEnvironmentHolder().mayBeUpdateConfiguration(configuration);
    }

    @MainThread
    @Override
    public void onDestroy() {
        DebugLogger.INSTANCE.info(TAG, "onDestroy()");
        GlobalServiceLocator.getInstance().getLifecycleDependentComponentManager().onDestroy();
    }

    @WorkerThread
    @Override
    public void reportData(Bundle data) {
        // Set class loader for unmarshalling
        data.setClassLoader(CounterConfiguration.class.getClassLoader());
        CounterReport counterReport = CounterReport.fromBundle(data);
        DebugLogger.INSTANCE.info(
            TAG,
            "reportData: type = %s; customType = %s; name = %s",
            counterReport.getType(), counterReport.getCustomType(), counterReport.getName()
        );
        if (mReportConsumer != null) {
            mReportConsumer.consumeReport(CounterReport.fromBundle(data), data);
        }
    }

    @WorkerThread
    @Override
    public void reportData(int type, Bundle data) {
        reportProxy.proxyReport(type, data);
    }

    @WorkerThread
    @Override
    public void resumeUserSession(@NonNull Bundle data) {
        Integer processId = extractProcessId(data);
        DebugLogger.INSTANCE.info(TAG, "resumeUserSession for pid = %s", processId);
        if (processId != null) {
            applicationStateProvider.resumeUserSessionForPid(processId);
        } else {
            DebugLogger.INSTANCE.error(TAG, "Process configuration or processId is null");
        }
    }

    @WorkerThread
    @Override
    public void pauseUserSession(@NonNull Bundle data) {
        Integer processId = extractProcessId(data);
        DebugLogger.INSTANCE.info(TAG, "pauseUserSession for pid = %s", processId);
        if (processId != null) {
            applicationStateProvider.pauseUserSessionForPid(processId);
        } else {
            DebugLogger.INSTANCE.error(TAG, "Process configuration or processId is null");
        }
    }

    @WorkerThread
    private Integer extractProcessId(@NonNull Bundle bundle) {
        bundle.setClassLoader(ProcessConfiguration.class.getClassLoader());
        ProcessConfiguration processConfiguration = ProcessConfiguration.fromBundle(bundle);
        return processConfiguration == null ? null : processConfiguration.getProcessID();
    }

    private void handleStart(Intent intent, int startId) {
        DebugLogger.INSTANCE.info(
            TAG,
            "Handle start of service with data: %s and startId: %d",
            intent,
            startId
        );

        // We don't want the hanging service in running apps.
        mCallback.onStartFinished(startId);
    }

    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    void setReportConsumer(@NonNull ReportConsumer reportConsumer) {
        mReportConsumer = reportConsumer;
    }
}
