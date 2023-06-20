package io.appmetrica.analytics;

import android.location.Location;
import io.appmetrica.analytics.testutils.LocationUtils;
import java.util.HashMap;

public class TestData {
    public static final String TEST_UUID = "some_test_uuid";
    static final int TEST_APP_BUILD_NUMBER = 1000;
    public static final String TEST_CUSTOM_HOST_URL = "http://metrica.heroism.yandex.ru";
    static final String TEST_APP_VERSION = "some_test_app_version";
    static final int TEST_SESSION_TIMEOUT = 100000;
    static final boolean TEST_REPORT_CRASHES_ENABLED = false;
    static final boolean TEST_REPORT_NATIVE_CRASHES_ENABLED = false;
    static final boolean TEST_ANR_MONITORING = false;
    static final int TEST_ANR_MONITORING_TIMEOUT = 42;
    public static final Location TEST_LOCATION = LocationUtils.INSTANCE.createFakeLocation(10000.0, 20000.0);
    static final boolean TEST_TRACK_LOCATION_ENABLED = false;
    public static final HashMap<String, String> TEST_CLIDS = new HashMap<String, String>() {
        {
            put("test", "123456");
            put("clid1", "34536");
            put("clid2", "1234567");
        }
    };
    static final int TEST_MAX_REPORTS_COUNT = 100;
    static final int TEST_MAX_REPORTS_IN_DB_COUNT = 2000;
    static final int TEST_NEGATIVE_MAX_REPORTS_COUNT = -100;
    static final int TEST_DISPATCH_PERIOD_SECONDS = 9000;
    static final int TEST_NEGATIVE_DISPATCH_PERIOD_SECONDS = -100;

}
