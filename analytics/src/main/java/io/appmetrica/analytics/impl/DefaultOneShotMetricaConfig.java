package io.appmetrica.analytics.impl;

import android.location.Location;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.AppMetricaConfig;
import io.appmetrica.analytics.coreutils.internal.logger.YLogger;
import java.util.LinkedHashMap;
import java.util.Map;

import static io.appmetrica.analytics.impl.Utils.isFieldSet;

/**
 * Config with values which will be used for metrica initialization,
 * in case if there are no such values in user config. This object lives according to one-shot
 * strategy. After merging with another config, default configuration will reset it state forever.
 */
public class DefaultOneShotMetricaConfig implements MetricaConfigurator {

    private static final String TAG = "[DefaultOneShotMetricaConfig]";

    private Location mLocation;
    private Boolean mLocationTracking;
    private Boolean mStatisticsSending;
    private Map<String, String> mAppEnvironment = new LinkedHashMap<String, String>();
    private Map<String, String> mErrorEnvironment = new LinkedHashMap<String, String>();

    private boolean mAppEnvironmentWasCleared;

    @Nullable
    private String userProfileID;

    private boolean mUsed;
    private ReportsHandler mReportsHandler;

    public Location getLocation() {
        return mLocation;
    }

    public Boolean isLocationTrackingEnabled() {
        return mLocationTracking;
    }

    @Override
    public void setLocationTracking(final boolean enabled) {
        YLogger.info(TAG, "setLocationTracking: %b", enabled);
        mLocationTracking = enabled;
        tryToUpdatePreActivationConfig();
    }

    public Boolean isStatisticsSendingEnabled() {
        return mStatisticsSending;
    }

    @Override
    public void setStatisticsSending(boolean value) {
        YLogger.info(TAG, "setStatisticsSending: %b", value);
        mStatisticsSending = value;
        tryToUpdatePreActivationConfig();
    }

    @Override
    public void setLocation(@Nullable final Location location) {
        YLogger.info(TAG, "setLocation: %s", location);
        mLocation = location;
    }

    @Override
    public void putAppEnvironmentValue(final String key, final String value) {
        YLogger.info(TAG, "putAppEnvironmentValue: {%s: %s}", key, value);
        mAppEnvironment.put(key, value);
    }

    @Override
    public void clearAppEnvironment() {
        YLogger.info(TAG, "clearAppEnvironment");
        mAppEnvironmentWasCleared = true;
        mAppEnvironment.clear();
    }

    public boolean wasAppEnvironmentCleared() {
        return mAppEnvironmentWasCleared;
    }

    @Override
    public void putErrorEnvironmentValue(final String key, final String value) {
        YLogger.info(TAG, "putErrorEnvironmentValue: {%s: %s}", key, value);
        mErrorEnvironment.put(key, value);
    }

    @Override
    public void setUserProfileID(@Nullable String userProfileID) {
        YLogger.info(TAG, "setUserProfileID: %s", userProfileID);
        this.userProfileID = userProfileID;
    }

    private void reset() {
        mLocation = null;
        mLocationTracking = null;
        mStatisticsSending = null;
        mAppEnvironment.clear();
        mErrorEnvironment.clear();

        mAppEnvironmentWasCleared = false;
        userProfileID = null;
    }

    public AppMetricaConfig mergeWithUserConfig(final AppMetricaConfig config) {
        YLogger.info(TAG, "mergeWithUserConfig. used? %b", mUsed);
        if (mUsed) {
            return config;
        } else {
            AppMetricaConfig.Builder builder = createBuilderFromConfig(config);
            mergeCommonPart(config, builder);

            mUsed = true;
            reset();
            return builder.build();
        }
    }

    private AppMetricaConfig.Builder createBuilderFromConfig(final AppMetricaConfig config) {
        AppMetricaConfig.Builder builder = AppMetricaConfig.newConfigBuilder(config.apiKey);
        builder.withDeviceType(config.deviceType);
        builder.withPreloadInfo(config.preloadInfo);
        builder.withLocation(config.location);
        addOptionalFields(builder, config);
        putAppEnvironmentToBuilder(mAppEnvironment, builder);
        putAppEnvironmentToBuilder(config.appEnvironment, builder);
        putErrorEnvironmentToBuilder(mErrorEnvironment, builder);
        putErrorEnvironmentToBuilder(config.errorEnvironment, builder);
        putAdditionalConfigToBuilder(config.additionalConfig, builder);
        return builder;
    }

