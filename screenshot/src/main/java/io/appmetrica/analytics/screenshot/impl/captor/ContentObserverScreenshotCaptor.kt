package io.appmetrica.analytics.screenshot.impl.captor

import android.app.Activity
import android.provider.MediaStore
import io.appmetrica.analytics.coreapi.internal.lifecycle.ActivityEvent
import io.appmetrica.analytics.coreapi.internal.lifecycle.ActivityLifecycleListener
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger
import io.appmetrica.analytics.modulesapi.internal.client.ClientContext
import io.appmetrica.analytics.screenshot.impl.ScreenshotObserver
import io.appmetrica.analytics.screenshot.impl.callback.ScreenshotCaptorCallback
import io.appmetrica.analytics.screenshot.impl.config.client.model.ClientSideContentObserverCaptorConfig
import io.appmetrica.analytics.screenshot.impl.config.client.model.ClientSideScreenshotConfig

class ContentObserverScreenshotCaptor(
    private val clientContext: ClientContext,
    private val callback: ScreenshotCaptorCallback,
) : ScreenshotCaptor {

    private val tag = "[ContentObserverScreenshotCaptor]"

    @Volatile
    private var contentObserverCaptorConfig: ClientSideContentObserverCaptorConfig? = null

    private val screenshotObserver = ScreenshotObserver(
        clientContext
    ) { callback.screenshotCaptured(getType()) }

    override fun getType() = "ContentObserverScreenshotCaptor"

    override fun startCapture() {
        DebugLogger.info(tag, "startCapture $contentObserverCaptorConfig")
        clientContext.activityLifecycleRegistry.registerListener(
            object : ActivityLifecycleListener {
                override fun onEvent(activity: Activity, event: ActivityEvent) {
                    DebugLogger.info(tag, "onEvent $event")
                    when (event) {
                        ActivityEvent.STARTED -> {
                            val localConfig = contentObserverCaptorConfig
                            DebugLogger.info(tag, "Activity started $localConfig")
                            if (localConfig?.enabled != true) {
                                DebugLogger.info(tag, "Captor is disabled")
                                return
                            }
                            try {
                                clientContext.context.contentResolver.registerContentObserver(
                                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                    true,
                                    screenshotObserver
                                )
                            } catch (e: Throwable) {
                                DebugLogger.error(tag, e)
                            }
                        }
                        ActivityEvent.STOPPED -> {
                            DebugLogger.info(tag, "Activity stopped")
                            try {
                                clientContext.context.contentResolver.unregisterContentObserver(
                                    screenshotObserver
                                )
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
        contentObserverCaptorConfig = config?.contentObserverCaptorConfig
        screenshotObserver.updateConfig(contentObserverCaptorConfig)
    }
}
