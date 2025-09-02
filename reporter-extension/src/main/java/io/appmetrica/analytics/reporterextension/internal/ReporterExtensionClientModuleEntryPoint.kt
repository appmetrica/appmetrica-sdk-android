package io.appmetrica.analytics.reporterextension.internal

import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger
import io.appmetrica.analytics.modulesapi.internal.client.ClientContext
import io.appmetrica.analytics.modulesapi.internal.client.ModuleClientEntryPoint

class ReporterExtensionClientModuleEntryPoint : ModuleClientEntryPoint<Any>() {

    private val tag = "[ReporterExtensionClientModuleEntryPoint]"

    override val identifier = "reporter_extension"

    override fun initClientSide(clientContext: ClientContext) {
        super.initClientSide(clientContext)
        DebugLogger.info(tag, "initClientSide")
        if (clientContext.processDetector.isMainProcess()) {
            DebugLogger.info(tag, "Schedule delayed activation")
            clientContext.clientActivator.activate(clientContext.context)
        } else {
            DebugLogger.info(tag, "Ignore for non main process")
        }
    }
}
