package io.appmetrica.analytics.impl;

import io.appmetrica.analytics.BuildConfig;

public final class SdkData {

    public static final String SDK_API_KEY_UUID = "20799a27-fa80-4b36-b2db-0f8141f24180";
    public static final String SDK_API_KEY_PUSH_SDK = "0e5e9c33-f8c3-4568-86c5-2e4f57523f72";

    public static final String CURRENT_VERSION = BuildConfig.VERSION_NAME;


    //Also change in build-logic/src/main/kotlin/io/appmetrica/analytics/gradle/Constants.kt
    public static final int CURRENT = 116;

    //This version used for creation merge conflicts for parallel version or api level updating
    public static final String CURRENT_VERSION_NAME_FOR_MAPPING = "7.15.0";

    public static final int INITIAL_API_LEVEL = 112;
    public static final int MIGRATE_SESSION_SLEEP_START_TIME_TO_MILLISECONDS = 113;
    public static final int TEMP_CACHE_ADDED = 114;
    public static final int MODULE_CONFIGS_ADDED = 115;
    public static final int BILLING_MOVED_TO_SEPARATE_MODULE = 116;
}
