package io.appmetrica.analytics;

import android.location.Location;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.coreutils.internal.collection.CollectionUtils;
import io.appmetrica.analytics.impl.ClientConfigSerializer;
import io.appmetrica.analytics.impl.VerificationConstants;
import io.appmetrica.analytics.impl.proxy.validation.ConfigChecker;
import io.appmetrica.analytics.impl.utils.validation.ThrowIfFailedValidator;
import io.appmetrica.analytics.impl.utils.validation.Validator;
import io.appmetrica.analytics.impl.utils.validation.api.ApiKeyValidator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Contains configuration of analytic processing.
 * Configuration created by {@link Builder}.
 */
public class AppMetricaConfig {

    /**
     * Unique identifier of app in AppMetrica
     *
     * @see AppMetricaConfig#newConfigBuilder(String)
     */
    @NonNull
    public final String apiKey;

    /**
     * Application version
     *
     * @see Builder#withAppVersion(String)
     */
    @Nullable
    public final String appVersion;

    /**
     * Duration of AppMetrica session
     *
     * @see Builder#withSessionTimeout(int)
     */
    @Nullable
    public final Integer sessionTimeout;

    /**
     * <p>Indicates whether to capture and send reports about crashes automatically</p>
     *
     * <p>{@code true} if we need to send reports about crashes, otherwise {@code false}</p>
     *
     * @see Builder#withCrashReporting(boolean)
     */
    @Nullable
    public final Boolean crashReporting;

    /**
     * <p>Indicates whether to capture and send reports about native crashes automatically</p>
     *
     * <p>{@code true} if we need to send reports about native crashes,
     * otherwise {@code false}</p>
     *
     * @see Builder#withNativeCrashReporting(boolean)
     */
    @Nullable
    public final Boolean nativeCrashReporting;

    /**
     * Location to be used as location for reports of AppMetrica
     *
     * @see Builder#withLocation(Location)
     */
    @Nullable
    public final Location location;

    /**
     * <p>Indicates whether AppMetrica should include location information within its reports</p>
     *
     * <p>{@code true} if allow AppMetrica to record location information in reports,
     * otherwise {@code false}</p>
     *
     * @see Builder#withLocationTracking(boolean)
     */
    @Nullable
    public final Boolean locationTracking;

    /**
     * <p>Indicates whether AppMetrica logging enabled</p>
     *
     * <p>{@code true} if enabled, {@code false} if not</p>
     *
     * @see Builder#withLogs()
     */
    @Nullable
    public final Boolean logs;

    /**
     * Preload info for tracking preloaded apps
     *
     * @see Builder#withPreloadInfo(PreloadInfo)
     */
    @Nullable
    public final PreloadInfo preloadInfo;

    /**
     * <p>Indicates, whether first activation of AppMetrica should be considered as app update or new app install.</p>
     * <p>{@code true} if first call of {@link AppMetrica#activate} should be considered as app update,
     * {@code false} otherwise.</p>
     */
    @Nullable
    public final Boolean firstActivationAsUpdate;

    /**
     * <p>Indicates whether data should be sent to the AppMetrica server.</p>
     */
    @Nullable
    public final Boolean dataSendingEnabled;

    /**
     * Maximum number of reports to store in database.
     * Default value is {@value AppMetricaDefaultValues#DEFAULT_MAX_REPORTS_IN_DATABASE_COUNT}.
     * Must be in range
     * [{@value AppMetricaDefaultValues#DEFAULT_MAX_REPORTS_COUNT_LOWER_BOUND};
     * {@value AppMetricaDefaultValues#DEFAULT_MAX_REPORTS_COUNT_UPPER_BOUND}].
     * If not, closest possible value will be used.
     *
     * @see Builder#withMaxReportsInDatabaseCount(int)
     */
    @Nullable
    public final Integer maxReportsInDatabaseCount;

