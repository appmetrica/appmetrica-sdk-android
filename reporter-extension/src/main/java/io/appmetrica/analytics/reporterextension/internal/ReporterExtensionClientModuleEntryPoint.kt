package io.appmetrica.analytics.reporterextension.internal

import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger
import io.appmetrica.analytics.modulesapi.internal.client.ClientContext
import io.appmetrica.analytics.modulesapi.internal.client.ModuleClientEntryPoint
import java.util.concurrent.TimeUnit

class ReporterExtensionClientModuleEntryPoint : ModuleClientEntryPoint<Any>() {

    private val tag = "[ReporterExtensionClientModuleEntryPoint]"

    private val activationDelaySeconds = 10L

    override val identifier = "reporter_extension"

    override fun initClientSide(clientContext: ClientContext) {
        super.initClientSide(clientContext)
        DebugLogger.info(tag, "initClientSide")
        if (clientContext.processDetector.isMainProcess()) {
            DebugLogger.info(tag, "Schedule delayed activation")
            clientContext.clientExecutorProvider.defaultExecutor.executeDelayed(
                {
                    clientContext.clientActivator.activate(clientContext.context)
                },
                activationDelaySeconds,
                TimeUnit.SECONDS
            )
        } else {
            DebugLogger.info(tag, "Ignore for non main process")
        }
    }
}
