package io.appmetrica.analytics.internal;

import android.location.Location;
import android.os.Build;
import androidx.annotation.NonNull;
import io.appmetrica.analytics.AppMetricaConfig;
import io.appmetrica.analytics.ReporterConfig;
import io.appmetrica.analytics.impl.CounterConfigurationReporterType;
import io.appmetrica.analytics.impl.SdkData;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.DummyLocationProvider;
import java.util.Arrays;
import java.util.Collection;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.ParameterizedRobolectricTestRunner;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = Build.VERSION_CODES.LOLLIPOP)
public class CounterConfigurationTest extends CommonTest {

    @RunWith(ParameterizedRobolectricTestRunner.class)
    public static final class ReporterTypeTest {

        @ParameterizedRobolectricTestRunner.Parameters(name = "Report type: {0}")
        public static Collection<Object[]> data() {
            return Arrays.asList(new Object[][]{
                    {CounterConfigurationReporterType.COMMUTATION, "commutation"},
                    {CounterConfigurationReporterType.MAIN, "main"},
                    {CounterConfigurationReporterType.MANUAL, "manual"},
                    {CounterConfigurationReporterType.SELF_DIAGNOSTIC_MAIN, "self_diagnostic_main"},
                    {CounterConfigurationReporterType.SELF_DIAGNOSTIC_MANUAL, "self_diagnostic_manual"},
                    {CounterConfigurationReporterType.SELF_SDK, "self_sdk"},
                    {CounterConfigurationReporterType.CRASH, "crash"}
            });
        }

        @NonNull
        private final String mStringValue;
        @NonNull
        private final CounterConfigurationReporterType mReporterType;

        public ReporterTypeTest(@NonNull CounterConfigurationReporterType reporterType, @NonNull String stringValue) {
            mReporterType = reporterType;
            mStringValue = stringValue;
        }

        @Test
        public void testStringValue() {
            assertThat(mReporterType.getStringValue()).isEqualTo(mStringValue);
        }

        @Test
        public void testFromString() {
            assertThat(CounterConfigurationReporterType.fromStringValue(mStringValue)).isEqualTo(mReporterType);
        }
    }

    private CounterConfiguration mCounterConfiguration;

    @Before
    public void setUp() {
        mCounterConfiguration = new CounterConfiguration();
    }

    @Test
    public void testWriteDefaultDispatchPeriod() {
        int dispatchPeriod = 170;
        mCounterConfiguration.setDispatchPeriod(dispatchPeriod);
        assertThat(mCounterConfiguration.getDispatchPeriod()).isEqualTo(dispatchPeriod);
    }

    @Test
    public void testWriteZeroToDispatchPeriod() {
        mCounterConfiguration.setDispatchPeriod(0);
        assertThat(mCounterConfiguration.getDispatchPeriod()).isZero();
    }

    @Test
    public void testRewriteDispatchPeriod() {
        int dispatchPeriod = 270;
        mCounterConfiguration.setDispatchPeriod(100);
        assertThat(mCounterConfiguration.getDispatchPeriod()).isNotEqualTo(dispatchPeriod);
        mCounterConfiguration.setDispatchPeriod(dispatchPeriod);
        assertThat(mCounterConfiguration.getDispatchPeriod()).isEqualTo(dispatchPeriod);
    }

    @Test
    public void testWriteSessionTimeout() {
        int sessionTimeout = 160;
        mCounterConfiguration.setSessionTimeout(sessionTimeout);
        assertThat(mCounterConfiguration.getSessionTimeout()).isEqualTo(sessionTimeout);
    }

    @Test
    public void testRewriteSessionTimeout() {
        int sessionTimeout = 140;
        mCounterConfiguration.setSessionTimeout(80);
        assertThat(mCounterConfiguration.getSessionTimeout()).isNotEqualTo(sessionTimeout);
        mCounterConfiguration.setSessionTimeout(sessionTimeout);
        assertThat(mCounterConfiguration.getSessionTimeout()).isEqualTo(sessionTimeout);
    }

    @Test
    public void testWriteMaxReportsCount() {
        int maxReportsCount = 22;
        mCounterConfiguration.setMaxReportsCount(maxReportsCount);
        assertThat(mCounterConfiguration.getMaxReportsCount()).isEqualTo(maxReportsCount);
    }

