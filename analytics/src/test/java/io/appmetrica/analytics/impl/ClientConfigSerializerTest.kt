package io.appmetrica.analytics.impl

import android.location.Location
import io.appmetrica.analytics.AppMetricaConfig
import io.appmetrica.analytics.ICrashTransformer
import io.appmetrica.analytics.PredefinedDeviceTypes
import io.appmetrica.analytics.PreloadInfo
import io.appmetrica.analytics.assertions.ObjectPropertyAssertions
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.constructionRule
import org.assertj.core.api.Assertions.assertThat
import org.json.JSONObject
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import org.skyscreamer.jsonassert.JSONAssert
import java.util.Random

@RunWith(RobolectricTestRunner::class)
class ClientConfigSerializerTest : CommonTest() {
    private val apiKey = "5012c3cc-20a4-4dac-92d1-83ebc27c0fa9"
    private val deviceType = PredefinedDeviceTypes.PHONE
    private val appBuildNumber = 42
    private val dispatchPeriodSeconds = 953242
    private val maxReportsCount = 21358
    private val appEnvKeyFirst = "key1"
    private val appEnvValueFirst = "value1"
    private val appEnvKeySecond = "key2"
    private val appEnvValueSecond = "value2"
    private val appEnvironmentMap = mapOf(appEnvKeyFirst to appEnvValueFirst, appEnvKeySecond to appEnvValueSecond)

    @get:Rule
    val additionalFieldsSerializerMockedConstructionRule =
        constructionRule<DefaultClientConfigAdditionalFieldsSerializer>()
    private val additionalFieldsSerializer by additionalFieldsSerializerMockedConstructionRule
    private val newAdditionalFieldsSerializer: ClientConfigAdditionalFieldsSerializer = mock()

    private val clientConfigSerializer by setUp { ClientConfigSerializer() }

    @Test
    fun filledConfig() {
        val random = Random()
        val errorEnvKeyFirst = "key1"
        val errorEnvValueFirst = "value1"
        val errorEnvKeySecond = "key2"
        val errorEnvValueSecond = "value2"
        val errorEnvironmentMap: MutableMap<String, String> = HashMap()
        errorEnvironmentMap[errorEnvKeyFirst] = errorEnvValueFirst
        errorEnvironmentMap[errorEnvKeySecond] = errorEnvValueSecond
        val handleFirstActivationAsUpdate = random.nextBoolean()
        val appVersion = "1.3.6"
        val crashReporting = random.nextBoolean()
        val location = Location("gps")
        location.latitude = random.nextDouble()
        location.longitude = random.nextDouble()
        val locationTracking = random.nextBoolean()
        val advIdentifiersTracking = true
        val maxReportsInDbCount = 800
        val nativeCrashReporting = random.nextBoolean()
        val preloadInfo = PreloadInfo.newBuilder("888999").setAdditionalParams("key", "value").build()
        val sessionTimeout = 23
        val dataSendingEnabled = random.nextBoolean()
        val userProfileID = "user_profile_id"
        val revenueAutoTracking = false
        val sessionsAutoTracking = false
        val appOpenAutoTracking = false
        val anrMonitoring = false
        val anrMonitoringTimeout = 42
        val additionalConfigKeyFirst = "key1"
        val additionalConfigValueFirst = "value1"
        val additionalConfigKeySecond = "key2"
        val additionalConfigValueSecond = "value2"
        val additionalConfigMap = mapOf(
            additionalConfigKeyFirst to additionalConfigValueFirst,
            additionalConfigKeySecond to additionalConfigValueSecond
        )
        val additionalFieldsJson = JSONObject(additionalConfigMap)
        whenever(additionalFieldsSerializer.toJson(additionalConfigMap)).thenReturn(additionalFieldsJson)
        whenever(additionalFieldsSerializer.parseJson(any(), any())).then { invocation ->
            val builder = invocation.arguments[1] as AppMetricaConfig.Builder
            JSONAssert.assertEquals(additionalFieldsJson, invocation.arguments.first() as JSONObject, true)
            builder.withAdditionalConfig(additionalConfigKeyFirst, additionalConfigValueFirst)
            builder.withAdditionalConfig(additionalConfigKeySecond, additionalConfigValueSecond)
            null
        }
        val customHosts = listOf("customHost1", "customHost2")
        val config = AppMetricaConfig.newConfigBuilder(apiKey)
            .withErrorEnvironmentValue(errorEnvKeyFirst, errorEnvValueFirst)
            .withErrorEnvironmentValue(errorEnvKeySecond, errorEnvValueSecond)
            .handleFirstActivationAsUpdate(handleFirstActivationAsUpdate)
            .withAppVersion(appVersion)
            .withCrashReporting(crashReporting)
            .withLocation(location)
            .withLocationTracking(locationTracking)
            .withAdvIdentifiersTracking(true)
            .withLogs()
            .withMaxReportsInDatabaseCount(maxReportsInDbCount)
            .withNativeCrashReporting(nativeCrashReporting)
            .withPreloadInfo(preloadInfo)
            .withSessionTimeout(sessionTimeout)
            .withDataSendingEnabled(dataSendingEnabled)
            .withUserProfileID(userProfileID)
            .withRevenueAutoTrackingEnabled(revenueAutoTracking)
            .withSessionsAutoTrackingEnabled(sessionsAutoTracking)
            .withAppOpenTrackingEnabled(appOpenAutoTracking)
            .withDeviceType(deviceType)
            .withAppBuildNumber(appBuildNumber)
            .withDispatchPeriodSeconds(dispatchPeriodSeconds)
            .withMaxReportsCount(maxReportsCount)
            .withAppEnvironmentValue(appEnvKeyFirst, appEnvValueFirst)
            .withAppEnvironmentValue(appEnvKeySecond, appEnvValueSecond)
            .withAnrMonitoring(anrMonitoring)
            .withAnrMonitoringTimeout(anrMonitoringTimeout)
            .withCustomHosts(customHosts)
            .withAdditionalConfig(additionalConfigKeyFirst, additionalConfigValueFirst)
            .withAdditionalConfig(additionalConfigKeySecond, additionalConfigValueSecond)
            .build()
        val json = clientConfigSerializer.toJson(config)
        val deserialized = clientConfigSerializer.fromJson(json)
        ObjectPropertyAssertions(deserialized).withIgnoredFields("location", "preloadInfo", "crashTransformer")
            .checkField("apiKey", apiKey)
            .checkField("appVersion", appVersion)
            .checkField("sessionTimeout", sessionTimeout)
            .checkField("crashReporting", crashReporting)
            .checkField("nativeCrashReporting", nativeCrashReporting)
            .checkField("locationTracking", locationTracking)
            .checkField("advIdentifiersTracking", advIdentifiersTracking)
            .checkField("logs", true)
            .checkField("firstActivationAsUpdate", handleFirstActivationAsUpdate)
            .checkField("dataSendingEnabled", dataSendingEnabled)
            .checkField("maxReportsInDatabaseCount", maxReportsInDbCount)
            .checkField("errorEnvironment", errorEnvironmentMap)
            .checkField("userProfileID", userProfileID)
            .checkField("revenueAutoTrackingEnabled", revenueAutoTracking)
            .checkField("sessionsAutoTrackingEnabled", sessionsAutoTracking)
            .checkField("appOpenTrackingEnabled", appOpenAutoTracking)
            .checkField("deviceType", deviceType)
            .checkField("appBuildNumber", appBuildNumber)
            .checkField("dispatchPeriodSeconds", dispatchPeriodSeconds)
            .checkField("maxReportsCount", maxReportsCount)
            .checkField("appEnvironment", appEnvironmentMap)
            .checkField("anrMonitoring", anrMonitoring)
            .checkField("anrMonitoringTimeout", anrMonitoringTimeout)
            .checkField("customHosts", customHosts)
            .checkField("additionalConfig", additionalConfigMap)
            .checkAll()
        assertThat(deserialized?.location).isEqualToComparingOnlyGivenFields(
            location,
            "provider",
            "latitude",
            "longitude",
            "time",
            "accuracy",
            "altitude"
        )
        assertThat(deserialized?.preloadInfo).isEqualToComparingFieldByField(preloadInfo)
    }

