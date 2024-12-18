package io.appmetrica.analytics.impl

import android.content.Intent
import io.appmetrica.analytics.impl.utils.ApiProxyThread
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger

class DeeplinkConsumer(private val mainReporter: IMainReporter) {

    private val tag = "[DeeplinkConsumer]"
    private var lastDeeplink = ""

    @ApiProxyThread
    fun reportAppOpen(intent: Intent?) {
        if (intent != null) {
            val deeplink = intent.dataString
            reportAppOpenInternal(deeplink, false)
        }
    }

    @ApiProxyThread
    fun reportAutoAppOpen(deeplink: String?) {
        reportAppOpenInternal(deeplink, true)
    }

    @ApiProxyThread
    fun reportAppOpen(deeplink: String?) {
        reportAppOpenInternal(deeplink, false)
    }

    @ApiProxyThread
    private fun reportAppOpenInternal(deeplink: String?, auto: Boolean) {
        DebugLogger.info(tag, "Try to report deeplink: $deeplink, auto: $auto")
        deeplink?.takeIf { it.isNotEmpty() } ?: return
        if (lastDeeplink != deeplink) {
            lastDeeplink = deeplink
            mainReporter.reportAppOpen(deeplink, auto)
        } else {
            DebugLogger.info(tag, "Ignore duplicated deeplink: $deeplink")
        }
    }
}
