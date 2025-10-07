package io.appmetrica.analytics.impl.modules

import android.content.Context
import io.appmetrica.analytics.impl.ClientServiceLocator
import io.appmetrica.analytics.impl.GlobalServiceLocator
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger
import io.appmetrica.analytics.modulesapi.internal.client.ModuleClientEntryPoint
import io.appmetrica.analytics.modulesapi.internal.service.ModuleServiceEntryPoint

class ModulesSeeker {

    private val tag = "[ModuleSeeker]"

    private val moduleLoader = ModuleLoader()

    fun discoverServiceModules() {
        DebugLogger.info(tag, "Discover service modules...")
        discoverModules<ModuleServiceEntryPoint<Any>>(
            moduleStatusReporter = ModuleStatusReporter(
                executor = GlobalServiceLocator.getInstance().serviceExecutorProvider.metricaCoreExecutor,
                preferences = GlobalServiceLocator.getInstance().servicePreferences,
                modulesType = "service_modules",
            ),
            modules = GlobalServiceLocator.getInstance().moduleEntryPointsRegister.classNames
        ) {
            GlobalServiceLocator.getInstance().modulesController.registerModule(it)
        }
    }

    fun discoverClientModules(
        context: Context
    ) {
        DebugLogger.info(tag, "Discover client modules...")
        discoverModules<ModuleClientEntryPoint<Any>>(
            moduleStatusReporter = ModuleStatusReporter(
                executor = ClientServiceLocator.getInstance().clientExecutorProvider.defaultExecutor,
                preferences = ClientServiceLocator.getInstance().getPreferencesClientDbStorage(context),
                modulesType = "client_modules",
            ),
            modules = ClientServiceLocator.getInstance().moduleEntryPointsRegister.classNames
        ) {
            ClientServiceLocator.getInstance().modulesController.registerModule(it)
        }
    }

    private inline fun <reified T> discoverModules(
        modules: List<String>,
        moduleStatusReporter: ModuleStatusReporter,
        register: (T) -> Unit
    ) {
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
        moduleStatusReporter.reportModulesStatus(modulesStatus)
        DebugLogger.info(tag, "Discover modules finished.")
    }
}
