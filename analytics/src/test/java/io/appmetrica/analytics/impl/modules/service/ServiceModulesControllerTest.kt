package io.appmetrica.analytics.impl.modules.service

import android.location.Location
import android.os.Bundle
import io.appmetrica.analytics.coreapi.internal.backport.Consumer
import io.appmetrica.analytics.coreapi.internal.control.Toggle
import io.appmetrica.analytics.coreapi.internal.data.Converter
import io.appmetrica.analytics.coreapi.internal.data.JsonParser
import io.appmetrica.analytics.coreapi.internal.permission.PermissionStrategy
import io.appmetrica.analytics.impl.GlobalServiceLocator
import io.appmetrica.analytics.impl.modules.ModuleEventHandlersHolder
import io.appmetrica.analytics.impl.modules.ModuleRemoteConfigController
import io.appmetrica.analytics.impl.permissions.NeverForbidPermissionStrategy
import io.appmetrica.analytics.impl.selfreporting.AppMetricaSelfReportFacade
import io.appmetrica.analytics.impl.selfreporting.SelfReporterWrapper
import io.appmetrica.analytics.impl.startup.StartupState
import io.appmetrica.analytics.modulesapi.internal.common.AskForPermissionStrategyModuleProvider
import io.appmetrica.analytics.modulesapi.internal.service.ClientConfigProvider
import io.appmetrica.analytics.modulesapi.internal.service.LocationServiceExtension
import io.appmetrica.analytics.modulesapi.internal.service.ModuleLocationSourcesServiceController
import io.appmetrica.analytics.modulesapi.internal.service.ModuleServiceEntryPoint
import io.appmetrica.analytics.modulesapi.internal.service.ModuleServicesDatabase
import io.appmetrica.analytics.modulesapi.internal.service.RemoteConfigExtensionConfiguration
import io.appmetrica.analytics.modulesapi.internal.service.RemoteConfigUpdateListener
import io.appmetrica.analytics.modulesapi.internal.service.ServiceContext
import io.appmetrica.analytics.modulesapi.internal.service.event.ModuleEventServiceHandlerFactory
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.GlobalServiceLocatorRule
import io.appmetrica.analytics.testutils.MockProvider
import io.appmetrica.analytics.testutils.MockedConstructionRule
import io.appmetrica.analytics.testutils.on
import io.appmetrica.analytics.testutils.staticRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.clearInvocations
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever

internal class ServiceModulesControllerTest : CommonTest() {

    private val firstModuleFeatures = listOf("First module feature")
    private val firstModuleBlocks = mapOf("First module block" to 1)
    private val firstModuleJsonParser = mock<JsonParser<Any>>()
    private val firstModuleProtobufConverter = mock<Converter<Any, ByteArray>>()
    private val firstModuleRemoteConfigListener = mock<RemoteConfigUpdateListener<Any>>()
    private val firstModuleEventHandlerFactory = mock<ModuleEventServiceHandlerFactory>()

    private val firstRemoteConfigExtensionConfiguration = mock<RemoteConfigExtensionConfiguration<Any>> {
        on { getFeatures() } doReturn firstModuleFeatures
        on { getBlocks() } doReturn firstModuleBlocks
        on { getJsonParser() } doReturn firstModuleJsonParser
        on { getProtobufConverter() } doReturn firstModuleProtobufConverter
        on { getRemoteConfigUpdateListener() } doReturn firstModuleRemoteConfigListener
    }
    private val firstModuleIdentifier = "First module with remote config identifier"

    private val firstLocationConsumer = mock<Consumer<Location?>>()
    private val firstLocationSourcesController = mock<ModuleLocationSourcesServiceController>()
    private val firstLocationControllerAppStateToggle = mock<Toggle>()

    private val firstLocationExtension = mock<LocationServiceExtension> {
        on { locationConsumer } doReturn firstLocationConsumer
        on { locationSourcesController } doReturn firstLocationSourcesController
        on { locationControllerAppStateToggle } doReturn firstLocationControllerAppStateToggle
    }

    private val firstModuleServiceDatabase = mock<ModuleServicesDatabase>()
    private val firstClientConfigProvider: ClientConfigProvider = mock()

