package io.appmetrica.analytics.impl.referrer.service.listener

import io.appmetrica.analytics.impl.referrer.service.ReferrerListener
import io.appmetrica.analytics.impl.referrer.service.ReferrerResult
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger

internal class SafeReferrerListener(
    private val delegate: ReferrerListener,
) : ReferrerListener {
    private val tag = "[SafeReferrerListener]"

    override fun onResult(result: ReferrerResult) {
        try {
            delegate.onResult(result)
        } catch (e: Throwable) {
            DebugLogger.error(tag, e, "The listener $delegate completed processing the result with an error")
        }
    }
}