    @SuppressWarnings("checkstyle:methodLength")
    private void addOptionalFields(@NonNull AppMetricaConfig.Builder builder,
                                   @NonNull AppMetricaConfig config) {
        if (isFieldSet(config.appVersion)) {
            builder.withAppVersion(config.appVersion);
        }
        if (isFieldSet(config.dispatchPeriodSeconds)) {
            builder.withDispatchPeriodSeconds(config.dispatchPeriodSeconds);
        }
        if (isFieldSet(config.appBuildNumber)) {
            builder.withAppBuildNumber(config.appBuildNumber);
        }
        if (isFieldSet(config.maxReportsCount)) {
            builder.withMaxReportCount(config.maxReportsCount);
        }
        if (isFieldSet(config.logs) && config.logs) {
            builder.withLogs();
        }
        if (isFieldSet(config.sessionTimeout)) {
            builder.withSessionTimeout(config.sessionTimeout);
        }
        if (isFieldSet(config.crashReporting)) {
            builder.withCrashReporting(config.crashReporting);
        }
        if (isFieldSet(config.nativeCrashReporting)) {
            builder.withNativeCrashReporting(config.nativeCrashReporting);
        }
        if (isFieldSet(config.locationTracking)) {
            builder.withLocationTracking(config.locationTracking);
        }
        if (isFieldSet(config.firstActivationAsUpdate)) {
            builder.handleFirstActivationAsUpdate(config.firstActivationAsUpdate);
        }
        if (isFieldSet(config.statisticsSending)) {
            builder.withStatisticsSending(config.statisticsSending);
        }
        if (isFieldSet(config.anrMonitoring)) {
            builder.withAnrMonitoring(config.anrMonitoring);
        }
        if (isFieldSet(config.anrMonitoringTimeout)) {
            builder.withAnrMonitoringTimeout(config.anrMonitoringTimeout);
        }
        if (isFieldSet(config.maxReportsInDatabaseCount)) {
            builder.withMaxReportsInDatabaseCount(config.maxReportsInDatabaseCount);
        }
        if (isFieldSet(config.crashTransformer)) {
            builder.withCrashTransformer(config.crashTransformer);
        }
        if (isFieldSet(config.userProfileID)) {
            builder.withUserProfileID(config.userProfileID);
        }
        if (Utils.isFieldSet(config.revenueAutoTrackingEnabled)) {
            builder.withRevenueAutoTrackingEnabled(config.revenueAutoTrackingEnabled);
        }
        if (Utils.isFieldSet(config.appOpenTrackingEnabled)) {
            builder.withAppOpenTrackingEnabled(config.appOpenTrackingEnabled);
        }
        if (Utils.isFieldSet(config.customHosts)) {
            builder.withCustomHosts(config.customHosts);
        }
    }

    private void putAppEnvironmentToBuilder(@Nullable final Map<String, String> appEnvironment,
                                            @NonNull final AppMetricaConfig.Builder builder) {
        if (Utils.isNullOrEmpty(appEnvironment) == false) {
            for (Map.Entry<String, String> envPair : appEnvironment.entrySet()) {
                builder.withAppEnvironmentValue(envPair.getKey(), envPair.getValue());
            }
        }
    }

    private void putErrorEnvironmentToBuilder(@Nullable final Map<String, String> errorEnvironment,
                                              @NonNull final AppMetricaConfig.Builder builder) {
        if (Utils.isNullOrEmpty(errorEnvironment) == false) {
            for (Map.Entry<String, String> envPair : errorEnvironment.entrySet()) {
                builder.withErrorEnvironmentValue(envPair.getKey(), envPair.getValue());
            }
        }
    }

    private void putAdditionalConfigToBuilder(@Nullable final Map<String, Object> additionalConfig,
                                              @NonNull final AppMetricaConfig.Builder builder) {
        if (Utils.isNullOrEmpty(additionalConfig) == false) {
            for (Map.Entry<String, Object> pair : additionalConfig.entrySet()) {
                builder.withAdditionalConfig(pair.getKey(), pair.getValue());
            }
        }
    }

    private void mergeCommonPart(final AppMetricaConfig useConfig,
                                 final AppMetricaConfig.Builder builder) {
        YLogger.info(TAG, "mergeCommonPart. Config = {locationTracking: %s, location: %s, statisticsSending: %s}, " +
                "defaultConfig = {locationTracking: %s, location: %s, statisticsSending: %s}",
                useConfig.locationTracking, useConfig.location, useConfig.statisticsSending,
                mLocationTracking, mLocation, mStatisticsSending);
        Boolean trackLocationEnabled = isLocationTrackingEnabled();
        if (isNull(useConfig.locationTracking) && isFieldSet(trackLocationEnabled)) {
            builder.withLocationTracking(trackLocationEnabled);
        }
        Location location = getLocation();
        if (isNull(useConfig.location) && isFieldSet(location)) {
            builder.withLocation(location);
        }
        Boolean statisticstSending = isStatisticsSendingEnabled();
        if (isNull(useConfig.statisticsSending) && isFieldSet(statisticstSending)) {
            builder.withStatisticsSending(statisticstSending);
        }
        if (!isFieldSet(useConfig.userProfileID) && isFieldSet(userProfileID)) {
            builder.withUserProfileID(userProfileID);
        }
    }

    @VisibleForTesting
    Map<String, String> getAppEnvironment() {
        return mAppEnvironment;
    }

    @VisibleForTesting
    Map<String, String> getErrorEnvironment() {
        return mErrorEnvironment;
    }

    private static boolean isNull(Object o) {
        return o == null;
    }

    public void setReportsHandler(ReportsHandler reportsHandler) {
        mReportsHandler = reportsHandler;
    }

    private void tryToUpdatePreActivationConfig() {
        if (mReportsHandler != null) {
            mReportsHandler.updatePreActivationConfig(mLocationTracking, mStatisticsSending);
        }
    }
}
