package io.appmetrica.analytics.impl.modules.client.context

import android.content.Context
import io.appmetrica.analytics.coreapi.internal.lifecycle.ActivityLifecycleRegistry
import io.appmetrica.analytics.impl.ClientServiceLocator
import io.appmetrica.analytics.impl.modules.client.ClientStorageProviderImpl
import io.appmetrica.analytics.impl.modules.client.CompositeModuleAdRevenueProcessor
import io.appmetrica.analytics.impl.proxy.InternalClientModuleProxy
import io.appmetrica.analytics.modulesapi.internal.client.ClientStorageProvider
import io.appmetrica.analytics.modulesapi.internal.client.ModuleClientActivator
import io.appmetrica.analytics.modulesapi.internal.client.ModuleClientExecutorProvider
import io.appmetrica.analytics.modulesapi.internal.client.ProcessDetector
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

    override val clientActivator: ModuleClientActivator = object : ModuleClientActivator {
        override fun activate(context: Context) {
            ClientServiceLocator.getInstance().anonymousClientActivator.activateDelayed(context)
        }
    }

    override val clientExecutorProvider: ModuleClientExecutorProvider = ModuleClientExecutorProviderImpl()

    override val processDetector: ProcessDetector = object : ProcessDetector {
        override fun isMainProcess(): Boolean =
            ClientServiceLocator.getInstance().currentProcessDetector.isMainProcess()
    }
}
