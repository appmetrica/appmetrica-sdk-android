package io.appmetrica.analytics.impl.proxy

import android.content.Context
import io.appmetrica.analytics.AppMetricaLibraryAdapterConfig
import io.appmetrica.analytics.ModulesFacade
import io.appmetrica.analytics.impl.ClientServiceLocator
import io.appmetrica.analytics.impl.events.LibraryEventConstructor
import io.appmetrica.analytics.impl.proxy.synchronous.LibraryAdapterSynchronousStageExecutor
import io.appmetrica.analytics.impl.proxy.validation.LibraryAdapterBarrier
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger
import io.appmetrica.analytics.logger.appmetrica.internal.ImportantLogger
import io.appmetrica.analytics.logger.appmetrica.internal.PublicLogger

class AppMetricaLibraryAdapterProxy {

    private val tag = "[AppMetricaLibraryAdapterProxy]"

    private val provider: AppMetricaFacadeProvider =
        ClientServiceLocator.getInstance().appMetricaFacadeProvider
    private val barrier = LibraryAdapterBarrier(provider)
    private val synchronousStageExecutor = LibraryAdapterSynchronousStageExecutor()
    private val libraryEventConstructor = LibraryEventConstructor()

    fun activate(context: Context) {
        if (barrier.activate(context)) {
            DebugLogger.info(tag, "Activate")
            synchronousStageExecutor.activate(context.applicationContext)
        } else {
            ImportantLogger.info(tag, "Activation failed due to context is null")
        }
    }

    fun activate(context: Context, config: AppMetricaLibraryAdapterConfig) {
        if (barrier.activate(context, config)) {
            DebugLogger.info(tag, "Activate with config: $config")
            synchronousStageExecutor.activate(context.applicationContext, config)
        } else {
            ImportantLogger.info(tag, "Activation failed due to context is null or invalid config")
        }
    }

    fun setAdvIdentifiersTracking(enabled: Boolean) {
        if (barrier.setAdvIdentifiersTracking(enabled)) {
            DebugLogger.info(tag, "SetAdvIdentifiersTracking with enabled: $enabled")
            synchronousStageExecutor.setAdvIdentifiersTracking(enabled)
            ModulesFacade.setAdvIdentifiersTracking(enabled)
        }
    }

    fun reportEvent(
        sender: String?,
        event: String?,
        payload: String?
    ) {
        if (barrier.reportEvent(sender, event, payload)) {
            synchronousStageExecutor.reportEvent(sender, event, payload)
            ModulesFacade.reportEvent(libraryEventConstructor.constructEvent(sender, event, payload))
        } else {
            val message = "Failed report event from sender: $sender with name = $event and payload = $payload"
            PublicLogger.getAnonymousInstance().warning("$tag$message")
            DebugLogger.warning(tag, message)
        }
    }
}
