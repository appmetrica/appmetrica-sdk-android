package io.appmetrica.analytics.impl.id.reflection

import android.os.Bundle
import io.appmetrica.analytics.coreapi.internal.identifiers.AdTrackingInfo
import io.appmetrica.analytics.coreapi.internal.identifiers.AdTrackingInfoResult
import io.appmetrica.analytics.coreapi.internal.identifiers.IdentifierStatus

internal class ReflectionAdvIdParser {

    fun fromBundle(bundle: Bundle?): AdTrackingInfoResult? {
        return bundle?.let { data ->
            AdTrackingInfoResult(
                data.getBundle(Constants.TRACKING_INFO)?.let { trackingInfoBundle ->
                    AdTrackingInfo(
                        requireNotNull(Constants.PROVIDER_MAP[trackingInfoBundle.getString(Constants.PROVIDER)]) {
                            "Provider ${trackingInfoBundle.getString(Constants.PROVIDER)} is invalid"
                        },
                        trackingInfoBundle.getString(Constants.ID),
                        if (trackingInfoBundle.containsKey(Constants.LIMITED)) {
                            trackingInfoBundle.getBoolean(Constants.LIMITED)
                        } else {
                            null
                        }
                    )
                },
                IdentifierStatus.from(data.getString(Constants.STATUS)),
                data.getString(Constants.ERROR_MESSAGE)
            )
        }
    }
}
