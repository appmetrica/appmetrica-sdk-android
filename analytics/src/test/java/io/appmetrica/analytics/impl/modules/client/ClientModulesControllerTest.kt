package io.appmetrica.analytics.impl.modules.client

import android.os.Bundle
import io.appmetrica.analytics.coreapi.internal.identifiers.SdkIdentifiers
import io.appmetrica.analytics.impl.IReporterExtended
import io.appmetrica.analytics.impl.modules.client.context.CoreClientContext
import io.appmetrica.analytics.impl.modules.client.context.CoreModuleAdRevenueContext
import io.appmetrica.analytics.impl.selfreporting.AppMetricaSelfReportFacade
import io.appmetrica.analytics.modulesapi.internal.client.BundleToServiceConfigConverter
import io.appmetrica.analytics.modulesapi.internal.client.ModuleClientEntryPoint
import io.appmetrica.analytics.modulesapi.internal.client.ServiceConfigExtensionConfiguration
import io.appmetrica.analytics.modulesapi.internal.client.ServiceConfigUpdateListener
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.constructionRule
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
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
internal class ClientModulesControllerTest : CommonTest() {

    private val firstModuleIdentifier = "firstModuleIdentifier"
    private val firstBundleParser: BundleToServiceConfigConverter<Any> = mock()
    private val firstServiceConfigUpdateListener: ServiceConfigUpdateListener<Any> = mock()
    private val firstServiceConfigExtensionConfiguration: ServiceConfigExtensionConfiguration<Any> = mock {
        on { getServiceConfigUpdateListener() } doReturn firstServiceConfigUpdateListener
        on { getBundleConverter() } doReturn firstBundleParser
    }
    private val firstModule = mock<ModuleClientEntryPoint<Any>> {
        on { identifier } doReturn firstModuleIdentifier
        on { serviceConfigExtensionConfiguration } doReturn firstServiceConfigExtensionConfiguration
    }

    private val secondModuleIdentifier = "secondModuleIdentifier"
    private val secondBundleParser: BundleToServiceConfigConverter<Any> = mock()
    private val secondServiceConfigUpdateListener: ServiceConfigUpdateListener<Any> = mock()
    private val secondServiceConfigExtensionConfiguration: ServiceConfigExtensionConfiguration<Any> = mock {
        on { getServiceConfigUpdateListener() } doReturn secondServiceConfigUpdateListener
        on { getBundleConverter() } doReturn secondBundleParser
    }
    private val secondModule = mock<ModuleClientEntryPoint<Any>> {
        on { identifier } doReturn secondModuleIdentifier
        on { serviceConfigExtensionConfiguration } doReturn secondServiceConfigExtensionConfiguration
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
    @get:Rule
    val clientModuleServiceConfigModelFactoryRule = constructionRule<ClientModuleServiceConfigModelFactory>()

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

    @Test
    fun notifyModulesWithConfig() {
        val bundle = Bundle()
        val identifiers: SdkIdentifiers = mock()
        whenever(clientModuleServiceConfigModelFactory().createClientModuleServiceConfigModel(
            bundle,
            firstModuleIdentifier,
            identifiers,
            firstServiceConfigExtensionConfiguration
        )).thenReturn(null)
        val secondConfig: ClientModuleServiceConfigModel<Any?> = mock()
        whenever(clientModuleServiceConfigModelFactory().createClientModuleServiceConfigModel(
            bundle,
            secondModuleIdentifier,
            identifiers,
            secondServiceConfigExtensionConfiguration
        )).thenReturn(secondConfig)

        modulesController.registerModule(firstModule)
        modulesController.registerModule(secondModule)

        modulesController.notifyModulesWithConfig(bundle, identifiers)

        verify(firstModule.serviceConfigExtensionConfiguration!!.getServiceConfigUpdateListener(), never())
            .onServiceConfigUpdated(any())
        verify(secondModule.serviceConfigExtensionConfiguration!!.getServiceConfigUpdateListener())
            .onServiceConfigUpdated(secondConfig)
    }

    private fun clientModuleServiceConfigModelFactory(): ClientModuleServiceConfigModelFactory {
        assertThat(clientModuleServiceConfigModelFactoryRule.constructionMock.constructed()).hasSize(1)
        assertThat(clientModuleServiceConfigModelFactoryRule.argumentInterceptor.flatArguments()).isEmpty()
        return clientModuleServiceConfigModelFactoryRule.constructionMock.constructed().first()
    }
}
