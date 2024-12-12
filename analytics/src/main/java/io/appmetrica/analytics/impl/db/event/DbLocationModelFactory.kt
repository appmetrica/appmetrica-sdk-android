package io.appmetrica.analytics.impl.db.event

import io.appmetrica.analytics.impl.GlobalServiceLocator
import io.appmetrica.analytics.impl.YLocation
import io.appmetrica.analytics.impl.request.ReportRequestConfig

class DbLocationModelFactory(
    private val reportRequestConfig: ReportRequestConfig
) {

    fun create(): DbLocationModel {
        val location = getReportLocation()
        return DbLocationModel(
            enabled = reportRequestConfig.isLocationTracking,
            latitude = location?.latitude,
            longitude = location?.longitude,
            timestamp = location?.time,
            precision = location?.accuracy?.toInt(),
            direction = location?.bearing?.toInt(),
            speed = location?.speed?.toInt(),
            altitude = location?.altitude?.toInt(),
            provider = location?.provider,
            originalProvider = location?.originalProvider
        )
    }

    private fun getReportLocation(): YLocation? {
        val locationApi = GlobalServiceLocator.getInstance().locationClientApi
        val location = locationApi.userLocation?.let {
            YLocation.createWithOriginalProvider(it)
        } ?: locationApi.systemLocation?.let {
            YLocation.createWithoutOriginalProvider(it)
        }
        return location
    }
}
