package io.appmetrica.analytics.screenshot.impl.config.clientservice.model

import android.os.Parcel
import android.os.Parcelable
import io.appmetrica.analytics.screenshot.impl.config.service.model.ServiceSideApiCaptorConfig

internal class ParcelableApiCaptorConfig(
    val enabled: Boolean,
) : Parcelable {

    constructor(remote: ServiceSideApiCaptorConfig) : this(remote.enabled)

    constructor(parcel: Parcel) : this(parcel.readByte() != 0.toByte())

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeByte(if (enabled) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun toString(): String {
        return "ParcelableApiCaptorConfig(" +
            "enabled=$enabled" +
            ")"
    }

    companion object CREATOR : Parcelable.Creator<ParcelableApiCaptorConfig> {
        override fun createFromParcel(parcel: Parcel): ParcelableApiCaptorConfig {
            return ParcelableApiCaptorConfig(parcel)
        }

        override fun newArray(size: Int): Array<ParcelableApiCaptorConfig?> {
            return arrayOfNulls(size)
        }
    }
}
