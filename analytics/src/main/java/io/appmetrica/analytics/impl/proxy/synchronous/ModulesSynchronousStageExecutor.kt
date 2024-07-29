package io.appmetrica.analytics.impl.proxy.synchronous

import android.content.Context
import io.appmetrica.analytics.AdRevenue
import io.appmetrica.analytics.ModuleEvent
import io.appmetrica.analytics.coreutils.internal.logger.LoggerStorage
import io.appmetrica.analytics.impl.ClientServiceLocator
import io.appmetrica.analytics.impl.ContextAppearedListener
import io.appmetrica.analytics.impl.DefaultValues
import io.appmetrica.analytics.impl.proxy.AppMetricaFacadeProvider

class ModulesSynchronousStageExecutor(
    private val appMetricaFacadeProvider: AppMetricaFacadeProvider,
) {

    private val contextAppearedListener: ContextAppearedListener =
        ClientServiceLocator.getInstance().contextAppearedListener

    fun activate(context: Context) {
        contextAppearedListener.onProbablyAppeared(context)
        val logger = LoggerStorage.getMainPublicOrAnonymousLogger()
        if (DefaultValues.DEFAULT_SESSIONS_AUTO_TRACKING_ENABLED_FOR_ANONYMOUS_ACTIVATION) {
            logger.info("Session autotracking enabled")
            ClientServiceLocator.getInstance().sessionsTrackingManager.startWatchingIfNotYet()
        } else {
            logger.info("Session autotracking disabled")
        }
        appMetricaFacadeProvider.getInitializedImpl(context, true).activateCore(null)
    }

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
}
