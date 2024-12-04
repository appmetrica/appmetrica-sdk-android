package io.appmetrica.analytics.internal

import android.location.Location
import android.os.Build
import io.appmetrica.analytics.AppMetricaConfig
import io.appmetrica.analytics.ReporterConfig
import io.appmetrica.analytics.internal.CounterConfigurationReporterType.Companion.fromStringValue
import io.appmetrica.analytics.impl.SdkData
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.DummyLocationProvider
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.SoftAssertions
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.UUID

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.LOLLIPOP])
class CounterConfigurationTest : CommonTest() {

    @RunWith(ParameterizedRobolectricTestRunner::class)
    class ReporterTypeTest(
        private val mReporterType: CounterConfigurationReporterType,
        private val mStringValue: String
    ) {

        @Test
        fun stringValue() {
            assertThat(mReporterType.stringValue).isEqualTo(mStringValue)
        }

        @Test
        fun fromString() {
            assertThat(fromStringValue(mStringValue)).isEqualTo(mReporterType)
        }

        companion object {

            @ParameterizedRobolectricTestRunner.Parameters(name = "Report type: {0}")
            @JvmStatic
            fun data(): Collection<Array<Any>> {
                return listOf(
                    arrayOf(CounterConfigurationReporterType.COMMUTATION, "commutation"),
                    arrayOf(CounterConfigurationReporterType.MAIN, "main"),
                    arrayOf(CounterConfigurationReporterType.MANUAL, "manual"),
                    arrayOf(CounterConfigurationReporterType.SELF_DIAGNOSTIC_MAIN, "self_diagnostic_main"),
                    arrayOf(CounterConfigurationReporterType.SELF_DIAGNOSTIC_MANUAL, "self_diagnostic_manual"),
                    arrayOf(CounterConfigurationReporterType.SELF_SDK, "self_sdk"),
                    arrayOf(CounterConfigurationReporterType.CRASH, "crash")
                )
            }
        }
    }

    private lateinit var counterConfiguration: CounterConfiguration

    @Before
    fun setUp() {
        counterConfiguration = CounterConfiguration()
    }

    @Test
    fun writeDefaultDispatchPeriod() {
        val dispatchPeriod = 170
        counterConfiguration.setDispatchPeriod(dispatchPeriod)
        assertThat(counterConfiguration.dispatchPeriod).isEqualTo(dispatchPeriod)
    }

    @Test
    fun writeZeroToDispatchPeriod() {
        counterConfiguration.setDispatchPeriod(0)
        assertThat(counterConfiguration.dispatchPeriod).isZero
    }

    @Test
    fun rewriteDispatchPeriod() {
        val dispatchPeriod = 270
        counterConfiguration.setDispatchPeriod(100)
        assertThat(counterConfiguration.dispatchPeriod).isNotEqualTo(dispatchPeriod)
        counterConfiguration.setDispatchPeriod(dispatchPeriod)
        assertThat(counterConfiguration.dispatchPeriod).isEqualTo(dispatchPeriod)
    }

    @Test
    fun writeSessionTimeout() {
        val sessionTimeout = 160
        counterConfiguration.setSessionTimeout(sessionTimeout)
        assertThat(counterConfiguration.sessionTimeout).isEqualTo(sessionTimeout)
    }

    @Test
    fun rewriteSessionTimeout() {
        val sessionTimeout = 140
        counterConfiguration.setSessionTimeout(80)
        assertThat(counterConfiguration.sessionTimeout).isNotEqualTo(sessionTimeout)
        counterConfiguration.setSessionTimeout(sessionTimeout)
        assertThat(counterConfiguration.sessionTimeout).isEqualTo(sessionTimeout)
    }

    @Test
    fun writeMaxReportsCount() {
        val maxReportsCount = 22
        counterConfiguration.setMaxReportsCount(maxReportsCount)
        assertThat(counterConfiguration.maxReportsCount).isEqualTo(maxReportsCount)
    }

