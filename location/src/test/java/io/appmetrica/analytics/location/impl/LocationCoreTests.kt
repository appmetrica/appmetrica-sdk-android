package io.appmetrica.analytics.location.impl

import android.content.Context
import android.location.Location
import io.appmetrica.analytics.coreapi.internal.executors.IHandlerExecutor
import io.appmetrica.analytics.coreapi.internal.system.PermissionExtractor
import io.appmetrica.analytics.coreutils.internal.cache.LocationDataCacheUpdateScheduler
import io.appmetrica.analytics.locationapi.internal.LastKnownLocationExtractor
import io.appmetrica.analytics.locationapi.internal.LastKnownLocationExtractorProvider
import io.appmetrica.analytics.locationapi.internal.LocationReceiver
import io.appmetrica.analytics.locationapi.internal.LocationReceiverProvider
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.MockedConstructionRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.clearInvocations
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
internal class LocationCoreTests : CommonTest() {

    private val context = mock<Context>()
    private val permissionExtractor = mock<PermissionExtractor>()
    private val executor = mock<IHandlerExecutor>()
    private val locationDataCache = mock<LocationDataCache>()

    private val locationStreamDispatcher = mock<LocationStreamDispatcher> {
        on { locationDataCache } doReturn locationDataCache
    }

    private val firstLastKnownExtractorIdentifier = "First last known extractor identifier"
    private val secondLastKnownExtractorIdentifier = "Second last known extractor identifier"
    private val firstLastKnownExtractor = mock<LastKnownLocationExtractor>()
    private val secondLastKnownExtractor = mock<LastKnownLocationExtractor>()

    private val firstLastKnownExtractorProvider = mock<LastKnownLocationExtractorProvider> {
        on { identifier } doReturn firstLastKnownExtractorIdentifier
    }

    private val secondLastKnownExtractorProvider = mock<LastKnownLocationExtractorProvider> {
        on { identifier } doReturn secondLastKnownExtractorIdentifier
    }

    private val firstLocationReceiverIdentifier = "First location receiver identifier"
    private val secondLocationReceiverIdentifier = "Second location receiver identifier"
    private val firstLocationReceiver = mock<LocationReceiver>()
    private val firstLocationReceiverWithSameIdentifier = mock<LocationReceiver>()
    private val secondLocationReceiver = mock<LocationReceiver>()

    private val firstLocationReceiverProvider = mock<LocationReceiverProvider> {
        on { identifier } doReturn firstLocationReceiverIdentifier
    }

    private val firstLocationReceiverWithSameIdentifierProvider = mock<LocationReceiverProvider> {
        on { identifier } doReturn firstLocationReceiverIdentifier
    }
    private val secondLocationReceiverProvider = mock<LocationReceiverProvider> {
        on { identifier } doReturn secondLocationReceiverIdentifier
    }

    private val location = mock<Location>()

    private val locationConfig = mock<LocationConfig>()

    private val runnableCaptor = argumentCaptor<Runnable>()

    @get:Rule
    val locationListenerWrapperMockedConstructionRule = MockedConstructionRule(LocationListenerWrapper::class.java)

    @get:Rule
    val locationDataCacheUpdateSchedulerMockedConstructionRule =
        MockedConstructionRule(LocationDataCacheUpdateScheduler::class.java)

    private lateinit var locationListenerWrapper: LocationListenerWrapper
    private lateinit var locationDataCacheUpdateScheduler: LocationDataCacheUpdateScheduler

    private lateinit var locationCore: LocationCore

    @Before
    fun setUp() {
        locationCore = LocationCore(context, permissionExtractor, executor, locationStreamDispatcher)

        locationListenerWrapper = locationListenerWrapper()
        locationDataCacheUpdateScheduler = locationDataCacheUpdateScheduler()

        whenever(firstLastKnownExtractorProvider.getExtractor(
            context,
            permissionExtractor,
            executor,
            locationListenerWrapper
        )).thenReturn(firstLastKnownExtractor)

        whenever(secondLastKnownExtractorProvider.getExtractor(
            context,
            permissionExtractor,
            executor,
            locationListenerWrapper
        )).thenReturn(secondLastKnownExtractor)

        whenever(firstLocationReceiverProvider.getLocationReceiver(
            context,
            permissionExtractor,
            executor,
            locationListenerWrapper
        )).thenReturn(firstLocationReceiver)

        whenever(firstLocationReceiverWithSameIdentifierProvider.getLocationReceiver(
            context,
            permissionExtractor,
            executor,
            locationListenerWrapper
        )).thenReturn(firstLocationReceiverWithSameIdentifier)

        whenever(secondLocationReceiverProvider.getLocationReceiver(
            context,
            permissionExtractor,
            executor,
            locationListenerWrapper
        )).thenReturn(secondLocationReceiver)
    }

