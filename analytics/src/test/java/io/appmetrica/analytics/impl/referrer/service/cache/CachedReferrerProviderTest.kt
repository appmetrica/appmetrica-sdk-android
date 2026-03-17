package io.appmetrica.analytics.impl.referrer.service.cache

import io.appmetrica.analytics.impl.referrer.service.ReferrerListener
import io.appmetrica.analytics.impl.referrer.service.ReferrerResult
import io.appmetrica.analytics.impl.referrer.service.provider.ReferrerProvider
import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

internal class CachedReferrerProviderTest : CommonTest() {
    private val delegate: ReferrerProvider = mock()
    private val cache: ReferrerCache = mock()

    private val cachedReferrerProvider by setUp { CachedReferrerProvider(delegate, cache) }

    @Test
    fun `referrerName delegates to delegate provider`() {
        whenever(delegate.referrerName).thenReturn("test-referrer")

        assertThat(cachedReferrerProvider.referrerName).isEqualTo("test-referrer")
    }

    @Test
    fun `requestReferrer with empty cache calls delegate`() {
        whenever(cache.hasReferrer()).thenReturn(false)
        val listener: ReferrerListener = mock()

        cachedReferrerProvider.requestReferrer(listener)

        verify(delegate).requestReferrer(any())
    }

    @Test
    fun `requestReferrer with empty cache saves result to cache`() {
        whenever(cache.hasReferrer()).thenReturn(false)
        val listener: ReferrerListener = mock()
        val result: ReferrerResult = mock()

        whenever(delegate.requestReferrer(any())).doAnswer { invocation ->
            val delegateListener = invocation.getArgument<ReferrerListener>(0)
            delegateListener.onResult(result)
        }

        cachedReferrerProvider.requestReferrer(listener)

        verify(cache).saveReferrer(result)
    }

    @Test
    fun `requestReferrer with empty cache notifies listener`() {
        whenever(cache.hasReferrer()).thenReturn(false)
        val listener: ReferrerListener = mock()
        val result: ReferrerResult = mock()

        whenever(delegate.requestReferrer(any())).doAnswer { invocation ->
            val delegateListener = invocation.getArgument<ReferrerListener>(0)
            delegateListener.onResult(result)
        }

        cachedReferrerProvider.requestReferrer(listener)

        verify(listener).onResult(result)
    }

    @Test
    fun `multiple requestReferrer calls delegate only once`() {
        whenever(cache.hasReferrer()).thenReturn(false)
        val listener1: ReferrerListener = mock()
        val listener2: ReferrerListener = mock()

        cachedReferrerProvider.requestReferrer(listener1)
        cachedReferrerProvider.requestReferrer(listener2)

        verify(delegate, times(1)).requestReferrer(any())
    }

    @Test
    fun `multiple requestReferrer notify all listeners`() {
        whenever(cache.hasReferrer()).thenReturn(false)
        val listener1: ReferrerListener = mock()
        val listener2: ReferrerListener = mock()
        val result: ReferrerResult = mock()

        val delegateListenerCaptor = argumentCaptor<ReferrerListener>()

        cachedReferrerProvider.requestReferrer(listener1)
        cachedReferrerProvider.requestReferrer(listener2)

        verify(delegate, times(1)).requestReferrer(delegateListenerCaptor.capture())

        // Invoke callback after both listeners are registered
        delegateListenerCaptor.firstValue.onResult(result)

        verify(listener1).onResult(result)
        verify(listener2).onResult(result)
    }

    @Test
    fun `requestReferrer with filled cache does not call delegate`() {
        whenever(cache.hasReferrer()).thenReturn(true)
        val cachedResult: ReferrerResult = mock()
        whenever(cache.getReferrerOrNull()).thenReturn(cachedResult)
        val listener: ReferrerListener = mock()

        cachedReferrerProvider.requestReferrer(listener)

        verify(delegate, never()).requestReferrer(any())
    }

    @Test
    fun `requestReferrer with filled cache returns result from cache immediately`() {
        whenever(cache.hasReferrer()).thenReturn(true)
        val cachedResult: ReferrerResult = mock()
        whenever(cache.getReferrerOrNull()).thenReturn(cachedResult)
        val listener: ReferrerListener = mock()

        cachedReferrerProvider.requestReferrer(listener)

        verify(listener).onResult(cachedResult)
    }

    @Test
    fun `delegate throws exception creates Failure and notifies listeners`() {
        whenever(cache.hasReferrer()).thenReturn(false)
        val listener: ReferrerListener = mock()
        val exception = RuntimeException("Test exception")
        whenever(delegate.requestReferrer(any())).doThrow(exception)

        cachedReferrerProvider.requestReferrer(listener)

        val resultCaptor = argumentCaptor<ReferrerResult>()
        verify(listener).onResult(resultCaptor.capture())

        assertThat(resultCaptor.firstValue).isInstanceOf(ReferrerResult.Failure::class.java)
        val failure = resultCaptor.firstValue as ReferrerResult.Failure
        assertThat(failure.message).isEqualTo("Failed to request referrer")
        assertThat(failure.throwable).isEqualTo(exception)
    }

    @Test
    fun `cache getReferrerOrNull returns null when hasReferrer is true creates Failure`() {
        whenever(cache.hasReferrer()).thenReturn(true)
        whenever(cache.getReferrerOrNull()).thenReturn(null)
        val listener: ReferrerListener = mock()

        cachedReferrerProvider.requestReferrer(listener)

        val resultCaptor = argumentCaptor<ReferrerResult>()
        verify(listener).onResult(resultCaptor.capture())

        assertThat(resultCaptor.firstValue).isInstanceOf(ReferrerResult.Failure::class.java)
        val failure = resultCaptor.firstValue as ReferrerResult.Failure
        assertThat(failure.message).isEqualTo("Referrer is null")
    }
}
