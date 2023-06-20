package io.appmetrica.analytics.impl;

import android.location.Location;
import androidx.annotation.NonNull;
import io.appmetrica.analytics.AppMetricaConfig;
import io.appmetrica.analytics.PreloadInfo;
import io.appmetrica.analytics.testutils.CommonTest;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import junit.framework.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.ParameterizedRobolectricTestRunner;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

@RunWith(ParameterizedRobolectricTestRunner.class)
public class ClientConfigConverterTest extends CommonTest {

    private static final String API_KEY = UUID.randomUUID().toString();

    private final AppMetricaConfig mTestConfig;

    private static final List<AppMetricaConfig> TEST_CONFIGS = new ArrayList<AppMetricaConfig>() {
        {

            AppMetricaConfig.Builder config = AppMetricaConfig.newConfigBuilder(API_KEY);
            add(config.build());

            add(config.withAppVersion("2.2.2").build());

            add(config.withLogs().build());

            add(config.withLocationTracking(true).build());
            add(config.withLocationTracking(false).build());

            add(config.withCrashReporting(true).build());
            add(config.withCrashReporting(false).build());

            add(config.withNativeCrashReporting(true).build());
            add(config.withNativeCrashReporting(false).build());

            add(config.withSessionTimeout(123).build());
            add(config.withSessionTimeout(Integer.MAX_VALUE).build());
            add(config.withSessionTimeout(Integer.MIN_VALUE).build());

            add(config.withPreloadInfo(PreloadInfo.newBuilder(null).build()).build());
            add(config.withPreloadInfo(PreloadInfo.newBuilder("").build()).build());
            add(config.withPreloadInfo(PreloadInfo.newBuilder("id-112").build()).build());
            add(config.withPreloadInfo(
                    PreloadInfo.newBuilder("id-112")
                            .setAdditionalParams("param1", "value1")
                            .build()
            ).build());
            add(config.withPreloadInfo(
                    PreloadInfo.newBuilder("")
                            .setAdditionalParams("param1", "")
                            .build()
            ).build());

            add(config.withLocation(getTestLocation()).build());
            add(config.withLocation(getInvalidTestLocation()).build());
            add(config.withLocation(getEmptyTestLocation()).build());
        }
    };

    @ParameterizedRobolectricTestRunner.Parameters()
    public static Collection<Object[]> data() {
        ArrayList<Object[]> list = new ArrayList<Object[]>();
        for (int i = 0; i < TEST_CONFIGS.size(); i++) {
            list.add(new Object[]{i});
        }
        return list;
    }

    public ClientConfigConverterTest(int index) {
        mTestConfig = TEST_CONFIGS.get(index);
    }

    @NonNull
    private static Location getTestLocation() {
        Location location = mock(Location.class);
        doReturn("provider_value").when(location).getProvider();
        doReturn(1.2).when(location).getLatitude();
        doReturn(2.3).when(location).getLongitude();
        doReturn(123L).when(location).getTime();
        doReturn(3.4f).when(location).getAccuracy();
        return location;
    }

    @NonNull
    private static Location getInvalidTestLocation() {
        Location location = mock(Location.class);
        doReturn("").when(location).getProvider();
        doReturn(Double.MAX_VALUE).when(location).getLatitude();
        doReturn(Double.MIN_VALUE).when(location).getLongitude();
        doReturn(Long.MAX_VALUE).when(location).getTime();
        return location;
    }

    @NonNull
    private static Location getEmptyTestLocation() {
        Location location = mock(Location.class);
        doReturn(0.0).when(location).getLatitude();
        doReturn(0.0).when(location).getLongitude();
        return location;
    }

    @Test
    public void assertConfigEquals() {
        ClientConfigSerializer storage = new ClientConfigSerializer();

        String jsonConfig = storage.toJson(mTestConfig);
        AppMetricaConfig resultConfig = storage.fromJson(jsonConfig);

        Assert.assertEquals(resultConfig.apiKey, mTestConfig.apiKey);
        Assert.assertEquals(resultConfig.appVersion, mTestConfig.appVersion);
        Assert.assertEquals(resultConfig.sessionTimeout, mTestConfig.sessionTimeout);


        Assert.assertEquals(resultConfig.logs, mTestConfig.logs);
        Assert.assertEquals(resultConfig.crashReporting, mTestConfig.crashReporting);
        Assert.assertEquals(resultConfig.nativeCrashReporting, mTestConfig.nativeCrashReporting);
        Assert.assertEquals(resultConfig.locationTracking, mTestConfig.locationTracking);

        assertLocationsEquals(mTestConfig.location, resultConfig.location);
        assertPreloadInfoEquals(mTestConfig.preloadInfo, resultConfig.preloadInfo);
    }

    private void assertLocationsEquals(final Location expected, final Location actual) {
        Assert.assertEquals(expected != null, actual != null);
        if (expected != null) {
            Assert.assertEquals(expected.getProvider(), actual.getProvider());
            Assert.assertEquals(expected.getLatitude(), actual.getLatitude());
            Assert.assertEquals(expected.getLongitude(), actual.getLongitude());
            Assert.assertEquals(expected.getTime(), actual.getTime());
            Assert.assertEquals(expected.getAccuracy(), actual.getAccuracy());
        }
    }

    private void assertPreloadInfoEquals(final PreloadInfo expected, final PreloadInfo actual) {
        Assert.assertEquals(expected != null, actual != null);
        if (expected != null) {
            Assert.assertEquals(expected.getTrackingId(), actual.getTrackingId());
            Assert.assertEquals(expected.getAdditionalParams(), actual.getAdditionalParams());
        }
    }
}
