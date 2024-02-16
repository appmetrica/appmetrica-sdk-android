package io.appmetrica.analytics.impl.proxy

import io.appmetrica.analytics.coreapi.internal.executors.ICommonExecutor
import io.appmetrica.analytics.impl.AppMetricaFacade
import io.appmetrica.analytics.impl.MainReporter
import io.appmetrica.analytics.impl.MainReporterApiConsumerProvider
import io.appmetrica.analytics.impl.proxy.synchronous.PluginsSynchronousStageExecutor
import io.appmetrica.analytics.impl.proxy.validation.PluginsBarrier
import io.appmetrica.analytics.plugins.IPluginReporter
import io.appmetrica.analytics.plugins.PluginErrorDetails
import io.appmetrica.analytics.testutils.CommonTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.mock
import org.mockito.kotlin.stubbing
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AppMetricaPluginsProxyTest : CommonTest() {

    private val executor: ICommonExecutor = mock()
    private val provider: AppMetricaFacadeProvider = mock()
    private val barrier: PluginsBarrier = mock()
    private val synchronousStageExecutor: PluginsSynchronousStageExecutor = mock()
    private val errorDetails: PluginErrorDetails = mock()
    private val mainReporter: MainReporter = mock()
    private val pluginReporter: IPluginReporter = mock()

    private val runnableCaptor = argumentCaptor<Runnable>()

    private lateinit var proxy: AppMetricaPluginsProxy

    @Before
    fun setUp() {
        stubbing(mainReporter) {
            on { pluginExtension } doReturn pluginReporter
        }

        val facade: AppMetricaFacade = mock()
        val mainReporterApiConsumerProvider: MainReporterApiConsumerProvider = mock()
        whenever(provider.peekInitializedImpl()).thenReturn(facade)
        whenever(facade.mainReporterApiConsumerProvider).thenReturn(mainReporterApiConsumerProvider)
        whenever(mainReporterApiConsumerProvider.mainReporter).thenReturn(mainReporter)

        proxy = AppMetricaPluginsProxy(executor, provider, barrier, synchronousStageExecutor)
    }

    @Test
    fun reportUnhandledException() {
        proxy.reportUnhandledException(errorDetails)

        val inOrder = inOrder(barrier, synchronousStageExecutor, executor)
        inOrder.verify(barrier).reportUnhandledException(errorDetails)
        inOrder.verify(synchronousStageExecutor).reportPluginUnhandledException(errorDetails)
        inOrder.verify(executor).execute(runnableCaptor.capture())
        verifyNoInteractions(pluginReporter)

        runnableCaptor.firstValue.run()
        verify(pluginReporter).reportUnhandledException(errorDetails)
        verifyNoMoreInteractions(pluginReporter)
    }

    @Test
    fun reportError() {
        whenever(barrier.reportErrorWithFilledStacktrace(any(), any())).thenReturn(true)

        val message = "some message"
        proxy.reportError(errorDetails, message)

        val inOrder = inOrder(barrier, synchronousStageExecutor, executor)
        inOrder.verify(barrier).reportErrorWithFilledStacktrace(errorDetails, message)
        inOrder.verify(synchronousStageExecutor).reportPluginError(errorDetails, message)
        inOrder.verify(executor).execute(runnableCaptor.capture())
        verifyNoInteractions(pluginReporter)

        runnableCaptor.firstValue.run()
        verify(pluginReporter).reportError(errorDetails, message)
        verifyNoMoreInteractions(pluginReporter)
    }

    @Test
    fun reportErrorValidationFailed() {
        whenever(barrier.reportErrorWithFilledStacktrace(any(), any())).thenReturn(false)

        val message = "some message"
        proxy.reportError(errorDetails, message)

        val inOrder = inOrder(barrier)
        inOrder.verify(barrier).reportErrorWithFilledStacktrace(errorDetails, message)
        verifyNoInteractions(synchronousStageExecutor, executor, pluginReporter)
    }

    @Test
    fun reportErrorWithIdentifier() {
        val message = "some message"
        val identifier = "identifier"
        proxy.reportError(identifier, message, errorDetails)

        val inOrder = inOrder(barrier, synchronousStageExecutor, executor)
        inOrder.verify(barrier).reportError(identifier, message, errorDetails)
        inOrder.verify(synchronousStageExecutor).reportPluginError(identifier, message, errorDetails)
        inOrder.verify(executor).execute(runnableCaptor.capture())
        verifyNoInteractions(pluginReporter)

        runnableCaptor.firstValue.run()
        verify(pluginReporter).reportError(identifier, message, errorDetails)
        verifyNoMoreInteractions(pluginReporter)
    }
}
