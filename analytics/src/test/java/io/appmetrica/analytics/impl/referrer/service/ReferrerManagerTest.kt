package io.appmetrica.analytics.impl.referrer.service

import android.content.Context
import io.appmetrica.analytics.impl.referrer.service.listener.SafeReferrerListener
import io.appmetrica.analytics.impl.referrer.service.provider.ReferrerProvider
import io.appmetrica.analytics.impl.referrer.service.provider.ReferrerProviderFactory
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.constructionRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify

internal class ReferrerManagerTest : CommonTest() {
    private val context: Context = mock()
    private val referrerProvider: ReferrerProvider = mock()
    private val listenerCaptor = argumentCaptor<ReferrerListener>()
    private val result = mock<ReferrerResult>()
    private val externalListener: ReferrerListener = mock()

    @get:Rule
    val referrerProviderFactoryRule = constructionRule<ReferrerProviderFactory> {
        on { create(context) } doReturn referrerProvider
    }

    private val referrerManager by setUp { ReferrerManager(context) }

    @Test
    fun `constructor with Context calls factory with correct arguments`() {
        ReferrerManager(context)

        val factories = referrerProviderFactoryRule.constructionMock.constructed()
        verify(factories[0]).create(context)
    }

    @Test
    fun `getCachedReferrer returns null initially`() {
        assertThat(referrerManager.getCachedReferrer()).isNull()
    }

    @Test
    fun `getCachedReferrer returns cached result after warmUpReferrer`() {
        referrerManager.warmUpReferrer()

        verify(referrerProvider).requestReferrer(listenerCaptor.capture())

        listenerCaptor.firstValue.onResult(result)
        assertThat(referrerManager.getCachedReferrer()).isEqualTo(result)
    }

    @Test
    fun `requestReferrer forwards listener to provider`() {
        referrerManager.requestReferrer(externalListener)

        verify(referrerProvider).requestReferrer(listenerCaptor.capture())

        listenerCaptor.firstValue.onResult(result) // first listener from init

        verify(externalListener).onResult(result)
    }

    @Test
    fun `requestReferrer wraps listener in SafeReferrerListener`() {
        referrerManager.requestReferrer(externalListener)

        verify(referrerProvider).requestReferrer(listenerCaptor.capture())

        assertThat(listenerCaptor.firstValue).isInstanceOf(SafeReferrerListener::class.java)
    }

    @Test
    fun `requestReferrer does not crash if listener throws exception`() {
        val throwingListener: ReferrerListener = mock {
            on { onResult(any()) } doThrow RuntimeException("Test exception")
        }

        referrerManager.requestReferrer(throwingListener)

        verify(referrerProvider).requestReferrer(listenerCaptor.capture())

        // Should not throw
        listenerCaptor.firstValue.onResult(result)
    }

    @Test
    fun `multiple requestReferrer calls do not affect cache`() {
        referrerManager.warmUpReferrer()

        val initListenerCaptor = argumentCaptor<ReferrerListener>()
        verify(referrerProvider).requestReferrer(initListenerCaptor.capture())

        val initResult = mock<ReferrerResult>()
        initListenerCaptor.firstValue.onResult(initResult)
        assertThat(referrerManager.getCachedReferrer()).isEqualTo(initResult)

        val externalListener1: ReferrerListener = mock()
        val externalListener2: ReferrerListener = mock()

        referrerManager.requestReferrer(externalListener1)
        referrerManager.requestReferrer(externalListener2)

        val allListenerCaptor = argumentCaptor<ReferrerListener>()
        verify(referrerProvider, times(3)).requestReferrer(allListenerCaptor.capture())

        val externalResult1 = mock<ReferrerResult>()
        val externalResult2 = mock<ReferrerResult>()

        allListenerCaptor.secondValue.onResult(externalResult1)
        allListenerCaptor.thirdValue.onResult(externalResult2)

        // Cache should still be the initial result, not affected by external listeners
        assertThat(referrerManager.getCachedReferrer()).isEqualTo(initResult)
    }

    @Test
    fun `external listener receives result even if another listener throws exception`() {
        val throwingListener: ReferrerListener = mock {
            on { onResult(any()) } doThrow RuntimeException("Test exception")
        }
        val normalListener: ReferrerListener = mock()

        referrerManager.requestReferrer(throwingListener)
        referrerManager.requestReferrer(normalListener)

        verify(referrerProvider, times(2)).requestReferrer(listenerCaptor.capture())

        // Both wrapped listeners should be called, no exception should propagate
        listenerCaptor.firstValue.onResult(result)
        listenerCaptor.secondValue.onResult(result)

        verify(normalListener).onResult(result)
    }
}
