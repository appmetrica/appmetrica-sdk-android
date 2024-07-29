package io.appmetrica.analytics

import android.location.Location
import io.appmetrica.analytics.assertions.ObjectPropertyAssertions
import io.appmetrica.analytics.impl.VerificationConstants
import io.appmetrica.analytics.impl.proxy.validation.ConfigChecker
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.constructionRule
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.robolectric.RobolectricTestRunner
import java.util.AbstractMap
import java.util.Random
import java.util.UUID

@RunWith(RobolectricTestRunner::class)
class AppMetricaConfigTest : CommonTest() {

    private val apiKey = UUID.randomUUID().toString()
    private val errorEnvKeyFirst = "key1"
    private val errorEnvValueFirst = "value1"
    private val errorEnvKeySecond = "key2"
    private val errorEnvValueSecond = "value2"
    private val errorEnvironmentMap = mapOf(
        errorEnvKeyFirst to errorEnvValueFirst,
        errorEnvKeySecond to errorEnvValueSecond
    )
    private val random = Random()
    private val handleFirstActivationAsUpdate = random.nextBoolean()
    private val appVersion = "1.3.6"
    private val crashReporting = random.nextBoolean()
    private val location = mock<Location>()
    private val locationTracking = random.nextBoolean()
    private val maxReportsInDbCount = 800
    private val nativeCrashReporting = random.nextBoolean()
    private val sessionTimeout = 23
    private val dataSendingEnabled = random.nextBoolean()
    private val revenueAutoTrackingEnabled = random.nextBoolean()
    private val sessionsAutoTrackingEnabled = random.nextBoolean()
    private val appOpenTrackingEnabled = random.nextBoolean()
    private val userProfileID = "user_profile_id"
    private val oldMaxReportsInDatabaseCount = 10
    private val newMaxReportsInDatabaseCount = 100

    @get:Rule
    val configCheckerMockedConstructionRule = constructionRule<ConfigChecker> {
        on { getCheckedMaxReportsInDatabaseCount(any()) } doAnswer { invocation -> invocation.arguments.first() as Int }
        on { getCheckedMaxReportsInDatabaseCount(oldMaxReportsInDatabaseCount) } doReturn newMaxReportsInDatabaseCount
    }

    private val deviceType = PredefinedDeviceTypes.PHONE
    private val appBuildNumber = 42
    private val dispatchPeriodSeconds = 125
    private val maxReportsCount = 16433
    private val appEnvKeyFirst = "key1"
    private val appEnvValueFirst = "value1"
    private val appEnvKeySecond = "key2"
    private val appEnvValueSecond = "value2"

    private val appEnvironmentMap = mapOf(
        appEnvKeyFirst to appEnvValueFirst,
        appEnvKeySecond to appEnvValueSecond
    )

    private val anrMonitoring = random.nextBoolean()
    private val anrMonitoringTimeout = random.nextInt()
    private val customHosts = listOf("customHost1", "customHost2")
    private val additionalConfigKeyFirst = "key1"
    private val additionalConfigValueFirst = "value1"
    private val additionalConfigKeySecond = "key2"
    private val additionalConfigValueSecond = "value2"
    private val additionalConfigMap = mapOf(
        additionalConfigKeyFirst to additionalConfigValueFirst,
        additionalConfigKeySecond to additionalConfigValueSecond
    )

    private val defaultConfigBuilder = AppMetricaConfig.newConfigBuilder(apiKey)
    private val defaultConfig: AppMetricaConfig = defaultConfigBuilder.build()

    private val preloadInfo: PreloadInfo = mock()
    private val crashTransformer: ICrashTransformer = mock()

    @Test
    fun createConfigFromConfig() {
        ObjectPropertyAssertions(AppMetricaConfig(createFilledConfig()))
            .checkField("apiKey", apiKey)
            .checkField("appVersion", appVersion)
            .checkField("sessionTimeout", sessionTimeout)
            .checkField("crashReporting", crashReporting)
            .checkField("nativeCrashReporting", nativeCrashReporting)
            .checkField("location", location)
            .checkField("locationTracking", locationTracking)
            .checkField("logs", true)
            .checkField("preloadInfo", preloadInfo)
            .checkField("firstActivationAsUpdate", handleFirstActivationAsUpdate)
            .checkField("dataSendingEnabled", dataSendingEnabled)
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
            .checkAll()
    }

    @Test
    fun jsonSerialization() {
        assertThat(
            AppMetricaConfig.fromJson(AppMetricaConfig.newConfigBuilder(apiKey).build().toJson())?.apiKey
        ).isEqualTo(apiKey)
    }

