package io.appmetrica.analytics.impl.referrer.service.provider.rustore

import android.content.Context
import io.appmetrica.analytics.impl.referrer.service.ReferrerListener
import io.appmetrica.analytics.impl.referrer.service.provider.ReferrerProvider
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger

internal class RuStoreReferrerProvider(
    private val context: Context,
) : ReferrerProvider {

    private val tag = "[RuStoreReferrerProvider]"

    override val referrerName: String = "rustore"

    override fun requestReferrer(listener: ReferrerListener) {
        DebugLogger.info(tag, "Try to get $referrerName referrer")
        RuStoreReferrerService(context).requestReferrer(listener)
    }
}
