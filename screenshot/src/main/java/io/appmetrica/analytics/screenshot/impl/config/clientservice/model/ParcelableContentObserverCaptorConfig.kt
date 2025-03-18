package io.appmetrica.analytics.screenshot.impl.config.clientservice.model

import android.os.Parcel
import android.os.Parcelable
import io.appmetrica.analytics.screenshot.impl.config.service.model.ServiceSideContentObserverCaptorConfig

class ParcelableContentObserverCaptorConfig(
    val enabled: Boolean,
    val mediaStoreColumnNames: List<String>,
    val detectWindowSeconds: Long,
) : Parcelable {

    constructor(remote: ServiceSideContentObserverCaptorConfig) : this(
        remote.enabled,
        remote.mediaStoreColumnNames,
        remote.detectWindowSeconds,
    )

    constructor(parcel: Parcel) : this(
        parcel.readByte() != 0.toByte(),
        parcel.createStringArrayList() ?: emptyList(),
        parcel.readLong(),
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeByte(if (enabled) 1 else 0)
        parcel.writeStringList(mediaStoreColumnNames)
        parcel.writeLong(detectWindowSeconds)
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun toString(): String {
        return "ParcelableContentObserverCaptorConfig(" +
            "enabled=$enabled" +
            ", mediaStoreColumnNames=$mediaStoreColumnNames" +
            ", detectWindowSeconds=$detectWindowSeconds" +
            ")"
    }

    companion object CREATOR : Parcelable.Creator<ParcelableContentObserverCaptorConfig> {
        override fun createFromParcel(parcel: Parcel): ParcelableContentObserverCaptorConfig {
            return ParcelableContentObserverCaptorConfig(parcel)
        }

        override fun newArray(size: Int): Array<ParcelableContentObserverCaptorConfig?> {
            return arrayOfNulls(size)
        }
    }
}
