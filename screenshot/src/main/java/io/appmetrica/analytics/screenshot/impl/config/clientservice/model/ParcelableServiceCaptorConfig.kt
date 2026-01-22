package io.appmetrica.analytics.screenshot.impl.config.clientservice.model

import android.os.Parcel
import android.os.Parcelable
import io.appmetrica.analytics.screenshot.impl.config.service.model.ServiceSideServiceCaptorConfig

internal class ParcelableServiceCaptorConfig(
    val enabled: Boolean,
    val delaySeconds: Long,
) : Parcelable {

    constructor(remote: ServiceSideServiceCaptorConfig) : this(
        remote.enabled,
        remote.delaySeconds,
    )

    constructor(parcel: Parcel) : this(
        parcel.readByte() != 0.toByte(),
        parcel.readLong(),
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeByte(if (enabled) 1 else 0)
        parcel.writeLong(delaySeconds)
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun toString(): String {
        return "ParcelableServiceCaptorConfig(" +
            "enabled=$enabled" +
            ", delaySeconds=$delaySeconds" +
            ")"
    }

    companion object CREATOR : Parcelable.Creator<ParcelableServiceCaptorConfig> {
        override fun createFromParcel(parcel: Parcel): ParcelableServiceCaptorConfig {
            return ParcelableServiceCaptorConfig(parcel)
        }

        override fun newArray(size: Int): Array<ParcelableServiceCaptorConfig?> {
            return arrayOfNulls(size)
        }
    }
}
