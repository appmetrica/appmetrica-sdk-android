package io.appmetrica.analytics.impl.referrer.service.provider

import io.appmetrica.analytics.coreapi.internal.executors.ICommonExecutor
import io.appmetrica.analytics.impl.referrer.service.ReferrerListener
import io.appmetrica.analytics.impl.referrer.service.ReferrerResult
import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import kotlin.concurrent.thread

internal class ExecutorReferrerProviderTest : CommonTest() {
    private val delegate: ReferrerProvider = mock()
    private val executor: ICommonExecutor = mock()

    private val executorReferrerProvider by setUp { ExecutorReferrerProvider(delegate, executor) }

    @Test
    fun `referrerName delegates to delegate provider`() {
        whenever(delegate.referrerName).thenReturn("test-referrer")

        assertThat(executorReferrerProvider.referrerName).isEqualTo("test-referrer")
    }

    @Test
    fun `requestReferrer executes through executor`() {
        val listener: ReferrerListener = mock()

        executorReferrerProvider.requestReferrer(listener)

        val runnableCaptor = argumentCaptor<Runnable>()
        verify(executor).execute(runnableCaptor.capture())
        verify(delegate, never()).requestReferrer(listener)

        runnableCaptor.firstValue.run()

        verify(delegate).requestReferrer(any())
    }

    @Test
    fun `callback in same thread calls listener directly`() {
        val listener: ReferrerListener = mock()
        val result: ReferrerResult = mock()

        executorReferrerProvider.requestReferrer(listener)

        val executorRunnableCaptor = argumentCaptor<Runnable>()
        verify(executor).execute(executorRunnableCaptor.capture())
        // Run in the same thread as executor
        executorRunnableCaptor.firstValue.run()

        val delegateListenerCaptor = argumentCaptor<ReferrerListener>()
        verify(delegate).requestReferrer(delegateListenerCaptor.capture())
        // Callback arrives on the same thread as executor
        delegateListenerCaptor.firstValue.onResult(result)

        // Listener should be called directly without an additional execute
        verify(listener).onResult(result)
    }

    @Test
    fun `callback in different thread calls listener through executor`() {
        val listener: ReferrerListener = mock()
        val result: ReferrerResult = mock()

        executorReferrerProvider.requestReferrer(listener)

        val executorRunnableCaptor = argumentCaptor<Runnable>()
        verify(executor).execute(executorRunnableCaptor.capture())
        executorRunnableCaptor.firstValue.run()

        val delegateListenerCaptor = argumentCaptor<ReferrerListener>()
        verify(delegate).requestReferrer(delegateListenerCaptor.capture())
        verify(listener, never()).onResult(result)

        // Simulate callback arriving on a different thread
        thread {
            delegateListenerCaptor.firstValue.onResult(result)
        }.join()

        val executorRunnableCaptor2 = argumentCaptor<Runnable>()
        verify(executor, times(2)).execute(executorRunnableCaptor2.capture())
        executorRunnableCaptor2.secondValue.run()

        // Listener should be called through executor (second execute)
        verify(listener).onResult(result)
    }
}
