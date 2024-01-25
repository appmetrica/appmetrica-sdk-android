package io.appmetrica.analytics.impl;

import io.appmetrica.analytics.BuildConfig;

public final class SdkData {

    public static final String SDK_API_KEY_UUID = "20799a27-fa80-4b36-b2db-0f8141f24180";
    public static final String SDK_API_KEY_PUSH_SDK = "0e5e9c33-f8c3-4568-86c5-2e4f57523f72";

    public static final String CURRENT_VERSION = BuildConfig.VERSION_NAME;

    public static final int CURRENT = 112; // Also change in constants.gradle

    //This version used for creation merge conflicts for parallel version or api level updating
    public static final String CURRENT_VERSION_NAME_FOR_MAPPING = "6.2.1";

    public static final int INITIAL_API_LEVEL = 112;
}
