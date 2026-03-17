package io.appmetrica.analytics.impl.referrer.service

import android.content.Context
import io.appmetrica.analytics.impl.referrer.service.listener.SafeReferrerListener
import io.appmetrica.analytics.impl.referrer.service.provider.ReferrerProvider
import io.appmetrica.analytics.impl.referrer.service.provider.ReferrerProviderFactory
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger

internal class ReferrerManager private constructor(
    private val referrerProvider: ReferrerProvider,
) {
    private val tag = "[ReferrerManager]"

    @Volatile
    private var cachedReferrer: ReferrerResult? = null

    constructor(context: Context) : this(ReferrerProviderFactory().create(context))

    fun warmUpReferrer() {
        DebugLogger.info(tag, "Requesting initial referrer")
        referrerProvider.requestReferrer(SaveListener())
    }

    fun requestReferrer(listener: ReferrerListener) {
        val safeListener = SafeReferrerListener(listener)
        DebugLogger.info(tag, "Requesting referrer with listener $safeListener (original $listener)")
        referrerProvider.requestReferrer(safeListener)
    }

    fun getCachedReferrer(): ReferrerResult? {
        DebugLogger.info(tag, "Getting cached referrer: $cachedReferrer")
        return cachedReferrer
    }

    private inner class SaveListener : ReferrerListener {
        override fun onResult(result: ReferrerResult) {
            DebugLogger.info(tag, "Save referrer: $result")
            cachedReferrer = result
        }
    }
}
