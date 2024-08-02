package io.appmetrica.analytics.impl.proxy

import android.content.Context
import io.appmetrica.analytics.ModulesFacade
import io.appmetrica.analytics.coreapi.internal.executors.ICommonExecutor
import io.appmetrica.analytics.impl.ClientServiceLocator
import io.appmetrica.analytics.impl.events.LibraryEventConstructor
import io.appmetrica.analytics.impl.proxy.synchronous.LibraryAdapterSynchronousStageExecutor
import io.appmetrica.analytics.impl.proxy.validation.LibraryAdapterBarrier

class AppMetricaLibraryAdapterProxy {

    private val provider: AppMetricaFacadeProvider =
        ClientServiceLocator.getInstance().appMetricaFacadeProvider
    private val barrier = LibraryAdapterBarrier(provider)
    private val synchronousStageExecutor = LibraryAdapterSynchronousStageExecutor(provider)
    private val libraryEventConstructor = LibraryEventConstructor()
    private val executor: ICommonExecutor =
        ClientServiceLocator.getInstance().clientExecutorProvider.defaultExecutor

    fun activate(context: Context) {
        barrier.activate(context)
        val applicationContext = context.applicationContext
        synchronousStageExecutor.activate(applicationContext)
        executor.execute {
            provider.getInitializedImpl(applicationContext).activateFull()
        }
        provider.markActivated()
    }

    fun reportEvent(
        sender: String,
        event: String,
        payload: String
    ) {
        barrier.reportEvent(sender, event, payload)
        synchronousStageExecutor.reportEvent(sender, event, payload)
        ModulesFacade.reportEvent(libraryEventConstructor.constructEvent(sender, event, payload))
    }
}
