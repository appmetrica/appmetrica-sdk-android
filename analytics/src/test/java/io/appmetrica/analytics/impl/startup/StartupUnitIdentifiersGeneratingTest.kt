package io.appmetrica.analytics.impl.startup

import android.content.Context
import io.appmetrica.analytics.coreapi.internal.identifiers.IdentifierStatus
import io.appmetrica.analytics.impl.ClidsInfoStorage
import io.appmetrica.analytics.impl.StartupStateHolder
import io.appmetrica.analytics.impl.component.ComponentId
import io.appmetrica.analytics.impl.request.StartupRequestConfig
import io.appmetrica.analytics.impl.startup.uuid.MultiProcessSafeUuidProvider
import io.appmetrica.analytics.impl.startup.uuid.UuidValidator
import io.appmetrica.analytics.impl.utils.DeviceIdGenerator
import io.appmetrica.analytics.internal.IdentifiersResult
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.GlobalServiceLocatorRule
import io.appmetrica.analytics.testutils.TestUtils
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
internal class StartupUnitIdentifiersGeneratingTest : CommonTest() {

    private var startupState: StartupState? = null
    private val generatedUuid = "generated uuid"
    private val invalidUuid = "invalid uuid"
    private val generatedDeviceId = "generated deviceid"
    private val packageName = "test.package"

    private val context: Context = mock()
    private val startupConfigurationHolder: StartupConfigurationHolder = mock()
    private val componentId: ComponentId = mock()
    private val startupResultListener: StartupResultListener = mock()
    private val storage: StartupState.Storage = mock()
    private val deviceIdGenerator: DeviceIdGenerator = mock()

    private val clidsStorage: ClidsInfoStorage = mock()
    private val clidsStateChecker: ClidsStateChecker = mock()

    private val uuidProvider: MultiProcessSafeUuidProvider = mock {
        on { readUuid() } doReturn IdentifiersResult(generatedUuid, IdentifierStatus.OK, null)
    }

    private val uuidValidator: UuidValidator = mock {
        on { isValid(generatedUuid) } doReturn true
        on { isValid(invalidUuid) } doReturn false
    }

    private val startupStateHolder: StartupStateHolder = mock()

    private val requestConfigArguments: StartupRequestConfig.Arguments = mock()

    private val startupUnitComponents: StartupUnitComponents = mock {
        on { context } doReturn context
        on { packageName } doReturn packageName
        on { startupConfigurationHolder } doReturn startupConfigurationHolder
        on { clidsStorage } doReturn clidsStorage
        on { clidsStateChecker } doReturn clidsStateChecker
        on { startupConfigurationHolder } doReturn startupConfigurationHolder
        on { multiProcessSafeUuidProvider } doReturn uuidProvider
        on { uuidValidator } doReturn uuidValidator
        on { deviceIdGenerator } doReturn deviceIdGenerator
        on { startupStateStorage } doReturn storage
        on { componentId } doReturn componentId
        on { resultListener } doReturn startupResultListener
        on { startupStateHolder } doReturn startupStateHolder
        on { requestConfigArguments } doReturn requestConfigArguments
    }

    private val startupStateCaptor = argumentCaptor<StartupState>()

    @get:Rule
    val globalServiceLocatorRule = GlobalServiceLocatorRule()

    @Test
    fun uuidGeneratedIfEmpty() {
        checkUuidGenerated("")
    }

    @Test
    fun uuidGeneratedIfNull() {
        checkUuidGenerated(null)
    }

    @Test
    fun uuidGeneratedIfInvalid() {
        checkUuidGenerated(invalidUuid)
    }

    @Test
    fun deviceIdGeneratedIfEmpty() {
        checkDeviceIdGenerated("")
    }

    @Test
    fun deviceIdGeneratedIfNull() {
        checkDeviceIdGenerated(null)
    }

    @Test
    fun deviceIdIsNotUpdated() {
        whenever(deviceIdGenerator.generateDeviceId()).thenReturn(generatedDeviceId)
        createStartupUnit("uuid", "")
        verify(storage).save(startupStateCaptor.capture())
        var startupState = startupStateCaptor.lastValue
        assertThat(startupState.deviceId).isEqualTo(generatedDeviceId)
        createStartupUnit("uuid", generatedDeviceId)
        verify(storage, times(2)).save(startupStateCaptor.capture())
        startupState = startupStateCaptor.lastValue
        assertThat(startupState.deviceId).isEqualTo(generatedDeviceId)
    }

    private fun checkDeviceIdGenerated(deviceId: String?) {
        whenever(deviceIdGenerator.generateDeviceId()).thenReturn(generatedDeviceId)
        createStartupUnit("uuid", deviceId)
        verify(deviceIdGenerator).generateDeviceId()
        verify(storage).save(startupStateCaptor.capture())
        val startupState = startupStateCaptor.firstValue
        assertThat(startupState.deviceId).isEqualTo(generatedDeviceId)
        assertThat(startupState.deviceIdHash).isEmpty()
        verify(startupConfigurationHolder).updateStartupState(startupState)
    }

    private fun checkUuidGenerated(uuid: String?) {
        createStartupUnit(uuid, "deviceid")
        verify(uuidProvider).readUuid()
        verify(storage).save(startupStateCaptor.capture())
        val startupState = startupStateCaptor.firstValue
        assertThat(startupState.uuid).isEqualTo(generatedUuid)
        verify(startupConfigurationHolder).updateStartupState(startupState)
    }

    private fun createStartupUnit(uuid: String?, deviceId: String?) {
        startupState = TestUtils.createDefaultStartupStateBuilder()
            .withUuid(uuid)
            .withDeviceId(deviceId)
            .build()
        whenever(startupConfigurationHolder.startupState).thenReturn(startupState)
        val startupUnit = StartupUnit(startupUnitComponents)
        startupUnit.init()
    }
}
