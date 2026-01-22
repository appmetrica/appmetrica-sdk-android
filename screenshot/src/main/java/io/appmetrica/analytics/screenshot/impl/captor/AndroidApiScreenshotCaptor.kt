package io.appmetrica.analytics.screenshot.impl.captor

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Build
import io.appmetrica.analytics.coreapi.internal.lifecycle.ActivityEvent
import io.appmetrica.analytics.coreapi.internal.lifecycle.ActivityLifecycleListener
import io.appmetrica.analytics.coreutils.internal.AndroidUtils
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger
import io.appmetrica.analytics.modulesapi.internal.client.ClientContext
import io.appmetrica.analytics.screenshot.impl.callback.ScreenshotCaptorCallback
import io.appmetrica.analytics.screenshot.impl.config.client.model.ClientSideApiCaptorConfig
import io.appmetrica.analytics.screenshot.impl.config.client.model.ClientSideScreenshotConfig

internal class AndroidApiScreenshotCaptor(
    private val clientContext: ClientContext,
    private val callback: ScreenshotCaptorCallback,
) : ScreenshotCaptor {

    private val tag = "[AndroidApiScreenshotCaptor]"

    @Volatile
    private var apiCaptorConfig: ClientSideApiCaptorConfig? = null
    private val screenCaptureCallback: Activity.ScreenCaptureCallback by lazy {
        Activity.ScreenCaptureCallback {
            DebugLogger.info(tag, "onScreenCaptured")
            callback.screenshotCaptured(getType())
        }
    }

    override fun getType() = "AndroidApiScreenshotCaptor"

    override fun startCapture() {
        if (!AndroidUtils.isApiAchieved(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)) {
            DebugLogger.info(tag, "API level UPSIDE_DOWN_CAKE not achieved")
            return
        }
        DebugLogger.info(tag, "startCapture $apiCaptorConfig")
        clientContext.activityLifecycleRegistry.registerListener(
            object : ActivityLifecycleListener {
                @SuppressLint("MissingPermission", "NewApi")
                override fun onEvent(activity: Activity, event: ActivityEvent) {
                    DebugLogger.info(tag, "onEvent $event")
                    when (event) {
                        ActivityEvent.STARTED -> {
                            val localConfig = apiCaptorConfig
                            DebugLogger.info(tag, "Activity started $localConfig")
                            if (localConfig?.enabled != true) {
                                DebugLogger.info(tag, "Captor is disabled")
                                return
                            }
                            try {
                                activity.registerScreenCaptureCallback(
                                    clientContext.context.mainExecutor,
                                    screenCaptureCallback
                                )
                            } catch (e: Throwable) {
                                DebugLogger.error(tag, e)
                            }
                        }

                        ActivityEvent.STOPPED -> {
                            DebugLogger.info(tag, "Activity stopped")
                            try {
                                activity.unregisterScreenCaptureCallback(screenCaptureCallback)
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
            ActivityEvent.STARTED,
            ActivityEvent.STOPPED
        )
    }

    override fun updateConfig(config: ClientSideScreenshotConfig?) {
        DebugLogger.info(tag, "updateConfig $config")
        apiCaptorConfig = config?.apiCaptorConfig
    }
}
