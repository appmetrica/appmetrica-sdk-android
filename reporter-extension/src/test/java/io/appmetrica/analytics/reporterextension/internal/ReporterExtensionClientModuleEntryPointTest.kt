package io.appmetrica.analytics.reporterextension.internal

import io.appmetrica.analytics.coreapi.internal.executors.IHandlerExecutor
import io.appmetrica.analytics.modulesapi.internal.client.ClientContext
import io.appmetrica.analytics.modulesapi.internal.client.ModuleClientActivator
import io.appmetrica.analytics.modulesapi.internal.client.ModuleClientExecutorProvider
import io.appmetrica.analytics.modulesapi.internal.client.ProcessDetector
import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mockito.never
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.concurrent.TimeUnit

class ReporterExtensionClientModuleEntryPointTest : CommonTest() {

    private val executor = mock<IHandlerExecutor>()

    private val moduleExecutorProvider = mock<ModuleClientExecutorProvider> {
        on { defaultExecutor } doReturn executor
    }

    private val clientActivator = mock<ModuleClientActivator>()

    private val processDetector = mock<ProcessDetector>()

    private val clientContext = mock<ClientContext> {
        on { clientExecutorProvider } doReturn moduleExecutorProvider
        on { clientActivator } doReturn clientActivator
        on { processDetector } doReturn processDetector
    }

    private val runnableCaptor = argumentCaptor<Runnable>()

    private val entryPoint by setUp { ReporterExtensionClientModuleEntryPoint() }

    @Test
    fun identifier() {
        assertThat(entryPoint.identifier).isEqualTo("reporter_extension")
    }

    @Test
    fun `initClientSide from main process`() {
        whenever(processDetector.isMainProcess()).thenReturn(true)
        entryPoint.initClientSide(clientContext)
        verify(executor).executeDelayed(runnableCaptor.capture(), eq(10L), eq(TimeUnit.SECONDS))
        runnableCaptor.firstValue.run()
        verify(clientActivator).activate(clientContext.context)
    }

    @Test
    fun `initClientSide from non main process`() {
        whenever(processDetector.isMainProcess()).thenReturn(false)
        entryPoint.initClientSide(clientContext)
        verify(executor, never()).executeDelayed(any(), any(), any())
        verify(clientActivator, never()).activate(any())
    }
}
