package io.appmetrica.analytics.impl.referrer.service.listener

import io.appmetrica.analytics.impl.referrer.service.ReferrerListener
import io.appmetrica.analytics.impl.referrer.service.ReferrerResult
import io.appmetrica.analytics.testutils.CommonTest
import org.junit.Test
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

internal class SafeReferrerListenerTest : CommonTest() {
    private val delegate: ReferrerListener = mock()

    private val safeReferrerListener by setUp { SafeReferrerListener(delegate) }

    @Test
    fun `onResult calls delegate onResult`() {
        val result: ReferrerResult = mock()

        safeReferrerListener.onResult(result)

        verify(delegate).onResult(result)
    }

    @Test
    fun `onResult catches exception from delegate and does not rethrow`() {
        val result: ReferrerResult = mock()
        whenever(delegate.onResult(result)).doThrow(RuntimeException("Test exception"))

        // Should not throw exception
        safeReferrerListener.onResult(result)
    }
}
