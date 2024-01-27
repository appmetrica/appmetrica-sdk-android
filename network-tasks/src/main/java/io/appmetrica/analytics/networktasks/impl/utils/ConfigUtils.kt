package io.appmetrica.analytics.networktasks.impl.utils

import io.appmetrica.analytics.coreapi.internal.identifiers.SdkIdentifiers

object ConfigUtils {

    @JvmStatic
    fun areMainIdentifiersValid(identifiers: SdkIdentifiers): Boolean =
        !identifiers.uuid.isNullOrEmpty() &&
            !identifiers.deviceId.isNullOrEmpty() &&
            !identifiers.deviceIdHash.isNullOrEmpty()
}
