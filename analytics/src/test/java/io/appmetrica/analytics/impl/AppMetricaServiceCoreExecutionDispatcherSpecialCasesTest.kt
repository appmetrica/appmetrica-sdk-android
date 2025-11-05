package io.appmetrica.analytics.impl

import io.appmetrica.analytics.coreapi.internal.executors.ICommonExecutor
import io.appmetrica.analytics.testutils.CommonTest
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.clearInvocations
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AppMetricaServiceCoreExecutionDispatcherSpecialCasesTest : CommonTest() {

    private val executor: ICommonExecutor = mock()
    private val appMetricaServiceCore: AppMetricaServiceCore = mock()
    private val runnableCaptor = argumentCaptor<Runnable>()

    private val appMetricaCoreExecutionDispatcher: AppMetricaServiceCoreExecutionDispatcher by setUp {
        AppMetricaServiceCoreExecutionDispatcher(executor, appMetricaServiceCore)
    }

    @Test
    fun onCreateNotDestroyed() {
        appMetricaCoreExecutionDispatcher.onCreate()
        verify(executor).execute(runnableCaptor.capture())
        runnableCaptor.firstValue.run()
        verify(appMetricaServiceCore).onCreate()
        verify(appMetricaServiceCore).onCreate()
    }

    @Test
    fun onCreateDestroyed() {
        appMetricaCoreExecutionDispatcher.onDestroy()
        clearInvocations(appMetricaServiceCore)
        appMetricaCoreExecutionDispatcher.onCreate()
        verify(executor).execute(runnableCaptor.capture())
        runnableCaptor.firstValue.run()
        verify(appMetricaServiceCore).onCreate()
    }

    @Test
    fun onCreateDestroyedWhenExecuting() {
        appMetricaCoreExecutionDispatcher.onCreate()
        verify(executor).execute(runnableCaptor.capture())
        appMetricaCoreExecutionDispatcher.onDestroy()
        runnableCaptor.firstValue.run()
        verify(appMetricaServiceCore, Mockito.never()).onCreate()
    }

    @Test
    fun onDestroy() {
        appMetricaCoreExecutionDispatcher.onDestroy()
        val inOrder = Mockito.inOrder(executor, appMetricaServiceCore)
        inOrder.verify(executor).removeAll()
        inOrder.verify(appMetricaServiceCore).onDestroy()
    }
}
