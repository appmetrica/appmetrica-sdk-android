package io.appmetrica.analytics.impl.db

import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever

class CompositeFileVitalDataSourceTest : CommonTest() {

    private val firstData = "First data"
    private val firstSource: VitalDataSource = mock {
        on { getVitalData() } doReturn firstData
    }

    private val secondData = "Second data"
    private val secondSource: VitalDataSource = mock {
        on { getVitalData() } doReturn secondData
    }

    private val compositeFileVitalDataSource: CompositeFileVitalDataSource by setUp {
        CompositeFileVitalDataSource(
            listOf(
                "first" to firstSource,
                "second" to secondSource
            )
        )
    }

    @Test
    fun `getVitalData for both filled`() {
        assertThat(compositeFileVitalDataSource.getVitalData()).isEqualTo(firstData)
        verifyNoInteractions(secondSource)
    }

    @Test
    fun `getVitalData for first null`() {
        whenever(firstSource.getVitalData()).thenReturn(null)
        assertThat(compositeFileVitalDataSource.getVitalData()).isEqualTo(secondData)
    }

    @Test
    fun `getVitalData for empty first`() {
        whenever(firstSource.getVitalData()).thenReturn("")
        assertThat(compositeFileVitalDataSource.getVitalData()).isEqualTo(secondData)
    }

    @Test
    fun `getVitalData for both are empty`() {
        whenever(firstSource.getVitalData()).thenReturn("")
        whenever(secondSource.getVitalData()).thenReturn("")
        assertThat(compositeFileVitalDataSource.getVitalData()).isNull()
    }

    @Test
    fun `getVitalData for both are null`() {
        whenever(firstSource.getVitalData()).thenReturn(null)
        whenever(secondSource.getVitalData()).thenReturn(null)
        assertThat(compositeFileVitalDataSource.getVitalData()).isNull()
    }

    @Test
    fun putVitalData() {
        val data = "data to save"
        compositeFileVitalDataSource.putVitalData(data)
        verify(firstSource).putVitalData(data)
        verify(secondSource).putVitalData(data)
    }
}
