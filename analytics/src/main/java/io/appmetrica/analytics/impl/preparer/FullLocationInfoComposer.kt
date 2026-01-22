package io.appmetrica.analytics.impl.preparer

import android.location.LocationManager
import io.appmetrica.analytics.impl.db.event.DbLocationModel
import io.appmetrica.analytics.impl.protobuf.backend.EventProto
import java.util.concurrent.TimeUnit

internal class FullLocationInfoComposer : LocationInfoComposer {

    override fun getLocation(locationInfoFromDb: DbLocationModel?): EventProto.ReportMessage.Location? {
        if (locationInfoFromDb?.longitude != null && locationInfoFromDb.latitude != null) {
            return EventProto.ReportMessage.Location().apply {
                lon = locationInfoFromDb.longitude
                lat = locationInfoFromDb.latitude

                locationInfoFromDb.altitude?.let {
                    altitude = it
                }
                locationInfoFromDb.direction?.let {
                    direction = it
                }
                locationInfoFromDb.precision?.let {
                    precision = it
                }
                locationInfoFromDb.speed?.let {
                    speed = it
                }
                locationInfoFromDb.timestamp?.let {
                    timestamp = TimeUnit.MILLISECONDS.toSeconds(it)
                }
                locationInfoFromDb.provider?.let {
                    when (it) {
                        LocationManager.GPS_PROVIDER -> provider = EventProto.ReportMessage.Location.PROVIDER_GPS
                        LocationManager.NETWORK_PROVIDER ->
                            provider = EventProto.ReportMessage.Location.PROVIDER_NETWORK
                    }
                }
                locationInfoFromDb.originalProvider?.let {
                    originalProvider = it
                }
            }
        }
        return null
    }
}
