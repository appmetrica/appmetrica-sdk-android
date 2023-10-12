package io.appmetrica.analytics.impl.component.processor.event

import io.appmetrica.analytics.impl.CounterReport
import io.appmetrica.analytics.impl.component.ComponentId
import io.appmetrica.analytics.impl.component.ComponentUnit
import io.appmetrica.analytics.impl.component.sessionextras.SessionExtrasHolder
import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever

class SaveSessionExtrasHandlerTest : CommonTest() {

    private val sessionExtrasHolder = mock<SessionExtrasHolder>()
    private val componentId = mock<ComponentId>()
    private val component = mock<ComponentUnit> {
        on { sessionExtrasHolder } doReturn sessionExtrasHolder
        on { componentId } doReturn componentId
    }
    private val report = mock<CounterReport>()

    private lateinit var saveSessionExtrasHandler: SaveSessionExtrasHandler

    @Before
    fun setUp() {
        saveSessionExtrasHandler = SaveSessionExtrasHandler(component)
    }

    @Test
    fun `process for empty extras`() {
        whenever(report.extras).thenReturn(mutableMapOf())
        assertThat(saveSessionExtrasHandler.process(report)).isTrue()
        verifyNoMoreInteractions(sessionExtrasHolder)
    }

    @Test
    fun `process for non empty extras`() {
        val firstKey = "first key"
        val secondKey = "second key"
        val firstValue = ByteArray(4) { it.toByte() }
        val secondValue = ByteArray(6) { it.toByte() }

        whenever(report.extras).thenReturn(mutableMapOf(firstKey to firstValue, secondKey to secondValue))
        assertThat(saveSessionExtrasHandler.process(report)).isTrue()

        verify(sessionExtrasHolder).put(firstKey, firstValue)
        verify(sessionExtrasHolder).put(secondKey, secondValue)
        verifyNoMoreInteractions(sessionExtrasHolder)
    }
}
