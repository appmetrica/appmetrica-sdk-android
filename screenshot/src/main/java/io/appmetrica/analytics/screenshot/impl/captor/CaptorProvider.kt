package io.appmetrica.analytics.screenshot.impl.captor

sealed interface CaptorProvider {

    fun getCaptors(): List<ScreenshotCaptor>
}
