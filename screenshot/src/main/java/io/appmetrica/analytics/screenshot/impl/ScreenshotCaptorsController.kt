package io.appmetrica.analytics.screenshot.impl

import android.os.Build
import io.appmetrica.analytics.coreutils.internal.AndroidUtils
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger
import io.appmetrica.analytics.modulesapi.internal.client.ClientContext
import io.appmetrica.analytics.screenshot.impl.callback.DefaultScreenshotCaptorCallback
import io.appmetrica.analytics.screenshot.impl.callback.ScreenshotCaptorCallback
import io.appmetrica.analytics.screenshot.impl.captor.CaptorProvider
import io.appmetrica.analytics.screenshot.impl.captor.DefaultCaptorProvider
import io.appmetrica.analytics.screenshot.impl.captor.Pre34ApiCaptorProvider
import io.appmetrica.analytics.screenshot.impl.config.client.model.ClientSideRemoteScreenshotConfig

internal class ScreenshotCaptorsController(
    private val clientContext: ClientContext,
    private val callback: ScreenshotCaptorCallback = DefaultScreenshotCaptorCallback(clientContext),
    captorProvider: CaptorProvider =
        if (AndroidUtils.isApiAchieved(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)) {
            DefaultCaptorProvider(clientContext, callback)
        } else {
            Pre34ApiCaptorProvider(clientContext, callback)
        },
) {

    private val captors = captorProvider.getCaptors()

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
