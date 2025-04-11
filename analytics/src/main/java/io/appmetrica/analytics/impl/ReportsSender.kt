package io.appmetrica.analytics.impl

import android.os.Bundle
import androidx.annotation.VisibleForTesting
import io.appmetrica.analytics.coreutils.internal.time.SystemTimeProvider
import io.appmetrica.analytics.coreutils.internal.time.TimeProvider
import io.appmetrica.analytics.impl.client.ProcessConfiguration
import io.appmetrica.analytics.impl.service.ServiceDataReporter
import io.appmetrica.analytics.impl.service.commands.ServiceCallableFactory
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger
import java.util.concurrent.TimeUnit
import kotlin.math.max

class ReportsSender @VisibleForTesting internal constructor(
    private val serviceConnector: AppMetricaConnector,
    private val serviceCallableFactory: ServiceCallableFactory,
    private val timeProvider: TimeProvider
) : ServiceDataReporter {
    private val executor = ClientServiceLocator.getInstance().clientExecutorProvider.reportSenderExecutor

    constructor(
        appMetricaConnector: AppMetricaConnector,
        serviceCallableFactory: ServiceCallableFactory
    ) : this(
        appMetricaConnector,
        serviceCallableFactory,
        SystemTimeProvider()
    )

    fun queueReport(report: ReportToSend) {
        executor.submit(
            if (report.isCrashReport) {
                serviceCallableFactory.createCrashCallable(report)
            } else {
                serviceCallableFactory.createReportCallable(report)
            }
        )
    }

    fun sendCrash(report: ReportToSend) {
        val crashProcessingStartTime = timeProvider.uptimeMillis()
        val callable = serviceCallableFactory.createCrashCallable(report)
        if (serviceConnector.isConnected) {
            try {
                DebugLogger.info(TAG, "Execute crash callable asynchronous and wait")
                executor.submit(callable).get(CRASH_PROCESSING_TIME_LIMIT, TimeUnit.MILLISECONDS)
            } catch (e: Throwable) {
                DebugLogger.error(TAG, e)
            }
        }
        // flag will be false if it is still executing but check inside CrashCallable will prevent duplicates
        if (!callable.isExecuted) {
            try {
                DebugLogger.info(TAG, "Execute crash callable synchronous")
                callable.call()
            } catch (ex: Throwable) {
                DebugLogger.error(TAG, ex)
            }
        } else {
            DebugLogger.info(TAG, "Crash callable is already executed. Ignore it")
        }
        val processingTimeSpent = timeProvider.uptimeMillis() - crashProcessingStartTime
        val remainingTime = max(0, CRASH_PROCESSING_TIME_LIMIT - processingTimeSpent)
        waitFor(remainingTime)
    }

    private fun waitFor(timeout: Long) {
        try {
            DebugLogger.error(TAG, "Wait for $timeout ms to process all events before crash")
            Thread.sleep(timeout)
        } catch (e: Throwable) {
            DebugLogger.error(TAG, e)
        }
    }

    override fun reportData(type: Int, bundle: Bundle) {
        executor.submit(serviceCallableFactory.createTypedReportCallable(type, bundle))
    }

    fun queueResumeUserSession(processConfiguration: ProcessConfiguration) {
        executor.submit(serviceCallableFactory.createResumeUseSessionCallable(processConfiguration))
    }

    fun queuePauseUserSession(processConfiguration: ProcessConfiguration) {
        executor.submit(serviceCallableFactory.createPauseUseSessionCallable(processConfiguration))
    }

    companion object {
        private const val TAG = "[ReportsSender]"

        private val CRASH_PROCESSING_TIME_LIMIT = TimeUnit.SECONDS.toMillis(4)
    }
}