    /**
     * Error environment to be set after initialization
     *
     * @see AppMetricaConfig.Builder#withErrorEnvironmentValue(String, String)
     */
    @Nullable
    public final Map<String, String> errorEnvironment;

    /**
     * The ID of the user profile.
     *
     * <b>NOTE:</b> The string value can contain up to 200 characters.
     *
     * @see AppMetricaConfig.Builder#withUserProfileID(String)
     */
    @Nullable
    public final String userProfileID;

    /**
     * Whether revenue auto tracking is enabled
     * Default value is {@value AppMetricaDefaultValues#DEFAULT_REVENUE_AUTO_TRACKING_ENABLED}.
     *
     * @see AppMetricaConfig.Builder#withRevenueAutoTrackingEnabled(boolean)
     */
    @Nullable
    public final Boolean revenueAutoTrackingEnabled;

    /**
     * <p>Indicates whether sessions auto tracking is enabled.</p>
     * Setting this flag to true is equivalent to invoking {@link AppMetrica#enableActivityAutoTracking(android.app.Application)}
     * Default value is {@value AppMetricaDefaultValues#DEFAULT_SESSIONS_AUTO_TRACKING_ENABLED}.
     */
    @Nullable
    public final Boolean sessionsAutoTrackingEnabled;

    /**
     * Whether app open auto tracking is enabled
     * Default value is {@value AppMetricaDefaultValues#DEFAULT_APP_OPEN_TRACKING_ENABLED}.
     *
     * Set this flag to true instead of reporting deeplinks manually from
     * {@link android.app.Activity#onCreate(android.os.Bundle)} via
     * {@link AppMetrica#reportAppOpen(android.app.Activity)}
     *
     * <b>NOTE: </b> Auto tracking will only capture links that open activity. Those that are clicked on while
     * activity is opened will be ignored. To track them call {@link AppMetrica#reportAppOpen(android.content.Intent)}
     * from {@link android.app.Activity#onNewIntent(android.content.Intent)}
     *
     * @see AppMetricaConfig.Builder#withAppOpenTrackingEnabled(boolean)
     */
    @Nullable
    public final Boolean appOpenTrackingEnabled;

    /**
     * Device type based on screen size: phone, tablet, TV
     */
    @Nullable
    public final String deviceType;

    /**
     * Build number of application
     *
     * @see AppMetricaConfig.Builder#withAppBuildNumber(int)
     */
    @Nullable
    public final Integer appBuildNumber;

    /**
     * Timeout for sending reports
     *
     * @see AppMetricaConfig.Builder#withDispatchPeriodSeconds(int)
     */
    @Nullable
    public final Integer dispatchPeriodSeconds;

    /**
     * Maximum buffer size for reports
     *
     * @see AppMetricaConfig.Builder#withMaxReportsCount(int)
     */
    @Nullable
    public final Integer maxReportsCount;

    /**
     * Application environment to be set after initialization
     *
     * @see AppMetricaConfig.Builder#withAppEnvironmentValue(String, String)
     * @see AppMetrica#putAppEnvironmentValue(String, String)
     */
    @Nullable
    public final Map<String, String> appEnvironment;

    /**
     * <p>Custom crash transformer. Applied to crashes before reporting. If null, crashes are not transformed.</p>
     */
    @Nullable
    public final ICrashTransformer crashTransformer;

    /**
     * <p>Indicates whether ANR monitoring is enabled.</p>
     * <p>{@code true} if enabled, otherwise, {@code false}</p>
     */
    @Nullable
    public final Boolean anrMonitoring;

    /**
     * <p>The timeout in seconds at which the fact of ANR is recorded.</p>
     * <p>Minimal value is {@value AppMetricaDefaultValues#DEFAULT_ANR_TICKS_COUNT}.
     * Default value is {@value AppMetricaDefaultValues#DEFAULT_ANR_TICKS_COUNT}.</p>
     */
    @Nullable
    public final Integer anrMonitoringTimeout;

