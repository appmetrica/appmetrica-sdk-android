package io.appmetrica.analytics.impl

import android.os.Bundle
import android.os.Looper
import androidx.annotation.VisibleForTesting
import io.appmetrica.analytics.coreapi.internal.executors.IHandlerExecutor
import io.appmetrica.analytics.coreutils.internal.time.SystemTimeProvider
import io.appmetrica.analytics.coreutils.internal.time.TimeProvider
import io.appmetrica.analytics.impl.client.ProcessConfiguration
import io.appmetrica.analytics.impl.service.ServiceDataReporter
import io.appmetrica.analytics.impl.service.commands.ServiceCallableFactory
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.math.max

internal class ReportsSender @VisibleForTesting internal constructor(
    private val serviceConnector: AppMetricaConnector,
    private val serviceCallableFactory: ServiceCallableFactory,
    private val timeProvider: TimeProvider,
    private val queueBudget: ClientReportsQueueBudget,
) : ServiceDataReporter {

    private val executor = ClientServiceLocator.getInstance().clientExecutorProvider.reportSenderExecutor
    private val queue = ClientReportsQueue(queueBudget, executor)

    constructor(
        appMetricaConnector: AppMetricaConnector,
        serviceCallableFactory: ServiceCallableFactory,
        queueBudget: ClientReportsQueueBudget,
    ) : this(
        appMetricaConnector,
        serviceCallableFactory,
        SystemTimeProvider(),
        queueBudget,
    )

    fun queueReport(report: ReportToSend) {
        // Classify by InternalEvents typeId: CONTROL commands must not enter the DATA budget.
        // API / plugin crashes stay DATA (budgeted), not FATAL.
        val category = ClientReportsQueueCategoryClassifier.ofEventType(report.report.type)
        queue.enqueue(
            category = category,
            weight = ClientReportsQueueItemWeight.of(report),
            callable = if (report.isCrashReport) {
                serviceCallableFactory.createCrashCallable(report, fromApi = true)
            } else {
                serviceCallableFactory.createReportCallable(report)
            },
        )
    }

    fun sendCrash(report: ReportToSend) {
        val crashProcessingStartTime = timeProvider.uptimeMillis()
        val callable = serviceCallableFactory.createCrashCallable(report)
        val crashProcessingTimeLimit = crashProcessingTimeLimit()
        if (serviceConnector.isConnected) {
            if (isOnCrsThread()) {
                // Avoid latch deadlock if already on CRS thread.
                try {
                    DebugLogger.info(TAG, "FATAL sync-bypass on CRS thread")
                    callable.call()
                } catch (e: Throwable) {
                    DebugLogger.error(TAG, e)
                }
            } else {
                val latch = CountDownLatch(1)
                val item = queue.enqueue(
                    category = ClientReportsQueueCategory.FATAL,
                    weight = 0L,
                    callable = callable,
                    completionLatch = latch,
                )
                if (item == null) {
                    DebugLogger.warning(TAG, "FATAL enqueue returned null; skip latch wait")
                } else {
                    var timedOut = false
                    try {
                        DebugLogger.info(TAG, "Wait for FATAL drain latch up to ${crashProcessingTimeLimit}ms")
                        timedOut = !latch.await(crashProcessingTimeLimit, TimeUnit.MILLISECONDS)
                    } catch (e: Throwable) {
                        DebugLogger.error(TAG, e)
                        timedOut = true
                    }
                    if (timedOut) {
                        // If still not started, remove from queue so sync fallback below can run.
                        queue.tryCancelIfNotStarted(item)
                    }
                }
            }
        }
        // flag will be false if it is still executing but check inside CrashCallable will prevent duplicates
        if (!callable.isExecuted) {
            try {
                DebugLogger.info(TAG, "Execute crash callable synchronous")
                callable.call()
                val processingTimeSpent = timeProvider.uptimeMillis() - crashProcessingStartTime
                val remainingTime = max(0, crashProcessingTimeLimit - processingTimeSpent)
                DebugLogger.info(TAG, "Wait for $remainingTime ms to process all events before crash")
                waitFor(remainingTime)
            } catch (ex: Throwable) {
                DebugLogger.error(TAG, ex)
            }
        } else {
            DebugLogger.info(TAG, "Crash callable is already executed. Ignore it")
        }
    }

    private fun crashProcessingTimeLimit(): Long =
        if (isMainThread()) CRASH_PROCESSING_TIME_LIMIT_MAIN_THREAD else CRASH_PROCESSING_TIME_LIMIT_BG_THREAD

    private fun isMainThread(): Boolean {
        return Looper.myLooper() == Looper.getMainLooper()
    }

    private fun isOnCrsThread(): Boolean {
        val handlerExecutor = executor as? IHandlerExecutor ?: return false
        return Looper.myLooper() === handlerExecutor.looper
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
        val category = ClientReportsQueueCategoryClassifier.ofTypedReport(type, bundle)
        queue.enqueue(
            category = category,
            weight = ClientReportsQueueItemWeight.ofTypedReport(bundle),
            callable = serviceCallableFactory.createTypedReportCallable(type, bundle),
        )
    }

    fun queueResumeUserSession(processConfiguration: ProcessConfiguration) {
        queue.enqueue(
            category = ClientReportsQueueCategory.CONTROL,
            weight = ClientReportsQueueItemWeight.sessionCommand(),
            callable = serviceCallableFactory.createResumeUseSessionCallable(processConfiguration),
        )
    }

    fun queuePauseUserSession(processConfiguration: ProcessConfiguration) {
        queue.enqueue(
            category = ClientReportsQueueCategory.CONTROL,
            weight = ClientReportsQueueItemWeight.sessionCommand(),
            callable = serviceCallableFactory.createPauseUseSessionCallable(processConfiguration),
        )
    }

    @VisibleForTesting
    internal fun pendingSizeForTests(): Int = queue.pendingSizeForTests()

    companion object {
        private const val TAG = "[ReportsSender]"

        private val CRASH_PROCESSING_TIME_LIMIT_MAIN_THREAD = TimeUnit.SECONDS.toMillis(1)
        private val CRASH_PROCESSING_TIME_LIMIT_BG_THREAD = TimeUnit.SECONDS.toMillis(4)
    }
}
