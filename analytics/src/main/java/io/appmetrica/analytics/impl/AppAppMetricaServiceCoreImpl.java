package io.appmetrica.analytics.impl;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.annotation.VisibleForTesting;
import androidx.annotation.WorkerThread;

import java.io.File;
import java.util.concurrent.TimeUnit;

import io.appmetrica.analytics.CounterConfiguration;
import io.appmetrica.analytics.coreapi.internal.backport.Consumer;
import io.appmetrica.analytics.coreapi.internal.executors.ICommonExecutor;
import io.appmetrica.analytics.coreutils.internal.AndroidUtils;
import io.appmetrica.analytics.coreutils.internal.logger.YLogger;
import io.appmetrica.analytics.impl.client.ClientConfiguration;
import io.appmetrica.analytics.impl.client.ProcessConfiguration;
import io.appmetrica.analytics.impl.component.CommonArguments;
import io.appmetrica.analytics.impl.component.clients.ClientDescription;
import io.appmetrica.analytics.impl.component.clients.ClientRepository;
import io.appmetrica.analytics.impl.component.clients.ComponentsRepository;
import io.appmetrica.analytics.impl.core.MetricaCoreImplFirstCreateTaskLauncherProvider;
import io.appmetrica.analytics.impl.crash.CrashpadListener;
import io.appmetrica.analytics.impl.crash.ReadOldCrashesRunnable;
import io.appmetrica.analytics.impl.crash.jvm.CrashDirectoryWatcher;
import io.appmetrica.analytics.impl.crash.ndk.NativeCrashReader;
import io.appmetrica.analytics.impl.crash.ndk.crashpad.CrashpadLoader;
import io.appmetrica.analytics.impl.crash.ndk.crashpad.RemoveCompletedCrashesRunnable;
import io.appmetrica.analytics.impl.modules.ModuleLifecycleControllerImpl;
import io.appmetrica.analytics.impl.modules.ModulesController;
import io.appmetrica.analytics.impl.modules.ServiceContextFacade;
import io.appmetrica.analytics.impl.selfreporting.AppMetricaSelfReportFacade;
import io.appmetrica.analytics.impl.service.AppMetricaServiceAction;
import io.appmetrica.analytics.impl.service.MetricaServiceCallback;
import io.appmetrica.analytics.impl.startup.StartupState;
import io.appmetrica.analytics.impl.utils.JsonHelper;
import io.appmetrica.analytics.impl.utils.ServerTime;

public class AppAppMetricaServiceCoreImpl implements AppMetricaServiceCore, MetricaCoreReporter {
    private static final String TAG = "[AppMetricaCoreImpl]";

    private boolean created = false;
    @NonNull
    private final Context mContext;
    @NonNull
    private volatile MetricaServiceCallback mCallback;
    @NonNull
    private final FileProvider mFileProvider;

    @NonNull
    private ClientRepository mClientRepository;
    @NonNull
    private final AppMetricaServiceLifecycle mAppMetricaServiceLifecycle;
    @NonNull
    private ReportConsumer mReportConsumer;
    @NonNull
    private final FirstServiceEntryPointManager firstServiceEntryPointManager;
    @NonNull
    private final CrashpadListener crashpadListener;
    @NonNull
    private final CrashpadLoader crashpadLoader;
    @NonNull
    private final ApplicationStateProviderImpl applicationStateProvider;
    @NonNull
    private final ICommonExecutor reportExecutor;
    @NonNull
    private final AppMetricaServiceCoreImplFieldsFactory fieldsFactory;
    @NonNull
    private Consumer<String> crashpadCrashConsumer;
    @NonNull
    private final Consumer<File> crashesListener = new Consumer<File>() {
        @WorkerThread // special FileObserver thread
        @Override
        public void consume(@NonNull File data) {
            handleNewCrashFromFile(data);
        }
    };
    @Nullable
    private NativeCrashReader<String> crashpadCrashReader;
    private final ICommonExecutor defaultExecutor;
    @NonNull
    private ReportProxy reportProxy;
    @NonNull
    private final ScreenInfoHolder screenInfoHolder;

    // Attention!! Do not convert to local variable or it will broken CrashFileObserver.
    // See warning block from description of https://developer.android.com/reference/android/os/FileObserver
    @Nullable
    private CrashDirectoryWatcher crashDirectoryWatcher;

    @MainThread
    public AppAppMetricaServiceCoreImpl(@NonNull Context context,
                                        @NonNull MetricaServiceCallback callback) {
        this(context, callback, new ComponentsRepository(context));
    }