    @Test
    fun writeZeroToMaxReportsCount() {
        counterConfiguration.setMaxReportsCount(0)
        assertThat(counterConfiguration.maxReportsCount).isEqualTo(Int.MAX_VALUE)
    }

    @Test
    fun writeNegativeValueToMaxReportsCount() {
        counterConfiguration.setMaxReportsCount(-1)
        assertThat(counterConfiguration.maxReportsCount).isEqualTo(Int.MAX_VALUE)
    }

    @Test
    fun rewriteMaxReportsCount() {
        val maxReportsCount = 44
        counterConfiguration.setMaxReportsCount(12)
        assertThat(counterConfiguration.maxReportsCount).isNotEqualTo(maxReportsCount)
        counterConfiguration.setMaxReportsCount(maxReportsCount)
        assertThat(counterConfiguration.maxReportsCount).isEqualTo(maxReportsCount)
    }

    @Test
    fun rewriteZeroToMaxReportsCount() {
        counterConfiguration.setMaxReportsCount(20)
        counterConfiguration.setMaxReportsCount(0)
        assertThat(counterConfiguration.maxReportsCount).isEqualTo(Int.MAX_VALUE)
    }

    @Test
    fun rewriteNegativeValueToMaxReportsCount() {
        counterConfiguration.setMaxReportsCount(30)
        counterConfiguration.setMaxReportsCount(-1)
        assertThat(counterConfiguration.maxReportsCount).isEqualTo(Int.MAX_VALUE)
    }

    @Test
    fun writeFalseToReportLocationEnabled() {
        counterConfiguration.setLocationTracking(false)
        assertThat(counterConfiguration.isLocationTrackingEnabled).isFalse()
    }

    @Test
    fun writeTrueToReportLocationEnabled() {
        counterConfiguration.setLocationTracking(true)
        assertThat(counterConfiguration.isLocationTrackingEnabled).isTrue()
    }

    @Test
    fun rewriteReportLocationEnabled() {
        counterConfiguration.setLocationTracking(true)
        counterConfiguration.setLocationTracking(false)
        assertThat(counterConfiguration.isLocationTrackingEnabled).isFalse()
        counterConfiguration.setLocationTracking(true)
        assertThat(counterConfiguration.isLocationTrackingEnabled).isTrue()
    }

    @Test
    fun writeFalseToAdvIdentifiersTrackingEnabled() {
        counterConfiguration.setAdvIdentifiersTracking(false)
        assertThat(counterConfiguration.isAdvIdentifiersTrackingEnabled).isFalse()
    }

    @Test
    fun writeTrueToAdvIdentifiersTrackingEnabled() {
        counterConfiguration.setAdvIdentifiersTracking(true)
        assertThat(counterConfiguration.isAdvIdentifiersTrackingEnabled).isTrue()
    }

    @Test
    fun rewriteAdvIdentifierTracking() {
        counterConfiguration.setAdvIdentifiersTracking(true)
        counterConfiguration.setAdvIdentifiersTracking(false)
        assertThat(counterConfiguration.isAdvIdentifiersTrackingEnabled).isFalse()
        counterConfiguration.setAdvIdentifiersTracking(true)
        assertThat(counterConfiguration.isAdvIdentifiersTrackingEnabled).isTrue()
    }

    @Test
    fun defaultManualLocation() {
        assertThat(counterConfiguration.manualLocation).isNull()
    }

    @Test
    fun writeNullToManualLocation() {
        counterConfiguration.manualLocation = null
        assertThat(counterConfiguration.manualLocation).isNull()
    }

    @Test
    fun writeManualLocation() {
        val location = DummyLocationProvider.getLocation()
        counterConfiguration.manualLocation = location
        assertThat(counterConfiguration.manualLocation).usingRecursiveComparison().isEqualTo(location)
    }

