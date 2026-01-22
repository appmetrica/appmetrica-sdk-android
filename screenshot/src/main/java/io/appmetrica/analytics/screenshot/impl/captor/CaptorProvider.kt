package io.appmetrica.analytics.screenshot.impl.captor

internal sealed interface CaptorProvider {

    fun getCaptors(): List<ScreenshotCaptor>
}
