package io.appmetrica.analytics.impl;

import android.app.Activity;
import android.location.Location;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import io.appmetrica.analytics.AnrListener;
import io.appmetrica.analytics.AppMetricaConfig;
import io.appmetrica.analytics.ExternalAttribution;
import io.appmetrica.analytics.coreutils.internal.WrapUtils;
import io.appmetrica.analytics.coreutils.internal.collection.CollectionUtils;
import io.appmetrica.analytics.impl.crash.jvm.client.MainReporterAnrController;
import io.appmetrica.analytics.impl.utils.ApiProxyThread;
import io.appmetrica.analytics.impl.utils.validation.NonEmptyStringValidator;
import io.appmetrica.analytics.impl.utils.validation.ThrowIfFailedValidator;
import io.appmetrica.analytics.impl.utils.validation.Validator;
import io.appmetrica.analytics.internal.CounterConfiguration;
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MainReporter extends BaseReporter implements IMainReporter {

    private static final String TAG = "[MainReporter]";

    private final Validator<String> referralUrlValidator = new ThrowIfFailedValidator<>(
        new NonEmptyStringValidator("Referral url")
    );

    private static final Long USER_SESSION_TIMEOUT = TimeUnit.SECONDS.toMillis(1);
    @NonNull
    private final MainReporterComponents mainReporterComponents;
    @NonNull
    private final MainReporterAnrController mainReporterAnrController;

    @WorkerThread
    MainReporter(@NonNull MainReporterComponents mainReporterComponents) {
        super(
            mainReporterComponents.getContext(),
            mainReporterComponents.getReportsHandler(),
            mainReporterComponents.getReporterEnvironment(),
            mainReporterComponents.getExtraMetaInfoRetriever(),
            mainReporterComponents.getProcessDetector(),
            mainReporterComponents.getUnhandledExceptionConverter(),
            mainReporterComponents.getRegularErrorConverter(),
            mainReporterComponents.getCustomErrorConverter(),
            mainReporterComponents.getAnrConverter(),
            mainReporterComponents.getPluginErrorDetailsConverter()
        );

        this.mainReporterComponents = mainReporterComponents;
        mainReporterAnrController = new MainReporterAnrController(this);
        initUserSessionObserver();
    }

    @Override
    public void start() {
        super.start();
        DebugLogger.INSTANCE.info(getTag(), "Start");
        ClientServiceLocator.getInstance().getModulesController().onActivated();
    }

    @Override
    public void enableAnrMonitoring() {
        mainReporterAnrController.enableAnrMonitoring();
    }

    @Override
    public void reportAppOpen(@NonNull String deeplink, boolean auto) {
        mPublicLogger.info("App opened via deeplink: " + WrapUtils.wrapToTag(deeplink));
        mReportsHandler.reportEvent(
                EventsManager.openAppReportEntry(deeplink, auto, mPublicLogger),
                mReporterEnvironment
        );
    }

    @Override
    public void reportReferralUrl(@NonNull String referralUrl) {
        referralUrlValidator.validate(referralUrl);
        mReportsHandler.reportEvent(EventsManager.referralUrlReportEntry(referralUrl, mPublicLogger),
                mReporterEnvironment);
        mPublicLogger.info("Referral URL received: " + WrapUtils.wrapToTag(referralUrl));
    }

    @Override
    public void onEnableAutoTrackingAttemptOccurred(@NonNull ActivityLifecycleManager.WatchingStatus status) {
        if (status == ActivityLifecycleManager.WatchingStatus.WATCHING) {
            mPublicLogger.info("Enable activity auto tracking");
        } else {
            mPublicLogger.warning("Could not enable activity auto tracking. " + status.error);
        }
    }

    @Override
    @ApiProxyThread
    public void resumeSession(@Nullable final Activity activity) {
        if (mainReporterComponents.getActivityStateManager().didStateChange(
            activity,
            ActivityStateManager.ActivityState.RESUMED
        )) {
            mPublicLogger.info("Resume session");
            onResumeForegroundSession(getActivityTag(activity));
            mainReporterComponents.getAppStatusMonitor().resume();
        }
    }

    @Override
    @ApiProxyThread
    public void pauseSession(@Nullable final Activity activity) {
        if (mainReporterComponents.getActivityStateManager().didStateChange(
            activity,
            ActivityStateManager.ActivityState.PAUSED
        )) {
            mPublicLogger.info("Pause session");
            onPauseForegroundSession(getActivityTag(activity));
            mainReporterComponents.getAppStatusMonitor().pause();
        }
    }

    String getActivityTag(@Nullable final Activity activity) {
        String tag = null;
        if (activity != null) {
            tag = activity.getClass().getSimpleName();
        }
        return tag;
    }

    void updateConfig(@NonNull AppMetricaConfig config, @NonNull AppMetricaConfigExtension configExtension) {
        DebugLogger.INSTANCE.info(getTag(), "Update config: %s; configExtension: $s", config, configExtension);

        if (configExtension.getNeedClearEnvironment()) {
            clearAppEnvironment();
        }

        List<String> autoCollectedDataSubscribers = configExtension.getAutoCollectedDataSubscribers();
        if (!CollectionUtils.isNullOrEmpty(autoCollectedDataSubscribers)) {
            DebugLogger.INSTANCE.info(getTag(), "Add auto collected subscribers: %s", autoCollectedDataSubscribers);
            mReporterEnvironment.getReporterConfiguration()
                .addAutoCollectedDataSubscribers(autoCollectedDataSubscribers);
        }

        putAllToAppEnvironment(config.appEnvironment);
        putAllToErrorEnvironment(config.errorEnvironment);

        enableNativeCrashHandling(config.nativeCrashReporting);
        mainReporterAnrController.updateConfig(config);
    }

    @Override
    public void setLocation(@Nullable final Location location) {
        mReporterEnvironment.getReporterConfiguration().setManualLocation(location);
        mPublicLogger.info("Set location: %s", location);
    }

    @Override
    public void setLocationTracking(final boolean enabled) {
        mReporterEnvironment.getReporterConfiguration().setLocationTracking(enabled);
    }

    @Override
    public void setAdvIdentifiersTracking(boolean enabled, boolean force) {
        mPublicLogger.info("Set advIdentifiersTracking to %s", enabled);
        CounterConfiguration counterConfiguration = mReporterEnvironment.getReporterConfiguration();
        if (force || counterConfiguration.isAdvIdentifiersTrackingEnabled() == null) {
            counterConfiguration.setAdvIdentifiersTracking(enabled);
        }
    }

    @Override
    public List<String> getCustomHosts() {
        return mReporterEnvironment.getProcessConfiguration().getCustomHosts();
    }

    private void initUserSessionObserver() {
        mReportsHandler.reportPauseUserSession(mReporterEnvironment.getProcessConfiguration());
        mainReporterComponents.getAppStatusMonitor().registerObserver(
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

    @Override
    public void onWebViewReportingInit(@NonNull WebViewJsInterfaceHandler webViewJsInterfaceHandler) {
        webViewJsInterfaceHandler.setLogger(mPublicLogger);
    }

    @Override
    public void registerAnrListener(@NonNull final AnrListener listener) {
        mainReporterAnrController.registerListener(listener);
    }

    @Override
    public void reportExternalAttribution(@NonNull ExternalAttribution value) {
        mPublicLogger.info("External attribution received: %s", value);
        mReportsHandler.reportEvent(
            EventsManager.clientExternalAttributionEntry(value.toBytes(), mPublicLogger),
            mReporterEnvironment
        );
    }

    @WorkerThread
    private void enableNativeCrashHandling(@Nullable Boolean enabledFromConfig) {
        final boolean enabled = WrapUtils.getOrDefault(
            enabledFromConfig,
            DefaultValuesForCrashReporting.DEFAULT_REPORTS_NATIVE_CRASHES_ENABLED
        );
        mPublicLogger.info("native crash reporting enabled: %b", enabled);
        if (enabled) {
            mainReporterComponents.getNativeCrashClient().initHandling(
                mContext,
                mReporterEnvironment.getReporterConfiguration().getApiKey(),
                mReporterEnvironment.getErrorEnvironment()
            );
        }
    }

    @Override
    public void putErrorEnvironmentValue(String key, String value) {
        super.putErrorEnvironmentValue(key, value);
        mainReporterComponents.getNativeCrashClient().updateErrorEnvironment(
            mReporterEnvironment.getErrorEnvironment()
        );
    }

    @Override
    public void addAutoCollectedDataSubscriber(@NonNull String subscriber) {
        mPublicLogger.info("Add auto collected data subscriber: %s", subscriber);
        mReporterEnvironment.getReporterConfiguration().addAutoCollectedDataSubscriber(subscriber);
    }

    @Override
    protected String getTag() {
        return TAG;
    }

}