    /**
     * Custom hosts for startup config
     *
     * @see AppMetricaConfig.Builder#withCustomHosts(List)
     */
    @Nullable
    public final List<String> customHosts;

    /**
     * Additional configs
     *
     * @see AppMetricaConfig.Builder#withAdditionalConfig(String, Object)
     */
    @NonNull
    public final Map<String, Object> additionalConfig;

    /**
     * Creates the new instance of {@link Builder}
     *
     * @param apiKey API_KEY - unique identifier of app in AppMetrica.
     *
     * @see <a href="https://appmetrica.io/docs/mobile-sdk-dg/android/about/android-initialize.html">
     * AppMetrica SDK documentation </a>
     *
     * @return builder of AppMetricaConfig
     *
     * @throws IllegalArgumentException if {@code apiKey} is null, empty or has invalid format.
     */
    @NonNull
    public static Builder newConfigBuilder(@NonNull String apiKey) {
        return new Builder(apiKey);
    }

    /**
     *
     * @param json {@link String} representing AppMetricaConfig
     * @return {@link AppMetricaConfig} parsed from giver json
     */
    @Nullable
    public static AppMetricaConfig fromJson(String json) {
        return new ClientConfigSerializer().fromJson(json);
    }

    /**
     * Serializes {@link AppMetricaConfig} to json
     *
     * @return {@link String} representing this {@link AppMetricaConfig}
     */
    public String toJson() {
        return new ClientConfigSerializer().toJson(this);
    }

    /**
     * Builds a new {@link AppMetricaConfig} object.
     */
    public static class Builder {

        private static final Validator<String> sApiKeyValidator =
                new ThrowIfFailedValidator<String>(new ApiKeyValidator());
        @NonNull
        private final ConfigChecker configChecker;

        @NonNull
        private final String mApiKey;

        @Nullable
        private String mAppVersion;

        @Nullable
        private Integer mSessionTimeout;

        @Nullable
        private Boolean mCrashReporting;

        @Nullable
        private Boolean mNativeCrashReporting;

        @Nullable
        private Location mLocation;

        @Nullable
        private Boolean mLocationTracking;

        @Nullable
        private Boolean mLogs;

        @Nullable
        private PreloadInfo mPreloadInfo;

        @Nullable
        private Boolean mFirstActivationAsUpdate;

        @Nullable
        private Boolean dataSendingEnabled;

        @Nullable
        private Integer mMaxReportsInDatabaseCount;

        @NonNull
        private LinkedHashMap<String, String> mErrorEnvironment = new LinkedHashMap<String, String>();

        @Nullable
        private String userProfileID;

        @Nullable
        private Boolean revenueAutoTrackingEnabled;

        @Nullable
        private Boolean sessionsAutoTrackingEnabled;

        @Nullable
        private Boolean appOpenTrackingEnabled;

        @Nullable
        private String deviceType;

        @Nullable
        private Integer appBuildNumber;

        @Nullable
        private Integer dispatchPeriodSeconds;

        @Nullable
        private Integer maxReportsCount;

        @NonNull
        private Map<String, String> appEnvironment = new LinkedHashMap<>();

        @Nullable
        private ICrashTransformer crashTransformer;

        @Nullable
        private Boolean anrMonitoring;

        @Nullable
        private Integer anrMonitoringTimeout;

        @Nullable
        private List<String> customHosts;

        @NonNull
        private final HashMap<String, Object> additionalConfig = new HashMap<>();

        private Builder(@NonNull String apiKey) {
            sApiKeyValidator.validate(apiKey);
            this.configChecker = new ConfigChecker(apiKey);
            mApiKey = apiKey;
        }

