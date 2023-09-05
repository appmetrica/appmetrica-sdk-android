package io.appmetrica.analytics;

import io.appmetrica.analytics.impl.DefaultValues;
import io.appmetrica.analytics.impl.DefaultValuesForCrashReporting;

/**
 * This class contains constants with default values for different aspects of SDK.
 */
public class AppMetricaDefaultValues {
    /**
     * Default session timeout in seconds.
     */
    public static final int DEFAULT_SESSION_TIMEOUT_SECONDS = DefaultValues.DEFAULT_SESSION_TIMEOUT_SECONDS;
    /**
     * Default report crashes enabled flag.
     */
    public static final boolean DEFAULT_REPORTS_CRASHES_ENABLED =
            DefaultValuesForCrashReporting.DEFAULT_REPORTS_CRASHES_ENABLED;
    /**
     * Default report native crashes enabled flag.
     */
    public static final boolean DEFAULT_REPORTS_NATIVE_CRASHES_ENABLED =
            DefaultValuesForCrashReporting.DEFAULT_REPORTS_NATIVE_CRASHES_ENABLED;
    /**
     * Default report location enabled flag.
     */
    public static final boolean DEFAULT_REPORT_LOCATION_ENABLED = DefaultValues.DEFAULT_REPORT_LOCATION_ENABLED;
    /**
     * Default report statistics sending enabled flag.
     */
    public static final boolean DEFAULT_REPORTER_STATISTICS_SENDING = true;
    /**
     * Default revenue auto tracking enabled flag.
     */
    public static final boolean DEFAULT_REVENUE_AUTO_TRACKING_ENABLED =
            DefaultValues.DEFAULT_REVENUE_AUTO_TRACKING_ENABLED;
    /**
     * Default sessions auto tracking enabled flag.
     */
    public static final boolean DEFAULT_SESSIONS_AUTO_TRACKING_ENABLED =
            DefaultValues.DEFAULT_SESSIONS_AUTO_TRACKING_ENABLED;
    /**
     * Default app open tracking enabled flag.
     */
    public static final boolean DEFAULT_APP_OPEN_TRACKING_ENABLED =
            DefaultValues.DEFAULT_APP_OPEN_TRACKING_ENABLED;
    /**
     * Default dispatch period in seconds.
     */

    public static final int DEFAULT_DISPATCH_PERIOD_SECONDS = DefaultValues.DEFAULT_DISPATCH_PERIOD_SECONDS;
    /**
     * Default max reports count.
     */
    public static final int DEFAULT_MAX_REPORTS_COUNT = DefaultValues.DEFAULT_MAX_REPORTS_COUNT;
    /**
     * Default max reports in database count.
     */
    public static final int DEFAULT_MAX_REPORTS_IN_DATABASE_COUNT = DefaultValues.MAX_REPORTS_IN_DB_COUNT_DEFAULT;
    /**
     * Default lower bound for max reports count.
     */
    public static final int DEFAULT_MAX_REPORTS_COUNT_LOWER_BOUND = DefaultValues.DEFAULT_MAX_REPORTS_COUNT_LOWER_BOUND;
    /**
     * Default upper bound for max reports count.
     */
    public static final int DEFAULT_MAX_REPORTS_COUNT_UPPER_BOUND = DefaultValues.DEFAULT_MAX_REPORTS_COUNT_UPPER_BOUND;
    /**
     * Default ANR ticks count.
     */
    public static final int DEFAULT_ANR_TICKS_COUNT = DefaultValues.DEFAULT_ANR_TICKS_COUNT;
}
