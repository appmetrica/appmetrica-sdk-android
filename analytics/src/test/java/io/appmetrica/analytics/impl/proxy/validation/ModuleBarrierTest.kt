package io.appmetrica.analytics.impl.proxy.validation

import android.content.Context
import io.appmetrica.analytics.ValidationException
import io.appmetrica.analytics.impl.proxy.AppMetricaFacadeProvider
import io.appmetrica.analytics.testutils.CommonTest
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class ModuleBarrierTest : CommonTest() {

    private val context: Context = mock()
    private val appMetricaFacadeProvider: AppMetricaFacadeProvider = mock<AppMetricaFacadeProvider>()

    private lateinit var modulesBarrier: ModulesBarrier

    @Before
    fun setUp() {
        modulesBarrier = ModulesBarrier(appMetricaFacadeProvider)
    }

    @Test
    fun activate() {
        modulesBarrier.activate(context)
    }

    @Test(expected = ValidationException::class)
    fun `activate for null context`() {
        modulesBarrier.activate(null)
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
}
