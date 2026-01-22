package io.appmetrica.analytics.impl

import io.appmetrica.analytics.impl.component.IComponent
import io.appmetrica.analytics.impl.events.EventsFlusher
import io.appmetrica.analytics.impl.startup.executor.StartupExecutor
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger
import io.appmetrica.analytics.networktasks.internal.NetworkTask
import java.util.concurrent.atomic.AtomicBoolean

internal open class TaskProcessor<C : IComponent>(
    val component: C,
    private val mStartupExecutor: StartupExecutor
) : ServiceLifecycleObserver, EventsFlusher {
    private val shuttingDown = AtomicBoolean(false)

    private val tag = "[TaskProcessor-${component.getComponentId()}]"

    override fun onCreate() {
        if (shuttingDown.compareAndSet(true, false)) {
            DebugLogger.info(tag, "start")
        }
    }

    override fun onDestroy() {
        if (shuttingDown.compareAndSet(false, true)) {
            cancelFlushTask()
            DebugLogger.info(tag, "stop")
        }
    }

    open fun cancelFlushTask() {
    }

    override fun flushAllTaskAsync() {
        if (shuttingDown.get().not()) {
            DebugLogger.info(tag, "scheduleFlushTask")
            scheduleFlushTaskNow()
        }
    }

    open fun scheduleFlushTaskNow() {
    }

    override fun flushAllTasks() {
        DebugLogger.info(tag, "flushAllTasks")
        if (shuttingDown.get().not()) {
            runTasks()
            cancelFlushTask()
        }
    }

    fun startTask(networkTask: NetworkTask) {
        GlobalServiceLocator.getInstance().networkCore.startTask(networkTask)
    }

    open fun runTasks() {
        DebugLogger.info(tag, "sendStartupIfRequired")
        mStartupExecutor.sendStartupIfRequired()
    }

    fun isShuttingDown(): Boolean {
        return shuttingDown.get()
    }
}
