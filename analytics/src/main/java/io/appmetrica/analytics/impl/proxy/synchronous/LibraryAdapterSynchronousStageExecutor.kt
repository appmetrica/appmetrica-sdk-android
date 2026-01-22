package io.appmetrica.analytics.impl.proxy.synchronous

import android.content.Context
import io.appmetrica.analytics.AppMetricaLibraryAdapterConfig
import io.appmetrica.analytics.impl.ClientServiceLocator

internal class LibraryAdapterSynchronousStageExecutor {

    fun activate(context: Context) {
        ClientServiceLocator.getInstance().contextAppearedListener.onProbablyAppeared(context)
        ClientServiceLocator.getInstance().anonymousClientActivator.activate(context)
    }

    fun activate(context: Context, config: AppMetricaLibraryAdapterConfig) {
        ClientServiceLocator.getInstance().contextAppearedListener.onProbablyAppeared(context)
        ClientServiceLocator.getInstance().anonymousClientActivator.activate(context, config)
    }

    fun setAdvIdentifiersTracking(enabled: Boolean) {}

    fun reportEvent(sender: String?, event: String?, payload: String?) {}

    fun subscribeForAutoCollectedData(context: Context, apiKey: String) {
        ClientServiceLocator.getInstance().contextAppearedListener.onProbablyAppeared(context)
        ClientServiceLocator.getInstance().anonymousClientActivator.activateDelayed(context)
    }
}
