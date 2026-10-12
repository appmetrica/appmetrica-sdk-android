package io.appmetrica.analytics.coreapi.internal.executors

import io.appmetrica.gradle.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.util.concurrent.Callable
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit

class QueuedExecutorTest : CommonTest() {

    @Test
    fun `execute posts the original runnable`() {
        val postedRunnable = Runnable { }
        val backingExecutor = RecordingExecutor()

        QueuedExecutor(backingExecutor).execute(postedRunnable)

        assertThat(backingExecutor.postedRunnables).containsExactly(postedRunnable)
    }

    private class RecordingExecutor : ICommonExecutor {
        val postedRunnables = mutableListOf<Runnable>()

        override fun execute(runnable: Runnable) = error("Unexpected execute")

        override fun post(runnable: Runnable) {
            postedRunnables += runnable
        }

        override fun <T> submit(task: Callable<T>): Future<T> = error("Unexpected submit")

        override fun postDelayed(runnable: Runnable, delay: Long) = error("Unexpected postDelayed")

        override fun postDelayed(runnable: Runnable, delay: Long, timeUnit: TimeUnit) =
            error("Unexpected postDelayed")

        override fun remove(runnable: Runnable) = error("Unexpected remove")

        override fun removeAll() = error("Unexpected removeAll")

        override fun isRunning(): Boolean = true

        override fun stopRunning() = error("Unexpected stopRunning")
    }
}
