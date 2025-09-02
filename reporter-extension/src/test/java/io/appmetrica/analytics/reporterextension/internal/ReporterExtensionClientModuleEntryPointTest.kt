package io.appmetrica.analytics.reporterextension.internal

import io.appmetrica.analytics.modulesapi.internal.client.ClientContext
import io.appmetrica.analytics.modulesapi.internal.client.ModuleClientActivator
import io.appmetrica.analytics.modulesapi.internal.client.ProcessDetector
import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.Mockito.never
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class ReporterExtensionClientModuleEntryPointTest : CommonTest() {

    private val clientActivator = mock<ModuleClientActivator>()

    private val processDetector = mock<ProcessDetector>()

    private val clientContext = mock<ClientContext> {
        on { clientActivator } doReturn clientActivator
        on { processDetector } doReturn processDetector
    }

    private val entryPoint by setUp { ReporterExtensionClientModuleEntryPoint() }

    @Test
    fun identifier() {
        assertThat(entryPoint.identifier).isEqualTo("reporter_extension")
    }

    @Test
    fun `initClientSide from main process`() {
        whenever(processDetector.isMainProcess()).thenReturn(true)
        entryPoint.initClientSide(clientContext)
        verify(clientActivator).activate(clientContext.context)
    }

    @Test
    fun `initClientSide from non main process`() {
        whenever(processDetector.isMainProcess()).thenReturn(false)
        entryPoint.initClientSide(clientContext)
        verify(clientActivator, never()).activate(any())
    }
}