    @Test
    fun `constructor register update scheduler`() {
        verify(locationDataCache).setUpdateScheduler(locationDataCacheUpdateScheduler)
    }

    @Test
    fun `registerLastKnownSource for single and updateLastKnown`() {
        locationCore.registerLastKnownSource(firstLastKnownExtractorProvider)
        locationCore.updateLastKnown()
        verify(firstLastKnownExtractor).updateLastKnownLocation()
    }

    @Test
    fun `registerLastKnownSource for both and updateLastKnown`() {
        locationCore.registerLastKnownSource(firstLastKnownExtractorProvider)
        locationCore.registerLastKnownSource(secondLastKnownExtractorProvider)
        locationCore.updateLastKnown()
        verify(firstLastKnownExtractor).updateLastKnownLocation()
        verify(secondLastKnownExtractor).updateLastKnownLocation()
    }

    @Test
    fun `registerLastKnownSource after start`() {
        locationCore.startLocationTracking()
        locationCore.registerLastKnownSource(firstLastKnownExtractorProvider)
        locationCore.registerLastKnownSource(secondLastKnownExtractorProvider)

        verify(firstLastKnownExtractor).updateLastKnownLocation()
        verify(secondLastKnownExtractor).updateLastKnownLocation()
    }

    @Test
    fun `start after registerLastKnownSource`() {
        locationCore.registerLastKnownSource(firstLastKnownExtractorProvider)
        locationCore.registerLastKnownSource(secondLastKnownExtractorProvider)

        verifyNoMoreInteractions(firstLastKnownExtractor, secondLastKnownExtractor)

        locationCore.startLocationTracking()

        verify(firstLastKnownExtractor).updateLastKnownLocation()
        verify(secondLastKnownExtractor).updateLastKnownLocation()

    }

    @Test
    fun `start multiple times`() {
        locationCore.registerLastKnownSource(firstLastKnownExtractorProvider)
        locationCore.registerLocationReceiver(firstLocationReceiverProvider)
        locationCore.startLocationTracking()

        clearInvocations(firstLocationReceiver, firstLastKnownExtractor)

        locationCore.startLocationTracking()
        verifyNoMoreInteractions(firstLocationReceiver, firstLastKnownExtractor)
    }

    @Test
    fun `stop for lastKnownExtractors without start`() {
        locationCore.registerLastKnownSource(firstLastKnownExtractorProvider)
        locationCore.registerLastKnownSource(secondLastKnownExtractorProvider)
        clearInvocations(firstLastKnownExtractor, secondLastKnownExtractor)
        locationCore.stopLocationTracking()

        verifyNoMoreInteractions(firstLastKnownExtractor, secondLastKnownExtractor)
    }

    @Test
    fun `stop for lastKnownExtractors after start`() {
        locationCore.registerLastKnownSource(firstLastKnownExtractorProvider)
        locationCore.registerLastKnownSource(secondLastKnownExtractorProvider)
        locationCore.startLocationTracking()
        clearInvocations(firstLastKnownExtractor, secondLastKnownExtractor)
        locationCore.stopLocationTracking()

        verifyNoMoreInteractions(firstLastKnownExtractor, secondLastKnownExtractor)
    }

    @Test
    fun `start after stop for lastKnownExtractors`() {
        locationCore.registerLastKnownSource(firstLastKnownExtractorProvider)
        locationCore.registerLastKnownSource(secondLastKnownExtractorProvider)

        locationCore.startLocationTracking()
        locationCore.stopLocationTracking()

        clearInvocations(firstLastKnownExtractor, secondLastKnownExtractor)
        locationCore.startLocationTracking()

        verify(firstLastKnownExtractor).updateLastKnownLocation()
        verify(secondLastKnownExtractor).updateLastKnownLocation()
    }

    @Test
    fun `stop without start for location receivers`() {
        locationCore.registerLocationReceiver(firstLocationReceiverProvider)
        locationCore.registerLocationReceiver(secondLocationReceiverProvider)
        clearInvocations(firstLocationReceiver, secondLocationReceiver)
        locationCore.stopLocationTracking()

        verifyNoMoreInteractions(firstLocationReceiver, secondLocationReceiver)
    }

