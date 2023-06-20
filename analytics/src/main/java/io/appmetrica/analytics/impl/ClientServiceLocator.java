package io.appmetrica.analytics.impl;

import android.annotation.SuppressLint;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.coreapi.internal.executors.ICommonExecutor;
import io.appmetrica.analytics.impl.crash.CrashProcessorFactory;
import io.appmetrica.analytics.impl.reporter.ReporterLifecycleListener;
import io.appmetrica.analytics.impl.startup.uuid.MultiProcessSafeUuidProvider;
import io.appmetrica.analytics.impl.startup.uuid.UuidFromClientPreferencesImporter;
import io.appmetrica.analytics.impl.utils.MainProcessDetector;
import io.appmetrica.analytics.impl.utils.ProcessDetector;
import io.appmetrica.analytics.impl.utils.executors.ClientExecutorProvider;

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
    private final MainProcessDetector mainProcessDetector;
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
    private ReporterLifecycleListener reporterLifecycleListener;
    @NonNull
    private final CrashProcessorFactory crashProcessorFactory;
    @Nullable
    private MultiProcessSafeUuidProvider multiProcessSafeUuidProvider;

    private ClientServiceLocator() {
        this(new MainProcessDetector(), new ActivityLifecycleManager(), new ClientExecutorProvider());
    }

    private ClientServiceLocator(@NonNull MainProcessDetector mainProcessDetector,
                                 @NonNull ActivityLifecycleManager activityLifecycleManager,
                                 @NonNull ClientExecutorProvider clientExecutorProvider) {
        this(
            mainProcessDetector,
            activityLifecycleManager,
            clientExecutorProvider,
            new ActivityAppearedListener(activityLifecycleManager, clientExecutorProvider.getApiProxyExecutor())
        );
    }

    private ClientServiceLocator(@NonNull MainProcessDetector mainProcessDetector,
                                 @NonNull ActivityLifecycleManager activityLifecycleManager,
                                 @NonNull ClientExecutorProvider clientExecutorProvider,
                                 @NonNull ActivityAppearedListener activityAppearedListener) {
        this(
            mainProcessDetector,
            new DefaultOneShotMetricaConfig(),
            clientExecutorProvider,
            activityAppearedListener,
            new AppMetricaServiceDelayHandler(mainProcessDetector),
            activityLifecycleManager,
            new SessionsTrackingManager(
                activityLifecycleManager,
                clientExecutorProvider.getApiProxyExecutor(),
                activityAppearedListener
            ),
            new ContextAppearedListener(activityLifecycleManager),
            new CrashProcessorFactory()
        );
    }

    @VisibleForTesting
    ClientServiceLocator(@NonNull MainProcessDetector mainProcessDetector,
                         @NonNull DefaultOneShotMetricaConfig defaultOneShotMetricaConfig,
                         @NonNull ClientExecutorProvider clientExecutorProvider,
                         @NonNull ActivityAppearedListener activityAppearedListener,
                         @NonNull AppMetricaServiceDelayHandler appMetricaServiceDelayHandler,
                         @NonNull ActivityLifecycleManager activityLifecycleManager,
                         @NonNull SessionsTrackingManager sessionsTrackingManager,
                         @NonNull ContextAppearedListener contextAppearedListener,
                         @NonNull CrashProcessorFactory crashProcessorFactory) {
        this.mainProcessDetector = mainProcessDetector;
        this.defaultOneShotConfig = defaultOneShotMetricaConfig;
        this.clientExecutorProvider = clientExecutorProvider;
        this.activityAppearedListener = activityAppearedListener;
        this.appMetricaServiceDelayHandler = appMetricaServiceDelayHandler;
        this.activityLifecycleManager = activityLifecycleManager;
        this.sessionsTrackingManager = sessionsTrackingManager;
        this.contextAppearedListener = contextAppearedListener;
        this.crashProcessorFactory = crashProcessorFactory;
    }

    @NonNull
    public ProcessDetector getProcessDetector() {
        return mainProcessDetector;
    }

    @NonNull
    public MainProcessDetector getMainProcessDetector() {
        return mainProcessDetector;
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
    public ICommonExecutor getApiProxyExecutor() {
        return clientExecutorProvider.getApiProxyExecutor();
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
    public CrashProcessorFactory getCrashProcessorFactory() {
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

    @VisibleForTesting(otherwise = VisibleForTesting.NONE)

    public static void setInstance(@Nullable ClientServiceLocator clientServiceLocator) {
        sHolder = clientServiceLocator;
    }
}
