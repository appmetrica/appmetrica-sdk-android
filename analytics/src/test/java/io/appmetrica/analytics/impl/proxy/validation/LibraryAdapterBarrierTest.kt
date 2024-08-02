package io.appmetrica.analytics.impl.proxy.validation

import android.content.Context
import io.appmetrica.analytics.ValidationException
import io.appmetrica.analytics.impl.proxy.AppMetricaFacadeProvider
import io.appmetrica.analytics.testutils.CommonTest
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class LibraryAdapterBarrierTest : CommonTest() {

    private val context: Context = mock()
    private val appMetricaFacadeProvider: AppMetricaFacadeProvider = mock<AppMetricaFacadeProvider>()

    private lateinit var barrier: LibraryAdapterBarrier

    @Before
    fun setUp() {
        barrier = LibraryAdapterBarrier(appMetricaFacadeProvider)
        whenever(appMetricaFacadeProvider.isActivated).thenReturn(true)
    }

    @Test
    fun activate() {
        barrier.activate(context)
    }

    @Test(expected = ValidationException::class)
    fun `activate for null context`() {
        barrier.activate(null)
    }

    @Test
    fun reportEvent() {
        barrier.reportEvent("sender", "event", "payload")
    }

    @Test(expected = ValidationException::class)
    fun `reportEvent for null sender`() {
        barrier.reportEvent(null, "event", "payload")
    }

    @Test(expected = ValidationException::class)
    fun `reportEvent for null event`() {
        barrier.reportEvent("sender", null, "payload")
    }

    @Test(expected = ValidationException::class)
    fun `reportEvent for null payload`() {
        barrier.reportEvent("sender", "event", null)
    }

    @Test(expected = ValidationException::class)
    fun `reportEvent if not activated`() {
        whenever(appMetricaFacadeProvider.isActivated).thenReturn(false)
        barrier.reportEvent("sender", "event", "payload")
    }
}
