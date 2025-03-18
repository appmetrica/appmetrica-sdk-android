package io.appmetrica.analytics.screenshot.impl.captor

import io.appmetrica.analytics.screenshot.impl.config.client.model.ClientSideScreenshotConfig

interface ScreenshotCaptor {

    fun getType(): String

    fun startCapture()

    fun updateConfig(config: ClientSideScreenshotConfig?)
}
