package io.appmetrica.analytics.coreapi.internal.executors

import java.util.concurrent.Executor

class QueuedExecutor(private val executor: ICommonExecutor) : Executor {

    override fun execute(command: Runnable) {
        executor.post(command)
    }
}
