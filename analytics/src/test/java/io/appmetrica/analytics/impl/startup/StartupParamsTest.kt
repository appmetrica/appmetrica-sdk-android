package io.appmetrica.analytics.impl.startup

import io.appmetrica.analytics.AdvIdentifiersResult
import io.appmetrica.analytics.StartupParamsItem
import io.appmetrica.analytics.StartupParamsItemStatus
import io.appmetrica.analytics.TestData
import io.appmetrica.analytics.coreapi.internal.identifiers.IdentifierStatus
import io.appmetrica.analytics.impl.ClientIdentifiersHolder
import io.appmetrica.analytics.impl.FeaturesResult
import io.appmetrica.analytics.impl.db.preferences.PreferencesClientDbStorage
import io.appmetrica.analytics.impl.selfreporting.AppMetricaSelfReportFacade
import io.appmetrica.analytics.impl.selfreporting.SelfReporterWrapper
import io.appmetrica.analytics.impl.startup.uuid.MultiProcessSafeUuidProvider
import io.appmetrica.analytics.impl.startup.uuid.UuidValidator
import io.appmetrica.analytics.impl.utils.JsonHelper
import io.appmetrica.analytics.impl.utils.StartupUtils
import io.appmetrica.analytics.internal.IdentifiersResult
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.ContextRule
import io.appmetrica.analytics.testutils.on
import io.appmetrica.analytics.testutils.staticRule
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.SoftAssertions
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.clearInvocations
import org.mockito.Mockito.never
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.UUID

internal class StartupParamsTest : CommonTest() {
    private val testAdGetUrl = IdentifiersResult("Test adUrlGet", IdentifierStatus.OK, null)
    private val testAdUrlReport = IdentifiersResult("Test adUrlReport", IdentifierStatus.OK, null)
    private val testServerSideOffset = -280L
    private val responseClids: MutableMap<String, String> = HashMap()
    private val requestClids: MutableMap<String, String> = HashMap()

    private val validCustomHosts: List<String> = StartupParamsTestUtils.CUSTOM_HOSTS
    private val uuid = "Valid uuid"
    private val deviceId = "Valid device id"
    private val deviceIdHash = "Valid device id hash"
    private val adUrlGet = "https://ad.url.get"
    private val adUrlReport = "https://ad.url.report"
    private val gaid = "test gaid"
    private val hoaid = "test hoaid"
    private val yandex: String = "test yandex adv_id"
    private val customKey1: String = "am"
    private val customKey2: String = "ads"
    private val customSdkHosts: MutableMap<String, List<String>> = HashMap()
    private val clidsFromPrefs = JsonHelper.clidsToString(StartupParamsTestUtils.CLIDS_MAP_1)
    private val clientClidsFromPrefs = StartupParamsTestUtils.CLIDS_2
    private val clientClids: Map<String, String> = StartupParamsTestUtils.CLIDS_MAP_3
    private val serverTimeOffset = 39787657L
    private val uuidResult = IdentifiersResult(uuid, IdentifierStatus.OK, null)
    private val uuidStartupParam = StartupParamsItem(uuid, StartupParamsItemStatus.OK, null)
    private val deviceIdResult = IdentifiersResult(deviceId, IdentifierStatus.OK, null)
    private val deviceIdStartupParam = StartupParamsItem(deviceId, StartupParamsItemStatus.OK, null)
    private val deviceIdHashResult = IdentifiersResult(deviceIdHash, IdentifierStatus.OK, null)
    private val deviceIdHashStartupParam = StartupParamsItem(deviceIdHash, StartupParamsItemStatus.OK, null)
    private val adUrlIdentifierResult = IdentifiersResult(adUrlGet, IdentifierStatus.OK, null)
    private val adUrlGetStartupParamsItem = StartupParamsItem(adUrlGet, StartupParamsItemStatus.OK, null)
    private val adUrlReportResult = IdentifiersResult(adUrlReport, IdentifierStatus.OK, null)
    private val adUrlReportStartupParamsItem = StartupParamsItem(adUrlReport, StartupParamsItemStatus.OK, null)
    private val gaidResult = IdentifiersResult(gaid, IdentifierStatus.OK, null)
    private val gaidStartupParamsItem = StartupParamsItem(gaid, StartupParamsItemStatus.OK, null)
    private val hoaidResult = IdentifiersResult(hoaid, IdentifierStatus.OK, null)
    private val hoaidStartupParamsItem = StartupParamsItem(hoaid, StartupParamsItemStatus.OK, null)
    private val yandexResult = IdentifiersResult(yandex, IdentifierStatus.OK, null)
    private val yandexStartupParamsItem = StartupParamsItem(yandex, StartupParamsItemStatus.OK, null)
    private val customSdkHostsResult1StartupParamsItem = StartupParamsItem(
        JsonHelper.listToJsonString(listOf("host1", "host2")),
        StartupParamsItemStatus.OK,
        null
    )
    private val customSdkHostsResult2StartupParamsItem = StartupParamsItem(
        JsonHelper.listToJsonString(listOf("host3")),
        StartupParamsItemStatus.OK,
        null
    )
    private val customSdkHostsResult = IdentifiersResult(
        JsonHelper.customSdkHostsToString(customSdkHosts),
        IdentifierStatus.OK,
        null
    )
    private val clidsFromPrefsResult = IdentifiersResult(clidsFromPrefs, IdentifierStatus.OK, null)
    private val clidsFromPrefsStartupParamItems = StartupParamsItem(clidsFromPrefs, StartupParamsItemStatus.OK, null)
    private val sslFeatureResult = IdentifiersResult("true", IdentifierStatus.OK, null)
    private val sslFeatureStartupParam = StartupParamsItem("true", StartupParamsItemStatus.OK, null)
    private lateinit var requestClidsResult: IdentifiersResult
    private lateinit var responseClidsResult: IdentifiersResult
    private val emptyIdentifierResult = IdentifiersResult("", IdentifierStatus.OK, null)
    private val nullIdentifierResult = IdentifiersResult(null, IdentifierStatus.OK, null)
    private val featuresInternal = FeaturesInternal(true, IdentifierStatus.OK, null)
    private val nextStartupTime = 327846276L
    private val initialCustomSdkHost = IdentifiersResult(null, IdentifierStatus.OK, null)

    private val uuidProvider: MultiProcessSafeUuidProvider = mock {
        on { readUuid() } doReturn uuidResult
    }

    private val preferences: PreferencesClientDbStorage = mock {
        on { uuidResult } doReturn uuidResult
        on { customSdkHosts } doReturn initialCustomSdkHost
        on { getFeatures() } doReturn FeaturesInternal()
    }

    private val featuresHolder: FeaturesHolder = mock {
        on { features } doReturn FeaturesInternal()
    }

    private val uuidValidator: UuidValidator = mock {
        on { isValid(uuid) } doReturn true
    }

    private val clientIdentifiersHolder: ClientIdentifiersHolder = mock()
    private val advIdentifiersConverter: AdvIdentifiersFromIdentifierResultConverter = mock()
    private val clidsStateChecker: ClidsStateChecker = mock()
    private val customSdkHostsHolder: CustomSdkHostsHolder = mock()
    private val featuresConverter: FeaturesConverter = mock()

    @get:Rule
    val contextRule = ContextRule()
    private val context by contextRule

    private lateinit var startupParams: StartupParams

    @get:Rule
    val startupRequiredUtilsRule = staticRule<StartupRequiredUtils> {
        on { StartupRequiredUtils.pickIdentifiersThatShouldTriggerStartup(any()) } doAnswer { invocation ->
            invocation.getArgument(0)
        }
    }

