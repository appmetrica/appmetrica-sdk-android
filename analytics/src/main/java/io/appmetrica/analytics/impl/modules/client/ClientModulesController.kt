package io.appmetrica.analytics.impl.modules.client

import android.os.Bundle
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

    private val modules = CopyOnWriteArrayList<ModuleClientEntryPoint<Any>>()
    private var clientContext: CoreClientContext? = null

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
    }

    fun getModuleAdRevenueProcessor(): ModuleAdRevenueProcessor? {
        return clientContext?.moduleAdRevenueContext?.adRevenueProcessorsHolder
    }

    fun notifyModulesWithConfig(bundle: Bundle?) {
        modules.forEach { module ->
            module.clientConfigExtension?.clientConfigListener?.let { listener ->
                bundle?.getBundle(module.identifier)?.let { config ->
                    listener.onConfigReceived(config)
                }
            }
        }
    }

    fun doModulesNeedConfig(): Boolean {
        return modules.any { module ->
            module.clientConfigExtension?.doesModuleNeedConfig() ?: false
        }
    }

    private fun reportSelfErrorEvent(moduleIdentifier: String, tag: String, throwable: Throwable) {
        AppMetricaSelfReportFacade.getReporter().reportEvent(
            "client_module_errors",
            mapOf(moduleIdentifier to mapOf(tag to throwable.stackTraceToString()))
        )
    }
}