        /**
         * Sets the application version. It is optional value.
         * By default, the app version is set in field {@code android:versionName} of
         * the <a href="http://developer.android.com/guide/topics/manifest/manifest-intro.html">
         * AndroidManifest.xml file.</a>
         * <p> <b>EXAMPLE:</b> 1.0</p>
         *
         * @param appVersion Application version
         *
         * @see android.content.pm.PackageInfo#versionName
         *
         * @throws IllegalArgumentException If {@code appVersion} is null/empty.
         *
         * @return the same {@link Builder} object.
         */
        @NonNull
        public Builder withAppVersion(@Nullable String appVersion) {
            mAppVersion = appVersion;
            return this;
        }

        /**
         * By default, the session times out if the app is inactive for
         * {@value AppMetricaDefaultValues#DEFAULT_SESSION_TIMEOUT_SECONDS} seconds.
         * To change this time limit, pass the builder.withSessionTimeout(int sessionTimeoutSeconds)
         * method your time limit in seconds. The minimum acceptable value for the sessionTimeoutSeconds
         * parameter is 10 seconds. If a value less than 10 is set, the value will automatically
         * be 10 seconds.
         * <p>
         * Under the duration of sessions, in the concept of <b>AppMetrica</b>,
         * means the following (see example):
         * <p>
         * <b>EXAMPLE:</b>
         * Let the duration of sessions is 2 minutes.
         * Then, if interaction with your application started
         * after 2 minutes of inactivity with the application,
         * then a new session will be created,
         * otherwise the session will continue.
         *
         * @param sessionTimeout Timeout in seconds.
         *
         * @return the same {@link Builder} object.
         */
        @NonNull
        public Builder withSessionTimeout(int sessionTimeout) {
            mSessionTimeout = sessionTimeout;
            return this;
        }

        /**
         * Whether to capture and send reports about crashes automatically.
         * <p> <b>NOTE:</b> Default value is
         * {@value AppMetricaDefaultValues#DEFAULT_REPORTS_CRASHES_ENABLED}
         *
         * @param enabled {@code true} if we need to send reports about crashes,
         * otherwise {@code false}. In the case of {@code false} you can still send
         * information about crashes via {@link AppMetrica#reportUnhandledException(Throwable)} method.
         *
         * @see Builder#withNativeCrashReporting(boolean) (boolean)
         *
         * @return the same {@link Builder} object.
         */
        @NonNull
        public Builder withCrashReporting(boolean enabled) {
            mCrashReporting = enabled;
            return this;
        }

        /**
         * Whether to capture and send reports about native crashes automatically.
         * <p> <b>NOTE:</b> Default value is
         * {@value AppMetricaDefaultValues#DEFAULT_REPORTS_NATIVE_CRASHES_ENABLED}
         *
         * @param enabled {@code true} if we need to send reports about native crashes,
         * otherwise {@code false}.
         *
         * @see Builder#withCrashReporting(boolean)
         *
         * @return the same {@link Builder} object.
         */
        @NonNull
        public Builder withNativeCrashReporting(boolean enabled) {
            mNativeCrashReporting = enabled;
            return this;
        }

        /**
         * Enable AppMetrica logging. Should be called before activation.
         *
         * @return the same {@link Builder} object.
         */
        @NonNull
        public Builder withLogs() {
            mLogs = true;
            return this;
        }

        /**
         * Sets {@link Location} to be used as location for reports of AppMetrica.<p>
         * If location is set using this method, it will be used instead of auto collected location.
         * <p>
         *
         * <b>NOTE:</b> Permissions:
         * {@link android.Manifest.permission#ACCESS_COARSE_LOCATION},
         * {@link android.Manifest.permission#ACCESS_FINE_LOCATION}
         * improve the quality of auto collected location.
         *
         * @see Builder#withLocationTracking(boolean)
         * @see AppMetrica#setLocation(Location)
         * @see AppMetrica#setLocationTracking(boolean)
         *
         * @param location location to be used for reports
         *
         * @return the same {@link Builder} object.
         */
        @NonNull
        public Builder withLocation(@Nullable Location location) {
            mLocation = location;
            return this;
        }

