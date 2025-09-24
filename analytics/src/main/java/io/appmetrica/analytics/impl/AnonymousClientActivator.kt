package io.appmetrica.analytics.impl

import android.content.Context
import io.appmetrica.analytics.AppMetricaLibraryAdapterConfig
import io.appmetrica.analytics.coreutils.internal.executors.SafeRunnable
import io.appmetrica.analytics.coreutils.internal.logger.LoggerStorage
import io.appmetrica.analytics.impl.proxy.AppMetricaFacadeProvider
import io.appmetrica.analytics.impl.utils.executors.ClientExecutorProvider
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger
import java.util.concurrent.TimeUnit

class AnonymousClientActivator(
    private val provider: AppMetricaFacadeProvider,
    private val sessionsTrackingManager: SessionsTrackingManager,
    private val clientExecutorProvider: ClientExecutorProvider
) {

    private val tag = "[AnonymousClientActivator]"

    private val anonymousReporterActivationDelay = TimeUnit.SECONDS.toMillis(10)

    fun activate(context: Context) {
        activate(context, AppMetricaLibraryAdapterConfig.newConfigBuilder().build())
    }

    fun activateDelayed(context: Context) {
        clientExecutorProvider.defaultExecutor.executeDelayed(
            object : SafeRunnable() {
                override fun runSafety() {
                    activate(context)
                }
            },
            anonymousReporterActivationDelay
        )
    }

    @Synchronized
    fun activate(context: Context, config: AppMetricaLibraryAdapterConfig) {
        DebugLogger.info(tag, "Try activating anonymous client")
        if (provider.isActivated) {
            DebugLogger.info(tag, "Client is already activated")
            return
        }
        val logger = LoggerStorage.getMainPublicOrAnonymousLogger()
        DebugLogger.info(tag, "Client is not activated")
        if (DefaultValues.DEFAULT_SESSIONS_AUTO_TRACKING_ENABLED_FOR_ANONYMOUS_ACTIVATION) {
            logger.info("Session autotracking enabled")
            sessionsTrackingManager.startWatchingIfNotYet()
        } else {
            logger.info("Session autotracking disabled")
        }
        provider.getInitializedImpl(context).activateCore(null)
        clientExecutorProvider.defaultExecutor.execute {
            provider.getInitializedImpl(context).activateFull(config)
        }
        provider.markActivated()
    }
}
