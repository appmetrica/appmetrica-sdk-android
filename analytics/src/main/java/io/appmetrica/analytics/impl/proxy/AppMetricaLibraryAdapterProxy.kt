package io.appmetrica.analytics.impl.proxy

import android.content.Context
import io.appmetrica.analytics.ModulesFacade
import io.appmetrica.analytics.coreapi.internal.executors.ICommonExecutor
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
    private val synchronousStageExecutor = LibraryAdapterSynchronousStageExecutor(provider)
    private val libraryEventConstructor = LibraryEventConstructor()
    private val executor: ICommonExecutor =
        ClientServiceLocator.getInstance().clientExecutorProvider.defaultExecutor

    fun activate(context: Context) {
        if (barrier.activate(context)) {
            DebugLogger.info(tag, "Activate")
            val applicationContext = context.applicationContext
            synchronousStageExecutor.activate(applicationContext)
            executor.execute {
                provider.getInitializedImpl(applicationContext).activateFull()
            }
            provider.markActivated()
        } else {
            ImportantLogger.info(tag, "Activation failed due to context is null")
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
