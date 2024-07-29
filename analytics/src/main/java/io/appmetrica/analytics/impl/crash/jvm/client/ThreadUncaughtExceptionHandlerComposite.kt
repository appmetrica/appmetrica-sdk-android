package io.appmetrica.analytics.impl.crash.jvm.client

import java.util.concurrent.CopyOnWriteArrayList

class ThreadUncaughtExceptionHandlerComposite : Thread.UncaughtExceptionHandler {

    private val handlers = CopyOnWriteArrayList<Thread.UncaughtExceptionHandler>()

    override fun uncaughtException(t: Thread, e: Throwable) {
        handlers.forEach { it.uncaughtException(t, e) }
    }

    fun register(handler: Thread.UncaughtExceptionHandler?) {
        handler?.let { handlers.add(it) }
    }

    fun unregister(handler: Thread.UncaughtExceptionHandler?) {
        handlers.remove(handler)
    }
}
