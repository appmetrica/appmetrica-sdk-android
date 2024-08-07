package io.appmetrica.analytics.impl.service.commands

import android.os.RemoteException
import io.appmetrica.analytics.impl.AppMetricaConnector
import io.appmetrica.analytics.impl.ShouldDisconnectFromServiceChecker
import io.appmetrica.analytics.impl.crash.jvm.client.AppMetricaUncaughtExceptionHandler
import io.appmetrica.analytics.internal.IAppMetricaService
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger
import java.util.concurrent.Callable

abstract class ReportToServiceCallable(
    protected val serviceConnector: AppMetricaConnector,
    private val shouldDisconnectFromServiceChecker: ShouldDisconnectFromServiceChecker?
) : Callable<Unit> {

    private val tag = "[ReportToServiceCallable]"

    var isExecuted = false
        protected set

    override fun call() {
        try {
            if (isExecuted) {
                return
            }
            isExecuted = true
            var retry: Boolean
            var triesCount = 0
            do {
                val service = serviceConnector.service
                if (service != null) {
                    try {
                        reportToService(service)
                        if (shouldDisconnect()) {
                            DebugLogger.info(tag, "schedule disconnect")
                            serviceConnector.scheduleDisconnect()
                        }
                        return
                    } catch (e: RemoteException) {
                        DebugLogger.error(
                            tag,
                            e,
                            "[${javaClass.name}]Exception during sending data to service:\\n${e.message}"
                        )
                    }
                }
                DebugLogger.info(
                    tag,
                    "[${javaClass.name}]There is no connected service. Try number $triesCount"
                )
                retry = handleAbsentService()
                triesCount++
            } while (
                retry && !AppMetricaUncaughtExceptionHandler.isProcessDying() && triesCount < CONNECTION_TRIES_COUNT
            )
        } catch (throwable: Throwable) {
            DebugLogger.error(tag, throwable)
            onReportToServiceError(throwable)
        }
        return
    }

    open fun onReportToServiceError(throwable: Throwable?) {
        DebugLogger.error(
            tag,
            throwable,
            "[${javaClass.name}] Exception during reporting to service."
        )
    }

    abstract fun reportToService(service: IAppMetricaService)

    open fun handleAbsentService(): Boolean {
        serviceConnector.bindService()
        serviceConnector.waitForConnect(CONNECTION_WAIT_TIMEOUT_MILLIS)
        return true
    }

    private fun shouldDisconnect(): Boolean {
        return shouldDisconnectFromServiceChecker == null || shouldDisconnectFromServiceChecker.shouldDisconnect()
    }

    companion object {
        private const val CONNECTION_TRIES_COUNT = 3
        private const val CONNECTION_WAIT_TIMEOUT_MILLIS = 5000L
    }
}
