package io.appmetrica.analytics.impl.crash.jvm.client

import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger
import java.util.concurrent.CopyOnWriteArrayList

internal class CrashProcessorComposite : ICrashProcessor {

    private val tag = "[CrashProcessorComposite]"

    private val crashProcessors: MutableList<ICrashProcessor> = CopyOnWriteArrayList()

    override fun processCrash(originalException: Throwable?, allThreads: AllThreads) {
        DebugLogger.info(tag, "Notify ${crashProcessors.size} handlers with crash: $originalException")
        crashProcessors.forEach { it.processCrash(originalException, allThreads) }
    }

    fun register(vararg crashProcessors: ICrashProcessor) {
        this.crashProcessors.addAll(crashProcessors)
    }

    fun register(crashProcessors: List<ICrashProcessor>) {
        this.crashProcessors.addAll(crashProcessors)
    }

    fun clearAllCrashProcessors() {
        crashProcessors.clear()
    }
}
