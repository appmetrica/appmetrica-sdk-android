package io.appmetrica.analytics.adrevenue.applovin.v12.auto.impl

import android.os.Bundle
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger
import io.appmetrica.analytics.modulesapi.internal.client.ClientContext

internal class AppLovinIlrdReporter(
    private val clientContext: ClientContext,
    private val converter: AppLovinIlrdConverter = AppLovinIlrdConverter(),
) {

    private val tag = "[AppLovinIlrdReporter]"

    private val processedIds = ArrayDeque<String>()

    fun onIlrdReceived(id: String, bundle: Bundle) {
        val alreadySeen = synchronized(this) { isAlreadySeen(id) }
        if (alreadySeen) {
            DebugLogger.info(tag, "Duplicate ILRD event skipped: id=$id")
            return
        }
        try {
            val adRevenue = converter.convert(bundle)
            clientContext.internalClientModuleFacade.reportAdRevenue(adRevenue)
        } catch (e: Throwable) {
            DebugLogger.error(tag, e, "Failed to report ILRD ad revenue")
        }
    }

    private fun isAlreadySeen(id: String): Boolean {
        if (processedIds.contains(id)) return true
        if (processedIds.size >= Constants.DEDUPLICATION_CACHE_SIZE) processedIds.removeFirst()
        processedIds.addLast(id)
        return false
    }
}
