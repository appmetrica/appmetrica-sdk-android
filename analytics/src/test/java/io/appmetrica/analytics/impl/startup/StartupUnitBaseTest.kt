package io.appmetrica.analytics.impl.startup

import io.appmetrica.analytics.coreapi.internal.identifiers.AdvertisingIdsHolder
import io.appmetrica.analytics.coreapi.internal.identifiers.IdentifierStatus
import io.appmetrica.analytics.coreutils.internal.time.TimeProvider
import io.appmetrica.analytics.impl.ClidsInfoStorage
import io.appmetrica.analytics.impl.DataResultReceiver
import io.appmetrica.analytics.impl.GlobalServiceLocator
import io.appmetrica.analytics.impl.StartupStateHolder
import io.appmetrica.analytics.impl.clids.ClidsInfo
import io.appmetrica.analytics.impl.client.ClientConfiguration
import io.appmetrica.analytics.impl.client.ProcessConfiguration
import io.appmetrica.analytics.impl.component.ComponentId
import io.appmetrica.analytics.impl.id.AdvertisingIdGetter
import io.appmetrica.analytics.impl.request.StartupRequestConfig
import io.appmetrica.analytics.impl.startup.parsing.StartupResult
import io.appmetrica.analytics.impl.startup.uuid.MultiProcessSafeUuidProvider
import io.appmetrica.analytics.impl.startup.uuid.UuidValidator
import io.appmetrica.analytics.impl.utils.DeviceIdGenerator
import io.appmetrica.analytics.internal.CounterConfiguration
import io.appmetrica.analytics.internal.IdentifiersResult
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.GlobalServiceLocatorRule
import io.appmetrica.analytics.testutils.ServiceMigrationCheckedRule
import io.appmetrica.analytics.testutils.TestUtils
import org.junit.Before
import org.junit.Rule
import org.junit.rules.RuleChain
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.robolectric.RuntimeEnvironment
import java.util.UUID

internal open class StartupUnitBaseTest : CommonTest() {

    private val startupState = TestUtils.createDefaultStartupState()

    val dataResultReceiver: DataResultReceiver = mock()

    val sdkConfig = StartupRequestConfig.Arguments(
        ClientConfiguration(
            ProcessConfiguration(
                RuntimeEnvironment.getApplication(),
                dataResultReceiver
            ),
            CounterConfiguration()
        )
    )

    private val collectingFlags: CollectingFlags = mock()

    val startupResult: StartupResult = mock {
        on { collectionFlags } doReturn collectingFlags
    }

    private val chosenClids: ClidsInfo.Candidate = mock()

    val startupRequestConfig: StartupRequestConfig = mock {
        on { chosenClids } doReturn chosenClids
    }

    val startupResultListener: StartupResultListener = mock()
    val advertisingIdsHolder: AdvertisingIdsHolder = mock()
    val timeProvider: TimeProvider = mock()
    val clidsStorage: ClidsInfoStorage = mock()
    val clidsStateChecker: ClidsStateChecker = mock()
    val advertisingIdGetter: AdvertisingIdGetter = mock()

    val startupStateStorage: StartupState.Storage = mock {
        on { read() } doReturn startupState
    }

    val startupConfigurationHolder: StartupConfigurationHolder = mock {
        on { startupState } doReturn startupState
    }

    val uuidProvider: MultiProcessSafeUuidProvider = mock {
        on { readUuid() } doReturn IdentifiersResult(UUID.randomUUID().toString(), IdentifierStatus.OK, null)
    }

    val uuidValidator: UuidValidator = mock()
    val deviceIdGenerator: DeviceIdGenerator = mock()

    lateinit var startupUnit: StartupUnit

    val obtainServerTime = 1234567L
    val firstStartupServerTime = 5000L

    val componentId: ComponentId = mock {
        on { `package` } doReturn RuntimeEnvironment.getApplication().packageName
    }

    val startupStateHolder: StartupStateHolder = mock()

    val startupUnitComponents: StartupUnitComponents = mock {
        on { requestConfigArguments } doReturn sdkConfig
        on { packageName } doReturn RuntimeEnvironment.getApplication().packageName
        on { context } doReturn RuntimeEnvironment.getApplication()
        on { resultListener } doReturn startupResultListener
        on { startupStateStorage } doReturn startupStateStorage
        on { timeProvider } doReturn timeProvider
        on { clidsStorage } doReturn clidsStorage
        on { clidsStateChecker } doReturn clidsStateChecker
        on { startupConfigurationHolder } doReturn startupConfigurationHolder
        on { multiProcessSafeUuidProvider } doReturn uuidProvider
        on { uuidValidator } doReturn uuidValidator
        on { deviceIdGenerator } doReturn deviceIdGenerator
        on { componentId } doReturn componentId
        on { startupStateHolder } doReturn startupStateHolder
    }

    @get:Rule
    val ruleChain: RuleChain = RuleChain.outerRule(GlobalServiceLocatorRule()).around(ServiceMigrationCheckedRule())

    @Before
    open fun setup() {
        startupUnit = StartupUnit(startupUnitComponents)
        startupConfigurationHolder.updateArguments(sdkConfig)
        doReturn(startupRequestConfig).whenever(startupConfigurationHolder).get()
        whenever(GlobalServiceLocator.getInstance().advertisingIdGetter.identifiers).thenReturn(advertisingIdsHolder)
    }

    companion object {
        const val TEST_RESPONSE =
            "{\"device_id\":{\"value\":\"abcae254a259c453a02d0e1bca02c4ff\"}," +
                "\"distribution_customization\":{\"brand_id\":\"\",\"clids\":{\"clid1\":{\"value\":\"1\"}}," +
                "\"switch_search_widget_to_yandex\":\"0\"},\"query_hosts\":{\"list\":{\"check_updates\"" +
                ":{\"url\":\"https:\\/\\/analytics.mobile.yandex.net\"},\"get_ad\":{\"url\"" +
                ":\"https:\\/\\/mobile.yandexadexchange.net\"},\"report\":{\"url\"" +
                ":\"https:\\/\\/analytics.mobile.yandex.net\"},\"report_ad\"" +
                ":{\"url\":\"https:\\/\\/mobile.yandexadexchange.net\"},\"search\":{\"url\":\"\"}," +
                "\"url_schemes\":{\"url\":\"https:\\/\\/analytics.mobile.yandex.net\"}}}," +
                "\"uuid\":{\"value\":\"ecf192096fb1c3e0b247a08edb0a60ce\"}," +
                "\"retry_policy\":{\"max_interval_seconds\":1000,\"exponential_multiplier\":2}}\n"
    }
}
