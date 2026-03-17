package io.appmetrica.analytics.impl.referrer.service.provider

import io.appmetrica.analytics.impl.referrer.service.ReferrerListener

internal interface ReferrerProvider {
    val referrerName: String

    fun requestReferrer(listener: ReferrerListener)
}
