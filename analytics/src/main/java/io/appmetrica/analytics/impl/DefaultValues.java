package io.appmetrica.analytics.impl;

import io.appmetrica.analytics.BuildConfig;
import io.appmetrica.analytics.impl.protobuf.client.StartupStateProtobuf;

public class DefaultValues {
    public static final int DEFAULT_SESSION_TIMEOUT_SECONDS = 10;
    public static final boolean DEFAULT_REPORT_LOCATION_ENABLED = BuildConfig.DEFAULT_LOCATION_COLLECTING;
    public static final boolean DEFAULT_FIRST_ACTIVATION_AS_UPDATE = false;
    public static final boolean DEFAULT_AUTO_PRELOAD_INFO_DETECTION = false;
    public static final boolean DEFAULT_LOG_ENABLED = false;
    public static final boolean DEFAULT_STATISTICS_SENDING = true;
    public static final boolean DEFAULT_SESSIONS_AUTO_TRACKING_ENABLED = true;
    public static final boolean DEFAULT_REVENUE_AUTO_TRACKING_ENABLED = true;
    public static final boolean DEFAULT_APP_OPEN_TRACKING_ENABLED = true;
    public static final int DEFAULT_DISPATCH_PERIOD_SECONDS = 90;
    public static final int DEFAULT_MAX_REPORTS_COUNT = 7;
    public static final int MAX_REPORTS_IN_DB_COUNT_DEFAULT = 1000;
    public static final int DEFAULT_MAX_REPORTS_COUNT_LOWER_BOUND = 100;
    public static final int DEFAULT_MAX_REPORTS_COUNT_UPPER_BOUND = 10000;
    public static final int DEFAULT_ANR_TICKS_COUNT = 5;

    public static final StartupStateProtobuf.StartupState.StartupUpdateConfig STARTUP_UPDATE_CONFIG =
        new StartupStateProtobuf.StartupState.StartupUpdateConfig();
}
