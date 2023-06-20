package io.appmetrica.analytics.identifiers.impl

import android.content.Context
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import io.appmetrica.analytics.coreapi.internal.identifiers.IdentifierStatus
import io.appmetrica.analytics.coreutils.internal.logger.YLogger

private const val TAG = "[GoogleAdvIdGetter]"

internal class GoogleAdvIdGetter : AdvIdProvider {

    override fun getAdTrackingInfo(context: Context): AdsIdResult {
        return try {
            val adInfo = AdvertisingIdClient.getAdvertisingIdInfo(context)
            YLogger.debug(TAG, "obtained info $adInfo")
            AdsIdResult(
                IdentifierStatus.OK,
                AdsIdInfo(Constants.Providers.GOOGLE, adInfo.id, adInfo.isLimitAdTrackingEnabled)
            )
        } catch (e: GooglePlayServicesNotAvailableException) {
            YLogger.debug(TAG, "AdvertisingIdProvider are not available")
            AdsIdResult(
                IdentifierStatus.IDENTIFIER_PROVIDER_UNAVAILABLE,
                errorExplanation = "could not resolve google services"
            )
        } catch (t: Throwable) {
            YLogger.error(TAG, t, "can't fetch adv id")
            AdsIdResult(
                IdentifierStatus.UNKNOWN,
                errorExplanation = "exception while fetching google adv_id: " + t.message
            )
        }
    }
}