    @get:Rule
    val appMetricaSelfReportFacadeRule = staticRule<AppMetricaSelfReportFacade>()

    private val selfReporter: SelfReporterWrapper = mock()

    @Before
    fun setUp() {
        whenever(AppMetricaSelfReportFacade.getReporter()).thenReturn(selfReporter)

        customSdkHosts.put(customKey1, listOf("host1", "host2"))
        customSdkHosts.put(customKey2, listOf("host3"))

        responseClids.put("clid0", "123")
        responseClids.put("clid1", "456")
        requestClids.put("clid2", "789")
        requestClids.put("clid3", "120")
        requestClidsResult = IdentifiersResult(JsonHelper.clidsToString(requestClids), IdentifierStatus.OK, null)
        responseClidsResult = IdentifiersResult(JsonHelper.clidsToString(responseClids), IdentifierStatus.OK, null)

        StartupParamsTestUtils.mockPreferencesClientDbStoragePutResponses(preferences)

        mockClientIdentifiersHolder(clientIdentifiersHolder)

        startupParams = StartupParams(
            preferences,
            advIdentifiersConverter,
            clidsStateChecker,
            uuidProvider,
            customSdkHostsHolder,
            featuresHolder,
            featuresConverter,
            uuidValidator
        )
    }

    @Test
    fun shouldUpdateFeatures() {
        whenever(preferences.getFeatures()).thenReturn(featuresInternal)
        clearInvocations(featuresHolder)
        createStartupParams()
        verify(featuresHolder).features = featuresInternal
    }

    @Test
    fun uuidShouldBeSameAsStoredIfExternalUuidIsEmptyAndStoredUuidIsNotEmpty() {
        val uuidString: String = UUID.randomUUID().toString()
        whenever(uuidValidator.isValid(uuidString)).thenReturn(true)
        val prefUuid = IdentifiersResult(uuidString, IdentifierStatus.OK, null)
        whenever(uuidProvider.readUuid()).thenReturn(prefUuid)

        val startupParams: StartupParams = createStartupParams()

        assertThat(startupParams.uuid).isEqualTo(uuidString)
    }

    @Test
    fun uuidShouldNotBeChangedIfCallbackReturnsDifferentUuid() {
        val initialUuidString: String = UUID.randomUUID().toString()
        val anotherUuidString: String = UUID.randomUUID().toString()
        whenever(uuidValidator.isValid(initialUuidString)).thenReturn(true)
        whenever(uuidValidator.isValid(anotherUuidString)).thenReturn(true)
        val initialUuid = IdentifiersResult(initialUuidString, IdentifierStatus.OK, null)
        whenever(uuidProvider.readUuid()).thenReturn(initialUuid)
        val startupParams: StartupParams = createStartupParams()

        whenever(clientIdentifiersHolder.uuid).thenReturn(
            IdentifiersResult(
                anotherUuidString,
                IdentifierStatus.OK,
                null
            )
        )
        startupParams.updateAllParamsByReceiver(clientIdentifiersHolder)

        assertThat(startupParams.uuid).isEqualTo(initialUuidString)
    }

    @Test
    fun deviceIdShouldNotBeClearedByReceiver() {
        val deviceId: String = UUID.randomUUID().toString()
        val startupParams = StartupParams(context, preferences)
        startupParams.setDeviceId(IdentifiersResult(deviceId, IdentifierStatus.OK, null))
        whenever(clientIdentifiersHolder.deviceId).thenReturn(null)
        startupParams.updateAllParamsByReceiver(clientIdentifiersHolder)

        val inOrder = inOrder(preferences)
        inOrder.verify(preferences).putDeviceIdResult(any<IdentifiersResult>())
        assertThat(startupParams.deviceId).isNotEmpty()

        whenever(clientIdentifiersHolder.deviceId).thenReturn(emptyIdentifierResult)
        startupParams.updateAllParamsByReceiver(clientIdentifiersHolder)

        inOrder.verify(preferences).putDeviceIdResult(any<IdentifiersResult>())
        assertThat(startupParams.deviceId).isNotEmpty()
    }

    @Test
    fun hostsShouldNotBeClearedByReceiver() {
        defineTestHostsFromPreferences()

        val startupParams = StartupParams(context, preferences)
        whenever(clientIdentifiersHolder.getAdUrl).thenReturn(null)
        whenever(clientIdentifiersHolder.reportAdUrl).thenReturn(null)
        startupParams.updateAllParamsByReceiver(clientIdentifiersHolder)

        val inOrder = inOrder(preferences)
        inOrder.verify(preferences).putAdUrlGetResult(any<IdentifiersResult>())
        inOrder.verify(preferences).putAdUrlReportResult(any<IdentifiersResult>())

        whenever(clientIdentifiersHolder.getAdUrl).thenReturn(emptyIdentifierResult)
        whenever(clientIdentifiersHolder.reportAdUrl).thenReturn(emptyIdentifierResult)
        startupParams.updateAllParamsByReceiver(clientIdentifiersHolder)

        inOrder.verify(preferences).putAdUrlGetResult(any<IdentifiersResult>())
        inOrder.verify(preferences).putAdUrlReportResult(any<IdentifiersResult>())
    }

    @Test
    fun someStartupParamsShouldBeValidIfAllDefined() {
        defineAllStartupParamsFromPreferences()
        defineEmptyUuidFromPreferences()

        val startupParams = StartupParams(context, preferences)

        assertThat(
            startupParams.containsIdentifiers(
                listOf(
                    Constants.StartupParamsCallbackKeys.DEVICE_ID,
                    Constants.StartupParamsCallbackKeys.DEVICE_ID_HASH
                )
            )
        ).isTrue()
    }

    @Test
    fun someStartupParamsShouldNotBeValidIfOnlyOneDefined() {
        defineAllStartupParamsFromPreferences()
        defineEmptyUuidFromPreferences()
        defineEmptyDeviceIdFromPreferences()

        val startupParams = StartupParams(context, preferences)

        assertThat(
            startupParams.containsIdentifiers(
                listOf(
                    Constants.StartupParamsCallbackKeys.DEVICE_ID,
                    Constants.StartupParamsCallbackKeys.DEVICE_ID_HASH
                )
            )
        ).isFalse()
    }

    @Test
    fun someStartupParamsShouldNotBeValidIfNoneDefined() {
        val startupParams = StartupParams(context, preferences)

        assertThat(
            startupParams.containsIdentifiers(
                listOf(
                    Constants.StartupParamsCallbackKeys.DEVICE_ID,
                    Constants.StartupParamsCallbackKeys.DEVICE_ID_HASH
                )
            )
        ).isFalse()
    }

    @Test
    fun someStartupParamsShouldNotBeValidIfExtraDefined() {
        defineAllStartupParamsFromPreferences()

        val startupParams = StartupParams(context, preferences)

        assertThat(
            startupParams.containsIdentifiers(
                listOf(
                    Constants.StartupParamsCallbackKeys.DEVICE_ID,
                    Constants.StartupParamsCallbackKeys.DEVICE_ID_HASH
                )
            )
        ).isTrue()
    }

    @Test
    fun deviceIdShouldSaveCorrectly() {
        val deviceId = UUID.randomUUID().toString()
        whenever(preferences.deviceIdResult).thenReturn(IdentifiersResult(deviceId, IdentifierStatus.OK, null))
        val startupParams = StartupParams(context, preferences)

        assertThat(startupParams.deviceId).isEqualTo(deviceId)
    }

