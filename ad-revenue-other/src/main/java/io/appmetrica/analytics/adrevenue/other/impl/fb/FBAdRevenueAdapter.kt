package io.appmetrica.analytics.adrevenue.other.impl.fb

import com.facebook.ads.AdSDKNotificationManager
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger
import io.appmetrica.analytics.modulesapi.internal.client.ClientContext

internal class FBAdRevenueAdapter {

    private val tag = "[FBAdRevenueAdapter]"

    private var listener: FBAdRevenueDataListener? = null

    fun registerListener(clientContext: ClientContext) {
        DebugLogger.info(tag, "registerListener")
        if (listener != null) return
        listener = FBAdRevenueDataListener(clientContext).also {
            AdSDKNotificationManager.addSDKNotificationListener(it)
        }
    }

    fun unregisterListener() {
        DebugLogger.info(tag, "unregisterListener")
        listener?.let { AdSDKNotificationManager.removeSDKNotificationListener(it) }
        listener = null
    }
}
