package io.appmetrica.analytics.impl.modules

import android.location.Location
import io.appmetrica.analytics.coreapi.internal.backport.Consumer
import io.appmetrica.analytics.coreapi.internal.control.Toggle
import io.appmetrica.analytics.coreapi.internal.permission.PermissionStrategy
import io.appmetrica.analytics.coreutils.internal.logger.YLogger
import io.appmetrica.analytics.impl.GlobalServiceLocator
import io.appmetrica.analytics.impl.StartupStateObserver
import io.appmetrica.analytics.impl.permissions.DefaultAskForPermissionStrategyProvider
import io.appmetrica.analytics.impl.startup.StartupState
import io.appmetrica.analytics.modulesapi.internal.AskForPermissionStrategyModuleProvider
import io.appmetrica.analytics.modulesapi.internal.ModuleEntryPoint
import io.appmetrica.analytics.modulesapi.internal.ModuleLocationSourcesController
import io.appmetrica.analytics.modulesapi.internal.ModuleServicesDatabase
import io.appmetrica.analytics.modulesapi.internal.ServiceContext
import java.util.concurrent.CopyOnWriteArrayList

internal class ModulesController :
    ModuleApi,
    ModuleHolder,
    StartupStateObserver,
    ServiceSideModuleInitializer,
    AskForPermissionStrategyModuleProvider {

    private val tag = "[ModulesController]"

    private val askForPermissionStrategyModuleId = "rp"

    private val modules = CopyOnWriteArrayList<ModuleEntryPoint<Any>>()

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
        val consumers = modules.mapNotNull { it.locationExtension?.locationConsumer }
        YLogger.info(tag, "Collect location consumers: $consumers")
        return consumers
    }

    override fun chooseLocationSourceController(): ModuleLocationSourcesController? {
        YLogger.info(tag, "Collect location source controller")
        return modules.mapNotNull { it.locationExtension?.locationSourcesController }.firstOrNull()
    }

    override fun chooseLocationAppStateControlToggle(): Toggle? {
        YLogger.info(tag, "Collect location app state control toggle")
        return modules.mapNotNull { it.locationExtension?.locationControllerAppStateToggle }.firstOrNull()
    }

    override fun collectModuleServiceDatabases(): List<ModuleServicesDatabase> =
        modules.mapNotNull { it.moduleServicesDatabase }

    override fun registerModule(moduleEntryPoint: ModuleEntryPoint<Any>) {
        YLogger.info(tag, "Register new module with identifier = ${moduleEntryPoint.identifier}")
        modules.add(moduleEntryPoint)
        registerAskForPermissionStrategyIfNeeded(moduleEntryPoint)
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
        modules.forEach { module ->
            val configProvider = ModuleRemoteConfigProvider(startupState)
            val config = configProvider.getRemoteConfigForModule(module.identifier)
            module.initServiceSide(serviceContext, config)

            module.moduleEventHandlerFactory?.let {
                YLogger.info(tag, "Register new event handler with identifier = ${module.identifier}")
                GlobalServiceLocator.getInstance().moduleEventHandlersHolder.register(module.identifier, it)
            }
        }
    }

    private fun registerAskForPermissionStrategyIfNeeded(moduleEntryPoint: ModuleEntryPoint<Any>) {
        if (askForPermissionStrategyModuleId == moduleEntryPoint.identifier &&
            moduleEntryPoint is AskForPermissionStrategyModuleProvider
        ) {
            YLogger.info(
                tag,
                "Register askForPermissionStrategy from module with id = $askForPermissionStrategyModuleId"
            )
            askForPermissionStrategyProvider = moduleEntryPoint
        }
    }
}
