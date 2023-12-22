package io.appmetrica.analytics.impl.startup

import io.appmetrica.analytics.assertions.ObjectPropertyAssertions
import io.appmetrica.analytics.billinginterface.internal.config.BillingConfig
import io.appmetrica.analytics.impl.DefaultValues
import io.appmetrica.analytics.networktasks.internal.RetryPolicyConfig
import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.mock

class StartupStateTest : CommonTest() {

    @Mock
    private lateinit var collectingFlags: CollectingFlags
    private val deviceId = "some device id"
    private val deviceIdHash = "some device id hash"
    private val uuid = "some uuid"
    private val reportUrls = listOf("report url 1", "report url 2")
    private val hostUrlsFromStartup = listOf("startup url 1", "startup url 2")
    private val hostUrlsFromClient = listOf("client url 1", "client url 2")
    private val diagnosticUrls = listOf("diagnostic url 1", "diagnostic url 2")
    private val customSdkHosts = mapOf("key1" to listOf("host1"), "key2" to listOf("host2"))
    private val getAdUrl = "get.ad.url"
    private val reportAdUrl = "report.ad.url"
    private val certificateUrl = "certificate.url"
    private val encodedClidsFromResponse = "clid0:0"
    private val lastClientClidsForStartupRequest = "clid1:1"
    private val lastChosenForRequestClids = "clid2:2"
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
    private val autoInappCollectingConfig = mock<BillingConfig>()
    private val cacheControl = mock<CacheControl>()
    private val attributionConfig = mock<AttributionConfig>()
    private val startupUpdateConfig = mock<StartupUpdateConfig>()
    private val modulesRemoteConfigs = mapOf("first module" to mock<Any>())
    private val externalAttributionConfig = mock<ExternalAttributionConfig>()

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
    }

    @Test
    fun emptyObject() {
        val startupState = StartupState.Builder(collectingFlags).build()
        ObjectPropertyAssertions(startupState)
            .withIgnoredFields("startupStateModel")
            .checkField("collectingFlags", collectingFlags)
            .checkFieldIsNull("deviceId")
            .checkFieldIsNull("deviceIdHash")
            .checkFieldIsNull("uuid")
            .checkFieldIsNull("reportUrls")
            .checkFieldIsNull("hostUrlsFromStartup")
            .checkFieldIsNull("hostUrlsFromClient")
            .checkFieldIsNull("diagnosticUrls")
            .checkFieldIsNull("customSdkHosts")
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
            .checkField("retryPolicyConfig",
                RetryPolicyConfig(600, 1)
            )
            .checkFieldIsNull("autoInappCollectingConfig")
            .checkFieldIsNull("cacheControl")
            .checkFieldIsNull("attributionConfig")
            .checkFieldComparingFieldByFieldRecursively("startupUpdateConfig",
                StartupUpdateConfig(DefaultValues.STARTUP_UPDATE_CONFIG.interval))
            .checkField("modulesRemoteConfigs", emptyMap<String, Any>())
            .checkFieldIsNull("externalAttributionConfig")
            .checkAll()
    }

    @Test
    fun filledObject() {
        val startupState = StartupState.Builder(collectingFlags)
            .withFirstStartupServerTime(firstStartupServerTime)
            .withHadFirstStartup(hadFirstStartup)
            .withHostUrlsFromStartup(hostUrlsFromStartup)
            .withLastClientClidsForStartupRequest(lastClientClidsForStartupRequest)
            .withStartupDidNotOverrideClids(startupDidNotOverrideClids)
            .withAttributionConfig(attributionConfig)
            .withAutoInappCollectingConfig(autoInappCollectingConfig)
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
        ObjectPropertyAssertions(startupState)
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
            .checkField("autoInappCollectingConfig", autoInappCollectingConfig)
            .checkField("cacheControl", cacheControl)
            .checkField("attributionConfig", attributionConfig)
            .checkField("customSdkHosts", customSdkHosts)
            .checkField("startupUpdateConfig", startupUpdateConfig)
            .checkField("modulesRemoteConfigs", modulesRemoteConfigs)
            .checkField("externalAttributionConfig", externalAttributionConfig)
            .checkAll()
    }

    @Test
    fun buildUpon() {
        val startupState = StartupState.Builder(collectingFlags)
            .withFirstStartupServerTime(firstStartupServerTime)
            .withHadFirstStartup(hadFirstStartup)
            .withHostUrlsFromStartup(hostUrlsFromStartup)
            .withLastClientClidsForStartupRequest(lastClientClidsForStartupRequest)
            .withStartupDidNotOverrideClids(startupDidNotOverrideClids)
            .withAttributionConfig(attributionConfig)
            .withAutoInappCollectingConfig(autoInappCollectingConfig)
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
            .buildUpon()
            .build()
        ObjectPropertyAssertions(startupState)
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
            .checkField("autoInappCollectingConfig", autoInappCollectingConfig)
            .checkField("cacheControl", cacheControl)
            .checkField("attributionConfig", attributionConfig)
            .checkField("customSdkHosts", customSdkHosts)
            .checkField("startupUpdateConfig", startupUpdateConfig)
            .checkField("modulesRemoteConfigs", modulesRemoteConfigs)
            .checkField("externalAttributionConfig", externalAttributionConfig)
            .checkAll()
    }

    @Test
    fun allModelFieldsAreUsed() {
        val excludedStateFields = setOf("startupStateModel", "deviceId", "deviceIdHash")
        val excludedModelFields = setOf("deviceID", "deviceIDHash")
        val stateFields = StartupState::class.java.declaredFields
        val modelFields = StartupStateModel::class.java.declaredFields
        assertThat(stateFields.map { it.name }.filterNot { excludedStateFields.contains(it) })
            .containsExactlyInAnyOrderElementsOf(modelFields.map { it.name }.filterNot { excludedModelFields.contains(it) })
    }

}
