package io.appmetrica.analytics.impl.service.commands

import android.content.Context
import androidx.annotation.VisibleForTesting
import io.appmetrica.analytics.impl.AppMetricaConnector
import io.appmetrica.analytics.impl.ClientServiceLocator
import io.appmetrica.analytics.impl.InternalEvents
import io.appmetrica.analytics.impl.ReportToSend
import io.appmetrica.analytics.impl.ServiceUtils
import io.appmetrica.analytics.impl.ShouldDisconnectFromServiceChecker
import io.appmetrica.analytics.impl.crash.CrashToFileWriter
import io.appmetrica.analytics.logger.internal.YLogger

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

    private val mainProcessDetector = ClientServiceLocator.getInstance().mainProcessDetector
    private val crashToFileWriter = CrashToFileWriter(context)

    // since this method is synchronized, if crash tries to send itself for the second time synchronously
    // while the first one is still executing in the background, it will have to wait, so crash will not be lost.
    @Synchronized
    override fun call() {
        if (isExecuted) {
            return
        }
        isExecuted = true
        if (mainProcessDetector.isNonMainProcess("AppMetrica")) {
            writeToFile(reportToSend)
        } else {
            serviceConnector.scheduleDisconnect() // Scheduled unbind, because the app is going to die
            isExecuted = false
            super.call()
        }
    }

    override fun handleAbsentService(): Boolean {
        YLogger.debug(tag, "Send crash via intent.")
        tryToSendCrashIntent(reportToSend)
        return false
    }

    @VisibleForTesting
    fun tryToSendCrashIntent(toSend: ReportToSend) {
        if (toSend.report.bytesTruncated == 0) {
            val intent = ServiceUtils.getOwnMetricaServiceIntent(context)
            toSend.report.type = InternalEvents.EVENT_TYPE_EXCEPTION_UNHANDLED_FROM_INTENT.typeId
            intent.putExtras(toSend.report.toBundle(toSend.environment.configBundle))
            try {
                context.startService(intent)
            } catch (e: Throwable) {
                writeToFile(toSend)
                YLogger.error(tag, e, e.message)
            }
        } else {
            writeToFile(toSend)
        }
    }

    private fun writeToFile(toSend: ReportToSend) {
        crashToFileWriter.writeToFile(toSend)
    }
}
