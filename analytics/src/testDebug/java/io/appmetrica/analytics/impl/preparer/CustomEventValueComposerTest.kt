package io.appmetrica.analytics.impl.preparer

import io.appmetrica.analytics.impl.request.ReportRequestConfig
import io.appmetrica.gradle.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever

internal class CustomEventValueComposerTest : CommonTest() {

    private val valueComposer: ValueComposer = mock()
    private val legacyValueComposer: ValueComposer = mock()
    private val config: ReportRequestConfig = mock()
    private val event: EventFromDbModel = mock()
    private val composer = CustomEventValueComposer(valueComposer, legacyValueComposer)

    private val newValue = byteArrayOf(1, 2)
    private val legacyValue = byteArrayOf(3, 4)

    @Test
    fun `getValue uses legacy composer when valueProtocolVersion is null`() {
        whenever(event.valueProtocolVersion).thenReturn(null)
        whenever(legacyValueComposer.getValue(event, config)).thenReturn(legacyValue)

        assertThat(composer.getValue(event, config)).isEqualTo(legacyValue)
        verify(legacyValueComposer).getValue(event, config)
        verifyNoInteractions(valueComposer)
    }

    @Test
    fun `getValue uses legacy composer when valueProtocolVersion is 0`() {
        whenever(event.valueProtocolVersion).thenReturn(0)
        whenever(legacyValueComposer.getValue(event, config)).thenReturn(legacyValue)

        assertThat(composer.getValue(event, config)).isEqualTo(legacyValue)
        verify(legacyValueComposer).getValue(event, config)
        verifyNoInteractions(valueComposer)
    }

    @Test
    fun `getValue uses legacy composer when valueProtocolVersion is 1`() {
        whenever(event.valueProtocolVersion).thenReturn(1)
        whenever(legacyValueComposer.getValue(event, config)).thenReturn(legacyValue)

        assertThat(composer.getValue(event, config)).isEqualTo(legacyValue)
        verify(legacyValueComposer).getValue(event, config)
        verifyNoInteractions(valueComposer)
    }

    @Test
    fun `getValue uses value composer when valueProtocolVersion is 2`() {
        whenever(event.valueProtocolVersion).thenReturn(2)
        whenever(valueComposer.getValue(event, config)).thenReturn(newValue)

        assertThat(composer.getValue(event, config)).isEqualTo(newValue)
        verify(valueComposer).getValue(event, config)
        verifyNoInteractions(legacyValueComposer)
    }

    @Test
    fun `getValue uses value composer when valueProtocolVersion is greater than 2`() {
        whenever(event.valueProtocolVersion).thenReturn(3)
        whenever(valueComposer.getValue(event, config)).thenReturn(newValue)

        assertThat(composer.getValue(event, config)).isEqualTo(newValue)
        verify(valueComposer).getValue(event, config)
        verifyNoInteractions(legacyValueComposer)
    }
}
