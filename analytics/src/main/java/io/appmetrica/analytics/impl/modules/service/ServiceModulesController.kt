package io.appmetrica.analytics.impl.modules.service

import android.location.Location
import android.os.Bundle
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
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger
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

    private val tag = "[ServiceModulesController]"

    private val askForPermissionStrategyModuleId = "rp"

    private val modules = CopyOnWriteArrayList<ModuleServiceEntryPoint<Any>>()

    @Volatile
    private var askForPermissionStrategyProvider: AskForPermissionStrategyModuleProvider =
        DefaultAskForPermissionStrategyProvider()

    override val askForPermissionStrategy: PermissionStrategy
        get() = askForPermissionStrategyProvider.askForPermissionStrategy

    override fun collectFeatures(): List<String> {
        val wrongModules = hashSetOf<ModuleServiceEntryPoint<Any>>()
        val result = modules.flatMap { module ->
            try {
                module.remoteConfigExtensionConfiguration?.getFeatures() ?: emptyList()
            } catch (e: Throwable) {
                DebugLogger.error(tag, "Failed to get features from module ${module.identifier}", e)
                reportSelfErrorEvent(module.identifier, "features", e)
                wrongModules.add(module)
                emptyList()
            }
        }
        disableDefectiveModules(wrongModules)
        DebugLogger.info(tag, "Collected features from modules: $result")

        return result
    }

    override fun collectBlocks(): Map<String, Int> {
        val wrongModules = hashSetOf<ModuleServiceEntryPoint<Any>>()
        val result = modules.flatMap {
            try {
                it.remoteConfigExtensionConfiguration?.getBlocks()?.toList() ?: emptyList()
            } catch (e: Throwable) {
                DebugLogger.error(tag, "Failed to get blocks from module ${it.identifier}", e)
                reportSelfErrorEvent(it.identifier, "blocks", e)
                wrongModules.add(it)
                emptyList()
            }
        }.toMap()

        disableDefectiveModules(wrongModules)
        DebugLogger.info(tag, "Collected blocks from modules: $result")

        return result
    }

    fun getModulesConfigsBundleForClient(): Bundle {
        val result = Bundle()
        val wrongModules = hashSetOf<ModuleServiceEntryPoint<Any>>()
        modules.forEach { module ->
            val bundle = try {
                module.clientConfigProvider?.getConfigBundleForClient()
            } catch (e: Throwable) {
                DebugLogger.error(tag, "Failed to get config bundle from module ${module.identifier}", e)
                reportSelfErrorEvent(module.identifier, "config_bundle", e)
                wrongModules.add(module)
                null
            }
            bundle?.let {
                result.putBundle(module.identifier, it)
            }
        }
        disableDefectiveModules(wrongModules)
        DebugLogger.info(tag, "Collected config bundles from modules: $result")

        return result
    }

    override fun collectRemoteConfigControllers(): Map<String, ModuleRemoteConfigController> {
        DebugLogger.info(tag, "Request remote config controllers")
        val wrongModules = hashSetOf<ModuleServiceEntryPoint<Any>>()
        val result = modules.mapNotNull { module ->
            try {
                module.remoteConfigExtensionConfiguration?.let {
                    module.identifier to ModuleRemoteConfigController(it)
                }
            } catch (e: Throwable) {
                DebugLogger.error(tag, "Failed to get remote config controller from module ${module.identifier}", e)
                reportSelfErrorEvent(module.identifier, "remote_config_controller", e)
                wrongModules.add(module)
                null
            }
        }.toMap()

        DebugLogger.info(tag, "Collected remote config controllers from modules: $result")
        disableDefectiveModules(wrongModules)
        return result
    }

    override fun collectLocationConsumers(): List<Consumer<Location?>> {
        val wrongModules = hashSetOf<ModuleServiceEntryPoint<Any>>()
        val consumers = modules.mapNotNull {
            try {
                it.locationServiceExtension?.locationConsumer
            } catch (e: Throwable) {
                DebugLogger.error(tag, "Failed to get location consumer from module ${it.identifier}", e)
                reportSelfErrorEvent(it.identifier, "location_consumer", e)
                wrongModules.add(it)
                null
            }
        }
        disableDefectiveModules(wrongModules)
        DebugLogger.info(tag, "Collect location consumers: $consumers")
        return consumers
    }

    override fun chooseLocationSourceController(): ModuleLocationSourcesServiceController? {
        DebugLogger.info(tag, "Collect location source controller")
        val wrongModules = hashSetOf<ModuleServiceEntryPoint<Any>>()
        val result = modules.firstNotNullOfOrNull {
            try {
                it.locationServiceExtension?.locationSourcesController
            } catch (e: Throwable) {
                DebugLogger.error(tag, "Failed to get location source controller from module ${it.identifier}", e)
                reportSelfErrorEvent(it.identifier, "location_source_controller", e)
                wrongModules.add(it)
                null
            }
        }

        DebugLogger.info(tag, "Detected location source controller: $result")
        disableDefectiveModules(wrongModules)
        return result
    }

    override fun chooseLocationAppStateControlToggle(): Toggle? {
        DebugLogger.info(tag, "Collect location app state control toggle")
        val wrongModules = hashSetOf<ModuleServiceEntryPoint<Any>>()
        val result = modules.firstNotNullOfOrNull {
            try {
                it.locationServiceExtension?.locationControllerAppStateToggle
            } catch (e: Throwable) {
                DebugLogger.error(
                    tag,
                    "Failed to get location app state control toggle from module ${it.identifier}",
                    e
                )
                reportSelfErrorEvent(it.identifier, "location_app_state_control_toggle", e)
                wrongModules.add(it)
                null
            }
        }

        DebugLogger.info(tag, "Detected location app state control toggle: $result")
        disableDefectiveModules(wrongModules)
        return result
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

        DebugLogger.info(tag, "Collected module services databases: $result")
        disableDefectiveModules(wrongModules)
        return result
    }

    override fun registerModule(moduleServiceEntryPoint: ModuleServiceEntryPoint<Any>) {
        DebugLogger.info(tag, "Register new module with identifier = ${moduleServiceEntryPoint.identifier}")
        modules.add(moduleServiceEntryPoint)
        registerAskForPermissionStrategyIfNeeded(moduleServiceEntryPoint)
    }

    override fun onStartupStateChanged(newState: StartupState) {
        DebugLogger.info(tag, "Notify modules with remote config updated")
        val wrongModules = hashSetOf<ModuleServiceEntryPoint<Any>>()
        val configProvider = ModuleRemoteConfigProvider(newState)
        modules.forEach { module ->
            try {
                module.remoteConfigExtensionConfiguration?.let {
                    val config = configProvider.getRemoteConfigForServiceModule(module.identifier)
                    DebugLogger.info(tag, "Notify module with id = ${module.identifier} with config = $config")
                    it.getRemoteConfigUpdateListener().onRemoteConfigUpdated(config)
                }
            } catch (e: Throwable) {
                DebugLogger.error(tag, "Failed to notify module with id = ${module.identifier}", e)
                wrongModules.add(module)
                reportSelfErrorEvent(module.identifier, "remote_config_updated", e)
            }
        }
        disableDefectiveModules(wrongModules)
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

        DebugLogger.info(tag, "Init service side finished. Total modules count = ${modules.size}")
        disableDefectiveModules(modulesWithProblems)
    }

    private fun disableDefectiveModules(modulesWithProblems: Set<ModuleServiceEntryPoint<Any>>) {
        if (modulesWithProblems.isNotEmpty()) {
            DebugLogger.warning(
                tag,
                "Disabling defective modules: ${modulesWithProblems.joinToString(", ") { it.identifier }}"
            )
            modules.removeAll(modulesWithProblems)
        }
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
