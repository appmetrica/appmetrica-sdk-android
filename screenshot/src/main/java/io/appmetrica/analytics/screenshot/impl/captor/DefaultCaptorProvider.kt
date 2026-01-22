package io.appmetrica.analytics.screenshot.impl.captor

import io.appmetrica.analytics.modulesapi.internal.client.ClientContext
import io.appmetrica.analytics.screenshot.impl.callback.ScreenshotCaptorCallback

internal class DefaultCaptorProvider(
    private val clientContext: ClientContext,
    private val callback: ScreenshotCaptorCallback,
) : CaptorProvider {

    override fun getCaptors() = listOf(
        AndroidApiScreenshotCaptor(clientContext, callback),
        ContentObserverScreenshotCaptor(clientContext, callback),
        ServiceScreenshotCaptor(clientContext, callback),
    )
}
