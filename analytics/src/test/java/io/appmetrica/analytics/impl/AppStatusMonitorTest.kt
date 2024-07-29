package io.appmetrica.analytics.impl

import io.appmetrica.analytics.coreapi.internal.executors.ICommonExecutor
import io.appmetrica.analytics.coreapi.internal.executors.IHandlerExecutor
import io.appmetrica.analytics.testutils.ClientServiceLocatorRule
import io.appmetrica.analytics.testutils.CommonTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import org.mockito.Mockito.any
import org.mockito.Mockito.eq
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.invocation.InvocationOnMock
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)

class AppStatusMonitorTest : CommonTest() {

    private val observer: AppStatusMonitor.Observer = mock()
    private val secondObserver: AppStatusMonitor.Observer = mock()
    private val thirdObserver: AppStatusMonitor.Observer = mock()

    private val executor: IHandlerExecutor = mock()

    private val sessionTimeout = 10L

    @get:Rule
    val clientServiceLocatorRule = ClientServiceLocatorRule()

    private val appStatusMonitor: AppStatusMonitor by setUp {
        whenever(ClientServiceLocator.getInstance().clientExecutorProvider.defaultExecutor)
            .thenReturn(executor)
        stubExecuteDelayed(sessionTimeout)
        AppStatusMonitor()
    }

    @Test
    fun resume() {
        appStatusMonitor.registerObserver(observer, sessionTimeout)
        appStatusMonitor.resume()
        verify(executor).remove(
            ArgumentMatchers.any(
                Runnable::class.java
            )
        )
        verify(observer).onResume()
    }

    @Test
    fun pause() {
        appStatusMonitor.registerObserver(observer, sessionTimeout)
        appStatusMonitor.pause()
        verifyNoMoreInteractions(observer)
    }

    @Test
    fun resumeTwice() {
        appStatusMonitor.registerObserver(observer, sessionTimeout)
        appStatusMonitor.resume()
        appStatusMonitor.resume()
        verify(executor).remove(any())
        verify(observer).onResume()
    }

    @Test
    fun pauseTwice() {
        appStatusMonitor.registerObserver(observer, sessionTimeout)
        appStatusMonitor.pause()
        appStatusMonitor.pause()
        verifyNoMoreInteractions(executor, observer)
    }

    @Test
    fun pauseAfterResume() {
        appStatusMonitor.registerObserver(observer, sessionTimeout)
        appStatusMonitor.resume()
        appStatusMonitor.pause()
        inOrder(observer, executor) {
            verify(executor).remove(
                ArgumentMatchers.any(
                    Runnable::class.java
                )
            )
            verify(observer).onResume()
            verify(executor).executeDelayed(any(), eq(sessionTimeout))
            verify(observer).onPause()
            verifyNoMoreInteractions()
        }
    }

    @Test
    fun resumeAfterPause() {
        appStatusMonitor.registerObserver(observer, sessionTimeout)
        appStatusMonitor.pause()
        appStatusMonitor.resume()
        verify(executor).remove(any())
        verify(observer).onResume()
    }

    @Test
    fun multipleObservers() {
        appStatusMonitor.registerObserver(observer, sessionTimeout)
        appStatusMonitor.registerObserver(secondObserver, sessionTimeout)
        appStatusMonitor.registerObserver(thirdObserver, sessionTimeout)
        appStatusMonitor.resume()
        verify(observer).onResume()
        verify(secondObserver).onResume()
        verify(thirdObserver).onResume()
    }

    @Test
    fun observersUnregister() {
        appStatusMonitor.registerObserver(observer, sessionTimeout)
        appStatusMonitor.resume()
        appStatusMonitor.unregisterObserver(observer)
        appStatusMonitor.pause()
        verify(observer).onResume()
        verifyNoMoreInteractions(observer)
    }

    @Test
    fun observerWithCustomTimeout() {
        val customTimeout = 4342L
        stubExecuteDelayed(customTimeout)
        appStatusMonitor.registerObserver(observer, customTimeout)
        appStatusMonitor.resume()
        appStatusMonitor.pause()
        verify(observer).onPause()
    }

    @Test
    fun observerWithStickyWhenPaused() {
        appStatusMonitor.pause()
        appStatusMonitor.registerObserver(observer, sessionTimeout,  /* sticky */true)
        verifyNoMoreInteractions(observer)
    }

    @Test
    fun observerWithStickyWhenResumed() {
        appStatusMonitor.resume()
        appStatusMonitor.registerObserver(observer,  sessionTimeout, /* sticky */true)
        verify(observer).onResume()
    }

    private fun stubExecuteDelayed(timeout: Long) {
        Mockito.doAnswer { invocation: InvocationOnMock ->
            (invocation.getArgument<Any>(0) as Runnable).run()
            null
        }.whenever(executor).executeDelayed(any(), eq(timeout))
    }
}
