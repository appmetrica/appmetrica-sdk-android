package io.appmetrica.analytics.impl.modules.client

import io.appmetrica.analytics.impl.IReporterExtended
import io.appmetrica.analytics.impl.modules.client.context.CoreClientContext
import io.appmetrica.analytics.impl.modules.client.context.CoreModuleAdRevenueContext
import io.appmetrica.analytics.impl.selfreporting.AppMetricaSelfReportFacade
import io.appmetrica.analytics.modulesapi.internal.client.ModuleClientEntryPoint
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.on
import io.appmetrica.analytics.testutils.staticRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
internal class ClientModulesControllerTest : CommonTest() {

    private val firstModuleIdentifier = "firstModuleIdentifier"
    private val firstModule = mock<ModuleClientEntryPoint<Any>> {
        on { identifier } doReturn firstModuleIdentifier
    }

    private val secondModuleIdentifier = "secondModuleIdentifier"
    private val secondModule = mock<ModuleClientEntryPoint<Any>> {
        on { identifier } doReturn secondModuleIdentifier
    }

    private val initException = RuntimeException("initException")
    private val activatedException = RuntimeException("activatedException")
    private val brokenModuleIdentifier = "brokenModuleIdentifier"
    private val brokenModule = mock<ModuleClientEntryPoint<Any>> {
        on { identifier } doReturn brokenModuleIdentifier
        on { initClientSide(any()) } doThrow initException
        on { onActivated() } doThrow activatedException
    }

    private val selfReporter = mock<IReporterExtended>()
    @get:Rule
    val selfReporterFacadeMockedStaticRule = staticRule<AppMetricaSelfReportFacade> {
        on { AppMetricaSelfReportFacade.getReporter() } doReturn selfReporter
    }

    private val modulesController: ClientModulesController by setUp { ClientModulesController() }

    @Test
    fun initClientSide() {
        modulesController.registerModule(brokenModule)
        modulesController.registerModule(firstModule)
        modulesController.registerModule(secondModule)

        val clientContext: CoreClientContext = mock()
        modulesController.initClientSide(clientContext)

        verify(firstModule).initClientSide(clientContext)
        verify(secondModule).initClientSide(clientContext)
        verify(selfReporter).reportEvent(
            "client_module_errors",
            mapOf(brokenModuleIdentifier to mapOf ("initClientSide" to initException.stackTraceToString()))
        )
    }

    @Test
    fun initClientSideRemovesBrokenModules() {
        modulesController.registerModule(brokenModule)
        modulesController.registerModule(firstModule)
        modulesController.registerModule(secondModule)

        val clientContext: CoreClientContext = mock()
        modulesController.initClientSide(clientContext)
        modulesController.onActivated()

        verify(brokenModule, times(0)).onActivated()
    }

    @Test
    fun onActivated() {
        modulesController.registerModule(brokenModule)
        modulesController.registerModule(firstModule)
        modulesController.registerModule(secondModule)
        modulesController.onActivated()

        verify(firstModule).onActivated()
        verify(secondModule).onActivated()
        verify(selfReporter).reportEvent(
            "client_module_errors",
            mapOf(brokenModuleIdentifier to mapOf ("onActivated" to activatedException.stackTraceToString()))
        )
    }

    @Test
    fun getModuleAdRevenueProcessor() {
        val holder: CompositeModuleAdRevenueProcessor = mock()
        val moduleContext: CoreModuleAdRevenueContext = mock {
            on { adRevenueProcessorsHolder } doReturn holder
        }
        val clientContext: CoreClientContext = mock {
            on { moduleAdRevenueContext } doReturn moduleContext
        }
        modulesController.initClientSide(clientContext)
        assertThat(modulesController.getModuleAdRevenueProcessor()).isSameAs(holder)
    }

    @Test
    fun getModuleAdRevenueProcessorIfContextIsNull() {
        assertThat(modulesController.getModuleAdRevenueProcessor()).isNull()
    }
}
