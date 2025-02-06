package io.appmetrica.analytics.impl.startup

import io.appmetrica.analytics.AppMetricaConfig
import io.appmetrica.analytics.assertions.ObjectPropertyAssertions
import io.appmetrica.analytics.billinginterface.internal.config.BillingConfig
import io.appmetrica.analytics.impl.DistributionSource
import io.appmetrica.analytics.impl.StartupTask
import io.appmetrica.analytics.impl.clids.ClidsInfo
import io.appmetrica.analytics.impl.client.ClientConfiguration
import io.appmetrica.analytics.impl.client.ProcessConfiguration
import io.appmetrica.analytics.impl.request.StartupRequestConfig
import io.appmetrica.analytics.impl.startup.parsing.StartupParser
import io.appmetrica.analytics.impl.startup.parsing.StartupResult
import io.appmetrica.analytics.impl.utils.StartupUtils
import io.appmetrica.analytics.internal.CounterConfiguration
import io.appmetrica.analytics.networktasks.internal.NetworkTask
import io.appmetrica.analytics.networktasks.internal.RetryPolicyConfig
import io.appmetrica.analytics.testutils.constructionRule
import net.bytebuddy.utility.RandomString
import org.assertj.core.api.Assertions.assertThat
import org.json.JSONObject
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import java.util.UUID

@RunWith(RobolectricTestRunner::class)
internal class StartupUnitGeneralTest : StartupUnitBaseTest() {

    @get:Rule
    val startupTaskMockedConstructionRule = constructionRule<NetworkTask>()

    @Before
    override fun setup() {
        super.setup()
    }

    @Test
    fun `getOrCreateStartupTaskIfRequired twice`() {
        val first = startupUnit.getOrCreateStartupTaskIfRequired()!!
        whenever(first.isRemoved).thenReturn(false)
        val second = startupUnit.getOrCreateStartupTaskIfRequired()
        assertThat(first).isSameAs(second)
    }

    @Test
    fun `getOrCreateStartupTaskIfRequired twice if first in removed state`() {
        val first = startupUnit.getOrCreateStartupTaskIfRequired()!!
        whenever(first.isRemoved).thenReturn(true)
        val second = startupUnit.getOrCreateStartupTaskIfRequired()
        assertThat(first).isNotSameAs(second)
    }

    @Test
    fun constructorWithCustomHosts() {
        val hosts = listOf("first", "second")
        val counterConfiguration = CounterConfiguration()
        counterConfiguration.applyFromConfig(
            AppMetricaConfig.newConfigBuilder(UUID.randomUUID().toString())
                .withCustomHosts(hosts)
                .build()
        )
        val processConfiguration = ProcessConfiguration(
            RuntimeEnvironment.getApplication(),
            dataResultReceiver
        )
        processConfiguration.customHosts = hosts
        val arguments = StartupRequestConfig.Arguments(
            ClientConfiguration(
                processConfiguration,
                counterConfiguration
            )
        )
        whenever(startupUnitComponents.requestConfigArguments)
            .thenReturn(arguments)
        whenever(startupUnitComponents.startupStateStorage).thenReturn(startupStateStorage)
        val startupUnit = StartupUnit(startupUnitComponents)
        startupUnit.init()
        val startupStateCaptor = argumentCaptor<StartupState>()
        verify(startupStateStorage).save(startupStateCaptor.capture())
        assertThat(startupStateCaptor.firstValue.hostUrlsFromClient).containsAll(hosts)
    }

    @Test
    fun retryPolicyConfig() {
        val startupState = startupUnit.parseStartupResult(
            StartupParser().parseStartupResponse(
                TEST_RESPONSE.toByteArray()
            ), startupRequestConfig, 0L
        )
        assertThat(startupState.retryPolicyConfig).isEqualTo(RetryPolicyConfig(1000, 2))
    }

