package io.appmetrica.analytics.impl.proxy

import io.appmetrica.analytics.coreapi.internal.executors.ICommonExecutor
import io.appmetrica.analytics.impl.AppMetricaFacade
import io.appmetrica.analytics.impl.MainReporter
import io.appmetrica.analytics.impl.MainReporterApiConsumerProvider
import io.appmetrica.analytics.impl.proxy.synchronous.PluginsSynchronousStageExecutor
import io.appmetrica.analytics.impl.proxy.validation.MainFacadeBarrier
import io.appmetrica.analytics.impl.proxy.validation.PluginsBarrier
import io.appmetrica.analytics.plugins.IPluginReporter
import io.appmetrica.analytics.plugins.PluginErrorDetails
import io.appmetrica.analytics.testutils.CommonTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.stubbing
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AppMetricaPluginsProxyTest : CommonTest() {

    @Mock
    private lateinit var executor: ICommonExecutor
    @Mock
    private lateinit var provider: AppMetricaFacadeProvider
    @Mock
    private lateinit var activationValidator: ActivationValidator
    @Mock
    private lateinit var barrier: MainFacadeBarrier
    @Mock
    private lateinit var pluginBarrier: PluginsBarrier
    @Mock
    private lateinit var synchronousStageExecutor: PluginsSynchronousStageExecutor
    @Mock
    private lateinit var errorDetails: PluginErrorDetails
    @Mock
    private lateinit var mainReporter: MainReporter
    @Mock
    private lateinit var pluginReporter: IPluginReporter
    @Captor
    private lateinit var runnableCaptor: ArgumentCaptor<Runnable>
    private lateinit var proxy: AppMetricaPluginsProxy

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        val facade = mock(AppMetricaFacade::class.java)
        val mainReporterApiConsumerProvider = mock(MainReporterApiConsumerProvider::class.java)
        stubbing(barrier) {
            on { pluginExtension } doReturn pluginBarrier
        }
        stubbing(mainReporter) {
            on { pluginExtension } doReturn pluginReporter
        }
        `when`(provider.peekInitializedImpl()).thenReturn(facade)
        `when`(facade.mainReporterApiConsumerProvider).thenReturn(mainReporterApiConsumerProvider)
        `when`(mainReporterApiConsumerProvider.mainReporter).thenReturn(mainReporter)
        proxy = AppMetricaPluginsProxy(executor, provider, activationValidator, barrier, synchronousStageExecutor)
    }

    @Test
    fun reportUnhandledException() {
        proxy.reportUnhandledException(errorDetails)

        val inOrder = inOrder(activationValidator, pluginBarrier, synchronousStageExecutor, executor)
        inOrder.verify(activationValidator).validate()
        inOrder.verify(pluginBarrier).reportUnhandledException(errorDetails)
        inOrder.verify(synchronousStageExecutor).reportPluginUnhandledException(errorDetails)
        inOrder.verify(executor).execute(runnableCaptor.capture())
        verifyNoInteractions(pluginReporter)

        runnableCaptor.value.run()
        verify(pluginReporter).reportUnhandledException(errorDetails)
        verifyNoMoreInteractions(pluginReporter)
    }

    @Test
    fun reportError() {
        `when`(pluginBarrier.reportErrorWithFilledStacktrace(any(), any())).thenReturn(true)

        val message = "some message"
        proxy.reportError(errorDetails, message)

        val inOrder = inOrder(activationValidator, pluginBarrier, synchronousStageExecutor, executor)
        inOrder.verify(activationValidator).validate()
        inOrder.verify(pluginBarrier).reportErrorWithFilledStacktrace(errorDetails, message)
        inOrder.verify(synchronousStageExecutor).reportPluginError(errorDetails, message)
        inOrder.verify(executor).execute(runnableCaptor.capture())
        verifyNoInteractions(pluginReporter)

        runnableCaptor.value.run()
        verify(pluginReporter).reportError(errorDetails, message)
        verifyNoMoreInteractions(pluginReporter)
    }

    @Test
    fun reportErrorValidationFailed() {
        `when`(pluginBarrier.reportErrorWithFilledStacktrace(any(), any())).thenReturn(false)

        val message = "some message"
        proxy.reportError(errorDetails, message)

        val inOrder = inOrder(activationValidator, pluginBarrier)
        inOrder.verify(activationValidator).validate()
        inOrder.verify(pluginBarrier).reportErrorWithFilledStacktrace(errorDetails, message)
        verifyNoInteractions(synchronousStageExecutor, executor, pluginReporter)
    }

    @Test
    fun reportErrorWithIdentifier() {
        val message = "some message"
        val identifier = "identifier"
        proxy.reportError(identifier, message, errorDetails)

        val inOrder = inOrder(activationValidator, pluginBarrier, synchronousStageExecutor, executor)
        inOrder.verify(activationValidator).validate()
        inOrder.verify(pluginBarrier).reportError(identifier, message, errorDetails)
        inOrder.verify(synchronousStageExecutor).reportPluginError(identifier, message, errorDetails)
        inOrder.verify(executor).execute(runnableCaptor.capture())
        verifyNoInteractions(pluginReporter)

        runnableCaptor.value.run()
        verify(pluginReporter).reportError(identifier, message, errorDetails)
        verifyNoMoreInteractions(pluginReporter)
    }
}
