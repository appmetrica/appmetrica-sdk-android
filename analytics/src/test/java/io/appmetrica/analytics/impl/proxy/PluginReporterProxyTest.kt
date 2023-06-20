package io.appmetrica.analytics.impl.proxy

import io.appmetrica.analytics.coreapi.internal.executors.ICommonExecutor
import io.appmetrica.analytics.impl.IReporterExtended
import io.appmetrica.analytics.impl.SynchronousStageExecutor
import io.appmetrica.analytics.impl.proxy.validation.PluginsBarrier
import io.appmetrica.analytics.plugins.IPluginReporter
import io.appmetrica.analytics.plugins.PluginErrorDetails
import io.appmetrica.analytics.testutils.CommonTest
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.stub
import org.mockito.kotlin.stubbing
import org.mockito.kotlin.verify

class PluginReporterProxyTest : CommonTest() {

    @Mock
    private lateinit var barrier: PluginsBarrier
    @Mock
    private lateinit var synchronousStageExecutor: SynchronousStageExecutor
    @Mock
    private lateinit var executor: ICommonExecutor
    @Mock
    private lateinit var reporter: IReporterExtended
    @Mock
    private lateinit var pluginReporter: IPluginReporter
    private lateinit var pluginReporterProxy: PluginReporterProxy

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        stubbing(reporter) {
            on { pluginExtension } doReturn pluginReporter
        }
        reporter.stub { pluginReporter }
        pluginReporterProxy = PluginReporterProxy(barrier, synchronousStageExecutor, executor) { reporter }
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
