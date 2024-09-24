package io.appmetrica.analytics.impl.crash.jvm.client

import io.appmetrica.analytics.impl.ClientServiceLocator
import io.appmetrica.analytics.impl.crash.utils.ThreadsStateDumper
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger

internal class LibraryAnrListener(
    private val anrReporter: AnrReporter
) : ANRMonitor.Listener {

    private val tag = "[LibraryAnrListener]"

    private val threadsStateDumper = ThreadsStateDumper()

    override fun onAppNotResponding() {
        val allThreads = threadsStateDumper.threadsDumpForAnr
        ClientServiceLocator.getInstance().clientExecutorProvider.defaultExecutor.execute {
            anrReporter.reportAnr(allThreads)
            DebugLogger.info(tag, "Detected ANR: $allThreads")
        }
    }
}
