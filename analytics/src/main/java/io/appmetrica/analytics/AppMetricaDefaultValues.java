package io.appmetrica.analytics;

import io.appmetrica.analytics.impl.DefaultValues;
import io.appmetrica.analytics.impl.DefaultValuesForCrashReporting;

public class AppMetricaDefaultValues {
    public static final int DEFAULT_SESSION_TIMEOUT_SECONDS = DefaultValues.DEFAULT_SESSION_TIMEOUT_SECONDS;
    public static final boolean DEFAULT_REPORTS_CRASHES_ENABLED =
            DefaultValuesForCrashReporting.DEFAULT_REPORTS_CRASHES_ENABLED;
    public static final boolean DEFAULT_REPORTS_NATIVE_CRASHES_ENABLED =
            DefaultValuesForCrashReporting.DEFAULT_REPORTS_NATIVE_CRASHES_ENABLED;
    public static final boolean DEFAULT_REPORT_LOCATION_ENABLED = DefaultValues.DEFAULT_REPORT_LOCATION_ENABLED;
    public static final boolean DEFAULT_REPORTER_STATISTICS_SENDING = true;
    public static final boolean DEFAULT_REVENUE_AUTO_TRACKING_ENABLED =
            DefaultValues.DEFAULT_REVENUE_AUTO_TRACKING_ENABLED;
    public static final boolean DEFAULT_SESSIONS_AUTO_TRACKING_ENABLED =
            DefaultValues.DEFAULT_SESSIONS_AUTO_TRACKING_ENABLED;
    public static final boolean DEFAULT_APP_OPEN_TRACKING_ENABLED =
            DefaultValues.DEFAULT_APP_OPEN_TRACKING_ENABLED;

    public static final int DEFAULT_DISPATCH_PERIOD_SECONDS = DefaultValues.DEFAULT_DISPATCH_PERIOD_SECONDS;
    public static final int DEFAULT_MAX_REPORTS_COUNT = DefaultValues.DEFAULT_MAX_REPORTS_COUNT;
    public static final int DEFAULT_MAX_REPORTS_IN_DATABASE_COUNT = DefaultValues.MAX_REPORTS_IN_DB_COUNT_DEFAULT;
    public static final int DEFAULT_MAX_REPORTS_COUNT_LOWER_BOUND = DefaultValues.DEFAULT_MAX_REPORTS_COUNT_LOWER_BOUND;
    public static final int DEFAULT_MAX_REPORTS_COUNT_UPPER_BOUND = DefaultValues.DEFAULT_MAX_REPORTS_COUNT_UPPER_BOUND;
    public static final int DEFAULT_ANR_TICKS_COUNT = DefaultValues.DEFAULT_ANR_TICKS_COUNT;
}
