package io.appmetrica.analytics.impl.startup

import android.os.Parcel
import android.os.Parcelable
import io.appmetrica.analytics.coreapi.internal.identifiers.IdentifierStatus

internal data class FeaturesInternal(
    val sslPinning: Boolean?,
    val status: IdentifierStatus,
    val errorExplanation: String?,
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readValue(Boolean::class.java.classLoader) as? Boolean?,
        IdentifierStatus.from(parcel.readString()),
        parcel.readString()
    )

    constructor() : this(null, IdentifierStatus.UNKNOWN, null)

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeValue(sslPinning)
        parcel.writeString(status.value)
        parcel.writeString(errorExplanation)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<FeaturesInternal> {
        override fun createFromParcel(parcel: Parcel): FeaturesInternal {
            return FeaturesInternal(parcel)
        }

        override fun newArray(size: Int): Array<FeaturesInternal?> {
            return arrayOfNulls(size)
        }
    }
}