    @Test
    fun hostsShouldSavedCorrectly() {
        whenever(preferences.adUrlGetResult).thenReturn(testAdGetUrl)
        whenever(preferences.adUrlReportResult).thenReturn(testAdUrlReport)
        whenever(preferences.customSdkHosts).thenReturn(customSdkHostsResult)

        // Trigger saving values to preferences
        StartupParams(context, preferences)

        verify(preferences).putAdUrlGetResult(testAdGetUrl)
        verify(preferences).putAdUrlReportResult(testAdUrlReport)
        verify(preferences).putCustomSdkHosts(customSdkHostsResult)
    }

    @Test
    fun featuresShouldBeSavedCorrectly() {
        whenever(preferences.getFeatures()).thenReturn(featuresInternal)

        // Trigger saving values to preferences
        StartupParams(context, preferences)

        verify(preferences).putFeatures(featuresInternal)
    }

    @Test
    fun serverTimeOffsetShouldSaveCorrectly() {
        whenever(preferences.getServerTimeOffset(anyOrNull())).thenReturn(testServerSideOffset)
        createStartupParams()

        verify(preferences).putServerTimeOffset(testServerSideOffset)
    }

    @Test
    fun putToMapSomeIdentifiers() {
        val uuid = "uuid"
        val getAdUrl = "getAdUrl"
        whenever(uuidProvider.readUuid()).thenReturn(IdentifiersResult(uuid, IdentifierStatus.OK, null))
        whenever(preferences.adUrlGetResult).thenReturn(IdentifiersResult(getAdUrl, IdentifierStatus.OK, null))
        whenever(uuidValidator.isValid(uuid)).thenReturn(true)

        val startupParams = createStartupParams()

        val params: List<String> = listOf(
            Constants.StartupParamsCallbackKeys.UUID,
            Constants.StartupParamsCallbackKeys.GET_AD_URL
        )

        val actual: MutableMap<String, StartupParamsItem> = HashMap()
        startupParams.putToMap(params, actual)

        assertThat(actual).hasSize(2)

        assertThat(actual[Constants.StartupParamsCallbackKeys.UUID])
            .usingRecursiveComparison()
            .isEqualTo(StartupParamsItem(uuid, StartupParamsItemStatus.OK, null))

        assertThat(actual[Constants.StartupParamsCallbackKeys.GET_AD_URL])
            .usingRecursiveComparison()
            .isEqualTo(StartupParamsItem(getAdUrl, StartupParamsItemStatus.OK, null))
    }

    @Test
    fun putToMapInvalidIdentifier() {
        val uuid = "uuid"
        whenever(uuidProvider.readUuid()).thenReturn(IdentifiersResult(uuid, IdentifierStatus.OK, null))
        whenever(uuidValidator.isValid(uuid)).thenReturn(true)

        val startupParams: StartupParams = createStartupParams()
        val params = listOf(Constants.StartupParamsCallbackKeys.UUID, "invalid param")
        val actual = HashMap<String, StartupParamsItem>()
        startupParams.putToMap(params, actual)

        assertThat(actual).containsAllEntriesOf(
            mapOf(Constants.StartupParamsCallbackKeys.UUID to StartupParamsItem(uuid, StartupParamsItemStatus.OK, null))
        )
    }

    @Test
    fun putIdentifiersToMap() {
        val customIdentifier1 = "custom_id1"
        val customIdentifier2 = "custom_id2"
        val identifierKeys = listOf(
            Constants.StartupParamsCallbackKeys.UUID,
            Constants.StartupParamsCallbackKeys.DEVICE_ID,
            Constants.StartupParamsCallbackKeys.DEVICE_ID_HASH,
            Constants.StartupParamsCallbackKeys.REPORT_AD_URL,
            Constants.StartupParamsCallbackKeys.GET_AD_URL,
            Constants.StartupParamsCallbackKeys.CLIDS,
            Constants.StartupParamsCallbackKeys.GOOGLE_ADV_ID,
            Constants.StartupParamsCallbackKeys.HUAWEI_ADV_ID,
            Constants.StartupParamsCallbackKeys.YANDEX_ADV_ID,
            customIdentifier1,
            customIdentifier2
        )

        mockPreferencesWithValidValues()

        val startupParams: StartupParams = createStartupParams()
        val actual: MutableMap<String, StartupParamsItem> = HashMap()
        val customResult1: StartupParamsItem = mock<StartupParamsItem>()
        val customResult2: StartupParamsItem = mock<StartupParamsItem>()

        doAnswer { invocation ->
            val map = invocation.getArgument<MutableMap<String, StartupParamsItem>>(1)
            map[customIdentifier1] = customResult1
            map[customIdentifier2] = customResult2
            null
        }.whenever(customSdkHostsHolder).putToMap(identifierKeys, actual)

        doAnswer { invocation ->
            val map = invocation.getArgument<MutableMap<String, StartupParamsItem>>(1)
            map[Constants.StartupParamsCallbackKeys.FEATURE_LIB_SSL_ENABLED] = sslFeatureStartupParam
            null
        }.whenever(featuresHolder).putToMap(identifierKeys, actual)

        startupParams.putToMap(identifierKeys, actual)

        assertThat(actual).containsAllEntriesOf(
            mapOf(
                Constants.StartupParamsCallbackKeys.UUID to uuidStartupParam,
                Constants.StartupParamsCallbackKeys.DEVICE_ID to deviceIdStartupParam,
                Constants.StartupParamsCallbackKeys.DEVICE_ID_HASH to deviceIdHashStartupParam,
                Constants.StartupParamsCallbackKeys.REPORT_AD_URL to adUrlReportStartupParamsItem,
                Constants.StartupParamsCallbackKeys.GET_AD_URL to adUrlGetStartupParamsItem,
                Constants.StartupParamsCallbackKeys.CLIDS to clidsFromPrefsStartupParamItems,
                Constants.StartupParamsCallbackKeys.GOOGLE_ADV_ID to gaidStartupParamsItem,
                Constants.StartupParamsCallbackKeys.HUAWEI_ADV_ID to hoaidStartupParamsItem,
                Constants.StartupParamsCallbackKeys.YANDEX_ADV_ID to yandexStartupParamsItem,
                customIdentifier1 to customResult1,
                customIdentifier2 to customResult2,
                Constants.StartupParamsCallbackKeys.FEATURE_LIB_SSL_ENABLED to sslFeatureStartupParam
            )
        )
    }

    @Test
    fun setDifferentClientClids() {
        StartupParamsTestUtils.mockPreferencesClientDbStoragePutResponses(preferences)
        mockPreferencesWithValidValues()
        startupParams = StartupParams(context, preferences)

        val inOrder = inOrder(preferences)
        inOrder.verify(preferences).putResponseClidsResult(
            IdentifiersResult(
                JsonHelper.clidsToString(StartupParamsTestUtils.CLIDS_MAP_1),
                IdentifierStatus.OK,
                null
            )
        )
        inOrder.verify(preferences).putClientClids(StartupParamsTestUtils.CLIDS_2)

        assertThat(startupParams.clids).isEqualTo(JsonHelper.clidsToString(StartupParamsTestUtils.CLIDS_MAP_1))
        assertThat(startupParams.clientClids).isEqualTo(StartupParamsTestUtils.CLIDS_MAP_2)
        startupParams.setClientClids(StartupParamsTestUtils.CLIDS_MAP_3)

        inOrder.verify(preferences).putClientClids(StartupParamsTestUtils.CLIDS_3)
        inOrder.verify(preferences).putClientClidsChangedAfterLastIdentifiersUpdate(true)

        assertThat(startupParams.clids).isEqualTo(JsonHelper.clidsToString(StartupParamsTestUtils.CLIDS_MAP_1))
        assertThat(startupParams.clientClids).isEqualTo(StartupParamsTestUtils.CLIDS_MAP_3)
    }