    @Test
    fun emptyConfig() {
        val emptyConfig = AppMetricaConfig.newConfigBuilder(apiKey).build()
        val json = clientConfigSerializer.toJson(emptyConfig)
        val deserialized = clientConfigSerializer.fromJson(json)
        val nullString: String? = null
        val nullBoolean: Boolean? = null
        val nullInt: Int? = null
        ObjectPropertyAssertions(deserialized)
            .checkField("apiKey", apiKey)
            .checkField("appVersion", nullString)
            .checkField("sessionTimeout", nullInt)
            .checkField("crashReporting", nullBoolean)
            .checkField("nativeCrashReporting", nullBoolean)
            .checkField("locationTracking", nullBoolean)
            .checkField("advIdentifiersTracking", nullBoolean)
            .checkField("logs", nullBoolean)
            .checkField("firstActivationAsUpdate", nullBoolean)
            .checkField("dataSendingEnabled", nullBoolean)
            .checkField("maxReportsInDatabaseCount", nullInt)
            .checkField("errorEnvironment", emptyMap<Any, Any>())
            .checkField("location", null as Location?)
            .checkField("preloadInfo", null as PreloadInfo?)
            .checkField<Any>("userProfileID", null)
            .checkField("deviceType", nullString)
            .checkField("appBuildNumber", nullInt)
            .checkField("dispatchPeriodSeconds", nullInt)
            .checkField("maxReportsCount", nullInt)
            .checkField("appEnvironment", emptyMap<Any, Any>())
            .checkField("crashTransformer", null as ICrashTransformer?)
            .checkFieldIsNull("revenueAutoTrackingEnabled")
            .checkFieldIsNull("sessionsAutoTrackingEnabled")
            .checkFieldIsNull("appOpenTrackingEnabled")
            .checkField("anrMonitoring", nullBoolean)
            .checkField("anrMonitoringTimeout", nullInt)
            .checkField("customHosts", null as List<String?>?)
            .checkField("additionalConfig", emptyMap<Any, Any>())
            .checkAll()
    }

    @Test
    fun updateAdditionalConfigFieldsSerializer() {
        clientConfigSerializer.setAdditionalConfigSerializer(newAdditionalFieldsSerializer)
        clientConfigSerializer.toJson(
            AppMetricaConfig.newConfigBuilder(apiKey)
                .withAdditionalConfig("first", "second")
                .build()
        )
        verify(newAdditionalFieldsSerializer).toJson(any())
        verifyNoInteractions(additionalFieldsSerializer)
    }
}
