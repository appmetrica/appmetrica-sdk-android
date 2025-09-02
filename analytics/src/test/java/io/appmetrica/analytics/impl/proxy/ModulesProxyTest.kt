package io.appmetrica.analytics.impl.proxy

import android.content.Context
import io.appmetrica.analytics.AdRevenue
import io.appmetrica.analytics.AppMetrica
import io.appmetrica.analytics.ModuleEvent
import io.appmetrica.analytics.coreapi.internal.executors.IHandlerExecutor
import io.appmetrica.analytics.impl.AppMetricaFacade
import io.appmetrica.analytics.impl.ClientServiceLocator
import io.appmetrica.analytics.impl.MainReporter
import io.appmetrica.analytics.impl.MainReporterApiConsumerProvider
import io.appmetrica.analytics.impl.attribution.ExternalAttributionFromModule
import io.appmetrica.analytics.impl.proxy.synchronous.ModulesSynchronousStageExecutor
import io.appmetrica.analytics.impl.proxy.validation.ModulesBarrier
import io.appmetrica.analytics.testutils.ClientServiceLocatorRule
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.MockedStaticRule
import io.appmetrica.analytics.testutils.constructionRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.KArgumentCaptor
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import java.util.UUID

@RunWith(RobolectricTestRunner::class)
class ModulesProxyTest : CommonTest() {

    private val mainReporter: MainReporter = mock()
    private val mainReporterApiConsumerProvider: MainReporterApiConsumerProvider = mock()
    private val facade: AppMetricaFacade = mock()
    private val reporterProxyStorageImpl: ReporterProxyStorage = mock()
    private val executor: IHandlerExecutor = mock()

    private val runnableArgumentCaptor: KArgumentCaptor<Runnable> = argumentCaptor()

    @get:Rule
    val reporterProxyStorage = MockedStaticRule(ReporterProxyStorage::class.java)

    @get:Rule
    val appMetrica = MockedStaticRule(AppMetrica::class.java)

    @get:Rule
    val externalAttributionFromModuleMockedConstructionRule = constructionRule<ExternalAttributionFromModule>()

    @get:Rule
    val clientServiceLocatorRule = ClientServiceLocatorRule()

    @get:Rule
    val modulesBarrierConstructionRule = constructionRule<ModulesBarrier>()
    private val modulesBarrier: ModulesBarrier by modulesBarrierConstructionRule

    @get:Rule
    val synchronousStageExecutorConstructionRule = constructionRule<ModulesSynchronousStageExecutor>()
    private val synchronousStageExecutor: ModulesSynchronousStageExecutor by synchronousStageExecutorConstructionRule

    private lateinit var proxy: ModulesProxy

    @Before
    fun setUp() {
        whenever(ClientServiceLocator.getInstance().appMetricaFacadeProvider.peekInitializedImpl()).thenReturn(facade)
        whenever(facade.mainReporterApiConsumerProvider).thenReturn(mainReporterApiConsumerProvider)
        whenever(mainReporterApiConsumerProvider.mainReporter).thenReturn(mainReporter)
        whenever(ClientServiceLocator.getInstance().clientExecutorProvider.defaultExecutor).thenReturn(executor)

        proxy = ModulesProxy()
    }

    @Test
    fun modulesBarrier() {
        assertThat(modulesBarrierConstructionRule.constructionMock.constructed()).hasSize(1)
        assertThat(modulesBarrierConstructionRule.argumentInterceptor.flatArguments())
            .containsExactly(ClientServiceLocator.getInstance().appMetricaFacadeProvider)
    }

    @Test
    fun synchronousStageExecutor() {
        assertThat(synchronousStageExecutorConstructionRule.constructionMock.constructed()).hasSize(1)
        assertThat(synchronousStageExecutorConstructionRule.argumentInterceptor.flatArguments()).isEmpty()
    }

    @Test
    fun setAdvIdentifiersTracking() {
        proxy.setAdvIdentifiersTracking(true)

        val inOrder = inOrder(modulesBarrier, synchronousStageExecutor, executor)
        inOrder.verify(modulesBarrier).setAdvIdentifiersTracking(true)
        inOrder.verify(synchronousStageExecutor).setAdvIdentifiersTracking(true)
        inOrder.verify(executor, times(1)).execute(runnableArgumentCaptor.capture())
        verifyNoMoreInteractions(mainReporter)

        runnableArgumentCaptor.firstValue.run()

        verify(mainReporter).setAdvIdentifiersTracking(true)
        verifyNoMoreInteractions(mainReporter)
    }

    @Test
    fun reportEvent() {
        val moduleEvent: ModuleEvent = mock()
        proxy.reportEvent(moduleEvent)

        val inOrder = inOrder(modulesBarrier, synchronousStageExecutor, executor)
        inOrder.verify(modulesBarrier).reportEvent(moduleEvent)
        inOrder.verify(synchronousStageExecutor).reportEvent(moduleEvent)
        inOrder.verify(executor, times(1)).execute(runnableArgumentCaptor.capture())
        verifyNoMoreInteractions(mainReporter)

        runnableArgumentCaptor.firstValue.run()

        verify(mainReporter).reportEvent(moduleEvent)
        verifyNoMoreInteractions(mainReporter)
    }

