package io.appmetrica.analytics.modulesapi.internal.client

import io.appmetrica.analytics.coreapi.internal.executors.IHandlerExecutor

interface ModuleClientExecutorProvider {

    /**
     * Returns the client default executor. Use [IHandlerExecutor.post] to preserve queued execution.
     */
    val defaultExecutor: IHandlerExecutor
}
