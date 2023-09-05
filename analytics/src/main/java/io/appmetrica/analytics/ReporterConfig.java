package io.appmetrica.analytics;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.coreutils.internal.collection.CollectionUtils;
import io.appmetrica.analytics.impl.proxy.validation.ConfigChecker;
import io.appmetrica.analytics.impl.utils.validation.ThrowIfFailedValidator;
import io.appmetrica.analytics.impl.utils.validation.Validator;
import io.appmetrica.analytics.impl.utils.validation.api.ApiKeyValidator;
import java.util.HashMap;
import java.util.Map;

/**
 * Contains configuration of analytic processing in {@link IReporter}.
 * Configuration created by {@link ReporterConfig.Builder}.
 */
public class ReporterConfig {

    /**
     * Unique identifier of app in AppMetrica.
     *
     * @see ReporterConfig#newConfigBuilder(String)
     */
    @NonNull
    public final String apiKey;

    /**
     * Duration of AppMetrica foreground session timeout.
     *
     * @see Builder#withSessionTimeout(int)
     */
    @Nullable
    public final Integer sessionTimeout;

    /**
     * Indicates whether statistics should be sent to the AppMetrica server.
     *
     * @see Builder#withStatisticsSending(boolean)
     */
    @Nullable
    public final Boolean statisticsSending;

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
     * The ID of the user profile.
     *
     * <b>NOTE:</b> The string value can contain up to 200 characters.
     *
     * @see Builder#withUserProfileID(String)
     */
    @Nullable
    public final String userProfileID;

    /**
     * <p>Indicates whether logging for appropriate {@link IReporter} enabled</p>
     *
     * <p>{@code true} if enabled, {@code false} if not</p>
     *
     * @see Builder#withLogs()
     */
    @Nullable
    public final Boolean logs;

    /**
     * Timeout for sending reports
     *
     * @see Builder#withDispatchPeriodSeconds(int)
     */
    @Nullable
    public final Integer dispatchPeriodSeconds;

    /**
     * Maximum buffer size for reports
     *
     * @see Builder#withMaxReportsCount(int)
     */
    @Nullable
    public final Integer maxReportsCount;

    /**
     * Application environment to be set after initialization
     *
     * @see Builder#withAppEnvironmentValue(String, String)
     */
    public final Map<String, String> appEnvironment;

    /**
     * Additional configs
     *
     * @see Builder#withAdditionalConfig(String, Object)
     */
    @NonNull
    public final Map<String, Object> additionalConfig;

    private ReporterConfig(@NonNull Builder builder) {
        apiKey = builder.mApiKey;
        sessionTimeout = builder.mSessionTimeout;
        logs = builder.mLogs;
        statisticsSending = builder.mStatisticsSending;
        maxReportsInDatabaseCount = builder.mMaxReportsInDatabaseCount;
        userProfileID = builder.userProfileID;
        dispatchPeriodSeconds = builder.dispatchPeriodSeconds;
        maxReportsCount = builder.maxReportsCount;
        appEnvironment = CollectionUtils.unmodifiableSameOrderMapCopy(builder.appEnvironment);
        additionalConfig = CollectionUtils.unmodifiableSameOrderMapCopy(builder.additionalConfig);
    }

    ReporterConfig(@NonNull ReporterConfig config) {
        apiKey = config.apiKey;
        sessionTimeout = config.sessionTimeout;
        logs = config.logs;
        statisticsSending = config.statisticsSending;
        maxReportsInDatabaseCount = config.maxReportsInDatabaseCount;
        userProfileID = config.userProfileID;
        dispatchPeriodSeconds = config.dispatchPeriodSeconds;
        maxReportsCount = config.maxReportsCount;
        appEnvironment = config.appEnvironment;
        additionalConfig = config.additionalConfig;
    }

    /**
     * Creates the new instance of {@link Builder}
     *
     * @param apiKey API_KEY - unique identifier of app in AppMetrica.
     *
     * @see <a href="https://yandex.com/dev/appmetrica/doc/mobile-sdk-dg/concepts/android-initialize.html">
     * AppMetrica SDK documentation </a>
     *
     * @return builder of {@link ReporterConfig}
     *
     * @throws IllegalArgumentException if {@code apiKey} is null, empty or has invalid format.
     */
    @NonNull
    public static Builder newConfigBuilder(@NonNull String apiKey) {
        return new Builder(apiKey);
    }

    /**
     * Builds a new {@link ReporterConfig} object.
     */
    public static class Builder {

        private static final Validator<String> sApiKeyValidator =
                new ThrowIfFailedValidator<String>(new ApiKeyValidator());

        @NonNull
        private final ConfigChecker configChecker;

        private final String mApiKey;
        @Nullable
        private Integer mSessionTimeout;
        @Nullable
        private Boolean mLogs;
        @Nullable
        private Boolean mStatisticsSending;
        @Nullable
        private Integer mMaxReportsInDatabaseCount;
        @Nullable
        private String userProfileID;
        @Nullable
        private Integer dispatchPeriodSeconds;
        @Nullable
        private Integer maxReportsCount;
        @NonNull
        private final HashMap<String, String> appEnvironment = new HashMap<>();
        @NonNull
        private final HashMap<String, Object> additionalConfig = new HashMap<>();

        private Builder(@NonNull String apiKey) {
            sApiKeyValidator.validate(apiKey);
            this.configChecker = new ConfigChecker(apiKey);
            mApiKey = apiKey;
        }