    @Test
    fun setSessionExtra() {
        val key = "Key"
        val value = ByteArray(5) { it.toByte() }

        proxy.setSessionExtra(key, value)

        inOrder(modulesBarrier, synchronousStageExecutor, executor, mainReporter) {
            verify(modulesBarrier).setSessionExtra(key, value)
            verify(synchronousStageExecutor).setSessionExtra(key, value)
            verify(executor).execute(runnableArgumentCaptor.capture())
            verifyNoMoreInteractions(mainReporter)
        }

        runnableArgumentCaptor.firstValue.run()

        verify(mainReporter).setSessionExtra(key, value)
        verifyNoMoreInteractions(mainReporter)
    }

    @Test
    fun reportExternalAttribution() {
        val source = 14
        val value = "Value"

        proxy.reportExternalAttribution(source, value)

        inOrder(modulesBarrier, synchronousStageExecutor, executor, mainReporter) {
            verify(modulesBarrier).reportExternalAttribution(source, value)
            verify(synchronousStageExecutor).reportExternalAttribution(source, value)
            verify(executor).execute(runnableArgumentCaptor.capture())
            verifyNoInteractions(mainReporter)
        }

        runnableArgumentCaptor.firstValue.run()
        verify(mainReporter).reportExternalAttribution(
            externalAttributionFromModuleMockedConstructionRule.constructionMock.constructed().first()
        )
        verifyNoMoreInteractions(mainReporter)

        assertThat(externalAttributionFromModuleMockedConstructionRule.constructionMock.constructed()).hasSize(1)
        assertThat(externalAttributionFromModuleMockedConstructionRule.argumentInterceptor.flatArguments())
            .containsExactly(source, value)
    }

    @Test
    fun sendEventsBuffer() {
        proxy.sendEventsBuffer()
        appMetrica.staticMock.verify { AppMetrica.sendEventsBuffer() }
        appMetrica.staticMock.verifyNoMoreInteractions()
        verify(modulesBarrier).sendEventsBuffer()
        verify(synchronousStageExecutor).sendEventsBuffer()
    }

    @Test
    fun getReporter() {
        val context: Context = mock {
            on { applicationContext } doReturn it
        }
        val apiKey = UUID.randomUUID().toString()
        whenever(reporterProxyStorageImpl.getOrCreate(context, apiKey)).thenReturn(mock())
        whenever(ReporterProxyStorage.getInstance()).thenReturn(reporterProxyStorageImpl)
        proxy.getReporter(context, apiKey)
        verify(modulesBarrier).getReporter(context, apiKey)
        verify(synchronousStageExecutor).getReporter(context, apiKey)
        verify(reporterProxyStorageImpl).getOrCreate(context, apiKey)
    }

    @Test
    fun isActivatedForTrue() {
        verifyIsActivated(true)
    }

    @Test
    fun isActivatedForFalse() {
        verifyIsActivated(false)
    }

    @Test
    fun reportAdRevenue() {
        val adRevenue: AdRevenue = mock()
        proxy.reportAdRevenue(adRevenue, true)

        val inOrder = inOrder(modulesBarrier, synchronousStageExecutor, executor)
        inOrder.verify(modulesBarrier).reportAdRevenue(adRevenue, true)
        inOrder.verify(synchronousStageExecutor).reportAdRevenue(adRevenue, true)
        inOrder.verify(executor, times(1)).execute(runnableArgumentCaptor.capture())
        verifyNoMoreInteractions(mainReporter)

        runnableArgumentCaptor.firstValue.run()

        verify(mainReporter).reportAdRevenue(adRevenue, true)
        verifyNoMoreInteractions(mainReporter)
    }

    @Test
    fun subscribeForAutoCollectedData() {
        val context: Context = mock()
        val apiKey = UUID.randomUUID().toString()

        proxy.subscribeForAutoCollectedData(context, apiKey)

        inOrder(
            modulesBarrier,
            synchronousStageExecutor,
            executor,
            ClientServiceLocator.getInstance().appMetricaFacadeProvider
        ) {
            verify(modulesBarrier).subscribeForAutoCollectedData(context, apiKey)
            verify(synchronousStageExecutor).subscribeForAutoCollectedData(context, apiKey)
            verify(executor).execute(runnableArgumentCaptor.capture())
            verifyNoInteractions(ClientServiceLocator.getInstance().appMetricaFacadeProvider)
            runnableArgumentCaptor.firstValue.run()
            verify(ClientServiceLocator.getInstance().appMetricaFacadeProvider)
                .addAutoCollectedDataSubscriber(apiKey)
        }
    }

    private fun verifyIsActivated(value: Boolean) {
        whenever(ClientServiceLocator.getInstance().appMetricaFacadeProvider.isActivated).thenReturn(value)
        assertThat(proxy.isActivatedForApp()).isEqualTo(value)
        verify(modulesBarrier).isActivatedForApp()
        verify(synchronousStageExecutor).isActivatedForApp()
    }
}
