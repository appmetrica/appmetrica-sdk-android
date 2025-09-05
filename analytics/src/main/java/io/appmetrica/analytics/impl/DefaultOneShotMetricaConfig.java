package io.appmetrica.analytics.impl;

import android.location.Location;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.AppMetricaConfig;
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static io.appmetrica.analytics.impl.Utils.isFieldSet;

/**
 * Config with values which will be used for metrica initialization,
 * in case if there are no such values in user config. This object lives according to one-shot
 * strategy. After merging with another config, default configuration will reset it state forever.
 */
public class DefaultOneShotMetricaConfig implements MetricaConfigurator {

    private static final String TAG = "[DefaultOneShotMetricaConfig]";

    private Location location;
    private Boolean locationTrackingEnabled;
    private Boolean advIdentifiersTrackingEnabled;
    private Boolean dataSendingEnabled;
    private final Map<String, String> appEnvironment = new LinkedHashMap<>();
    private final Map<String, String> errorEnvironment = new LinkedHashMap<>();

    private boolean mAppEnvironmentWasCleared;

    @Nullable
    private String userProfileID;
    @NonNull
    public List<String> autoCollectedDataSubscribers = new ArrayList<>();

    private boolean mUsed;
    private ReportsHandler mReportsHandler;

    public Location getLocation() {
        return location;
    }

    public Boolean isLocationTrackingEnabled() {
        return locationTrackingEnabled;
    }

    @Override
    public void setLocationTracking(final boolean enabled) {
        DebugLogger.INSTANCE.info(TAG, "setLocationTracking: %b", enabled);
        locationTrackingEnabled = enabled;
        tryToUpdatePreActivationConfig();
    }

    @Override
    public void setAdvIdentifiersTracking(final boolean enabled, final boolean force) {
        DebugLogger.INSTANCE.info(TAG, "setAdvIdentifiersTracking: %b, force = %b", enabled, force);
        if (force || advIdentifiersTrackingEnabled == null) {
            advIdentifiersTrackingEnabled = enabled;
            tryToUpdatePreActivationConfig();
        }
    }

    public Boolean isAdvIdentifiersTrackingEnabled() {
        return advIdentifiersTrackingEnabled;
    }

    public Boolean isDataSendingEnabled() {
        return dataSendingEnabled;
    }

    @Override
    public void setDataSendingEnabled(boolean value) {
        DebugLogger.INSTANCE.info(TAG, "setDataSendingEnabled: %b", value);
        dataSendingEnabled = value;
        tryToUpdatePreActivationConfig();
    }

    @Override
    public void setLocation(@Nullable final Location location) {
        DebugLogger.INSTANCE.info(TAG, "setLocation: %s", location);
        this.location = location;
    }

    @Override
    public void putAppEnvironmentValue(final String key, final String value) {
        DebugLogger.INSTANCE.info(TAG, "putAppEnvironmentValue: {%s: %s}", key, value);
        appEnvironment.put(key, value);
    }

    @Override
    public void clearAppEnvironment() {
        DebugLogger.INSTANCE.info(TAG, "clearAppEnvironment");
        mAppEnvironmentWasCleared = true;
        appEnvironment.clear();
    }

    public boolean wasAppEnvironmentCleared() {
        return mAppEnvironmentWasCleared;
    }

    public AppMetricaConfigExtension configExtension() {
        return new AppMetricaConfigExtension(autoCollectedDataSubscribers, mAppEnvironmentWasCleared);
    }

    @Override
    public void putErrorEnvironmentValue(final String key, final String value) {
        DebugLogger.INSTANCE.info(TAG, "putErrorEnvironmentValue: {%s: %s}", key, value);
        errorEnvironment.put(key, value);
    }

    @Override
    public void setUserProfileID(@Nullable String userProfileID) {
        DebugLogger.INSTANCE.info(TAG, "setUserProfileID: %s", userProfileID);
        this.userProfileID = userProfileID;
    }

    private void reset() {
        location = null;
        locationTrackingEnabled = null;
        advIdentifiersTrackingEnabled = null;
        dataSendingEnabled = null;
        appEnvironment.clear();
        errorEnvironment.clear();

        mAppEnvironmentWasCleared = false;
        userProfileID = null;
    }

    public AppMetricaConfig mergeWithUserConfig(final AppMetricaConfig config) {
        DebugLogger.INSTANCE.info(TAG, "mergeWithUserConfig. used? %b", mUsed);
        if (mUsed) {
            return config;
        } else {
            AppMetricaConfig.Builder builder = createBuilderFromConfig(config);
            mergeCommonPart(config, builder);
            mUsed = true;
            reset();
            DebugLogger.INSTANCE.info(
                TAG,
                "merge configs - input: %s -> result: %s",
                config.toJson(),
                builder.build().toJson()
            );
            return builder.build();
        }
    }

