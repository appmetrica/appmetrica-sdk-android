package io.appmetrica.analytics.screenshot.impl.callback

internal fun interface ScreenshotCaptorCallback {

    fun screenshotCaptured(captorType: String)
}
