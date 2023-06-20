package io.appmetrica.analytics.networktasks.impl.utils

import io.appmetrica.analytics.coreapi.internal.identifiers.Identifiers

object ConfigUtils {

    @JvmStatic
    fun areMainIdentifiersValid(identifiers: Identifiers): Boolean =
        !identifiers.uuid.isNullOrEmpty() &&
            !identifiers.deviceId.isNullOrEmpty() &&
            !identifiers.deviceIdHash.isNullOrEmpty()
}