    @Test
    fun testRewriteManualLocation() {
        val location = Location("535")
        val lastLocation = DummyLocationProvider.getLocation()
        counterConfiguration.manualLocation = location
        counterConfiguration.manualLocation = lastLocation
        assertThat(counterConfiguration.manualLocation).isEqualToComparingFieldByField(lastLocation)
    }

    @Test
    fun testDefaultAppVersion() {
        assertThat(counterConfiguration.appVersion).isNull()
    }

    @Test
    fun writeAppVersion() {
        val appVersion = "3.23"
        counterConfiguration.setCustomAppVersion(appVersion)
        assertThat(counterConfiguration.appVersion).isEqualTo(appVersion)
    }

    @Test
    fun rewriteAppVersion() {
        counterConfiguration.setCustomAppVersion("3.55")
        val appVersion = "5.33"
        counterConfiguration.setCustomAppVersion(appVersion)
        assertThat(counterConfiguration.appVersion).isEqualTo(appVersion)
    }

    @Test
    fun defaultBuildNumber() {
        assertThat(counterConfiguration.appBuildNumber).isEqualTo(null)
    }

    @Test
    fun writeBuildNumber() {
        val buildNumber = 232
        counterConfiguration.setAppBuildNumber(buildNumber)
        assertThat(counterConfiguration.appBuildNumber).isEqualTo(buildNumber.toString())
    }

    @Test
    fun rewriteBuildNumber() {
        counterConfiguration.setAppBuildNumber(311)
        val buildNumber = 4545
        counterConfiguration.setAppBuildNumber(buildNumber)
        assertThat(counterConfiguration.appBuildNumber).isEqualTo(buildNumber.toString())
    }

    @Test
    fun defaultDeviceType() {
        assertThat(counterConfiguration.deviceType).isNull()
    }

    @Test
    fun writeValidDeviceType() {
        val deviceType = "phone"
        counterConfiguration.deviceType = deviceType
        assertThat(counterConfiguration.deviceType).isEqualTo(deviceType)
    }

    @Test
    fun rewriteValidDeviceType() {
        val deviceType = "phone"
        counterConfiguration.deviceType = "car"
        assertThat(counterConfiguration.deviceType).isNotEqualTo(deviceType)
        counterConfiguration.deviceType = deviceType
        assertThat(counterConfiguration.deviceType).isEqualTo(deviceType)
    }

    @Test
    fun writeEmptyDeviceType() {
        counterConfiguration.deviceType = null
        assertThat(counterConfiguration.deviceType).isNull()
    }

    @Test
    fun rewriteEmptyDeviceType() {
        counterConfiguration.deviceType = "phone"
        assertThat(counterConfiguration.deviceType).isNotNull
        counterConfiguration.deviceType = null
        assertThat(counterConfiguration.deviceType).isNull()
    }

    @Test
    fun rewriteDefaultWithMaxReportsCountFromConfig() {
        val maxReportsCount = 20
        counterConfiguration.setMaxReportsCount(30)
        assertThat(counterConfiguration.maxReportsCount).isNotEqualTo(maxReportsCount)
        counterConfiguration.setMaxReportsCount(maxReportsCount)
        assertThat(counterConfiguration.maxReportsCount).isEqualTo(maxReportsCount)
    }

    @Test
    fun isFirstActivationAsUpdate() {
        val apiKey = UUID.randomUUID().toString()
        val metricaInternalConfig = AppMetricaConfig.newConfigBuilder(apiKey)
            .handleFirstActivationAsUpdate(true)
            .build()
        assertThat(
            CounterConfiguration(metricaInternalConfig, CounterConfigurationReporterType.MAIN).isFirstActivationAsUpdate
        ).isTrue()
    }

    @Test
    fun maxReportsInDatabaseCountFromConfig() {
        val maxReportsInDatabaseCount = 2000
        val config = CounterConfiguration(
            AppMetricaConfig.newConfigBuilder(UUID.randomUUID().toString())
                .withMaxReportsInDatabaseCount(maxReportsInDatabaseCount).build(),
            CounterConfigurationReporterType.MAIN
        )
        assertThat(config.maxReportsInDbCount).isEqualTo(maxReportsInDatabaseCount)
    }

