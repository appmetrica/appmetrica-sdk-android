package io.appmetrica.analytics.coreutils.internal

import android.location.Location
import android.os.Parcel

object LocationUtils {

    @JvmStatic
    fun locationToBytes(location: Location?): ByteArray? {
        var locationBytes: ByteArray? = null
        if (location != null) {
            val locationParcel = Parcel.obtain()
            try {
                locationParcel.writeValue(location)
                locationBytes = locationParcel.marshall()
            } catch (exception: Throwable) {
                // Do nothing
            } finally {
                locationParcel.recycle()
            }
        }
        return locationBytes
    }

    @JvmStatic
    fun bytesToLocation(locationBytes: ByteArray?): Location? {
        var result: Location? = null
        if (locationBytes != null) {
            val locationParcel = Parcel.obtain()
            try {
                locationParcel.unmarshall(locationBytes, 0, locationBytes.size)
                locationParcel.setDataPosition(0)
                result = locationParcel.readValue(Location::class.java.classLoader) as Location?
            } catch (exception: Throwable) {
                // Do nothing
            } finally {
                locationParcel.recycle()
            }
        }
        return result
    }
}
