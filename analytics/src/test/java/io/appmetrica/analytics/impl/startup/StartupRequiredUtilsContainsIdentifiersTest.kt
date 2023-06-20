package io.appmetrica.analytics.impl.startup

import io.appmetrica.analytics.coreutils.internal.time.SystemTimeProvider
import io.appmetrica.analytics.coreutils.internal.time.TimeProvider
import io.appmetrica.analytics.impl.ClidsInfoStorage
import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.stubbing

@RunWith(Parameterized::class)
internal class StartupRequiredUtilsContainsIdentifiersTest(
    private val startup: StartupState,
    private val identifiers: List<String>,
    private val doClidsMatch: Boolean,
    private val currentTimeSeconds: Long,
    private val expected: Boolean
) : CommonTest() {

    private val timeProvider = mock<TimeProvider>()
    private val clidsStateChecker = mock<ClidsStateChecker>()
    private val clientClids = mapOf("clid0" to "0")
    private val clidsInfoStorage = mock<ClidsInfoStorage>()

    companion object {

        private val customIdentifiers = listOf("custom1", "custom2")
        private val featureIdentifiers = listOf(Constants.StartupParamsCallbackKeys.FEATURE_LIB_SSL_ENABLED)
        private val allIdentifiers = listOf(
            Constants.StartupParamsCallbackKeys.UUID,
            Constants.StartupParamsCallbackKeys.DEVICE_ID,
            Constants.StartupParamsCallbackKeys.DEVICE_ID_HASH,
            Constants.StartupParamsCallbackKeys.REPORT_AD_URL,
            Constants.StartupParamsCallbackKeys.GET_AD_URL,
            Constants.StartupParamsCallbackKeys.CLIDS,
        )
        private val obtainTime = 777888L
        private val updateInterval = 400
        private val greaterCurrentTime = obtainTime + updateInterval + 1
        private val lessCurrentTime = obtainTime + updateInterval - 1
        private val basicStartup = StartupState.Builder(CollectingFlags.CollectingFlagsBuilder().build())
            .withObtainTime(obtainTime)
            .withStartupUpdateConfig(StartupUpdateConfig(updateInterval))
            .build()

        private fun basicStartupWith(block: (StartupState.Builder) -> Unit): StartupState {
            val startupBuilder = StartupState.Builder(CollectingFlags.CollectingFlagsBuilder().withSslPinning(true).build())
            startupBuilder
                .withObtainTime(obtainTime)
                .withStartupUpdateConfig(StartupUpdateConfig(updateInterval))
            block(startupBuilder)
            return startupBuilder.build()
        }

        private fun startupWithEmptyFieldsWith(block: (StartupState.Builder) -> Unit): StartupState {
            val startupBuilder = StartupState.Builder(CollectingFlags.CollectingFlagsBuilder().build())
                .withObtainTime(obtainTime)
                .withStartupUpdateConfig(StartupUpdateConfig(updateInterval))
                .withUuid("")
                .withDeviceId("")
                .withDeviceIdHash("")
                .withReportAdUrl("")
                .withGetAdUrl("")
            block(startupBuilder)
            return startupBuilder.build()
        }

        private fun filledStartupWith(block: (StartupState.Builder) -> Unit): StartupState {
            val startupBuilder = StartupState.Builder(CollectingFlags.CollectingFlagsBuilder().build())
                .withObtainTime(obtainTime)
                .withStartupUpdateConfig(StartupUpdateConfig(updateInterval))
                .withUuid("uuid")
                .withDeviceId("device_id")
                .withDeviceIdHash("device_id_hash")
                .withReportAdUrl("report_ad_url")
                .withGetAdUrl("get_ad_url")
            block(startupBuilder)
            return startupBuilder.build()
        }

        @JvmStatic
        @Parameterized.Parameters
        fun data(): Collection<Array<Any>> = listOf(
            // #0
            arrayOf(basicStartup, emptyList<String>(), false, greaterCurrentTime, true),
            arrayOf(basicStartup, listOf(Constants.StartupParamsCallbackKeys.UUID), true, lessCurrentTime, false),
            arrayOf(basicStartup, listOf(Constants.StartupParamsCallbackKeys.DEVICE_ID), true, lessCurrentTime, false),
            arrayOf(basicStartup, listOf(Constants.StartupParamsCallbackKeys.DEVICE_ID_HASH), true, lessCurrentTime, false),
            arrayOf(basicStartup, listOf(Constants.StartupParamsCallbackKeys.REPORT_AD_URL), true, lessCurrentTime, false),
            // #5
            arrayOf(basicStartup, listOf(Constants.StartupParamsCallbackKeys.GET_AD_URL), true, lessCurrentTime, false),
            arrayOf(basicStartup, listOf(Constants.StartupParamsCallbackKeys.CLIDS), true, greaterCurrentTime, true),
            arrayOf(basicStartup, listOf(Constants.StartupParamsCallbackKeys.CLIDS), false, lessCurrentTime, false),
            arrayOf(startupWithEmptyFieldsWith {  }, listOf(Constants.StartupParamsCallbackKeys.UUID), true, lessCurrentTime, false),
            arrayOf(startupWithEmptyFieldsWith {  }, listOf(Constants.StartupParamsCallbackKeys.DEVICE_ID), true, lessCurrentTime, false),
            // #10
            arrayOf(startupWithEmptyFieldsWith {  }, listOf(Constants.StartupParamsCallbackKeys.DEVICE_ID_HASH), true, lessCurrentTime, false),
            arrayOf(startupWithEmptyFieldsWith {  }, listOf(Constants.StartupParamsCallbackKeys.REPORT_AD_URL), true, lessCurrentTime, false),
            arrayOf(startupWithEmptyFieldsWith {  }, listOf(Constants.StartupParamsCallbackKeys.GET_AD_URL), true, lessCurrentTime, false),
            arrayOf(filledStartupWith { it.withUuid(null) }, listOf(Constants.StartupParamsCallbackKeys.UUID), true, lessCurrentTime, false),
            arrayOf(filledStartupWith { it.withDeviceId(null) }, listOf(Constants.StartupParamsCallbackKeys.DEVICE_ID), true, lessCurrentTime, false),
            // #15
            arrayOf(filledStartupWith { it.withDeviceIdHash(null) }, listOf(Constants.StartupParamsCallbackKeys.DEVICE_ID_HASH), true, lessCurrentTime, false),
            arrayOf(filledStartupWith { it.withReportAdUrl(null) }, listOf(Constants.StartupParamsCallbackKeys.REPORT_AD_URL), true, lessCurrentTime, false),
            arrayOf(filledStartupWith { it.withGetAdUrl(null) }, listOf(Constants.StartupParamsCallbackKeys.GET_AD_URL), true, lessCurrentTime, false),
            arrayOf(basicStartupWith { it.withUuid("uuid") }, listOf(Constants.StartupParamsCallbackKeys.UUID), false, greaterCurrentTime, true),
            arrayOf(basicStartupWith { it.withDeviceId("device_id") }, listOf(Constants.StartupParamsCallbackKeys.DEVICE_ID), false, greaterCurrentTime, true),
            // #20
            arrayOf(basicStartupWith { it.withDeviceIdHash("device_id_hash") }, listOf(Constants.StartupParamsCallbackKeys.DEVICE_ID_HASH), false, greaterCurrentTime, true),
            arrayOf(basicStartupWith { it.withReportAdUrl("reports_ad_url") }, listOf(Constants.StartupParamsCallbackKeys.REPORT_AD_URL), false, greaterCurrentTime, true),
            arrayOf(basicStartupWith { it.withGetAdUrl("get_ad_url") }, listOf(Constants.StartupParamsCallbackKeys.GET_AD_URL), false, greaterCurrentTime, true),
            arrayOf(filledStartupWith { it.withUuid(null) }, allIdentifiers, true, lessCurrentTime, false),
            arrayOf(filledStartupWith { it.withDeviceId(null) }, allIdentifiers, true, lessCurrentTime, false),
            // #25
            arrayOf(filledStartupWith { it.withDeviceIdHash(null) }, allIdentifiers, true, lessCurrentTime, false),
            arrayOf(filledStartupWith { it.withReportAdUrl(null) }, allIdentifiers, true, lessCurrentTime, false),
            arrayOf(filledStartupWith { it.withGetAdUrl(null) }, allIdentifiers, true, lessCurrentTime, false),
            arrayOf(filledStartupWith {}, allIdentifiers, true, lessCurrentTime, true),
            arrayOf(filledStartupWith {}, allIdentifiers, false, lessCurrentTime, false),
            // #30
            arrayOf(basicStartup, customIdentifiers, true, lessCurrentTime, true),
            arrayOf(basicStartup, customIdentifiers, true, greaterCurrentTime, false),
            arrayOf(basicStartupWith { it.withOutdated(true) }, customIdentifiers, true, lessCurrentTime, false),
            arrayOf(filledStartupWith {  }, allIdentifiers + customIdentifiers, true, greaterCurrentTime, false),
            arrayOf(filledStartupWith {  }, allIdentifiers + customIdentifiers, false, lessCurrentTime, false),
            // #35
            arrayOf(filledStartupWith { it.withOutdated(true) }, allIdentifiers + customIdentifiers, true, lessCurrentTime, false),
            arrayOf(filledStartupWith { it.withUuid(null) }, allIdentifiers + customIdentifiers, true, lessCurrentTime, false),
            arrayOf(filledStartupWith { it.withDeviceId(null) }, allIdentifiers + customIdentifiers, true, lessCurrentTime, false),
            arrayOf(filledStartupWith { it.withDeviceIdHash(null) }, allIdentifiers + customIdentifiers, true, lessCurrentTime, false),
            arrayOf(filledStartupWith { it.withReportAdUrl(null) }, allIdentifiers + customIdentifiers, true, lessCurrentTime, false),
            // #40
            arrayOf(filledStartupWith { it.withGetAdUrl(null) }, allIdentifiers + customIdentifiers, true, lessCurrentTime, false),
            arrayOf(filledStartupWith {  }, allIdentifiers + customIdentifiers, true, lessCurrentTime, true),
            arrayOf(basicStartup, featureIdentifiers, true, lessCurrentTime, true),
            arrayOf(basicStartup, featureIdentifiers, true, greaterCurrentTime, false),
            arrayOf(basicStartupWith { it.withOutdated(true) }, featureIdentifiers, true, lessCurrentTime, false),
            // #45
            arrayOf(filledStartupWith {  }, allIdentifiers + featureIdentifiers, true, greaterCurrentTime, false),
            arrayOf(filledStartupWith {  }, allIdentifiers + featureIdentifiers, false, lessCurrentTime, false),
            arrayOf(filledStartupWith { it.withOutdated(true) }, allIdentifiers + featureIdentifiers, true, lessCurrentTime, false),
            arrayOf(filledStartupWith { it.withUuid(null) }, allIdentifiers + featureIdentifiers, true, lessCurrentTime, false),
            arrayOf(filledStartupWith { it.withDeviceId(null) }, allIdentifiers + featureIdentifiers, true, lessCurrentTime, false),
            // #50
            arrayOf(filledStartupWith { it.withDeviceIdHash(null) }, allIdentifiers + featureIdentifiers, true, lessCurrentTime, false),
            arrayOf(filledStartupWith { it.withReportAdUrl(null) }, allIdentifiers + featureIdentifiers, true, lessCurrentTime, false),
            arrayOf(filledStartupWith { it.withGetAdUrl(null) }, allIdentifiers + featureIdentifiers, true, lessCurrentTime, false),
            arrayOf(filledStartupWith {  }, allIdentifiers + featureIdentifiers, true, lessCurrentTime, true),
            arrayOf(filledStartupWith { it.withOutdated(true) }, allIdentifiers + customIdentifiers + featureIdentifiers, true, lessCurrentTime, false),
            // #55
            arrayOf(filledStartupWith { it.withUuid(null) }, allIdentifiers + customIdentifiers + featureIdentifiers, true, lessCurrentTime, false),
            arrayOf(filledStartupWith { it.withDeviceId(null) }, allIdentifiers + customIdentifiers + featureIdentifiers, true, lessCurrentTime, false),
            arrayOf(filledStartupWith { it.withDeviceIdHash(null) }, allIdentifiers + customIdentifiers + featureIdentifiers, true, lessCurrentTime, false),
            arrayOf(filledStartupWith { it.withReportAdUrl(null) }, allIdentifiers + customIdentifiers + featureIdentifiers, true, lessCurrentTime, false),
            arrayOf(filledStartupWith { it.withGetAdUrl(null) }, allIdentifiers + customIdentifiers + featureIdentifiers, true, lessCurrentTime, false),
            // #60
            arrayOf(filledStartupWith {  }, allIdentifiers + customIdentifiers + featureIdentifiers, true, lessCurrentTime, true),
        )
    }

    @Before
    fun setUp() {
        StartupRequiredUtils.timeProvider = timeProvider
        StartupRequiredUtils.clidsStateChecker = clidsStateChecker
        stubbing(timeProvider) {
            on { currentTimeSeconds() } doReturn currentTimeSeconds
        }
        stubbing(clidsStateChecker) {
            on { doChosenClidsForRequestMatchLastRequestClids(clientClids, startup, clidsInfoStorage) } doReturn doClidsMatch
        }
    }

    @After
    fun tearDown() {
        StartupRequiredUtils.timeProvider = SystemTimeProvider()
        StartupRequiredUtils.clidsStateChecker = ClidsStateChecker()
    }

    @Test
    fun containsIdentifiers() {
        val actual = StartupRequiredUtils.containsIdentifiers(startup, identifiers, clientClids) { clidsInfoStorage }
        assertThat(actual).isEqualTo(expected)
    }
}
