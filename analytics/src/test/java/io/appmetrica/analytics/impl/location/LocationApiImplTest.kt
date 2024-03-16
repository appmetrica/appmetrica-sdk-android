package io.appmetrica.analytics.impl.location

import android.content.Context
import android.location.Location
import io.appmetrica.analytics.assertions.ObjectPropertyAssertions
import io.appmetrica.analytics.coreapi.internal.backport.Consumer
import io.appmetrica.analytics.coreapi.internal.control.Toggle
import io.appmetrica.analytics.coreapi.internal.executors.IHandlerExecutor
import io.appmetrica.analytics.coreapi.internal.permission.PermissionStrategy
import io.appmetrica.analytics.impl.GlobalServiceLocator
import io.appmetrica.analytics.impl.StartupStateHolder
import io.appmetrica.analytics.impl.modules.ModulesController
import io.appmetrica.analytics.impl.permissions.CompositePermissionStrategy
import io.appmetrica.analytics.impl.permissions.LocationFlagStrategy
import io.appmetrica.analytics.impl.permissions.SimplePermissionExtractor
import io.appmetrica.analytics.impl.startup.CacheControl
import io.appmetrica.analytics.impl.startup.StartupState
import io.appmetrica.analytics.locationapi.internal.CacheArguments
import io.appmetrica.analytics.locationapi.internal.LastKnownLocationExtractorProvider
import io.appmetrica.analytics.locationapi.internal.LastKnownLocationExtractorProviderFactory
import io.appmetrica.analytics.locationapi.internal.LocationClient
import io.appmetrica.analytics.locationapi.internal.LocationControllerObserver
import io.appmetrica.analytics.locationapi.internal.LocationFilter
import io.appmetrica.analytics.locationapi.internal.LocationReceiverProvider
import io.appmetrica.analytics.locationapi.internal.LocationReceiverProviderFactory
import io.appmetrica.analytics.modulesapi.internal.service.ModuleLocationSourcesServiceController
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.GlobalServiceLocatorRule
import io.appmetrica.analytics.testutils.MockedConstructionRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.clearInvocations
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
internal class LocationApiImplTest : CommonTest() {

    private val context = mock<Context>()
    private val locationController = mock<LocationController>()
    private val location = mock<Location>()
    private val wakelock = mock<Any>()
    private val gplLastKnownLocationExtractorProvider = mock<LastKnownLocationExtractorProvider>()
    private val networkLastKnownLocationExtractorProvider = mock<LastKnownLocationExtractorProvider>()
    private val moduleLocationAppStateToggle = mock<Toggle>()
    private val locationControllerObserver = mock<LocationControllerObserver>()
    private val lastKnownLocationExtractorProvider = mock<LastKnownLocationExtractorProvider>()
    private val locationReceiverProvider = mock<LocationReceiverProvider>()
    private val startupState = mock<StartupState>()
    private val locationFilter = mock<LocationFilter>()
    private val askForPermissionStrategy = mock<PermissionStrategy>()

    private val lastKnownLocationExtractorProviderFactory = mock<LastKnownLocationExtractorProviderFactory> {
        on { gplLastKnownLocationExtractorProvider } doReturn gplLastKnownLocationExtractorProvider
        on { networkLastKnownLocationExtractorProvider } doReturn networkLastKnownLocationExtractorProvider
    }

    private val locationReceiverProviderFactory = mock<LocationReceiverProviderFactory>()

    private val locationClient = mock<LocationClient> {
        on { lastKnownExtractorProviderFactory } doReturn lastKnownLocationExtractorProviderFactory
        on { locationReceiverProviderFactory } doReturn locationReceiverProviderFactory
    }

    private val locationSourcesController = mock<ModuleLocationSourcesServiceController>()

    private val firstLocationConsumer = mock<Consumer<Location?>>()
    private val secondLocationConsumer = mock<Consumer<Location?>>()
    private val locationConsumers = listOf(firstLocationConsumer, secondLocationConsumer)

    @get:Rule
    val locationFlagStrategyMockedConstructionRule = MockedConstructionRule(LocationFlagStrategy::class.java)

