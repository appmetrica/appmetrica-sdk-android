package io.appmetrica.analytics.screenshot.impl.captor

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.os.Handler
import io.appmetrica.analytics.coreapi.internal.lifecycle.ActivityEvent
import io.appmetrica.analytics.coreapi.internal.lifecycle.ActivityLifecycleListener
import io.appmetrica.analytics.coreutils.internal.system.SystemServiceUtils
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger
import io.appmetrica.analytics.modulesapi.internal.client.ClientContext
import io.appmetrica.analytics.screenshot.impl.callback.ScreenshotCaptorCallback
import io.appmetrica.analytics.screenshot.impl.config.client.model.ClientSideScreenshotConfig
import io.appmetrica.analytics.screenshot.impl.config.client.model.ClientSideServiceCaptorConfig
import java.util.concurrent.TimeUnit

class ServiceScreenshotCaptor(
    private val clientContext: ClientContext,
    private val callback: ScreenshotCaptorCallback,
) : ScreenshotCaptor {

    private val tag = "[ServiceScreenshotCaptor]"

    private val handler: Handler = clientContext.clientExecutorProvider.defaultExecutor.handler

    @Volatile
    private var stoped = false

    @Volatile
    private var serviceCaptorConfig: ClientSideServiceCaptorConfig? = null

    private val serviceSearcher = object : Runnable {
        override fun run() {
            val localConfig = serviceCaptorConfig
            if (stoped || localConfig == null || !localConfig.enabled) {
                return
            }
            SystemServiceUtils.accessSystemServiceByNameSafely(
                clientContext.context,
                Context.ACTIVITY_SERVICE,
                "running service screenshot captor",
                "ActivityManager"
            ) { activityManager: ActivityManager ->
                val runningServices = activityManager.getRunningServices(200) ?: emptyList()

                runningServices.onEach {
                    DebugLogger.info(tag, "Running service: ${it.process}")
                }.firstOrNull {
                    it.process == "com.android.systemui:screenshot"
                }?.let {
                    callback.screenshotCaptured(getType())
                }
                handler.postDelayed(this, TimeUnit.SECONDS.toMillis(localConfig.delaySeconds))
            }
        }
    }

    override fun getType() = "ServiceScreenshotCaptor"

    override fun startCapture() {
        DebugLogger.info(tag, "startCapture $serviceCaptorConfig")
        clientContext.activityLifecycleRegistry.registerListener(
            object : ActivityLifecycleListener {
                override fun onEvent(activity: Activity, event: ActivityEvent) {
                    DebugLogger.info(tag, "onEvent $event")
                    when (event) {
                        ActivityEvent.RESUMED -> {
                            val localConfig = serviceCaptorConfig
                            DebugLogger.info(tag, "Activity resumed $localConfig")
                            if (localConfig?.enabled != true) {
                                DebugLogger.info(tag, "Captor is disabled")
                                return
                            }
                            try {
                                stoped = false
                                handler.postDelayed(serviceSearcher, 0)
                            } catch (e: Throwable) {
                                DebugLogger.error(tag, e)
                            }
                        }

                        ActivityEvent.PAUSED -> {
                            DebugLogger.info(tag, "Activity paused")
                            try {
                                stoped = true
                            } catch (e: Throwable) {
                                DebugLogger.error(tag, e)
                            }
                        }

                        else -> {
                            DebugLogger.info(tag, "Unknown event: $event")
                        }
                    }
                }
            },
            ActivityEvent.RESUMED,
            ActivityEvent.PAUSED
        )
    }

    override fun updateConfig(config: ClientSideScreenshotConfig?) {
        DebugLogger.info(tag, "updateConfig $config")
        serviceCaptorConfig = config?.serviceCaptorConfig
    }
}