    @Test
    public void testWriteZeroToMaxReportsCount() {
        mCounterConfiguration.setMaxReportsCount(0);
        assertThat(mCounterConfiguration.getMaxReportsCount()).isEqualTo(Integer.MAX_VALUE);
    }

    @Test
    public void testWriteNegativeValueToMaxReportsCount() {
        mCounterConfiguration.setMaxReportsCount(-1);
        assertThat(mCounterConfiguration.getMaxReportsCount()).isEqualTo(Integer.MAX_VALUE);
    }

    @Test
    public void testRewriteMaxReportsCount() {
        int maxReportsCount = 44;
        mCounterConfiguration.setMaxReportsCount(12);
        assertThat(mCounterConfiguration.getMaxReportsCount()).isNotEqualTo(maxReportsCount);
        mCounterConfiguration.setMaxReportsCount(maxReportsCount);
        assertThat(mCounterConfiguration.getMaxReportsCount()).isEqualTo(maxReportsCount);
    }

    @Test
    public void testRewriteZeroToMaxReportsCount() {
        mCounterConfiguration.setMaxReportsCount(20);
        mCounterConfiguration.setMaxReportsCount(0);
        assertThat(mCounterConfiguration.getMaxReportsCount()).isEqualTo(Integer.MAX_VALUE);
    }

    @Test
    public void testRewriteNegativeValueToMaxReportsCount() {
        mCounterConfiguration.setMaxReportsCount(30);
        mCounterConfiguration.setMaxReportsCount(-1);
        assertThat(mCounterConfiguration.getMaxReportsCount()).isEqualTo(Integer.MAX_VALUE);
    }

    @Test
    public void testWriteFalseToReportLocationEnabled() {
        mCounterConfiguration.setLocationTracking(false);
        assertThat(mCounterConfiguration.isLocationTrackingEnabled()).isFalse();
    }

    @Test
    public void testWriteTrueToReportLocationEnabled() {
        mCounterConfiguration.setLocationTracking(true);
        assertThat(mCounterConfiguration.isLocationTrackingEnabled()).isTrue();
    }

    @Test
    public void testRewriteReportLocationEnabled() {
        mCounterConfiguration.setLocationTracking(true);
        mCounterConfiguration.setLocationTracking(false);
        assertThat(mCounterConfiguration.isLocationTrackingEnabled()).isFalse();
        mCounterConfiguration.setLocationTracking(true);
        assertThat(mCounterConfiguration.isLocationTrackingEnabled()).isTrue();
    }

    @Test
    public void testDefaultManualLocation() {
        assertThat(mCounterConfiguration.getManualLocation()).isNull();
    }

    @Test
    public void testWriteNullToManualLocation() {
        mCounterConfiguration.setManualLocation(null);
        assertThat(mCounterConfiguration.getManualLocation()).isNull();
    }

    @Test
    public void testWriteManualLocation() {
        Location location = DummyLocationProvider.getLocation();
        mCounterConfiguration.setManualLocation(location);
        assertThat(mCounterConfiguration.getManualLocation()).isEqualToComparingFieldByFieldRecursively(location);
    }

    @Test
    public void testRewriteManualLocation() {
        Location location = new Location("535");
        Location lastLocation = DummyLocationProvider.getLocation();
        mCounterConfiguration.setManualLocation(location);
        mCounterConfiguration.setManualLocation(lastLocation);
        assertThat(mCounterConfiguration.getManualLocation()).isEqualToComparingFieldByField(lastLocation);
    }

    @Test
    public void testDefaultAppVersion() {
        assertThat(mCounterConfiguration.getAppVersion()).isNull();
    }

    @Test
    public void testWriteAppVersion() {
        String appVersion = "3.23";
        mCounterConfiguration.setCustomAppVersion(appVersion);
        assertThat(mCounterConfiguration.getAppVersion()).isEqualTo(appVersion);
    }

    @Test
    public void testRewriteAppVersion() {
        mCounterConfiguration.setCustomAppVersion("3.55");
        String appVersion = "5.33";
        mCounterConfiguration.setCustomAppVersion(appVersion);
        assertThat(mCounterConfiguration.getAppVersion()).isEqualTo(appVersion);
    }

