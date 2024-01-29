package io.appmetrica.analytics.impl.preloadinfo

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.text.TextUtils
import io.appmetrica.analytics.coreutils.internal.logger.YLogger
import io.appmetrica.analytics.impl.DistributionSource
import io.appmetrica.analytics.impl.SatelliteDataProvider
import io.appmetrica.analytics.impl.SdkUtils
import io.appmetrica.analytics.impl.Utils
import org.json.JSONObject

class PreloadInfoFromSatelliteProvider(private val context: Context) : SatelliteDataProvider<PreloadInfoState?> {
    private val tag = "[PreloadInfoFromSatelliteProvider]"

    private val uri = "content://com.yandex.preinstallsatellite.appmetrica.provider/preload_info"
    private val columnNameTrackingId = "tracking_id"
    private val columnNameAdditionalParameters = "additional_parameters"

    override fun invoke(): PreloadInfoState? {
        var cursor: Cursor? = null
        try {
            val contentResolver = context.contentResolver
            cursor = contentResolver.query(Uri.parse(uri), null, null, null, null)
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    val trackingId = cursor.getString(cursor.getColumnIndexOrThrow(columnNameTrackingId))
                    val additionalParamsColumn = cursor.getColumnIndexOrThrow(columnNameAdditionalParameters)
                    val additionalParams = parseAdditionalParams(cursor.getString(additionalParamsColumn))

                    YLogger.info(tag, "Parsed tracking id: $trackingId, additional parameters: $additionalParams")

                    SdkUtils.logAttribution(
                        "Preload info from Satellite: {tracking id = %s, additional parameters = %s}",
                        trackingId,
                        additionalParams
                    )

                    return PreloadInfoState(
                        trackingId,
                        additionalParams,
                        !TextUtils.isEmpty(trackingId),
                        false,
                        DistributionSource.SATELLITE
                    )
                } else {
                    SdkUtils.logAttribution("No Preload Info data in Satellite content provider")
                }
            } else {
                YLogger.info(tag, "Failed to retrieve cursor")
                SdkUtils.logAttribution("No Satellite content provider found")
            }
        } catch (ex: Throwable) {
            YLogger.error(tag, ex)
        } finally {
            Utils.closeCursor(cursor)
        }
        return null
    }

    private fun parseAdditionalParams(additionalParamsString: String?): JSONObject = try {
        if (additionalParamsString?.isEmpty() != false) JSONObject() else JSONObject(additionalParamsString)
    } catch (ex: Throwable) {
        YLogger.error(tag, ex, "Could not parse additional parameters")
        JSONObject()
    }
}