    @Test
    fun setSameClientClids() {
        StartupParamsTestUtils.mockPreferencesClientDbStoragePutResponses(preferences)
        mockPreferencesWithValidValues()
        startupParams = StartupParams(context, preferences)

        val inOrder = inOrder(preferences)

        inOrder.verify(preferences).putResponseClidsResult(
            IdentifiersResult(
                JsonHelper.clidsToString(StartupParamsTestUtils.CLIDS_MAP_1),
                IdentifierStatus.OK,
                null
            )
        )
        inOrder.verify(preferences).putClientClids(StartupParamsTestUtils.CLIDS_2)

        assertThat(startupParams.clids).isEqualTo(JsonHelper.clidsToString(StartupParamsTestUtils.CLIDS_MAP_1))
        assertThat(startupParams.clientClids).isEqualTo(StartupParamsTestUtils.CLIDS_MAP_2)

        clearInvocations(preferences)

        startupParams.setClientClids(StartupParamsTestUtils.CLIDS_MAP_2)

        inOrder.verify(preferences, never()).putClientClids(any())
        inOrder.verify(preferences, never()).putClientClidsChangedAfterLastIdentifiersUpdate(any())

        assertThat(startupParams.clids).isEqualTo(JsonHelper.clidsToString(StartupParamsTestUtils.CLIDS_MAP_1))
        assertThat(startupParams.clientClids).isEqualTo(StartupParamsTestUtils.CLIDS_MAP_2)
    }

    @Test
    fun setClientClidsAfterNull() {
        StartupParamsTestUtils.mockPreferencesClientDbStoragePutResponses(preferences)
        mockPreferencesWithValidValues()
        whenever(preferences.getClientClids(anyOrNull())).thenReturn(null)
        startupParams = StartupParams(context, preferences)

        val inOrder = inOrder(preferences)
        inOrder.verify(preferences).putResponseClidsResult(
            IdentifiersResult(
                JsonHelper.clidsToString(StartupParamsTestUtils.CLIDS_MAP_1),
                IdentifierStatus.OK,
                null
            )
        )
        inOrder.verify(preferences).putClientClids("")

        assertThat(startupParams.clids).isEqualTo(JsonHelper.clidsToString(StartupParamsTestUtils.CLIDS_MAP_1))
        assertThat(startupParams.clientClids).isNull()

        startupParams.setClientClids(StartupParamsTestUtils.CLIDS_MAP_2)

        inOrder.verify(preferences).putClientClids(StartupParamsTestUtils.CLIDS_2)
        inOrder.verify(preferences).putClientClidsChangedAfterLastIdentifiersUpdate(true)

        assertThat(startupParams.clids).isEqualTo(JsonHelper.clidsToString(StartupParamsTestUtils.CLIDS_MAP_1))
        assertThat(startupParams.clientClids).isEqualTo(StartupParamsTestUtils.CLIDS_MAP_2)
    }

    @Test
    fun setNullClientClidsAfterNotNull() {
        StartupParamsTestUtils.mockPreferencesClientDbStoragePutResponses(preferences)
        mockPreferencesWithValidValues()
        startupParams = StartupParams(context, preferences)

        val inOrder = inOrder(preferences)
        inOrder.verify(preferences).putResponseClidsResult(
            IdentifiersResult(
                JsonHelper.clidsToString(StartupParamsTestUtils.CLIDS_MAP_1),
                IdentifierStatus.OK,
                null
            )
        )
        inOrder.verify(preferences).putClientClids(StartupParamsTestUtils.CLIDS_2)

        assertThat(startupParams.clids).isEqualTo(JsonHelper.clidsToString(StartupParamsTestUtils.CLIDS_MAP_1))
        assertThat(startupParams.clientClids).isEqualTo(StartupParamsTestUtils.CLIDS_MAP_2)

        startupParams.setClientClids(null)

        inOrder.verify(preferences, never()).putClientClids("")
        inOrder.verify(preferences, never()).putClientClidsChangedAfterLastIdentifiersUpdate(true)

        assertThat(startupParams.clids).isEqualTo(JsonHelper.clidsToString(StartupParamsTestUtils.CLIDS_MAP_1))
        assertThat(startupParams.clientClids).isEqualTo(StartupParamsTestUtils.CLIDS_MAP_2)
    }

    @Test
    fun shouldSendStartupForTheFirstTime() {
        StartupParamsTestUtils.mockPreferencesClientDbStoragePutResponses(preferences)
        mockPreferencesWithValidValues()
        whenever(preferences.getClientClidsChangedAfterLastIdentifiersUpdate(true)).thenReturn(true)
        startupParams = createStartupParams()
        assertThat(startupParams.shouldSendStartup()).isTrue()
    }

    @Test
    fun shouldNotSendStartupForAllValid() {
        StartupParamsTestUtils.mockPreferencesClientDbStoragePutResponses(preferences)
        mockPreferencesWithValidValues()
        startupParams = createStartupParams()
        assertThat(startupParams.shouldSendStartup()).isFalse()
    }

    @Test
    fun shouldSendStartupForOutdated() {
        StartupParamsTestUtils.mockPreferencesClientDbStoragePutResponses(preferences)
        mockPreferencesWithValidValues()
        whenever(StartupRequiredUtils.isOutdated(nextStartupTime)).thenReturn(true)
        startupParams = createStartupParams()
        assertThat(startupParams.shouldSendStartup()).isTrue()
    }

    @Test
    fun shouldWriteClidsUpdatedToPreferences() {
        StartupParamsTestUtils.mockPreferencesClientDbStoragePutResponses(preferences)
        mockPreferencesWithValidValues()
        startupParams = createStartupParams()
        clearInvocations(preferences)
        startupParams.setClientClids(clientClids)
        verify(preferences).putClientClidsChangedAfterLastIdentifiersUpdate(true)
        whenever(clidsStateChecker.doClientClidsMatchClientClidsForRequest(clientClids, requestClids)).thenReturn(true)
        val mock: ClientIdentifiersHolder = mock()
        mockClientIdentifiersHolder(mock)
        startupParams.updateAllParamsByReceiver(mock)
        verify(preferences).putClientClidsChangedAfterLastIdentifiersUpdate(false)
    }

    @Test
    fun shouldSendStartupForUpdateOnNewClids() {
        StartupParamsTestUtils.mockPreferencesClientDbStoragePutResponses(preferences)
        mockPreferencesWithValidValues()
        startupParams = createStartupParams()
        startupParams.setClientClids(clientClids)
        whenever(
            clidsStateChecker.doClientClidsMatchClientClidsForRequest(
                clientClids,
                requestClids
            )
        ).thenReturn(false)
        val mock: ClientIdentifiersHolder = mock()
        mockClientIdentifiersHolder(mock)
        startupParams.updateAllParamsByReceiver(mock)
        assertThat(startupParams.shouldSendStartup()).isTrue()
    }

    @Test
    fun shouldSendStartupIfRequestedGaid() {
        StartupParamsTestUtils.mockPreferencesClientDbStoragePutResponses(preferences)
        mockPreferencesWithValidValues()
        startupParams = createStartupParams()
        assertThat(
            startupParams.shouldSendStartup(
                listOf(
                    Constants.StartupParamsCallbackKeys.GOOGLE_ADV_ID
                )
            )
        ).isTrue()
    }

