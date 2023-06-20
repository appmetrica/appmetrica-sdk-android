package io.appmetrica.analytics.impl;

import android.location.Location;
import io.appmetrica.analytics.AppMetricaConfig;
import io.appmetrica.analytics.ICrashTransformer;
import io.appmetrica.analytics.PredefinedDeviceTypes;
import io.appmetrica.analytics.PreloadInfo;
import io.appmetrica.analytics.assertions.ObjectPropertyAssertions;
import io.appmetrica.analytics.testutils.CommonTest;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import static io.appmetrica.analytics.assertions.AssertionsKt.ObjectPropertyAssertions;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(RobolectricTestRunner.class)
public class ClientConfigSerializerTest extends CommonTest {

    private final String mApiKey = "5012c3cc-20a4-4dac-92d1-83ebc27c0fa9";
    private final ClientConfigSerializer mClientConfigSerializer = new ClientConfigSerializer();

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
    @Mock
    private ICrashTransformer crashTransformer;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void filledConfig() throws Exception {
        Random random = new Random();
        final String errorEnvKeyFirst = "key1";
        final String errorEnvValueFirst = "value1";
        final String errorEnvKeySecond = "key2";
        final String errorEnvValueSecond = "value2";
        final Map<String, String> errorEnvironmentMap = new HashMap<String, String>();
        errorEnvironmentMap.put(errorEnvKeyFirst, errorEnvValueFirst);
        errorEnvironmentMap.put(errorEnvKeySecond, errorEnvValueSecond);
        final boolean handleFirstActivationAsUpdate = random.nextBoolean();
        final String appVersion = "1.3.6";
        final boolean crashReporting = random.nextBoolean();
        final Location location = new Location("gps");
        location.setLatitude(random.nextDouble());
        location.setLongitude(random.nextDouble());
        final boolean locationTracking = random.nextBoolean();
        final int maxReportsInDbCount = 800;
        final boolean nativeCrashReporting = random.nextBoolean();
        final PreloadInfo preloadInfo = PreloadInfo.newBuilder("888999").setAdditionalParams("key", "value").build();
        final int sessionTimeout = 23;
        final boolean statisticsSending = random.nextBoolean();
        String userProfileID = "user_profile_id";
        final boolean revenueAutoTracking = false;
        final boolean sessionsAutoTracking = false;
        final boolean appOpenAutoTracking = false;
        final boolean anrMonitoring = false;
        final int anrMonitoringTimeout = 42;
        final String additionalConfigKeyFirst = "key1";
        final String additionalConfigValueFirst = "value1";
        final String additionalConfigKeySecond = "key2";
        final String additionalConfigValueSecond = "value2";
        final Map<String, String> additionalConfigMap = new HashMap<String, String>() {{
            put(additionalConfigKeyFirst, additionalConfigValueFirst);
            put(additionalConfigKeySecond, additionalConfigValueSecond);
        }};
        final List<String> customHosts = Arrays.asList("customHost1", "customHost2");
        AppMetricaConfig config = AppMetricaConfig.newConfigBuilder(mApiKey)
            .withErrorEnvironmentValue(errorEnvKeyFirst, errorEnvValueFirst)
            .withErrorEnvironmentValue(errorEnvKeySecond, errorEnvValueSecond)
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
            .withUserProfileID(userProfileID)
            .withRevenueAutoTrackingEnabled(revenueAutoTracking)
            .withSessionsAutoTrackingEnabled(sessionsAutoTracking)
            .withAppOpenTrackingEnabled(appOpenAutoTracking)
            .withDeviceType(deviceType)
            .withAppBuildNumber(appBuildNumber)
            .withDispatchPeriodSeconds(dispatchPeriodSeconds)
            .withMaxReportCount(maxReportsCount)
            .withAppEnvironmentValue(appEnvKeyFirst, appEnvValueFirst)
            .withAppEnvironmentValue(appEnvKeySecond, appEnvValueSecond)
            .withAnrMonitoring(anrMonitoring)
            .withAnrMonitoringTimeout(anrMonitoringTimeout)
            .withCustomHosts(customHosts)
            .withAdditionalConfig(additionalConfigKeyFirst, additionalConfigValueFirst)
            .withAdditionalConfig(additionalConfigKeySecond, additionalConfigValueSecond)
            .build();
        String json = mClientConfigSerializer.toJson(config);
        AppMetricaConfig deserialized = mClientConfigSerializer.fromJson(json);
        ObjectPropertyAssertions<AppMetricaConfig> assertions = ObjectPropertyAssertions(deserialized)
            .withIgnoredFields("location", "preloadInfo", "crashTransformer");
        assertions.checkField("apiKey", mApiKey);
        assertions.checkField("appVersion", appVersion);
        assertions.checkField("sessionTimeout", sessionTimeout);
        assertions.checkField("crashReporting", crashReporting);
        assertions.checkField("nativeCrashReporting", nativeCrashReporting);
        assertions.checkField("locationTracking", locationTracking);
        assertions.checkField("logs", true);
        assertions.checkField("firstActivationAsUpdate", handleFirstActivationAsUpdate);
        assertions.checkField("statisticsSending", statisticsSending);
        assertions.checkField("maxReportsInDatabaseCount", maxReportsInDbCount);
        assertions.checkField("errorEnvironment", errorEnvironmentMap);
        assertions.checkField("userProfileID", userProfileID);
        assertions.checkField("revenueAutoTrackingEnabled", revenueAutoTracking);
        assertions.checkField("sessionsAutoTrackingEnabled", sessionsAutoTracking);
        assertions.checkField("appOpenTrackingEnabled", appOpenAutoTracking);
        assertions.checkField("deviceType", deviceType);
        assertions.checkField("appBuildNumber", appBuildNumber);
        assertions.checkField("dispatchPeriodSeconds", dispatchPeriodSeconds);
        assertions.checkField("maxReportsCount", maxReportsCount);
        assertions.checkField("appEnvironment", appEnvironmentMap);
        assertions.checkField("anrMonitoring", anrMonitoring);
        assertions.checkField("anrMonitoringTimeout", anrMonitoringTimeout);
        assertions.checkField("customHosts", customHosts);
        assertions.checkField("additionalConfig", additionalConfigMap);
        assertions.checkAll();
        assertThat(deserialized.location).isEqualToComparingOnlyGivenFields(location,
            "provider", "latitude", "longitude", "time", "accuracy", "altitude");
        assertThat(deserialized.preloadInfo).isEqualToComparingFieldByField(preloadInfo);
    }

