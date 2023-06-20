package io.appmetrica.analytics.location.impl.system

import android.content.Context
import android.location.Location
import android.location.LocationManager
import io.appmetrica.analytics.coreapi.internal.backport.FunctionWithThrowable
import io.appmetrica.analytics.coreapi.internal.permission.PermissionResolutionStrategy
import io.appmetrica.analytics.coreutils.internal.system.SystemServiceUtils
import io.appmetrica.analytics.location.impl.LocationListenerWrapper
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

class SystemLocationLastKnownExtractorTest {

    @get:Rule
    val utilsMockedRule = MockedStaticRule(SystemServiceUtils::class.java)

    private val context = mock<Context>()
    private val permissionResolutionStrategy = mock<PermissionResolutionStrategy> {
        on { hasNecessaryPermissions(context) } doReturn true
    }
    private val location = mock<Location>()
    private val provider = "Some provider"
    private val locationManager = mock<LocationManager> {
        on { getLastKnownLocation(provider) } doReturn location
    }
    private val functionWithThrowableCaptor = argumentCaptor<FunctionWithThrowable<LocationManager, Location?>>()
    private val locationListener = mock<LocationListenerWrapper>()

    private lateinit var systemLastKnownLocationExtractor: SystemLastKnownLocationExtractor

    @Before
    fun setUp() {
        whenever(
            SystemServiceUtils.accessSystemServiceByNameSafely(
            eq(context),
            eq(Context.LOCATION_SERVICE),
            any(),
            any(),
            functionWithThrowableCaptor.capture()
        )).thenReturn(location)
        systemLastKnownLocationExtractor = SystemLastKnownLocationExtractor(
            context,
            permissionResolutionStrategy,
            locationListener,
            provider
        )
    }

    @Test
    fun updateLastKnownLocation() {
        systemLastKnownLocationExtractor.updateLastKnownLocation()
        assertThat(touchFunctionWithThrowable()).isEqualTo(location)
        verify(locationListener).onLocationChanged(location)
    }

    @Test
    fun `updateLastKnownLocation() if no permissions`() {
        whenever(permissionResolutionStrategy.hasNecessaryPermissions(context)).thenReturn(false)
        systemLastKnownLocationExtractor.updateLastKnownLocation()
        utilsMockedRule.staticMock.verifyNoInteractions()
    }

    @Test
    fun `updateLastKnownLocation() if location is null`() {
        whenever(
            SystemServiceUtils.accessSystemServiceByNameSafely(
            eq(context),
            eq(Context.LOCATION_SERVICE),
            any(),
            any(),
            functionWithThrowableCaptor.capture()
        )).thenReturn(null)
        systemLastKnownLocationExtractor.updateLastKnownLocation()
        touchFunctionWithThrowable()
        verifyZeroInteractions(locationListener)
    }

    private fun touchFunctionWithThrowable(): Location? {
        utilsMockedRule.staticMock.verify {
            SystemServiceUtils.accessSystemServiceByNameSafely(
                eq(context),
                eq(Context.LOCATION_SERVICE),
                any(),
                any(),
                functionWithThrowableCaptor.capture()
            )
        }
        return functionWithThrowableCaptor.firstValue.apply(locationManager)
    }
}