        /**
         * Sets whether AppMetrica should include location information within its reports.<p>
         * <b>NOTE:</b> Default value is
         * {@value AppMetricaDefaultValues#DEFAULT_REPORT_LOCATION_ENABLED}
         *
         * @param enabled {@code true} to allow AppMetrica to record location information in reports,
         * otherwise {@code false}.
         *
         * @see Builder#withLocation(Location)
         * @see AppMetrica#setLocationTracking(boolean)
         * @see AppMetrica#setLocation(Location)
         *
         * @return the same {@link Builder} object.
         */
        @NonNull
        public Builder withLocationTracking(boolean enabled) {
            mLocationTracking = enabled;
            return this;
        }

        /**
         * Sets {@code preload info} for tracking preloaded apps
         * @param preloadInfo instance of {@link PreloadInfo}
         * @return the same {@link Builder} object
         */
        @NonNull
        public Builder withPreloadInfo(@Nullable PreloadInfo preloadInfo) {
            mPreloadInfo = preloadInfo;
            return this;
        }

        /**
         * Whether first activation of AppMetrica should be considered as app update or new app install.
         * @param value {@code true} if first call of {@link AppMetrica#activate} should be considered as app update,
         * {@code false} otherwise.
         * @return the same {@link Builder} object
         */
        @NonNull
        public Builder handleFirstActivationAsUpdate(boolean value) {
            mFirstActivationAsUpdate = value;
            return this;
        }

        /**
         * Enables/disables data sending to the AppMetrica server. By default, the sending is enabled.
         *
         * <p><b>NOTE:</b> Disabling this option also turns off data sending from the reporters
         * that initialized for different apiKey.
         *
         * @param value {@code true} to allow AppMetrica sending data,
         *              otherwise {@code false}.
         * @return the same {@link Builder} object
         */
        @NonNull
        public Builder withDataSendingEnabled(boolean value) {
            dataSendingEnabled = value;
            return this;
        }

        /**
         *
         * Sets maximum number of reports to store in database.
         * If this number is exceeded, some reports will be removed.
         * <p> <b>NOTE:</b>
         * Default value is {@value AppMetricaDefaultValues#DEFAULT_MAX_REPORTS_IN_DATABASE_COUNT}
         *
         * @param value Max number of reports to store in database.
         *              Must be in range [{@value AppMetricaDefaultValues#DEFAULT_MAX_REPORTS_COUNT_LOWER_BOUND};
         *              {@value AppMetricaDefaultValues#DEFAULT_MAX_REPORTS_COUNT_UPPER_BOUND}].
         *              If not, closest possible value will be used.
         *
         * @return the same {@link Builder} object.
         */
        @NonNull
        public Builder withMaxReportsInDatabaseCount(int value) {
            mMaxReportsInDatabaseCount = configChecker.getCheckedMaxReportsInDatabaseCount(value);
            return this;
        }

        /**
         * Sets key - value data to be used as additional information, associated
         * with your unhandled exception and error reports.
         * @param key the environment key.
         * @param value the environment value. To remove pair from environment pass {@code null} value.
         * @return the same {@link AppMetricaConfig.Builder} object
         */
        @NonNull
        public Builder withErrorEnvironmentValue(@NonNull String key, @Nullable String value) {
            mErrorEnvironment.put(key, value);
            return this;
        }

        /**
         * Sets the ID of the user profile.
         *
         * <b>NOTE:</b> The string value can contain up to 200 characters.
         *
         * @param userProfileID The custom user profile ID.
         * @return the same {@link AppMetricaConfig.Builder} object
         * @see AppMetrica#setUserProfileID(String)
         */
        @NonNull
        public Builder withUserProfileID(@Nullable String userProfileID) {
            this.userProfileID = userProfileID;
            return this;
        }

