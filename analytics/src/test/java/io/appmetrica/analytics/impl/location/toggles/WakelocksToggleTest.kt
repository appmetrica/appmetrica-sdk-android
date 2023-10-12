package io.appmetrica.analytics.impl.location.toggles

import io.appmetrica.analytics.coreapi.internal.control.ToggleObserver
import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.clearInvocations
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.verifyNoMoreInteractions
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class WakelocksToggleTest : CommonTest() {

    private val firstRegistrant = mock<Any>()
    private val secondRegistrant = mock<Any>()

    private val observer = mock<ToggleObserver>()

    private lateinit var toggle: WakelocksToggle

    @Before
    fun setUp() {
        toggle = WakelocksToggle()
        toggle.registerObserver(observer, false)
    }

    @Test
    fun initialState() {
        assertThat(toggle.actualState).isFalse()
    }

    @Test
    fun register() {
        toggle.registerWakelock(firstRegistrant)
        verify(observer).onStateChanged(true)

        toggle.registerWakelock(secondRegistrant)
        verifyNoMoreInteractions(observer)
    }

    @Test
    fun remove() {
        toggle.registerWakelock(firstRegistrant)
        toggle.registerWakelock(secondRegistrant)
        clearInvocations(observer)

        toggle.removeWakelock(firstRegistrant)
        verifyNoMoreInteractions(observer)
        toggle.removeWakelock(secondRegistrant)
        verify(observer).onStateChanged(false)
    }
}
