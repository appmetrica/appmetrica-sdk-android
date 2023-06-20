package io.appmetrica.analytics.impl.proxy

import android.content.Context
import io.appmetrica.analytics.AppMetrica
import io.appmetrica.analytics.ModuleEvent
import io.appmetrica.analytics.coreapi.internal.executors.ICommonExecutor
import io.appmetrica.analytics.impl.AppMetricaFacade
import io.appmetrica.analytics.impl.MainReporter
import io.appmetrica.analytics.impl.MainReporterApiConsumerProvider
import io.appmetrica.analytics.impl.proxy.validation.ModulesBarrier
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.MockedStaticRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.KArgumentCaptor
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.verifyZeroInteractions
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import java.util.UUID

@RunWith(RobolectricTestRunner::class)
class ModulesProxyTest : CommonTest() {

    private val provider: AppMetricaFacadeProvider = mock()
    private val executor: ICommonExecutor = mock()
    private val modulesBarrier: ModulesBarrier = mock()
    private val mainReporter: MainReporter = mock()
    private val mainReporterApiConsumerProvider: MainReporterApiConsumerProvider = mock()
    private val facade: AppMetricaFacade = mock()
    private val reporterProxyStorageImpl: ReporterProxyStorage = mock()

    private val runnableArgumentCaptor: KArgumentCaptor<Runnable> = argumentCaptor()

    @get:Rule
    val reporterProxyStorage = MockedStaticRule(ReporterProxyStorage::class.java)

    @get:Rule
    val appMetrica = MockedStaticRule(AppMetrica::class.java)

    private lateinit var proxy: ModulesProxy

    @Before
    fun setUp() {
        whenever(provider.peekInitializedImpl()).thenReturn(facade)
        whenever(facade.mainReporterApiConsumerProvider).thenReturn(mainReporterApiConsumerProvider)
        whenever(mainReporterApiConsumerProvider.mainReporter).thenReturn(mainReporter)

        proxy = ModulesProxy(
            executor,
            provider,
            modulesBarrier
        )
    }

    @Test
    fun testReportEvent() {
        val moduleEvent: ModuleEvent = mock()
        proxy.reportEvent(moduleEvent)

        val inOrder = inOrder(modulesBarrier, executor)
        inOrder.verify(modulesBarrier).reportEvent(moduleEvent)
        inOrder.verify(executor, times(1)).execute(runnableArgumentCaptor.capture())
        verifyZeroInteractions(mainReporter)

        runnableArgumentCaptor.firstValue.run()

        verify(mainReporter).reportEvent(moduleEvent)
        verifyNoMoreInteractions(mainReporter)
    }

    @Test
    fun setSessionExtra() {
        val key = "Key"
        val value = ByteArray(5) { it.toByte() }

        proxy.setSessionExtra(key, value)

        inOrder(modulesBarrier, executor, mainReporter) {
            verify(modulesBarrier).setSessionExtra(key, value)
            verify(executor).execute(runnableArgumentCaptor.capture())
            verifyZeroInteractions(mainReporter)
        }

        runnableArgumentCaptor.firstValue.run()

        verify(mainReporter).setSessionExtra(key, value)
        verifyNoMoreInteractions(mainReporter)
    }

    @Test
    fun testSendEventsBuffer() {
        proxy.sendEventsBuffer()
        appMetrica.staticMock.verify { AppMetrica.sendEventsBuffer() }
        appMetrica.staticMock.verifyNoMoreInteractions()
        verify(modulesBarrier).sendEventsBuffer()
    }

    @Test
    fun getReporter() {
        val context: Context = mock()
        val apiKey = UUID.randomUUID().toString()
        whenever(reporterProxyStorageImpl.getOrCreate(context, apiKey)).thenReturn(mock())
        whenever(ReporterProxyStorage.getInstance()).thenReturn(reporterProxyStorageImpl)
        proxy.getReporter(context, apiKey)
        verify(modulesBarrier).getReporter(context, apiKey)
        verify(reporterProxyStorageImpl).getOrCreate(context, apiKey)
    }

    @Test
    fun testIsActivatedForTrue() {
        testIsActivated(true)
    }

    @Test
    fun testIsActivatedForFalse() {
        testIsActivated(false)
    }

    private fun testIsActivated(value: Boolean) {
        whenever(provider.isActivated).thenReturn(value)
        assertThat(proxy.isActivatedForApp()).isEqualTo(value)
        verify(modulesBarrier).isActivatedForApp()
    }
}