    @Test
    fun retryPolicyConfigNull() {
        val responseWithoutRetryPolicy = JSONObject(TEST_RESPONSE)
        responseWithoutRetryPolicy.remove("retry_policy")
        val startupState = startupUnit.parseStartupResult(
            StartupParser().parseStartupResponse(responseWithoutRetryPolicy.toString().toByteArray()),
            startupRequestConfig,
            0L
        )
        assertThat(startupState.retryPolicyConfig).isEqualTo(RetryPolicyConfig(600, 1))
    }

    @Test
    fun startupError() {
        val startupState = mock<StartupState>()
        whenever(startupUnitComponents.startupConfigurationHolder.startupState).thenReturn(startupState)
        val error = StartupError.NETWORK
        startupUnit.onRequestError(error)
        verify(startupResultListener)
            .onStartupError(RuntimeEnvironment.getApplication().packageName, error, startupState)
    }

    @Test
    fun parseStartupResult() {
        val result = mock<StartupResult>()
        val startupRequestConfig = mock<StartupRequestConfig>()
        val serverTime: Long = 324653726
        val uuid = RandomString.make(10)
        val deviceId = RandomString.make(10)
        val deviceIdHash = RandomString.make(10)
        val reportUrls: List<String> = mutableListOf("report url")
        val diagnosticUrls: List<String> = mutableListOf("diagnostic url")
        val hostUrlsFromStartup: List<String> = mutableListOf("startup url")
        val hostUrlsFromClient: List<String> = mutableListOf("client startup url")
        val getAdUrl = "some.get.tst.url"
        val reportAdUrl = "some.report.tst.url"
        val certificateUrl = "certificate.url"
        val encodedClids = "clid0:0,clid1:1"
        val collectingFlags = mock<CollectingFlags>()
        val clientClids: MutableMap<String, String> = HashMap()
        clientClids["clid0"] = "10"
        val chosenClids: MutableMap<String, String> = HashMap()
        chosenClids["clid1"] = "20"
        val customSdkHosts: MutableMap<String, List<String>> = HashMap()
        customSdkHosts["key1"] = mutableListOf("host1", "host2")
        customSdkHosts["key2"] = mutableListOf("host3", "host4")
        val countryInit = "by"
        val statSending = mock<StatSending>()
        val permissionsCollectingConfig = mock<PermissionsCollectingConfig>()
        val retryPolicyConfig = mock<RetryPolicyConfig>()
        val firstStartupObtainTime: Long = 4385768
        val autoInappCollectingConfig = mock<BillingConfig>()
        val obtainTime = 124326487324L
        doReturn(obtainTime).whenever(timeProvider).currentTimeSeconds()
        val cacheControl = mock<CacheControl>()
        val attributionConfig = mock<AttributionConfig>()
        val startupUpdateConfig = mock<StartupUpdateConfig>()
        val modulesRemoteConfigs = mapOf("String" to mock<Any>())
        val externalAttributionConfig = mock<ExternalAttributionConfig>()
        whenever(result.deviceId).thenReturn(deviceId)
        whenever(result.deviceIDHash).thenReturn(deviceIdHash)
        val oldStartupRequestConfig = mock<StartupRequestConfig>()
        whenever(oldStartupRequestConfig.getOrSetFirstStartupTime(any())).thenReturn(firstStartupObtainTime)
        whenever(startupConfigurationHolder.get()).thenReturn(oldStartupRequestConfig)
        whenever(startupConfigurationHolder.startupState).thenReturn(
            StartupState.Builder(mock<CollectingFlags>()).withUuid(uuid).build())
        whenever(result.reportHostUrls).thenReturn(reportUrls)
        whenever(result.getAdUrl).thenReturn(getAdUrl)
        whenever(result.reportAdUrl).thenReturn(reportAdUrl)
        whenever(result.certificateUrl).thenReturn(certificateUrl)
        whenever(result.startupUrls).thenReturn(hostUrlsFromStartup)
        whenever(result.diagnosticUrls).thenReturn(diagnosticUrls)
        whenever(startupRequestConfig.startupHostsFromClient).thenReturn(hostUrlsFromClient)
        whenever(result.encodedClids).thenReturn(encodedClids)
        whenever(result.collectionFlags).thenReturn(collectingFlags)
        whenever(startupRequestConfig.clidsFromClient).thenReturn(clientClids)
        whenever(startupRequestConfig.chosenClids)
            .thenReturn(ClidsInfo.Candidate(chosenClids, DistributionSource.APP))
        whenever(result.countryInit).thenReturn(countryInit)
        whenever(result.statSending).thenReturn(statSending)
        whenever(result.permissionsCollectingConfig).thenReturn(permissionsCollectingConfig)
        whenever(result.retryPolicyConfig).thenReturn(retryPolicyConfig)
        whenever(result.cacheControl).thenReturn(cacheControl)
        whenever(result.autoInappCollectingConfig).thenReturn(autoInappCollectingConfig)
        whenever(result.attributionConfig).thenReturn(attributionConfig)
        whenever(result.customSdkHosts).thenReturn(customSdkHosts)
        whenever(result.startupUpdateConfig).thenReturn(startupUpdateConfig)
        whenever(result.modulesRemoteConfigs).thenReturn(modulesRemoteConfigs)
        whenever(result.externalAttributionConfig).thenReturn(externalAttributionConfig)
        val startupState = startupUnit.parseStartupResult(result, startupRequestConfig, serverTime)
        val assertions = ObjectPropertyAssertions(startupState)
            .withIgnoredFields("obtainTime", "startupStateModel")
        assertions.checkField("uuid", uuid)
        assertions.checkField("deviceId", deviceId)
        assertions.checkField("deviceIdHash", deviceIdHash)
        assertions.checkField("reportUrls", reportUrls)
        assertions.checkField("getAdUrl", getAdUrl)
        assertions.checkField("reportAdUrl", reportAdUrl)
        assertions.checkField("certificateUrl", certificateUrl)
        assertions.checkField("hostUrlsFromStartup", hostUrlsFromStartup)
        assertions.checkField("hostUrlsFromClient", hostUrlsFromClient)
        assertions.checkField("diagnosticUrls", diagnosticUrls)
        assertions.checkField("encodedClidsFromResponse", encodedClids)
        assertions.checkField(
            "lastClientClidsForStartupRequest",
            StartupUtils.encodeClids(clientClids)
        )
        assertions.checkField("lastChosenForRequestClids", StartupUtils.encodeClids(chosenClids))
        assertions.checkField("collectingFlags", collectingFlags)
        assertions.checkField("hadFirstStartup", true)
        assertions.checkField("startupDidNotOverrideClids", false)
        assertions.checkField("countryInit", countryInit)
        assertions.checkField("statSending", statSending)
        assertions.checkField("permissionsCollectingConfig", permissionsCollectingConfig)
        assertions.checkField("retryPolicyConfig", retryPolicyConfig)
        assertions.checkField("obtainServerTime", serverTime)
        assertions.checkField("firstStartupServerTime", firstStartupObtainTime)
        assertions.checkField("outdated", false)
        assertions.checkField("cacheControl", cacheControl)
        assertions.checkField("autoInappCollectingConfig", autoInappCollectingConfig)
        assertions.checkField("attributionConfig", attributionConfig)
        assertions.checkField("customSdkHosts", customSdkHosts)
        assertions.checkField("startupUpdateConfig", startupUpdateConfig)
        assertions.checkField("modulesRemoteConfigs", modulesRemoteConfigs)
        assertions.checkField("externalAttributionConfig", externalAttributionConfig)
        assertions.checkAll()
        assertThat(startupState.obtainTime).isEqualTo(obtainTime)
    }

