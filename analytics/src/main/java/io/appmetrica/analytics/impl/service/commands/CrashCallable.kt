package io.appmetrica.analytics.impl.service.commands

import android.content.Context
import io.appmetrica.analytics.impl.AppMetricaConnector
import io.appmetrica.analytics.impl.ClientServiceLocator
import io.appmetrica.analytics.impl.ReportToSend
import io.appmetrica.analytics.impl.ShouldDisconnectFromServiceChecker
import io.appmetrica.analytics.impl.crash.CrashToFileWriter
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger

internal class CrashCallable(
    private val context: Context,
    serviceConnector: AppMetricaConnector,
    shouldDisconnectFromServiceChecker: ShouldDisconnectFromServiceChecker?,
    private val reportToSend: ReportToSend
) : ReportCallable(
    serviceConnector,
    shouldDisconnectFromServiceChecker,
    reportToSend
) {

    private val tag = "[CrashCallable]"

    private val currentProcessDetector = ClientServiceLocator.getInstance().currentProcessDetector
    private val appMetricaServiceProcessDetector = ClientServiceLocator.getInstance().appMetricaServiceProcessDetector
    private val crashToFileWriter = CrashToFileWriter(context)

    // since this method is synchronized, if crash tries to send itself for the second time synchronously
    // while the first one is still executing in the background, it will have to wait, so crash will not be lost.
    @Synchronized
    override fun call() {
        if (isExecuted) {
            DebugLogger.info(tag, "Ignore callable as it has already been executed")
            return
        }
        isExecuted = true
        if (shouldProcessCrashViaFile()) {
            crashToFileWriter.writeToFile(reportToSend)
        } else {
            serviceConnector.scheduleDisconnect() // Scheduled unbind, because the app is going to die
            isExecuted = false
            super.call()
        }
    }

    private fun shouldProcessCrashViaFile(): Boolean {
        return currentProcessDetector.getProcessName() == appMetricaServiceProcessDetector.processName(context)
    }

    override fun handleAbsentService(): Boolean {
        DebugLogger.info(tag, "Handle absence service: write crash to file")
        crashToFileWriter.writeToFile(reportToSend)
        return false
    }
}
