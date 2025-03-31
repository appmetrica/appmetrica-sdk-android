package io.appmetrica.analytics.impl

import android.content.Context
import io.appmetrica.analytics.coreutils.internal.logger.LoggerStorage
import io.appmetrica.analytics.impl.proxy.AppMetricaFacadeProvider
import io.appmetrica.analytics.impl.utils.executors.ClientExecutorProvider
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger

class AnonymousClientActivator(
    private val provider: AppMetricaFacadeProvider,
    private val sessionsTrackingManager: SessionsTrackingManager,
    private val clientExecutorProvider: ClientExecutorProvider
) {

    private val tag = "[AnonymousClientActivator]"

    fun activate(context: Context) {
        DebugLogger.info(tag, "Activating anonymous client")
        val logger = LoggerStorage.getMainPublicOrAnonymousLogger()
        if (!provider.isActivated) {
            if (DefaultValues.DEFAULT_SESSIONS_AUTO_TRACKING_ENABLED_FOR_ANONYMOUS_ACTIVATION) {
                logger.info("Session autotracking enabled")
                sessionsTrackingManager.startWatchingIfNotYet()
            } else {
                logger.info("Session autotracking disabled")
            }
        }
        provider.getInitializedImpl(context).activateCore(null)
        clientExecutorProvider.defaultExecutor.execute {
            provider.getInitializedImpl(context).activateFull()
        }
        provider.markActivated()
    }
}