    @Test
    fun shouldSendStartupIfRequestedHoaid() {
        StartupParamsTestUtils.mockPreferencesClientDbStoragePutResponses(preferences)
        mockPreferencesWithValidValues()
        startupParams = createStartupParams()

        assertThat(
            startupParams.shouldSendStartup(
                listOf(
                    Constants.StartupParamsCallbackKeys.HUAWEI_ADV_ID
                )
            )
        ).isTrue()
    }

    @Test
    fun shouldSendStartupIfRequestedYandexAdvId() {
        StartupParamsTestUtils.mockPreferencesClientDbStoragePutResponses(preferences)
        mockPreferencesWithValidValues()
        startupParams = createStartupParams()

        assertThat(
            startupParams.shouldSendStartup(
                listOf(
                    Constants.StartupParamsCallbackKeys.YANDEX_ADV_ID
                )
            )
        ).isTrue()
    }

    @Test
    fun shouldNotSendStartupForCustomKeyIfNotOutdated() {
        val customKey = "custom key"
        StartupParamsTestUtils.mockPreferencesClientDbStoragePutResponses(preferences)
        mockPreferencesWithValidValues()

        whenever(StartupRequiredUtils.isOutdated(nextStartupTime)).thenReturn(false)
        whenever(StartupRequiredUtils.pickIdentifiersThatShouldTriggerStartup(listOf(customKey)))
            .thenReturn(emptyList())

        startupParams = createStartupParams()
        assertThat(startupParams.shouldSendStartup(listOf(customKey))).isFalse()

        startupRequiredUtilsRule.staticMock.verify {
            StartupRequiredUtils.pickIdentifiersThatShouldTriggerStartup(listOf(customKey))
        }
    }

    @Test
    fun shouldNotSendStartupForCustomKeyIfOutdated() {
        val customKey = "custom key"
        StartupParamsTestUtils.mockPreferencesClientDbStoragePutResponses(preferences)
        mockPreferencesWithValidValues()
        mockPreferencesWithValidValues()

        whenever(StartupRequiredUtils.isOutdated(nextStartupTime)).thenReturn(true)
        whenever(StartupRequiredUtils.pickIdentifiersThatShouldTriggerStartup(listOf(customKey)))
            .thenReturn(emptyList())

        startupParams = createStartupParams()

        assertThat(startupParams.shouldSendStartup(listOf(customKey))).isTrue()
    }

    @Test
    fun shouldNotSendStartupForSslPinningIfUpToDate() {
        StartupParamsTestUtils.mockPreferencesClientDbStoragePutResponses(preferences)
        mockPreferencesWithValidValues()

        whenever(featuresHolder.getFeature(Constants.StartupParamsCallbackKeys.FEATURE_LIB_SSL_ENABLED)).thenReturn(
            sslFeatureResult
        )
        whenever(StartupRequiredUtils.isOutdated(nextStartupTime)).thenReturn(false)
        whenever(
            StartupRequiredUtils.pickIdentifiersThatShouldTriggerStartup(
                listOf(Constants.StartupParamsCallbackKeys.FEATURE_LIB_SSL_ENABLED)
            )
        ).thenReturn(emptyList())

        startupParams = createStartupParams()

        assertThat(startupParams.shouldSendStartup(listOf(Constants.StartupParamsCallbackKeys.FEATURE_LIB_SSL_ENABLED)))
            .isFalse()
    }

    @Test
    fun shouldSendStartupForSslPinningIOutdated() {
        StartupParamsTestUtils.mockPreferencesClientDbStoragePutResponses(preferences)
        mockPreferencesWithValidValues()

        whenever(featuresHolder.getFeature(Constants.StartupParamsCallbackKeys.FEATURE_LIB_SSL_ENABLED))
            .thenReturn(sslFeatureResult)
        whenever(StartupRequiredUtils.isOutdated(nextStartupTime)).thenReturn(true)
        whenever(
            StartupRequiredUtils.pickIdentifiersThatShouldTriggerStartup(
                listOf(Constants.StartupParamsCallbackKeys.FEATURE_LIB_SSL_ENABLED)
            )
        ).thenReturn(emptyList())

        startupParams = createStartupParams()

        assertThat(startupParams.shouldSendStartup(listOf(Constants.StartupParamsCallbackKeys.FEATURE_LIB_SSL_ENABLED)))
            .isTrue()
    }

    @Test
    fun shouldSendStartupForIdentifiersForAllValid() {
        StartupParamsTestUtils.mockPreferencesClientDbStoragePutResponses(preferences)
        mockPreferencesWithValidValues()
        startupParams = createStartupParams()

        assertThat(
            startupParams.shouldSendStartup(
                listOf(
                    Constants.StartupParamsCallbackKeys.UUID,
                    Constants.StartupParamsCallbackKeys.DEVICE_ID
                )
            )
        ).isFalse()
    }

    @Test
    fun shouldSendStartupForIdentifiersForOutdated() {
        StartupParamsTestUtils.mockPreferencesClientDbStoragePutResponses(preferences)
        mockPreferencesWithValidValues()
        whenever(StartupRequiredUtils.isOutdated(nextStartupTime)).thenReturn(true)

        startupParams = createStartupParams()

        assertThat(
            startupParams.shouldSendStartup(
                listOf(
                    Constants.StartupParamsCallbackKeys.UUID,
                    Constants.StartupParamsCallbackKeys.DEVICE_ID
                )
            )
        ).isTrue()
    }

    @Test
    fun shouldSendStartupForIdentifiersForUpdateOnNewClids() {
        StartupParamsTestUtils.mockPreferencesClientDbStoragePutResponses(preferences)
        mockPreferencesWithValidValues()

        startupParams = createStartupParams()
        startupParams.setClientClids(StartupParamsTestUtils.CLIDS_MAP_3)

        assertThat(
            startupParams.shouldSendStartup(
                listOf(
                    Constants.StartupParamsCallbackKeys.UUID,
                    Constants.StartupParamsCallbackKeys.DEVICE_ID
                )
            )
        ).isTrue()
    }