    private val firstModule = mock<ModuleServiceEntryPoint<Any>> {
        on { identifier } doReturn firstModuleIdentifier
        on { remoteConfigExtensionConfiguration } doReturn firstRemoteConfigExtensionConfiguration
        on { moduleEventServiceHandlerFactory } doReturn firstModuleEventHandlerFactory
        on { locationServiceExtension } doReturn firstLocationExtension
        on { moduleServicesDatabase } doReturn firstModuleServiceDatabase
        on { clientConfigProvider } doReturn firstClientConfigProvider
    }

    private val secondModuleFeatures = listOf("Second module feature #1", "Second module feature #2")
    private val secondModuleBlocks = mapOf("Second module block #1" to 1, "Second module block #2" to 2)
    private val secondModuleJsonParser = mock<JsonParser<Any>>()
    private val secondModuleProtobufConverter = mock<Converter<Any, ByteArray>>()
    private val secondModuleConfigUpdateListener = mock<RemoteConfigUpdateListener<Any>>()
    private val secondModuleEventHandlerFactory = mock<ModuleEventServiceHandlerFactory>()

    private val secondRemoteConfigExtensionConfiguration = mock<RemoteConfigExtensionConfiguration<Any>> {
        on { getFeatures() } doReturn secondModuleFeatures
        on { getBlocks() } doReturn secondModuleBlocks
        on { getJsonParser() } doReturn secondModuleJsonParser
        on { getProtobufConverter() } doReturn secondModuleProtobufConverter
        on { getRemoteConfigUpdateListener() } doReturn secondModuleConfigUpdateListener
    }
    private val secondModuleIdentifier = "Second module with remote config identifier"

    private val secondLocationConsumer = mock<Consumer<Location?>>()
    private val secondLocationSourcesController = mock<ModuleLocationSourcesServiceController>()
    private val secondLocationControllerAppStateToggle = mock<Toggle>()

    private val secondLocationExtension = mock<LocationServiceExtension> {
        on { locationConsumer } doReturn secondLocationConsumer
        on { locationSourcesController } doReturn secondLocationSourcesController
        on { locationControllerAppStateToggle } doReturn secondLocationControllerAppStateToggle
    }

    private val secondModuleServicesDatabase = mock<ModuleServicesDatabase>()
    private val secondClientConfigProvider: ClientConfigProvider = mock()

    private val secondModule = mock<ModuleServiceEntryPoint<Any>> {
        on { identifier } doReturn secondModuleIdentifier
        on { remoteConfigExtensionConfiguration } doReturn secondRemoteConfigExtensionConfiguration
        on { moduleEventServiceHandlerFactory } doReturn secondModuleEventHandlerFactory
        on { locationServiceExtension } doReturn secondLocationExtension
        on { moduleServicesDatabase } doReturn secondModuleServicesDatabase
        on { clientConfigProvider } doReturn secondClientConfigProvider
    }

    private val moduleWithoutRemoteConfigIdentifier = "Module without remote config identifier"

    private val lightModule = mock<ModuleServiceEntryPoint<Any>> {
        on { identifier } doReturn moduleWithoutRemoteConfigIdentifier
    }

    private val askForPermissionStrategyModuleId = "rp"
    private val askForPermissionStrategy = mock<PermissionStrategy>()
    private val askForPermissionStrategyModule = mock<AskForPermissionStrategyModule> {
        on { identifier } doReturn askForPermissionStrategyModuleId
        on { askForPermissionStrategy } doReturn askForPermissionStrategy
    }

    private val serviceContext = mock<ServiceContext>()

    private val firstModuleRemoteConfig = mock<ServiceModuleRemoteConfigModel<Any?>>()
    private val secondModuleRemoteConfig = mock<ServiceModuleRemoteConfigModel<Any?>>()
    private val moduleWithoutRemoteConfigFullConfig = mock<ServiceModuleRemoteConfigModel<Any?>>()

    private val startupState = mock<StartupState>()

