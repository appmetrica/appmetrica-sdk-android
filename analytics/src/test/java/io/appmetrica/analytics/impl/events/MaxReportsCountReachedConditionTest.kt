package io.appmetrica.analytics.impl.events

import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class MaxReportsCountReachedConditionTest : CommonTest() {

    private val pendingReportsCountProvider: PendingReportsCountProvider = mock()

    private val thresholdValue = 20
    private val threshold: () -> Int = mock {
        on { invoke() }.thenReturn(thresholdValue)
    }

    private val condition by setUp { MaxReportsCountReachedCondition(pendingReportsCountProvider, threshold) }

    @Test
    fun `isConditionMet for false`() {
        whenever(pendingReportsCountProvider.pendingReportsCount).thenReturn((thresholdValue - 1).toLong())
        assertThat(condition.isConditionMet).isFalse()
    }

    @Test
    fun `isConditionMet for true`() {
        whenever(pendingReportsCountProvider.pendingReportsCount).thenReturn(thresholdValue.toLong())
        assertThat(condition.isConditionMet()).isTrue()
    }
}
