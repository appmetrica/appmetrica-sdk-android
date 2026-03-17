package io.appmetrica.analytics.impl.referrer.service

internal fun interface ReferrerListener {
    fun onResult(result: ReferrerResult)
}