    @MainThread
    private AppAppMetricaServiceCoreImpl(@NonNull Context context,
                                         @NonNull MetricaServiceCallback callback,
                                         @NonNull ComponentsRepository componentsRepository) {
        this(
            context,
            callback,
            new ClientRepository(context, componentsRepository),
            new AppMetricaServiceLifecycle(),
            new FileProvider(),
            FirstServiceEntryPointManager.INSTANCE,
            GlobalServiceLocator.getInstance().getApplicationStateProvider(),
            GlobalServiceLocator.getInstance().getLifecycleDependentComponentManager()
                .getCrashpadListener(),
            CrashpadLoader.getInstance(),
            GlobalServiceLocator.getInstance().getServiceExecutorProvider().getReportRunnableExecutor(),
            GlobalServiceLocator.getInstance().getServiceExecutorProvider().getDefaultExecutor(),
            new AppMetricaServiceCoreImplFieldsFactory(),
            GlobalServiceLocator.getInstance().getScreenInfoHolder()
        );
    }

    @MainThread
    @VisibleForTesting
    AppAppMetricaServiceCoreImpl(@NonNull Context context,
                                 @NonNull MetricaServiceCallback callback,
                                 @NonNull ClientRepository clientRepository,
                                 @NonNull AppMetricaServiceLifecycle appMetricaServiceLifecycle,
                                 @NonNull FileProvider fileProvider,
                                 @NonNull FirstServiceEntryPointManager firstServiceEntryPointManager,
                                 @NonNull ApplicationStateProviderImpl applicationStateProvider,
                                 @NonNull CrashpadListener crashpadListener,
                                 @NonNull CrashpadLoader crashpadLoader,
                                 @NonNull ICommonExecutor reportExecutor,
                                 @NonNull ICommonExecutor defaultExecutor,
                                 @NonNull AppMetricaServiceCoreImplFieldsFactory fieldsFactory,
                                 @NonNull ScreenInfoHolder screenInfoHolder) {
        mContext = context;
        mCallback = callback;
        mClientRepository = clientRepository;
        mAppMetricaServiceLifecycle = appMetricaServiceLifecycle;
        mFileProvider = fileProvider;
        this.firstServiceEntryPointManager = firstServiceEntryPointManager;
        this.applicationStateProvider = applicationStateProvider;
        this.reportExecutor = reportExecutor;
        this.defaultExecutor = defaultExecutor;
        this.fieldsFactory = fieldsFactory;
        this.crashpadListener = crashpadListener;
        this.crashpadLoader = crashpadLoader;
        this.reportProxy = new ReportProxy();
        this.screenInfoHolder = screenInfoHolder;
    }

    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    void setClientRepository(ClientRepository repository) {
        mClientRepository = repository;
    }

    @WorkerThread
    @Override
    public void onCreate() {
        YLogger.d("%sonCreate", TAG);
        if (!created) {
            YLogger.info(TAG, "onFirstCreate()");
            onFirstCreate();
            created = true;
        } else {
            YLogger.info(TAG, "onNonFirstCreate()");
            loadLocaleFromConfiguration(mContext.getResources().getConfiguration());
        }
        if (AndroidUtils.isApiAchieved(Build.VERSION_CODES.LOLLIPOP)) {
            crashpadListener.addListener(crashpadCrashConsumer);
        }
    }

    @SuppressLint("NewApi")
    @WorkerThread
    private void onFirstCreate() {
        YLogger.info(TAG, "onFirstCreate... discover modules");
        firstServiceEntryPointManager.onPossibleFirstEntry(mContext);

        GlobalServiceLocator.getInstance().initAsync();
        ServerTime.getInstance().init();
        StartupStateHolder startupStateHolder = GlobalServiceLocator.getInstance().getStartupStateHolder();

        StartupState startupState = startupStateHolder.getStartupState();
        initModules(startupStateHolder);

        GlobalServiceLocator.getInstance().getSslSocketFactoryProvider().onStartupStateChanged(startupState);
        initMetricaServiceLifecycleObservers();

        YLogger.info(TAG, "Init location service API");
        GlobalServiceLocator.getInstance().getLocationClientApi().init();
        YLogger.info(TAG, "Init serviceInternalAdvertisingIdGetter");
        GlobalServiceLocator.getInstance().getServiceInternalAdvertisingIdGetter().init(mContext, startupState);

        mReportConsumer = fieldsFactory.createReportConsumer(mContext, mClientRepository);

        AppMetricaSelfReportFacade.warmupForMetricaProcess(mContext);
        initJvmCrashWatcher();
        if (AndroidUtils.isApiAchieved(Build.VERSION_CODES.LOLLIPOP)) {
            initCrashpad();
        }
        YLogger.info(TAG, "Run scheduler on first create additional tasks");
        new MetricaCoreImplFirstCreateTaskLauncherProvider().getLauncher().run();
        YLogger.info(TAG, "Finish onFirstCreate");
    }

