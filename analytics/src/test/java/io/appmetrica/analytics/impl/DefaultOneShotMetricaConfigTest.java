package io.appmetrica.analytics.impl;

import android.location.Location;
import androidx.annotation.NonNull;
import io.appmetrica.analytics.AppMetricaConfig;
import io.appmetrica.analytics.PredefinedDeviceTypes;
import io.appmetrica.analytics.PreloadInfo;
import io.appmetrica.analytics.TestData;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.LocationUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import static io.appmetrica.analytics.assertions.AssertionsKt.ObjectPropertyAssertions;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricTestRunner.class)
public class DefaultOneShotMetricaConfigTest extends CommonTest {

    private AppMetricaConfig.Builder mTestUserConfig;

    private ReportsHandler mReportsHandler = mock(ReportsHandler.class);

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mTestUserConfig = createTestUserConfig();
    }

    @Test
    public void mergeWithEmptyDefaultConfigNotChangeUserValuesTest() throws Exception {
        DefaultOneShotMetricaConfig emptyDefaultConfig = new DefaultOneShotMetricaConfig();

        AppMetricaConfig userConfig = mTestUserConfig.build();
        AppMetricaConfig merged = emptyDefaultConfig.mergeWithUserConfig(userConfig);

        assertMergedValuesEqualsForConfigs(userConfig, merged);
        assertUnmergedValuesEqualsForConfigs(userConfig, merged);
    }

    @Test
    public void mergeWithDefaultConfigNotOverrideUserValuesTest() throws Exception {
        DefaultOneShotMetricaConfig defaultOneShotMetricaConfig = createDefaultMetricaConfigWithValues();

        AppMetricaConfig userConfig = mTestUserConfig.build();
        AppMetricaConfig merged = defaultOneShotMetricaConfig.mergeWithUserConfig(userConfig);

        assertMergedValuesEqualsForConfigs(userConfig, merged);
        assertUnmergedValuesEqualsForConfigs(userConfig, merged);
    }

    private void assertMergedValuesEqualsForConfigs(final AppMetricaConfig userConfig,
                                                    final AppMetricaConfig merged)
        throws Exception {
        ObjectPropertyAssertions(merged)
            .withIgnoredFields("anrMonitoring", "anrMonitoringTimeout", "apiKey", "appBuildNumber", "clids", "crashTransformer",
                "customHosts", "deviceType", "dispatchPeriodSeconds", "distributionReferrer",
                "firstActivationAsUpdate", "logs", "permissionsCollection", "preloadInfo",
                "preloadInfoAutoTracking", "pulseConfig", "rtmConfig")
            .checkField("appVersion", userConfig.appVersion)
            .checkField("location", userConfig.location)
            .checkField("sessionTimeout", userConfig.sessionTimeout)
            .checkField("crashReporting", userConfig.crashReporting)
            .checkField("nativeCrashReporting", userConfig.nativeCrashReporting)
            .checkField("locationTracking", userConfig.locationTracking)
            .checkField("advIdentifiersTracking", userConfig.advIdentifiersTracking)
            .checkField("dataSendingEnabled", userConfig.dataSendingEnabled)
            .checkField("maxReportsCount", userConfig.maxReportsCount)
            .checkField("maxReportsInDatabaseCount", userConfig.maxReportsInDatabaseCount)
            .checkField("appEnvironment", userConfig.appEnvironment)
            .checkField("errorEnvironment", userConfig.errorEnvironment)
            .checkField("userProfileID", userConfig.userProfileID)
            .checkField("sessionsAutoTrackingEnabled", userConfig.sessionsAutoTrackingEnabled)
            .checkField("revenueAutoTrackingEnabled", userConfig.revenueAutoTrackingEnabled)
            .checkField("appOpenTrackingEnabled", userConfig.appOpenTrackingEnabled)
            .checkField("additionalConfig", userConfig.additionalConfig)
            .checkAll();
    }

    @Test
    public void mergeAddNotProvidedByUserValuesTest() throws Exception {
        DefaultOneShotMetricaConfig defaultOneShotMetricaConfig = createDefaultMetricaConfigWithValues();
        defaultOneShotMetricaConfig.putAppEnvironmentValue("a", "1");
        defaultOneShotMetricaConfig.putErrorEnvironmentValue("e", "1");

        Location location = defaultOneShotMetricaConfig.getLocation();
        Boolean trackLocationEnabled = defaultOneShotMetricaConfig.isLocationTrackingEnabled();
        Boolean advIdentifierTrackingEnabled = defaultOneShotMetricaConfig.isAdvIdentifiersTrackingEnabled();

        AppMetricaConfig userConfig = AppMetricaConfig.newConfigBuilder(TestsData.UUID_API_KEY)
            .withLogs()
            .withPreloadInfo(PreloadInfo.newBuilder("test").build())
            .withAppBuildNumber(11)
            .withDispatchPeriodSeconds(111)
            .withMaxReportsCount(77)
            .withDeviceType(PredefinedDeviceTypes.TABLET)
            .withAdditionalConfig("YMM_clids", Collections.EMPTY_MAP)
            .withAdditionalConfig("YMM_preloadInfoAutoTracking", false)
            .withAdditionalConfig("YMM_preloadInfoAutoTracking", false)
            .withAdditionalConfig("YMM_customHosts", Collections.singletonList(TestData.TEST_CUSTOM_HOST_URL))
            .withUserProfileID("user_profile_id")
            .withRevenueAutoTrackingEnabled(false)
            .build();

        AppMetricaConfig merged = defaultOneShotMetricaConfig.mergeWithUserConfig(userConfig);

        assertThat(merged.location).isEqualTo(location);
        assertThat(merged.locationTracking).isEqualTo(trackLocationEnabled);
        assertThat(merged.advIdentifiersTracking).isEqualTo(advIdentifierTrackingEnabled);
        assertThat(merged.appEnvironment.size()).isEqualTo(1);
        assertThat(merged.appEnvironment.containsKey("a")).isTrue();
        assertThat(merged.errorEnvironment.size()).isEqualTo(1);
        assertThat(merged.errorEnvironment.containsKey("e")).isTrue();

        assertUnmergedValuesEqualsForConfigs(userConfig, merged);
    }

    @Test
    public void appEnvironmentInDefaultConfigMergedWithUserConfigTest() throws Exception {
        DefaultOneShotMetricaConfig defaultOneShotMetricaConfig = new DefaultOneShotMetricaConfig();
        defaultOneShotMetricaConfig.putAppEnvironmentValue("a", "1");

        AppMetricaConfig userConfig = AppMetricaConfig.newConfigBuilder(TestsData.UUID_API_KEY)
            .withAppEnvironmentValue("b", "2").build();
        AppMetricaConfig merged = defaultOneShotMetricaConfig.mergeWithUserConfig(userConfig);
        Map<String, String> appEnvironment = merged.appEnvironment;

        assertThat(appEnvironment.size()).isEqualTo(2);
        assertThat(appEnvironment.containsKey("b")).isTrue();
        assertThat(appEnvironment.containsKey("a")).isTrue();
        assertThat(appEnvironment.get("b")).isEqualTo("2");
        assertThat(appEnvironment.get("a")).isEqualTo("1");
    }

    @Test
    public void appEnvironmentClearedTest() throws Exception {
        DefaultOneShotMetricaConfig defaultOneShotMetricaConfig = new DefaultOneShotMetricaConfig();
        defaultOneShotMetricaConfig.putAppEnvironmentValue("a", "1");
        defaultOneShotMetricaConfig.putAppEnvironmentValue("b", "2");
        defaultOneShotMetricaConfig.clearAppEnvironment();

        AppMetricaConfig userConfig = AppMetricaConfig.newConfigBuilder(TestsData.UUID_API_KEY).build();
        AppMetricaConfig merged = defaultOneShotMetricaConfig.mergeWithUserConfig(userConfig);

        assertThat(merged.appEnvironment.size()).isEqualTo(0);
    }

    @Test
    public void appEnvironmentAddedAfterClearedTest() throws Exception {
        DefaultOneShotMetricaConfig defaultOneShotMetricaConfig = new DefaultOneShotMetricaConfig();
        defaultOneShotMetricaConfig.putAppEnvironmentValue("a", "1");
        defaultOneShotMetricaConfig.clearAppEnvironment();
        defaultOneShotMetricaConfig.putAppEnvironmentValue("b", "2");

        AppMetricaConfig userConfig = AppMetricaConfig.newConfigBuilder(TestsData.UUID_API_KEY).build();
        AppMetricaConfig merged = defaultOneShotMetricaConfig.mergeWithUserConfig(userConfig);

        assertThat(merged.appEnvironment.size()).isEqualTo(1);
        assertThat(merged.appEnvironment).containsKey("b");
    }

    @Test
    public void errorEnvironmentInDefaultConfigMergedWithUserConfigTest() throws Exception {
        DefaultOneShotMetricaConfig defaultOneShotMetricaConfig = new DefaultOneShotMetricaConfig();
        defaultOneShotMetricaConfig.putErrorEnvironmentValue("a", "1");

        AppMetricaConfig userConfig = AppMetricaConfig.newConfigBuilder(TestsData.UUID_API_KEY)
            .withErrorEnvironmentValue("b", "2").build();
        AppMetricaConfig merged = defaultOneShotMetricaConfig.mergeWithUserConfig(userConfig);
        Map<String, String> errorEnvironment = merged.errorEnvironment;

        assertThat(errorEnvironment.size()).isEqualTo(2);
        assertThat(errorEnvironment.containsKey("b")).isTrue();
        assertThat(errorEnvironment.containsKey("a")).isTrue();
        assertThat(errorEnvironment.get("b")).isEqualTo("2");
        assertThat(errorEnvironment.get("a")).isEqualTo("1");
    }

    @Test
    public void appAndErrorEnvironmentAddedWithoutMessingTest() throws Exception {
        DefaultOneShotMetricaConfig defaultOneShotMetricaConfig = new DefaultOneShotMetricaConfig();
        defaultOneShotMetricaConfig.putAppEnvironmentValue("a", "1");
        defaultOneShotMetricaConfig.putErrorEnvironmentValue("b", "2");
        defaultOneShotMetricaConfig.clearAppEnvironment();
        defaultOneShotMetricaConfig.putAppEnvironmentValue("c", "3");

        AppMetricaConfig userConfig = AppMetricaConfig.newConfigBuilder(TestsData.UUID_API_KEY).build();
        AppMetricaConfig merged = defaultOneShotMetricaConfig.mergeWithUserConfig(userConfig);

        assertThat(merged.appEnvironment.size()).isEqualTo(1);
        assertThat(merged.appEnvironment).containsKey("c");
        assertThat(merged.errorEnvironment.size()).isEqualTo(1);
        assertThat(merged.errorEnvironment).containsKey("b");
    }

    @Test
    public void testOneShotStrategy() {
        DefaultOneShotMetricaConfig config = createDefaultMetricaConfigWithValues();
        config.clearAppEnvironment();
        final AppMetricaConfig userConfig = AppMetricaConfig.newConfigBuilder(TestsData.UUID_API_KEY).build();
        assertThat(config.mergeWithUserConfig(userConfig)).isNotSameAs(userConfig);
        assertThat(config.wasAppEnvironmentCleared()).isFalse();
        assertThat(config.mergeWithUserConfig(userConfig)).isSameAs(userConfig);
    }

    private void assertUnmergedValuesEqualsForConfigs(final AppMetricaConfig userConfig, final AppMetricaConfig merged) {
        assertThat(merged.appBuildNumber).isEqualTo(userConfig.appBuildNumber);
        assertThat(merged.deviceType).isEqualTo(userConfig.deviceType);
        assertThat(merged.dispatchPeriodSeconds).isEqualTo(userConfig.dispatchPeriodSeconds);
        assertThat(merged.maxReportsCount).isEqualTo(userConfig.maxReportsCount);
        assertThat(merged.apiKey).isEqualTo(userConfig.apiKey);
        assertThat(merged.preloadInfo).isEqualTo(userConfig.preloadInfo);
        assertThat(merged.logs).isEqualTo(userConfig.logs);
        assertThat(merged.userProfileID).isEqualTo(userConfig.userProfileID);
        assertThat(merged.revenueAutoTrackingEnabled).isEqualTo(userConfig.revenueAutoTrackingEnabled);
        assertThat(merged.appOpenTrackingEnabled).isEqualTo(userConfig.appOpenTrackingEnabled);
        assertThat(merged.additionalConfig).containsExactlyInAnyOrderEntriesOf(userConfig.additionalConfig);
    }

    @NonNull
    private DefaultOneShotMetricaConfig createDefaultMetricaConfigWithValues() {
        DefaultOneShotMetricaConfig defaultOneShotMetricaConfig = new DefaultOneShotMetricaConfig();

        defaultOneShotMetricaConfig.setLocation(LocationUtils.INSTANCE.createFakeLocation(1.0, 2.0));
        defaultOneShotMetricaConfig.setLocationTracking(false);
        defaultOneShotMetricaConfig.setAdvIdentifiersTracking(false, true);
        defaultOneShotMetricaConfig.setUserProfileID("User profile ID");
        return defaultOneShotMetricaConfig;
    }

    @NonNull
    private AppMetricaConfig.Builder createTestUserConfig() {
        return AppMetricaConfig.newConfigBuilder(TestsData.UUID_API_KEY)
            .withLogs()
            .withPreloadInfo(PreloadInfo.newBuilder("test").build())
            .withAppBuildNumber(11)
            .withAppVersion("1.1.1")
            .withDispatchPeriodSeconds(111)
            .withMaxReportsCount(77)
            .withDeviceType(PredefinedDeviceTypes.TABLET)
            .withAdditionalConfig("YMM_clids", Collections.EMPTY_MAP)
            .withAdditionalConfig("YMM_preloadInfoAutoTracking", false)
            .withAdditionalConfig("YMM_customHosts", Collections.singletonList(TestData.TEST_CUSTOM_HOST_URL))
            .withLocationTracking(true)
            .withAdvIdentifiersTracking(true)
            .withSessionTimeout(111)
            .withAppEnvironmentValue("a", "1")
            .withErrorEnvironmentValue("error", "1")
            .withNativeCrashReporting(true)
            .withCrashReporting(true)
            .withLocation(TestData.TEST_LOCATION)
            .withAdditionalConfig("YMM_customHosts", Collections.singletonList(TestData.TEST_CUSTOM_HOST_URL))
            .withAnrMonitoring(true)
            .withAnrMonitoringTimeout(42)
            .withDataSendingEnabled(true)
            .withMaxReportsInDatabaseCount(500)
            .withUserProfileID("user_profile_id")
            .withSessionsAutoTrackingEnabled(true)
            .withRevenueAutoTrackingEnabled(false)
            .withAppOpenTrackingEnabled(false);
    }

    @Test
    public void testHandleFirstActivationAsUpdateMerged() throws Exception {
        String apiKey = UUID.randomUUID().toString();
        AppMetricaConfig withUpdate = AppMetricaConfig.newConfigBuilder(apiKey)
            .handleFirstActivationAsUpdate(true).build();

        DefaultOneShotMetricaConfig defaultOneShotMetricaConfig = new DefaultOneShotMetricaConfig();

        assertThat(defaultOneShotMetricaConfig.mergeWithUserConfig(withUpdate).firstActivationAsUpdate).isTrue();

        withUpdate = AppMetricaConfig.newConfigBuilder(apiKey)
            .handleFirstActivationAsUpdate(false).build();

        assertThat(defaultOneShotMetricaConfig.mergeWithUserConfig(withUpdate).firstActivationAsUpdate).isFalse();
    }

    @Test
    public void testDefaultConfigContainsAppEnvironmentInAddingOrder() {
        final int CAPACITY = 50;
        ArrayList<String> keys = new ArrayList<String>(CAPACITY);
        DefaultOneShotMetricaConfig config = new DefaultOneShotMetricaConfig();
        for (int i = 0; i < CAPACITY; i++) {
            String key = "key" + i + "salt";
            keys.add(key);
            config.putAppEnvironmentValue(key, "value" + i);
        }
        assertThat(config.getAppEnvironment().keySet()).containsExactlyElementsOf(keys);
    }

    @Test
    public void testDefaultConfigContainsErrorEnvironmentInAddingOrder() {
        final int CAPACITY = 50;
        ArrayList<String> keys = new ArrayList<String>(CAPACITY);
        DefaultOneShotMetricaConfig config = new DefaultOneShotMetricaConfig();
        for (int i = 0; i < CAPACITY; i++) {
            String key = "key" + i + "salt";
            keys.add(key);
            config.putErrorEnvironmentValue(key, "value" + i);
        }
        assertThat(config.getErrorEnvironment().keySet()).containsExactlyElementsOf(keys);
    }

    @Test
    public void testConfigContainsAppEnvironmentInAddingOrderAfterMerge() {
        final int BEFORE_ACTIVATION = 50;
        final int AFTER_ACTIVATION = 50;
        ArrayList<String> keys = new ArrayList<String>(BEFORE_ACTIVATION + AFTER_ACTIVATION);
        DefaultOneShotMetricaConfig defaultConfig = new DefaultOneShotMetricaConfig();
        for (int i = 0; i < BEFORE_ACTIVATION; i++) {
            String key = "key" + i + "salt";
            keys.add(key);
            defaultConfig.putAppEnvironmentValue(key, "value" + i);
        }
        AppMetricaConfig.Builder config = AppMetricaConfig.newConfigBuilder(UUID.randomUUID().toString());
        for (int i = 0; i < BEFORE_ACTIVATION; i++) {
            String key = "key" + i + "in config with salt";
            keys.add(key);
            config.withAppEnvironmentValue(key, "value" + i);
        }
        assertThat(defaultConfig.mergeWithUserConfig(config.build()).appEnvironment.keySet()).containsExactlyInAnyOrderElementsOf(keys);
    }

    @Test
    public void testConfigContainsErrorEnvironmentInAddingOrderAfterMerge() {
        final int BEFORE_ACTIVATION = 50;
        final int AFTER_ACTIVATION = 50;
        ArrayList<String> keys = new ArrayList<String>(BEFORE_ACTIVATION + AFTER_ACTIVATION);
        DefaultOneShotMetricaConfig defaultConfig = new DefaultOneShotMetricaConfig();
        for (int i = 0; i < BEFORE_ACTIVATION; i++) {
            String key = "key" + i + "salt";
            keys.add(key);
            defaultConfig.putErrorEnvironmentValue(key, "value" + i);
        }
        AppMetricaConfig.Builder config = AppMetricaConfig.newConfigBuilder(UUID.randomUUID().toString());
        for (int i = 0; i < BEFORE_ACTIVATION; i++) {
            String key = "key" + i + "in config with salt";
            keys.add(key);
            config.withErrorEnvironmentValue(key, "value" + i);
        }
        assertThat(defaultConfig.mergeWithUserConfig(config.build()).errorEnvironment.keySet()).containsExactlyElementsOf(keys);
    }

    @Test
    public void setReportsHandler() {
        DefaultOneShotMetricaConfig config = new DefaultOneShotMetricaConfig();
        config.setAdvIdentifiersTracking(true, true);
        config.setLocationTracking(false);
        config.setDataSendingEnabled(false);
        config.setReportsHandler(mReportsHandler);
        verify(mReportsHandler).updatePreActivationConfig(false, false, true, true);
    }

    @Test
    public void testProxyLocationTracking() {
        DefaultOneShotMetricaConfig config = new DefaultOneShotMetricaConfig();
        config.setReportsHandler(mReportsHandler);
        config.setLocationTracking(true);
        verify(mReportsHandler).updatePreActivationConfig(true, null, null, false);
    }

    @Test
    public void proxyDataSendingEnabled() {
        DefaultOneShotMetricaConfig config = new DefaultOneShotMetricaConfig();
        config.setReportsHandler(mReportsHandler);
        config.setDataSendingEnabled(true);
        verify(mReportsHandler).updatePreActivationConfig(null, true, null, false);
    }

    @Test
    public void proxyAdvIdentifiersTracking() {
        DefaultOneShotMetricaConfig config = new DefaultOneShotMetricaConfig();
        config.setReportsHandler(mReportsHandler);
        config.setAdvIdentifiersTracking(true, true);
        verify(mReportsHandler).updatePreActivationConfig(null, null, true, true);
    }

    @Test
    public void addAutoCollectedDataSubscriber() {
        String first = "first";
        String second = "second";
        DefaultOneShotMetricaConfig config = new DefaultOneShotMetricaConfig();
        config.addAutoCollectedDataSubscriber(first);
        config.addAutoCollectedDataSubscriber(second);
        assertThat(config.autoCollectedDataSubscribers).containsExactly(first, second);
        assertThat(config.configExtension().getAutoCollectedDataSubscribers()).containsExactly(first, second);
    }

    @Test
    public void configExtension() {
        DefaultOneShotMetricaConfig config = new DefaultOneShotMetricaConfig();
        String subscriber = "subscriber";
        config.addAutoCollectedDataSubscriber(subscriber);
        config.clearAppEnvironment();
        ObjectPropertyAssertions(config.configExtension())
            .checkField("autoCollectedDataSubscribers", Collections.singletonList(subscriber))
            .checkField("needClearEnvironment", true)
            .checkAll();
    }

    @Test
    public void configExtensionForDefault() {
        ObjectPropertyAssertions(new DefaultOneShotMetricaConfig().configExtension())
            .checkField("autoCollectedDataSubscribers", new ArrayList<String>())
            .checkField("needClearEnvironment", false)
            .checkAll();
    }
}
