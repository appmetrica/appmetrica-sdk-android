package io.appmetrica.analytics.impl.crash.jvm.client

import androidx.annotation.VisibleForTesting
import io.appmetrica.analytics.coreutils.internal.logger.LoggerStorage
import io.appmetrica.analytics.impl.ClientServiceLocator
import io.appmetrica.analytics.impl.crash.utils.CrashedThreadConverter
import io.appmetrica.analytics.impl.crash.utils.ThreadsStateDumper
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger
import java.util.concurrent.atomic.AtomicBoolean

class AppMetricaUncaughtExceptionHandler(
    private val crashProcessor: ICrashProcessor
) : Thread.UncaughtExceptionHandler {

    private val tag = "[AppMetricaUncaughtExceptionHandler]"

    private val processNameProvider = ClientServiceLocator.getInstance().processNameProvider
    private val crashedThreadConverter = CrashedThreadConverter()
    private val threadsStateDumper = ThreadsStateDumper()

    override fun uncaughtException(thread: Thread, ex: Throwable) {
        try {
            DebugLogger.info(tag, "Process is dying")
            processDying.set(true)
            crashProcessor.processCrash(
                ex,
                AllThreads(
                    crashedThreadConverter.apply(thread),
                    threadsStateDumper.getThreadsDumpForCrash(thread),
                    processNameProvider.getProcessName()
                )
            )
        } catch (e: Throwable) {
            LoggerStorage.getMainPublicOrAnonymousLogger().error(e, e.message)
        }
    }

    companion object {

        private val processDying = AtomicBoolean()

        @JvmStatic
        fun isProcessDying(): Boolean {
            return processDying.get()
        }

        @VisibleForTesting
        fun reset() {
            processDying.set(false)
        }

        @VisibleForTesting
        fun set() {
            processDying.set(true)
        }
    }
}
