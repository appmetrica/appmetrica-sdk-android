package io.appmetrica.analytics.impl;

import java.util.UUID;

public class TestsData {
    // API keys
    public static final String SOME_STRING_API_KEY = "You're a liar!";
    public static final String INT_API_KEY = String.valueOf(0);
    public static final String NULL_API_KEY = null;
    public static final String EMPTY_API_KEY = "";
    public static final String BIG_INT_API_KEY = String.valueOf(2 * (long) Integer.MAX_VALUE + 999);
    public static final String VERY_BIG_INT_API_KEY = "123456789012345678901234567890";
    public static final String UUID_API_KEY = UUID.randomUUID().toString();

    // Other
    public static final String NON_JSON_SERVER_RESPONSE = "<\"You're a liar!\">";
    public static final String APP_PACKAGE = "com.yandex.test";
    public static final String TEST_ENVIRONMENT_KEY = "test_key";
    public static final String TEST_ERROR_ENVIRONMENT_KEY = "error_key";
    public static final String TEST_ENVIRONMENT_VALUE = "test_value";
    public static final String TEST_ERROR_ENVIRONMENT_VALUE = "error_value";

    public static String generateApiKey() {
        return UUID.randomUUID().toString();
    }

    public static String generatePackage() {
        return APP_PACKAGE + "." + UUID.randomUUID().toString();
    }
}