    @Test
    fun createConfigFromEmptyConfig() {
        ObjectPropertyAssertions(
            AppMetricaConfig(AppMetricaConfig.newConfigBuilder(apiKey).build())
        )
            .checkField("apiKey", apiKey)
            .checkField("errorEnvironment", HashMap<String, String>())
            .checkField("appEnvironment", HashMap<String, String>())
            .checkField("additionalConfig", HashMap<String, String>())
            .checkFieldsAreNull(
                "appVersion", "sessionTimeout", "crashReporting", "nativeCrashReporting", "location",
                "locationTracking", "logs", "preloadInfo", "firstActivationAsUpdate", "dataSendingEnabled",
                "maxReportsInDatabaseCount", "userProfileID", "revenueAutoTrackingEnabled",
                "sessionsAutoTrackingEnabled", "appOpenTrackingEnabled", "deviceType", "appBuildNumber",
                "dispatchPeriodSeconds", "maxReportsCount", "crashTransformer", "anrMonitoring",
                "anrMonitoringTimeout", "customHosts"
            )
            .checkAll()
    }
    private fun createFilledConfig(): AppMetricaConfig = AppMetricaConfig.newConfigBuilder(apiKey)
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
            .withDataSendingEnabled(dataSendingEnabled)
            .withErrorEnvironmentValue(errorEnvKeyFirst, errorEnvValueFirst)
            .withErrorEnvironmentValue(errorEnvKeySecond, errorEnvValueSecond)
            .withUserProfileID(userProfileID)
            .withRevenueAutoTrackingEnabled(revenueAutoTrackingEnabled)
            .withSessionsAutoTrackingEnabled(sessionsAutoTrackingEnabled)
            .withAppOpenTrackingEnabled(appOpenTrackingEnabled)
            .withDeviceType(deviceType)
            .withAppBuildNumber(appBuildNumber)
            .withDispatchPeriodSeconds(dispatchPeriodSeconds)
            .withMaxReportsCount(maxReportsCount)
            .withAppEnvironmentValue(appEnvKeyFirst, appEnvValueFirst)
            .withAppEnvironmentValue(appEnvKeySecond, appEnvValueSecond)
            .withCrashTransformer(crashTransformer)
            .withAnrMonitoring(anrMonitoring)
            .withAnrMonitoringTimeout(anrMonitoringTimeout)
            .withCustomHosts(customHosts)
            .withAdditionalConfig(additionalConfigKeyFirst, additionalConfigValueFirst)
            .withAdditionalConfig(additionalConfigKeySecond, additionalConfigValueSecond)
            .build()

    @Test
    fun apiKey() {
        assertThat(defaultConfig.apiKey).isEqualToIgnoringCase(apiKey)
    }

