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
import io.appmetrica.analytics.DeferredDeeplinkListener;
import io.appmetrica.analytics.DeferredDeeplinkParametersListener;
import io.appmetrica.analytics.ReporterConfig;
import io.appmetrica.analytics.StartupParamsCallback;
import io.appmetrica.analytics.coreutils.internal.logger.YLogger;
import io.appmetrica.analytics.impl.id.IAdvertisingIdGetter;
import io.appmetrica.analytics.impl.selfreporting.AppMetricaSelfReportFacade;
import io.appmetrica.analytics.impl.utils.executors.ClientExecutorProvider;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

public class AppMetricaFacade implements IReporterFactoryProvider {

    private static final String TAG = "[AppMetricaImpl]";

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
    @NonNull
    private final ClientExecutorProvider mClientExecutorProvider;
    private static volatile boolean sActivated = false;

    @AnyThread
    private AppMetricaFacade(@NonNull final Context context) {
        this(
                context.getApplicationContext(),
                new AppMetricaCoreComponentsProvider(),
                ClientServiceLocator.getInstance().getClientExecutorProvider()
        );
    }

    private AppMetricaFacade(@NonNull Context context,
                                @NonNull AppMetricaCoreComponentsProvider coreComponentsProvider,
                                @NonNull ClientExecutorProvider clientExecutorProvider) {
        this(
                context,
                coreComponentsProvider,
                coreComponentsProvider.getCore(context, clientExecutorProvider),
                clientExecutorProvider
        );
    }

    @VisibleForTesting
    AppMetricaFacade(@NonNull final Context context,
                        @NonNull AppMetricaCoreComponentsProvider coreComponentsProvider,
                        @NonNull IAppMetricaCore core,
                        @NonNull ClientExecutorProvider clientExecutorProvider) {
        mContext = context;
        this.coreComponentsProvider = coreComponentsProvider;
        mCore = core;
        mClientExecutorProvider = clientExecutorProvider;
        mFullInitFuture = new FutureTask<IAppMetricaImpl>(new Callable<IAppMetricaImpl>() {
            @Override
            public IAppMetricaImpl call() throws Exception {
                return createImpl();
            }
        });
        mClientExecutorProvider.getDefaultExecutor().execute(new Runnable() {
            @Override
            public void run() {
                new ClientMigrationManager(context).checkMigration(context);
                ClientServiceLocator.getInstance().getMultiProcessSafeUuidProvider(context).readUuid();
            }
        });
        mClientExecutorProvider.getDefaultExecutor().execute(mFullInitFuture);
    }

    @AnyThread
    @Nullable
    public static AppMetricaFacade peekInstance() {
        return sInstance;
    }

    @AnyThread
    @NonNull
    public static AppMetricaFacade getInstance(@NonNull final Context context) {
        if (sInstance == null) {
            synchronized (AppMetricaFacade.class) {
                if (sInstance == null) {
                    sInstance = new AppMetricaFacade(context);
                    sInstance.onInstanceCreated();
                }
            }
        }
        return sInstance;
    }

    private void onInstanceCreated() {
        mClientExecutorProvider.getDefaultExecutor().execute(new Runnable() {
            @Override
            public void run() {
                mClientExecutorProvider.getApiProxyExecutor().execute(new Runnable() {
                    @Override
                    public void run() {
                        AppMetricaSelfReportFacade.onInitializationFinished(mContext);                    }
                });
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
    public synchronized static boolean isInitializedForApp() {
        return sInstance != null && sInstance.isFullyInitialized() &&
                sInstance.peekMainReporterApiConsumerProvider() != null;
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
    public static void setStatisticsSending(final boolean enabled) {
        getConfigurator().setStatisticsSending(enabled);
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

    @AnyThread
    public void activateCore(@NonNull AppMetricaConfig from) {
        mCore.activate(from, this);
    }

    @WorkerThread
    public void activateFull(@NonNull AppMetricaConfig originalConfig,
                             @NonNull AppMetricaConfig from) {
        getImpl().activate(originalConfig, from);
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
        getImpl().requestStartupParams(callback, params);
    }

    @AnyThread
    @NonNull
    public ClientTimeTracker getClientTimeTracker() {
        return mCore.getClientTimeTracker();
    }

    @AnyThread
    @NonNull
    public IAdvertisingIdGetter getAdvertisingIdGetter() {
        return mCore.getAdvertisingIdGetter();
    }

    @AnyThread
    @NonNull
    private IAppMetricaImpl getImpl() {
        try {
            return mFullInitFuture.get();
        } catch (Exception e) {
            YLogger.e(e, TAG);
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
    boolean isFullyInitialized() {
        return mFullInitFuture.isDone();
    }

    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    public static void setInstance(@Nullable AppMetricaFacade instance) {
        sInstance = instance;
    }

    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    public static void killInstance() {
        sInstance = null;
        sActivated = false;
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