    @get:Rule
    val bundleConstructionRule = MockedConstructionRule(Bundle::class.java) { mock, _ ->
        val data = mutableMapOf<String, Any?>()
        whenever(mock.putBundle(any(), any())).thenAnswer { invocation ->
            data[invocation.getArgument(0)] = invocation.getArgument(1)
            null
        }
        whenever(mock.getBundle(any<String>())).thenAnswer { invocation ->
            data[invocation.getArgument(0)] as? Bundle
        }
        whenever(mock.keySet()).thenAnswer {
            data.keys
        }
    }

    @get:Rule
    val configProviderMockedConstructionRule = MockedConstructionRule(
        ModuleRemoteConfigProvider::class.java
    ) { mock, context ->
        whenever(mock.getRemoteConfigForServiceModule(firstModuleIdentifier))
            .thenReturn(firstModuleRemoteConfig)
        whenever(mock.getRemoteConfigForServiceModule(secondModuleIdentifier))
            .thenReturn(secondModuleRemoteConfig)
        whenever(mock.getRemoteConfigForServiceModule(moduleWithoutRemoteConfigIdentifier))
            .thenReturn(moduleWithoutRemoteConfigFullConfig)
    }

    @get:Rule
    val neverForbidPermissionStrategyMockedConstructionRule =
        MockedConstructionRule(NeverForbidPermissionStrategy::class.java)

    @get:Rule
    val globalServiceLocatorRule = GlobalServiceLocatorRule()

    private val selfReporter = mock<SelfReporterWrapper>()

    @get:Rule
    val selfReporterFacadeMockedStaticRule = staticRule<AppMetricaSelfReportFacade> {
        on { AppMetricaSelfReportFacade.getReporter() } doReturn selfReporter
    }

    private lateinit var modulesController: ServiceModulesController
    private lateinit var moduleEventHandlersHolder: ModuleEventHandlersHolder
    private lateinit var neverForbidPermissionStrategy: NeverForbidPermissionStrategy

    @Before
    fun setUp() {
        modulesController = ServiceModulesController()
        moduleEventHandlersHolder = GlobalServiceLocator.getInstance().moduleEventHandlersHolder
        neverForbidPermissionStrategy = alwaysAskForPermissionStrategy()
    }

    @Test
    fun `register module should not register moduleEventHandler if null`() {
        modulesController.registerModule(lightModule)
        verifyNoMoreInteractions(moduleEventHandlersHolder)
    }

    @Test
    fun collectFeaturesWithoutModules() {
        assertThat(modulesController.collectFeatures()).isEmpty()
    }

    @Test
    fun collectFeaturesWithSingleModuleWithoutRemoteConfig() {
        modulesController.registerModule(lightModule)
        assertThat(modulesController.collectFeatures()).isEmpty()
    }

    @Test
    fun collectFeaturesWithModuleWithoutFeatures() {
        val remoteConfigExtensionConfigurationValue = mock<RemoteConfigExtensionConfiguration<Any>> {
            on { getFeatures() } doReturn emptyList()
        }
        val module = mock<ModuleServiceEntryPoint<Any>> {
            on { remoteConfigExtensionConfiguration } doReturn remoteConfigExtensionConfigurationValue
        }
        modulesController.registerModule(module)
        assertThat(modulesController.collectFeatures()).isEmpty()
    }

    @Test
    fun collectFeatureWithSingleModuleWithRemoteConfig() {
        modulesController.registerModule(firstModule)
        assertThat(modulesController.collectFeatures()).containsExactlyElementsOf(firstModuleFeatures)
    }

    @Test
    fun collectFeaturesWithSeveralModules() {
        modulesController.registerModule(firstModule)
        modulesController.registerModule(secondModule)
        modulesController.registerModule(lightModule)
        assertThat(modulesController.collectFeatures())
            .containsExactlyElementsOf(firstModuleFeatures + secondModuleFeatures)
    }

    @Test
    fun collectFeaturesIfModuleThrowException() {
        modulesController.registerModule(firstModule)
        modulesController.registerModule(secondModule)

        val exception = RuntimeException()
        doThrow(exception).whenever(firstModule).remoteConfigExtensionConfiguration

        assertThat(modulesController.collectFeatures()).containsExactlyElementsOf(secondModuleFeatures)

        verify(selfReporter).reportEvent(
            "service_module_errors",
            mapOf(firstModuleIdentifier to mapOf("features" to exception.stackTraceToString()))
        )

        clearInvocations(firstModule, secondModule)
        modulesController.onStartupStateChanged(startupState)
        verifyNoInteractions(firstModule)
        verify(secondModuleConfigUpdateListener).onRemoteConfigUpdated(secondModuleRemoteConfig)
    }

