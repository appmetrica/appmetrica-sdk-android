package io.appmetrica.analytics.networktasks.impl.utils

import io.appmetrica.analytics.coreapi.internal.identifiers.SdkIdentifiers

internal object ConfigUtils {

    @JvmStatic
    fun areMainIdentifiersValid(identifiers: SdkIdentifiers): Boolean =
        !identifiers.uuid.isNullOrEmpty() &&
            !identifiers.deviceId.isNullOrEmpty() &&
            !identifiers.deviceIdHash.isNullOrEmpty()
}
