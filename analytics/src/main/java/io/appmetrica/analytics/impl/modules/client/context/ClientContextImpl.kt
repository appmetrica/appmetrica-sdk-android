package io.appmetrica.analytics.impl.modules.client.context

import android.content.Context
import io.appmetrica.analytics.coreapi.internal.executors.IHandlerExecutor
import io.appmetrica.analytics.coreapi.internal.lifecycle.ActivityLifecycleRegistry
import io.appmetrica.analytics.impl.ClientServiceLocator
import io.appmetrica.analytics.impl.modules.client.ClientStorageProviderImpl
import io.appmetrica.analytics.impl.modules.client.CompositeModuleAdRevenueProcessor
import io.appmetrica.analytics.impl.proxy.InternalClientModuleProxy
import io.appmetrica.analytics.modulesapi.internal.client.ClientStorageProvider
import io.appmetrica.analytics.modulesapi.internal.common.InternalClientModuleFacade

class ClientContextImpl(
    override val context: Context
) : CoreClientContext {

    override val moduleAdRevenueContext: CoreModuleAdRevenueContext =
        CoreModuleAdRevenueContextImpl(
            CompositeModuleAdRevenueProcessor()
        )

    override val clientStorageProvider: ClientStorageProvider = ClientStorageProviderImpl(
        ClientServiceLocator.getInstance().getPreferencesClientDbStorage(context)
    )

    override val internalClientModuleFacade: InternalClientModuleFacade = InternalClientModuleProxy()

    override val activityLifecycleRegistry: ActivityLifecycleRegistry =
        ClientServiceLocator.getInstance().activityLifecycleManager

    override val defaultExecutor: IHandlerExecutor =
        ClientServiceLocator.getInstance().clientExecutorProvider.defaultExecutor
}