    @Test
    fun updateAllParamsByReceiverEverythingFilled() {
        val identifierKeys: List<String> = listOf(
            Constants.StartupParamsCallbackKeys.GET_AD_URL,
            Constants.StartupParamsCallbackKeys.REPORT_AD_URL,
            Constants.StartupParamsCallbackKeys.GOOGLE_ADV_ID,
            Constants.StartupParamsCallbackKeys.HUAWEI_ADV_ID,
            Constants.StartupParamsCallbackKeys.YANDEX_ADV_ID,
            Constants.StartupParamsCallbackKeys.FEATURE_LIB_SSL_ENABLED,
            customKey1,
            customKey2
        )
        mockPreferencesWithValidValues()
        startupParams = createStartupParams()

        whenever(
            clidsStateChecker.doClientClidsMatchClientClidsForRequest(
                StartupUtils.decodeClids(clientClidsFromPrefs),
                requestClids
            )
        ).thenReturn(true)

        doAnswer { invocation ->
            val map = invocation.getArgument<MutableMap<String, StartupParamsItem>>(1)
            map[customKey1] = customSdkHostsResult1StartupParamsItem
            map[customKey2] = customSdkHostsResult2StartupParamsItem
            null
        }.whenever(customSdkHostsHolder).putToMap(eq(identifierKeys), any())

        clearInvocations(customSdkHostsHolder, featuresHolder)

        startupParams.updateAllParamsByReceiver(clientIdentifiersHolder)
        val identifiers = HashMap<String, StartupParamsItem>()

        startupParams.putToMap(identifierKeys, identifiers)

        verify(customSdkHostsHolder).update(customSdkHostsResult)
        verify(featuresHolder).features = featuresInternal

        val softly = SoftAssertions()
        softly.assertThat(startupParams.uuid).isEqualTo(uuid)
        softly.assertThat(startupParams.deviceId).isEqualTo(deviceId)
        softly.assertThat(startupParams.deviceIDHash).isEqualTo(deviceIdHash)
        softly.assertThat(startupParams.clids).isEqualTo(JsonHelper.clidsToString(responseClids))
        softly.assertThat(startupParams.serverTimeOffsetSeconds).isEqualTo(serverTimeOffset)
        softly.assertThat(identifiers[Constants.StartupParamsCallbackKeys.GET_AD_URL])
            .usingRecursiveComparison()
            .isEqualTo(adUrlGetStartupParamsItem)
        softly.assertThat(identifiers[Constants.StartupParamsCallbackKeys.REPORT_AD_URL])
            .usingRecursiveComparison()
            .isEqualTo(adUrlReportStartupParamsItem)
        softly.assertThat(identifiers[Constants.StartupParamsCallbackKeys.GOOGLE_ADV_ID])
            .usingRecursiveComparison()
            .isEqualTo(gaidStartupParamsItem)
        softly.assertThat(identifiers[Constants.StartupParamsCallbackKeys.HUAWEI_ADV_ID])
            .usingRecursiveComparison()
            .isEqualTo(hoaidStartupParamsItem)
        softly.assertThat(identifiers[Constants.StartupParamsCallbackKeys.YANDEX_ADV_ID])
            .usingRecursiveComparison()
            .isEqualTo(yandexStartupParamsItem)
        softly.assertThat(identifiers[customKey1])
            .usingRecursiveComparison()
            .isEqualTo(customSdkHostsResult1StartupParamsItem)
        softly.assertThat(identifiers[customKey2])
            .usingRecursiveComparison()
            .isEqualTo(customSdkHostsResult2StartupParamsItem)
        softly.assertAll()
    }

    @Test
    fun updateClidsAfterStartup() {
        val startupClids = hashMapOf("clid" to "from_startup")

        whenever(preferences.getClientClids(anyOrNull())).thenReturn(StartupUtils.encodeClids(TestData.TEST_CLIDS))
        whenever(clientIdentifiersHolder.responseClids)
            .thenReturn(IdentifiersResult(JsonHelper.clidsToString(startupClids), IdentifierStatus.OK, null))
        whenever(clientIdentifiersHolder.clientClidsForRequest)
            .thenReturn(IdentifiersResult(JsonHelper.clidsToString(TestData.TEST_CLIDS), IdentifierStatus.OK, null))

        startupParams = StartupParams(context, preferences)

        whenever(
            clidsStateChecker.doClientClidsMatchClientClidsForRequest(
                TestData.TEST_CLIDS,
                TestData.TEST_CLIDS
            )
        ).thenReturn(true)

        startupParams.updateAllParamsByReceiver(clientIdentifiersHolder)

        assertThat(startupParams.clids).isEqualTo(JsonHelper.clidsToString(startupClids))
    }

    @Test
    fun clidsAreNotUpdatedAfterStartupIfClientDoNotMatchRequest() {
        val startupClids = hashMapOf("clid" to "from_startup")

        whenever(preferences.getClientClids(anyOrNull())).thenReturn(StartupUtils.encodeClids(TestData.TEST_CLIDS))
        whenever(clientIdentifiersHolder.responseClids)
            .thenReturn(IdentifiersResult(JsonHelper.clidsToString(startupClids), IdentifierStatus.OK, null))
        whenever(clientIdentifiersHolder.clientClidsForRequest)
            .thenReturn(IdentifiersResult(JsonHelper.clidsToString(TestData.TEST_CLIDS), IdentifierStatus.OK, null))

        startupParams = createStartupParams()

        whenever(
            clidsStateChecker.doClientClidsMatchClientClidsForRequest(
                TestData.TEST_CLIDS,
                TestData.TEST_CLIDS
            )
        ).thenReturn(false)

        startupParams.updateAllParamsByReceiver(clientIdentifiersHolder)

        assertThat(startupParams.clids).isNotEqualTo(JsonHelper.clidsToString(startupClids))
    }

    @Test
    fun advIdentifiersAreUpdatedAnyway() {
        StartupParamsTestUtils.mockPreferencesClientDbStoragePutResponses(preferences)
        mockPreferencesWithValidValues()

        startupParams = StartupParams(context, preferences)

        whenever(clientIdentifiersHolder.gaid).thenReturn(nullIdentifierResult)
        whenever(clientIdentifiersHolder.hoaid).thenReturn(nullIdentifierResult)
        whenever(clientIdentifiersHolder.yandexAdvId).thenReturn(nullIdentifierResult)

        startupParams.updateAllParamsByReceiver(clientIdentifiersHolder)

        verify(preferences).putGaid(nullIdentifierResult)
        verify(preferences).putHoaid(nullIdentifierResult)
        verify(preferences).putYandexAdvId(nullIdentifierResult)
    }

    @Test
    fun containsIdentifiersClidsChanged() {
        mockPreferencesWithValidValues()
        startupParams = createStartupParams()
        val newClids: MutableMap<String, String> = HashMap()
        newClids.put("newclid0", "0")
        startupParams.setClientClids(newClids)
        assertThat(startupParams.containsIdentifiers(listOf(Constants.StartupParamsCallbackKeys.CLIDS))).isFalse()
    }

    @Test
    fun containsIdentifiersClidsDidNotChangeNewAreNull() {
        mockPreferencesWithValidValues()
        startupParams = createStartupParams()
        startupParams.setClientClids(null)
        assertThat(startupParams.containsIdentifiers(listOf(Constants.StartupParamsCallbackKeys.CLIDS))).isTrue()
    }

    @Test
    fun containsIdentifiersClidsDidNotChangeNewAreEmpty() {
        mockPreferencesWithValidValues()
        startupParams = createStartupParams()
        startupParams.setClientClids(HashMap())
        assertThat(startupParams.containsIdentifiers(listOf(Constants.StartupParamsCallbackKeys.CLIDS))).isTrue()
    }

    @Test
    fun containsIdentifiersClidsDidNotChangeNewAreTheSame() {
        mockPreferencesWithValidValues()
        startupParams = createStartupParams()
        startupParams.setClientClids(StartupParamsTestUtils.CLIDS_MAP_2)
        assertThat(startupParams.containsIdentifiers(listOf(Constants.StartupParamsCallbackKeys.CLIDS))).isTrue()
    }

    @Test
    fun containsIdentifiersClidsDidNotChange() {
        mockPreferencesWithValidValues()
        startupParams = createStartupParams()
        assertThat(startupParams.containsIdentifiers(listOf(Constants.StartupParamsCallbackKeys.CLIDS))).isTrue()
    }

    @Test
    fun listContainsIdentifiersGaid() {
        assertThat(startupParams.listContainsAdvIdentifiers(listOf(Constants.StartupParamsCallbackKeys.GOOGLE_ADV_ID)))
            .isTrue()
    }

