package io.appmetrica.analytics.impl.location

import io.appmetrica.analytics.coreapi.internal.control.Toggle
import io.appmetrica.analytics.coreapi.internal.executors.IHandlerExecutor
import io.appmetrica.analytics.coreutils.internal.toggle.ConjunctiveCompositeThreadSafeToggle
import io.appmetrica.analytics.impl.GlobalServiceLocator
import io.appmetrica.analytics.impl.location.toggles.ClientApiTrackingStatusToggle
import io.appmetrica.analytics.impl.location.toggles.WakelocksToggle
import io.appmetrica.analytics.locationapi.internal.LocationControllerObserver
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.GlobalServiceLocatorRule
import io.appmetrica.analytics.testutils.MockedConstructionRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.clearInvocations
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever

internal class LocationControllerImplTest : CommonTest() {

    private val resultToggle = mock<ConjunctiveCompositeThreadSafeToggle>()
    private val executor = mock<IHandlerExecutor>()
    private val firstLocationControllerObserver = mock<LocationControllerObserver>()
    private val secondLocationControllerObserver = mock<LocationControllerObserver>()
    private val outerAppStateToggle = mock<Toggle>()
    private val wakelockToggle = mock<WakelocksToggle>()
    private val clientTrackingStatusController = mock<ClientApiTrackingStatusToggle>()

    private val runnableCaptor = argumentCaptor<Runnable>()

    @get:Rule
    val globalServiceLocatorRule = GlobalServiceLocatorRule()

    @get:Rule
    val togglesHolderMockedConstructionRule = MockedConstructionRule(TogglesHolder::class.java) { mock, mockedContext ->
        whenever(mock.resultLocationControlToggle).thenReturn(resultToggle)
        whenever(mock.wakelocksToggle).thenReturn(wakelockToggle)
        whenever(mock.clientTrackingStatusController).thenReturn(clientTrackingStatusController)
    }

    private lateinit var locationControllerImpl: LocationControllerImpl

    @Before
    fun setUp() {
        whenever(GlobalServiceLocator.getInstance().serviceExecutorProvider.moduleExecutor).thenReturn(executor)

        locationControllerImpl = LocationControllerImpl()
    }

    @Test
    fun init() {
        locationControllerImpl.init(outerAppStateToggle)
        verify(resultToggle).registerObserver(locationControllerImpl, true)
        assertThat(togglesHolderMockedConstructionRule.constructionMock.constructed()).hasSize(1)
        assertThat(togglesHolderMockedConstructionRule.argumentInterceptor.flatArguments())
            .containsExactly(outerAppStateToggle)
    }

    @Test
    fun `registerObserver sticky before state updating`() {
        locationControllerImpl.registerObserver(firstLocationControllerObserver, true)
        verifyNoMoreInteractions(firstLocationControllerObserver)
        touchLocationExecutorRunnable()
        verify(firstLocationControllerObserver).stopLocationTracking()
    }

    @Test
    fun `registerObserver non sticky before state updating`() {
        locationControllerImpl.registerObserver(firstLocationControllerObserver, false)
        verifyNoMoreInteractions(firstLocationControllerObserver)
        touchLocationExecutorRunnable()
        verifyNoMoreInteractions(firstLocationControllerObserver)
    }

    @Test
    fun `registerObserver default sticky before state updating`() {
        locationControllerImpl.registerObserver(firstLocationControllerObserver)
        verifyNoMoreInteractions(firstLocationControllerObserver)
        touchLocationExecutorRunnable()
        verify(firstLocationControllerObserver).stopLocationTracking()
    }

    @Test
    fun `registerObserver with sticky after state updating`() {
        locationControllerImpl.onStateChanged(true)
        touchLocationExecutorRunnable()
        locationControllerImpl.registerObserver(firstLocationControllerObserver)
        verifyNoMoreInteractions(firstLocationControllerObserver)
        touchLocationExecutorRunnable()
        verify(firstLocationControllerObserver).startLocationTracking()
    }

    @Test
    fun `registerObserver without sticky after state updating`() {
        locationControllerImpl.onStateChanged(true)
        touchLocationExecutorRunnable()
        locationControllerImpl.registerObserver(firstLocationControllerObserver, false)
        verifyNoMoreInteractions(firstLocationControllerObserver)
        touchLocationExecutorRunnable()
        verifyNoMoreInteractions(firstLocationControllerObserver)
    }

    @Test
    fun `registerObserver with default sticky after state updating`() {
        locationControllerImpl.onStateChanged(true)
        touchLocationExecutorRunnable()
        locationControllerImpl.registerObserver(firstLocationControllerObserver)
        verifyNoMoreInteractions(firstLocationControllerObserver)
        touchLocationExecutorRunnable()
        verify(firstLocationControllerObserver).startLocationTracking()
    }

    @Test
    fun `onStateChanged from false to false`() {
        registerBothNonStickyObservers()
        locationControllerImpl.onStateChanged(false)
        touchLocationExecutorRunnable()
        verifyNoMoreInteractions(firstLocationControllerObserver, secondLocationControllerObserver)
    }

    @Test
    fun `onStateChanged from false to true`() {
        registerBothNonStickyObservers()
        locationControllerImpl.onStateChanged(true)
        verifyNoMoreInteractions(firstLocationControllerObserver, secondLocationControllerObserver)
        touchLocationExecutorRunnable()
        verify(firstLocationControllerObserver).startLocationTracking()
        verify(secondLocationControllerObserver).startLocationTracking()
    }

    @Test
    fun `onStateChanged from true to false`() {
        locationControllerImpl.onStateChanged(true)
        touchLocationExecutorRunnable()
        registerBothNonStickyObservers()
        locationControllerImpl.onStateChanged(false)
        verifyNoMoreInteractions(firstLocationControllerObserver, secondLocationControllerObserver)
        touchLocationExecutorRunnable()
        verify(firstLocationControllerObserver).stopLocationTracking()
        verify(secondLocationControllerObserver).stopLocationTracking()
    }

    private fun registerBothNonStickyObservers() {
        locationControllerImpl.registerObserver(firstLocationControllerObserver, false)
        touchLocationExecutorRunnable()
        locationControllerImpl.registerObserver(secondLocationControllerObserver, false)
        touchLocationExecutorRunnable()
    }

    private fun touchLocationExecutorRunnable() {
        verify(executor).execute(runnableCaptor.capture())
        runnableCaptor.lastValue.run()
        clearInvocations(executor)
    }
}