    @Test
    fun collectBlocksWithoutModules() {
        assertThat(modulesController.collectBlocks()).isEmpty()
    }

    @Test
    fun collectBlocksWithSingleModulesWithoutRemoteConfig() {
        modulesController.registerModule(lightModule)
        assertThat(modulesController.collectBlocks()).isEmpty()
    }

    @Test
    fun collectBlocksWithSingleModuleWithRemoteConfig() {
        modulesController.registerModule(firstModule)
        assertThat(modulesController.collectBlocks()).containsAllEntriesOf(firstModuleBlocks)
    }

    @Test
    fun collectBlocksWithSeveralModules() {
        modulesController.registerModule(firstModule)
        modulesController.registerModule(secondModule)
        modulesController.registerModule(lightModule)
        assertThat(modulesController.collectBlocks()).containsAllEntriesOf(firstModuleBlocks + secondModuleBlocks)
    }

    @Test
    fun collectBlocksIfModuleThrowException() {
        modulesController.registerModule(firstModule)
        modulesController.registerModule(secondModule)

        val exception = RuntimeException()
        doThrow(exception).whenever(firstModule).remoteConfigExtensionConfiguration

        assertThat(modulesController.collectBlocks()).containsExactlyEntriesOf(secondModuleBlocks)

        verify(selfReporter).reportEvent(
            "service_module_errors",
            mapOf(firstModuleIdentifier to mapOf("blocks" to exception.stackTraceToString()))
        )

        clearInvocations(firstModule, secondModule)
        modulesController.onStartupStateChanged(startupState)
        verifyNoInteractions(firstModule)
        verify(secondModuleConfigUpdateListener).onRemoteConfigUpdated(secondModuleRemoteConfig)
    }

    @Test
    fun collectRemoteConfigControllersWithoutModules() {
        assertThat(modulesController.collectRemoteConfigControllers()).isEmpty()
    }

    @Test
    fun collectRemoteConfigControllersWithSingleModuleWithoutRemoteConfig() {
        modulesController.registerModule(lightModule)
        assertThat(modulesController.collectRemoteConfigControllers()).isEmpty()
    }

    @Test
    fun collectRemoteConfigControllersWithSingleModuleWithRemoteConfig() {
        modulesController.registerModule(firstModule)
        assertThat(modulesController.collectRemoteConfigControllers())
            .usingRecursiveComparison()
            .isEqualTo(
                mapOf(
                    firstModuleIdentifier
                        to ModuleRemoteConfigController(firstRemoteConfigExtensionConfiguration)
                )
            )
    }

    @Test
    fun collectRemoteConfigControllersIfModuleThrowException() {
        modulesController.registerModule(firstModule)
        modulesController.registerModule(secondModule)

        val exception = RuntimeException()
        doThrow(exception).whenever(firstModule).remoteConfigExtensionConfiguration

        assertThat(modulesController.collectRemoteConfigControllers())
            .usingRecursiveComparison()
            .isEqualTo(
                mapOf(
                    secondModuleIdentifier to ModuleRemoteConfigController(secondRemoteConfigExtensionConfiguration)
                )
            )

        verify(selfReporter).reportEvent(
            "service_module_errors",
            mapOf(firstModuleIdentifier to mapOf("remote_config_controller" to exception.stackTraceToString()))
        )

        clearInvocations(firstModule, secondModule)
        modulesController.onStartupStateChanged(startupState)
        verifyNoInteractions(firstModule)
        verify(secondModuleConfigUpdateListener).onRemoteConfigUpdated(secondModuleRemoteConfig)
    }

    @Test
    fun `collectLocationConsumers without modules`() {
        assertThat(modulesController.collectLocationConsumers()).isEmpty()
    }

    @Test
    fun `collectLocationConsumers with single module without location extension`() {
        modulesController.registerModule(lightModule)
        assertThat(modulesController.collectLocationConsumers()).isEmpty()
    }