        /**
         * Enables/disables auto tracking of inapp purchases.
         * Default value is {@value AppMetricaDefaultValues#DEFAULT_REVENUE_AUTO_TRACKING_ENABLED}.
         *
         * @param enabled {@code true} to allow inapp purchases auto tracking,
         *                otherwise {@code false}.
         *
         * @return the same {@link Builder} object
         */
        @NonNull
        public Builder withRevenueAutoTrackingEnabled(boolean enabled) {
            this.revenueAutoTrackingEnabled = enabled;
            return this;
        }

        /**
         * Sets whether sessions auto tracking is enabled.
         * @see AppMetricaConfig#sessionsAutoTrackingEnabled
         *
         * Default value is {@value AppMetricaDefaultValues#DEFAULT_SESSIONS_AUTO_TRACKING_ENABLED}.
         *
         * @param enabled true if auto tracking should be enabled, false otherwise
         * @return the same {@link Builder} object.
         */
        @NonNull
        public Builder withSessionsAutoTrackingEnabled(boolean enabled) {
            this.sessionsAutoTrackingEnabled = enabled;
            return this;
        }

        /**
         * Sets whether app open auto tracking is enabled.
         * Default value is {@value AppMetricaDefaultValues#DEFAULT_APP_OPEN_TRACKING_ENABLED}.
         * @see AppMetricaConfig#appOpenTrackingEnabled
         *
         * Set this flag to true instead of reporting deeplinks manually from
         * {@link android.app.Activity#onCreate(android.os.Bundle)} via
         * {@link AppMetrica#reportAppOpen(android.app.Activity)}
         *
         * <b>NOTE: </b> Auto tracking will only capture links that open activity. Those that are clicked on while
         * activity is opened will be ignored. To track them call {@link AppMetrica#reportAppOpen(android.content.Intent)}
         * from {@link android.app.Activity#onNewIntent(android.content.Intent)}
         *
         * @param enabled true if auto tracking should be enabled, false otherwise
         * @return the same {@link Builder} object.
         */
        @NonNull
        public Builder withAppOpenTrackingEnabled(boolean enabled) {
            this.appOpenTrackingEnabled = enabled;
            return this;
        }

        /**
         * Sets key - config data
         * @param key the config key.
         * @param config the config value.
         * @return the same {@link AppMetricaConfig.Builder} object.
         */
        @NonNull
        public Builder withAdditionalConfig(
                @NonNull final String key,
                @Nullable final Object config
        ) {
            this.additionalConfig.put(key, config);
            return this;
        }

        /**
         * Sets size type of device based on screen size: phone, tablet, phablet, TV.
         * @param deviceType type of device.
         *                   Some common-used types are listed in {@link PredefinedDeviceTypes} class.
         *                   One can find complete list in the official documentation.
         * @return the same {@link AppMetricaConfig.Builder} object.
         */
        @NonNull
        public Builder withDeviceType(@Nullable String deviceType) {
            this.deviceType = deviceType;
            return this;
        }

        /**
         * Sets the application build number, if missing,
         * the application version is read from {@code android:versionCode} field
         * {@code AndroidManifest.xml} file.
         * <p> <b>EXAMPLE:</b> 1
         *
         * @param appBuildNumber Application version code.
         *
         * @see android.content.pm.PackageInfo#versionCode
         *
         * @throws IllegalArgumentException if {@code appBuildNumber} &lt; 0
         *
         * @return the same {@link AppMetricaConfig.Builder} object.
         */
        @NonNull
        public Builder withAppBuildNumber(int appBuildNumber) {
            if (appBuildNumber < 0) {
                throw new IllegalArgumentException(
                        String.format(Locale.US, "Invalid %1$s. %1$s should be positive.",
                                VerificationConstants.APP_BUILD_NUMBER)
                );
            }
            this.appBuildNumber = appBuildNumber;
            return this;
        }

