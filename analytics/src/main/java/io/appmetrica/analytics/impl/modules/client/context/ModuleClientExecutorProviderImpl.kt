package io.appmetrica.analytics.impl.modules.client.context

import io.appmetrica.analytics.coreapi.internal.executors.IHandlerExecutor
import io.appmetrica.analytics.impl.ClientServiceLocator
import io.appmetrica.analytics.modulesapi.internal.client.ModuleClientExecutorProvider

class ModuleClientExecutorProviderImpl : ModuleClientExecutorProvider {

    override val defaultExecutor: IHandlerExecutor
        get() = ClientServiceLocator.getInstance().clientExecutorProvider.defaultExecutor
}