    @Test
    fun `collectLocationConsumers with location extension without location consumers`() {
        whenever(firstLocationExtension.locationConsumer).thenReturn(null)
        modulesController.registerModule(firstModule)
        assertThat(modulesController.collectLocationConsumers()).isEmpty()
    }

    @Test
    fun `collectLocationConsumers with single modules with location consumer`() {
        modulesController.registerModule(firstModule)
        assertThat(modulesController.collectLocationConsumers()).containsExactly(firstLocationConsumer)
    }

    @Test
    fun `collectLocationConsumers several modules with location consumer`() {
        modulesController.registerModule(firstModule)
        modulesController.registerModule(secondModule)
        assertThat(modulesController.collectLocationConsumers())
            .containsExactlyInAnyOrder(firstLocationConsumer, secondLocationConsumer)
    }

    @Test
    fun `collectLocationConsumers if module throw exception`() {
        modulesController.registerModule(firstModule)
        modulesController.registerModule(secondModule)

        val exception = RuntimeException()
        doThrow(exception).whenever(firstModule).locationServiceExtension

        assertThat(modulesController.collectLocationConsumers()).containsExactly(secondLocationConsumer)

        verify(selfReporter).reportEvent(
            "service_module_errors",
            mapOf(firstModuleIdentifier to mapOf("location_consumer" to exception.stackTraceToString()))
        )

        clearInvocations(firstModule, secondModule)
        modulesController.onStartupStateChanged(startupState)
        verifyNoInteractions(firstModule)
        verify(secondModuleConfigUpdateListener).onRemoteConfigUpdated(secondModuleRemoteConfig)
    }

    @Test
    fun `collectLocationSourceController without modules`() {
        assertThat(modulesController.chooseLocationSourceController()).isNull()
    }

    @Test
    fun `collectLocationSourceController with single module without location extension`() {
        modulesController.registerModule(lightModule)
        assertThat(modulesController.chooseLocationSourceController()).isNull()
    }

    @Test
    fun `collectLocationSourceController with location extension without location source controller`() {
        whenever(firstLocationExtension.locationSourcesController).thenReturn(null)
        modulesController.registerModule(firstModule)
        assertThat(modulesController.chooseLocationSourceController()).isNull()
    }

    @Test
    fun `collectLocationSourceController with single module with location source controller`() {
        modulesController.registerModule(firstModule)
        assertThat(modulesController.chooseLocationSourceController()).isEqualTo(firstLocationSourcesController)
    }

    @Test
    fun `collectLocationSourceController with several modules with location source controller`() {
        modulesController.registerModule(firstModule)
        modulesController.registerModule(secondModule)
        assertThat(modulesController.chooseLocationSourceController()).isEqualTo(firstLocationSourcesController)
    }

    @Test
    fun `collectLocationSourceController if module throw exception`() {
        modulesController.registerModule(firstModule)
        modulesController.registerModule(secondModule)

        val exception = RuntimeException()
        doThrow(exception).whenever(firstModule).locationServiceExtension

        assertThat(modulesController.chooseLocationSourceController()).isEqualTo(secondLocationSourcesController)

        verify(selfReporter).reportEvent(
            "service_module_errors",
            mapOf(firstModuleIdentifier to mapOf("location_source_controller" to exception.stackTraceToString()))
        )

        clearInvocations(firstModule, secondModule)
        modulesController.onStartupStateChanged(startupState)
        verifyNoInteractions(firstModule)
        verify(secondModuleConfigUpdateListener).onRemoteConfigUpdated(secondModuleRemoteConfig)
    }

    @Test
    fun `collectLocationSourceController if single module throw exception`() {
        modulesController.registerModule(firstModule)

        val exception = RuntimeException()
        doThrow(exception).whenever(firstModule).locationServiceExtension

        assertThat(modulesController.chooseLocationSourceController()).isNull()

        verify(selfReporter).reportEvent(
            "service_module_errors",
            mapOf(firstModuleIdentifier to mapOf("location_source_controller" to exception.stackTraceToString()))
        )
    }

    @Test
    fun `collectLocationAppStateControlToggle without modules`() {
        assertThat(modulesController.chooseLocationAppStateControlToggle()).isNull()
    }

