package io.appmetrica.analytics.location.impl

import android.location.Location
import io.appmetrica.analytics.coreapi.internal.backport.Consumer
import io.appmetrica.analytics.coreutils.internal.time.TimePassedChecker
import io.appmetrica.analytics.locationapi.internal.LocationFilter
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.MockedConstructionRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.clearInvocations
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyZeroInteractions
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class SingleProviderLocationFiltratorTests : CommonTest() {

    @get:Rule
    val timePassedCheckerMockedRule = MockedConstructionRule(TimePassedChecker::class.java)

    private val firstConsumer = mock<Consumer<Location>>()
    private val secondConsumer = mock<Consumer<Location>>()

    private val updateTimeIntervalValue = 100L
    private val updateDistanceIntervalValue = 200f
    private val locationFilter = mock<LocationFilter> {
        on { updateTimeInterval } doReturn updateTimeIntervalValue
        on { updateDistanceInterval } doReturn updateDistanceIntervalValue
    }

    private val firstLocationTime = 1000000000L
    private val firstLocation = mock<Location> {
        on { time } doReturn firstLocationTime
    }

    private val secondLocation = mock<Location> {
        on { time } doReturn firstLocationTime + updateTimeIntervalValue - 1
        on { distanceTo(firstLocation) } doReturn updateDistanceIntervalValue - 10
    }

    private lateinit var singleProviderLocationFiltrator: SingleProviderLocationFiltrator
    private lateinit var timePassedChecker: TimePassedChecker

    @Before
    fun setUp() {
        singleProviderLocationFiltrator = SingleProviderLocationFiltrator(locationFilter)
        timePassedChecker = timePassChecker()
        singleProviderLocationFiltrator.registerConsumer(firstConsumer)
        singleProviderLocationFiltrator.registerConsumer(secondConsumer)
        singleProviderLocationFiltrator.handleLocation(firstLocation)
    }

    @Test
    fun `handleLocation if last location was null`() {
        verify(firstConsumer).consume(firstLocation)
        verify(secondConsumer).consume(firstLocation)
    }

    @Test
    fun `handleLocation if receive the same location`() {
        clearInvocations(firstConsumer, secondConsumer)
        singleProviderLocationFiltrator.handleLocation(firstLocation)
        verifyZeroInteractions(firstConsumer, secondConsumer)
    }

    @Test
    fun `handleLocation without consumers if last location is null`() {
        singleProviderLocationFiltrator = SingleProviderLocationFiltrator(
            locationFilter
        )
        singleProviderLocationFiltrator.handleLocation(firstLocation)
    }

    @Test
    fun `handleLocation if second location close to first`() {
        clearInvocations(firstConsumer, secondConsumer)
        singleProviderLocationFiltrator.handleLocation(secondLocation)
        verifyZeroInteractions(firstConsumer, secondConsumer)
    }

    @Test
    fun `handleLocation if second location far from first`() {
        whenever(secondLocation.distanceTo(firstLocation)).thenReturn(updateDistanceIntervalValue + 100)
        clearInvocations(firstConsumer, secondConsumer)
        singleProviderLocationFiltrator.handleLocation(secondLocation)
        verify(firstConsumer).consume(secondLocation)
        verify(secondConsumer).consume(secondLocation)
    }

    @Test
    fun `handleLocation is secondLocation newer than first`() {
        whenever(timePassedChecker.didTimePassMillis(any(), eq(updateTimeIntervalValue), any()))
            .thenReturn(true)
        clearInvocations(firstConsumer, secondConsumer)
        singleProviderLocationFiltrator.handleLocation(secondLocation)
        verify(firstConsumer).consume(secondLocation)
        verify(secondConsumer).consume(secondLocation)
    }

    @Test
    fun `handleLocation if second location older`() {
        whenever(secondLocation.distanceTo(firstLocation)).thenReturn(updateDistanceIntervalValue + 100)
        whenever(timePassedChecker.didTimePassMillis(eq(firstLocationTime), eq(updateTimeIntervalValue), any()))
            .thenReturn(true)
        whenever(secondLocation.time).thenReturn(firstLocationTime - 10)
        clearInvocations(firstConsumer, secondConsumer)
        singleProviderLocationFiltrator.handleLocation(secondLocation)
        verifyZeroInteractions(firstConsumer, secondConsumer)
    }

    private fun timePassChecker(): TimePassedChecker {
        assertThat(timePassedCheckerMockedRule.constructionMock.constructed()).hasSize(1)
        assertThat(timePassedCheckerMockedRule.argumentInterceptor.flatArguments()).isEmpty()
        return timePassedCheckerMockedRule.constructionMock.constructed().first()
    }
}
