package io.appmetrica.analytics.coreapi.internal.servicecomponents

import io.appmetrica.analytics.coreapi.internal.executors.ICommonExecutor

interface FirstExecutionConditionService {

    fun createDelayedTask(tag: String, executor: ICommonExecutor, runnable: Runnable): FirstExecutionDelayedTask
}
