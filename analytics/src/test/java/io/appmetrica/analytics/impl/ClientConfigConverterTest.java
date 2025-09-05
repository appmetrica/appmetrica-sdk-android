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
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.ParameterizedRobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

@RunWith(ParameterizedRobolectricTestRunner.class)
public class ClientConfigConverterTest extends CommonTest {

    private static final String API_KEY = UUID.randomUUID().toString();

    private final AppMetricaConfig testConfig;

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
        testConfig = TEST_CONFIGS.get(index);
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

        String jsonConfig = storage.toJson(testConfig);
        AppMetricaConfig resultConfig = storage.fromJson(jsonConfig).build();

        assertThat(resultConfig.apiKey).isEqualTo(testConfig.apiKey);
        assertThat(resultConfig.appVersion).isEqualTo(testConfig.appVersion);
        assertThat(resultConfig.sessionTimeout).isEqualTo(testConfig.sessionTimeout);


        assertThat(resultConfig.logs).isEqualTo(testConfig.logs);
        assertThat(resultConfig.crashReporting).isEqualTo(testConfig.crashReporting);
        assertThat(resultConfig.nativeCrashReporting).isEqualTo(testConfig.nativeCrashReporting);
        assertThat(resultConfig.locationTracking).isEqualTo(testConfig.locationTracking);

        assertLocationsEquals(testConfig.location, resultConfig.location);
        assertPreloadInfoEquals(testConfig.preloadInfo, resultConfig.preloadInfo);
    }

    private void assertLocationsEquals(final Location expected, final Location actual) {
        assertThat(expected != null).isEqualTo(actual != null);
        if (expected != null) {
            assertThat(actual.getProvider()).isEqualTo(expected.getProvider());
            assertThat(actual.getLatitude()).isEqualTo(expected.getLatitude());
            assertThat(actual.getLongitude()).isEqualTo(expected.getLongitude());
            assertThat(actual.getTime()).isEqualTo(expected.getTime());
            assertThat(actual.getAccuracy()).isEqualTo(expected.getAccuracy());
        }
    }

    private void assertPreloadInfoEquals(final PreloadInfo expected, final PreloadInfo actual) {
        assertThat(expected != null).isEqualTo(actual != null);
        if (expected != null) {
            assertThat(actual.getTrackingId()).isEqualTo(expected.getTrackingId());
            assertThat(actual.getAdditionalParams()).isEqualTo(expected.getAdditionalParams());
        }
    }
}
