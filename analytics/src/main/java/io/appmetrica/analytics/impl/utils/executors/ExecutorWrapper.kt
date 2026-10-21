package io.appmetrica.analytics.impl.utils.executors

import android.os.Handler
import android.os.Looper
import androidx.annotation.VisibleForTesting
import io.appmetrica.analytics.coreapi.internal.executors.IHandlerExecutor
import java.util.concurrent.Callable
import java.util.concurrent.Future
import java.util.concurrent.FutureTask
import java.util.concurrent.TimeUnit

internal open class ExecutorWrapper @VisibleForTesting constructor(
    private val handlerThread: InterruptionSafeHandlerThread,
    private val looper: Looper = handlerThread.looper,
    private val handler: Handler = Handler(looper),
) : IHandlerExecutor {

    constructor(threadName: String) : this(createLaunchedHandlerThread(threadName))

    open override fun getHandler(): Handler = handler

    open override fun getLooper(): Looper = looper

    /**
     * Runs immediately on this executor's Looper; otherwise posts the original task.
     * Immediate calls can reenter the caller and bypass pending tasks. Exceptions propagate to the caller.
     * There is no trampoline for nested calls; use [post] to break the call stack.
     */
    open override fun execute(runnable: Runnable) {
        if (Looper.myLooper() === looper) {
            runnable.run()
        } else {
            post(runnable)
        }
    }

    open override fun post(runnable: Runnable) {
        handler.post(runnable)
    }

    open override fun <T> submit(callable: Callable<T>): Future<T> {
        val futureTask = FutureTask<T>(callable)
        post(futureTask)
        return futureTask
    }

    open override fun postDelayed(runnable: Runnable, delay: Long, timeUnit: TimeUnit) {
        handler.postDelayed(runnable, timeUnit.toMillis(delay))
    }

    open override fun remove(runnable: Runnable) {
        handler.removeCallbacks(runnable)
    }

    open override fun removeAll() {
        handler.removeCallbacksAndMessages(null)
    }

    open override fun isRunning(): Boolean = handlerThread.isRunning

    open override fun stopRunning() {
        handlerThread.stopRunning()
    }

    companion object {
        private fun createLaunchedHandlerThread(threadName: String): InterruptionSafeHandlerThread {
            return NamedThreadFactory(threadName).newHandlerThread().also { it.start() }
        }
    }
}
