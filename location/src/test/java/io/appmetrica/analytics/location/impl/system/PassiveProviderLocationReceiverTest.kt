package io.appmetrica.analytics.location.impl.system

import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.os.Looper
import io.appmetrica.analytics.coreapi.internal.backport.FunctionWithThrowable
import io.appmetrica.analytics.coreapi.internal.permission.PermissionResolutionStrategy
import io.appmetrica.analytics.coreutils.internal.system.SystemServiceUtils
import io.appmetrica.analytics.location.impl.LocationListenerWrapper
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.MockedStaticRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyZeroInteractions
import org.mockito.kotlin.whenever
import java.util.concurrent.TimeUnit

internal class PassiveProviderLocationReceiverTest : CommonTest() {

    private val context = mock<Context>()
    private val looper = mock<Looper>()
    private val permissionResolutionStrategy = mock<PermissionResolutionStrategy> {
        on { hasNecessaryPermissions(context) } doReturn true
    }
    private val location = mock<Location>()
    private val locationManager = mock<LocationManager> {
        on { getLastKnownLocation(LocationManager.PASSIVE_PROVIDER) } doReturn location
    }
    private val locationListener = mock<LocationListenerWrapper>()

    private val locationFunctionCaptor = argumentCaptor<FunctionWithThrowable<LocationManager, Location?>>()
    private val unitFunctionCaptor = argumentCaptor<FunctionWithThrowable<LocationManager, Unit?>>()

    private lateinit var passiveProviderLocationReceiver: PassiveProviderLocationReceiver

    private val passiveProviderTimeInterval = TimeUnit.SECONDS.toMillis(1)
    private val passiveProviderDistanceInterval = 0f

    @get:Rule
    val utilsMockedRule = MockedStaticRule(SystemServiceUtils::class.java)

    @Before
    fun setUp() {
        passiveProviderLocationReceiver = PassiveProviderLocationReceiver(
            context,
            looper,
            permissionResolutionStrategy,
            locationListener
        )
        whenever(
            SystemServiceUtils.accessSystemServiceByNameSafely(
            eq(context),
            eq(Context.LOCATION_SERVICE),
            any(),
            any(),
            locationFunctionCaptor.capture())).thenReturn(location)
    }

    @Test
    fun updateLastKnownLocation() {
        passiveProviderLocationReceiver.updateLastKnownLocation()
        val locationFromFunction = touchFunctionWithThrowable()
        verify(locationListener).onLocationChanged(location)
        assertThat(locationFromFunction).isEqualTo(location)
    }

    @Test
    fun `updateLastKnownLocation() if no permission`() {
        whenever(permissionResolutionStrategy.hasNecessaryPermissions(context)).thenReturn(false)
        passiveProviderLocationReceiver.updateLastKnownLocation()
        utilsMockedRule.staticMock.verifyNoInteractions()
    }

    @Test
    fun `updateLastKnownLocation() if not location`() {
        whenever(locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER)).thenReturn(null)
        whenever(
            SystemServiceUtils.accessSystemServiceByNameSafely(
            eq(context),
            eq(Context.LOCATION_SERVICE),
            any(),
            any(),
            locationFunctionCaptor.capture())).thenReturn(null)
        passiveProviderLocationReceiver.updateLastKnownLocation()
        val locationFromFunction = touchFunctionWithThrowable()
        verifyZeroInteractions(locationListener)
        assertThat(locationFromFunction).isNull()
    }

    @Test
    fun startLocationUpdates() {
        passiveProviderLocationReceiver.startLocationUpdates()
        touchConsumerWithThrowable()
        verify(locationManager).requestLocationUpdates(
            LocationManager.PASSIVE_PROVIDER,
            passiveProviderTimeInterval,
            passiveProviderDistanceInterval,
            locationListener,
            looper
        )
    }

    @Test
    fun `startLocationUpdates() if no permissions`() {
        whenever(permissionResolutionStrategy.hasNecessaryPermissions(context)).thenReturn(false)
        passiveProviderLocationReceiver.startLocationUpdates()
        utilsMockedRule.staticMock.verifyNoInteractions()
    }

    @Test
    fun stopLocationUpdates() {
        passiveProviderLocationReceiver.stopLocationUpdates()
        touchConsumerWithThrowable()
        verify(locationManager).removeUpdates(locationListener)
    }


    private fun touchConsumerWithThrowable() {
        utilsMockedRule.staticMock.verify {
            SystemServiceUtils.accessSystemServiceByNameSafely(
                eq(context),
                eq(Context.LOCATION_SERVICE),
                any(),
                any(),
                unitFunctionCaptor.capture()
            )
        }
        unitFunctionCaptor.firstValue.apply(locationManager)
    }

    private fun touchFunctionWithThrowable(): Location? {
        utilsMockedRule.staticMock.verify {
            SystemServiceUtils.accessSystemServiceByNameSafely(
                eq(context),
                eq(Context.LOCATION_SERVICE),
                any(),
                any(),
                locationFunctionCaptor.capture()
            )
        }
        return locationFunctionCaptor.firstValue.apply(locationManager)
    }
}
