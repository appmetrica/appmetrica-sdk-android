package io.appmetrica.analytics.impl.proxy.synchronous

import android.content.Context
import io.appmetrica.analytics.impl.ClientServiceLocator

class LibraryAdapterSynchronousStageExecutor {

    fun activate(context: Context) {
        ClientServiceLocator.getInstance().contextAppearedListener.onProbablyAppeared(context)
        ClientServiceLocator.getInstance().anonymousClientActivator.activate(context)
    }

    fun reportEvent(
        sender: String?,
        event: String?,
        payload: String?
    ) {
    }
}