    @Test
    fun hadFirstStartup() {
        val result = mock<StartupResult>()
        doReturn(mock<CollectingFlags>()).whenever(result).collectionFlags
        assertThat(startupUnit.parseStartupResult(result, mockedStartupRequestConfig(), 0L).hadFirstStartup)
            .isTrue()
    }

    @Test
    fun serverTime() {
        val result = mock<StartupResult>()
        doReturn(mock<CollectingFlags>()).whenever(result).collectionFlags
        assertThat(
            startupUnit.parseStartupResult(result, mockedStartupRequestConfig(), obtainServerTime).obtainServerTime
        ).isEqualTo(obtainServerTime)
    }

    @Test
    fun firstStartupServerTime() {
        val result = mock<StartupResult>()
        doReturn(mock<CollectingFlags>()).whenever(result).collectionFlags
        whenever(startupRequestConfig.getOrSetFirstStartupTime(any())).thenReturn(firstStartupServerTime)
        assertThat(
            startupUnit.parseStartupResult(result, mockedStartupRequestConfig(), 0L).firstStartupServerTime
        ).isEqualTo(firstStartupServerTime)
    }

    @Test
    fun outdatedStartup() {
        startupConfigurationHolder.updateStartupState(
            startupConfigurationHolder.startupState
                .buildUpon()
                .withOutdated(true)
                .build()
        )
        assertThat(startupUnit.isStartupRequired()).isTrue()
    }

