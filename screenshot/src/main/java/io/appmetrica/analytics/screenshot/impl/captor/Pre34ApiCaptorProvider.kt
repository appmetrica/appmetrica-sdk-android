package io.appmetrica.analytics.screenshot.impl.captor

import io.appmetrica.analytics.modulesapi.internal.client.ClientContext
import io.appmetrica.analytics.screenshot.impl.callback.ScreenshotCaptorCallback

internal class Pre34ApiCaptorProvider(
    private val clientContext: ClientContext,
    private val callback: ScreenshotCaptorCallback,
) : CaptorProvider {

    override fun getCaptors() = listOf(
        ContentObserverScreenshotCaptor(clientContext, callback),
        ServiceScreenshotCaptor(clientContext, callback),
    )
}