    @Test
    fun `collectLocationAppStateControlToggle with single module without location extension`() {
        modulesController.registerModule(lightModule)
        assertThat(modulesController.chooseLocationAppStateControlToggle()).isNull()
    }

    @Test
    fun `collectLocationAppStateControlToggle with location extension without control toggle`() {
        whenever(firstLocationExtension.locationControllerAppStateToggle).thenReturn(null)
        modulesController.registerModule(firstModule)
        assertThat(modulesController.chooseLocationAppStateControlToggle()).isNull()
    }

    @Test
    fun `collectLocationAppStateControlToggle with single module with control toggle`() {
        modulesController.registerModule(firstModule)
        assertThat(modulesController.chooseLocationAppStateControlToggle())
            .isEqualTo(firstLocationControllerAppStateToggle)
    }

    @Test
    fun `collectLocationAppStateControlToggle with several modules with control toggles`() {
        modulesController.registerModule(firstModule)
        modulesController.registerModule(secondModule)
        assertThat(modulesController.chooseLocationAppStateControlToggle())
            .isEqualTo(firstLocationControllerAppStateToggle)
    }

    @Test
    fun `collectLocationAppStateControlToggle if module throw exception`() {
        modulesController.registerModule(firstModule)
        modulesController.registerModule(secondModule)

        val exception = RuntimeException()
        doThrow(exception).whenever(firstModule).locationServiceExtension

        assertThat(modulesController.chooseLocationAppStateControlToggle())
            .isEqualTo(secondLocationControllerAppStateToggle)

        verify(selfReporter).reportEvent(
            "service_module_errors",
            mapOf(firstModuleIdentifier to mapOf("location_app_state_control_toggle" to exception.stackTraceToString()))
        )

        clearInvocations(firstModule, secondModule)
        modulesController.onStartupStateChanged(startupState)
        verifyNoInteractions(firstModule)
        verify(secondModuleConfigUpdateListener).onRemoteConfigUpdated(secondModuleRemoteConfig)
    }

    @Test
    fun `collectLocationAppStateControlToggle if single module throw exception`() {
        modulesController.registerModule(firstModule)

        val exception = RuntimeException()
        doThrow(exception).whenever(firstModule).locationServiceExtension

        assertThat(modulesController.chooseLocationAppStateControlToggle()).isNull()

        verify(selfReporter).reportEvent(
            "service_module_errors",
            mapOf(firstModuleIdentifier to mapOf("location_app_state_control_toggle" to exception.stackTraceToString()))
        )
    }

    @Test
    fun `collectModuleServiceDatabases without modules`() {
        assertThat(modulesController.collectModuleServiceDatabases()).isEmpty()
    }

    @Test
    fun `collectModuleServiceDatabases with module without module service database`() {
        modulesController.registerModule(lightModule)
        assertThat(modulesController.collectModuleServiceDatabases()).isEmpty()
    }

    @Test
    fun `collectModuleServiceDatabases with single module`() {
        modulesController.registerModule(firstModule)
        assertThat(modulesController.collectModuleServiceDatabases()).containsExactly(firstModuleServiceDatabase)
    }

    @Test
    fun `collectModuleServiceDatabases with several modules`() {
        modulesController.registerModule(firstModule)
        modulesController.registerModule(secondModule)
        assertThat(modulesController.collectModuleServiceDatabases())
            .containsExactly(firstModuleServiceDatabase, secondModuleServicesDatabase)
    }

    @Test
    fun `collectModuleServiceDatabases if module throw exception`() {
        modulesController.registerModule(firstModule)
        modulesController.registerModule(secondModule)

        val exception = RuntimeException()
        doThrow(exception).whenever(firstModule).moduleServicesDatabase

        assertThat(modulesController.collectModuleServiceDatabases()).containsExactly(secondModuleServicesDatabase)

        verify(selfReporter).reportEvent(
            "service_module_errors",
            mapOf(firstModuleIdentifier to mapOf("db" to exception.stackTraceToString()))
        )

        clearInvocations(firstModule, secondModule)

        modulesController.initServiceSide(serviceContext, startupState)
        verify(secondModule).initServiceSide(serviceContext, secondModuleRemoteConfig)
        verifyNoInteractions(firstModule)
    }

