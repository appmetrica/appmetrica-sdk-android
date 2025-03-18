package io.appmetrica.analytics.screenshot.impl

import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger
import io.appmetrica.analytics.modulesapi.internal.client.ClientContext
import io.appmetrica.analytics.screenshot.impl.callback.DefaultScreenshotCaptorCallback
import io.appmetrica.analytics.screenshot.impl.callback.ScreenshotCaptorCallback
import io.appmetrica.analytics.screenshot.impl.captor.AndroidApiScreenshotCaptor
import io.appmetrica.analytics.screenshot.impl.captor.ContentObserverScreenshotCaptor
import io.appmetrica.analytics.screenshot.impl.captor.ScreenshotCaptor
import io.appmetrica.analytics.screenshot.impl.captor.ServiceScreenshotCaptor
import io.appmetrica.analytics.screenshot.impl.config.client.model.ClientSideRemoteScreenshotConfig

class ScreenshotCaptorsController(
    private val clientContext: ClientContext,
    private val callback: ScreenshotCaptorCallback = DefaultScreenshotCaptorCallback(clientContext),
    private val captors: List<ScreenshotCaptor> =
        listOf(
            AndroidApiScreenshotCaptor(clientContext, callback),
            ServiceScreenshotCaptor(clientContext, callback),
            ContentObserverScreenshotCaptor(clientContext, callback),
        ),
) {

    private val tag = "[ScreenshotCaptorsController]"

    fun startCapture(config: ClientSideRemoteScreenshotConfig?) {
        DebugLogger.info(tag, "Start capture with config $config")

        captors.forEach { it.startCapture() }

        updateConfig(config)
    }

    fun updateConfig(config: ClientSideRemoteScreenshotConfig?) {
        DebugLogger.info(tag, "Update config $config")

        captors.forEach { it.updateConfig(config?.takeIf { it.enabled }?.config) }
    }
}