    @Test
    fun `stop after start for location receivers`() {
        locationCore.registerLocationReceiver(firstLocationReceiverProvider)
        locationCore.registerLocationReceiver(secondLocationReceiverProvider)
        locationCore.startLocationTracking()
        clearInvocations(firstLocationReceiver, secondLocationReceiver)
        locationCore.stopLocationTracking()

        verify(firstLocationReceiver).stopLocationUpdates()
        verify(secondLocationReceiver).stopLocationUpdates()
    }

    @Test
    fun `multiple stop after start for location receivers`() {
        locationCore.registerLocationReceiver(firstLocationReceiverProvider)
        locationCore.registerLocationReceiver(secondLocationReceiverProvider)
        locationCore.startLocationTracking()
        locationCore.stopLocationTracking()

        clearInvocations(firstLocationReceiver, secondLocationReceiver)

        locationCore.stopLocationTracking()

        verifyNoMoreInteractions(firstLocationReceiver, secondLocationReceiver)
    }

    @Test
    fun `start after stop for location receivers`() {
        locationCore.registerLocationReceiver(firstLocationReceiverProvider)
        locationCore.registerLocationReceiver(secondLocationReceiverProvider)
        locationCore.startLocationTracking()
        locationCore.stopLocationTracking()

        clearInvocations(firstLocationReceiver, secondLocationReceiver)

        locationCore.startLocationTracking()

        verify(firstLocationReceiver).startLocationUpdates()
        verify(secondLocationReceiver).startLocationUpdates()
    }

    @Test
    fun `unregisterLastKnownSource for absent`() {
        locationCore.unregisterLastKnownSource(firstLastKnownExtractorProvider)
        verifyNoMoreInteractions(firstLastKnownExtractor)
    }

    @Test
    fun `unregisterLastKnownSource for registered`() {
        locationCore.registerLastKnownSource(firstLastKnownExtractorProvider)
        locationCore.registerLastKnownSource(secondLastKnownExtractorProvider)
        locationCore.unregisterLastKnownSource(firstLastKnownExtractorProvider)
        locationCore.updateLastKnown()

        verify(secondLastKnownExtractor).updateLastKnownLocation()
        verifyNoMoreInteractions(firstLastKnownExtractor)
    }

    @Test
    fun `startLocationTracking update last known`() {
        locationCore.registerLastKnownSource(firstLastKnownExtractorProvider)
        clearInvocations(firstLastKnownExtractor)
        locationCore.startLocationTracking()

        verify(firstLastKnownExtractor).updateLastKnownLocation()
    }

    @Test
    fun `registerLocationReceiver before startTracking`() {
        locationCore.registerLocationReceiver(firstLocationReceiverProvider)
        locationCore.registerLocationReceiver(firstLocationReceiverWithSameIdentifierProvider)
        locationCore.registerLocationReceiver(secondLocationReceiverProvider)

        verifyNoMoreInteractions(firstLocationReceiver, secondLocationReceiver)
    }

    @Test
    fun `registerLocationReceiver after startTracking`() {
        locationCore.startLocationTracking()

        locationCore.registerLocationReceiver(firstLocationReceiverProvider)
        locationCore.registerLocationReceiver(firstLocationReceiverWithSameIdentifierProvider)
        locationCore.registerLocationReceiver(secondLocationReceiverProvider)

        inOrder(firstLocationReceiver, firstLocationReceiverWithSameIdentifier, secondLocationReceiver) {
            verify(firstLocationReceiver).startLocationUpdates()
            verify(firstLocationReceiver).stopLocationUpdates()
            verify(firstLocationReceiverWithSameIdentifier).startLocationUpdates()
            verify(secondLocationReceiver).startLocationUpdates()
        }
    }

    @Test
    fun `startTracking for registered receivers`() {
        locationCore.registerLocationReceiver(firstLocationReceiverProvider)
        locationCore.registerLocationReceiver(firstLocationReceiverWithSameIdentifierProvider)
        locationCore.registerLocationReceiver(secondLocationReceiverProvider)

        locationCore.startLocationTracking()

        verify(firstLocationReceiverWithSameIdentifier).startLocationUpdates()
        verify(secondLocationReceiver).startLocationUpdates()

        verifyNoMoreInteractions(firstLocationReceiver)
    }