    @Test
    fun collectRemoteConfigControllersWithSeveralModules() {
        modulesController.registerModule(firstModule)
        modulesController.registerModule(secondModule)
        modulesController.registerModule(lightModule)
        assertThat(modulesController.collectRemoteConfigControllers())
            .usingRecursiveComparison()
            .isEqualTo(
                mapOf(
                    firstModuleIdentifier to
                        ModuleRemoteConfigController(firstRemoteConfigExtensionConfiguration),
                    secondModuleIdentifier to
                        ModuleRemoteConfigController(secondRemoteConfigExtensionConfiguration)
                )
            )
    }

    @Test
    fun onStartupChangedWithSeveralModules() {
        modulesController.registerModule(firstModule)
        modulesController.registerModule(secondModule)
        modulesController.registerModule(lightModule)

        modulesController.onStartupStateChanged(startupState)

        verify(firstModuleRemoteConfigListener).onRemoteConfigUpdated(firstModuleRemoteConfig)
        verify(secondModuleConfigUpdateListener).onRemoteConfigUpdated(secondModuleRemoteConfig)
        verify(configProviderMockedConstructionRule.constructionMock.constructed()[0], never())
            .getRemoteConfigForServiceModule(moduleWithoutRemoteConfigIdentifier)
    }

    @Test
    fun onStartupChangedWithoutModules() {
        modulesController.onStartupStateChanged(startupState)
        verifyNoMoreInteractions(firstModule, secondModule, lightModule)
    }

    @Test
    fun onStartupStateChangedIfModuleThrowException() {
        modulesController.registerModule(firstModule)
        modulesController.registerModule(secondModule)

        val exception = RuntimeException()
        doThrow(exception).whenever(firstModule).remoteConfigExtensionConfiguration

        modulesController.onStartupStateChanged(startupState)
        verify(secondModuleConfigUpdateListener).onRemoteConfigUpdated(secondModuleRemoteConfig)

        verify(selfReporter).reportEvent(
            "service_module_errors",
            mapOf(firstModuleIdentifier to mapOf("remote_config_updated" to exception.stackTraceToString()))
        )

        clearInvocations(firstModule, secondModule)
        modulesController.onStartupStateChanged(startupState)
        verifyNoInteractions(firstModule)
        verify(secondModuleConfigUpdateListener, times(2)).onRemoteConfigUpdated(secondModuleRemoteConfig)
    }

    @Test
    fun initServiceSide() {
        modulesController.registerModule(firstModule)
        modulesController.registerModule(secondModule)
        modulesController.registerModule(lightModule)

        modulesController.initServiceSide(serviceContext, startupState)

        inOrder(firstModule, secondModule, lightModule, moduleEventHandlersHolder) {
            verify(firstModule).initServiceSide(serviceContext, firstModuleRemoteConfig)
            verify(moduleEventHandlersHolder).register(firstModuleIdentifier, firstModuleEventHandlerFactory)
            verify(secondModule).initServiceSide(serviceContext, secondModuleRemoteConfig)
            verify(moduleEventHandlersHolder).register(secondModuleIdentifier, secondModuleEventHandlerFactory)
            verify(lightModule).initServiceSide(serviceContext, moduleWithoutRemoteConfigFullConfig)
        }
    }

    @Test
    fun `initServiceSide if module throw exception`() {
        modulesController.registerModule(firstModule)
        modulesController.registerModule(secondModule)

        val exception = RuntimeException()
        doThrow(exception).whenever(firstModule).initServiceSide(serviceContext, firstModuleRemoteConfig)

        modulesController.initServiceSide(serviceContext, startupState)

        verify(selfReporter).reportEvent(
            "service_module_errors",
            mapOf(firstModuleIdentifier to mapOf("init" to exception.stackTraceToString()))
        )

        clearInvocations(firstModule, secondModule)
        modulesController.onStartupStateChanged(startupState)

        verifyNoInteractions(firstModule)
        verify(secondModuleConfigUpdateListener).onRemoteConfigUpdated(secondModuleRemoteConfig)
    }

