package io.appmetrica.analytics.impl.proxy.validation

import android.content.Context
import io.appmetrica.analytics.AppMetricaLibraryAdapterConfig
import io.appmetrica.analytics.impl.proxy.AppMetricaFacadeProvider
import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.util.UUID

class LibraryAdapterBarrierTest : CommonTest() {

    private val context: Context = mock()
    private val appMetricaFacadeProvider: AppMetricaFacadeProvider = mock<AppMetricaFacadeProvider>()
    private val config: AppMetricaLibraryAdapterConfig = mock()

    private lateinit var barrier: LibraryAdapterBarrier

    @Before
    fun setUp() {
        barrier = LibraryAdapterBarrier(appMetricaFacadeProvider)
        whenever(appMetricaFacadeProvider.isActivated).thenReturn(true)
    }

    @Test
    fun activate() {
        assertThat(barrier.activate(context, config)).isTrue()
    }

    @Test
    fun `activate for null context`() {
        assertThat(barrier.activate(null, config)).isFalse()
    }

    @Test
    fun `activate for null config`() {
        assertThat(barrier.activate(context, null)).isFalse()
    }

    @Test
    fun setAdvIdentifiersTracking() {
        assertThat(barrier.setAdvIdentifiersTracking(true)).isTrue()
    }

    @Test
    fun reportEvent() {
        assertThat(barrier.reportEvent("sender", "event", "payload")).isTrue()
    }

    @Test
    fun `reportEvent for null sender`() {
        assertThat(barrier.reportEvent(null, "event", "payload")).isFalse()
    }

    @Test
    fun `reportEvent for null event`() {
        assertThat(barrier.reportEvent("sender", null, "payload")).isFalse()
    }

    fun `reportEvent for null payload`() {
        assertThat(barrier.reportEvent("sender", "event", null)).isFalse()
    }

    @Test
    fun `reportEvent if not activated`() {
        whenever(appMetricaFacadeProvider.isActivated).thenReturn(false)
        assertThat(barrier.reportEvent("sender", "event", "payload")).isFalse()
    }

    @Test
    fun subscribeForAutoCollectedData() {
        assertThat(barrier.subscribeForAutoCollectedData(context, UUID.randomUUID().toString()))
            .isTrue()
    }

    @Test
    fun `subscribeForAutoCollectedData for null context`() {
        assertThat(barrier.subscribeForAutoCollectedData(null, "apiKey"))
            .isFalse()
    }

    @Test
    fun `subscribeForAutoCollectedData for null apiKey`() {
        assertThat(barrier.subscribeForAutoCollectedData(context, null))
            .isFalse()
    }
}
