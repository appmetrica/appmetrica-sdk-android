package io.appmetrica.analytics.impl.request

import io.appmetrica.analytics.impl.referrer.service.ReferrerManager
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger

internal class StartupRequestReferrerProvider(
    private val referrerFromConfiguration: StartupRequestReferrer?,
    private val referrerManager: ReferrerManager,
) {

    private val tag = "[StartupRequestReferrerProvider]"
    private var referrerFromAppStore: StartupRequestReferrer? = null

    val referrer: StartupRequestReferrer?
        get() = referrerFromConfiguration
            ?.takeUnless { it.referrer.isEmpty() }
            ?: getReferrerFromAppStore()
            ?: referrerFromConfiguration

    @Synchronized
    private fun getReferrerFromAppStore(): StartupRequestReferrer? {
        if (referrerFromAppStore?.referrer.isNullOrEmpty()) {
            referrerManager.getCachedReferrer()?.referrerInfo?.let {
                referrerFromAppStore = StartupRequestReferrer(it.installReferrer, it.source.value)
            }
        }
        DebugLogger.info(
            tag,
            "selected startup request referrer: ${referrerFromAppStore?.referrer}, " +
                "source: ${referrerFromAppStore?.source}",
        )
        return referrerFromAppStore
    }

    @Synchronized
    override fun toString(): String =
        "StartupRequestReferrerProvider(" +
            "referrerFromConfiguration=${referrerFromConfiguration?.referrer}, " +
            "referrerSourceFromConfiguration=${referrerFromConfiguration?.source}, " +
            "referrerFromAppStore=${referrerFromAppStore?.referrer}, " +
            "referrerSourceFromAppStore=${referrerFromAppStore?.source}" +
            ")"
}
