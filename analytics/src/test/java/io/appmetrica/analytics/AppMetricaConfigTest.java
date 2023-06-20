package io.appmetrica.analytics;

import android.location.Location;
import io.appmetrica.analytics.impl.TestsData;
import io.appmetrica.analytics.impl.VerificationConstants;
import io.appmetrica.analytics.impl.proxy.validation.ConfigChecker;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.ContainsArgumentMessageMatcher;
import io.appmetrica.analytics.testutils.MockedConstructionRule;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.robolectric.RobolectricTestRunner;

import static io.appmetrica.analytics.assertions.AssertionsKt.ObjectPropertyAssertions;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class AppMetricaConfigTest extends CommonTest {

    protected static final String sApiKey = TestsData.generateApiKey();
    private final String mApiKey = UUID.randomUUID().toString();
    private final String errorEnvKeyFirst = "key1";
    private final String errorEnvValueFirst = "value1";
    private final String errorEnvKeySecond = "key2";
    private final String errorEnvValueSecond = "value2";
    private final Map<String, String> errorEnvironmentMap = new HashMap<String, String>() {
        {
            put(errorEnvKeyFirst, errorEnvValueFirst);
            put(errorEnvKeySecond, errorEnvValueSecond);
        }
    };
    private final Random random = new Random();
    private final boolean handleFirstActivationAsUpdate = random.nextBoolean();
    private final String appVersion = "1.3.6";
    private final boolean crashReporting = random.nextBoolean();
    private final Location location = mock(Location.class);
    private final boolean locationTracking = random.nextBoolean();
    private final int maxReportsInDbCount = 800;
    private final boolean nativeCrashReporting = random.nextBoolean();
    private final int sessionTimeout = 23;
    private final boolean statisticsSending = random.nextBoolean();
    private final boolean revenueAutoTrackingEnabled = random.nextBoolean();
    private final boolean sessionsAutoTrackingEnabled = random.nextBoolean();
    private final boolean appOpenTrackingEnabled = random.nextBoolean();
    private final String userProfileID = "user_profile_id";
    private final int oldMaxReportsInDatabaseCount = 10;
    private final int newMaxReportsInDatabaseCount = 100;
    @Rule
    public final MockedConstructionRule<ConfigChecker> configCheckerRule =
        new MockedConstructionRule<>(ConfigChecker.class,
            new MockedConstruction.MockInitializer<ConfigChecker>() {
                @Override
                public void prepare(ConfigChecker mock, MockedConstruction.Context context) {
                    when(mock.getCheckedMaxReportsInDatabaseCount(anyInt())).thenAnswer(new Answer<Integer>() {
                        @Override
                        public Integer answer(InvocationOnMock invocation) throws Throwable {
                            return invocation.getArgument(0);
                        }
                    });
                    when(mock.getCheckedMaxReportsInDatabaseCount(oldMaxReportsInDatabaseCount))
                        .thenReturn(newMaxReportsInDatabaseCount);
                }
            });
    private final String deviceType = PredefinedDeviceTypes.PHONE;
    private final int appBuildNumber = 42;
    private final int dispatchPeriodSeconds = 1488;
    private final int maxReportsCount = 228;
    private final String appEnvKeyFirst = "key1";
    private final String appEnvValueFirst = "value1";
    private final String appEnvKeySecond = "key2";
    private final String appEnvValueSecond = "value2";
    private final Map<String, String> appEnvironmentMap = new HashMap<String, String>() {
        {
            put(appEnvKeyFirst, appEnvValueFirst);
            put(appEnvKeySecond, appEnvValueSecond);
        }
    };
    private final boolean anrMonitoring = random.nextBoolean();
    private final int anrMonitoringTimeout = random.nextInt();
    private final List<String> customHosts = Arrays.asList("customHost1", "customHost2");
    private final String additionalConfigKeyFirst = "key1";
    private final String additionalConfigValueFirst = "value1";
    private final String additionalConfigKeySecond = "key2";
    private final String additionalConfigValueSecond = "value2";
    private final Map<String, Object> additionalConfigMap = new HashMap<String, Object>() {
        {
            put(additionalConfigKeyFirst, additionalConfigValueFirst);
            put(additionalConfigKeySecond, additionalConfigValueSecond);
        }
    };
    @Rule
    public ExpectedException thrown = ExpectedException.none();
    protected AppMetricaConfig mDefaultConfig;
    @Mock
    private PreloadInfo preloadInfo;
    @Mock
    private ICrashTransformer crashTransformer;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mDefaultConfig = getDefaultConfig();
    }

    protected AppMetricaConfig getDefaultConfig() {
        return getDefaultConfigBuilder().build();
    }

    private AppMetricaConfig.Builder getDefaultConfigBuilder() {
        return AppMetricaConfig.newConfigBuilder(sApiKey);
    }

    @Test
    public void createConfigFromConfig() throws Exception {
        ObjectPropertyAssertions(new AppMetricaConfig(createFilledConfig()))
            .checkField("apiKey", mApiKey)
            .checkField("appVersion", appVersion)
            .checkField("sessionTimeout", sessionTimeout)
            .checkField("crashReporting", crashReporting)
            .checkField("nativeCrashReporting", nativeCrashReporting)
            .checkField("location", location)
            .checkField("locationTracking", locationTracking)
            .checkField("logs", true)
            .checkField("preloadInfo", preloadInfo)
            .checkField("firstActivationAsUpdate", handleFirstActivationAsUpdate)
            .checkField("statisticsSending", statisticsSending)
            .checkField("maxReportsInDatabaseCount", maxReportsInDbCount)
            .checkField("errorEnvironment", errorEnvironmentMap)
            .checkField("userProfileID", userProfileID)
            .checkField("revenueAutoTrackingEnabled", revenueAutoTrackingEnabled)
            .checkField("sessionsAutoTrackingEnabled", sessionsAutoTrackingEnabled)
            .checkField("appOpenTrackingEnabled", appOpenTrackingEnabled)
            .checkField("deviceType", deviceType)
            .checkField("appBuildNumber", appBuildNumber)
            .checkField("dispatchPeriodSeconds", dispatchPeriodSeconds)
            .checkField("maxReportsCount", maxReportsCount)
            .checkField("appEnvironment", appEnvironmentMap)
            .checkField("crashTransformer", crashTransformer)
            .checkField("additionalConfig", additionalConfigMap)
            .checkField("anrMonitoring", anrMonitoring)
            .checkField("anrMonitoringTimeout", anrMonitoringTimeout)
            .checkField("customHosts", customHosts)
            .checkAll();
    }

    @Test
    public void jsonSerialization() {
        assertThat(AppMetricaConfig.fromJson(AppMetricaConfig.newConfigBuilder(mApiKey).build().toJson()).apiKey)
            .isEqualTo(mApiKey);
    }

    @Test
    public void createConfigFromEmptyConfig() throws Exception {
        ObjectPropertyAssertions(
            new AppMetricaConfig(AppMetricaConfig.newConfigBuilder(mApiKey).build())
        )
            .checkField("apiKey", mApiKey)
            .checkField("errorEnvironment", new HashMap<String, String>())
            .checkField("appEnvironment", new HashMap<String, String>())
            .checkField("additionalConfig", new HashMap<String, String>())
            .checkFieldsAreNull("appVersion", "sessionTimeout", "crashReporting", "nativeCrashReporting",
                "location", "locationTracking", "logs", "preloadInfo",
                "firstActivationAsUpdate", "statisticsSending", "maxReportsInDatabaseCount",
                "userProfileID", "revenueAutoTrackingEnabled",
                "sessionsAutoTrackingEnabled", "appOpenTrackingEnabled",
                "deviceType",
                "appBuildNumber",
                "dispatchPeriodSeconds",
                "maxReportsCount",
                "crashTransformer",
                "anrMonitoring",
                "anrMonitoringTimeout",
                "customHosts"
            )
            .checkAll();
    }

    private AppMetricaConfig createFilledConfig() {
        return AppMetricaConfig.newConfigBuilder(mApiKey)
            .handleFirstActivationAsUpdate(handleFirstActivationAsUpdate)
            .withAppVersion(appVersion)
            .withCrashReporting(crashReporting)
            .withLocation(location)
            .withLocationTracking(locationTracking)
            .withLogs()
            .withMaxReportsInDatabaseCount(maxReportsInDbCount)
            .withNativeCrashReporting(nativeCrashReporting)
            .withPreloadInfo(preloadInfo)
            .withSessionTimeout(sessionTimeout)
            .withStatisticsSending(statisticsSending)
            .withErrorEnvironmentValue(errorEnvKeyFirst, errorEnvValueFirst)
            .withErrorEnvironmentValue(errorEnvKeySecond, errorEnvValueSecond)
            .withUserProfileID(userProfileID)
            .withRevenueAutoTrackingEnabled(revenueAutoTrackingEnabled)
            .withSessionsAutoTrackingEnabled(sessionsAutoTrackingEnabled)
            .withAppOpenTrackingEnabled(appOpenTrackingEnabled)
            .withDeviceType(deviceType)
            .withAppBuildNumber(appBuildNumber)
            .withDispatchPeriodSeconds(dispatchPeriodSeconds)
            .withMaxReportCount(maxReportsCount)
            .withAppEnvironmentValue(appEnvKeyFirst, appEnvValueFirst)
            .withAppEnvironmentValue(appEnvKeySecond, appEnvValueSecond)
            .withCrashTransformer(crashTransformer)
            .withAnrMonitoring(anrMonitoring)
            .withAnrMonitoringTimeout(anrMonitoringTimeout)
            .withCustomHosts(customHosts)
            .withAdditionalConfig(additionalConfigKeyFirst, additionalConfigValueFirst)
            .withAdditionalConfig(additionalConfigKeySecond, additionalConfigValueSecond)
            .build();
    }

    @Test
    public void testApiKey() {
        assertThat(mDefaultConfig.apiKey).isEqualToIgnoringCase(sApiKey);
    }

    @Test
    public void testEmptyApiKey() {
        thrown.expect(IllegalArgumentException.class);
        AppMetricaConfig.newConfigBuilder("").build();
    }

    @Test
    public void testNullApiKey() {
        thrown.expect(IllegalArgumentException.class);
        AppMetricaConfig.newConfigBuilder(null).build();
    }

    @Test
    public void testAppVersion() {
        final AppMetricaConfig config = getDefaultConfigBuilder()
            .withAppVersion(TestData.TEST_APP_VERSION).build();
        assertThat(config.appVersion).isEqualToIgnoringCase(TestData.TEST_APP_VERSION);
    }

    @Test
    public void testNoDefAppVersion() {
        assertThat(mDefaultConfig.appVersion).isNull();
    }

    @Test
    public void testSessionTimeout() {
        final AppMetricaConfig config = getDefaultConfigBuilder()
            .withSessionTimeout(TestData.TEST_SESSION_TIMEOUT).build();
        assertThat(config.sessionTimeout).isEqualTo(TestData.TEST_SESSION_TIMEOUT);
    }

    @Test
    public void testReportCrashEnabled() {
        final AppMetricaConfig config = getDefaultConfigBuilder()
            .withCrashReporting(TestData.TEST_REPORT_CRASHES_ENABLED).build();
        assertThat(config.crashReporting).isEqualTo(TestData.TEST_REPORT_CRASHES_ENABLED);
    }

    @Test
    public void testNoDefReportCrashEnabled() {
        assertThat(mDefaultConfig.crashReporting).isNull();
    }

    @Test
    public void testReportNativeCrashEnabled() {
        final AppMetricaConfig config = getDefaultConfigBuilder()
            .withNativeCrashReporting(TestData.TEST_REPORT_NATIVE_CRASHES_ENABLED).build();
        assertThat(config.nativeCrashReporting).isEqualTo(TestData.TEST_REPORT_NATIVE_CRASHES_ENABLED);
    }

    @Test
    public void testNoDefReportsNativeCrashEnabled() {
        assertThat(mDefaultConfig.nativeCrashReporting)
            .isNull();
    }

    @Test
    public void testLocation() {
        final AppMetricaConfig config = getDefaultConfigBuilder()
            .withLocation(TestData.TEST_LOCATION).build();
        assertThat(config.location).isEqualTo(TestData.TEST_LOCATION);
    }

    @Test
    public void testNoDefLocation() {
        assertThat(mDefaultConfig.location).isEqualTo(null);
    }

    @Test
    public void testTrackLocationEnabled() {
        final AppMetricaConfig config = getDefaultConfigBuilder()
            .withLocationTracking(TestData.TEST_TRACK_LOCATION_ENABLED).build();
        assertThat(config.locationTracking).isEqualTo(TestData.TEST_TRACK_LOCATION_ENABLED);
    }

    @Test
    public void testNoDefTrackLocationEnabled() {
        assertThat(mDefaultConfig.locationTracking)
            .isNull();
    }

    @Test
    public void testPreloadInfo() {
        PreloadInfo preloadInfo = PreloadInfo.newBuilder("test_tracking_id").setAdditionalParams
            ("test_key", "test_value").build();
        final AppMetricaConfig config = getDefaultConfigBuilder().withPreloadInfo(preloadInfo).build();

        assertThat(config.preloadInfo).isEqualTo(preloadInfo);
    }

    @Test
    public void testNoDefPreloadInfo() {
        assertThat(mDefaultConfig.preloadInfo).isNull();
    }

    @Test
    public void testHandleFirstActivationAsUpdate() {
        AppMetricaConfig config = getDefaultConfigBuilder().handleFirstActivationAsUpdate(true).build();
        assertThat(config.firstActivationAsUpdate).isTrue();

        config = getDefaultConfigBuilder().handleFirstActivationAsUpdate(false).build();
        assertThat(config.firstActivationAsUpdate).isFalse();

    }

    @Test
    public void testNoErrorEnvironment() {
        AppMetricaConfig config = getDefaultConfigBuilder().build();
        assertThat(config.errorEnvironment).isNotNull().isEmpty();
    }

    @Test
    public void testHasErrorEnvironment() {
        String key1 = "key1";
        String value1 = "value1";
        String key2 = "key2";
        String value2 = "value2";
        AppMetricaConfig config = getDefaultConfigBuilder()
            .withErrorEnvironmentValue(key1, value1)
            .withErrorEnvironmentValue(key2, value2)
            .build();
        assertThat(config.errorEnvironment).containsExactly(
            new AbstractMap.SimpleEntry<String, String>(key1, value1),
            new AbstractMap.SimpleEntry<String, String>(key2, value2)
        );

    }

    @Test
    public void userProfileID() {
        String userProfileID = "user_profile_id";
        assertThat(getDefaultConfigBuilder().withUserProfileID(userProfileID).build().userProfileID)
            .isEqualTo(userProfileID);
    }

    @Test
    public void userProfileIDIfNotSet() {
        assertThat(getDefaultConfigBuilder().build().userProfileID).isNull();
    }

    @Test
    public void revenueAutoTrackingEnabled() {
        final AppMetricaConfig config = getDefaultConfigBuilder()
            .withRevenueAutoTrackingEnabled(true).build();
        assertThat(config.revenueAutoTrackingEnabled).isTrue();
    }

    @Test
    public void revenueAutoTrackingEnabledIsNotSet() {
        assertThat(mDefaultConfig.revenueAutoTrackingEnabled).isNull();
    }

    @Test
    public void sessionsAutoTrackingEnabled() {
        final AppMetricaConfig config = getDefaultConfigBuilder()
            .withSessionsAutoTrackingEnabled(true).build();
        assertThat(config.sessionsAutoTrackingEnabled).isTrue();
    }

    @Test
    public void sessionsAutoTrackingDisabled() {
        final AppMetricaConfig config = getDefaultConfigBuilder()
            .withSessionsAutoTrackingEnabled(false).build();
        assertThat(config.sessionsAutoTrackingEnabled).isFalse();
    }

    @Test
    public void sessionsAutoTrackingEnabledIsNotSet() {
        assertThat(mDefaultConfig.sessionsAutoTrackingEnabled).isNull();
    }

    @Test
    public void appOpenTrackingEnabled() {
        final AppMetricaConfig config = getDefaultConfigBuilder()
            .withAppOpenTrackingEnabled(true).build();
        assertThat(config.appOpenTrackingEnabled).isTrue();
    }

    @Test
    public void appOpenTrackingDisabled() {
        final AppMetricaConfig config = getDefaultConfigBuilder()
            .withAppOpenTrackingEnabled(false).build();
        assertThat(config.appOpenTrackingEnabled).isFalse();
    }

    @Test
    public void appOpenTrackingEnabledIsNotSet() {
        assertThat(mDefaultConfig.appOpenTrackingEnabled).isNull();
    }

    @Test
    public void testInvalidMaxReportsInDatabaseCount() {
        AppMetricaConfig config = AppMetricaConfig.newConfigBuilder(mApiKey)
            .withMaxReportsInDatabaseCount(oldMaxReportsInDatabaseCount)
            .build();

        assertThat(config.maxReportsInDatabaseCount).isEqualTo(newMaxReportsInDatabaseCount);
    }

    @Test
    public void testAppBuildNumber() {
        final AppMetricaConfig config = getDefaultConfigBuilder()
            .withAppBuildNumber(TestData.TEST_APP_BUILD_NUMBER).build();
        assertThat(config.appBuildNumber)
            .isEqualTo(TestData.TEST_APP_BUILD_NUMBER);
    }

    @Test
    public void testNegativeBuildNumber() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage(new ContainsArgumentMessageMatcher(VerificationConstants.APP_BUILD_NUMBER));
        getDefaultConfigBuilder().withAppBuildNumber(-1000).build();
    }

    @Test
    public void testNoDefBuildNumber() {
        assertThat(mDefaultConfig.appBuildNumber).isNull();
    }

    @Test
    public void testKnownDeviceTypeDefinedWithString() {
        String deviceType = "tv";
        final AppMetricaConfig config = getDefaultConfigBuilder()
            .withDeviceType(deviceType).build();
        assertThat(config.deviceType).isEqualTo(deviceType);
    }

    @Test
    public void testCustomDeviceType() {
        String deviceType = "car";
        final AppMetricaConfig config = getDefaultConfigBuilder()
            .withDeviceType(deviceType).build();
        assertThat(config.deviceType).isEqualTo(deviceType);
    }

    @Test
    public void testNoDefDeviceType() {
        final AppMetricaConfig config = getDefaultConfigBuilder().build();
        assertThat(config.deviceType).isNull();
    }

    @Test
    public void testMaxReportsCount() {
        final AppMetricaConfig config = getDefaultConfigBuilder().withMaxReportCount(TestData
            .TEST_MAX_REPORTS_COUNT).build();
        assertThat(config.maxReportsCount).isEqualTo(TestData.TEST_MAX_REPORTS_COUNT);
    }

    @Test
    public void testNoDefMaxReportCount() {
        final AppMetricaConfig config = getDefaultConfigBuilder().build();
        assertThat(config.maxReportsCount).isNull();
    }

    @Test
    public void testNegativeMaxReportsCount() {
        final AppMetricaConfig config = getDefaultConfigBuilder().withMaxReportCount(TestData
            .TEST_NEGATIVE_MAX_REPORTS_COUNT).build();
        assertThat(config.maxReportsCount).isNegative();
    }

    @Test
    public void testMaxReportsInDbCount() {
        final AppMetricaConfig config = getDefaultConfigBuilder().withMaxReportsInDatabaseCount(TestData
            .TEST_MAX_REPORTS_IN_DB_COUNT).build();
        assertThat(config.maxReportsInDatabaseCount).isEqualTo(TestData.TEST_MAX_REPORTS_IN_DB_COUNT);
    }

    @Test
    public void testNoDefMaxReportInDbCount() {
        final AppMetricaConfig config = getDefaultConfigBuilder().build();
        assertThat(config.maxReportsInDatabaseCount).isNull();
    }

    @Test
    public void testDispatchPeriodSeconds() {
        final AppMetricaConfig config = getDefaultConfigBuilder().withDispatchPeriodSeconds(TestData
            .TEST_DISPATCH_PERIOD_SECONDS).build();
        assertThat(config.dispatchPeriodSeconds).isEqualTo(TestData.TEST_DISPATCH_PERIOD_SECONDS);
    }

    @Test
    public void testNoDefDispatchPeriodSeconds() {
        final AppMetricaConfig config = getDefaultConfigBuilder().build();
        assertThat(config.dispatchPeriodSeconds).isNull();
    }

    @Test
    public void testNegativeDispatchPerionSeconds() {
        final AppMetricaConfig config = getDefaultConfigBuilder().withDispatchPeriodSeconds(TestData
            .TEST_NEGATIVE_DISPATCH_PERIOD_SECONDS).build();
        assertThat(config.dispatchPeriodSeconds).isNegative();
    }

    @Test
    public void testConfigContainsAppEnvironment() {
        final int CAPACITY = 50;
        ArrayList<String> keys = new ArrayList<String>(CAPACITY);
        AppMetricaConfig.Builder config = AppMetricaConfig.newConfigBuilder(sApiKey);
        for (int i = 0; i < CAPACITY; i++) {
            String key = "key" + i + "salt";
            keys.add(key);
            config.withAppEnvironmentValue(key, "value" + i);
        }
        assertThat(config.build().appEnvironment.keySet()).containsExactlyInAnyOrderElementsOf(keys);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testAppEnvironmentUnmodifiable() {
        AppMetricaConfig.newConfigBuilder(sApiKey).build().appEnvironment.put("key", "value");
    }

    @Test
    public void testNoDefCrashTransformer() {
        assertThat(mDefaultConfig.crashTransformer).isNull();
    }

    @Test
    public void testCrashTransformer() {
        ICrashTransformer crashTransformer = mock(ICrashTransformer.class);
        AppMetricaConfig config = getDefaultConfigBuilder().withCrashTransformer(crashTransformer).build();
        assertThat(config.crashTransformer).isEqualTo(crashTransformer);
    }

    @Test
    public void testAnrMonitoring() {
        final AppMetricaConfig config = getDefaultConfigBuilder()
            .withAnrMonitoring(TestData.TEST_ANR_MONITORING).build();
        assertThat(config.anrMonitoring).isEqualTo(TestData.TEST_ANR_MONITORING);
    }

    @Test
    public void testAnrMonitoringTimeout() {
        final AppMetricaConfig config = getDefaultConfigBuilder()
            .withAnrMonitoringTimeout(TestData.TEST_ANR_MONITORING_TIMEOUT).build();
        assertThat(config.anrMonitoringTimeout).isEqualTo(TestData.TEST_ANR_MONITORING_TIMEOUT);
    }
}