    @Test
    fun defaultMaxReportsInDatabaseCount() {
        val config = CounterConfiguration(
            AppMetricaConfig.newConfigBuilder(UUID.randomUUID().toString()).build(),
            CounterConfigurationReporterType.MAIN
        )
        assertThat(config.maxReportsInDbCount).isNull()
    }

    @Test
    fun reportNativeCrashesEnabled() {
        val apiKey = UUID.randomUUID().toString()
        val config = CounterConfiguration(
            AppMetricaConfig.newConfigBuilder(apiKey)
                .withNativeCrashReporting(true).build(),
            CounterConfigurationReporterType.MAIN
        )
        assertThat(config.reportNativeCrashesEnabled).isTrue()
    }

    @Test
    fun reportNativeCrashesDisabled() {
        val apiKey = UUID.randomUUID().toString()
        val config = CounterConfiguration(
            AppMetricaConfig.newConfigBuilder(apiKey).withNativeCrashReporting(false).build(),
            CounterConfigurationReporterType.MAIN
        )
        assertThat(config.reportNativeCrashesEnabled).isFalse()
    }

    @Test
    fun reportNativeCrashesNull() {
        val apiKey = UUID.randomUUID().toString()
        val config = CounterConfiguration(
            AppMetricaConfig.newConfigBuilder(apiKey).build(),
            CounterConfigurationReporterType.MAIN
        )
        assertThat(config.reportNativeCrashesEnabled).isNull()
    }

    @Test
    fun revenueAutoTrackingEnabledNotSetFromConfig() {
        val config = CounterConfiguration(
            AppMetricaConfig.newConfigBuilder(UUID.randomUUID().toString()).build(),
            CounterConfigurationReporterType.MAIN
        )
        assertThat(config.isRevenueAutoTrackingEnabled).isNull()
    }

    @Test
    fun setRevenueAutoTrackingEnabled() {
        val config = CounterConfiguration()
        config.setRevenueAutoTrackingEnabled(true)
        assertThat(config.isRevenueAutoTrackingEnabled).isTrue()
        config.setRevenueAutoTrackingEnabled(false)
        assertThat(config.isRevenueAutoTrackingEnabled).isFalse()
    }

    @Test
    fun appMetricaConfigReporterTypeMain() {
        val apiKey = UUID.randomUUID().toString()
        val config = CounterConfiguration(
            AppMetricaConfig.newConfigBuilder(apiKey).build(),
            CounterConfigurationReporterType.MAIN
        )
        assertThat(config.reporterType).isEqualTo(CounterConfigurationReporterType.MAIN)
    }

    @Test
    fun appMetricaConfigReporterTypeCrash() {
        val apiKey = UUID.randomUUID().toString()
        val config = CounterConfiguration(
            AppMetricaConfig.newConfigBuilder(apiKey).build(),
            CounterConfigurationReporterType.CRASH
        )
        assertThat(config.reporterType).isEqualTo(CounterConfigurationReporterType.CRASH)
    }

    @Test
    fun reporterConfigReporterType() {
        val apiKey = UUID.randomUUID().toString()
        val config = CounterConfiguration(ReporterConfig.newConfigBuilder(apiKey).build())
        assertThat(config.reporterType).isEqualTo(CounterConfigurationReporterType.MANUAL)
    }

    @Test
    fun reporterConfigReporterTypeAppmetrica() {
        val config = CounterConfiguration(ReporterConfig.newConfigBuilder(SdkData.SDK_API_KEY_UUID).build())
        assertThat(config.reporterType).isEqualTo(CounterConfigurationReporterType.SELF_SDK)
    }

    @Test
    fun apiKeyConstructor() {
        val apiKey = UUID.randomUUID().toString()
        val config = CounterConfiguration(apiKey)
        assertThat(config.apiKey).isEqualTo(apiKey)
    }