    @Test
    fun emptyApiKey() {
        assertThatThrownBy { AppMetricaConfig.newConfigBuilder("").build() }
            .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun appVersion() {
        val config = defaultConfigBuilder
            .withAppVersion(TestData.TEST_APP_VERSION).build()
        assertThat(config.appVersion).isEqualToIgnoringCase(TestData.TEST_APP_VERSION)
    }

    @Test
    fun noDefAppVersion() {
        assertThat(defaultConfig.appVersion).isNull()
    }

    @Test
    fun sessionTimeout() {
        val config = defaultConfigBuilder.withSessionTimeout(TestData.TEST_SESSION_TIMEOUT).build()
        assertThat(config.sessionTimeout).isEqualTo(TestData.TEST_SESSION_TIMEOUT)
    }

    @Test
    fun reportCrashEnabled() {
        val config = defaultConfigBuilder.withCrashReporting(TestData.TEST_REPORT_CRASHES_ENABLED).build()
        assertThat(config.crashReporting).isEqualTo(TestData.TEST_REPORT_CRASHES_ENABLED)
    }

    @Test
    fun noDefReportCrashEnabled() {
        assertThat(defaultConfig.crashReporting).isNull()
    }

    @Test
    fun reportNativeCrashEnabled() {
        val config = defaultConfigBuilder.withNativeCrashReporting(TestData.TEST_REPORT_NATIVE_CRASHES_ENABLED).build()
        assertThat(config.nativeCrashReporting).isEqualTo(TestData.TEST_REPORT_NATIVE_CRASHES_ENABLED)
    }

    @Test
    fun noDefReportsNativeCrashEnabled() {
        assertThat(defaultConfig.nativeCrashReporting).isNull()
    }

    @Test
    fun testLocation() {
        val config = defaultConfigBuilder.withLocation(TestData.TEST_LOCATION).build()
        assertThat(config.location).isEqualTo(TestData.TEST_LOCATION)
    }

    @Test
    fun noDefLocation() {
        assertThat(defaultConfig.location).isEqualTo(null)
    }

    @Test
    fun trackLocationEnabled() {
        val config = defaultConfigBuilder.withLocationTracking(TestData.TEST_TRACK_LOCATION_ENABLED).build()
        assertThat(config.locationTracking).isEqualTo(TestData.TEST_TRACK_LOCATION_ENABLED)
    }

    @Test
    fun noDefTrackLocationEnabled() {
        assertThat(defaultConfig.locationTracking).isNull()
    }

    @Test
    fun preloadInfo() {
        val preloadInfo = PreloadInfo.newBuilder("test_tracking_id")
            .setAdditionalParams("test_key", "test_value")
            .build()
        val config = defaultConfigBuilder.withPreloadInfo(preloadInfo).build()
        assertThat(config.preloadInfo).isEqualTo(preloadInfo)
    }

    @Test
    fun noDefPreloadInfo() {
        assertThat(defaultConfig.preloadInfo).isNull()
    }

    @Test
    fun handleFirstActivationAsUpdate() {
        var config = defaultConfigBuilder.handleFirstActivationAsUpdate(true).build()
        assertThat(config.firstActivationAsUpdate).isTrue()
        config = defaultConfigBuilder.handleFirstActivationAsUpdate(false).build()
        assertThat(config.firstActivationAsUpdate).isFalse()
    }

    @Test
    fun noErrorEnvironment() {
        val config = defaultConfigBuilder.build()
        assertThat(config.errorEnvironment).isNotNull.isEmpty()
    }

    @Test
    fun hasErrorEnvironment() {
        val key1 = "key1"
        val value1 = "value1"
        val key2 = "key2"
        val value2 = "value2"
        val config = defaultConfigBuilder
            .withErrorEnvironmentValue(key1, value1)
            .withErrorEnvironmentValue(key2, value2)
            .build()
        assertThat(config.errorEnvironment).containsExactly(
            AbstractMap.SimpleEntry(key1, value1),
            AbstractMap.SimpleEntry(key2, value2)
        )
    }

    @Test
    fun userProfileID() {
        val userProfileID = "user_profile_id"
        assertThat(defaultConfigBuilder.withUserProfileID(userProfileID).build().userProfileID)
            .isEqualTo(userProfileID)
    }

    @Test
    fun userProfileIDIfNotSet() {
        assertThat(defaultConfigBuilder.build().userProfileID).isNull()
    }

    @Test
    fun revenueAutoTrackingEnabled() {
        val config = defaultConfigBuilder.withRevenueAutoTrackingEnabled(true).build()
        assertThat(config.revenueAutoTrackingEnabled).isTrue()
    }

    @Test
    fun revenueAutoTrackingEnabledIsNotSet() {
        assertThat(defaultConfig.revenueAutoTrackingEnabled).isNull()
    }

    @Test
    fun sessionsAutoTrackingEnabled() {
        val config = defaultConfigBuilder.withSessionsAutoTrackingEnabled(true).build()
        assertThat(config.sessionsAutoTrackingEnabled).isTrue()
    }

    @Test
    fun sessionsAutoTrackingDisabled() {
        val config = defaultConfigBuilder.withSessionsAutoTrackingEnabled(false).build()
        assertThat(config.sessionsAutoTrackingEnabled).isFalse()
    }

    @Test
    fun sessionsAutoTrackingEnabledIsNotSet() {
        assertThat(defaultConfig.sessionsAutoTrackingEnabled).isNull()
    }

    @Test
    fun appOpenTrackingEnabled() {
        val config = defaultConfigBuilder.withAppOpenTrackingEnabled(true).build()
        assertThat(config.appOpenTrackingEnabled).isTrue()
    }

    @Test
    fun appOpenTrackingDisabled() {
        val config = defaultConfigBuilder.withAppOpenTrackingEnabled(false).build()
        assertThat(config.appOpenTrackingEnabled).isFalse()
    }

    @Test
    fun appOpenTrackingEnabledIsNotSet() {
        assertThat(defaultConfig.appOpenTrackingEnabled).isNull()
    }

    @Test
    fun invalidMaxReportsInDatabaseCount() {
        val config = AppMetricaConfig.newConfigBuilder(apiKey)
            .withMaxReportsInDatabaseCount(oldMaxReportsInDatabaseCount)
            .build()
        assertThat(config.maxReportsInDatabaseCount).isEqualTo(newMaxReportsInDatabaseCount)
    }

    @Test
    fun appBuildNumber() {
        val config = defaultConfigBuilder.withAppBuildNumber(TestData.TEST_APP_BUILD_NUMBER).build()
        assertThat(config.appBuildNumber).isEqualTo(TestData.TEST_APP_BUILD_NUMBER)
    }

    @Test
    fun negativeBuildNumber() {
        assertThatThrownBy { defaultConfigBuilder.withAppBuildNumber(-1000).build() }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining(VerificationConstants.APP_BUILD_NUMBER)
    }

    @Test
    fun noDefBuildNumber() {
        assertThat(defaultConfig.appBuildNumber).isNull()
    }

    @Test
    fun knownDeviceTypeDefinedWithString() {
        val deviceType = "tv"
        val config = defaultConfigBuilder
            .withDeviceType(deviceType).build()
        assertThat(config.deviceType).isEqualTo(deviceType)
    }

    @Test
    fun customDeviceType() {
        val deviceType = "car"
        val config = defaultConfigBuilder.withDeviceType(deviceType).build()
        assertThat(config.deviceType).isEqualTo(deviceType)
    }

    @Test
    fun noDefDeviceType() {
        val config = defaultConfigBuilder.build()
        assertThat(config.deviceType).isNull()
    }

    @Test
    fun maxReportsCount() {
        val config = defaultConfigBuilder.withMaxReportsCount(TestData.TEST_MAX_REPORTS_COUNT).build()
        assertThat(config.maxReportsCount).isEqualTo(TestData.TEST_MAX_REPORTS_COUNT)
    }

    @Test
    fun noDefMaxReportCount() {
        val config = defaultConfigBuilder.build()
        assertThat(config.maxReportsCount).isNull()
    }

    @Test
    fun negativeMaxReportsCount() {
        val config = defaultConfigBuilder.withMaxReportsCount(TestData.TEST_NEGATIVE_MAX_REPORTS_COUNT).build()
        assertThat(config.maxReportsCount).isNegative
    }

    @Test
    fun maxReportsInDbCount() {
        val config = defaultConfigBuilder.withMaxReportsInDatabaseCount(TestData.TEST_MAX_REPORTS_IN_DB_COUNT).build()
        assertThat(config.maxReportsInDatabaseCount).isEqualTo(TestData.TEST_MAX_REPORTS_IN_DB_COUNT)
    }

    @Test
    fun noDefMaxReportInDbCount() {
        val config = defaultConfigBuilder.build()
        assertThat(config.maxReportsInDatabaseCount).isNull()
    }

    @Test
    fun dispatchPeriodSeconds() {
        val config = defaultConfigBuilder.withDispatchPeriodSeconds(TestData.TEST_DISPATCH_PERIOD_SECONDS).build()
        assertThat(config.dispatchPeriodSeconds).isEqualTo(TestData.TEST_DISPATCH_PERIOD_SECONDS)
    }

    @Test
    fun noDefDispatchPeriodSeconds() {
        val config = defaultConfigBuilder.build()
        assertThat(config.dispatchPeriodSeconds).isNull()
    }

    @Test
    fun negativeDispatchPeriodSeconds() {
        val config = defaultConfigBuilder
            .withDispatchPeriodSeconds(TestData.TEST_NEGATIVE_DISPATCH_PERIOD_SECONDS)
            .build()
        assertThat(config.dispatchPeriodSeconds).isNegative
    }

    @Test
    fun configContainsAppEnvironment() {
        val capacity = 50
        val keys = ArrayList<String>(capacity)
        val config = AppMetricaConfig.newConfigBuilder(apiKey)
        for (i in 0 until capacity) {
            val key = "key" + i + "salt"
            keys.add(key)
            config.withAppEnvironmentValue(key, "value$i")
        }
        assertThat(config.build().appEnvironment!!.keys).containsExactlyInAnyOrderElementsOf(keys)
    }

    @Test(expected = UnsupportedOperationException::class)
    fun appEnvironmentUnmodifiable() {
        AppMetricaConfig.newConfigBuilder(apiKey).build().appEnvironment!!["key"] = "value"
    }

    @Test
    fun noDefCrashTransformer() {
        assertThat(defaultConfig.crashTransformer).isNull()
    }

    @Test
    fun crashTransformer() {
        val crashTransformer = mock<ICrashTransformer>()
        val config = defaultConfigBuilder.withCrashTransformer(crashTransformer).build()
        assertThat(config.crashTransformer).isEqualTo(crashTransformer)
    }

    @Test
    fun anrMonitoring() {
        val config = defaultConfigBuilder.withAnrMonitoring(TestData.TEST_ANR_MONITORING).build()
        assertThat(config.anrMonitoring).isEqualTo(TestData.TEST_ANR_MONITORING)
    }

    @Test
    fun anrMonitoringTimeout() {
        val config = defaultConfigBuilder.withAnrMonitoringTimeout(TestData.TEST_ANR_MONITORING_TIMEOUT).build()
        assertThat(config.anrMonitoringTimeout).isEqualTo(TestData.TEST_ANR_MONITORING_TIMEOUT)
    }
}
