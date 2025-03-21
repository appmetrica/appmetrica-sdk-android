package io.appmetrica.analytics.modulesapi.internal.client

import io.appmetrica.analytics.coreapi.internal.executors.IHandlerExecutor

interface ModuleClientExecutorProvider {

    val defaultExecutor: IHandlerExecutor
}
