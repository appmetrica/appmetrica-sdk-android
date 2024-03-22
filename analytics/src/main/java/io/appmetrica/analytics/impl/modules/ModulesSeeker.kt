package io.appmetrica.analytics.impl.modules

import io.appmetrica.analytics.impl.ClientServiceLocator
import io.appmetrica.analytics.impl.GlobalServiceLocator
import io.appmetrica.analytics.logger.internal.YLogger
import io.appmetrica.analytics.modulesapi.internal.client.ModuleClientEntryPoint
import io.appmetrica.analytics.modulesapi.internal.service.ModuleServiceEntryPoint

private const val TAG = "[ModuleSeeker]"

class ModulesSeeker {

    private val moduleLoader = ModuleLoader()

    fun discoverServiceModules() {
        discoverModules<ModuleServiceEntryPoint<Any>>(
            modules = GlobalServiceLocator.getInstance().moduleEntryPointsRegister.classNames
        ) {
            GlobalServiceLocator.getInstance().modulesController.registerModule(it)
        }
    }

    fun discoverClientModules() {
        discoverModules<ModuleClientEntryPoint<Any>>(
            modules = ClientServiceLocator.getInstance().moduleEntryPointsRegister.classNames
        ) {
            ClientServiceLocator.getInstance().modulesController.registerModule(it)
        }
    }

    private inline fun <reified T> discoverModules(modules: Set<String>, register: (T) -> Unit) {
        YLogger.info(TAG, "Discover modules...")
        modules.forEach { moduleEntryPoint ->
            val module = moduleLoader.loadModule<T>(moduleEntryPoint)
            if (module == null) {
                YLogger.info(TAG, "Could not load module with entry point = $moduleEntryPoint")
            } else {
                register(module)
                YLogger.info(TAG, "Module with entry point = $moduleEntryPoint loaded.")
            }
        }
    }
}
