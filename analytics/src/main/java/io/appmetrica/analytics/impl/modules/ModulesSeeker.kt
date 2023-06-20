package io.appmetrica.analytics.impl.modules

import io.appmetrica.analytics.coreutils.internal.logger.YLogger
import io.appmetrica.analytics.impl.GlobalServiceLocator

private const val TAG = "[ModuleSeeker]"

class ModulesSeeker {

    private val moduleLoader = ModuleLoader()

    fun discoverModules() {
        YLogger.info(TAG, "Discover modules...")
        GlobalServiceLocator.getInstance().moduleEntryPointsRegister.classNames.forEach { moduleEntryPoint ->
            val module = moduleLoader.loadModule(moduleEntryPoint)
            if (module == null) {
                YLogger.info(TAG, "Could not load module with entry point = $moduleEntryPoint")
            } else {
                GlobalServiceLocator.getInstance().modulesController.registerModule(module)
                YLogger.info(TAG, "Module with entry point = $moduleEntryPoint loaded.")
            }
        }
    }
}
