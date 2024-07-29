package io.appmetrica.analytics.impl.crash.jvm.client

import io.appmetrica.analytics.impl.ClientServiceLocator
import io.appmetrica.analytics.impl.IUnhandledSituationReporter
import io.appmetrica.analytics.impl.MainReporterComponents
import io.appmetrica.analytics.impl.crash.utils.ThreadsStateDumper
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger

internal class LibraryAnrListener(
    private val mainReporterComponents: MainReporterComponents,
    private val mainReporterConsumer: IUnhandledSituationReporter
) : ANRMonitor.Listener {

    private val tag = "[LibraryAnrListener]"

    private val threadsStateDumper = ThreadsStateDumper()

    override fun onAppNotResponding() {
        val allThreads = threadsStateDumper.threadsDumpForAnr
        val stacktrace = allThreads.affectedThread?.stacktrace ?: emptyList()
        ClientServiceLocator.getInstance().clientExecutorProvider.defaultExecutor.execute {
            mainReporterConsumer.reportAnr(allThreads)
            if (mainReporterComponents.libraryAnrDetector.isAppmetricaAnr(stacktrace)) {
                mainReporterComponents.selfSdkCrashReporterProvider.reporter.reportAnr(allThreads)
            }
            if (mainReporterComponents.libraryAnrDetector.isPushAnr(stacktrace)) {
                mainReporterComponents.pushSdkCrashReporterProvider.reporter.reportAnr(allThreads)
            }
            DebugLogger.info(tag, "Detected ANR: $allThreads")
        }
    }
}
