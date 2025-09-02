package io.appmetrica.analytics.impl.proxy.synchronous

import android.content.Context
import io.appmetrica.analytics.ModuleEvent
import io.appmetrica.analytics.impl.ClientServiceLocator
import io.appmetrica.analytics.testutils.ClientServiceLocatorRule
import io.appmetrica.analytics.testutils.CommonTest
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions

class ModulesSynchronousStageExecutorTest : CommonTest() {

    private val applicationContext: Context = mock()
    private val context: Context = mock {
        on { applicationContext } doReturn applicationContext
    }

    @get:Rule
    val clientServiceLocatorRule = ClientServiceLocatorRule()

    private val synchronousStageExecutor by setUp {
        ModulesSynchronousStageExecutor()
    }

    @Test
    fun setAdvIdentifiersTracking() {
        synchronousStageExecutor.setAdvIdentifiersTracking(true)
        verify(ClientServiceLocator.getInstance()).contextAppearedListener
        verifyNoMoreInteractions(ClientServiceLocator.getInstance())
    }

    @Test
    fun reportEvent() {
        synchronousStageExecutor.reportEvent(ModuleEvent.newBuilder(12).build())
        verify(ClientServiceLocator.getInstance()).contextAppearedListener
        verifyNoMoreInteractions(ClientServiceLocator.getInstance())
    }

    @Test
    fun setSessionExtra() {
        synchronousStageExecutor.setSessionExtra("key", byteArrayOf(1, 2, 3))
        verify(ClientServiceLocator.getInstance()).contextAppearedListener
        verifyNoMoreInteractions(ClientServiceLocator.getInstance())
    }

    @Test
    fun reportExternalAttribution() {
        synchronousStageExecutor.reportExternalAttribution(1, "External attribution")
        verify(ClientServiceLocator.getInstance()).contextAppearedListener
        verifyNoMoreInteractions(ClientServiceLocator.getInstance())
    }

    @Test
    fun isActivatedForApp() {
        synchronousStageExecutor.isActivatedForApp()
        verify(ClientServiceLocator.getInstance()).contextAppearedListener
        verifyNoMoreInteractions(ClientServiceLocator.getInstance())
    }

    @Test
    fun sendEventsBuffer() {
        synchronousStageExecutor.sendEventsBuffer()
        verify(ClientServiceLocator.getInstance()).contextAppearedListener
        verifyNoMoreInteractions(ClientServiceLocator.getInstance())
    }

    @Test
    fun getReporter() {
        synchronousStageExecutor.getReporter(context, "apiKey")
        verify(ClientServiceLocator.getInstance()).contextAppearedListener
        verifyNoMoreInteractions(ClientServiceLocator.getInstance())
    }

    @Test
    fun reportAdRevenue() {
        synchronousStageExecutor.reportAdRevenue(mock(), true)
        verify(ClientServiceLocator.getInstance()).contextAppearedListener
        verifyNoMoreInteractions(ClientServiceLocator.getInstance())
    }

    @Test
    fun subscribeForAutoCollectedData() {
        synchronousStageExecutor.subscribeForAutoCollectedData(context, "apiKey")
        verify(ClientServiceLocator.getInstance()).contextAppearedListener
        verifyNoMoreInteractions(ClientServiceLocator.getInstance())
    }
}
