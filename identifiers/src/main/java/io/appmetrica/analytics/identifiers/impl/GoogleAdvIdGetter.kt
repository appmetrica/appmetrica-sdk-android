package io.appmetrica.analytics.identifiers.impl

import android.content.Context
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import io.appmetrica.analytics.coreapi.internal.identifiers.IdentifierStatus
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger

private const val TAG = "[GoogleAdvIdGetter]"

internal class GoogleAdvIdGetter : AdvIdProvider {

    override fun getAdTrackingInfo(context: Context): AdvIdResult {
        return try {
            val adInfo = AdvertisingIdClient.getAdvertisingIdInfo(context)
            DebugLogger.info(TAG, "obtained info $adInfo")
            AdvIdResult(
                IdentifierStatus.OK,
                AdvIdInfo(Constants.Providers.GOOGLE, adInfo.id, adInfo.isLimitAdTrackingEnabled)
            )
        } catch (e: GooglePlayServicesNotAvailableException) {
            DebugLogger.info(TAG, "AdvertisingIdProvider are not available")
            AdvIdResult(
                IdentifierStatus.IDENTIFIER_PROVIDER_UNAVAILABLE,
                errorExplanation = "could not resolve google services"
            )
        } catch (t: Throwable) {
            DebugLogger.error(TAG, t, "can't fetch adv id")
            AdvIdResult(
                IdentifierStatus.UNKNOWN,
                errorExplanation = "exception while fetching google adv_id: " + t.message
            )
        }
    }
}
