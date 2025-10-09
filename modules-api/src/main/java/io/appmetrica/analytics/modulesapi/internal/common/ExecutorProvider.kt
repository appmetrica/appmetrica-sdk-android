package io.appmetrica.analytics.modulesapi.internal.common

import io.appmetrica.analytics.coreapi.internal.executors.IHandlerExecutor
import io.appmetrica.analytics.coreapi.internal.executors.InterruptionSafeThread
import java.util.concurrent.Executor

/**
 * Declares the API for getting access to different executors and threads.
 */
interface ExecutorProvider {
    /**
     * Returns default shared executor for simple short-term background tasks.
     * For I/O operations use executor from [getSupportIOExecutor].
     * @return The single [IHandlerExecutor] instance.
     * @see getSupportIOExecutor
     * @see getThread
     */
    fun getDefaultExecutor(): IHandlerExecutor

    /**
     * Returns shared I/O executor for different I/O operations like network and file system requests.
     * For simple short-term background operations use [getDefaultExecutor].
     * @return The single [IHandlerExecutor] instance.
     * @see getDefaultExecutor
     * @see getThread
     */
    fun getSupportIOExecutor(): IHandlerExecutor

    val moduleExecutor: IHandlerExecutor

    /**
     * Returns executor for main thread. Use it only for tasks that need to be performed on the main thread.
     * For other tasks use
     * @return The single [Executor] instance.
     * @see getDefaultExecutor
     * @see getSupportIOExecutor
     * @see getThread
     */
    fun getUiExecutor(): Executor

    /**
     * Returns new thread with [threadName] postfix. To perform background tasks and I/O tasks,
     * use [getDefaultExecutor] and [getSupportIOExecutor].
     * @param threadNamePostfix The postfix to be used in thread name.
     * @return a new [Thread] instance with [threadNamePostfix] postfix.
     * @see getDefaultExecutor
     * @see getSupportIOExecutor
     */
    fun getInterruptionThread(
        moduleIdentifier: String,
        threadNamePostfix: String,
        runnable: Runnable
    ): InterruptionSafeThread

    fun getReportRunnableExecutor(): Executor
}
