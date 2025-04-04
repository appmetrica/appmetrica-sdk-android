package io.appmetrica.analytics.screenshot.impl.callback

import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger
import io.appmetrica.analytics.modulesapi.internal.client.ClientContext
import io.appmetrica.analytics.modulesapi.internal.common.InternalModuleEvent
import io.appmetrica.analytics.screenshot.impl.Constants

class DefaultScreenshotCaptorCallback(
    private val clientContext: ClientContext
) : ScreenshotCaptorCallback {

    private val tag = "[DefaultScreenshotCaptorCallback]"

    override fun screenshotCaptured(captorType: String) {
        DebugLogger.info(tag, "Screenshot captured")
        clientContext.internalClientModuleFacade.reportEvent(
            InternalModuleEvent.newBuilder(Constants.Events.TYPE)
                .withName(Constants.Events.NAME)
                .withAttributes(
                    mapOf(
                        Constants.Events.CAPTOR_TYPE_KEY to captorType
                    )
                )
                .withCategory(InternalModuleEvent.Category.SYSTEM)
                .build()
        )
    }
}