    @Test
    fun `unregisterLocationReceiver before startTracking`() {
        locationCore.registerLocationReceiver(firstLocationReceiverProvider)
        locationCore.registerLocationReceiver(firstLocationReceiverWithSameIdentifierProvider)
        locationCore.registerLocationReceiver(secondLocationReceiverProvider)

        locationCore.unregisterLocationReceiver(firstLocationReceiverProvider)
        locationCore.unregisterLocationReceiver(firstLocationReceiverWithSameIdentifierProvider)
        locationCore.unregisterLocationReceiver(secondLocationReceiverProvider)

        verifyNoMoreInteractions(firstLocationReceiver, firstLocationReceiverWithSameIdentifier, secondLocationReceiver)
    }

    @Test
    fun `unregisterLocationReceiver after startTracking`() {
        locationCore.startLocationTracking()

        locationCore.registerLocationReceiver(firstLocationReceiverProvider)
        locationCore.registerLocationReceiver(firstLocationReceiverWithSameIdentifierProvider)
        locationCore.registerLocationReceiver(secondLocationReceiverProvider)

        clearInvocations(firstLocationReceiver, firstLocationReceiverWithSameIdentifier, secondLocationReceiver)

        locationCore.unregisterLocationReceiver(firstLocationReceiverProvider)
        locationCore.unregisterLocationReceiver(firstLocationReceiverWithSameIdentifierProvider)
        locationCore.unregisterLocationReceiver(secondLocationReceiverProvider)

        verify(firstLocationReceiverWithSameIdentifier).stopLocationUpdates()
        verify(secondLocationReceiver).stopLocationUpdates()

        verifyNoMoreInteractions(firstLocationReceiver)
    }

    @Test
    fun `getCachedLocation for null`() {
        whenever(locationStreamDispatcher.cachedLocation).thenReturn(null)
        assertThat(locationCore.cachedSystemLocation).isNull()
    }

    @Test
    fun `getCachedLocation for non-null`() {
        whenever(locationStreamDispatcher.cachedLocation).thenReturn(location)
        assertThat(locationCore.cachedSystemLocation).isEqualTo(location)
    }

    @Test
    fun `getUserLocation before set`() {
        assertThat(locationCore.userLocation).isNull()
    }

    @Test
    fun `getUserLocation after set`() {
        locationCore.userLocation = location
        assertThat(locationCore.userLocation).isEqualTo(location)
    }

    @Test
    fun `getUserOrSystemLocation without any`() {
        assertThat(locationCore.userOrCachedSystemLocation).isNull()
    }

    @Test
    fun `getUserOrSystemLocation with cached only`() {
        whenever(locationStreamDispatcher.cachedLocation).thenReturn(location)
        assertThat(locationCore.userOrCachedSystemLocation).isEqualTo(location)
    }

    @Test
    fun `getUserOrSystemLocation with user only`() {
        locationCore.userLocation = location
        assertThat(locationCore.userOrCachedSystemLocation).isEqualTo(location)
    }

    @Test
    fun `getUserOrSystemLocation with cached and user locations`() {
        whenever(locationStreamDispatcher.cachedLocation).thenReturn(location)
        val userLocation: Location = mock()
        locationCore.userLocation = userLocation
        assertThat(locationCore.userOrCachedSystemLocation).isEqualTo(userLocation)
    }

    @Test
    fun updateConfig() {
        clearInvocations(locationStreamDispatcher)

        locationCore.updateConfig(locationConfig)

        verifyNoMoreInteractions(locationStreamDispatcher)
        verify(executor).execute(runnableCaptor.capture())

        assertThat(runnableCaptor.allValues).hasSize(1)
        runnableCaptor.firstValue.run()

        verify(locationStreamDispatcher).setLocationConfig(locationConfig)
    }

    private fun locationDataCacheUpdateScheduler(): LocationDataCacheUpdateScheduler {
        assertThat(locationDataCacheUpdateSchedulerMockedConstructionRule.constructionMock.constructed()).hasSize(1)
        assertThat(locationDataCacheUpdateSchedulerMockedConstructionRule.argumentInterceptor.flatArguments())
            .containsExactly(executor, locationCore, locationDataCache, "loc")
        return locationDataCacheUpdateSchedulerMockedConstructionRule.constructionMock.constructed().first()
    }

    private fun locationListenerWrapper(): LocationListenerWrapper {
        assertThat(locationListenerWrapperMockedConstructionRule.constructionMock.constructed()).hasSize(1)
        assertThat(locationListenerWrapperMockedConstructionRule.argumentInterceptor.flatArguments())
            .containsExactly(locationStreamDispatcher)
        return locationListenerWrapperMockedConstructionRule.constructionMock.constructed().first()
    }
}