    @Test
    fun setReporterType() {
        val config = CounterConfiguration()
        assertThat(config.reporterType).isEqualTo(CounterConfigurationReporterType.MAIN)
        config.reporterType = CounterConfigurationReporterType.COMMUTATION
        assertThat(config.reporterType).isEqualTo(CounterConfigurationReporterType.COMMUTATION)
        config.reporterType = CounterConfigurationReporterType.MANUAL
        assertThat(config.reporterType).isEqualTo(CounterConfigurationReporterType.MANUAL)
    }

    @Test
    fun reporterTypeFromNullValue() {
        assertThat(fromStringValue(null)).isEqualTo(CounterConfigurationReporterType.MAIN)
    }

    @Test
    fun `constructor with AppMetricaConfig`() {
        val reporterType = CounterConfigurationReporterType.MAIN
        val inputConfig = AppMetricaConfig.newConfigBuilder(UUID.randomUUID().toString())
            .withSessionTimeout(100500)
            .withLocation(mock())
            .withLocationTracking(true)
            .withDeviceType("Device type")
            .withDispatchPeriodSeconds(400)
            .withMaxReportsCount(100)
            .withLogs()
            .withAppVersion("3.1.1")
            .withAppBuildNumber(300010001)
            .handleFirstActivationAsUpdate(true)
            .withDataSendingEnabled(true)
            .withMaxReportsInDatabaseCount(5000)
            .withNativeCrashReporting(true)
            .withRevenueAutoTrackingEnabled(true)
            .build()

        val actual = CounterConfiguration(inputConfig, reporterType)

        with(SoftAssertions()) {
            assertThat(actual.apiKey).isEqualTo(inputConfig.apiKey)
            assertThat(actual.sessionTimeout).isEqualTo(inputConfig.sessionTimeout)
            assertThat(actual.manualLocation).isNotNull
            assertThat(actual.isLocationTrackingEnabled).isEqualTo(inputConfig.locationTracking)
            assertThat(actual.deviceType).isEqualTo(inputConfig.deviceType)
            assertThat(actual.dispatchPeriod).isEqualTo(inputConfig.dispatchPeriodSeconds)
            assertThat(actual.maxReportsCount).isEqualTo(inputConfig.maxReportsCount)
            assertThat(actual.maxReportsInDbCount).isEqualTo(inputConfig.maxReportsInDatabaseCount)
            assertThat(actual.isLogEnabled).isEqualTo(inputConfig.logs)
            assertThat(actual.appVersion).isEqualTo(inputConfig.appVersion)
            assertThat(actual.appBuildNumber).isEqualTo(inputConfig.appBuildNumber.toString())
            assertThat(actual.isFirstActivationAsUpdate).isEqualTo(inputConfig.firstActivationAsUpdate)
            assertThat(actual.dataSendingEnabled).isEqualTo(inputConfig.dataSendingEnabled)
            assertThat(actual.maxReportsInDbCount).isEqualTo(inputConfig.maxReportsInDatabaseCount)
            assertThat(actual.reportNativeCrashesEnabled).isEqualTo(inputConfig.nativeCrashReporting)
            assertThat(actual.isRevenueAutoTrackingEnabled).isEqualTo(inputConfig.revenueAutoTrackingEnabled)
            assertThat(actual.reporterType).isEqualTo(reporterType)
            assertAll()
        }
    }