    @Test
    fun listContainsIdentifiersHoaid() {
        assertThat(startupParams.listContainsAdvIdentifiers(listOf(Constants.StartupParamsCallbackKeys.HUAWEI_ADV_ID)))
            .isTrue()
    }

    @Test
    fun listContainsIdentifiersYandexAdvId() {
        assertThat(startupParams.listContainsAdvIdentifiers(listOf(Constants.StartupParamsCallbackKeys.YANDEX_ADV_ID)))
            .isTrue()
    }

    @Test
    fun listDoesNotContainIdentifiers() {
        assertThat(
            startupParams.listContainsAdvIdentifiers(
                listOf(
                    Constants.StartupParamsCallbackKeys.UUID,
                    Constants.StartupParamsCallbackKeys.DEVICE_ID,
                    Constants.StartupParamsCallbackKeys.DEVICE_ID_HASH,
                    Constants.StartupParamsCallbackKeys.REPORT_AD_URL,
                    Constants.StartupParamsCallbackKeys.GET_AD_URL,
                    Constants.StartupParamsCallbackKeys.CLIDS
                )
            )
        ).isFalse()
    }

    @Test
    fun getCachedAdvIdentifiers() {
        whenever(preferences.gaid).thenReturn(gaidResult)
        whenever(preferences.hoaid).thenReturn(hoaidResult)
        whenever(preferences.yandexAdvId).thenReturn(yandexResult)

        startupParams = createStartupParams()

        val converted = mock<AdvIdentifiersResult>()
        whenever(advIdentifiersConverter.convert(gaidResult, hoaidResult, yandexResult)).thenReturn(converted)
        assertThat(startupParams.cachedAdvIdentifiers).isSameAs(converted)
    }

    private fun defineAllStartupParamsFromPreferences() {
        defineRandomUuidFromPrefencesSpy()
        defineRandomDeviceIdFromPreferences()
        defineRandomDeviceIDHashFromPreferences()
        defineTestHostsFromPreferences()
        defineClidsFromPreferences()
        defineCustomSdkHostsFromPreferences()
    }

    private fun defineClidsFromPreferences() {
        whenever(preferences.responseClidsResult).thenReturn(responseClidsResult)
    }

    private fun defineCustomSdkHostsFromPreferences() {
        whenever(preferences.customSdkHosts).thenReturn(customSdkHostsResult)
    }

    private fun defineRandomDeviceIdFromPreferences() {
        defineDeviceIdFromPreferences(IdentifiersResult(UUID.randomUUID().toString(), IdentifierStatus.OK, null))
    }

    private fun defineRandomDeviceIDHashFromPreferences() {
        defineDeviceIdHashFromPreferences(IdentifiersResult(UUID.randomUUID().toString(), IdentifierStatus.OK, null))
    }

    private fun defineEmptyDeviceIdFromPreferences() {
        defineDeviceIdFromPreferences(IdentifiersResult("", IdentifierStatus.OK, null))
    }

    private fun defineDeviceIdFromPreferences(deviceId: IdentifiersResult) {
        whenever(preferences.deviceIdResult).thenReturn(deviceId)
    }

    private fun defineRandomUuidFromPrefencesSpy() {
        defineUuidFromPreferences(IdentifiersResult(UUID.randomUUID().toString(), IdentifierStatus.OK, null))
    }

    private fun defineUuidFromPreferences(prefUuid: IdentifiersResult) {
        doReturn(prefUuid).whenever(preferences).uuidResult
    }

    private fun defineEmptyUuidFromPreferences() {
        defineUuidFromPreferences(IdentifiersResult("", IdentifierStatus.OK, null))
    }

    private fun defineTestAdUrlGetFromPreferences() {
        defineAdUrlGetFromPrefences(testAdGetUrl)
    }

    private fun defineAdUrlGetFromPrefences(adUrlGet: IdentifiersResult) {
        whenever(preferences.adUrlGetResult).thenReturn(adUrlGet)
    }

    private fun defineTestAdUrlReportFromPreferences() {
        defineAdUrlReportFromPreferences(testAdUrlReport)
    }

    private fun defineAdUrlReportFromPreferences(adUrlReport: IdentifiersResult) {
        whenever(preferences.adUrlReportResult).thenReturn(adUrlReport)
    }

    private fun defineDeviceIdHashFromPreferences(deviceIdHash: IdentifiersResult) {
        whenever(preferences.deviceIdHashResult).thenReturn(deviceIdHash)
    }

    private fun defineTestHostsFromPreferences() {
        defineTestAdUrlGetFromPreferences()
        defineTestAdUrlReportFromPreferences()
    }

    private fun mockPreferencesWithValidValues() {
        whenever(preferences.getServerTimeOffset(0)).thenReturn(serverTimeOffset)
        whenever(preferences.getCustomHosts()).thenReturn(validCustomHosts)
        whenever(preferences.deviceIdResult).thenReturn(deviceIdResult)
        whenever(preferences.deviceIdHashResult).thenReturn(deviceIdHashResult)
        whenever(preferences.uuidResult).thenReturn(uuidResult)
        whenever(preferences.adUrlGetResult).thenReturn(adUrlIdentifierResult)
        whenever(preferences.adUrlReportResult).thenReturn(adUrlReportResult)
        whenever(preferences.gaid).thenReturn(gaidResult)
        whenever(preferences.hoaid).thenReturn(hoaidResult)
        whenever(preferences.yandexAdvId).thenReturn(yandexResult)
        whenever(preferences.responseClidsResult).thenReturn(clidsFromPrefsResult)
        whenever(preferences.getClientClids(anyOrNull())).thenReturn(clientClidsFromPrefs)
        whenever(preferences.getClientClidsChangedAfterLastIdentifiersUpdate(true)).thenReturn(false)
        whenever(preferences.customSdkHosts).thenReturn(customSdkHostsResult)
        whenever(preferences.nextStartupTime).thenReturn(nextStartupTime)
        whenever(preferences.getFeatures()).thenReturn(featuresInternal)
    }

    private fun mockClientIdentifiersHolder(mock: ClientIdentifiersHolder) {
        whenever(mock.uuid).thenReturn(uuidResult)
        whenever(mock.deviceId).thenReturn(deviceIdResult)
        whenever(mock.deviceIdHash).thenReturn(deviceIdHashResult)
        whenever(mock.reportAdUrl).thenReturn(adUrlReportResult)
        whenever(mock.getAdUrl).thenReturn(adUrlIdentifierResult)
        whenever(mock.responseClids).thenReturn(responseClidsResult)
        whenever(mock.clientClidsForRequest).thenReturn(requestClidsResult)
        whenever(mock.serverTimeOffset).thenReturn(serverTimeOffset)
        whenever(mock.gaid).thenReturn(gaidResult)
        whenever(mock.hoaid).thenReturn(hoaidResult)
        whenever(mock.yandexAdvId).thenReturn(yandexResult)
        whenever(mock.customSdkHosts).thenReturn(customSdkHostsResult)
        whenever(mock.nextStartupTime).thenReturn(nextStartupTime)
        whenever(mock.features).thenReturn(featuresInternal)
    }

