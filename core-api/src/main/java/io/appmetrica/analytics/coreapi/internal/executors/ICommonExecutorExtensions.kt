package io.appmetrica.analytics.coreapi.internal.executors

import java.util.concurrent.TimeUnit
import kotlin.time.Duration

object ICommonExecutorExtensions {

    /**
     * Enqueues a task with a finite delay, without running it inline, even if the delay is zero.
     * Sub-millisecond precision is truncated towards zero, as with the TimeUnit overload.
     *
     * @throws IllegalArgumentException if [delay] is infinite.
     */
    fun ICommonExecutor.postDelayed(runnable: Runnable, delay: Duration) {
        require(delay.isFinite()) { "Delay must be finite" }
        postDelayed(runnable, delay.inWholeMilliseconds, TimeUnit.MILLISECONDS)
    }
}