    @Test
    fun askForPermissionStrategy() {
        modulesController.registerModule(askForPermissionStrategyModule)
        modulesController.initServiceSide(serviceContext, startupState)
        assertThat(modulesController.askForPermissionStrategy).isEqualTo(askForPermissionStrategy)
    }

    @Test
    fun `askForPermissionStrategy without initServiceSide`() {
        modulesController.registerModule(askForPermissionStrategyModule)
        assertThat(modulesController.askForPermissionStrategy).isEqualTo(askForPermissionStrategy)
    }

    @Test
    fun `askForPermissionStrategy without modules`() {
        modulesController.initServiceSide(serviceContext, startupState)
        assertThat(modulesController.askForPermissionStrategy).isEqualTo(neverForbidPermissionStrategy)
    }

    @Test
    fun `askForPermissionStrategy if wrong id`() {
        whenever(askForPermissionStrategyModule.identifier).thenReturn("wrong id")
        modulesController.registerModule(askForPermissionStrategyModule)
        modulesController.initServiceSide(serviceContext, startupState)
        assertThat(modulesController.askForPermissionStrategy).isEqualTo(neverForbidPermissionStrategy)
    }

    @Test
    fun `askForPermissionStrategy if incompatible interface`() {
        whenever(firstModule.identifier).thenReturn(askForPermissionStrategyModuleId)
        modulesController.registerModule(firstModule)
        modulesController.initServiceSide(serviceContext, startupState)
        assertThat(modulesController.askForPermissionStrategy).isEqualTo(neverForbidPermissionStrategy)
    }

    @Test
    fun getModulesConfigsBundleForClient() {
        val firstBundle = MockProvider.mockBundle()
        whenever(firstClientConfigProvider.getConfigBundleForClient()).thenReturn(firstBundle)
        whenever(secondClientConfigProvider.getConfigBundleForClient()).thenReturn(null)

        modulesController.registerModule(firstModule)
        modulesController.registerModule(secondModule)
        modulesController.registerModule(lightModule)

        val modulesConfig = modulesController.getModulesConfigsBundleForClient()
        assertThat(modulesConfig.keySet()).containsExactlyInAnyOrder(
            firstModuleIdentifier
        )
        assertThat(modulesConfig.getBundle(firstModuleIdentifier)).isEqualTo(firstBundle)
    }

    @Test
    fun `getModulesConfigsBundleForClient if module throw exception`() {
        val firstBundle = MockProvider.mockBundle()
        val secondBundle = MockProvider.mockBundle()

        whenever(firstClientConfigProvider.getConfigBundleForClient()).thenReturn(firstBundle)
        whenever(secondClientConfigProvider.getConfigBundleForClient()).thenReturn(secondBundle)

        val exception = RuntimeException()
        doThrow(exception).whenever(firstModule).clientConfigProvider

        modulesController.registerModule(firstModule)
        modulesController.registerModule(secondModule)

        val modulesConfig = modulesController.getModulesConfigsBundleForClient()
        assertThat(modulesConfig.keySet()).containsExactlyInAnyOrder(secondModuleIdentifier)

        verify(selfReporter).reportEvent(
            "service_module_errors",
            mapOf(firstModuleIdentifier to mapOf("config_bundle" to exception.stackTraceToString()))
        )

        clearInvocations(firstModule, secondModule)
        modulesController.onStartupStateChanged(startupState)
        verifyNoInteractions(firstModule)
        verify(secondModuleConfigUpdateListener).onRemoteConfigUpdated(secondModuleRemoteConfig)
    }

    private fun alwaysAskForPermissionStrategy(): NeverForbidPermissionStrategy {
        assertThat(neverForbidPermissionStrategyMockedConstructionRule.constructionMock.constructed()).hasSize(1)
        assertThat(neverForbidPermissionStrategyMockedConstructionRule.argumentInterceptor.flatArguments())
            .isEmpty()
        return neverForbidPermissionStrategyMockedConstructionRule.constructionMock.constructed().first()
    }

    internal abstract class AskForPermissionStrategyModule :
        ModuleServiceEntryPoint<Any>(),
        AskForPermissionStrategyModuleProvider
}
