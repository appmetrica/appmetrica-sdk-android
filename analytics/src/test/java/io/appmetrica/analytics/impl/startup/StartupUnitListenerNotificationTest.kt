package io.appmetrica.analytics.impl.startup

import io.appmetrica.analytics.coreutils.internal.time.TimeProvider
import io.appmetrica.analytics.impl.ClidsInfoStorage
import io.appmetrica.analytics.impl.StartupStateHolder
import io.appmetrica.analytics.impl.clids.ClidsInfo
import io.appmetrica.analytics.impl.component.ComponentId
import io.appmetrica.analytics.impl.request.StartupRequestConfig
import io.appmetrica.analytics.impl.startup.parsing.StartupResult
import io.appmetrica.analytics.impl.startup.uuid.MultiProcessSafeUuidProvider
import io.appmetrica.analytics.impl.startup.uuid.UuidValidator
import io.appmetrica.analytics.impl.utils.DeviceIdGenerator
import io.appmetrica.analytics.impl.utils.ServerTime
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.GlobalServiceLocatorRule
import io.appmetrica.analytics.testutils.TestUtils
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
internal class StartupUnitListenerNotificationTest : CommonTest() {

    @get:Rule
    var globalServiceLocatorRule = GlobalServiceLocatorRule()

    private val packageName = "test.package"

    private val componentId: ComponentId = mock {
        on { `package` } doReturn packageName
    }

    private val deviceIdGenerator: DeviceIdGenerator = mock()
    private val startupResultListener: StartupResultListener = mock()

    private val startupRequestConfig: StartupRequestConfig = mock()

    private val startupConfigurationHolder: StartupConfigurationHolder = mock {
        on { get() } doReturn startupRequestConfig
    }

    private val startupStateStorage: StartupState.Storage = mock()
    private val clidsStorage: ClidsInfoStorage = mock()
    private val clidsStateChecker: ClidsStateChecker = mock()
    private val uuidProvider: MultiProcessSafeUuidProvider = mock()
    private val uuidValidator: UuidValidator = mock()
    private val timeProvider: TimeProvider = mock()
    private val startupStateHolder: StartupStateHolder = mock()
    private val requestConfigArguments: StartupRequestConfig.Arguments = mock()

    private val startupUnitComponents: StartupUnitComponents = mock {
        on { startupConfigurationHolder } doReturn startupConfigurationHolder
        on { deviceIdGenerator } doReturn deviceIdGenerator
        on { componentId } doReturn componentId
        on { resultListener } doReturn startupResultListener
        on { startupStateStorage } doReturn startupStateStorage
        on { clidsStorage } doReturn clidsStorage
        on { clidsStateChecker } doReturn clidsStateChecker
        on { multiProcessSafeUuidProvider } doReturn uuidProvider
        on { uuidValidator } doReturn uuidValidator
        on { timeProvider } doReturn timeProvider
        on { packageName } doReturn packageName
        on { startupStateHolder } doReturn startupStateHolder
        on { requestConfigArguments } doReturn requestConfigArguments
    }

    private lateinit var startupUnit: StartupUnit
    private lateinit var startupState: StartupState

    @Test
    fun generatedIdentifiersNotifies() {
        createStartupUnitToModifyStartup()
        checkListenersNotified(1)
    }

    @Test
    fun didNotGenerateIdentifiersButConstructorNotifies() {
        createStartupUnitToNotModifyStartup()
        checkListenersNotified(1)
    }

    @Test
    fun onRequestCompleteNotifies() {
        createStartupUnitToNotModifyStartup()
        checkListenersNotified(1)

        val collectingFlags = mock<CollectingFlags>()
        val chosenClids = mock<ClidsInfo.Candidate>()
        whenever(startupRequestConfig.chosenClids).thenReturn(chosenClids)
        val startupResult = mock<StartupResult> {
            on { validTimeDifference } doReturn 100L
            on { collectionFlags } doReturn collectingFlags
        }
        ServerTime.getInstance().init()
        whenever(startupConfigurationHolder.startupState).thenReturn(startupState)
        startupUnit.onRequestComplete(startupResult, startupRequestConfig, null)
        checkListenersNotified(2)
    }

    @Test
    fun updateConfigurationNotifiesListener() {
        createStartupUnitToNotModifyStartup()
        checkListenersNotified(1)
        whenever(startupConfigurationHolder.startupState).thenReturn(startupState)
        val arguments = mock<StartupRequestConfig.Arguments>()
        whenever(startupRequestConfig.newCustomHosts).thenReturn(null)
        whenever(startupRequestConfig.startupHostsFromClient).thenReturn(mutableListOf("host"))
        whenever(startupRequestConfig.hasNewCustomHosts()).thenReturn(true)
        startupUnit.updateConfiguration(arguments)
        checkListenersNotified(2)
    }

    private fun checkListenersNotified(times: Int) {
        val objects: MutableList<Any> = ArrayList()
        for (i in 0 until times) {
            objects.add(startupConfigurationHolder)
            objects.add(startupStateHolder)
            objects.add(startupResultListener)
        }
        inOrder(*objects.toTypedArray()) {
            for (i in 0 until times) {
                verify(startupConfigurationHolder).updateStartupState(any())
                verify(startupStateHolder).onStartupStateChanged(any())
                verify(startupResultListener).onStartupChanged(eq(packageName), any())
            }
        }
    }

    private fun createStartupUnitToModifyStartup() {
        startupState = TestUtils.createDefaultStartupStateBuilder()
            .withUuid("uuid")
            .withDeviceId("deviceId")
            .build()
        whenever(uuidValidator.isValid("uuid")).thenReturn(true)
        whenever(startupConfigurationHolder.get()).thenReturn(startupRequestConfig)
        whenever(startupConfigurationHolder.startupState).thenReturn(startupState)
        whenever(deviceIdGenerator.generateDeviceId()).thenReturn("deviceid")
        startupUnit = StartupUnit(startupUnitComponents)
        startupUnit.init()
    }

    private fun createStartupUnitToNotModifyStartup() {
        whenever(startupConfigurationHolder.get()).thenReturn(startupRequestConfig)
        startupState = TestUtils.createDefaultStartupStateBuilder()
            .withUuid("uuid")
            .withDeviceId("deviceId")
            .build()
        whenever(uuidValidator.isValid("uuid")).thenReturn(true)
        whenever(startupConfigurationHolder.get()).thenReturn(startupRequestConfig)
        whenever(startupConfigurationHolder.startupState).thenReturn(startupState)
        startupUnit = StartupUnit(startupUnitComponents)
        startupUnit.init()
    }
}
