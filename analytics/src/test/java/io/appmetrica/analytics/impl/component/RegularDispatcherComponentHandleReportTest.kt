package io.appmetrica.analytics.impl.component

import android.content.Context
import io.appmetrica.analytics.impl.CounterReport
import io.appmetrica.analytics.impl.InternalEvents
import io.appmetrica.analytics.impl.component.clients.ClientUnit
import io.appmetrica.analytics.impl.component.clients.ComponentUnitFactory
import io.appmetrica.analytics.impl.request.StartupArgumentsTest
import io.appmetrica.analytics.impl.startup.StartupCenter
import io.appmetrica.analytics.impl.startup.StartupUnit
import io.appmetrica.analytics.testutils.CommonTest
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.same
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.robolectric.ParameterizedRobolectricTestRunner

@RunWith(ParameterizedRobolectricTestRunner::class)
class RegularDispatcherComponentHandleReportTest(
    eventType: Int,
    private val shouldUpdateConfig: Boolean
) : CommonTest() {

    private val context: Context = mock()
    private val componentId: ComponentId = mock()
    private val startupArguments = StartupArgumentsTest.empty()
    private val reportArguments = CommonArgumentsTestUtils.emptyReporterArguments()
    private val reportingComponent: ComponentUnit = mock()
    private val startupUnit: StartupUnit = mock()

    private val startupCenter: StartupCenter = mock {
        on { getOrCreateStartupUnit(same(context), any(), any()) } doReturn startupUnit
    }

    private val clientConfiguration: CommonArguments = CommonArguments(startupArguments, reportArguments, null)

    private val componentUnitFactory: ComponentUnitFactory<ComponentUnit> = mock {
        on { createComponentUnit(same(context), same(componentId), any(), any()) } doReturn reportingComponent
    }

    private val lifecycleManager: ComponentLifecycleManager<ClientUnit?> = mock()
    private val counterReport: CounterReport = CounterReport(null, eventType)

    private val regularDispatcherComponent: RegularDispatcherComponent<ComponentUnit> by setUp {
        RegularDispatcherComponent(
            context,
            componentId,
            clientConfiguration,
            componentUnitFactory,
            lifecycleManager,
            startupCenter
        )
    }

    @Test
    fun handleReport() {
        regularDispatcherComponent.handleReport(counterReport, CommonArgumentsTestUtils.createMockedArguments())
        verify(reportingComponent).handleReport(counterReport)
        verify(reportingComponent, times(if (shouldUpdateConfig) 1 else 0)).updateSdkConfig(reportArguments)
    }

    companion object {

        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters(name = "[{index}]Event with type = {0} updateConfig ? {1}")
        fun data(): Collection<Array<Any>> = InternalEvents.values().map {
            arrayOf(
                it.typeId,
                it != InternalEvents.EVENT_TYPE_UPDATE_FOREGROUND_TIME &&
                    it != InternalEvents.EVENT_TYPE_EXCEPTION_UNHANDLED_FROM_FILE &&
                    it != InternalEvents.EVENT_TYPE_PREV_SESSION_EXCEPTION_UNHANDLED_FROM_FILE &&
                    it != InternalEvents.EVENT_TYPE_PREV_SESSION_NATIVE_CRASH_PROTOBUF &&
                    it != InternalEvents.EVENT_TYPE_CURRENT_SESSION_NATIVE_CRASH_PROTOBUF
            )
        }
    }
}
