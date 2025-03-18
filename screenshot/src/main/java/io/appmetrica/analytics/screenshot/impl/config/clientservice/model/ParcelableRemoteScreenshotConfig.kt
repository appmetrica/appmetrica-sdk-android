package io.appmetrica.analytics.screenshot.impl.config.clientservice.model

import android.os.Parcel
import android.os.Parcelable
import io.appmetrica.analytics.screenshot.impl.config.service.model.ServiceSideRemoteScreenshotConfig

class ParcelableRemoteScreenshotConfig(
    val enabled: Boolean,
    val config: ParcelableScreenshotConfig?,
) : Parcelable {

    constructor() : this(ServiceSideRemoteScreenshotConfig())

    constructor(remote: ServiceSideRemoteScreenshotConfig) : this(
        remote.enabled,
        remote.config?.let { ParcelableScreenshotConfig(it) },
    )

    constructor(parcel: Parcel) : this(
        parcel.readByte() != 0.toByte(),
        parcel.readParcelable(ParcelableScreenshotConfig::class.java.classLoader),
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeByte(if (enabled) 1 else 0)
        parcel.writeParcelable(config, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun toString(): String {
        return "ParcelableRemoteScreenshotConfig(" +
            "enabled=$enabled" +
            ", config=$config" +
            ")"
    }

    companion object CREATOR : Parcelable.Creator<ParcelableRemoteScreenshotConfig> {
        override fun createFromParcel(parcel: Parcel): ParcelableRemoteScreenshotConfig {
            return ParcelableRemoteScreenshotConfig(parcel)
        }

        override fun newArray(size: Int): Array<ParcelableRemoteScreenshotConfig?> {
            return arrayOfNulls(size)
        }
    }
}
