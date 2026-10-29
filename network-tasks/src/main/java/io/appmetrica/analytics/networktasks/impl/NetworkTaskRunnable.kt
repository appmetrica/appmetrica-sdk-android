package io.appmetrica.analytics.networktasks.impl

import androidx.annotation.VisibleForTesting
import io.appmetrica.analytics.coreapi.internal.executors.InterruptionSafeThread
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger
import io.appmetrica.analytics.networktasks.internal.ExponentialBackoffPolicy
import io.appmetrica.analytics.networktasks.internal.NetworkTask
import io.appmetrica.analytics.networktasks.internal.attempt.AttemptOutcome
import io.appmetrica.analytics.networktasks.internal.attempt.RetryState

internal class NetworkTaskRunnable @VisibleForTesting constructor(
    private val networkTask: NetworkTask,
    private val rootThreadStateSource: InterruptionSafeThread,
    private val taskPerformingStrategy: NetworkTaskPerformingStrategy
) : Runnable {

    private val tag = "[NetworkTaskRunnable]"

    internal class Provider {

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
        val exponentialBackoffPolicy = networkTask.exponentialBackoffPolicy
        if (!canStartExecution(exponentialBackoffPolicy)) {
            handleIncompatibleNetworkType()
            return
        }

        var lastAttemptOutcome = createInitialOutcome()
        var attemptNumber = 0
        while (
            rootThreadStateSource.isRunning &&
            exponentialBackoffPolicy.canBeExecuted(networkTask.retryPolicyConfig)
        ) {
            when (lastAttemptOutcome.retry) {
                RetryState.RUN -> {
                    ++attemptNumber
                    DebugLogger.info(
                        tag,
                        "Executing task .. attempt: $attemptNumber, desc: ${networkTask.description()}",
                    )
                    lastAttemptOutcome = taskPerformingStrategy.performRequest(networkTask)
                }

                RetryState.STOP -> break
            }
            exponentialBackoffPolicy.onHostAttemptFinished(lastAttemptOutcome)
        }

        DebugLogger.info(
            tag,
            "All attempts finished for task ${networkTask.description()}, " +
                "outcome: $lastAttemptOutcome",
        )
        exponentialBackoffPolicy.onAllHostsAttemptsFinished(lastAttemptOutcome)
    }

    private fun canStartExecution(exponentialBackoffPolicy: ExponentialBackoffPolicy): Boolean {
        return rootThreadStateSource.isRunning &&
            networkTask.connectionExecutionPolicy.canBeExecuted() &&
            exponentialBackoffPolicy.canBeExecuted(networkTask.retryPolicyConfig)
    }

    private fun createInitialOutcome(): AttemptOutcome {
        return if (networkTask.onCreateNetworkTask()) {
            AttemptOutcome.ReadyToRun
        } else {
            DebugLogger.info(tag, "Skipping task, desc: ${networkTask.description()}")
            AttemptOutcome.Skipped
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
