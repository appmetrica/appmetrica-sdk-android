package io.appmetrica.analytics.screenshot.impl.captor

import io.appmetrica.analytics.screenshot.impl.config.client.model.ClientSideScreenshotConfig

internal interface ScreenshotCaptor {

    fun getType(): String

    fun startCapture()

    fun updateConfig(config: ClientSideScreenshotConfig?)
}