        /**
         * Sets timeout for sending reports.
         * <p> <b>NOTE:</b> Default value is
         * {@value AppMetricaDefaultValues#DEFAULT_DISPATCH_PERIOD_SECONDS}.
         * If you set a non-positive value,
         * then automatic sending by timer will be disabled.
         *
         * @param dispatchPeriodSeconds Timeout in seconds to automatically send reports.
         *
         * @see AppMetricaConfig.Builder#withMaxReportsCount(int)
         *
         * @return the same {@link AppMetricaConfig.Builder} object.
         */
        @NonNull
        public Builder withDispatchPeriodSeconds(int dispatchPeriodSeconds) {
            this.dispatchPeriodSeconds = dispatchPeriodSeconds;
            return this;
        }

        /**
         *
         * Sets maximum buffer size for reports.
         * <p> <b>NOTE:</b> Default value is
         * {@value AppMetricaDefaultValues#DEFAULT_MAX_REPORTS_COUNT}.
         * If you set a non-positive value,
         * then automatic sending will be disabled
         * for the situation when the events buffer is full.
         *
         * @param maxReportsCount Max number of items/reports to automatically send reports.
         *
         * @see AppMetricaConfig.Builder#withDispatchPeriodSeconds(int)
         *
         * @return the same {@link AppMetricaConfig.Builder} object.
         */
        @NonNull
        public Builder withMaxReportsCount(int maxReportsCount) {
            this.maxReportsCount = maxReportsCount;
            return this;
        }

        /**
         * Sets key - value pair to be used as additional information, associated
         * with your application runtime's environment. This environment is unique for every unique
         * APIKey and shared between processes. Application's environment persists to storage and
         * retained between application launches. To reset environment use
         * {@link AppMetrica#clearAppEnvironment()}
         * Pairs added to config builder, will be set right after metrica initialization.
         * <p> <b>WARNING:</b> Application's environment is a global permanent state and
         * can't be changed too often. For frequently changed parameters use extended reportMessage methods.
         * @param key the environment key.
         * @param value the environment value. To remove pair from environment pass {@code null} value.
         * @see AppMetrica#putAppEnvironmentValue(String, String)
         * @return the same {@link AppMetricaConfig.Builder} object
         */
        @NonNull
        public Builder withAppEnvironmentValue(@NonNull String key, @Nullable String value) {
            appEnvironment.put(key, value);
            return this;
        }

        /**
         * Custom crash transformer which is applied to crashes.
         *
         * @param crashTransformer {@link ICrashTransformer} implementation to be applied.
         * @return the same {@link AppMetricaConfig.Builder} object.
         */
        @NonNull
        public Builder withCrashTransformer(@Nullable ICrashTransformer crashTransformer) {
            this.crashTransformer = crashTransformer;
            return this;
        }

        /**
         * Whether to capture and send reports about ANRs automatically.
         * <p> <b>NOTE:</b> Default value is {@value AppMetricaDefaultValues#DEFAULT_ANR_COLLECTING_ENABLED}.
         *
         * @param enabled {@code true} if we need to send reports about ANRs,
         * otherwise {@code false}.
         *
         * @return the same {@link AppMetricaConfig.Builder} object.
         */
        @NonNull
        public Builder withAnrMonitoring(boolean enabled) {
            this.anrMonitoring = enabled;
            return this;
        }

        /**
         * The timeout in seconds at which the fact of ANR is recorded.
         * <p>Minimal value is {@value AppMetricaDefaultValues#DEFAULT_ANR_TICKS_COUNT}.
         * Default value is {@value AppMetricaDefaultValues#DEFAULT_ANR_TICKS_COUNT}.</p>
         *
         * @param timeout the timeout in seconds at which the fact of ANR is recorded
         *
         * @return the same {@link AppMetricaConfig.Builder} object.
         */
        @NonNull
        public Builder withAnrMonitoringTimeout(int timeout) {
            this.anrMonitoringTimeout = timeout;
            return this;
        }

