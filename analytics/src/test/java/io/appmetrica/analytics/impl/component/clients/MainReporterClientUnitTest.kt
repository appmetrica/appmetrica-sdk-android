package io.appmetrica.analytics.impl.component.clients

import android.content.Context
import android.location.Location
import io.appmetrica.analytics.impl.CounterReport
import io.appmetrica.analytics.impl.GlobalServiceLocator
import io.appmetrica.analytics.impl.InternalEvents
import io.appmetrica.analytics.impl.component.CommonArguments
import io.appmetrica.analytics.impl.component.CommonArguments.ReporterArguments
import io.appmetrica.analytics.impl.component.CommonArgumentsTestUtils
import io.appmetrica.analytics.impl.component.MainReporterComponentUnit
import io.appmetrica.analytics.impl.component.RegularDispatcherComponent
import io.appmetrica.analytics.impl.request.StartupArgumentsTest
import io.appmetrica.analytics.internal.CounterConfiguration
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.GlobalServiceLocatorRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class MainReporterClientUnitTest : CommonTest() {

    @get:Rule
    val globalServiceLocatorRule = GlobalServiceLocatorRule()

    private val componentUnit: RegularDispatcherComponent<MainReporterComponentUnit> = mock()
    private val context: Context = mock()
    private val counterConfiguration: CounterConfiguration = mock()

    private val clientConfiguration: CommonArguments
        get() = CommonArguments(
            StartupArgumentsTest.empty(),
            ReporterArguments(counterConfiguration, null),
            null
        )

    private val mainReporterClientUnit: MainReporterClientUnit by setUp {
        MainReporterClientUnit(context, componentUnit)
    }

    @Test
    fun trackingStatusUpdatingWithTrue() {
        verifyTrackingStatusUpdating(true)
    }

    @Test
    fun trackingStatusUpdatingWithFalse() {
        verifyTrackingStatusUpdating(false)
    }

    private fun verifyTrackingStatusUpdating(value: Boolean) {
        whenever(counterConfiguration.isLocationTrackingEnabled()).thenReturn(value)
        mainReporterClientUnit.handleReport(CounterReport(), clientConfiguration)
        verify(GlobalServiceLocator.getInstance().locationClientApi).updateTrackingStatusFromClient(value)
    }

    @Test
    fun updateLocation() {
        val location: Location = mock()
        whenever(counterConfiguration.manualLocation).thenReturn(location)
        mainReporterClientUnit.handleReport(CounterReport(), clientConfiguration)
        verify(GlobalServiceLocator.getInstance().locationClientApi).updateLocationFromClient(location)
    }

    @Test
    fun `updateLocation if null`() {
        mainReporterClientUnit.handleReport(CounterReport(), clientConfiguration)
        verify(GlobalServiceLocator.getInstance().locationClientApi).updateLocationFromClient(null)
    }

    @Test
    fun dispatchEvent() {
        val counterReport = CounterReport("Test event", InternalEvents.EVENT_TYPE_REGULAR.typeId)
        val mockedArguments = CommonArgumentsTestUtils.createMockedArguments()
        mainReporterClientUnit.handleReport(counterReport, mockedArguments)
        verify(componentUnit, times(1)).handleReport(counterReport, mockedArguments)
    }

    @Test
    fun `updateAdvIdTracking if true`() {
        whenever(counterConfiguration.isAdvIdentifiersTrackingEnabled).thenReturn(true)
        mainReporterClientUnit.handleReport(CounterReport(), clientConfiguration)
        verify(GlobalServiceLocator.getInstance().advertisingIdGetter).updateStateFromClientConfig(true)
    }

    @Test
    fun `updateAdvIdTracking if false`() {
        whenever(counterConfiguration.isAdvIdentifiersTrackingEnabled).thenReturn(false)
        mainReporterClientUnit.handleReport(CounterReport(), clientConfiguration)
        verify(GlobalServiceLocator.getInstance().advertisingIdGetter).updateStateFromClientConfig(false)
    }

    @Test
    fun `updateAdvIdTracking if null`() {
        whenever(counterConfiguration.isAdvIdentifiersTrackingEnabled).thenReturn(null)
        mainReporterClientUnit.handleReport(CounterReport(), clientConfiguration)
        verifyNoInteractions(GlobalServiceLocator.getInstance().advertisingIdGetter)
    }
}
