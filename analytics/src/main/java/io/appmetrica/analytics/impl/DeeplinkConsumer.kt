package io.appmetrica.analytics.impl

import android.content.Intent
import io.appmetrica.analytics.impl.utils.ApiProxyThread
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger

internal class DeeplinkConsumer(private val reportEvent: AppOpenEventReporter) {

    fun interface AppOpenEventReporter {
        fun report(deeplink: String, auto: Boolean)
    }

    private val tag = "[DeeplinkConsumer]"
    private var lastDeeplink: String? = null

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
        if (deeplink.isNullOrBlank()) return
        if (lastDeeplink != deeplink) {
            lastDeeplink = deeplink
            reportEvent.report(deeplink, auto)
        } else {
            DebugLogger.info(tag, "Ignore duplicated deeplink: $deeplink")
        }
    }
}