    private AppMetricaConfig.Builder createBuilderFromConfig(final AppMetricaConfig config) {
        AppMetricaConfig.Builder builder = AppMetricaConfig.newConfigBuilder(config.apiKey);
        builder.withDeviceType(config.deviceType);
        builder.withPreloadInfo(config.preloadInfo);
        builder.withLocation(config.location);
        addOptionalFields(builder, config);
        putAppEnvironmentToBuilder(appEnvironment, builder);
        putAppEnvironmentToBuilder(config.appEnvironment, builder);
        putErrorEnvironmentToBuilder(errorEnvironment, builder);
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
            builder.withMaxReportsCount(config.maxReportsCount);
        }
        if (isFieldSet(config.logs) && config.logs) {
            builder.withLogs();
        }
        if (isFieldSet(config.sessionTimeout)) {
            builder.withSessionTimeout(config.sessionTimeout);
        }
        if (isFieldSet(config.sessionsAutoTrackingEnabled)) {
            builder.withSessionsAutoTrackingEnabled(config.sessionsAutoTrackingEnabled);
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
        if (isFieldSet(config.advIdentifiersTracking)) {
            builder.withAdvIdentifiersTracking(config.advIdentifiersTracking);
        }
        if (isFieldSet(config.firstActivationAsUpdate)) {
            builder.handleFirstActivationAsUpdate(config.firstActivationAsUpdate);
        }
        if (isFieldSet(config.dataSendingEnabled)) {
            builder.withDataSendingEnabled(config.dataSendingEnabled);
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
        if (!Utils.isNullOrEmpty(appEnvironment)) {
            for (Map.Entry<String, String> envPair : appEnvironment.entrySet()) {
                builder.withAppEnvironmentValue(envPair.getKey(), envPair.getValue());
            }
        }
    }

    private void putErrorEnvironmentToBuilder(@Nullable final Map<String, String> errorEnvironment,
                                              @NonNull final AppMetricaConfig.Builder builder) {
        if (!Utils.isNullOrEmpty(errorEnvironment)) {
            for (Map.Entry<String, String> envPair : errorEnvironment.entrySet()) {
                builder.withErrorEnvironmentValue(envPair.getKey(), envPair.getValue());
            }
        }
    }

    private void putAdditionalConfigToBuilder(@Nullable final Map<String, Object> additionalConfig,
                                              @NonNull final AppMetricaConfig.Builder builder) {
        if (!Utils.isNullOrEmpty(additionalConfig)) {
            for (Map.Entry<String, Object> pair : additionalConfig.entrySet()) {
                builder.withAdditionalConfig(pair.getKey(), pair.getValue());
            }
        }
    }

    @Override
    public void addAutoCollectedDataSubscriber(@NonNull String subscriber) {
        DebugLogger.INSTANCE.info(TAG, "addAutoCollectedDataSubscribers: %s", subscriber);
        autoCollectedDataSubscribers.add(subscriber);
    }

    private void mergeCommonPart(final AppMetricaConfig useConfig,
                                 final AppMetricaConfig.Builder builder) {
        DebugLogger.INSTANCE.info(
            TAG,
            "mergeCommonPart. Config = {locationTracking: %s, location: %s, dataSendingEnabled: %s}, " +
                "defaultConfig = {locationTracking: %s, location: %s, dataSendingEnabled: %s, " +
                "advIdentifierTrackingEnabled}: %s",
            useConfig.locationTracking,
            useConfig.location,
            useConfig.dataSendingEnabled,
            locationTrackingEnabled,
            location,
            dataSendingEnabled,
            advIdentifiersTrackingEnabled
        );
        Boolean trackLocationEnabled = isLocationTrackingEnabled();
        if (isNull(useConfig.locationTracking) && isFieldSet(trackLocationEnabled)) {
            builder.withLocationTracking(trackLocationEnabled);
        }
        Location location = getLocation();
        if (isNull(useConfig.location) && isFieldSet(location)) {
            builder.withLocation(location);
        }
        Boolean dataSendingEnabled = isDataSendingEnabled();
        if (isNull(useConfig.dataSendingEnabled) && isFieldSet(dataSendingEnabled)) {
            builder.withDataSendingEnabled(dataSendingEnabled);
        }
        if (!isFieldSet(useConfig.userProfileID) && isFieldSet(userProfileID)) {
            builder.withUserProfileID(userProfileID);
        }
        Boolean advIdentifiersTrackingEnabled = isAdvIdentifiersTrackingEnabled();
        if (isNull(useConfig.advIdentifiersTracking) && isFieldSet(advIdentifiersTrackingEnabled)) {
            builder.withAdvIdentifiersTracking(advIdentifiersTrackingEnabled);
        }
    }

    @VisibleForTesting
    Map<String, String> getAppEnvironment() {
        return appEnvironment;
    }

    @VisibleForTesting
    Map<String, String> getErrorEnvironment() {
        return errorEnvironment;
    }

    private static boolean isNull(Object o) {
        return o == null;
    }

    public void setReportsHandler(ReportsHandler reportsHandler) {
        if (locationTrackingEnabled != null || dataSendingEnabled != null || advIdentifiersTrackingEnabled != null) {
            DebugLogger.INSTANCE.info(
                TAG,
                "Some configuration was defined before activation. Apply pre-activation config: " +
                    "locationTracking: %s, dataSendingEnabled: %s, advIdentifiersTracking: %s",
                locationTrackingEnabled,
                dataSendingEnabled,
                advIdentifiersTrackingEnabled
            );
            reportsHandler.updatePreActivationConfig(
                locationTrackingEnabled,
                dataSendingEnabled,
                advIdentifiersTrackingEnabled);
        }
        mReportsHandler = reportsHandler;
    }

    private void tryToUpdatePreActivationConfig() {
        if (mReportsHandler != null) {
            mReportsHandler.updatePreActivationConfig(
                locationTrackingEnabled,
                dataSendingEnabled,
                advIdentifiersTrackingEnabled
            );
        }
    }
}
