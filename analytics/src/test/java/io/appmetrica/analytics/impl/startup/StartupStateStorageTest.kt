package io.appmetrica.analytics.impl.startup

import io.appmetrica.analytics.assertions.ObjectPropertyAssertions
import io.appmetrica.analytics.coreapi.internal.data.ProtobufStateStorage
import io.appmetrica.analytics.impl.db.VitalCommonDataProvider
import io.appmetrica.analytics.impl.startup.StartupStateModel.StartupStateBuilder
import io.appmetrica.analytics.networktasks.internal.RetryPolicyConfig
import io.appmetrica.analytics.testutils.CommonTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class StartupStateStorageTest : CommonTest() {

    private val modelStorage: ProtobufStateStorage<StartupStateModel> = mock()
    private val vitalCommonDataProvider: VitalCommonDataProvider = mock()

    private val startupStateModelCaptor = argumentCaptor<StartupStateModel>()
    private lateinit var startupStateStorage: StartupState.Storage

    private val collectingFlags: CollectingFlags = mock()
    private val deviceId = "some device id"
    private val deviceIdHash = "some device id hash"
    private val uuid = "some uuid"
    private val reportUrls = listOf("report url 1", "report url 2")
    private val hostUrlsFromStartup = listOf("startup url 1", "startup url 2")
    private val hostUrlsFromClient = listOf("client url 1", "client url 2")
    private val diagnosticUrls = listOf("diagnostic url 1", "diagnostic url 2")
    private val getAdUrl = "get.ad.url"
    private val reportAdUrl = "report.ad.url"
    private val certificateUrl = "certificate.url"
    private val encodedClidsFromResponse = "clid0:0"
    private val lastClientClidsForStartupRequest = "clid1:1"
    private val lastChosenForRequestClids = "clid2:2"
    private val customSdkHosts = mapOf("am" to listOf("host1"), "ads" to listOf("host2"))
    private val obtainTime = 676578768787L
    private val obtainServerTime = 231341354L
    private val firstStartupServerTime = 666666777L
    private val hadFirstStartup = true
    private val startupDidNotOverrideClids = true
    private val outdated = true
    private val countryInit = "UK"
    private val statSending = StatSending(45)
    private val permissionsCollectingConfig = mock<PermissionsCollectingConfig>()
    private val retryPolicyConfig = mock<RetryPolicyConfig>()
    private val cacheControl = mock<CacheControl>()
    private val attributionConfig = mock<AttributionConfig>()
    private val startupUpdateConfig = mock<StartupUpdateConfig>()
    private val modulesRemoteConfigs = mapOf("some id" to mock<Any>())
    private val externalAttributionConfig = mock<ExternalAttributionConfig>()

    @Before
    fun setUp() {
        startupStateStorage = StartupState.Storage(modelStorage, vitalCommonDataProvider)
    }

    @Test
    fun readEmptyValue() {
        whenever(vitalCommonDataProvider.deviceId).thenReturn(null)
        whenever(vitalCommonDataProvider.deviceIdHash).thenReturn(null)
        whenever(modelStorage.read())
            .thenReturn(StartupStateBuilder(CollectingFlags.CollectingFlagsBuilder().build()).build())
        val startupState = startupStateStorage.read()

        ObjectPropertyAssertions(startupState)
            .withIgnoredFields("startupStateModel")
            .checkField("collectingFlags", CollectingFlags.CollectingFlagsBuilder().build())
            .checkFieldIsNull("deviceId")
            .checkFieldIsNull("deviceIdHash")
            .checkFieldIsNull("uuid")
            .checkFieldIsNull("reportUrls")
            .checkFieldIsNull("hostUrlsFromStartup")
            .checkFieldIsNull("hostUrlsFromClient")
            .checkFieldIsNull("diagnosticUrls")
            .checkFieldIsNull("getAdUrl")
            .checkFieldIsNull("reportAdUrl")
            .checkFieldIsNull("certificateUrl")
            .checkFieldIsNull("encodedClidsFromResponse")
            .checkFieldIsNull("lastClientClidsForStartupRequest")
            .checkFieldIsNull("lastChosenForRequestClids")
            .checkField("obtainTime", 0L)
            .checkField("obtainServerTime", 0L)
            .checkField("firstStartupServerTime", 0L)
            .checkField("hadFirstStartup", false)
            .checkField("startupDidNotOverrideClids", false)
            .checkField("outdated", false)
            .checkFieldIsNull("countryInit")
            .checkFieldIsNull("statSending")
            .checkFieldIsNull("permissionsCollectingConfig")
            .checkField(
                "retryPolicyConfig",
                RetryPolicyConfig(600, 1)
            )
            .checkFieldIsNull("cacheControl")
            .checkFieldIsNull("attributionConfig")
            .checkFieldIsNull("customSdkHosts")
            .checkFieldRecursively<StartupUpdateConfig>("startupUpdateConfig") {
                it.withPrivateFields(true).withFinalFieldOnly(false)
                it.checkField("intervalSeconds", 86400)
            }
            .checkField("modulesRemoteConfigs", emptyMap<String, Any>())
            .checkFieldIsNull("externalAttributionConfig")
            .checkAll()
    }

    @Test
    fun saveEmptyValue() {
        val startupState = StartupState.Builder(CollectingFlags.CollectingFlagsBuilder().build()).build()
        startupStateStorage.save(startupState)
        verify(vitalCommonDataProvider).deviceId = null
        verify(vitalCommonDataProvider).deviceIdHash = null
        verify(modelStorage).save(startupStateModelCaptor.capture())

        val assertions = ObjectPropertyAssertions(startupStateModelCaptor.firstValue)
            .withFinalFieldOnly(false)

        assertions.checkFieldIsNull("uuid")
        assertions.checkFieldIsNull("reportUrls")
        assertions.checkFieldIsNull("getAdUrl")
        assertions.checkFieldIsNull("reportAdUrl")
        assertions.checkFieldIsNull("certificateUrl")
        assertions.checkFieldIsNull("hostUrlsFromStartup")
        assertions.checkFieldIsNull("hostUrlsFromClient")
        assertions.checkFieldIsNull("diagnosticUrls")
        assertions.checkFieldIsNull("encodedClidsFromResponse")
        assertions.checkFieldIsNull("lastClientClidsForStartupRequest")
        assertions.checkFieldIsNull("lastChosenForRequestClids")
        assertions.checkField("obtainTime", 0L)
        assertions.checkField("hadFirstStartup", false)
        assertions.checkField("startupDidNotOverrideClids", false)
        assertions.checkFieldIsNull("countryInit")
        assertions.checkFieldIsNull("statSending")
        assertions.checkFieldIsNull("permissionsCollectingConfig")
        assertions.checkField("obtainServerTime", 0L)
        assertions.checkField("firstStartupServerTime", 0L)
        assertions.checkField("outdated", false)
        assertions.checkFieldComparingFieldByFieldRecursively(
            "retryPolicyConfig",
            RetryPolicyConfig(600, 1)
        )
        assertions.checkField("collectingFlags", CollectingFlags.CollectingFlagsBuilder().build())
        assertions.checkFieldIsNull("cacheControl")
        assertions.checkFieldIsNull("attributionConfig")
        assertions.checkFieldIsNull("customSdkHosts")
        assertions.checkFieldRecursively<StartupUpdateConfig>("startupUpdateConfig") {
            it.withPrivateFields(true).withFinalFieldOnly(false)
            it.checkField("intervalSeconds", 86400)
        }
        assertions.checkField("modulesRemoteConfigs", emptyMap<String, Any>())
        assertions.checkFieldIsNull("externalAttributionConfig")

        assertions.checkAll()
    }

    @Test
    fun readFilledValue() {
        whenever(vitalCommonDataProvider.deviceId).thenReturn(deviceId)
        whenever(vitalCommonDataProvider.deviceIdHash).thenReturn(deviceIdHash)

        val startupStateModel = StartupStateBuilder(collectingFlags)
            .withUuid(uuid)
            .withReportUrls(reportUrls)
            .withGetAdUrl(getAdUrl)
            .withReportAdUrl(reportAdUrl)
            .withCertificateUrl(certificateUrl)
            .withHostUrlsFromStartup(hostUrlsFromStartup)
            .withHostUrlsFromClient(hostUrlsFromClient)
            .withDiagnosticUrls(diagnosticUrls)
            .withEncodedClidsFromResponse(encodedClidsFromResponse)
            .withLastClientClidsForStartupRequest(lastClientClidsForStartupRequest)
            .withLastChosenForRequestClids(lastChosenForRequestClids)
            .withObtainTime(obtainTime)
            .withHadFirstStartup(hadFirstStartup)
            .withStartupDidNotOverrideClids(startupDidNotOverrideClids)
            .withCountryInit(countryInit)
            .withStatSending(statSending)
            .withPermissionsCollectingConfig(permissionsCollectingConfig)
            .withObtainServerTime(obtainServerTime)
            .withFirstStartupServerTime(firstStartupServerTime)
            .withOutdated(outdated)
            .withRetryPolicyConfig(retryPolicyConfig)
            .withCacheControl(cacheControl)
            .withAttributionConfig(attributionConfig)
            .withCustomSdkHosts(customSdkHosts)
            .withStartupUpdateConfig(startupUpdateConfig)
            .withModulesRemoteConfigs(modulesRemoteConfigs)
            .withExternalAttributionConfig(externalAttributionConfig)
            .build()
        whenever(modelStorage.read()).thenReturn(startupStateModel)

        ObjectPropertyAssertions(startupStateStorage.read())
            .withIgnoredFields("startupStateModel")
            .checkField("collectingFlags", collectingFlags)
            .checkField("deviceId", deviceId)
            .checkField("deviceIdHash", deviceIdHash)
            .checkField("uuid", uuid)
            .checkField("reportUrls", reportUrls)
            .checkField("hostUrlsFromStartup", hostUrlsFromStartup)
            .checkField("hostUrlsFromClient", hostUrlsFromClient)
            .checkField("diagnosticUrls", diagnosticUrls)
            .checkField("getAdUrl", getAdUrl)
            .checkField("reportAdUrl", reportAdUrl)
            .checkField("certificateUrl", certificateUrl)
            .checkField("encodedClidsFromResponse", encodedClidsFromResponse)
            .checkField("lastClientClidsForStartupRequest", lastClientClidsForStartupRequest)
            .checkField("lastChosenForRequestClids", lastChosenForRequestClids)
            .checkField("obtainTime", obtainTime)
            .checkField("obtainServerTime", obtainServerTime)
            .checkField("firstStartupServerTime", firstStartupServerTime)
            .checkField("hadFirstStartup", hadFirstStartup)
            .checkField("startupDidNotOverrideClids", startupDidNotOverrideClids)
            .checkField("outdated", outdated)
            .checkField("countryInit", countryInit)
            .checkField("statSending", statSending)
            .checkField("permissionsCollectingConfig", permissionsCollectingConfig)
            .checkField("retryPolicyConfig", retryPolicyConfig)
            .checkField("cacheControl", cacheControl)
            .checkField("attributionConfig", attributionConfig)
            .checkField("customSdkHosts", customSdkHosts)
            .checkField("startupUpdateConfig", startupUpdateConfig)
            .checkField("modulesRemoteConfigs", modulesRemoteConfigs)
            .checkField("externalAttributionConfig", externalAttributionConfig)
            .checkAll()
    }

    @Test
    fun saveFilledValue() {
        val startupState = StartupState.Builder(collectingFlags)
            .withFirstStartupServerTime(firstStartupServerTime)
            .withHadFirstStartup(hadFirstStartup)
            .withHostUrlsFromStartup(hostUrlsFromStartup)
            .withLastClientClidsForStartupRequest(lastClientClidsForStartupRequest)
            .withStartupDidNotOverrideClids(startupDidNotOverrideClids)
            .withAttributionConfig(attributionConfig)
            .withCacheControl(cacheControl)
            .withCertificateUrl(certificateUrl)
            .withCountryInit(countryInit)
            .withDeviceId(deviceId)
            .withDeviceIdHash(deviceIdHash)
            .withDiagnosticUrls(diagnosticUrls)
            .withEncodedClidsFromResponse(encodedClidsFromResponse)
            .withGetAdUrl(getAdUrl)
            .withHostUrlsFromClient(hostUrlsFromClient)
            .withLastChosenForRequestClids(lastChosenForRequestClids)
            .withObtainServerTime(obtainServerTime)
            .withObtainTime(obtainTime)
            .withOutdated(outdated)
            .withPermissionsCollectingConfig(permissionsCollectingConfig)
            .withReportAdUrl(reportAdUrl)
            .withReportUrls(reportUrls)
            .withRetryPolicyConfig(retryPolicyConfig)
            .withStatSending(statSending)
            .withUuid(uuid)
            .withCustomSdkHosts(customSdkHosts)
            .withStartupUpdateConfig(startupUpdateConfig)
            .withModulesRemoteConfigs(modulesRemoteConfigs)
            .withExternalAttributionConfig(externalAttributionConfig)
            .build()
        startupStateStorage.save(startupState)

        verify(vitalCommonDataProvider).deviceId = deviceId
        verify(vitalCommonDataProvider).deviceIdHash = deviceIdHash
        verify(modelStorage).save(startupStateModelCaptor.capture())

        val assertions = ObjectPropertyAssertions(startupStateModelCaptor.firstValue)
            .withFinalFieldOnly(false)

        assertions.checkField("uuid", uuid)
        assertions.checkField("reportUrls", reportUrls)
        assertions.checkField("getAdUrl", getAdUrl)
        assertions.checkField("reportAdUrl", reportAdUrl)
        assertions.checkField("certificateUrl", certificateUrl)
        assertions.checkField("hostUrlsFromStartup", hostUrlsFromStartup)
        assertions.checkField("hostUrlsFromClient", hostUrlsFromClient)
        assertions.checkField("diagnosticUrls", diagnosticUrls)
        assertions.checkField("encodedClidsFromResponse", encodedClidsFromResponse)
        assertions.checkField("lastClientClidsForStartupRequest", lastClientClidsForStartupRequest)
        assertions.checkField("lastChosenForRequestClids", lastChosenForRequestClids)
        assertions.checkField("obtainTime", obtainTime)
        assertions.checkField("hadFirstStartup", hadFirstStartup)
        assertions.checkField("startupDidNotOverrideClids", startupDidNotOverrideClids)
        assertions.checkField("countryInit", countryInit)
        assertions.checkField("statSending", statSending)
        assertions.checkField("permissionsCollectingConfig", permissionsCollectingConfig)
        assertions.checkField("obtainServerTime", obtainServerTime)
        assertions.checkField("firstStartupServerTime", firstStartupServerTime)
        assertions.checkField("outdated", outdated)
        assertions.checkField("retryPolicyConfig", retryPolicyConfig)
        assertions.checkField("collectingFlags", collectingFlags)
        assertions.checkField("cacheControl", cacheControl)
        assertions.checkField("attributionConfig", attributionConfig)
        assertions.checkField("customSdkHosts", customSdkHosts)
        assertions.checkField("startupUpdateConfig", startupUpdateConfig)
        assertions.checkField("modulesRemoteConfigs", modulesRemoteConfigs)
        assertions.checkField("externalAttributionConfig", externalAttributionConfig)

        assertions.checkAll()
    }
}
