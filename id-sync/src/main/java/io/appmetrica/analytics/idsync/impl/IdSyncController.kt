package io.appmetrica.analytics.idsync.impl

import io.appmetrica.analytics.coreapi.internal.executors.IHandlerExecutor
import io.appmetrica.analytics.coreapi.internal.servicecomponents.ActivationBarrierCallback
import io.appmetrica.analytics.coreutils.internal.executors.SafeRunnable
import io.appmetrica.analytics.idsync.impl.model.RequestStateHolder
import io.appmetrica.analytics.idsync.internal.model.IdSyncConfig
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger
import io.appmetrica.analytics.modulesapi.internal.service.ServiceContext
import java.util.concurrent.TimeUnit

class IdSyncController(
    private val serviceContext: ServiceContext
) {
    private val tag = "[IdSyncController]"

    private val periodicInterval = TimeUnit.MINUTES.toMillis(1)
    private val executor: IHandlerExecutor = serviceContext.executorProvider.moduleExecutor
    private val requestController = IdSyncRequestController(
        serviceContext,
        RequestStateHolder(
            serviceContext.serviceStorageProvider
                .modulePreferences(IdSyncConstants.IDENTIFIER)
        )
    )
    @Volatile
    private var config: IdSyncConfig? = null
    @Volatile
    private var enabled: Boolean = false

    private lateinit var syncRunnable: SafeRunnable

    init {
        syncRunnable = object : SafeRunnable() {
            override fun runSafety() {
                if (!enabled) {
                    DebugLogger.info(tag, "IdSync is disabled. Ignore runnable")
                    return
                }
                val localConfig = config
                if (localConfig == null || !localConfig.isEnabled()) {
                    DebugLogger.info(tag, "IdSync config is not enabled. Ignore runnable")
                    return
                }
                localConfig.requests.forEach {
                    DebugLogger.info(tag, "Handle request: $it")
                    requestController.handle(it)
                }

                DebugLogger.info(tag, "Schedule sync task with interval: $periodicInterval ms")
                executor.executeDelayed(syncRunnable, periodicInterval)
            }
        }
    }

    @Synchronized
    fun refresh(config: IdSyncConfig) {
        if (this.config != config) {
            this.config = config
            if (config.isEnabled() && !enabled) {
                DebugLogger.info(tag, "IdSync config is enabled. Start sync task")
                start(config.launchDelay)
            } else if (!config.isEnabled() && enabled) {
                DebugLogger.info(tag, "IdSync config is disabled. Stop sync task")
                stop()
            } else {
                DebugLogger.info(tag, "IdSync config is not changed. Ignore refresh")
            }
        }
    }

    private fun start(delay: Long) {
        DebugLogger.info(tag, "Start sync task with activation delay: $delay ms")
        serviceContext.activationBarrier.subscribe(
            delay,
            executor,
            object : ActivationBarrierCallback {
                override fun onWaitFinished() {
                    DebugLogger.info(tag, "Run sync task from start")
                    syncRunnable.run()
                }
            }
        )
        enabled = true
    }

    private fun stop() {
        DebugLogger.info(tag, "Stop sync task")
        enabled = false
        executor.remove(syncRunnable)
    }

    private fun IdSyncConfig?.isEnabled(): Boolean {
        this?.enabled ?: return false
        return this.enabled && this.requests.isNotEmpty()
    }
}