    @get:Rule
    val compositePermissionStrategyMockedConstructionRule =
        MockedConstructionRule(CompositePermissionStrategy::class.java)

    @get:Rule
    val simplePermissionExtractorMockedConstructionRule = MockedConstructionRule(SimplePermissionExtractor::class.java)

    @get:Rule
    val globalServiceLocatorRule = GlobalServiceLocatorRule()

    private lateinit var modulesController: ModulesController
    private lateinit var modulesExecutor: IHandlerExecutor
    private lateinit var startupStateHolder: StartupStateHolder
    private lateinit var locationFlagStrategy: LocationFlagStrategy
    private lateinit var compositePermissionStrategy: CompositePermissionStrategy
    private lateinit var permissionExtractor: SimplePermissionExtractor

    private lateinit var locationApiImpl: LocationApiImpl

    @Before
    fun setUp() {
        modulesController = GlobalServiceLocator.getInstance().modulesController
        modulesExecutor = GlobalServiceLocator.getInstance().serviceExecutorProvider.moduleExecutor
        startupStateHolder = GlobalServiceLocator.getInstance().startupStateHolder

        whenever(modulesController.collectLocationConsumers()).thenReturn(locationConsumers)
        whenever(modulesController.chooseLocationSourceController()).thenReturn(locationSourcesController)
        whenever(modulesController.chooseLocationAppStateControlToggle()).thenReturn(moduleLocationAppStateToggle)
        whenever(modulesController.askForPermissionStrategy).thenReturn(askForPermissionStrategy)

        locationApiImpl = LocationApiImpl(context, locationController, locationClient)

        locationFlagStrategy = locationFlagStrategy()
        compositePermissionStrategy = compositePermissionStrategy()
        permissionExtractor = simplePermissionExtractor()
    }

    @Test
    fun `constructor subscribes locationFlagStrategy on location controller`() {
        verify(locationController).registerObserver(locationFlagStrategy)
    }

    @Test
    fun `constructor subscribes location client on location controller`() {
        verify(locationController).registerObserver(locationClient)
    }

    @Test
    fun getLocation() {
        whenever(locationClient.location).thenReturn(location)
        assertThat(locationApiImpl.getLocation()).isEqualTo(location)
    }

    @Test
    fun `getLocation for null`() {
        whenever(locationClient.location).thenReturn(null)
        assertThat(locationApiImpl.getLocation()).isNull()
    }

    @Test
    fun registerWakelock() {
        locationApiImpl.registerWakelock(wakelock)
        verify(locationController).registerWakelock(wakelock)
    }

    @Test
    fun removeWakelock() {
        locationApiImpl.removeWakelock(wakelock)
        verify(locationController).removeWakelock(wakelock)
    }

    @Test
    fun `updateTrackingStatusFromClient for true`() {
        updateTrackingStatusFromClient(true)
    }

    @Test
    fun `updateTrackingStatusFromClient for false`() {
        updateTrackingStatusFromClient(false)
    }

    private fun updateTrackingStatusFromClient(status: Boolean) {
        locationApiImpl.updateTrackingStatusFromClient(status)
        verify(locationController).updateTrackingStatusFromClient(status)
    }

    @Test
    fun `init warm up locationClient`() {
        locationApiImpl.init()
        verify(locationClient).init(context, permissionExtractor, modulesExecutor, locationConsumers)
    }

    @Test
    fun `init warm up moduleLocationSourceController if not null`() {
        clearInvocations(locationSourcesController)
        locationApiImpl.init()
        verify(locationSourcesController).init()
        verifyNoMoreInteractions(locationSourcesController)
    }

    @Test
    fun `init warm up moduleLocationSourceController if null`() {
        whenever(modulesController.chooseLocationSourceController()).thenReturn(null)
        locationApiImpl.init()

        verify(locationClient).registerLocationSource(gplLastKnownLocationExtractorProvider)
        verify(locationClient).registerLocationSource(networkLastKnownLocationExtractorProvider)
    }

    @Test
    fun `init warm up location controller`() {
        locationApiImpl.init()
        verify(locationController).init(moduleLocationAppStateToggle)
    }

