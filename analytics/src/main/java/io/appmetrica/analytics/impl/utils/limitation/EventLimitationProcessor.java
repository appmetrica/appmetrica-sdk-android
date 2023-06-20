package io.appmetrica.analytics.impl.utils.limitation;

import androidx.annotation.Nullable;
import io.appmetrica.analytics.coreutils.internal.StringUtils;

public class EventLimitationProcessor {

    public static final int SESSIONS_DATA_MAX_SIZE = 245 * 1024;
    public static final int EXTENDED_SINGLE_EVENT_SESSION_DATA_MAX_SIZE = 1024 * 1024;
    public static final int REPORT_VALUE_MAX_SIZE = SESSIONS_DATA_MAX_SIZE - 5 * 1024;
    public static final int REPORT_EXTENDED_VALUE_MAX_SIZE = 1000 * 1024;
    public static final int EVENT_NAME_MAX_LENGTH = 1000;
    public static final int USER_INFO_MAX_LENGTH = 10000;
    public static final int USER_PROFILE_ID_MAX_LENGTH = 200;
    public static final int USER_PROFILE_CUSTOM_ATTRIBUTE_KEY_MAX_LENGTH = 200;
    public static final int USER_PROFILE_STRING_ATTRIBUTE_MAX_LENGTH = 200;
    public static final int USER_PROFILE_NAME_MAX_LENGTH = 100;
    public static final int USER_PROFILE_CUSTOM_ATTRIBUTE_MAX_COUNT = 100;

    public static final int REVENUE_PRODUCT_ID_MAX_LENGTH = 200;
    public static final int REVENUE_PAYLOAD_MAX_SIZE = 30 * 1024;
    public static final int RECEIPT_DATA_MAX_SIZE = 180 * 1024;
    public static final int RECEIPT_SIGNATURE_MAX_LENGTH = 1000;

    public static final int AD_REVENUE_GENERIC_STRING_MAX_SIZE = 100;
    public static final int AD_REVENUE_PAYLOAD_MAX_SIZE = 30 * 1024;

    public static boolean valueWasTrimmed(@Nullable String original, @Nullable String newValue) {
        return StringUtils.equalsNullSafety(original, newValue) == false;
    }
}