    @Test
    public void testDefaultBuildNumber() {
        assertThat(mCounterConfiguration.getAppBuildNumber()).isEqualTo(null);
    }

    @Test
    public void testWriteBuildNumber() {
        int buildNumber = 232;
        mCounterConfiguration.setAppBuildNumber(buildNumber);
        assertThat(mCounterConfiguration.getAppBuildNumber()).isEqualTo(String.valueOf(buildNumber));
    }

    @Test
    public void testRewriteBuildNumber() {
        mCounterConfiguration.setAppBuildNumber(311);
        int buildNumber = 4545;
        mCounterConfiguration.setAppBuildNumber(buildNumber);
        assertThat(mCounterConfiguration.getAppBuildNumber()).isEqualTo(String.valueOf(buildNumber));
    }

    @Test
    public void testDefaultDeviceType() {
        assertThat(mCounterConfiguration.getDeviceType()).isNull();
    }

    @Test
    public void testWriteValidDeviceType() {
        String deviceType = "phone";
        mCounterConfiguration.setDeviceType(deviceType);
        assertThat(mCounterConfiguration.getDeviceType()).isEqualTo(deviceType);
    }

    @Test
    public void testRewriteValidDeviceType() {
        String deviceType = "phone";
        mCounterConfiguration.setDeviceType("car");
        assertThat(mCounterConfiguration.getDeviceType()).isNotEqualTo(deviceType);
        mCounterConfiguration.setDeviceType(deviceType);
        assertThat(mCounterConfiguration.getDeviceType()).isEqualTo(deviceType);
    }

    @Test
    public void testWriteEmptyDeviceType() {
        mCounterConfiguration.setDeviceType(null);
        assertThat(mCounterConfiguration.getDeviceType()).isNull();
    }

    @Test
    public void testRewriteEmptyDeviceType() {
        mCounterConfiguration.setDeviceType("phone");
        assertThat(mCounterConfiguration.getDeviceType()).isNotNull();
        mCounterConfiguration.setDeviceType(null);
        assertThat(mCounterConfiguration.getDeviceType()).isNull();
    }

    @Test
    public void testRewriteDefaultWithMaxReportsCountFromConfig() {
        int maxReportsCount = 20;
        mCounterConfiguration.setMaxReportsCount(30);
        assertThat(mCounterConfiguration.getMaxReportsCount()).isNotEqualTo(maxReportsCount);
        mCounterConfiguration.setMaxReportsCount(maxReportsCount);
        assertThat(mCounterConfiguration.getMaxReportsCount()).isEqualTo(maxReportsCount);
    }

    @Test
    public void testIsFirstActivationAsUpdate() throws Exception {
        String apiKey = UUID.randomUUID().toString();
        AppMetricaConfig metricaInternalConfig = AppMetricaConfig.newConfigBuilder(apiKey).
                handleFirstActivationAsUpdate(true).
                build();
        assertThat(new CounterConfiguration(metricaInternalConfig, CounterConfigurationReporterType.MAIN).isFirstActivationAsUpdate()).isTrue();
    }

    @Test
    public void testMaxReportsInDatabaseCountFromConfig() {
        final int maxReportsInDatabaseCount = 2000;
        CounterConfiguration config = new CounterConfiguration(AppMetricaConfig.newConfigBuilder(UUID.randomUUID().toString())
                .withMaxReportsInDatabaseCount(maxReportsInDatabaseCount).build(), CounterConfigurationReporterType.MAIN);
        assertThat(config.getMaxReportsInDbCount()).isEqualTo(maxReportsInDatabaseCount);
    }

    @Test
    public void testDefaultMaxReportsInDatabaseCount() {
        CounterConfiguration config = new CounterConfiguration(
                AppMetricaConfig.newConfigBuilder(UUID.randomUUID().toString()).build(),
                CounterConfigurationReporterType.MAIN
        );
        assertThat(config.getMaxReportsInDbCount()).isNull();
    }

    @Test
    public void testReportNativeCrashesEnabled() {
        String apiKey = UUID.randomUUID().toString();
        CounterConfiguration config = new CounterConfiguration(AppMetricaConfig.newConfigBuilder(apiKey)
                .withNativeCrashReporting(true).build(), CounterConfigurationReporterType.MAIN);
        assertThat(config.getReportNativeCrashesEnabled()).isTrue();
    }

