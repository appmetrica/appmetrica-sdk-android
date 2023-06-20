package io.appmetrica.analytics.impl.preloadinfo

import android.content.ContentValues
import android.text.TextUtils
import io.appmetrica.analytics.coreutils.internal.logger.YLogger
import io.appmetrica.analytics.impl.ContentProviderDataParser
import io.appmetrica.analytics.impl.DistributionSource
import io.appmetrica.analytics.impl.SdkUtils
import org.json.JSONObject

private const val TAG = "[PreloadInfoDataParser]"
const val KEY_TRACKING_ID = "tracking_id"
const val KEY_ADDITIONAL_PARAMS = "additional_params"

internal class PreloadInfoDataParser : ContentProviderDataParser<PreloadInfoState> {

    override fun invoke(values: ContentValues): PreloadInfoState? {
        val trackingId = values.getAsString(KEY_TRACKING_ID)
        if (TextUtils.isEmpty(trackingId)) {
            SdkUtils.logAttributionW("Tracking id is empty")
            return null
        }
        try {
            val additionalParamsString = values.getAsString(KEY_ADDITIONAL_PARAMS)
            if (TextUtils.isEmpty(additionalParamsString)) {
                SdkUtils.logAttributionW("No additional params")
                return null
            }
            val additionalParams = JSONObject(additionalParamsString)
            if (additionalParams.length() == 0) {
                SdkUtils.logAttributionW("Additional params are empty")
                return null
            }
            SdkUtils.logAttribution(
                "Successfully parsed preload info. Tracking id = %s, additionalParams = %s",
                trackingId, additionalParams
            )
            return PreloadInfoState(
                trackingId,
                additionalParams,
                true,
                false,
                DistributionSource.RETAIL
            )
        } catch (ex: Throwable) {
            YLogger.error(TAG, ex)
            SdkUtils.logAttributionE(ex, "Could not parse additional parameters")
        }
        return null
    }
}