    @Test
    fun `init subscribes self as startup state observer`() {
        locationApiImpl.init()
        verify(GlobalServiceLocator.getInstance().startupStateHolder).registerObserver(locationApiImpl)
    }

    @Test
    fun registerControllerObserver() {
        locationApiImpl.registerControllerObserver(locationControllerObserver)
        verify(locationController).registerObserver(locationControllerObserver)
    }

    @Test
    fun `registerSource for lastKnownLocationExtractor`() {
        locationApiImpl.registerSource(lastKnownLocationExtractorProvider)
        verify(locationClient).registerLocationSource(lastKnownLocationExtractorProvider)
    }

    @Test
    fun `registerSource for locationReceiver`() {
        locationApiImpl.registerSource(locationReceiverProvider)
        verify(locationClient).registerLocationSource(locationReceiverProvider)
    }

    @Test
    fun `unregisterSource for lastKnownLocationExtractor`() {
        locationApiImpl.unregisterSource(lastKnownLocationExtractorProvider)
        verify(locationClient).unregisterLocationSource(lastKnownLocationExtractorProvider)
    }

    @Test
    fun `unregisterSource for locationReceiver`() {
        locationApiImpl.unregisterSource(locationReceiverProvider)
        verify(locationClient).unregisterLocationSource(locationReceiverProvider)
    }

    @Test
    fun lastKnownExtractorProviderFactory() {
        assertThat(locationApiImpl.lastKnownExtractorProviderFactory)
            .isEqualTo(lastKnownLocationExtractorProviderFactory)
    }

    @Test
    fun locationReceiverProviderFactory() {
        assertThat(locationApiImpl.locationReceiverProviderFactory)
            .isEqualTo(locationReceiverProviderFactory)
    }

    @Test
    fun `onStartupStateChanged if cacheControl is null`() {
        clearInvocations(locationClient)
        whenever(startupState.cacheControl).thenReturn(null)
        verifyNoMoreInteractions(locationClient)
    }

    @Test
    fun `onStartupStateChanged if cacheControl is not null`() {
        val ttl = 100400L
        val cacheControl = CacheControl(ttl)
        whenever(startupState.cacheControl).thenReturn(cacheControl)
        val cacheArgumentsCaptor = argumentCaptor<CacheArguments>()

        locationApiImpl.onStartupStateChanged(startupState)
        verify(locationClient).updateCacheArguments(cacheArgumentsCaptor.capture())

        ObjectPropertyAssertions(cacheArgumentsCaptor.firstValue)
            .checkField("refreshPeriod", ttl)
            .checkField("outdatedTimeInterval", ttl * 2)
            .checkAll()
    }

    @Test
    fun updateLocationFilter() {
        locationApiImpl.updateLocationFilter(locationFilter)
        verify(locationClient).updateLocationFilter(locationFilter)
    }

    private fun locationFlagStrategy(): LocationFlagStrategy {
        assertThat(locationFlagStrategyMockedConstructionRule.constructionMock.constructed()).hasSize(1)
        assertThat(locationFlagStrategyMockedConstructionRule.argumentInterceptor.flatArguments()).isEmpty()
        return locationFlagStrategyMockedConstructionRule.constructionMock.constructed().first()
    }

    private fun compositePermissionStrategy(): CompositePermissionStrategy {
        assertThat(compositePermissionStrategyMockedConstructionRule.constructionMock.constructed()).hasSize(1)
        assertThat(compositePermissionStrategyMockedConstructionRule.argumentInterceptor.flatArguments())
            .containsExactly(arrayOf(locationFlagStrategy, askForPermissionStrategy))
        return compositePermissionStrategyMockedConstructionRule.constructionMock.constructed().first()
    }

    private fun simplePermissionExtractor(): SimplePermissionExtractor {
        assertThat(simplePermissionExtractorMockedConstructionRule.constructionMock.constructed()).hasSize(1)
        assertThat(simplePermissionExtractorMockedConstructionRule.argumentInterceptor.flatArguments())
            .containsExactly(compositePermissionStrategy)
        return simplePermissionExtractorMockedConstructionRule.constructionMock.constructed().first()
    }
}
