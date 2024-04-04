package io.appmetrica.analytics.impl.proxy.synchronous

import android.content.Context
import androidx.annotation.VisibleForTesting
import io.appmetrica.analytics.ModuleEvent
import io.appmetrica.analytics.impl.ClientServiceLocator
import io.appmetrica.analytics.impl.ContextAppearedListener

class ModulesSynchronousStageExecutor @VisibleForTesting constructor(
    private val contextAppearedListener: ContextAppearedListener
) {

    constructor() : this(
        ClientServiceLocator.getInstance().contextAppearedListener
    )

    fun reportEvent(moduleEvent: ModuleEvent) {}

    fun setSessionExtra(key: String, value: ByteArray?) {}

    fun reportExternalAttribution(source: Int, value: String?) {}

    fun isActivatedForApp() {}

    fun sendEventsBuffer() {}

    fun getReporter(context: Context, apiKey: String) {
        contextAppearedListener.onProbablyAppeared(context.applicationContext)
    }
}
