package io.appmetrica.analytics.impl.modules.client

import android.os.Bundle
import io.appmetrica.analytics.coreapi.internal.identifiers.SdkIdentifiers
import io.appmetrica.analytics.impl.modules.client.context.CoreClientContext
import io.appmetrica.analytics.impl.selfreporting.AppMetricaSelfReportFacade
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger
import io.appmetrica.analytics.modulesapi.internal.client.ModuleClientEntryPoint
import io.appmetrica.analytics.modulesapi.internal.client.adrevenue.ModuleAdRevenueProcessor
import java.util.concurrent.CopyOnWriteArrayList

internal class ClientModulesController :
    ClientModuleHolder,
    ClientSideModuleInitializer,
    ClientModuleActivationListener {

    private val tag = "[ClientModulesController]"

    private val clientModuleServiceConfigModelFactory = ClientModuleServiceConfigModelFactory()
    private val modules = CopyOnWriteArrayList<ModuleClientEntryPoint<Any>>()
    private var clientContext: CoreClientContext? = null

    val adRevenueCollectorsSourceIds: MutableList<String> = CopyOnWriteArrayList<String>()

    override fun registerModule(moduleClientEntryPoint: ModuleClientEntryPoint<Any>) {
        DebugLogger.info(tag, "Register new module with identifier = ${moduleClientEntryPoint.identifier}")
        modules.add(moduleClientEntryPoint)
    }

    override fun initClientSide(clientContext: CoreClientContext) {
        DebugLogger.info(tag, "Init client side. Total modules count = ${modules.size}")
        this.clientContext = clientContext
        val modulesWithProblems = hashSetOf<ModuleClientEntryPoint<Any>>()
        modules.forEach { module ->
            try {
                module.initClientSide(clientContext)
            } catch (e: Throwable) {
                DebugLogger.error(
                    "$tag [${module.identifier}]",
                    e,
                    "unhandled exception when calling initClientSide"
                )
                reportSelfErrorEvent(module.identifier, "initClientSide", e)
                modulesWithProblems.add(module)
            }
        }
        DebugLogger.warning(
            tag,
            "Disabling defective modules: ${modulesWithProblems.joinToString(", ") { it.identifier }}"
        )
        modules.removeAll(modulesWithProblems)
    }

    override fun onActivated() {
        DebugLogger.info(tag, "Notify modules with remote config updated")
        modules.forEach { module ->
            try {
                module.onActivated()
            } catch (e: Throwable) {
                DebugLogger.error(
                    "$tag [${module.identifier}]",
                    e,
                    "unhandled exception when calling onActivated"
                )
                reportSelfErrorEvent(module.identifier, "onActivated", e)
            }
        }
        adRevenueCollectorsSourceIds.addAll(
            modules.mapNotNull { it.adRevenueCollector }.filter { it.enabled }.map { it.sourceIdentifier }
        )
        DebugLogger.info(tag, "adRevenueCollectorsSourceIds = $adRevenueCollectorsSourceIds")
    }

    fun getModuleAdRevenueProcessor(): ModuleAdRevenueProcessor? {
        return clientContext?.moduleAdRevenueContext?.adRevenueProcessorsHolder
    }

    fun notifyModulesWithConfig(bundle: Bundle?, identifiers: SdkIdentifiers) {
        if (bundle == null) {
            return
        }
        modules.forEach { module ->
            try {
                module.serviceConfigExtensionConfiguration?.let { extension ->
                    val listener = extension.getServiceConfigUpdateListener()
                    clientModuleServiceConfigModelFactory.createClientModuleServiceConfigModel(
                        bundle = bundle,
                        moduleIdentifier = module.identifier,
                        identifiers = identifiers,
                        extensionConfiguration = extension
                    )?.let { config ->
                        DebugLogger.info(tag, "Notify module ${module.identifier} with config $config")
                        listener.onServiceConfigUpdated(config)
                    }
                }
            } catch (e: Throwable) {
                DebugLogger.error(
                    "$tag [${module.identifier}]",
                    e,
                    "unhandled exception when notifying with config"
                )
                reportSelfErrorEvent(module.identifier, "notifyModulesWithConfig", e)
            }
        }
    }

    private fun reportSelfErrorEvent(moduleIdentifier: String, tag: String, throwable: Throwable) {
        AppMetricaSelfReportFacade.getReporter().reportEvent(
            "client_module_errors",
            mapOf(moduleIdentifier to mapOf(tag to throwable.stackTraceToString()))
        )
    }
}
