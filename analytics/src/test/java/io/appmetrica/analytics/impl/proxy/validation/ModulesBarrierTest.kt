package io.appmetrica.analytics.impl.proxy.validation

import android.content.Context
import io.appmetrica.analytics.AdRevenue
import io.appmetrica.analytics.ModuleEvent
import io.appmetrica.analytics.ValidationException
import io.appmetrica.analytics.impl.proxy.AppMetricaFacadeProvider
import io.appmetrica.analytics.testutils.CommonTest
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.util.Currency
import java.util.UUID

class ModulesBarrierTest : CommonTest() {

    private val context: Context = mock()
    private val appMetricaFacadeProvider: AppMetricaFacadeProvider = mock<AppMetricaFacadeProvider>()

    private val modulesBarrier by setUp { ModulesBarrier(appMetricaFacadeProvider) }

    @Test
    fun setAdvIdentifiersTracking() {
        modulesBarrier.setAdvIdentifiersTracking(true)
    }

    @Test
    fun `reportEvent if activated`() {
        whenever(appMetricaFacadeProvider.isActivated).thenReturn(true)
        modulesBarrier.reportEvent(ModuleEvent.newBuilder(1).build())
    }

    @Test(expected = ValidationException::class)
    fun `reportEvent if not activated`() {
        whenever(appMetricaFacadeProvider.isActivated).thenReturn(false)
        modulesBarrier.reportEvent(ModuleEvent.newBuilder(1).build())
    }

    @Test
    fun setSessionExtra() {
        modulesBarrier.setSessionExtra("Some key", byteArrayOf(1, 2, 3))
    }

    @Test(expected = ValidationException::class)
    fun setSessionExtraForNullKey() {
        modulesBarrier.setSessionExtra(null, byteArrayOf(2, 3, 4))
    }

    @Test
    fun setSessionExtraForNullValue() {
        modulesBarrier.setSessionExtra("key", null)
    }

    @Test
    fun setSessionExtraForEmptyValue() {
        modulesBarrier.setSessionExtra("key", ByteArray(0))
    }

    @Test
    fun reportExternalAttribution() {
        modulesBarrier.reportExternalAttribution(1, "Some value")
    }

    @Test
    fun isActivatedForApp() {
        modulesBarrier.isActivatedForApp()
    }

    @Test
    fun sendEventsBuffer() {
        modulesBarrier.sendEventsBuffer()
    }

    @Test
    fun getReporter() {
        modulesBarrier.getReporter(context, UUID.randomUUID().toString())
    }

    @Test(expected = ValidationException::class)
    fun `getReporter if context is null`() {
        modulesBarrier.getReporter(null, UUID.randomUUID().toString())
    }

    @Test(expected = ValidationException::class)
    fun `getReporter if apiKey is null`() {
        modulesBarrier.getReporter(context, null)
    }

    @Test(expected = ValidationException::class)
    fun `getReporter if apiKey is invalid`() {
        whenever(appMetricaFacadeProvider.isActivated).thenReturn(true)
        modulesBarrier.getReporter(context, "invalid")
    }

    @Test
    fun reportAdRevenue() {
        modulesBarrier.reportAdRevenue(
            AdRevenue.newBuilder(200L, Currency.getInstance("EUR"))
                .build(),
            true
        )
    }

    @Test
    fun subscribeForAutoCollectedData() {
        modulesBarrier.subscribeForAutoCollectedData(context, UUID.randomUUID().toString())
    }

    @Test(expected = ValidationException::class)
    fun `subscribeForAutoCollectedData if context is null`() {
        modulesBarrier.subscribeForAutoCollectedData(null, UUID.randomUUID().toString())
    }

    @Test(expected = ValidationException::class)
    fun `subscribeForAutoCollectedData if apiKey is null`() {
        modulesBarrier.subscribeForAutoCollectedData(context, null)
    }

    @Test(expected = ValidationException::class)
    fun `subscribeForAutoCollectedData if apiKey is invalid`() {
        whenever(appMetricaFacadeProvider.isActivated).thenReturn(true)
        modulesBarrier.subscribeForAutoCollectedData(context, "invalid")
    }
}
