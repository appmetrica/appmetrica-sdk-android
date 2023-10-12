package io.appmetrica.analytics.coreutils.internal.toggle

import io.appmetrica.analytics.coreapi.internal.control.ToggleObserver
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions

class SimpleThreadSafeToggleTest {

    private val firstObserver = mock<ToggleObserver>()
    private val secondObserver = mock<ToggleObserver>()

    private val toggle = TestSimpleThreadSafeToggle(false)

    @Test
    fun initialValue() {
        assertThat(toggle.actualState).isFalse()
    }

    @Test
    fun initialValueForFalse() {
        assertThat(TestSimpleThreadSafeToggle(true).actualState).isTrue()
    }

    @Test
    fun registerObserver() {
        toggle.registerObserver(firstObserver, true)
        toggle.registerObserver(secondObserver, false)

        verify(firstObserver).onStateChanged(false)
        verifyNoMoreInteractions(secondObserver)
    }

    @Test
    fun updateState() {
        toggle.registerObserver(firstObserver, false)
        toggle.registerObserver(secondObserver, false)

        val initialState = toggle.actualState
        toggle.notifyState(initialState)
        verifyNoMoreInteractions(firstObserver, secondObserver)

        toggle.notifyState(!initialState)
        verify(firstObserver).onStateChanged(!initialState)
        verify(secondObserver).onStateChanged(!initialState)
    }

    @Test
    fun removeObserver() {
        toggle.registerObserver(firstObserver, false)
        toggle.registerObserver(secondObserver, false)
        toggle.removeObserver(firstObserver)

        val initialState = toggle.actualState
        toggle.notifyState(!initialState)
        verify(secondObserver).onStateChanged(!initialState)
        verifyNoMoreInteractions(firstObserver)
    }
}
