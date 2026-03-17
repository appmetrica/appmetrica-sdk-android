package io.appmetrica.analytics.impl.referrer.service.cache

import io.appmetrica.analytics.impl.referrer.service.ReferrerResult

internal interface ReferrerCache {
    val name: String

    fun hasReferrer(): Boolean
    fun getReferrerOrNull(): ReferrerResult?
    fun saveReferrer(result: ReferrerResult)
}
