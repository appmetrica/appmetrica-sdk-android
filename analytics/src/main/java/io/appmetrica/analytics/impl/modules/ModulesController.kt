package io.appmetrica.analytics.impl.modules

import android.location.Location
import io.appmetrica.analytics.coreapi.internal.backport.Consumer
import io.appmetrica.analytics.coreapi.internal.control.Toggle
import io.appmetrica.analytics.coreapi.internal.permission.PermissionStrategy
import io.appmetrica.analytics.impl.GlobalServiceLocator
import io.appmetrica.analytics.impl.StartupStateObserver
import io.appmetrica.analytics.impl.permissions.DefaultAskForPermissionStrategyProvider
import io.appmetrica.analytics.impl.selfreporting.AppMetricaSelfReportFacade
import io.appmetrica.analytics.impl.startup.StartupState
import io.appmetrica.analytics.logger.internal.YLogger
import io.appmetrica.analytics.modulesapi.internal.common.AskForPermissionStrategyModuleProvider
import io.appmetrica.analytics.modulesapi.internal.service.ModuleLocationSourcesServiceController
import io.appmetrica.analytics.modulesapi.internal.service.ModuleServiceEntryPoint
import io.appmetrica.analytics.modulesapi.internal.service.ModuleServicesDatabase
import io.appmetrica.analytics.modulesapi.internal.service.ServiceContext
import java.util.concurrent.CopyOnWriteArrayList

internal class ModulesController :
    ModuleApi,
    ModuleHolder,
    StartupStateObserver,
    ServiceSideModuleInitializer,
    AskForPermissionStrategyModuleProvider {

    private val tag = "[ModulesController]"

    private val askForPermissionStrategyModuleId = "rp"

    private val modules = CopyOnWriteArrayList<ModuleServiceEntryPoint<Any>>()

    @Volatile
    private var askForPermissionStrategyProvider: AskForPermissionStrategyModuleProvider =
        DefaultAskForPermissionStrategyProvider()

    override val askForPermissionStrategy: PermissionStrategy
        get() = askForPermissionStrategyProvider.askForPermissionStrategy

    override fun collectFeatures(): List<String> {
        val result = modules.flatMap { module ->
            module.remoteConfigExtensionConfiguration?.getFeatures() ?: emptyList()
        }
        YLogger.info(tag, "Collected features from modules: $result")

        return result
    }

    override fun collectBlocks(): Map<String, Int> {
        val result = modules.flatMap {
            it.remoteConfigExtensionConfiguration?.getBlocks()?.toList() ?: emptyList()
        }.toMap()

        YLogger.info(tag, "Collected blocks from modules: $result")

        return result
    }

    override fun collectRemoteConfigControllers(): Map<String, ModuleRemoteConfigController> {
        YLogger.info(tag, "Request remote config controllers")
        return modules.mapNotNull { module ->
            module.remoteConfigExtensionConfiguration?.let {
                module.identifier to ModuleRemoteConfigController(it)
            }
        }.toMap()
    }

    override fun collectLocationConsumers(): List<Consumer<Location?>> {
        val consumers = modules.mapNotNull { it.locationServiceExtension?.locationConsumer }
        YLogger.info(tag, "Collect location consumers: $consumers")
        return consumers
    }

    override fun chooseLocationSourceController(): ModuleLocationSourcesServiceController? {
        YLogger.info(tag, "Collect location source controller")
        return modules.firstNotNullOfOrNull { it.locationServiceExtension?.locationSourcesController }
    }

    override fun chooseLocationAppStateControlToggle(): Toggle? {
        YLogger.info(tag, "Collect location app state control toggle")
        return modules.firstNotNullOfOrNull { it.locationServiceExtension?.locationControllerAppStateToggle }
    }

    override fun collectModuleServiceDatabases(): List<ModuleServicesDatabase> {
        val wrongModules = hashSetOf<ModuleServiceEntryPoint<Any>>()
        val result = arrayListOf<ModuleServicesDatabase>()
        modules.mapNotNull { module ->
            try {
                module.moduleServicesDatabase?.let { result.add(it) }
            } catch (e: Throwable) {
                wrongModules.add(module)
                YLogger.error("$tag [${module.identifier}]", e)
                reportSelfErrorEvent(module.identifier, "db", e)
            }
        }
        YLogger.warning(tag, "Disabling defective modules: ${wrongModules.joinToString(", ") { it.identifier }}")
        modules.removeAll(wrongModules)

        return result
    }

    override fun registerModule(moduleServiceEntryPoint: ModuleServiceEntryPoint<Any>) {
        YLogger.info(tag, "Register new module with identifier = ${moduleServiceEntryPoint.identifier}")
        modules.add(moduleServiceEntryPoint)
        registerAskForPermissionStrategyIfNeeded(moduleServiceEntryPoint)
    }

    override fun onStartupStateChanged(newState: StartupState) {
        YLogger.info(tag, "Notify modules with remote config updated")
        val configProvider = ModuleRemoteConfigProvider(newState)
        modules.forEach { module ->
            module.remoteConfigExtensionConfiguration?.let {
                val config = configProvider.getRemoteConfigForModule(module.identifier)
                YLogger.info(tag, "Notify module with id = ${module.identifier} with config = $config")
                it.getRemoteConfigUpdateListener().onRemoteConfigUpdated(config)
            }
        }
    }

    override fun initServiceSide(serviceContext: ServiceContext, startupState: StartupState) {
        YLogger.info(tag, "Init service side. Total modules count = ${modules.size}")
        val modulesWithProblems = hashSetOf<ModuleServiceEntryPoint<Any>>()
        modules.forEach { module ->
            try {
                val configProvider = ModuleRemoteConfigProvider(startupState)
                val config = configProvider.getRemoteConfigForModule(module.identifier)
                module.initServiceSide(serviceContext, config)

                module.moduleEventServiceHandlerFactory?.let {
                    YLogger.info(tag, "Register new event handler with identifier = ${module.identifier}")
                    GlobalServiceLocator.getInstance().moduleEventHandlersHolder.register(module.identifier, it)
                }
            } catch (e: Throwable) {
                YLogger.error("$tag [${module.identifier}]", e)
                reportSelfErrorEvent(module.identifier, "init", e)
                modulesWithProblems.add(module)
            }
        }
        YLogger.warning(
            tag,
            "Disabling defective modules: ${modulesWithProblems.joinToString(", ") { it.identifier }}"
        )
        modules.removeAll(modulesWithProblems)
    }

    private fun registerAskForPermissionStrategyIfNeeded(moduleServiceEntryPoint: ModuleServiceEntryPoint<Any>) {
        if (askForPermissionStrategyModuleId == moduleServiceEntryPoint.identifier &&
            moduleServiceEntryPoint is AskForPermissionStrategyModuleProvider
        ) {
            YLogger.info(
                tag,
                "Register askForPermissionStrategy from module with id = $askForPermissionStrategyModuleId"
            )
            askForPermissionStrategyProvider = moduleServiceEntryPoint
        }
    }

    private fun reportSelfErrorEvent(moduleIdentifier: String, tag: String, throwable: Throwable) {
        AppMetricaSelfReportFacade.getReporter().reportEvent(
            "module_errors",
            mapOf(moduleIdentifier to mapOf(tag to throwable.stackTraceToString()))
        )
    }
}