    @Test
    fun uuidNotParsed() {
        val uuid = "uuid"
        whenever(startupConfigurationHolder.startupState)
            .thenReturn(StartupState.Builder(mock<CollectingFlags>()).withUuid(uuid).build())
        val result = mock<StartupResult>()
        whenever(result.collectionFlags).thenReturn(mock<CollectingFlags>())
        val startupState = startupUnit.parseStartupResult(result, startupRequestConfig, 0L)
        assertThat(startupState.uuid).isEqualTo(uuid)
    }

    @Test
    fun emptyDeviceIDReplacedWithDefault() {
        val deviceID = "deviceID"
        whenever(startupConfigurationHolder.startupState)
            .thenReturn(StartupState.Builder(mock<CollectingFlags>()).withDeviceId(deviceID).build()
            )
        startupUnit.init()
        val result = mock<StartupResult>()
        doReturn(null).whenever(result).deviceId
        doReturn(mock<CollectingFlags>()).whenever(result).collectionFlags
        val startupState = startupUnit.parseStartupResult(result, startupRequestConfig, 0L)
        assertThat(startupState.deviceId).isEqualTo(deviceID)
    }

    @Test
    fun deviceIDNotParsedIfPresent() {
        val deviceIDOld = "deviceIDOld"
        val deviceIDNew = "deviceIDNew"
        whenever(startupConfigurationHolder.startupState)
            .thenReturn(StartupState.Builder(mock<CollectingFlags>()).withDeviceId(deviceIDOld).build())
        val result = mock<StartupResult>()
        doReturn(deviceIDNew).whenever(result).deviceId
        doReturn(mock<CollectingFlags>()).whenever(result).collectionFlags
        val startupState = startupUnit.parseStartupResult(result, startupRequestConfig, 0L)
        assertThat(startupState.deviceId).isEqualTo(deviceIDOld)
    }

    @Test
    fun useDeviceIDHashFromResponse() {
        val deviceIDHash = "deviceIDHash"
        val deviceIDHash2 = "deviceIDHash2"
        startupConfigurationHolder.updateStartupState(
            startupConfigurationHolder.startupState
                .buildUpon()
                .withDeviceIdHash(deviceIDHash)
                .build()
        )
        val result = mock<StartupResult>()
        doReturn(deviceIDHash2).whenever(result).deviceIDHash
        whenever(result.collectionFlags).thenReturn(mock<CollectingFlags>())
        val startupState = startupUnit.parseStartupResult(result, startupRequestConfig, 0L)
        assertThat(startupState.deviceIdHash).isEqualTo(deviceIDHash2)
    }

    private fun mockedStartupRequestConfig(): StartupRequestConfig {
        val config = mock<StartupRequestConfig>()
        whenever(config.chosenClids).thenReturn(mock<ClidsInfo.Candidate>())
        return config
    }
}
