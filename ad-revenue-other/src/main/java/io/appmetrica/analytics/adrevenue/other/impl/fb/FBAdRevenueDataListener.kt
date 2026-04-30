package io.appmetrica.analytics.adrevenue.other.impl.fb

import android.os.Bundle
import com.facebook.ads.AdSDKNotificationListener
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger
import io.appmetrica.analytics.modulesapi.internal.client.ClientContext

internal class FBAdRevenueDataListener(
    private val clientContext: ClientContext,
    private val converter: FBAdRevenueConverter = FBAdRevenueConverter(),
) : AdSDKNotificationListener {

    private val tag = "[FBAdRevenueDataListener]"

    override fun onAdEvent(eventType: String, bundle: Bundle) {
        DebugLogger.info(tag, "onAdEvent: eventType=$eventType")
        if (eventType == AdSDKNotificationListener.IMPRESSION_EVENT) {
            clientContext.internalClientModuleFacade.reportAdRevenue(converter.convert(bundle))
        }
    }
}
