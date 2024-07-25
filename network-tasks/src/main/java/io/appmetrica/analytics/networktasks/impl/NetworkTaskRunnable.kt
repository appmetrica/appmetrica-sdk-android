package io.appmetrica.analytics.networktasks.impl

import androidx.annotation.VisibleForTesting
import io.appmetrica.analytics.coreapi.internal.executors.InterruptionSafeThread
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger
import io.appmetrica.analytics.networktasks.internal.NetworkTask

internal class NetworkTaskRunnable @VisibleForTesting constructor(
    private val networkTask: NetworkTask,
    private val rootThreadStateSource: InterruptionSafeThread,
    private val taskPerformingStrategy: NetworkTaskPerformingStrategy
) : Runnable {

    private val tag = "[NetworkTaskRunnable]"

    class Provider {

        fun create(networkTask: NetworkTask, rootThreadStateSource: InterruptionSafeThread): NetworkTaskRunnable {
            return NetworkTaskRunnable(networkTask, rootThreadStateSource)
        }
    }

    constructor(
        networkTask: NetworkTask,
        rootThreadStateSource: InterruptionSafeThread,
    ) : this(
        networkTask,
        rootThreadStateSource,
        NetworkTaskPerformingStrategy()
    )

    override fun run() {
        var taskFinished = false
        var shouldExecuteTask: Boolean
        val exponentialBackoffPolicy = networkTask.exponentialBackoffPolicy
        val connectionBasedPolicy = networkTask.connectionExecutionPolicy
        if (rootThreadStateSource.isRunning) {
            if (
                connectionBasedPolicy.canBeExecuted() &&
                exponentialBackoffPolicy.canBeExecuted(networkTask.retryPolicyConfig)
            ) {
                var countRequest = 0
                shouldExecuteTask = networkTask.onCreateNetworkTask()
                if (!shouldExecuteTask) {
                    DebugLogger.info(tag, "Skipping task, desc: ${networkTask.description()}")
                }
                var requestSuccessful: Boolean? = null
                while (
                    rootThreadStateSource.isRunning && shouldExecuteTask &&
                    exponentialBackoffPolicy.canBeExecuted(networkTask.retryPolicyConfig)
                ) {
                    ++countRequest
                    DebugLogger.info(
                        tag,
                        "Executing task .. attempt: $countRequest, desc: ${networkTask.description()}",
                    )
                    requestSuccessful = taskPerformingStrategy.performRequest(networkTask)
                    shouldExecuteTask = !requestSuccessful && networkTask.shouldTryNextHost()
                    exponentialBackoffPolicy.onHostAttemptFinished(requestSuccessful)
                }
                if (requestSuccessful != null) {
                    exponentialBackoffPolicy.onAllHostsAttemptsFinished(requestSuccessful)
                }
                taskFinished = true
            }
        }
        if (!taskFinished) {
            handleIncompatibleNetworkType()
        }
    }

    private fun handleIncompatibleNetworkType() {
        DebugLogger.info(
            tag,
            "Task: ${networkTask.description()} didn't finished because of incompatible network type",
        )
        networkTask.onShouldNotExecute()
    }
}
