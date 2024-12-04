package io.appmetrica.analytics.coreutils.internal.toggle

import io.appmetrica.analytics.coreapi.internal.data.Savable
import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.kotlin.clearInvocations
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever

class SavableToggleTest : CommonTest() {

    private val savable: Savable<Boolean> = mock()

    @Test
    fun `initial value from savable if true`() {
        whenever(savable.value).thenReturn(true)
        assertThat(SavableToggle("tag", savable).actualState).isTrue()
    }

    @Test
    fun `initial value from savable if false`() {
        whenever(savable.value).thenReturn(false)
        assertThat(SavableToggle("tag", savable).actualState).isFalse()
    }

    @Test
    fun update() {
        whenever(savable.value).thenReturn(false)
        val toggle = SavableToggle("tag", savable)
        clearInvocations(savable)
        toggle.update(true)
        verify(savable).value = true
        verifyNoMoreInteractions(savable)
    }
}
