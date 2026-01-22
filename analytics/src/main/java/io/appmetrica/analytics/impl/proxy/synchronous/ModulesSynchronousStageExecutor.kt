package io.appmetrica.analytics.impl.proxy.synchronous

import android.content.Context
import io.appmetrica.analytics.AdRevenue
import io.appmetrica.analytics.ModuleEvent
import io.appmetrica.analytics.impl.ClientServiceLocator
import io.appmetrica.analytics.impl.ContextAppearedListener

internal class ModulesSynchronousStageExecutor {

    private val contextAppearedListener: ContextAppearedListener =
        ClientServiceLocator.getInstance().contextAppearedListener

    @Suppress("UNUSED_PARAMETER")
    fun setAdvIdentifiersTracking(enabled: Boolean) {}

    fun reportEvent(moduleEvent: ModuleEvent) {}

    fun setSessionExtra(key: String, value: ByteArray?) {}

    fun reportExternalAttribution(source: Int, value: String?) {}

    fun isActivatedForApp() {}

    fun sendEventsBuffer() {}

    fun getReporter(context: Context, apiKey: String) {
        contextAppearedListener.onProbablyAppeared(context.applicationContext)
    }

    fun reportAdRevenue(adRevenue: AdRevenue, autoCollected: Boolean) {
    }

    fun subscribeForAutoCollectedData(context: Context, apiKey: String) {
        contextAppearedListener.onProbablyAppeared(context.applicationContext)
    }
}
