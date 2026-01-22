package io.appmetrica.analytics.impl.location.toggles

import io.appmetrica.analytics.coreapi.internal.control.ToggleObserver
import io.appmetrica.analytics.impl.db.preferences.PreferencesServiceDbStorage
import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

internal class ClientApiTrackingStatusToggleTest : CommonTest() {

    private val storage = mock<PreferencesServiceDbStorage>()

    private val toggleObserver = mock<ToggleObserver>()

    private lateinit var clientStatusToggle: ClientApiTrackingStatusToggle

    @Before
    fun setUp() {
        clientStatusToggle = ClientApiTrackingStatusToggle(storage)
    }

    @Test
    fun `initialValue for true`() {
        whenever(storage.isLocationTrackingEnabled).thenReturn(true)
        assertThat(ClientApiTrackingStatusToggle(storage).actualState).isTrue()
    }

    @Test
    fun `initialValue for false`() {
        whenever(storage.isLocationTrackingEnabled).thenReturn(false)
        assertThat(ClientApiTrackingStatusToggle(storage).actualState).isFalse()
    }

    @Test
    fun updateStatus() {
        clientStatusToggle.registerObserver(toggleObserver, true)
        clientStatusToggle.updateTrackingStatus(true)
        inOrder(toggleObserver) {
            verify(toggleObserver).onStateChanged(false)
            verify(toggleObserver).onStateChanged(true)
            verifyNoMoreInteractions()
        }
    }
}
