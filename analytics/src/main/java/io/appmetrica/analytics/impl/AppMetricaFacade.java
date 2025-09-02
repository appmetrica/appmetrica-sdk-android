package io.appmetrica.analytics.impl;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import androidx.annotation.AnyThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.annotation.WorkerThread;
import io.appmetrica.analytics.AdvIdentifiersResult;
import io.appmetrica.analytics.AppMetricaConfig;
import io.appmetrica.analytics.AppMetricaLibraryAdapterConfig;
import io.appmetrica.analytics.DeferredDeeplinkListener;
import io.appmetrica.analytics.DeferredDeeplinkParametersListener;
import io.appmetrica.analytics.ReporterConfig;
import io.appmetrica.analytics.StartupParamsCallback;
import io.appmetrica.analytics.impl.selfreporting.AppMetricaSelfReportFacade;
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger;
import java.util.List;
import java.util.Map;
import java.util.concurrent.FutureTask;

public class AppMetricaFacade implements IReporterFactoryProvider {

    private static final String TAG = "[AppMetricaFacade]";

    @NonNull
    private final Context mContext;
    @NonNull
    private final AppMetricaCoreComponentsProvider coreComponentsProvider;
    @NonNull
    private final FutureTask<IAppMetricaImpl> mFullInitFuture;
    @SuppressLint("StaticFieldLeak")
    @Nullable
    private static volatile AppMetricaFacade sInstance;
    @NonNull
    private final IAppMetricaCore mCore;
    private static volatile boolean sActivated = false;
    private static volatile boolean fullyInitialized;

    @AnyThread
    public AppMetricaFacade(@NonNull final Context context) {
        mContext = context;
        coreComponentsProvider = ClientServiceLocator.getInstance().getAppMetricaCoreComponentsProvider();
        mCore = coreComponentsProvider.getCore(
            context,
            ClientServiceLocator.getInstance().getClientExecutorProvider()
        );

        mFullInitFuture = new FutureTask<>(() -> {
            DebugLogger.INSTANCE.info(TAG, "createImpl");
            return createImpl();
        });
    }

    public void init() {
        ClientServiceLocator.getInstance().getClientExecutorProvider().getCoreInitThread(
            () -> {
                DebugLogger.INSTANCE.info(TAG, "Init first launch detector");
                ClientServiceLocator.getInstance().getFirstLaunchDetector().init(mContext);
                DebugLogger.INSTANCE.info(TAG, "Check client migration");
                new ClientMigrationManager(mContext).checkMigration(mContext);
                DebugLogger.INSTANCE.info(TAG, "Warm up uuid");
                ClientServiceLocator.getInstance().getMultiProcessSafeUuidProvider(mContext).readUuid();
                mFullInitFuture.run();
            }
        ).start();
    }

    @AnyThread
    @Nullable
    public static AppMetricaFacade peekInstance() {
        return sInstance;
    }

    @AnyThread
    @NonNull
    public static AppMetricaFacade getInstance(@NonNull final Context context) {
        AppMetricaFacade localCopy = sInstance;
        if (localCopy == null) {
            synchronized (AppMetricaFacade.class) {
                localCopy = sInstance;
                if (localCopy == null) {
                    DebugLogger.INSTANCE.info(TAG, "needs create facade");
                    localCopy = new AppMetricaFacade(context);
                    localCopy.init();
                    localCopy.onInstanceCreated();
                    sInstance = localCopy;
                }
            }
        }
        return localCopy;
    }

    private void onInstanceCreated() {
        DebugLogger.INSTANCE.info(TAG, "onInstanceCreated");
        ClientServiceLocator.getInstance().getClientExecutorProvider().getDefaultExecutor().execute(new Runnable() {
            @Override
            public void run() {
                DebugLogger.INSTANCE.info(TAG, "onInitializationFinished");
                AppMetricaSelfReportFacade.onInitializationFinished(mContext);
            }
        });
    }

    @AnyThread
    public synchronized static void markActivated() {
        sActivated = true;
    }

    @AnyThread
    public synchronized static boolean isActivated() {
        return sActivated;
    }

    @AnyThread
    public static void markFullyInitialized() {
        fullyInitialized = true;
    }

    @AnyThread
    public static boolean isFullyInitialized() {
        return fullyInitialized;
    }

    @AnyThread
    public synchronized static boolean isInitializedForApp() {
        AppMetricaFacade localCopy = sInstance;
        return localCopy != null && localCopy.isFullInitFutureDone() &&
            localCopy.peekMainReporterApiConsumerProvider() != null;
    }

    @WorkerThread
    public static void setLocation(@Nullable final Location location) {
        getConfigurator().setLocation(location);
    }

    @WorkerThread
    public static void setLocationTracking(final boolean enabled) {
        getConfigurator().setLocationTracking(enabled);
    }

