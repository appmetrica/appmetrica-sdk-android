package io.appmetrica.analytics.impl.referrer.service.cache

import io.appmetrica.analytics.impl.db.VitalCommonDataProvider
import io.appmetrica.analytics.impl.referrer.service.ReferrerResult

internal class VitalReferrerCache(private val vitalCommonDataProvider: VitalCommonDataProvider) : ReferrerCache {
    override val name: String = "vital"

    override fun hasReferrer(): Boolean {
        return vitalCommonDataProvider.referrerChecked
    }

    override fun getReferrerOrNull(): ReferrerResult? {
        return vitalCommonDataProvider.referrer?.let { ReferrerResult.Success(it) }
    }

    override fun saveReferrer(result: ReferrerResult) {
        vitalCommonDataProvider.referrer = result.referrerInfo
        vitalCommonDataProvider.referrerChecked = true
    }
}
