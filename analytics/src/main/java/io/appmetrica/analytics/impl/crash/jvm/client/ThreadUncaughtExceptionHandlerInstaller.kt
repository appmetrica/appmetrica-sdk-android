package io.appmetrica.analytics.impl.crash.jvm.client

import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger

class ThreadUncaughtExceptionHandlerInstaller(
    private val handler: Thread.UncaughtExceptionHandler
) {

    private val tag = "[ThreadUncaughtExceptionHandlerInstaller]"

    fun install() {
        DebugLogger.info(tag, "Install")
        Thread.setDefaultUncaughtExceptionHandler(
            ThreadUncaughtExceptionHandlerComposite().apply {
                register(handler)
                register(Thread.getDefaultUncaughtExceptionHandler())
            }
        )
    }
}
