package io.appmetrica.analytics.impl.preloadinfo

import android.content.ContentValues
import io.appmetrica.analytics.coreutils.internal.StringUtils
import io.appmetrica.analytics.coreutils.internal.parsing.ParseUtils
import io.appmetrica.analytics.impl.ContentProviderDataParser
import io.appmetrica.analytics.impl.DistributionSource
import io.appmetrica.analytics.impl.SdkUtils
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger
import org.json.JSONObject

private const val KEY_TRACKING_ID = "tracking_id"
private const val KEY_ADDITIONAL_PARAMS = "additional_params"

internal class PreloadInfoDataParser : ContentProviderDataParser<PreloadInfoState> {

    private val tag = "[PreloadInfoDataParser]"

    override fun invoke(values: ContentValues): PreloadInfoState? {
        val trackingId = values.getAsString(KEY_TRACKING_ID)
        if (StringUtils.isNullOrEmpty(trackingId)) {
            SdkUtils.logAttribution("Tracking id is empty")
            return null
        }
        if (ParseUtils.parseLong(trackingId) == null) {
            SdkUtils.logAttribution("Tracking id from preload info content provider is not a number")
            return null
        }
        try {
            val additionalParamsString = values.getAsString(KEY_ADDITIONAL_PARAMS)
            if (StringUtils.isNullOrEmpty(additionalParamsString)) {
                SdkUtils.logAttribution("No additional params")
                return null
            }
            val additionalParams = JSONObject(additionalParamsString)
            if (additionalParams.length() == 0) {
                SdkUtils.logAttribution("Additional params are empty")
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
            DebugLogger.error(tag, ex)
            SdkUtils.logAttributionE(ex, "Could not parse additional parameters")
        }
        return null
    }
}
