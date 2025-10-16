package io.appmetrica.analytics.impl.modules

import io.appmetrica.analytics.impl.ClientServiceLocator
import io.appmetrica.analytics.impl.GlobalServiceLocator
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger
import io.appmetrica.analytics.modulesapi.internal.client.ModuleClientEntryPoint
import io.appmetrica.analytics.modulesapi.internal.service.ModuleServiceEntryPoint

class ModulesSeeker {

    private val tag = "[ModuleSeeker]"

    private val moduleLoader = ModuleLoader()

    fun discoverServiceModules(): List<ModuleStatus> {
        DebugLogger.info(tag, "Discover service modules...")
        return discoverModules<ModuleServiceEntryPoint<Any>>(
            modules = GlobalServiceLocator.getInstance().moduleEntryPointsRegister.classNames
        ) {
            GlobalServiceLocator.getInstance().modulesController.registerModule(it)
        }
    }

    fun discoverClientModules(): List<ModuleStatus> {
        DebugLogger.info(tag, "Discover client modules...")
        return discoverModules<ModuleClientEntryPoint<Any>>(
            modules = ClientServiceLocator.getInstance().moduleEntryPointsRegister.classNames
        ) {
            ClientServiceLocator.getInstance().modulesController.registerModule(it)
        }
    }

    private inline fun <reified T> discoverModules(
        modules: List<String>,
        register: (T) -> Unit
    ): List<ModuleStatus> {
        val modulesStatus = modules.map { moduleEntryPoint ->
            val module = moduleLoader.loadModule<T>(moduleEntryPoint)
            if (module == null) {
                DebugLogger.info(tag, "Could not load module with entry point = $moduleEntryPoint")
                ModuleStatus(moduleEntryPoint, false)
            } else {
                register(module)
                DebugLogger.info(tag, "Module with entry point = $moduleEntryPoint loaded.")
                ModuleStatus(moduleEntryPoint, true)
            }
        }
        DebugLogger.info(tag, "Discover modules finished: $modulesStatus")
        return modulesStatus
    }
}
