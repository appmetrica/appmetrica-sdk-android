package io.appmetrica.analytics.impl.crash.jvm.client

import java.util.concurrent.CopyOnWriteArrayList

class CrashProcessorComposite : ICrashProcessor {

    private val crashProcessors: MutableList<ICrashProcessor> = CopyOnWriteArrayList()

    override fun processCrash(originalException: Throwable?, allThreads: AllThreads) {
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
