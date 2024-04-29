package io.appmetrica.analytics.impl;

import android.content.Context;
import android.os.Handler;
import androidx.annotation.AnyThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.AppMetricaConfig;
import io.appmetrica.analytics.coreapi.internal.executors.ICommonExecutor;
import io.appmetrica.analytics.coreapi.internal.executors.IHandlerExecutor;
import io.appmetrica.analytics.coreutils.internal.WrapUtils;
import io.appmetrica.analytics.impl.clientcomponents.ClientComponentsInitializerProvider;
import io.appmetrica.analytics.impl.utils.PublicLogger;
import io.appmetrica.analytics.impl.utils.executors.ClientExecutorProvider;

@AnyThread
public class AppMetricaCore implements IAppMetricaCore {

    @NonNull
    private final Context mContext;
    @NonNull
    private final Handler mMetricaHandler;
    @NonNull
    private final ClientTimeTracker mClientTimeTracker;
    @NonNull
    private final ICommonExecutor mDefaultExecutor;
    @NonNull
    private final AppOpenWatcher appOpenWatcher;
    @Nullable
    private volatile AppMetricaUncaughtExceptionHandler mUncaughtExceptionHandler;
    private boolean activated = false;

    AppMetricaCore(@NonNull Context context, @NonNull ClientExecutorProvider clientExecutorProvider) {
        this(
                context.getApplicationContext(),
                clientExecutorProvider.getDefaultExecutor(),
                clientExecutorProvider.getApiProxyExecutor()
        );
    }

    private AppMetricaCore(@NonNull Context context,
                              @NonNull IHandlerExecutor defaultExecutor,
                              @NonNull ICommonExecutor apiProxyExecutor) {
        this(
                context,
                defaultExecutor,
                new ClientTimeTracker(),
                new AppOpenWatcher(apiProxyExecutor)
        );
    }

    @VisibleForTesting
    AppMetricaCore(@NonNull Context context,
                      @NonNull IHandlerExecutor defaultExecutor,
                      @NonNull ClientTimeTracker clientTimeTracker,
                      @NonNull AppOpenWatcher appOpenWatcher) {
        mContext = context;
        mDefaultExecutor = defaultExecutor;
        this.appOpenWatcher = appOpenWatcher;
        PublicLogger.init(mContext);
        defaultExecutor.execute(new Runnable() {
            @Override
            public void run() {
                SdkUtils.logSdkInfo();
            }
        });
        mMetricaHandler = defaultExecutor.getHandler();
        mClientTimeTracker = clientTimeTracker;
        mClientTimeTracker.trackCoreCreation();
        new ClientComponentsInitializerProvider()
            .getClientComponentsInitializer()
            .onCreate();
    }

    @Override
    public synchronized void activate(@NonNull final AppMetricaConfig config,
                                      @NonNull IReporterFactoryProvider reporterFactoryProvider) {
        if (!activated) {
            final boolean crashReportingEnabled = WrapUtils.getOrDefault(
                    config.crashReporting, DefaultValuesForCrashReporting.DEFAULT_REPORTS_CRASHES_ENABLED);
            if (crashReportingEnabled && mUncaughtExceptionHandler == null) {
                mUncaughtExceptionHandler = createUncaughtExceptionHandler(config, reporterFactoryProvider);
                Thread.setDefaultUncaughtExceptionHandler(mUncaughtExceptionHandler);
            }
            final boolean appOpenTrackingEnabled = WrapUtils.getOrDefault(
                    config.appOpenTrackingEnabled,
                    DefaultValues.DEFAULT_APP_OPEN_TRACKING_ENABLED
            );
            if (appOpenTrackingEnabled) {
                appOpenWatcher.startWatching();
            }
            activated = true;
        }
    }

    @Override
    @NonNull
    public Handler getMetricaHandler() {
        return mMetricaHandler;
    }

    @Override
    @NonNull
    public ClientTimeTracker getClientTimeTracker() {
        return mClientTimeTracker;
    }

    @Override
    @NonNull
    public ICommonExecutor getExecutor() {
        return mDefaultExecutor;
    }

    @AnyThread
    @NonNull
    private AppMetricaUncaughtExceptionHandler createUncaughtExceptionHandler(
            @NonNull AppMetricaConfig config,
            @NonNull IReporterFactoryProvider reporterFactoryProvider
    ) {
        return new AppMetricaUncaughtExceptionHandler(
                Thread.getDefaultUncaughtExceptionHandler(),
                ClientServiceLocator.getInstance()
                        .getCrashProcessorFactory()
                        .createCrashProcessors(
                                mContext,
                                config,
                                reporterFactoryProvider
                        )
        );
    }

    @Override
    @NonNull
    public AppOpenWatcher getAppOpenWatcher() {
        return appOpenWatcher;
    }

    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    @Nullable
    AppMetricaUncaughtExceptionHandler getUncaughtExceptionHandler() {
        return mUncaughtExceptionHandler;
    }
}
