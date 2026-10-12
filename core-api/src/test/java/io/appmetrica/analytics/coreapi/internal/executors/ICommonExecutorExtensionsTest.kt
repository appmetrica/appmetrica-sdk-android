package io.appmetrica.analytics.coreapi.internal.executors

import io.appmetrica.analytics.coreapi.internal.executors.ICommonExecutorExtensions.postDelayed
import io.appmetrica.gradle.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.verifyNoMoreInteractions
import java.util.concurrent.TimeUnit
import kotlin.time.Duration
import kotlin.time.Duration.Companion.microseconds
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes

class ICommonExecutorExtensionsTest : CommonTest() {

    private val executor: ICommonExecutor = mock()
    private val runnable: Runnable = mock()

    @Test
    fun `finite durations enqueue the original runnable with whole milliseconds`() {
        listOf(5.minutes, 1.5.milliseconds, 500.microseconds, Duration.ZERO, (-2).milliseconds).forEach { delay ->
            executor.postDelayed(runnable, delay)
        }

        val runnableCaptor = argumentCaptor<Runnable>()
        val delayCaptor = argumentCaptor<Long>()
        verify(executor, times(5)).postDelayed(
            runnableCaptor.capture(),
            delayCaptor.capture(),
            eq(TimeUnit.MILLISECONDS)
        )
        runnableCaptor.allValues.forEach { assertThat(it).isSameAs(runnable) }
        assertThat(delayCaptor.allValues).containsExactly(300_000L, 1L, 0L, 0L, -2L)
        verifyNoMoreInteractions(executor)
    }

    @Test
    fun `infinite durations are rejected before enqueueing`() {
        listOf(Duration.INFINITE, -Duration.INFINITE).forEach { delay ->
            assertThatThrownBy { executor.postDelayed(runnable, delay) }
                .isInstanceOf(IllegalArgumentException::class.java)
        }

        verifyNoInteractions(executor, runnable)
    }
}
