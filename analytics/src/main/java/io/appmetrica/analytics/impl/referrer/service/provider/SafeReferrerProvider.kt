package io.appmetrica.analytics.impl.referrer.service.provider

import io.appmetrica.analytics.impl.referrer.service.ReferrerListener
import io.appmetrica.analytics.impl.referrer.service.ReferrerResult
import io.appmetrica.analytics.impl.selfreporting.AppMetricaSelfReportFacade
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger

internal class SafeReferrerProvider(
    private val delegate: ReferrerProvider,
) : ReferrerProvider {
    private val tag = "[SafeReferrerProvider]"

    override val referrerName: String get() = delegate.referrerName

    override fun requestReferrer(listener: ReferrerListener) {
        try {
            delegate.requestReferrer(listener)
        } catch (e: Throwable) {
            val message = "Failed to request $referrerName referrer"
            DebugLogger.error(tag, e, message)
            AppMetricaSelfReportFacade.getReporter().reportError(message, e)
            listener.onResult(ReferrerResult.Failure(message, e))
        }
    }
}
