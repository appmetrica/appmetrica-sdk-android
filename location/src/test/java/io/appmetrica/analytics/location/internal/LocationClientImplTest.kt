package io.appmetrica.analytics.location.internal

import android.content.Context
import android.location.Location
import io.appmetrica.analytics.coreapi.internal.backport.Consumer
import io.appmetrica.analytics.coreapi.internal.executors.IHandlerExecutor
import io.appmetrica.analytics.coreapi.internal.system.PermissionExtractor
import io.appmetrica.analytics.location.impl.LastKnownLocationExtractorProviderFactoryImpl
import io.appmetrica.analytics.location.impl.LocationConfig
import io.appmetrica.analytics.location.impl.LocationCore
import io.appmetrica.analytics.location.impl.LocationReceiverProviderFactoryImpl
import io.appmetrica.analytics.location.impl.LocationStreamDispatcher
import io.appmetrica.analytics.location.impl.system.PassiveLocationReceiverProvider
import io.appmetrica.analytics.locationapi.internal.CacheArguments
import io.appmetrica.analytics.locationapi.internal.LastKnownLocationExtractorProvider
import io.appmetrica.analytics.locationapi.internal.LocationFilter
import io.appmetrica.analytics.locationapi.internal.LocationReceiverProvider
import io.appmetrica.analytics.testutils.MockedConstructionRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyZeroInteractions
import org.mockito.kotlin.whenever

internal class LocationClientImplTest {

    private val context = mock<Context>()
    private val permissionExtractor = mock<PermissionExtractor>()
    private val executor = mock<IHandlerExecutor>()
    private val firstConsumer = mock<Consumer<Location?>>()
    private val secondConsumer = mock<Consumer<Location?>>()
    private val filledConsumers = listOf(firstConsumer, secondConsumer)
    private val incomingCacheArguments = mock<CacheArguments>()
    private val incomingLocationFilter = mock<LocationFilter>()
    private val locationConfigWithUpdatedCacheArguments = mock<LocationConfig>()
    private val locationWithUpdatedLocationFilter = mock<LocationConfig>()
    private val lastKnownLocationExtractorProvider = mock<LastKnownLocationExtractorProvider>()
    private val locationReceiverProvider = mock<LocationReceiverProvider>()
    private val location = mock<Location>()

    @get:Rule
    val passiveLocationReceiverProviderMockedConstructionRule =
        MockedConstructionRule(PassiveLocationReceiverProvider::class.java)

    @get:Rule
    val lastKnownExtractorProviderFactoryMockedConstructionRule =
        MockedConstructionRule(LastKnownLocationExtractorProviderFactoryImpl::class.java)

    @get:Rule
    val locationReceiverProviderFactoryImplMockedConstructionRule =
        MockedConstructionRule(LocationReceiverProviderFactoryImpl::class.java)

    @get:Rule
    val locationConfigMockedConstructionRule = MockedConstructionRule(LocationConfig::class.java)

    @get:Rule
    val locationStreamDispatcherMockedConstructionRule =
        MockedConstructionRule(LocationStreamDispatcher::class.java)

    @get:Rule
    val locationCoreMockedConstructionRule = MockedConstructionRule(LocationCore::class.java)

    private lateinit var defaultConfig: LocationConfig

    private lateinit var locationClientImpl: LocationClientImpl

    @Before
    fun setUp() {
        locationClientImpl = LocationClientImpl()

        defaultConfig = defaultLocationConfig()
        whenever(defaultConfig.buildUpon(incomingCacheArguments)).thenReturn(locationConfigWithUpdatedCacheArguments)
        whenever(defaultConfig.buildUpon(incomingLocationFilter)).thenReturn(locationWithUpdatedLocationFilter)
    }

    @Test
    fun lastKnownExtractorProviderFactory() {
        assertThat(locationClientImpl.lastKnownExtractorProviderFactory)
            .isEqualTo(lastKnownExtractorProviderFactoryImpl())
    }

    @Test
    fun locationReceiverProviderFactory() {
        assertThat(locationClientImpl.locationReceiverProviderFactory)
            .isEqualTo(locationReceiverProviderFactoryImpl())
    }

