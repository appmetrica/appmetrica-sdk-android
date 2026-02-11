package io.appmetrica.analytics.impl.component

import android.content.Context
import io.appmetrica.analytics.impl.CounterReport
import io.appmetrica.analytics.impl.InternalEvents
import io.appmetrica.analytics.impl.component.CommonArguments.ReporterArguments
import io.appmetrica.analytics.impl.component.clients.ClientUnit
import io.appmetrica.analytics.impl.component.clients.ComponentUnitFactory
import io.appmetrica.analytics.impl.request.StartupArgumentsTest
import io.appmetrica.analytics.impl.request.StartupRequestConfig
import io.appmetrica.analytics.impl.startup.StartupCenter
import io.appmetrica.analytics.impl.startup.StartupError
import io.appmetrica.analytics.impl.startup.StartupState
import io.appmetrica.analytics.impl.startup.StartupUnit
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.GlobalServiceLocatorRule
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentMatchers
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.same
import org.mockito.kotlin.times
import org.mockito.kotlin.verify

internal class RegularDispatcherComponentTest : CommonTest() {

    private var context: Context = mock()
    private val componentId: ComponentId = mock()
    private val reportingComponent: ComponentUnit = mock()

    private val componentUnitFactory: ComponentUnitFactory<ComponentUnit> = mock {
        on { createComponentUnit(same(context), same(componentId), any(), any()) } doReturn reportingComponent
    }

    private val startupArguments = StartupArgumentsTest.empty()
    private val reportArguments = CommonArgumentsTestUtils.emptyReporterArguments()
    private var clientConfiguration: CommonArguments = CommonArguments(startupArguments, reportArguments, null)

    private val lifecycleManager: ComponentLifecycleManager<ClientUnit?> = mock()
    private val startupUnit: StartupUnit = mock()

    private val startupCenter: StartupCenter = mock {
        on {
            getOrCreateStartupUnit(
                ArgumentMatchers.any(
                    Context::class.java
                ),
                ArgumentMatchers.any(ComponentId::class.java),
                ArgumentMatchers.any(
                    StartupRequestConfig.Arguments::class.java
                )
            )
        } doReturn startupUnit
    }

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

    private val clientUnit: ClientUnit = mock()

    @get:Rule
    val globalServiceLocatorRule = GlobalServiceLocatorRule()

    @Test
    fun listenerRegistered() {
        verify(startupCenter).registerStartupListener(componentId, regularDispatcherComponent)
    }

    @Test
    fun handleReport() {
        val activationReport = CounterReport()
        activationReport.type = InternalEvents.EVENT_TYPE_ACTIVATION.typeId
        val regularReport = CounterReport()
        regularReport.type = InternalEvents.EVENT_TYPE_REGULAR.typeId
        val startReport = CounterReport()
        startReport.type = InternalEvents.EVENT_TYPE_START.typeId
        regularDispatcherComponent.handleReport(activationReport, CommonArgumentsTestUtils.createMockedArguments())
        regularDispatcherComponent.handleReport(regularReport, CommonArgumentsTestUtils.createMockedArguments())
        regularDispatcherComponent.handleReport(startReport, CommonArgumentsTestUtils.createMockedArguments())
        verify(reportingComponent).handleReport(activationReport)
        verify(reportingComponent).handleReport(regularReport)
        verify(reportingComponent).handleReport(startReport)
    }

    @Test
    fun `updateConfig before reportEvent`() {
        regularDispatcherComponent.updateConfig(clientConfiguration)
        verify(reportingComponent).updateSdkConfig(reportArguments)
    }

    @Test
    fun `connectClient dispatch client to lifecycle manager`() {
        regularDispatcherComponent.connectClient(clientUnit)
        verify(lifecycleManager, times(1)).connectClient(clientUnit)
    }

    @Test
    fun `disconnectClient dispatch client to lifecycle manager`() {
        regularDispatcherComponent.disconnectClient(clientUnit)
        verify(lifecycleManager, times(1)).disconnectClient(clientUnit)
    }

    @Test
    fun startupChanged() {
        warmUpAllComponents()
        val newState: StartupState = mock()
        regularDispatcherComponent.onStartupChanged(newState)
        verify(reportingComponent).onStartupChanged(newState)
    }

    @Test
    fun startupError() {
        warmUpAllComponents()
        val error = StartupError.UNKNOWN
        val startupState: StartupState = mock()
        regularDispatcherComponent.onStartupError(error, startupState)
        verify(reportingComponent).onStartupError(error, startupState)
    }

    @Test
    fun updateConfig() {
        val reporterArguments: ReporterArguments = mock()
        val startupArguments: StartupRequestConfig.Arguments = mock()
        val arguments = CommonArguments(startupArguments, reporterArguments, null)
        regularDispatcherComponent.updateConfig(arguments)
        verify(reportingComponent).updateSdkConfig(reporterArguments)
        verify(startupUnit).updateConfiguration(startupArguments)
    }

    private fun warmUpAllComponents() {
        val regularReport = CounterReport()
        regularReport.type = InternalEvents.EVENT_TYPE_REGULAR.typeId
        regularDispatcherComponent.handleReport(
            regularReport,
            CommonArgumentsTestUtils.createMockedArguments()
        )
    }
}
