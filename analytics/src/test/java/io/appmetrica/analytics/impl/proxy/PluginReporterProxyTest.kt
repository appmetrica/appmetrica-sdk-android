package io.appmetrica.analytics.impl.proxy

import io.appmetrica.analytics.coreapi.internal.executors.IHandlerExecutor
import io.appmetrica.analytics.impl.ClientServiceLocator
import io.appmetrica.analytics.impl.IReporterExtended
import io.appmetrica.analytics.impl.proxy.synchronous.PluginsReporterSynchronousStageExecutor
import io.appmetrica.analytics.impl.proxy.validation.PluginsReporterBarrier
import io.appmetrica.analytics.plugins.IPluginReporter
import io.appmetrica.analytics.plugins.PluginErrorDetails
import io.appmetrica.analytics.testutils.ClientServiceLocatorRule
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.MockedConstructionRule
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.mock
import org.mockito.kotlin.stub
import org.mockito.kotlin.stubbing
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever

internal class PluginReporterProxyTest : CommonTest() {

    private val reporter: IReporterExtended = mock()
    private val pluginReporter: IPluginReporter = mock()
    private val executor: IHandlerExecutor = mock()
    private lateinit var pluginReporterProxy: PluginReporterProxy

    private lateinit var barrier: PluginsReporterBarrier

    @get:Rule
    val barrierRule = MockedConstructionRule(PluginsReporterBarrier::class.java) { mock, _ ->
        barrier = mock
    }

    private lateinit var synchronousStageExecutor: PluginsReporterSynchronousStageExecutor

    @get:Rule
    val synchronousStageExecutorRule =
        MockedConstructionRule(PluginsReporterSynchronousStageExecutor::class.java) { mock, _ ->
            synchronousStageExecutor = mock
        }

    @get:Rule
    val clientServiceLocatorRule = ClientServiceLocatorRule()

    private val runnableCaptor = argumentCaptor<Runnable>()

    @Before
    fun setUp() {
        stubbing(reporter) {
            on { pluginExtension } doReturn pluginReporter
        }
        stubbing(ClientServiceLocator.getInstance().clientExecutorProvider) {
            on { defaultExecutor } doReturn executor
        }
        reporter.stub { pluginReporter }
        pluginReporterProxy = PluginReporterProxy { reporter }
    }

    @Test
    fun reportPluginUnhandledException() {
        val error: PluginErrorDetails = mock()
        pluginReporterProxy.reportUnhandledException(error)

        val inOrder = inOrder(barrier, synchronousStageExecutor, executor)
        inOrder.verify(barrier).reportUnhandledException(error)
        inOrder.verify(synchronousStageExecutor).reportPluginUnhandledException(error)
        inOrder.verify(executor).execute(runnableCaptor.capture())
        verifyNoInteractions(pluginReporter)

        runnableCaptor.firstValue.run()
        verify(pluginReporter).reportUnhandledException(error)
        verifyNoMoreInteractions(pluginReporter)
    }

    @Test
    fun reportPluginError() {
        whenever(barrier.reportErrorWithFilledStacktrace(any(), any())).thenReturn(true)
        val message = "some message"
        val error: PluginErrorDetails = mock()
        pluginReporterProxy.reportError(error, message)

        val inOrder = inOrder(barrier, synchronousStageExecutor, executor)
        inOrder.verify(barrier).reportErrorWithFilledStacktrace(error, message)
        inOrder.verify(synchronousStageExecutor).reportPluginError(error, message)
        inOrder.verify(executor).execute(runnableCaptor.capture())
        verifyNoInteractions(pluginReporter)

        runnableCaptor.firstValue.run()
        verify(pluginReporter).reportError(error, message)
        verifyNoMoreInteractions(pluginReporter)
    }

    @Test
    fun reportPluginErrorValidationFailed() {
        whenever(barrier.reportErrorWithFilledStacktrace(any(), any())).thenReturn(false)
        val message = "some message"
        val error: PluginErrorDetails = mock()
        pluginReporterProxy.reportError(error, message)

        val inOrder = inOrder(barrier)
        inOrder.verify(barrier).reportErrorWithFilledStacktrace(error, message)
        verifyNoInteractions(synchronousStageExecutor, executor, pluginReporter)
    }

    @Test
    fun reportPluginErrorWithIdentifier() {
        val id = "some id"
        val message = "some message"
        val error: PluginErrorDetails = mock()
        pluginReporterProxy.reportError(id, message, error)

        val inOrder = inOrder(barrier, synchronousStageExecutor, executor)
        inOrder.verify(barrier).reportError(id, message, error)
        inOrder.verify(synchronousStageExecutor).reportPluginError(id, message, error)
        inOrder.verify(executor).execute(runnableCaptor.capture())
        verifyNoInteractions(pluginReporter)

        runnableCaptor.firstValue.run()
        verify(pluginReporter).reportError(id, message, error)
        verifyNoMoreInteractions(pluginReporter)
    }
}