    @Test
    fun `init with default config`() {
        repeat(5) {
            locationClientImpl.init(context, permissionExtractor, executor, filledConsumers)
        }
        repeat(5) {
            locationClientImpl.init(mock(), mock(), mock(), emptyList())
        }
        locationCore(context, permissionExtractor, executor, locationStreamDispatcher(filledConsumers, defaultConfig))
    }

    @Test
    fun `init after updateCacheArguments`() {
        locationClientImpl.updateCacheArguments(incomingCacheArguments)
        repeat(5) {
            locationClientImpl.init(context, permissionExtractor, executor, filledConsumers)
        }
        repeat(5) {
            locationClientImpl.init(mock(), mock(), mock(), emptyList())
        }
        locationCore(context, permissionExtractor, executor,
            locationStreamDispatcher(filledConsumers, locationConfigWithUpdatedCacheArguments))
    }

    @Test
    fun `init after updateLocationFilter`() {
        locationClientImpl.updateLocationFilter(incomingLocationFilter)
        repeat(5) {
            locationClientImpl.init(context, permissionExtractor, executor, filledConsumers)
        }
        repeat(5) {
            locationClientImpl.init(mock(), mock(), mock(), emptyList())
        }
        locationCore(context, permissionExtractor, executor,
            locationStreamDispatcher(filledConsumers, locationWithUpdatedLocationFilter))
    }

    @Test
    fun `updateCacheArguments after init`() {
        locationClientImpl.init(context, permissionExtractor, executor, filledConsumers)
        locationClientImpl.updateCacheArguments(incomingCacheArguments)
        verify(defaultLocationCore()).updateConfig(locationConfigWithUpdatedCacheArguments)
    }

    @Test
    fun `updateLocationFilter after init`() {
        locationClientImpl.init(context, permissionExtractor, executor, filledConsumers)
        locationClientImpl.updateLocationFilter(incomingLocationFilter)
        verify(defaultLocationCore()).updateConfig(locationWithUpdatedLocationFilter)
    }

    @Test
    fun `registerLocationSource with last known extractor provider without init`() {
        locationClientImpl.registerLocationSource(lastKnownLocationExtractorProvider)
        verifyZeroInteractions(lastKnownLocationExtractorProvider)
    }

    @Test
    fun `registerLocationSource with last known extractor provider after init`() {
        locationClientImpl.init(context, permissionExtractor, executor, filledConsumers)
        locationClientImpl.registerLocationSource(lastKnownLocationExtractorProvider)
        verify(defaultLocationCore()).registerLastKnownSource(lastKnownLocationExtractorProvider)
    }

    @Test
    fun `unregisterLocationSource with last known extractor provider without init`() {
        locationClientImpl.unregisterLocationSource(lastKnownLocationExtractorProvider)
        verifyZeroInteractions(lastKnownLocationExtractorProvider)
    }

    @Test
    fun `unregisterLocationSource with last known extractor provider after init`() {
        locationClientImpl.init(context, permissionExtractor, executor, filledConsumers)
        locationClientImpl.unregisterLocationSource(lastKnownLocationExtractorProvider)
        verify(defaultLocationCore()).unregisterLastKnownSource(lastKnownLocationExtractorProvider)
    }

    @Test
    fun `registerLocationSource with location receiver before init`() {
        locationClientImpl.registerLocationSource(locationReceiverProvider)
        verifyZeroInteractions(locationReceiverProvider)
    }

    @Test
    fun `registerLocationSource with location receiver after init`() {
        locationClientImpl.init(context, permissionExtractor, executor, filledConsumers)
        locationClientImpl.registerLocationSource(locationReceiverProvider)
        verify(defaultLocationCore()).registerLocationReceiver(locationReceiverProvider)
    }

    @Test
    fun `unregisterLocationSource with location receiver before init`() {
        locationClientImpl.unregisterLocationSource(locationReceiverProvider)
        verifyZeroInteractions(locationReceiverProvider)
    }

    @Test
    fun `unregisterLocationSource with location receiver after init`() {
        locationClientImpl.init(context, permissionExtractor, executor, filledConsumers)
        locationClientImpl.unregisterLocationSource(locationReceiverProvider)
        verify(defaultLocationCore()).unregisterLocationReceiver(locationReceiverProvider)
    }

    @Test
    fun `startLocationTracking before init`() {
        locationClientImpl.startLocationTracking()
    }

