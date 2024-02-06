package io.appmetrica.analytics.impl.proxy

import io.appmetrica.analytics.coreapi.internal.executors.ICommonExecutor
import io.appmetrica.analytics.impl.IReporterExtended
import io.appmetrica.analytics.impl.proxy.synchronous.PluginsReporterSynchronousStageExecutor
import io.appmetrica.analytics.impl.proxy.validation.PluginsBarrier
import io.appmetrica.analytics.plugins.IPluginReporter
import io.appmetrica.analytics.plugins.PluginErrorDetails
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.MockedConstructionRule
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.Mockito
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.stub
import org.mockito.kotlin.stubbing
import org.mockito.kotlin.verify

class PluginReporterProxyTest : CommonTest() {

    private val barrier: PluginsBarrier = mock()
    private val executor: ICommonExecutor = mock()
    private val reporter: IReporterExtended = mock()
    private val pluginReporter: IPluginReporter = mock()
    private lateinit var pluginReporterProxy: PluginReporterProxy

    private lateinit var synchronousStageExecutor: PluginsReporterSynchronousStageExecutor
    @get:Rule
    val synchronousStageExecutorRule = MockedConstructionRule(PluginsReporterSynchronousStageExecutor::class.java) { mock, _ ->
        synchronousStageExecutor = mock
    }

    @Before
    fun setUp() {
        stubbing(reporter) {
            on { pluginExtension } doReturn pluginReporter
        }
        reporter.stub { pluginReporter }
        pluginReporterProxy = PluginReporterProxy(barrier, executor) { reporter }
    }

    @Test
    fun reportPluginUnhandledException() {
        val error = Mockito.mock(PluginErrorDetails::class.java)
        pluginReporterProxy.reportUnhandledException(error)
        val inOrder = Mockito.inOrder(barrier, synchronousStageExecutor, pluginReporter)
        inOrder.verify(barrier).reportUnhandledException(error)
        inOrder.verify(synchronousStageExecutor).reportPluginUnhandledException(error)
        runOnExecutor()
        inOrder.verify(pluginReporter).reportUnhandledException(error)
        inOrder.verifyNoMoreInteractions()
    }

    @Test
    fun reportPluginError() {
        `when`(barrier.reportErrorWithFilledStacktrace(any(), any())).thenReturn(true)
        val message = "some message"
        val error = Mockito.mock(PluginErrorDetails::class.java)
        pluginReporterProxy.reportError(error, message)
        val inOrder = Mockito.inOrder(barrier, synchronousStageExecutor, pluginReporter)
        inOrder.verify(barrier).reportErrorWithFilledStacktrace(error, message)
        inOrder.verify(synchronousStageExecutor).reportPluginError(error, message)
        runOnExecutor()
        inOrder.verify(pluginReporter).reportError(error, message)
        inOrder.verifyNoMoreInteractions()
    }

    @Test
    fun reportPluginErrorValidationFailed() {
        `when`(barrier.reportErrorWithFilledStacktrace(any(), any())).thenReturn(false)
        val message = "some message"
        val error = Mockito.mock(PluginErrorDetails::class.java)
        pluginReporterProxy.reportError(error, message)
        val inOrder = Mockito.inOrder(barrier)
        inOrder.verify(barrier).reportErrorWithFilledStacktrace(error, message)
        verifyNoInteractions(synchronousStageExecutor, executor, pluginReporter)
    }

    @Test
    fun reportPluginErrorWithIdentifier() {
        val id = "some id"
        val message = "some message"
        val error = Mockito.mock(PluginErrorDetails::class.java)
        pluginReporterProxy.reportError(id, message, error)
        val inOrder = Mockito.inOrder(barrier, synchronousStageExecutor, pluginReporter)
        inOrder.verify(barrier).reportError(id, message, error)
        inOrder.verify(synchronousStageExecutor).reportPluginError(id, message, error)
        runOnExecutor()
        inOrder.verify(pluginReporter).reportError(id, message, error)
        inOrder.verifyNoMoreInteractions()
    }

    private fun runOnExecutor() {
        val captor = ArgumentCaptor.forClass(Runnable::class.java)
        verify(executor).execute(captor.capture())
        captor.value.run()
    }
}