    private void initModules(@NonNull StartupStateHolder startupStateHolder) {
        YLogger.info(TAG, "Load and init modules");
        StartupState startupState = startupStateHolder.getStartupState();
        ModulesController modulesController = GlobalServiceLocator.getInstance().getModulesController();
        modulesController.initServiceSide(
            new ServiceContextFacade(
                new ModuleLifecycleControllerImpl(mAppMetricaServiceLifecycle)
            ),
            startupState
        );
        startupStateHolder.registerObserver(modulesController);
    }

    private void initJvmCrashWatcher() {
        File crashDirectory = mFileProvider.getCrashesDirectory(mContext);
        if (crashDirectory != null) {
            crashDirectoryWatcher = fieldsFactory.createCrashDirectoryWatcher(crashDirectory, crashesListener);
            YLogger.info(TAG, "readOldCrashes for directory: %s", crashDirectory.getAbsolutePath());

            reportExecutor.execute(new ReadOldCrashesRunnable(mContext, crashDirectory, crashesListener));
            crashDirectoryWatcher.startWatching();
        } else {
            YLogger.info(TAG, "Do not init JVM crash watcher as crashes directory is null");
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private void initCrashpad() {
        crashpadCrashReader = fieldsFactory.createCrashpadCrashReader(mReportConsumer);

        crashpadCrashConsumer = new Consumer<String>() {
            @Override
            public void consume(@NonNull String input) {
                crashpadCrashReader.handleRealtimeCrash(input);
            }
        };
        if (crashpadLoader.loadIfNeeded()) {
            crashpadCrashReader.checkForPreviousSessionCrashes();
            defaultExecutor.executeDelayed(new RemoveCompletedCrashesRunnable(), 1, TimeUnit.MINUTES);
        }
    }

    @WorkerThread
    private void initMetricaServiceLifecycleObservers() {
        YLogger.info(TAG, "initMetricaServiceLifecycleObservers");
        mAppMetricaServiceLifecycle.addNewClientConnectObserver(new AppMetricaServiceLifecycle.LifecycleObserver() {
            @Override
            public void onEvent(@NonNull Intent intent) {
                YLogger.d("%sonNewClientConnect", TAG);
                onNewClientConnected(intent);
            }
        });
    }

    @WorkerThread
    @Override
    public void onStart(Intent intent, int startId) {
        YLogger.d("%sonStart", TAG);
        handleStart(intent, startId);
    }

    @WorkerThread
    @Override
    public void onStartCommand(Intent intent, int flags, int startId) {
        YLogger.d("%sonStartCommand", TAG);
        handleStart(intent, startId);
    }

    @WorkerThread
    @Override
    public void onBind(Intent intent) {
        YLogger.d("%sonBind with intent:%s", TAG, intent);
        mAppMetricaServiceLifecycle.onBind(intent);
    }

    @WorkerThread
    @Override
    public void onRebind(Intent intent) {
        YLogger.d("%sonRebind()", TAG);
        mAppMetricaServiceLifecycle.onRebind(intent);
    }

    @WorkerThread
    @Override
    public void onUnbind(Intent intent) {
        YLogger.d("%sonUnbind()", TAG);
        mAppMetricaServiceLifecycle.onUnbind(intent);
        if (intent != null) {
            String action = intent.getAction();
            Uri intentData = intent.getData();
            String packageName = intentData == null ? null : intentData.getEncodedAuthority();
            YLogger.d("%sUnbind from the service with data: %s and action: %s and package: %s", TAG, intent, action,
                packageName);

            if (AppMetricaServiceAction.ACTION_CLIENT_CONNECTION.equals(action)) {
                removeClients(intentData, packageName);
            }
        }
    }

    @WorkerThread
    private void onNewClientConnected(@NonNull Intent intent) {
        YLogger.d("%sremove scheduled disconnect from onBind()", TAG);
        updateScreenInfo(intent);
    }

    private void updateScreenInfo(@NonNull Intent intent) {
        screenInfoHolder.maybeUpdateInfo(JsonHelper.screenInfoFromJsonString(
            intent.getStringExtra(ServiceUtils.EXTRA_SCREEN_SIZE)));
    }

    @WorkerThread
    @VisibleForTesting
    void removeClients(@Nullable Uri intentData, @Nullable String packageName) {
        if (intentData != null && intentData.getPath().equals("/" + ServiceUtils.PATH_CLIENT)) {
            int pid = Integer.parseInt(intentData.getQueryParameter(ServiceUtils.PARAMETER_PID));
            String psid = intentData.getQueryParameter(ServiceUtils.PARAMETER_PSID);
            YLogger.d("%sunbounded client pid %d and psid %s", TAG, pid, psid);
            mClientRepository.remove(packageName, pid, psid);
            YLogger.i("%sRemains clients after unbind: %d", TAG, mClientRepository.getClientsCount());
            applicationStateProvider.notifyProcessDisconnected(pid);
        }
    }

    @WorkerThread
    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        YLogger.info(TAG, "onConfigurationChanged()");
        loadLocaleFromConfiguration(newConfig);
    }

    private void loadLocaleFromConfiguration(@NonNull Configuration configuration) {
        LocaleHolder.getInstance(mContext).updateLocales(configuration);
    }

    @MainThread
    @Override
    public void onDestroy() {
        YLogger.debug(TAG, "onDestroy()");
        if (AndroidUtils.isApiAchieved(Build.VERSION_CODES.LOLLIPOP)) {
            crashpadListener.removeListener(crashpadCrashConsumer);
        }
    }

    @WorkerThread
    @Override
    public void reportData(Bundle data) {
        // Set class loader for unmarshalling
        data.setClassLoader(CounterConfiguration.class.getClassLoader());
        CounterReport counterReport = CounterReport.fromBundle(data);
        YLogger.info(
            TAG,
            "reportData: type = %s; customType = %s; name = %s",
            counterReport.getType(), counterReport.getCustomType(), counterReport.getName()
        );
        mReportConsumer.consumeReport(CounterReport.fromBundle(data), data);
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
        YLogger.d("%sresumeUserSession for pid = %s", TAG, processId);
        if (processId != null) {
            applicationStateProvider.resumeUserSessionForPid(processId);
        } else {
            YLogger.e("%sProcess configuration or processId is null", TAG);
        }
    }

    @WorkerThread
    @Override
    public void pauseUserSession(@NonNull Bundle data) {
        Integer processId = extractProcessId(data);
        YLogger.d("%spauseUserSession for pid = %s", TAG, processId);
        if (processId != null) {
            applicationStateProvider.pauseUserSessionForPid(processId);
        } else {
            YLogger.e("%sProcess configuration or processId is null", TAG);
        }
    }

    @Override
    public void updateCallback(@NonNull MetricaServiceCallback callback) {
        mCallback = callback;
    }

    @WorkerThread
    private Integer extractProcessId(@NonNull Bundle bundle) {
        bundle.setClassLoader(ProcessConfiguration.class.getClassLoader());
        ProcessConfiguration processConfiguration = ProcessConfiguration.fromBundle(bundle);
        return processConfiguration == null ? null : processConfiguration.getProcessID();
    }

    @WorkerThread
    public void handleNewCrashFromFile(@NonNull File crashFile) {
        YLogger.d("%s handleNewCrashFromFile %s", TAG, crashFile.getName());
        mReportConsumer.consumeCrashFromFile(crashFile);
    }

    private void handleStart(Intent intent, int startId) {
        YLogger.d("%sHandle start of service with data: %s and startId: %d", TAG, intent, startId);

        if (null != intent) {
            // Set class loader for unmarshalling
            intent.getExtras().setClassLoader(CounterConfiguration.class.getClassLoader());

            handleEventOnStart(intent);
        }

        // We don't want the hanging service in running apps.
        mCallback.onStartFinished(startId);
    }

    @WorkerThread
    private boolean isInvalidIntentData(final Intent intent) {
        return (null == intent || null == intent.getData());
    }

    @WorkerThread
    private void handleEventOnStart(final Intent intent) {
        if (isInvalidIntentData(intent)) {
            return;
        }
        final Bundle extras = intent.getExtras();
        final ClientConfiguration clientConfiguration = ClientConfiguration.fromBundle(mContext, extras);
        if (clientConfiguration == null) {
            return;
        }

        final CounterReport reportData = CounterReport.fromBundle(extras);

        boolean isInvalidData = reportData.isNoEvent();
        isInvalidData |= reportData.isUndefinedType();
        if (isInvalidData) {
            return; // Skip invalid attempt to report data due to lack of data
        }

        try {
            // Immediately create & send due to crash
            mReportConsumer.consumeCrash(
                ClientDescription.fromClientConfiguration(clientConfiguration),
                reportData,
                new CommonArguments(clientConfiguration)
            );
        } catch (Throwable exception) {
            YLogger.e("%sSomething was wrong while handling event.\n%s", TAG, exception);
        }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    void setReportConsumer(@NonNull ReportConsumer reportConsumer) {
        mReportConsumer = reportConsumer;
    }
}