    @Test
    fun `startLocationTracking after init`() {
        locationClientImpl.init(context, permissionExtractor, executor, filledConsumers)
        locationClientImpl.startLocationTracking()
        verify(defaultLocationCore()).startLocationTracking()
    }

    @Test
    fun `stopLocationTracking before init`() {
        locationClientImpl.stopLocationTracking()
    }

    @Test
    fun `stopLocationTracking after init`() {
        locationClientImpl.init(context, permissionExtractor, executor, filledConsumers)
        locationClientImpl.stopLocationTracking()
        verify(defaultLocationCore()).stopLocationTracking()
    }

    @Test
    fun `location before init`() {
        assertThat(locationClientImpl.location).isNull()
    }

    @Test
    fun `location after init if return null`() {
        locationClientImpl.init(context, permissionExtractor, executor, filledConsumers)
        whenever(defaultLocationCore().cachedLocation).thenReturn(null)
        assertThat(locationClientImpl.location).isNull()
    }

    @Test
    fun `location after init`() {
        locationClientImpl.init(context, permissionExtractor, executor, filledConsumers)
        whenever(defaultLocationCore().cachedLocation).thenReturn(location)
        assertThat(locationClientImpl.location).isEqualTo(location)
    }

    private fun defaultLocationCore(): LocationCore =
        locationCore(context, permissionExtractor, executor, locationStreamDispatcher(filledConsumers, defaultConfig))


    private fun locationCore(
        context: Context,
        permissionExtractor: PermissionExtractor,
        executor: IHandlerExecutor,
        locationStreamDispatcher: LocationStreamDispatcher
    ): LocationCore {
        assertThat(locationCoreMockedConstructionRule.constructionMock.constructed()).hasSize(1)
        assertThat(locationCoreMockedConstructionRule.argumentInterceptor.flatArguments())
            .containsExactly(context, permissionExtractor, executor, locationStreamDispatcher)
        return locationCoreMockedConstructionRule.constructionMock.constructed().first()
    }

    private fun locationStreamDispatcher(
        consumers: List<Consumer<Location?>>,
        locationConfig: LocationConfig
    ): LocationStreamDispatcher {
        assertThat(locationStreamDispatcherMockedConstructionRule.constructionMock.constructed())
            .hasSize(1)
        assertThat(locationStreamDispatcherMockedConstructionRule.argumentInterceptor.flatArguments())
            .containsExactly(consumers, locationConfig)
        return locationStreamDispatcherMockedConstructionRule.constructionMock.constructed().first()
    }

    private fun defaultLocationConfig(): LocationConfig {
        assertThat(locationConfigMockedConstructionRule.constructionMock.constructed()).hasSize(1)
        return locationConfigMockedConstructionRule.constructionMock.constructed().first()
    }

    private fun locationReceiverProviderFactoryImpl(): LocationReceiverProviderFactoryImpl {
        assertThat(locationReceiverProviderFactoryImplMockedConstructionRule.constructionMock.constructed())
            .hasSize(1)
        assertThat(locationReceiverProviderFactoryImplMockedConstructionRule.argumentInterceptor.flatArguments())
            .containsExactly(passiveLocationReceiverProvider())

        return locationReceiverProviderFactoryImplMockedConstructionRule.constructionMock.constructed().first()
    }

    private fun lastKnownExtractorProviderFactoryImpl(): LastKnownLocationExtractorProviderFactoryImpl {
        assertThat(lastKnownExtractorProviderFactoryMockedConstructionRule.constructionMock.constructed())
            .hasSize(1)
        assertThat(lastKnownExtractorProviderFactoryMockedConstructionRule.argumentInterceptor.flatArguments())
            .containsExactly(passiveLocationReceiverProvider())

        return lastKnownExtractorProviderFactoryMockedConstructionRule.constructionMock.constructed().first()
    }

    private fun passiveLocationReceiverProvider(): PassiveLocationReceiverProvider {
        assertThat(passiveLocationReceiverProviderMockedConstructionRule.constructionMock.constructed())
            .hasSize(1)
        assertThat(passiveLocationReceiverProviderMockedConstructionRule.argumentInterceptor.flatArguments())
            .isEmpty()

        return passiveLocationReceiverProviderMockedConstructionRule.constructionMock.constructed().first()
    }
}
