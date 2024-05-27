package io.appmetrica.analytics.impl.core

import io.appmetrica.analytics.impl.GlobalServiceLocator
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger
import java.util.concurrent.TimeUnit

private const val TAG = "[MetricaCoreImplFirstCreateTaskLauncher]"

class CoreImplFirstCreateTaskLauncher(private val tasks: List<Runnable>) : Runnable {

    private val executor = GlobalServiceLocator.getInstance().serviceExecutorProvider.defaultExecutor
    private val activationBarrier = GlobalServiceLocator.getInstance().activationBarrier

    override fun run() {
        DebugLogger.info(TAG, "Run and subscriber on activation barrier")
        activationBarrier.subscribe(TimeUnit.SECONDS.toMillis(10), executor) {
            DebugLogger.info(TAG, "Run ${tasks.size} tasks")
            tasks.forEach { it.run() }
        }
    }
}
