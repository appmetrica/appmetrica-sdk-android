package io.appmetrica.analytics.screenshot.impl.config.clientservice.model

import android.os.Parcel
import android.os.Parcelable
import io.appmetrica.analytics.screenshot.impl.config.service.model.ServiceSideScreenshotConfig

class ParcelableScreenshotConfig(
    val apiCaptorConfig: ParcelableApiCaptorConfig?,
    val serviceCaptorConfig: ParcelableServiceCaptorConfig?,
    val contentObserverCaptorConfig: ParcelableContentObserverCaptorConfig?,
) : Parcelable {

    constructor(remote: ServiceSideScreenshotConfig) : this(
        remote.apiCaptorConfig?.let { ParcelableApiCaptorConfig(it) },
        remote.serviceCaptorConfig?.let { ParcelableServiceCaptorConfig(it) },
        remote.contentObserverCaptorConfig?.let { ParcelableContentObserverCaptorConfig(it) },
    )

    constructor(parcel: Parcel) : this(
        parcel.readParcelable(ParcelableApiCaptorConfig::class.java.classLoader),
        parcel.readParcelable(ParcelableServiceCaptorConfig::class.java.classLoader),
        parcel.readParcelable(ParcelableContentObserverCaptorConfig::class.java.classLoader),
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(apiCaptorConfig, flags)
        parcel.writeParcelable(serviceCaptorConfig, flags)
        parcel.writeParcelable(contentObserverCaptorConfig, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun toString(): String {
        return "ParcelableScreenshotConfig(" +
            "apiCaptorConfig=$apiCaptorConfig" +
            ", serviceCaptorConfig=$serviceCaptorConfig" +
            ", contentObserverCaptorConfig=$contentObserverCaptorConfig" +
            ")"
    }

    companion object CREATOR : Parcelable.Creator<ParcelableScreenshotConfig> {
        override fun createFromParcel(parcel: Parcel): ParcelableScreenshotConfig {
            return ParcelableScreenshotConfig(parcel)
        }

        override fun newArray(size: Int): Array<ParcelableScreenshotConfig?> {
            return arrayOfNulls(size)
        }
    }
}
