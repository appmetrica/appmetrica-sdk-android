package io.appmetrica.analytics.impl.modules.service

import android.location.Location
import io.appmetrica.analytics.coreapi.internal.backport.Consumer
import io.appmetrica.analytics.coreapi.internal.control.Toggle
import io.appmetrica.analytics.coreapi.internal.permission.PermissionStrategy
import io.appmetrica.analytics.impl.GlobalServiceLocator
import io.appmetrica.analytics.impl.StartupStateObserver
import io.appmetrica.analytics.impl.modules.ModuleApi
import io.appmetrica.analytics.impl.modules.ModuleRemoteConfigController
import io.appmetrica.analytics.impl.permissions.DefaultAskForPermissionStrategyProvider
import io.appmetrica.analytics.impl.selfreporting.AppMetricaSelfReportFacade
import io.appmetrica.analytics.impl.startup.StartupState
import io.appmetrica.analytics.logger.internal.DebugLogger
import io.appmetrica.analytics.modulesapi.internal.common.AskForPermissionStrategyModuleProvider
import io.appmetrica.analytics.modulesapi.internal.service.ModuleLocationSourcesServiceController
import io.appmetrica.analytics.modulesapi.internal.service.ModuleServiceEntryPoint
import io.appmetrica.analytics.modulesapi.internal.service.ModuleServicesDatabase
import io.appmetrica.analytics.modulesapi.internal.service.ServiceContext
import java.util.concurrent.CopyOnWriteArrayList

internal class ServiceModulesController :
    ModuleApi,
    ServiceModuleHolder,
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
        DebugLogger.info(tag, "Collected features from modules: $result")

        return result
    }

    override fun collectBlocks(): Map<String, Int> {
        val result = modules.flatMap {
            it.remoteConfigExtensionConfiguration?.getBlocks()?.toList() ?: emptyList()
        }.toMap()

        DebugLogger.info(tag, "Collected blocks from modules: $result")

        return result
    }

    override fun collectRemoteConfigControllers(): Map<String, ModuleRemoteConfigController> {
        DebugLogger.info(tag, "Request remote config controllers")
        return modules.mapNotNull { module ->
            module.remoteConfigExtensionConfiguration?.let {
                module.identifier to ModuleRemoteConfigController(it)
            }
        }.toMap()
    }

    override fun collectLocationConsumers(): List<Consumer<Location?>> {
        val consumers = modules.mapNotNull { it.locationServiceExtension?.locationConsumer }
        DebugLogger.info(tag, "Collect location consumers: $consumers")
        return consumers
    }

    override fun chooseLocationSourceController(): ModuleLocationSourcesServiceController? {
        DebugLogger.info(tag, "Collect location source controller")
        return modules.firstNotNullOfOrNull { it.locationServiceExtension?.locationSourcesController }
    }

    override fun chooseLocationAppStateControlToggle(): Toggle? {
        DebugLogger.info(tag, "Collect location app state control toggle")
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
                DebugLogger.error("$tag [${module.identifier}]", e)
                reportSelfErrorEvent(module.identifier, "db", e)
            }
        }
        DebugLogger.warning(tag, "Disabling defective modules: ${wrongModules.joinToString(", ") { it.identifier }}")
        modules.removeAll(wrongModules)

        return result
    }

    override fun registerModule(moduleServiceEntryPoint: ModuleServiceEntryPoint<Any>) {
        DebugLogger.info(tag, "Register new module with identifier = ${moduleServiceEntryPoint.identifier}")
        modules.add(moduleServiceEntryPoint)
        registerAskForPermissionStrategyIfNeeded(moduleServiceEntryPoint)
    }

    override fun onStartupStateChanged(newState: StartupState) {
        DebugLogger.info(tag, "Notify modules with remote config updated")
        val configProvider = ModuleRemoteConfigProvider(newState)
        modules.forEach { module ->
            module.remoteConfigExtensionConfiguration?.let {
                val config = configProvider.getRemoteConfigForServiceModule(module.identifier)
                DebugLogger.info(tag, "Notify module with id = ${module.identifier} with config = $config")
                it.getRemoteConfigUpdateListener().onRemoteConfigUpdated(config)
            }
        }
    }

    override fun initServiceSide(serviceContext: ServiceContext, startupState: StartupState) {
        DebugLogger.info(tag, "Init service side. Total modules count = ${modules.size}")
        val modulesWithProblems = hashSetOf<ModuleServiceEntryPoint<Any>>()
        modules.forEach { module ->
            try {
                val configProvider = ModuleRemoteConfigProvider(startupState)
                val config = configProvider.getRemoteConfigForServiceModule(module.identifier)
                module.initServiceSide(serviceContext, config)

                module.moduleEventServiceHandlerFactory?.let {
                    DebugLogger.info(tag, "Register new event handler with identifier = ${module.identifier}")
                    GlobalServiceLocator.getInstance().moduleEventHandlersHolder.register(module.identifier, it)
                }
            } catch (e: Throwable) {
                DebugLogger.error("$tag [${module.identifier}]", e)
                reportSelfErrorEvent(module.identifier, "init", e)
                modulesWithProblems.add(module)
            }
        }
        DebugLogger.warning(
            tag,
            "Disabling defective modules: ${modulesWithProblems.joinToString(", ") { it.identifier }}"
        )
        modules.removeAll(modulesWithProblems)
    }

    private fun registerAskForPermissionStrategyIfNeeded(moduleServiceEntryPoint: ModuleServiceEntryPoint<Any>) {
        if (askForPermissionStrategyModuleId == moduleServiceEntryPoint.identifier &&
            moduleServiceEntryPoint is AskForPermissionStrategyModuleProvider
        ) {
            DebugLogger.info(
                tag,
                "Register askForPermissionStrategy from module with id = $askForPermissionStrategyModuleId"
            )
            askForPermissionStrategyProvider = moduleServiceEntryPoint
        }
    }

    private fun reportSelfErrorEvent(moduleIdentifier: String, tag: String, throwable: Throwable) {
        AppMetricaSelfReportFacade.getReporter().reportEvent(
            "service_module_errors",
            mapOf(moduleIdentifier to mapOf(tag to throwable.stackTraceToString()))
        )
    }
}