    @Test
    public void emptyConfig() throws Exception {
        AppMetricaConfig emptyConfig = AppMetricaConfig.newConfigBuilder(mApiKey).build();
        String json = mClientConfigSerializer.toJson(emptyConfig);
        AppMetricaConfig deserialized = mClientConfigSerializer.fromJson(json);

        final String nullString = null;
        final Boolean nullBoolean = null;
        final Integer nullInt = null;
        ObjectPropertyAssertions<AppMetricaConfig> assertions = ObjectPropertyAssertions(deserialized);
        assertions.checkField("apiKey", mApiKey);
        assertions.checkField("appVersion", nullString);
        assertions.checkField("sessionTimeout", nullInt);
        assertions.checkField("crashReporting", nullBoolean);
        assertions.checkField("nativeCrashReporting", nullBoolean);
        assertions.checkField("locationTracking", nullBoolean);
        assertions.checkField("logs", nullBoolean);
        assertions.checkField("firstActivationAsUpdate", nullBoolean);
        assertions.checkField("statisticsSending", nullBoolean);
        assertions.checkField("maxReportsInDatabaseCount", nullInt);
        assertions.checkField("errorEnvironment", Collections.emptyMap());
        assertions.checkField("location", (Location) null);
        assertions.checkField("preloadInfo", (PreloadInfo) null);
        assertions.checkField("userProfileID", null);
        assertions.checkField("deviceType", nullString);
        assertions.checkField("appBuildNumber", nullInt);
        assertions.checkField("dispatchPeriodSeconds", nullInt);
        assertions.checkField("maxReportsCount", nullInt);
        assertions.checkField("appEnvironment", Collections.emptyMap());
        assertions.checkField("crashTransformer", (ICrashTransformer) null);
        assertions.checkFieldIsNull("revenueAutoTrackingEnabled");
        assertions.checkFieldIsNull("sessionsAutoTrackingEnabled");
        assertions.checkFieldIsNull("appOpenTrackingEnabled");
        assertions.checkField("anrMonitoring", nullBoolean);
        assertions.checkField("anrMonitoringTimeout", nullInt);
        assertions.checkField("customHosts", (List<String>) null);
        assertions.checkField("additionalConfig", Collections.emptyMap());
        assertions.checkAll();

    }
}