        /**
         * Set the timeout for expiring session. <p>
         * By default, the session times out if the app is inactive for
         * {@value AppMetricaDefaultValues#DEFAULT_SESSION_TIMEOUT_SECONDS} seconds.
         * To change this time limit, pass to the IReporter.setSessionTimeout(int sessionTimeoutSeconds)
         * method your time limit in seconds. The minimum acceptable value for the sessionTimeoutSeconds
         * parameter is 10 seconds. If a value less than 10 is set, the value will automatically
         * be 10 seconds.
         * <p>
         * Under the duration of sessions, in the concept of <b>Metrica</b>,
         * means the following (see example):
         * <p>
         * <b>EXAMPLE:</b>
         * Let the duration of session timeout is 2 minutes.
         * Then, if interaction with your application started
         * after 2 minutes of inactivity with the application,
         * then a new session will be created,
         * otherwise the session will continue.
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
         * Enable logging for appropriate {@link IReporter}.
         * Should be called before {@link AppMetrica#getReporter(Context, String)}.
         *
         * @return the same {@link Builder} object.
         */
        @NonNull
        public Builder withLogs() {
            mLogs = true;
            return this;
        }

        /**
         * Enables/disables statistics sending to the AppMetrica server. By default, the sending is enabled.
         *
         * <p><b>NOTE:</b> Disabling this option doesn't affect data sending from the main apiKey and other
         * reporters.
         *
         * @param enabled {@code true} to allow AppMetrica sending statistics, otherwise {@code false}.
         * @return the same {@link Builder} object.
         */
        @NonNull
        public Builder withStatisticsSending(boolean enabled) {
            mStatisticsSending = enabled;
            return this;
        }

        /**
         *
         * Sets maximum number of reports to store in database.
         * If this number is exceeded, some reports will be removed.
         * <p> <b>NOTE:</b>
         * Default value is {@value AppMetricaDefaultValues#DEFAULT_MAX_REPORTS_IN_DATABASE_COUNT}
         *
         * @param maxReportsInDatabaseCount Max number of reports to store in database.
         *                                  Must be in range
         *                                  [{@value AppMetricaDefaultValues#DEFAULT_MAX_REPORTS_COUNT_LOWER_BOUND};
         *                                  {@value AppMetricaDefaultValues#DEFAULT_MAX_REPORTS_COUNT_UPPER_BOUND}].
         *                                  If not, closest possible value will be used.
         *
         * @return the same {@link Builder} object.
         */
        @NonNull
        public Builder withMaxReportsInDatabaseCount(int maxReportsInDatabaseCount) {
            mMaxReportsInDatabaseCount = configChecker.getCheckedMaxReportsInDatabaseCount(maxReportsInDatabaseCount);
            return this;
        }

        /**
         * Sets the ID of the user profile.
         *
         * <b>NOTE:</b> The string value can contain up to 200 characters.
         *
         * @param userProfileID The custom user profile ID.
         * @return the same {@link Builder} object
         * @see AppMetrica#setUserProfileID(String)
         */
        @NonNull
        public ReporterConfig.Builder withUserProfileID(@Nullable String userProfileID) {
            this.userProfileID = userProfileID;
            return this;
        }

        /**
         * Sets maximum buffer size for reports.
         * <p> <b>NOTE:</b> Default value is
         * {@value AppMetricaDefaultValues#DEFAULT_MAX_REPORTS_COUNT}.
         * If you set a non-positive value,
         * then automatic sending will be disabled
         * for the situation when the events buffer is full.
         *
         * @param maxReportsCount Max number of items/reports to automatically send reports.
         *
         * @see Builder#withDispatchPeriodSeconds(int)
         *
         * @return the same {@link Builder} object.
         */
        @NonNull
        public Builder withMaxReportsCount(int maxReportsCount) {
            this.maxReportsCount = maxReportsCount;
            return this;
        }

        /**
         * Timeout of events sending if the number of events is less than {@link ReporterConfig#maxReportsCount}.
         * <p> <b>NOTE:</b> Default value is
         * {@value AppMetricaDefaultValues#DEFAULT_DISPATCH_PERIOD_SECONDS}.
         * If you set a non-positive value,
         * then automatic sending by timer will be disabled.
         *
         * @param dispatchPeriodSeconds Timeout in seconds to automatically send reports.
         *
         * @see Builder#withMaxReportsCount(int)
         *
         * @return the same {@link Builder} object.
         */
        @NonNull
        public Builder withDispatchPeriodSeconds(int dispatchPeriodSeconds) {
            this.dispatchPeriodSeconds = dispatchPeriodSeconds;
            return this;
        }

        /**
         * Sets key - value pair to be used as additional information, associated
         * with your application runtime's environment. This environment is unique for every unique
         * APIKey and shared between processes. Application's environment persists to storage and
         * retained between application launches. To reset environment use
         * Pairs added to config builder, will be set right after metrica initialization.
         * <p> <b>WARNING:</b> Application's environment is a global permanent state and
         * can't be changed too often. For frequently changed parameters use extended reportMessage methods.
         * @param key the environment key.
         * @param value the environment value. To remove pair from environment pass {@code null} value.
         * @return the same object
         */
        @NonNull
        public Builder withAppEnvironmentValue(String key, String value) {
            this.appEnvironment.put(key, value);
            return this;
        }

        /**
         * Sets key - config data
         * @param key the config key.
         * @param config the config value.
         * @return the same {@link Builder} object.
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
         * Creates instance of {@link ReporterConfig}
         *
         * @return {@link ReporterConfig} object
         */
        @NonNull
        public ReporterConfig build() {
            return new ReporterConfig(this);
        }
    }

}
