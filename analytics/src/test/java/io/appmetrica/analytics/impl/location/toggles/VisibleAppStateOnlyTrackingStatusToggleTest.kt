package io.appmetrica.analytics.impl.location.toggles

import io.appmetrica.analytics.coreapi.internal.control.ToggleObserver
import io.appmetrica.analytics.coreapi.internal.servicecomponents.applicationstate.ApplicationState
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.GlobalServiceLocatorRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.clearInvocations
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class VisibleAppStateOnlyTrackingStatusToggleTest : CommonTest() {

    @get:Rule
    val globalServiceLocatorRule = GlobalServiceLocatorRule()

    private val observer = mock<ToggleObserver>()

    private lateinit var toggle: VisibleAppStateOnlyTrackingStatusToggle

    @Before
    fun setUp() {
        toggle = VisibleAppStateOnlyTrackingStatusToggle()
    }

    @Test
    fun initialState() {
        assertThat(toggle.actualState).isFalse()
    }

    @Test
    fun registerStickyObserver() {
        toggle.registerObserver(observer, true)
        verify(observer).onStateChanged(false)
    }

    @Test
    fun onApplicationStateChanged() {
        toggle.registerObserver(observer, sticky = false)

        toggle.onApplicationStateChanged(ApplicationState.UNKNOWN)
        verifyNoMoreInteractions(observer)

        toggle.onApplicationStateChanged(ApplicationState.BACKGROUND)
        verifyNoMoreInteractions(observer)

        toggle.onApplicationStateChanged(ApplicationState.VISIBLE)
        verify(observer).onStateChanged(true)
        clearInvocations(observer)

        toggle.onApplicationStateChanged(ApplicationState.VISIBLE)
        verifyNoMoreInteractions(observer)

        toggle.onApplicationStateChanged(ApplicationState.BACKGROUND)
        verify(observer).onStateChanged(false)
        clearInvocations(observer)

        toggle.onApplicationStateChanged(ApplicationState.BACKGROUND)
        verifyNoMoreInteractions(observer)
    }
}
