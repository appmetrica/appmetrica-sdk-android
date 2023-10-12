package io.appmetrica.analytics.impl.modules

import android.location.Location
import io.appmetrica.analytics.coreapi.internal.backport.Consumer
import io.appmetrica.analytics.coreapi.internal.control.Toggle
import io.appmetrica.analytics.coreapi.internal.data.Converter
import io.appmetrica.analytics.coreapi.internal.data.JsonParser
import io.appmetrica.analytics.coreapi.internal.permission.PermissionStrategy
import io.appmetrica.analytics.impl.GlobalServiceLocator
import io.appmetrica.analytics.impl.permissions.NeverForbidPermissionStrategy
import io.appmetrica.analytics.impl.startup.StartupState
import io.appmetrica.analytics.modulesapi.internal.AskForPermissionStrategyModuleProvider
import io.appmetrica.analytics.modulesapi.internal.LocationExtension
import io.appmetrica.analytics.modulesapi.internal.ModuleEntryPoint
import io.appmetrica.analytics.modulesapi.internal.ModuleLocationSourcesController
import io.appmetrica.analytics.modulesapi.internal.ModuleRemoteConfig
import io.appmetrica.analytics.modulesapi.internal.ModuleServicesDatabase
import io.appmetrica.analytics.modulesapi.internal.RemoteConfigExtensionConfiguration
import io.appmetrica.analytics.modulesapi.internal.RemoteConfigUpdateListener
import io.appmetrica.analytics.modulesapi.internal.ServiceContext
import io.appmetrica.analytics.modulesapi.internal.event.ModuleEventHandlerFactory
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.GlobalServiceLocatorRule
import io.appmetrica.analytics.testutils.MockedConstructionRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
internal class ModulesControllerTest : CommonTest() {

    private val firstModuleFeatures = listOf("First module feature")
    private val firstModuleBlocks = mapOf("First module block" to 1)
    private val firstModuleJsonParser = mock<JsonParser<Any>>()
    private val firstModuleProtobufConverter = mock<Converter<Any, ByteArray>>()
    private val firstModuleRemoteConfigListener = mock<RemoteConfigUpdateListener<Any>>()
    private val firstModuleEventHandlerFactory = mock<ModuleEventHandlerFactory>()

    private val firstRemoteConfigExtensionConfiguration = mock<RemoteConfigExtensionConfiguration<Any>> {
        on { getFeatures() } doReturn firstModuleFeatures
        on { getBlocks() } doReturn firstModuleBlocks
        on { getJsonParser() } doReturn firstModuleJsonParser
        on { getProtobufConverter() } doReturn firstModuleProtobufConverter
        on { getRemoteConfigUpdateListener() } doReturn firstModuleRemoteConfigListener
    }
    private val firstModuleIdentifier = "First module with remote config identifier"

    private val firstLocationConsumer = mock<Consumer<Location?>>()
    private val firstLocationSourcesController = mock<ModuleLocationSourcesController>()
    private val firstLocationControllerAppStateToggle = mock<Toggle>()

    private val firstLocationExtension = mock<LocationExtension> {
        on { locationConsumer } doReturn firstLocationConsumer
        on { locationSourcesController } doReturn firstLocationSourcesController
        on { locationControllerAppStateToggle } doReturn firstLocationControllerAppStateToggle
    }

    private val firstModuleServiceDatabase = mock<ModuleServicesDatabase>()

    private val firstModule = mock<ModuleEntryPoint<Any>> {
        on { identifier } doReturn firstModuleIdentifier
        on { remoteConfigExtensionConfiguration } doReturn firstRemoteConfigExtensionConfiguration
        on { moduleEventHandlerFactory } doReturn firstModuleEventHandlerFactory
        on { locationExtension } doReturn firstLocationExtension
        on { moduleServicesDatabase } doReturn firstModuleServiceDatabase
    }

    private val secondModuleFeatures = listOf("Second module feature #1", "Second module feature #2")
    private val secondModuleBlocks = mapOf("Second module block #1" to 1, "Second module block #2" to 2)
    private val secondModuleJsonParser = mock<JsonParser<Any>>()
    private val secondModuleProtobufConverter = mock<Converter<Any, ByteArray>>()
    private val secondModuleConfigUpdateListener = mock<RemoteConfigUpdateListener<Any>>()
    private val secondModuleEventHandlerFactory = mock<ModuleEventHandlerFactory>()