        /**
         * Sets the list of hosts. This is optional value.
         *
         * @param hosts non-empty host list.
         *
         * @return the same {@link Builder} object.
         */
        @NonNull
        public Builder withCustomHosts(@NonNull List<String> hosts) {
            this.customHosts = CollectionUtils.unmodifiableListCopy(hosts);
            return this;
        }

        /**
         * Creates instance of {@link AppMetricaConfig}
         *
         * @return {@link AppMetricaConfig} object
         */
        @NonNull
        public AppMetricaConfig build() {
            return new AppMetricaConfig(this);
        }
    }

    private AppMetricaConfig(@NonNull Builder builder) {
        apiKey = builder.mApiKey;
        appVersion = builder.mAppVersion;
        sessionTimeout = builder.mSessionTimeout;
        crashReporting = builder.mCrashReporting;
        nativeCrashReporting = builder.mNativeCrashReporting;
        location = builder.mLocation;
        locationTracking = builder.mLocationTracking;
        logs = builder.mLogs;
        preloadInfo = builder.mPreloadInfo;
        firstActivationAsUpdate = builder.mFirstActivationAsUpdate;
        dataSendingEnabled = builder.dataSendingEnabled;
        maxReportsInDatabaseCount = builder.mMaxReportsInDatabaseCount;
        errorEnvironment = CollectionUtils.unmodifiableSameOrderMapCopy(builder.mErrorEnvironment);
        userProfileID = builder.userProfileID;
        revenueAutoTrackingEnabled = builder.revenueAutoTrackingEnabled;
        sessionsAutoTrackingEnabled = builder.sessionsAutoTrackingEnabled;
        appOpenTrackingEnabled = builder.appOpenTrackingEnabled;
        deviceType = builder.deviceType;
        appBuildNumber = builder.appBuildNumber;
        dispatchPeriodSeconds = builder.dispatchPeriodSeconds;
        maxReportsCount = builder.maxReportsCount;
        appEnvironment = CollectionUtils.unmodifiableSameOrderMapCopy(builder.appEnvironment);
        crashTransformer = builder.crashTransformer;
        anrMonitoring = builder.anrMonitoring;
        anrMonitoringTimeout = builder.anrMonitoringTimeout;
        customHosts = builder.customHosts;
        additionalConfig = CollectionUtils.unmodifiableSameOrderMapCopy(builder.additionalConfig);
    }

    /**
     * Clone constructor
     *
     * @param source {@link AppMetricaConfig} to clone
     */
    protected AppMetricaConfig(@NonNull AppMetricaConfig source) {
        apiKey = source.apiKey;
        appVersion = source.appVersion;
        sessionTimeout = source.sessionTimeout;
        crashReporting = source.crashReporting;
        nativeCrashReporting = source.nativeCrashReporting;
        location = source.location;
        locationTracking = source.locationTracking;
        logs = source.logs;
        preloadInfo = source.preloadInfo;
        firstActivationAsUpdate = source.firstActivationAsUpdate;
        dataSendingEnabled = source.dataSendingEnabled;
        maxReportsInDatabaseCount = source.maxReportsInDatabaseCount;
        errorEnvironment = source.errorEnvironment;
        userProfileID = source.userProfileID;
        revenueAutoTrackingEnabled = source.revenueAutoTrackingEnabled;
        sessionsAutoTrackingEnabled = source.sessionsAutoTrackingEnabled;
        appOpenTrackingEnabled = source.appOpenTrackingEnabled;
        deviceType = source.deviceType;
        appBuildNumber = source.appBuildNumber;
        dispatchPeriodSeconds = source.dispatchPeriodSeconds;
        maxReportsCount = source.maxReportsCount;
        appEnvironment = source.appEnvironment;
        crashTransformer = source.crashTransformer;
        anrMonitoring = source.anrMonitoring;
        anrMonitoringTimeout = source.anrMonitoringTimeout;
        customHosts = source.customHosts;
        additionalConfig = source.additionalConfig;
    }
}
