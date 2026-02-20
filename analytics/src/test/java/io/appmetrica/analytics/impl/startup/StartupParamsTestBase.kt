package io.appmetrica.analytics.impl.startup

import io.appmetrica.analytics.coreapi.internal.identifiers.IdentifierStatus
import io.appmetrica.analytics.impl.ClientIdentifiersHolder
import io.appmetrica.analytics.impl.db.preferences.PreferencesClientDbStorage
import io.appmetrica.analytics.impl.selfreporting.AppMetricaSelfReportFacade
import io.appmetrica.analytics.impl.selfreporting.SelfReporterWrapper
import io.appmetrica.analytics.impl.startup.uuid.MultiProcessSafeUuidProvider
import io.appmetrica.analytics.impl.startup.uuid.UuidValidator
import io.appmetrica.analytics.impl.utils.JsonHelper
import io.appmetrica.analytics.impl.utils.StartupUtils
import io.appmetrica.analytics.internal.IdentifiersResult
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.LogRule
import io.appmetrica.analytics.testutils.MockedStaticRule
import org.junit.Before
import org.junit.Rule
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

internal abstract class StartupParamsTestBase(
    protected val clientIdentifiersHolder: ClientIdentifiersHolder,
    protected val isOutdated: Boolean,
    protected val requestedIdentifiers: List<String>,
    protected val clientClids: Map<String, String>?,
    protected val shouldUpdateClids: Boolean
) : CommonTest() {

    @Rule
    @JvmField
    internal val logRule = LogRule()

    internal val storage = mock<PreferencesClientDbStorage>()
    internal val multiProcessSafeUuidProvider = mock<MultiProcessSafeUuidProvider>()
    internal val advIdentifiersConverter = mock<AdvIdentifiersFromIdentifierResultConverter>()
    internal val clidsStateChecker = mock<ClidsStateChecker>()
    internal val uuidValidator = mock<UuidValidator>()
    internal val selfReporter = mock<SelfReporterWrapper>()

    @Rule
    @JvmField
    internal val startupRequiredUtils = MockedStaticRule(StartupRequiredUtils::class.java)

    @Rule
    @JvmField
    internal val appMetricaSelfReportFacade = MockedStaticRule(AppMetricaSelfReportFacade::class.java)

    protected lateinit var startupParams: StartupParams

    @Before
    fun setUp() {
        whenever(AppMetricaSelfReportFacade.getReporter()).thenReturn(selfReporter)
        StartupParamsTestUtils.mockPreferencesClientDbStoragePutResponses(storage)
        whenever(storage.getClientClids(anyOrNull()))
            .thenReturn(StartupUtils.encodeClids(CLIENT_CLIDS))
        val nextStartupTime = 374687L
        whenever(clientIdentifiersHolder.nextStartupTime).thenReturn(nextStartupTime)
        whenever(StartupRequiredUtils.isOutdated(nextStartupTime)).thenReturn(isOutdated)
        whenever(StartupRequiredUtils.pickIdentifiersThatShouldTriggerStartup(any()))
            .thenCallRealMethod()
        whenever(storage.getClientClidsChangedAfterLastIdentifiersUpdate(true))
            .thenReturn(false)
        whenever(storage.customSdkHosts)
            .thenReturn(IdentifiersResult(null, IdentifierStatus.UNKNOWN, null))
        whenever(storage.features).thenReturn(FeaturesInternal())

        val uuidForSetup = clientIdentifiersHolder.uuid
        val uuidId = uuidForSetup.id
        whenever(storage.uuidResult).thenReturn(uuidForSetup)
        whenever(multiProcessSafeUuidProvider.readUuid()).thenReturn(uuidForSetup)
        whenever(uuidValidator.isValid(uuidId)).thenReturn(uuidId != null && uuidId.isNotEmpty())

        startupParams = StartupParams(
            storage,
            advIdentifiersConverter,
            clidsStateChecker,
            multiProcessSafeUuidProvider,
            CustomSdkHostsHolder(),
            FeaturesHolder(),
            FeaturesConverter(),
            uuidValidator
        )
        startupParams.clientClids = clientClids
        val requestClids =
            JsonHelper.clidsFromString(clientIdentifiersHolder.clientClidsForRequest.id)
        whenever(clidsStateChecker.doClientClidsMatchClientClidsForRequest(clientClids, requestClids))
            .thenReturn(shouldUpdateClids)
        whenever(
            clidsStateChecker.doClientClidsMatchClientClidsForRequest(
                CLIENT_CLIDS,
                requestClids
            )
        ).thenReturn(shouldUpdateClids)
        startupParams.updateAllParamsByReceiver(clientIdentifiersHolder)
    }

    companion object {
        @JvmField
        val CLIENT_CLIDS: Map<String, String> = StartupParamsTestUtils.CLIDS_MAP_1

        @JvmField
        val REQUEST_CLIDS = IdentifiersResult(
            JsonHelper.clidsToString(StartupParamsTestUtils.CLIDS_MAP_1),
            IdentifierStatus.OK,
            null
        )

        @JvmField
        val RESPONSE_CLIDS = IdentifiersResult(
            JsonHelper.clidsToString(StartupParamsTestUtils.CLIDS_MAP_2),
            IdentifierStatus.OK,
            null
        )

        @JvmField
        val EMPTY_IDENTIFIER = IdentifiersResult("", IdentifierStatus.OK, null)

        @JvmField
        val NULL_IDENTIFIER = IdentifiersResult(null, IdentifierStatus.OK, null)

        @JvmField
        val UUID = IdentifiersResult("uuid", IdentifierStatus.OK, null)

        @JvmField
        val DEVICE_ID = IdentifiersResult("device id", IdentifierStatus.OK, null)

        @JvmField
        val DEVICE_ID_HASH = IdentifiersResult("device id hash", IdentifierStatus.OK, null)

        @JvmField
        val GET_AD_URL = IdentifiersResult("get ad url", IdentifierStatus.OK, null)

        @JvmField
        val REPORT_AD_URL = IdentifiersResult("report ad url", IdentifierStatus.OK, null)

        @JvmField
        val GAID = IdentifiersResult("gaid", IdentifierStatus.OK, null)

        @JvmField
        val HOAID = IdentifiersResult("hoaid", IdentifierStatus.OK, null)

        @JvmField
        val YANDEX_ADV_ID = IdentifiersResult("yandex adv id", IdentifierStatus.OK, null)

        @JvmField
        val FEATURES = FeaturesInternal(false, IdentifierStatus.OK, null)

        @JvmField
        val CUSTOM_HOSTS1 = listOf("custom_host_1_1", "custom_host_1_2")

        @JvmField
        val CUSTOM_HOSTS2 = listOf("custom_host_2_1", "custom_host_2_2")

        @JvmField
        val CUSTOM_SDK_HOSTS: IdentifiersResult = run {
            val customSdkHostsMap = mapOf(
                StartupParamsTestUtils.CUSTOM_IDENTIFIER1 to CUSTOM_HOSTS1,
                StartupParamsTestUtils.CUSTOM_IDENTIFIER2 to CUSTOM_HOSTS2
            )
            IdentifiersResult(
                JsonHelper.customSdkHostsToString(customSdkHostsMap),
                IdentifierStatus.OK,
                null
            )
        }

        @JvmField
        val SERVER_TIME_OFFSET = 123L

        private val emptyIdentifiers = IdentifiersResult(null, IdentifierStatus.UNKNOWN, null)

        @JvmStatic
        protected fun prepareEmptyIdentifiersHolderMock(): ClientIdentifiersHolder = mock {
            on { responseClids } doReturn emptyIdentifiers
            on { clientClidsForRequest } doReturn emptyIdentifiers
            on { reportAdUrl } doReturn emptyIdentifiers
            on { getAdUrl } doReturn emptyIdentifiers
            on { deviceId } doReturn emptyIdentifiers
            on { uuid } doReturn emptyIdentifiers
            on { deviceIdHash } doReturn emptyIdentifiers
            on { gaid } doReturn emptyIdentifiers
            on { hoaid } doReturn emptyIdentifiers
            on { yandexAdvId } doReturn emptyIdentifiers
            on { customSdkHosts } doReturn emptyIdentifiers
            on { features } doReturn FeaturesInternal()
        }

        @JvmStatic
        protected fun prepareFilledIdentifiersHolderMock(): ClientIdentifiersHolder = mock {
            on { responseClids } doReturn RESPONSE_CLIDS
            on { clientClidsForRequest } doReturn REQUEST_CLIDS
            on { reportAdUrl } doReturn REPORT_AD_URL
            on { getAdUrl } doReturn GET_AD_URL
            on { deviceId } doReturn DEVICE_ID
            on { uuid } doReturn UUID
            on { deviceIdHash } doReturn DEVICE_ID_HASH
            on { gaid } doReturn GAID
            on { hoaid } doReturn HOAID
            on { yandexAdvId } doReturn YANDEX_ADV_ID
            on { serverTimeOffset } doReturn SERVER_TIME_OFFSET
            on { customSdkHosts } doReturn CUSTOM_SDK_HOSTS
            on { features } doReturn FEATURES
        }

        @JvmStatic
        protected fun prepareEmptyWithUuid(uuid: IdentifiersResult): ClientIdentifiersHolder =
            prepareEmptyIdentifiersHolderMock().apply {
                whenever(getUuid()).thenReturn(uuid)
            }

        @JvmStatic
        protected fun prepareEmptyWithDeviceId(deviceId: IdentifiersResult): ClientIdentifiersHolder =
            prepareEmptyIdentifiersHolderMock().apply {
                whenever(getDeviceId()).thenReturn(deviceId)
            }

        @JvmStatic
        protected fun prepareEmptyWithDeviceIdHash(
            deviceIdHash: IdentifiersResult
        ): ClientIdentifiersHolder =
            prepareEmptyIdentifiersHolderMock().apply {
                whenever(getDeviceIdHash()).thenReturn(deviceIdHash)
            }

        @JvmStatic
        protected fun prepareEmptyWithReportAdUrl(
            reportAdUrl: IdentifiersResult
        ): ClientIdentifiersHolder =
            prepareEmptyIdentifiersHolderMock().apply {
                whenever(getReportAdUrl()).thenReturn(reportAdUrl)
            }

        @JvmStatic
        protected fun prepareEmptyWithGetAdUrl(getAdUrl: IdentifiersResult): ClientIdentifiersHolder =
            prepareEmptyIdentifiersHolderMock().apply {
                whenever(getGetAdUrl()).thenReturn(getAdUrl)
            }

        @JvmStatic
        protected fun prepareEmptyWithClids(clids: IdentifiersResult): ClientIdentifiersHolder =
            prepareEmptyIdentifiersHolderMock().apply {
                whenever(getResponseClids()).thenReturn(clids)
            }

        @JvmStatic
        protected fun prepareEmptyWithGaid(gaid: IdentifiersResult): ClientIdentifiersHolder =
            prepareEmptyIdentifiersHolderMock().apply {
                whenever(getGaid()).thenReturn(gaid)
            }

        @JvmStatic
        protected fun prepareEmptyWithHoaid(hoaid: IdentifiersResult): ClientIdentifiersHolder =
            prepareEmptyIdentifiersHolderMock().apply {
                whenever(getHoaid()).thenReturn(hoaid)
            }

        @JvmStatic
        protected fun prepareEmptyWithYandexAdvId(
            yandexAdvId: IdentifiersResult
        ): ClientIdentifiersHolder =
            prepareEmptyIdentifiersHolderMock().apply {
                whenever(getYandexAdvId()).thenReturn(yandexAdvId)
            }

        @JvmStatic
        protected fun prepareEmptyWithCustomSdkHosts(
            customSdkHosts: IdentifiersResult
        ): ClientIdentifiersHolder =
            prepareEmptyIdentifiersHolderMock().apply {
                whenever(getCustomSdkHosts()).thenReturn(customSdkHosts)
            }

        @JvmStatic
        internal fun prepareEmptyWithFeatures(
            features: FeaturesInternal
        ): ClientIdentifiersHolder =
            prepareEmptyIdentifiersHolderMock().apply {
                whenever(getFeatures()).thenReturn(features)
            }

        @JvmStatic
        internal fun prepareFilledWithUuid(uuid: IdentifiersResult): ClientIdentifiersHolder =
            prepareFilledIdentifiersHolderMock().apply {
                whenever(getUuid()).thenReturn(uuid)
            }

        @JvmStatic
        protected fun prepareFilledWithDeviceId(deviceId: IdentifiersResult): ClientIdentifiersHolder =
            prepareFilledIdentifiersHolderMock().apply {
                whenever(getDeviceId()).thenReturn(deviceId)
            }

        @JvmStatic
        protected fun prepareFilledWithDeviceIdHash(
            deviceIdHash: IdentifiersResult
        ): ClientIdentifiersHolder =
            prepareFilledIdentifiersHolderMock().apply {
                whenever(getDeviceIdHash()).thenReturn(deviceIdHash)
            }

        @JvmStatic
        protected fun prepareFilledWithReportAdUrl(
            reportAdUrl: IdentifiersResult
        ): ClientIdentifiersHolder =
            prepareFilledIdentifiersHolderMock().apply {
                whenever(getReportAdUrl()).thenReturn(reportAdUrl)
            }

        @JvmStatic
        protected fun prepareFilledWithGetAdUrl(getAdUrl: IdentifiersResult): ClientIdentifiersHolder =
            prepareFilledIdentifiersHolderMock().apply {
                whenever(getGetAdUrl()).thenReturn(getAdUrl)
            }

        @JvmStatic
        protected fun prepareFilledWithClids(clids: IdentifiersResult): ClientIdentifiersHolder =
            prepareFilledIdentifiersHolderMock().apply {
                whenever(getResponseClids()).thenReturn(clids)
            }

        @JvmStatic
        protected fun prepareFilledWithGaid(gaid: IdentifiersResult): ClientIdentifiersHolder =
            prepareFilledIdentifiersHolderMock().apply {
                whenever(getGaid()).thenReturn(gaid)
            }

        @JvmStatic
        protected fun prepareFilledWithHoaid(hoaid: IdentifiersResult): ClientIdentifiersHolder =
            prepareFilledIdentifiersHolderMock().apply {
                whenever(getHoaid()).thenReturn(hoaid)
            }

        @JvmStatic
        protected fun prepareFilledWithYandexAdvId(
            yandexAdvId: IdentifiersResult
        ): ClientIdentifiersHolder =
            prepareFilledIdentifiersHolderMock().apply {
                whenever(getYandexAdvId()).thenReturn(yandexAdvId)
            }

        @JvmStatic
        protected fun prepareFilledWithCustomSdkHosts(
            customSdkHosts: IdentifiersResult
        ): ClientIdentifiersHolder =
            prepareFilledIdentifiersHolderMock().apply {
                whenever(getCustomSdkHosts()).thenReturn(customSdkHosts)
            }

        @JvmStatic
        internal fun prepareFilledWithFeatures(
            features: FeaturesInternal
        ): ClientIdentifiersHolder =
            prepareFilledIdentifiersHolderMock().apply {
                whenever(getFeatures()).thenReturn(features)
            }
    }
}