    @Test
    fun applyFromConfig() {
        val reporterType = CounterConfigurationReporterType.MAIN
        val inputConfig = AppMetricaConfig.newConfigBuilder(UUID.randomUUID().toString())
            .withSessionTimeout(100500)
            .withLocation(mock())
            .withLocationTracking(true)
            .withDeviceType("Device type")
            .withDispatchPeriodSeconds(400)
            .withMaxReportsCount(100)
            .withLogs()
            .withAppVersion("3.1.1")
            .withAppBuildNumber(300010001)
            .handleFirstActivationAsUpdate(true)
            .withDataSendingEnabled(true)
            .withMaxReportsInDatabaseCount(5000)
            .withNativeCrashReporting(true)
            .withRevenueAutoTrackingEnabled(true)
            .withAdvIdentifiersTracking(true)
            .build()

        val actual = CounterConfiguration(reporterType)
        actual.applyFromConfig(inputConfig)

        with(SoftAssertions()) {
            assertThat(actual.apiKey).isEqualTo(inputConfig.apiKey)
            assertThat(actual.sessionTimeout).isEqualTo(inputConfig.sessionTimeout)
            assertThat(actual.manualLocation).isNotNull
            assertThat(actual.isLocationTrackingEnabled).isEqualTo(inputConfig.locationTracking)
            assertThat(actual.deviceType).isEqualTo(inputConfig.deviceType)
            assertThat(actual.dispatchPeriod).isEqualTo(inputConfig.dispatchPeriodSeconds)
            assertThat(actual.maxReportsCount).isEqualTo(inputConfig.maxReportsCount)
            assertThat(actual.maxReportsInDbCount).isEqualTo(inputConfig.maxReportsInDatabaseCount)
            assertThat(actual.isLogEnabled).isEqualTo(inputConfig.logs)
            assertThat(actual.appVersion).isEqualTo(inputConfig.appVersion)
            assertThat(actual.appBuildNumber).isEqualTo(inputConfig.appBuildNumber.toString())
            assertThat(actual.isFirstActivationAsUpdate).isEqualTo(inputConfig.firstActivationAsUpdate)
            assertThat(actual.dataSendingEnabled).isEqualTo(inputConfig.dataSendingEnabled)
            assertThat(actual.maxReportsInDbCount).isEqualTo(inputConfig.maxReportsInDatabaseCount)
            assertThat(actual.reportNativeCrashesEnabled).isEqualTo(inputConfig.nativeCrashReporting)
            assertThat(actual.isRevenueAutoTrackingEnabled).isEqualTo(inputConfig.revenueAutoTrackingEnabled)
            assertThat(actual.reporterType).isEqualTo(reporterType)
            assertThat(actual.isAdvIdentifiersTrackingEnabled).isEqualTo(inputConfig.advIdentifiersTracking)
            assertAll()
        }
    }

    @Test
    fun `fun constructor with empty AppMetricaConfig`() {
        val reporterType = CounterConfigurationReporterType.SELF_SDK

        val inputConfig = AppMetricaConfig.newConfigBuilder(UUID.randomUUID().toString())
            .withDataSendingEnabled(false)
            .build()

        val actual = CounterConfiguration(inputConfig, reporterType)

        with(SoftAssertions()) {
            assertThat(actual.apiKey).isEqualTo(inputConfig.apiKey)
            assertThat(actual.sessionTimeout).isNull()
            assertThat(actual.manualLocation).isNull()
            assertThat(actual.isLocationTrackingEnabled).isNull()
            assertThat(actual.deviceType).isNull()
            assertThat(actual.dispatchPeriod).isNull()
            assertThat(actual.maxReportsCount).isNull()
            assertThat(actual.maxReportsInDbCount).isNull()
            assertThat(actual.isLogEnabled).isNull()
            assertThat(actual.appVersion).isNull()
            assertThat(actual.appBuildNumber).isNull()
            assertThat(actual.isFirstActivationAsUpdate).isNull()
            assertThat(actual.dataSendingEnabled).isFalse
            assertThat(actual.maxReportsInDbCount).isNull()
            assertThat(actual.reportNativeCrashesEnabled).isNull()
            assertThat(actual.isRevenueAutoTrackingEnabled).isNull()
            assertThat(actual.reporterType).isEqualTo(reporterType)
            assertThat(actual.isAdvIdentifiersTrackingEnabled).isNull()
            assertAll()

        }
    }
}