    @Test
    public void testReportNativeCrashesDisabled() {
        String apiKey = UUID.randomUUID().toString();
        CounterConfiguration config = new CounterConfiguration(AppMetricaConfig.newConfigBuilder(apiKey)
                .withNativeCrashReporting(false).build(), CounterConfigurationReporterType.MAIN);
        assertThat(config.getReportNativeCrashesEnabled()).isFalse();
    }

    @Test
    public void testReportNativeCrashesNull() {
        String apiKey = UUID.randomUUID().toString();
        CounterConfiguration config = new CounterConfiguration(
                AppMetricaConfig.newConfigBuilder(apiKey).build(),
                CounterConfigurationReporterType.MAIN
        );
        assertThat(config.getReportNativeCrashesEnabled()).isNull();
    }

    @Test
    public void revenueAutoTrackingEnabledNotSetFromConfig() {
        CounterConfiguration config = new CounterConfiguration(
                AppMetricaConfig.newConfigBuilder(UUID.randomUUID().toString())
                        .build(),
                CounterConfigurationReporterType.MAIN
        );
        assertThat(config.isRevenueAutoTrackingEnabled()).isNull();
    }

    @Test
    public void setRevenueAutoTrackingEnabled() {
        CounterConfiguration config = new CounterConfiguration();
        config.setRevenueAutoTrackingEnabled(true);
        assertThat(config.isRevenueAutoTrackingEnabled()).isTrue();
        config.setRevenueAutoTrackingEnabled(false);
        assertThat(config.isRevenueAutoTrackingEnabled()).isFalse();
    }

    @Test
    public void testAppMetricaConfigReporterTypeMain() {
        String apiKey = UUID.randomUUID().toString();
        CounterConfiguration config = new CounterConfiguration(
                AppMetricaConfig.newConfigBuilder(apiKey).build(),
                CounterConfigurationReporterType.MAIN
        );
        assertThat(config.getReporterType()).isEqualTo(CounterConfigurationReporterType.MAIN);
    }

    @Test
    public void testAppMetricaConfigReporterTypeCrash() {
        String apiKey = UUID.randomUUID().toString();
        CounterConfiguration config = new CounterConfiguration(
                AppMetricaConfig.newConfigBuilder(apiKey).build(),
                CounterConfigurationReporterType.CRASH
        );
        assertThat(config.getReporterType()).isEqualTo(CounterConfigurationReporterType.CRASH);
    }

    @Test
    public void testReporterConfigReporterType() {
        String apiKey = UUID.randomUUID().toString();
        CounterConfiguration config = new CounterConfiguration(ReporterConfig.newConfigBuilder(apiKey).build());
        assertThat(config.getReporterType()).isEqualTo(CounterConfigurationReporterType.MANUAL);
    }

    @Test
    public void testReporterConfigReporterTypeAppmetrica() {
        CounterConfiguration config = new CounterConfiguration(ReporterConfig.newConfigBuilder(SdkData.SDK_API_KEY_UUID).build());
        assertThat(config.getReporterType()).isEqualTo(CounterConfigurationReporterType.SELF_SDK);
    }

    @Test
    public void testApiKeyConstructor() {
        String apiKey = UUID.randomUUID().toString();
        CounterConfiguration config = new CounterConfiguration(apiKey);
        assertThat(config.getApiKey()).isEqualTo(apiKey);
    }

    @Test
    public void testSetReporterType() {
        CounterConfiguration config = new CounterConfiguration();
        assertThat(config.getReporterType()).isEqualTo(CounterConfigurationReporterType.MAIN);
        config.setReporterType(CounterConfigurationReporterType.COMMUTATION);
        assertThat(config.getReporterType()).isEqualTo(CounterConfigurationReporterType.COMMUTATION);
        config.setReporterType(CounterConfigurationReporterType.MANUAL);
        assertThat(config.getReporterType()).isEqualTo(CounterConfigurationReporterType.MANUAL);
    }

    @Test
    public void testReporterTypeFromNullValue() {
        assertThat(CounterConfigurationReporterType.fromStringValue(null)).isEqualTo(CounterConfigurationReporterType.MAIN);
    }

}