    @WorkerThread
    public static void setAdvIdentifiersTracking(final boolean enabled) {
        getConfigurator().setAdvIdentifiersTracking(enabled);
    }

    @WorkerThread
    public static void setDataSendingEnabled(final boolean enabled) {
        getConfigurator().setDataSendingEnabled(enabled);
    }

    @WorkerThread
    public static void putErrorEnvironmentValue(String key, String value) {
        getConfigurator().putErrorEnvironmentValue(key, value);
    }

    @WorkerThread
    public static void putAppEnvironmentValue(String key, String value) {
        getConfigurator().putAppEnvironmentValue(key, value);
    }

    @WorkerThread
    public static void clearAppEnvironment() {
        getConfigurator().clearAppEnvironment();
    }

    @WorkerThread
    public static void setUserProfileID(@Nullable String userProfileID) {
        getConfigurator().setUserProfileID(userProfileID);
    }

    @WorkerThread
    public static void addAutoCollectedDataSubscriber(@NonNull String subscriber) {
        getConfigurator().addAutoCollectedDataSubscriber(subscriber);
    }

    @AnyThread
    public void activateCore(@Nullable AppMetricaConfig from) {
        mCore.activate(from, this);
    }

    @WorkerThread
    public void activateFull(@NonNull AppMetricaConfig config) {
        getImpl().activate(config);
        AppMetricaSelfReportFacade.onFullyInitializationFinished(mContext);
    }

    @WorkerThread
    public void activateFull(@NonNull AppMetricaLibraryAdapterConfig config) {
        getImpl().activateAnonymously(config);
        AppMetricaSelfReportFacade.onFullyInitializationFinished(mContext);
    }

    @WorkerThread
    @Nullable
    public MainReporterApiConsumerProvider getMainReporterApiConsumerProvider() {
        return getImpl().getMainReporterApiConsumerProvider();
    }

    @AnyThread
    @Nullable
    @VisibleForTesting
    // should be called only if future is already done
    MainReporterApiConsumerProvider peekMainReporterApiConsumerProvider() {
        return getImpl().getMainReporterApiConsumerProvider();
    }

    @WorkerThread
    public void requestDeferredDeeplinkParameters(DeferredDeeplinkParametersListener listener) {
        getImpl().requestDeferredDeeplinkParameters(listener);
    }

    @WorkerThread
    public void requestDeferredDeeplink(DeferredDeeplinkListener listener) {
        getImpl().requestDeferredDeeplink(listener);
    }

    @WorkerThread
    public void activateReporter(@NonNull ReporterConfig config) {
        getImpl().activateReporter(config);
    }

    @WorkerThread
    @NonNull
    public IReporterExtended getReporter(@NonNull ReporterConfig config) {
        return getImpl().getReporter(config);
    }

    @AnyThread
    @Nullable
    public String getDeviceId() {
        return getImpl().getDeviceId();
    }

    @AnyThread
    @Nullable
    public Map<String, String> getClids() {
        return getImpl().getClids();
    }

    @AnyThread
    @NonNull
    public AdvIdentifiersResult getCachedAdvIdentifiers() {
        return getImpl().getCachedAdvIdentifiers();
    }

    @AnyThread
    @NonNull
    public FeaturesResult getFeatures() {
        return getImpl().getFeatures();
    }

    @WorkerThread
    public void requestStartupParams(
            @NonNull final StartupParamsCallback callback,
            @NonNull final List<String> params
    ) {
        DebugLogger.INSTANCE.info(TAG, "requestStartupParams");
        getImpl().requestStartupParams(callback, params);
    }

    @AnyThread
    @NonNull
    public ClientTimeTracker getClientTimeTracker() {
        return mCore.getClientTimeTracker();
    }

    @AnyThread
    @NonNull
    private IAppMetricaImpl getImpl() {
        try {
            return mFullInitFuture.get();
        } catch (Exception e) {
            DebugLogger.INSTANCE.error(TAG, e);
            throw new RuntimeException(e);
        }
    }

    @AnyThread
    private static MetricaConfigurator getConfigurator() {
        return isInitializedForApp()
                ? sInstance.getImpl()
                : ClientServiceLocator.getInstance().getDefaultOneShotConfig();
    }

    @VisibleForTesting
    boolean isFullInitFutureDone() {
        return mFullInitFuture.isDone();
    }

    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    public static synchronized void setInstance(@Nullable AppMetricaFacade instance) {
        sInstance = instance;
    }

    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    public static synchronized void killInstance() {
        sInstance = null;
        sActivated = false;
        fullyInitialized = false;
    }

    @NonNull
    private IAppMetricaImpl createImpl() {
        return coreComponentsProvider.getImpl(mContext, mCore);
    }

    @NonNull
    @Override
    public IReporterFactory getReporterFactory() {
        return getImpl().getReporterFactory();
    }
}
