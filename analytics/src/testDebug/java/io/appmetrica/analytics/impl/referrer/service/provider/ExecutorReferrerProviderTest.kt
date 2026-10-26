package io.appmetrica.analytics.impl.referrer.service.provider

import io.appmetrica.analytics.coreapi.internal.executors.ICommonExecutor
import io.appmetrica.analytics.impl.referrer.service.ReferrerListener
import io.appmetrica.analytics.impl.referrer.service.ReferrerResult
import io.appmetrica.gradle.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever

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
        verify(executor).post(runnableCaptor.capture())
        verify(delegate, never()).requestReferrer(any())

        runnableCaptor.firstValue.run()

        verify(delegate).requestReferrer(any())
    }

    @Test
    fun `callback delivers result through execute`() {
        val listener: ReferrerListener = mock()
        val result: ReferrerResult = mock()

        executorReferrerProvider.requestReferrer(listener)

        val executorRunnableCaptor = argumentCaptor<Runnable>()
        verify(executor).post(executorRunnableCaptor.capture())
        executorRunnableCaptor.firstValue.run()

        val delegateListenerCaptor = argumentCaptor<ReferrerListener>()
        verify(delegate).requestReferrer(delegateListenerCaptor.capture())
        verifyNoInteractions(listener)

        delegateListenerCaptor.firstValue.onResult(result)

        val callbackRunnableCaptor = argumentCaptor<Runnable>()
        verify(executor).execute(callbackRunnableCaptor.capture())
        verify(executor).post(any())
        verifyNoInteractions(listener)
        callbackRunnableCaptor.firstValue.run()

        verify(listener).onResult(result)
    }

    @Test
    fun `listener and delegate exceptions propagate without wrapping`() {
        val listener: ReferrerListener = mock()
        val result = ReferrerResult.Failure("failure", IllegalArgumentException("referrer error"))
        val listenerFailure = IllegalStateException("listener error")
        doThrow(listenerFailure).whenever(listener).onResult(result)
        doAnswer {
            it.getArgument<ReferrerListener>(0).onResult(result)
            null
        }.whenever(delegate).requestReferrer(any())

        executorReferrerProvider.requestReferrer(listener)
        val requestCaptor = argumentCaptor<Runnable>()
        verify(executor).post(requestCaptor.capture())
        requestCaptor.firstValue.run()
        val callbackCaptor = argumentCaptor<Runnable>()
        verify(executor).execute(callbackCaptor.capture())

        assertThatThrownBy { callbackCaptor.firstValue.run() }.isSameAs(listenerFailure)

        val delegateFailure = IllegalArgumentException("delegate error")
        doThrow(delegateFailure).whenever(delegate).requestReferrer(any())

        assertThatThrownBy { requestCaptor.firstValue.run() }.isSameAs(delegateFailure)
    }
}