    @Test
    fun shouldUpdateAllParamsInPreferencesOnCreate() {
        mockPreferencesWithValidValues()
        clearInvocations(preferences)

        whenever(customSdkHostsHolder.commonResult).thenReturn(customSdkHostsResult)
        whenever(featuresHolder.features).thenReturn(featuresInternal)

        createStartupParams()

        verify(preferences).putUuidResult(uuidResult)
        verify(preferences).putDeviceIdResult(deviceIdResult)
        verify(preferences).putDeviceIdHashResult(deviceIdHashResult)
        verify(preferences).putGaid(gaidResult)
        verify(preferences).putHoaid(hoaidResult)
        verify(preferences).putYandexAdvId(yandexResult)
        verify(preferences).putAdUrlReportResult(adUrlReportResult)
        verify(preferences).putAdUrlGetResult(adUrlIdentifierResult)
        verify(preferences).putServerTimeOffset(serverTimeOffset)
        verify(preferences).putResponseClidsResult(clidsFromPrefsResult)
        verify(preferences).putClientClids(clientClidsFromPrefs)
        verify(preferences).putClientClidsChangedAfterLastIdentifiersUpdate(false)
        verify(preferences).putCustomSdkHosts(customSdkHostsResult)
        verify(preferences).putFeatures(featuresInternal)
        verify(preferences).putNextStartupTime(nextStartupTime)
        verify(preferences).commit()
    }

    @Test
    fun shouldUpdateAllParamsInPreferencesOnUpdateFromReceiver() {
        startupParams.setClientClids(clientClids)

        clearInvocations(preferences)

        whenever(customSdkHostsHolder.commonResult).thenReturn(customSdkHostsResult)
        whenever(clidsStateChecker.doClientClidsMatchClientClidsForRequest(anyOrNull(), anyOrNull())).thenReturn(true)

        startupParams.updateAllParamsByReceiver(clientIdentifiersHolder)

        verify(preferences).putUuidResult(uuidResult)
        verify(preferences).putDeviceIdResult(deviceIdResult)
        verify(preferences).putDeviceIdHashResult(deviceIdHashResult)
        verify(preferences).putGaid(gaidResult)
        verify(preferences).putHoaid(hoaidResult)
        verify(preferences).putYandexAdvId(yandexResult)
        verify(preferences).putAdUrlReportResult(adUrlReportResult)
        verify(preferences).putAdUrlGetResult(adUrlIdentifierResult)
        verify(preferences).putServerTimeOffset(serverTimeOffset)
        verify(preferences).putResponseClidsResult(responseClidsResult)
        verify(preferences).putClientClids(StartupUtils.encodeClids(clientClids))
        verify(preferences).putClientClidsChangedAfterLastIdentifiersUpdate(false)
        verify(preferences).putCustomSdkHosts(customSdkHostsResult)
        verify(preferences).putNextStartupTime(nextStartupTime)
        verify(preferences).commit()
    }

    @Test
    fun shouldUpdateAllParamsInPreferencesOnSetClientClids() {
        val newClids = HashMap<String, String>()
        newClids.put("clid878", "999000")

        whenever(customSdkHostsHolder.commonResult).thenReturn(customSdkHostsResult)
        whenever(featuresHolder.features).thenReturn(featuresInternal)

        mockPreferencesWithValidValues()

        startupParams = createStartupParams()

        clearInvocations(preferences)

        startupParams.setClientClids(newClids)

        verify(preferences).putUuidResult(uuidResult)
        verify(preferences).putDeviceIdResult(deviceIdResult)
        verify(preferences).putDeviceIdHashResult(deviceIdHashResult)
        verify(preferences).putGaid(gaidResult)
        verify(preferences).putHoaid(hoaidResult)
        verify(preferences).putYandexAdvId(yandexResult)
        verify(preferences).putAdUrlReportResult(adUrlReportResult)
        verify(preferences).putAdUrlGetResult(adUrlIdentifierResult)
        verify(preferences).putServerTimeOffset(serverTimeOffset)
        verify(preferences).putResponseClidsResult(clidsFromPrefsResult)
        verify(preferences).putClientClids(StartupUtils.encodeClids(newClids))
        verify(preferences).putClientClidsChangedAfterLastIdentifiersUpdate(true)
        verify(preferences).putCustomSdkHosts(customSdkHostsResult)
        verify(preferences).putFeatures(featuresInternal)
        verify(preferences).putNextStartupTime(nextStartupTime)
        verify(preferences).commit()
    }

    @Test
    fun getFeatures() {
        val result: FeaturesResult = mock()

        whenever(featuresHolder.features).thenReturn(featuresInternal)

        startupParams = createStartupParams()

        whenever(featuresConverter.convert(featuresInternal)).thenReturn(result)

        assertThat(startupParams.features).isSameAs(result)
    }

    @Test
    fun shouldNotReportErrorWhenUuidMatchesBackup() {
        val uuid = "matching-uuid"
        val uuidResult = IdentifiersResult(uuid, IdentifierStatus.OK, null)

        whenever(uuidProvider.readUuid()).thenReturn(uuidResult)
        whenever(preferences.uuidResult).thenReturn(uuidResult)
        whenever(uuidValidator.isValid(uuid)).thenReturn(true)

        createStartupParams()

        verify(selfReporter, never()).reportError(any<String>(), any<String>())
    }

    @Test
    fun shouldReportErrorWhenUuidIsNull() {
        val backupUuid = "backup-uuid"
        val nullUuidResult = IdentifiersResult(null, IdentifierStatus.OK, null)
        val backupUuidResult = IdentifiersResult(backupUuid, IdentifierStatus.OK, null)

        whenever(uuidProvider.readUuid()).thenReturn(nullUuidResult)
        whenever(preferences.uuidResult).thenReturn(backupUuidResult)
        whenever(uuidValidator.isValid(null)).thenReturn(false)

        createStartupParams()

        verify(selfReporter).reportError(
            eq("null_uuid_on_client"),
            eq("The only true uuid: null; backup uuid: $backupUuid")
        )
    }

    @Test
    fun shouldReportErrorWhenUuidDoesNotMatchBackup() {
        val theOnlyTrueUuid = "true-uuid"
        val backupUuid = "backup-uuid"
        val trueUuidResult = IdentifiersResult(theOnlyTrueUuid, IdentifierStatus.OK, null)
        val backupUuidResult = IdentifiersResult(backupUuid, IdentifierStatus.OK, null)

        whenever(uuidProvider.readUuid()).thenReturn(trueUuidResult)
        whenever(preferences.uuidResult).thenReturn(backupUuidResult)
        whenever(uuidValidator.isValid(theOnlyTrueUuid)).thenReturn(true)

        createStartupParams()

        verify(selfReporter).reportError(
            eq("wrong_uuid_on_client"),
            eq("The only true uuid: $theOnlyTrueUuid; backup uuid: $backupUuid")
        )
    }

    @Test
    fun shouldUseValidUuidEvenWhenReportingError() {
        val theOnlyTrueUuid = "true-uuid"
        val backupUuid = "backup-uuid"
        val trueUuidResult = IdentifiersResult(theOnlyTrueUuid, IdentifierStatus.OK, null)
        val backupUuidResult = IdentifiersResult(backupUuid, IdentifierStatus.OK, null)

        whenever(uuidProvider.readUuid()).thenReturn(trueUuidResult)
        whenever(preferences.uuidResult).thenReturn(backupUuidResult)
        whenever(uuidValidator.isValid(theOnlyTrueUuid)).thenReturn(true)

        val startupParams = createStartupParams()

        assertThat(startupParams.uuid).isEqualTo(theOnlyTrueUuid)
        verify(selfReporter).reportError(any<String>(), any<String>())
    }

    private fun createStartupParams(): StartupParams {
        return StartupParams(
            preferences,
            advIdentifiersConverter,
            clidsStateChecker,
            uuidProvider,
            customSdkHostsHolder,
            featuresHolder,
            featuresConverter,
            uuidValidator
        )
    }
}
