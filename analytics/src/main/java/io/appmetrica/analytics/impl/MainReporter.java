package io.appmetrica.analytics.impl;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.annotation.WorkerThread;
import io.appmetrica.analytics.AppMetricaConfig;
import io.appmetrica.analytics.CounterConfiguration;
import io.appmetrica.analytics.coreapi.internal.executors.ICommonExecutor;
import io.appmetrica.analytics.coreutils.internal.WrapUtils;
import io.appmetrica.analytics.coreutils.internal.logger.YLogger;
import io.appmetrica.analytics.impl.client.ProcessConfiguration;
import io.appmetrica.analytics.impl.crash.ANRMonitor;
import io.appmetrica.analytics.impl.crash.PluginErrorDetailsConverter;
import io.appmetrica.analytics.impl.crash.client.AllThreads;
import io.appmetrica.analytics.impl.crash.client.converter.AnrConverter;
import io.appmetrica.analytics.impl.crash.client.converter.CustomErrorConverter;
import io.appmetrica.analytics.impl.crash.client.converter.RegularErrorConverter;
import io.appmetrica.analytics.impl.crash.client.converter.UnhandledExceptionConverter;
import io.appmetrica.analytics.impl.crash.ndk.NdkCrashHelper;
import io.appmetrica.analytics.impl.crash.utils.ThreadsStateDumper;
import io.appmetrica.analytics.impl.preloadinfo.PreloadInfoWrapper;
import io.appmetrica.analytics.impl.reporter.MainReporterContext;
import io.appmetrica.analytics.impl.reporter.ReporterLifecycleListener;
import io.appmetrica.analytics.impl.startup.StartupHelper;
import io.appmetrica.analytics.impl.utils.ApiProxyThread;
import io.appmetrica.analytics.impl.utils.BooleanUtils;
import io.appmetrica.analytics.impl.utils.ProcessDetector;
import io.appmetrica.analytics.impl.utils.validation.NonEmptyStringValidator;
import io.appmetrica.analytics.impl.utils.validation.ThrowIfFailedValidator;
import io.appmetrica.analytics.impl.utils.validation.Validator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class MainReporter extends BaseReporter implements IMainReporter {

    private static final String TAG = "[MainReporter]";

    private static final Validator<String> sReferralUrlValidator = new ThrowIfFailedValidator<String>(
            new NonEmptyStringValidator("Referral url")
    );

    private static final Long USER_SESSION_TIMEOUT = TimeUnit.SECONDS.toMillis(5);

    @NonNull
    private final AppStatusMonitor mAppStatusMonitor;
    @NonNull
    private final AppMetricaConfig mConfig;
    @NonNull
    private ANRMonitor anrMonitor;
    @NonNull
    private final ActivityStateManager activityStateManager;
    private final AtomicBoolean anrMonitorBarrier = new AtomicBoolean(false);
    private final ThreadsStateDumper threadsStateDumper = new ThreadsStateDumper();
    @NonNull
    private final NdkCrashHelper nativeCrashesHelper;

    @WorkerThread
    public MainReporter(@NonNull Context context,
                        @NonNull ProcessConfiguration processConfiguration,
                        @NonNull AppMetricaConfig config,
                        @NonNull ReportsHandler reportsHandler,
                        @NonNull StartupHelper startupHelper,
                        @NonNull UnhandledSituationReporterProvider appmetricaReporterProvider,
                        @NonNull UnhandledSituationReporterProvider pushReporterProvider) {
        this(
                context,
                processConfiguration,
                config,
                reportsHandler,
                new NdkCrashHelper(context, processConfiguration),
                startupHelper,
                appmetricaReporterProvider,
                pushReporterProvider,
                ClientServiceLocator.getInstance(),
                new ExtraMetaInfoRetriever(context)
        );
    }

    @VisibleForTesting
    @WorkerThread
    MainReporter(@NonNull Context context,
                 @NonNull ProcessConfiguration processConfiguration,
                 @NonNull AppMetricaConfig config,
                 @NonNull ReportsHandler reportsHandler,
                 @NonNull NdkCrashHelper nativeCrashesHelper,
                 @NonNull StartupHelper startupHelper,
                 @NonNull UnhandledSituationReporterProvider appmetricaReporterProvider,
                 @NonNull UnhandledSituationReporterProvider pushReporterProvider,
                 @NonNull ClientServiceLocator clientServiceLocator,
                 @NonNull ExtraMetaInfoRetriever extraMetaInfoRetriever) {
        this(
                context,
                config,
                reportsHandler,
                nativeCrashesHelper,
                new ReporterEnvironment(
                        processConfiguration,
                        new CounterConfiguration(config, CounterConfigurationReporterType.MAIN),
                        config.userProfileID
                ),
                new AppStatusMonitor(getSessionTimeout(config)),
                startupHelper,
                new LibraryAnrDetector(),
                clientServiceLocator.getProcessDetector(),
                appmetricaReporterProvider,
                pushReporterProvider,
                clientServiceLocator.getApiProxyExecutor(),
                extraMetaInfoRetriever,
                new ActivityStateManager(),
                new PluginErrorDetailsConverter(extraMetaInfoRetriever),
                new UnhandledExceptionConverter(),
                new RegularErrorConverter(),
                new CustomErrorConverter(),
                new AnrConverter()
        );
    }

    @SuppressWarnings("checkstyle:methodlength")
    @VisibleForTesting
    @WorkerThread
    MainReporter(@NonNull Context context,
                 @NonNull AppMetricaConfig config,
                 @NonNull ReportsHandler reportsHandler,
                 @NonNull NdkCrashHelper nativeCrashesHelper,
                 @NonNull ReporterEnvironment reporterEnvironment,
                 @NonNull AppStatusMonitor appStatusMonitor,
                 @NonNull StartupHelper startupHelper,
                 @NonNull final LibraryAnrDetector libraryAnrDetector,
                 @NonNull ProcessDetector processDetector,
                 @NonNull final UnhandledSituationReporterProvider appmetricaReporterProvider,
                 @NonNull final UnhandledSituationReporterProvider pushReporterProvider,
                 @NonNull final ICommonExecutor executor,
                 @NonNull ExtraMetaInfoRetriever extraMetaInfoRetriever,
                 @NonNull ActivityStateManager activityStateManager,
                 @NonNull PluginErrorDetailsConverter pluginErrorDetailsConverter,
                 @NonNull UnhandledExceptionConverter unhandledExceptionConverter,
                 @NonNull RegularErrorConverter regularErrorConverter,
                 @NonNull CustomErrorConverter customErrorConverter,
                 @NonNull AnrConverter anrConverter) {
        super(
                context,
                reportsHandler,
                reporterEnvironment,
                extraMetaInfoRetriever,
                processDetector,
                unhandledExceptionConverter,
                regularErrorConverter,
                customErrorConverter,
                anrConverter,
                pluginErrorDetailsConverter
        );
        //todo (avitenko) create CounterConfiguration from config METRIKALIB-4520
        // Create environment for SDK's crashes to be able to send to the separate API-key
        mReporterEnvironment.setPreloadInfoWrapper(createPreloadInfoWrapper(config));
        mAppStatusMonitor = appStatusMonitor;
        this.nativeCrashesHelper = nativeCrashesHelper;
        mConfig = config;
        this.activityStateManager = activityStateManager;
        setReportNativeCrashesEnabled(config.nativeCrashReporting, mReporterEnvironment);
        anrMonitor = createAnrMonitor(
            executor,
            libraryAnrDetector,
            appmetricaReporterProvider,
            pushReporterProvider,
            config.anrMonitoringTimeout
        );
        if (BooleanUtils.isTrue(config.anrMonitoring)) {
            enableAnrMonitoring();
        }
        initUserSessionObserver();
        final ReporterLifecycleListener listener =
                ClientServiceLocator.getInstance().getReporterLifecycleListener();
        if (listener != null) {
            final MainReporterContext mainReporterContext = new MainReporterContext(
                    context.getApplicationContext(),
                    appStatusMonitor,
                    mConfig,
                    startupHelper.getDeviceId(),
                    mPublicLogger,
                    reportsHandler
            );
            listener.onCreateMainReporter(mainReporterContext);
        }
        if (mPublicLogger.isEnabled()) {
            mPublicLogger.i("Actual sessions timeout is " + getSessionTimeout(config));
        }
    }

    @Override
    public final void enableAnrMonitoring() {
        if (anrMonitorBarrier.compareAndSet(false, true)) {
            anrMonitor.startMonitoring();
        }
    }

    private void logAppOpen(@Nullable final String deeplink) {
        if (mPublicLogger.isEnabled()) {
            StringBuilder builder = new StringBuilder("App opened via deeplink: ");
            builder.append(WrapUtils.wrapToTag(deeplink));
            mPublicLogger.i(builder.toString());
        }

    }

    @Override
    public void reportAppOpen(@NonNull String deeplink, boolean auto) {
        logAppOpen(deeplink);
        mReportsHandler.reportEvent(
                EventsManager.openAppReportEntry(deeplink, auto, mPublicLogger),
                mReporterEnvironment
        );
    }

    @Override
    public void reportReferralUrl(@NonNull String referralUrl) {
        sReferralUrlValidator.validate(referralUrl);
        mReportsHandler.reportEvent(EventsManager.referralUrlReportEntry(referralUrl, mPublicLogger),
                mReporterEnvironment);
        logReferralUrl(referralUrl);
    }

    private void logReferralUrl(@Nullable final String referralUrl) {
        if (mPublicLogger.isEnabled()) {
            StringBuilder builder = new StringBuilder("Referral URL received: ");
            builder.append(WrapUtils.wrapToTag(referralUrl));
            mPublicLogger.i(builder.toString());
        }
    }

    @Override
    public void onEnableAutoTrackingAttemptOccurred(@NonNull ActivityLifecycleManager.WatchingStatus status) {
        if (status == ActivityLifecycleManager.WatchingStatus.WATCHING) {
            if (mPublicLogger.isEnabled()) {
                mPublicLogger.i("Enable activity auto tracking");
            }
        } else {
            if (mPublicLogger.isEnabled()) {
                mPublicLogger.w("Could not enable activity auto tracking. " + status.error);
            }
        }
    }

    @Override
    @ApiProxyThread
    public void resumeSession(@Nullable final Activity activity) {
        if (activityStateManager.didStateChange(activity, ActivityStateManager.ActivityState.RESUMED)) {
            if (mPublicLogger.isEnabled()) {
                mPublicLogger.i("Resume session");
            }
            onResumeForegroundSession(getActivityTag(activity));
            mAppStatusMonitor.resume();
        }
    }

    @Override
    @ApiProxyThread
    public void pauseSession(@Nullable final Activity activity) {
        if (activityStateManager.didStateChange(activity, ActivityStateManager.ActivityState.PAUSED)) {
            if (mPublicLogger.isEnabled()) {
                mPublicLogger.i("Pause session");
            }
            onPauseForegroundSession(getActivityTag(activity));
            mAppStatusMonitor.pause();
        }
    }

    String getActivityTag(@Nullable final Activity activity) {
        String tag = null;
        if (activity != null) {
            tag = activity.getClass().getSimpleName();
        }
        return tag;
    }

    void updateConfig(AppMetricaConfig config, final boolean needToClearEnvironment) {
        if (needToClearEnvironment) {
            clearAppEnvironment();
        }
        putAllToAppEnvironment(config.appEnvironment);
        putAllToErrorEnvironment(config.errorEnvironment);
    }

    @Override
    public void setLocation(@Nullable final Location location) {
        mReporterEnvironment.getReporterConfiguration().setManualLocation(location);
        if (mPublicLogger.isEnabled()) {
            mPublicLogger.fi("Set location: %s", location);
        }
    }

    @Override
    public void setLocationTracking(final boolean enabled) {
        mReporterEnvironment.getReporterConfiguration().setLocationTracking(enabled);
    }

    @Override
    public List<String> getCustomHosts() {
        return mReporterEnvironment.getProcessConfiguration().getCustomHosts();
    }

    @NonNull
    private ANRMonitor createAnrMonitor(@NonNull final ICommonExecutor executor,
                                        @NonNull final LibraryAnrDetector libraryAnrDetector,
                                        @NonNull final UnhandledSituationReporterProvider appmetricaReporterProvider,
                                        @NonNull final UnhandledSituationReporterProvider pushReporterProvider,
                                        @Nullable final Integer anrMonitoringTimeout) {
        final ANRMonitor.Listener anrMonitoringListener = new ANRMonitor.Listener() {
            @Override
            public void onAppNotResponding() {
                final AllThreads allThreads = threadsStateDumper.getThreadsDumpForAnr();
                executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        reportAnr(allThreads);
                        if (libraryAnrDetector.isAppmetricaAnr(allThreads.affectedThread.stacktrace)) {
                            appmetricaReporterProvider.getReporter().reportAnr(allThreads);
                        }
                        if (libraryAnrDetector.isPushAnr(allThreads.affectedThread.stacktrace)) {
                            pushReporterProvider.getReporter().reportAnr(allThreads);
                        }
                        YLogger.d("anr registered %s", allThreads);
                    }
                });
            }
        };
        return new ANRMonitor(anrMonitoringListener, anrMonitoringTimeout);
    }

    private void initUserSessionObserver() {
        mReportsHandler.reportPauseUserSession(mReporterEnvironment.getProcessConfiguration());
        mAppStatusMonitor.registerObserver(
                new AppStatusMonitor.Observer() {
                    @Override
                    public void onResume() {
                        mReportsHandler.reportResumeUserSession(mReporterEnvironment.getProcessConfiguration());
                    }

                    @Override
                    public void onPause() {
                        mReportsHandler.reportPauseUserSession(mReporterEnvironment.getProcessConfiguration());
                    }
                },
                USER_SESSION_TIMEOUT
        );
    }

    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    void setAnrMonitor(@NonNull ANRMonitor anrMonitor) {
        this.anrMonitor = anrMonitor;
    }

    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    @NonNull
    ANRMonitor getAnrMonitor() {
        return anrMonitor;
    }

    @Override
    public void onWebViewReportingInit(@NonNull WebViewJsInterfaceHandler webViewJsInterfaceHandler) {
        webViewJsInterfaceHandler.setLogger(mPublicLogger);
    }

    @WorkerThread
    private void setReportNativeCrashesEnabled(@Nullable Boolean enabledFromConfig,
                                               final ReporterEnvironment reporterEnvironment) {
        final boolean enabled = WrapUtils.getOrDefault(
                enabledFromConfig,
                DefaultValuesForCrashReporting.DEFAULT_REPORTS_NATIVE_CRASHES_ENABLED
        );
        nativeCrashesHelper.setReportsEnabled(enabled,
                reporterEnvironment.getReporterConfiguration().getApiKey(),
                reporterEnvironment.getErrorEnvironment()
        );
        if (mPublicLogger.isEnabled()) {
            mPublicLogger.fi("Set report native crashes enabled: %b", enabled);
        }
    }

    @Override
    public void putErrorEnvironmentValue(String key, String value) {
        super.putErrorEnvironmentValue(key, value);
        nativeCrashesHelper.updateErrorEnvironment(mReporterEnvironment.getErrorEnvironment());
    }

    @NonNull
    private PreloadInfoWrapper createPreloadInfoWrapper(@NonNull AppMetricaConfig config) {
        return new PreloadInfoWrapper(
                config.preloadInfo,
                mPublicLogger,
                WrapUtils.getOrDefault(
                    AppMetricaInternalConfigExtractor.getPreloadInfoAutoTracking(config),
                    DefaultValues.DEFAULT_AUTO_PRELOAD_INFO_DETECTION
                )
        );
    }

    @Override
    protected String getTag() {
        return TAG;
    }

    private static long getSessionTimeout(@NonNull final AppMetricaConfig config) {
        return config.sessionTimeout == null ?
            TimeUnit.SECONDS.toMillis(DefaultValues.DEFAULT_SESSION_TIMEOUT_SECONDS) :
            config.sessionTimeout;
    }
}