    private val secondRemoteConfigExtensionConfiguration = mock<RemoteConfigExtensionConfiguration<Any>> {
        on { getFeatures() } doReturn secondModuleFeatures
        on { getBlocks() } doReturn secondModuleBlocks
        on { getJsonParser() } doReturn secondModuleJsonParser
        on { getProtobufConverter() } doReturn secondModuleProtobufConverter
        on { getRemoteConfigUpdateListener() } doReturn secondModuleConfigUpdateListener
    }
    private val secondModuleIdentifier = "Second module with remote config identifier"

    private val secondLocationConsumer = mock<Consumer<Location?>>()
    private val secondLocationSourcesController = mock<ModuleLocationSourcesController>()
    private val secondLocationControllerAppStateToggle = mock<Toggle>()

    private val secondLocationExtension = mock<LocationExtension> {
        on { locationConsumer } doReturn secondLocationConsumer
        on { locationSourcesController } doReturn secondLocationSourcesController
        on { locationControllerAppStateToggle } doReturn secondLocationControllerAppStateToggle
    }

    private val secondModuleServicesDatabase = mock<ModuleServicesDatabase>()

    private val secondModule = mock<ModuleEntryPoint<Any>> {
        on { identifier } doReturn secondModuleIdentifier
        on { remoteConfigExtensionConfiguration } doReturn secondRemoteConfigExtensionConfiguration
        on { moduleEventHandlerFactory } doReturn secondModuleEventHandlerFactory
        on { locationExtension } doReturn secondLocationExtension
        on { moduleServicesDatabase } doReturn secondModuleServicesDatabase
    }

    private val moduleWithoutRemoteConfigIdentifier = "Module without remote config identifier"

    private val lightModule = mock<ModuleEntryPoint<Any>> {
        on { identifier } doReturn moduleWithoutRemoteConfigIdentifier
    }

    private val askForPermissionStrategyModuleId = "rp"
    private val askForPermissionStrategy = mock<PermissionStrategy>()
    private val askForPermissionStrategyModule = mock<AskForPermissionStrategyModule> {
        on { identifier } doReturn askForPermissionStrategyModuleId
        on { askForPermissionStrategy } doReturn askForPermissionStrategy
    }

    private val serviceContext = mock<ServiceContext>()

    private val firstModuleRemoteConfig = mock<ModuleRemoteConfig<Any?>>()
    private val secondModuleRemoteConfig = mock<ModuleRemoteConfig<Any?>>()
    private val moduleWithoutRemoteConfigFullConfig = mock<ModuleRemoteConfig<Any?>>()

    private val startupState = mock<StartupState>()

    @get:Rule
    val configProviderMockedConstructionRule = MockedConstructionRule(
        ModuleRemoteConfigProvider::class.java
    ) { mock, context ->
        whenever(mock.getRemoteConfigForModule(firstModuleIdentifier))
            .thenReturn(firstModuleRemoteConfig)
        whenever(mock.getRemoteConfigForModule(secondModuleIdentifier))
            .thenReturn(secondModuleRemoteConfig)
        whenever(mock.getRemoteConfigForModule(moduleWithoutRemoteConfigIdentifier))
            .thenReturn(moduleWithoutRemoteConfigFullConfig)
    }

    @get:Rule
    val neverForbidPermissionStrategyMockedConstructionRule =
        MockedConstructionRule(NeverForbidPermissionStrategy::class.java)

    @get:Rule
    val globalServiceLocatorRule = GlobalServiceLocatorRule()

    private lateinit var modulesController: ModulesController
    private lateinit var moduleEventHandlersHolder: ModuleEventHandlersHolder
    private lateinit var neverForbidPermissionStrategy: NeverForbidPermissionStrategy

    @Before
    fun setUp() {
        modulesController = ModulesController()
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
        val module = mock<ModuleEntryPoint<Any>> {
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
            .getRemoteConfigForModule(moduleWithoutRemoteConfigIdentifier)
    }

    @Test
    fun onStartupChangedWithoutModules() {
        modulesController.onStartupStateChanged(startupState)
        verifyNoMoreInteractions(firstModule, secondModule, lightModule)
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

    private fun alwaysAskForPermissionStrategy(): NeverForbidPermissionStrategy {
        assertThat(neverForbidPermissionStrategyMockedConstructionRule.constructionMock.constructed()).hasSize(1)
        assertThat(neverForbidPermissionStrategyMockedConstructionRule.argumentInterceptor.flatArguments())
            .isEmpty()
        return neverForbidPermissionStrategyMockedConstructionRule.constructionMock.constructed().first()
    }

    interface AskForPermissionStrategyModule : ModuleEntryPoint<Any>, AskForPermissionStrategyModuleProvider
}
