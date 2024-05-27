package io.appmetrica.analytics.impl.preloadinfo

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.text.TextUtils
import io.appmetrica.analytics.coreutils.internal.parsing.ParseUtils
import io.appmetrica.analytics.coreutils.internal.services.PackageManagerUtils
import io.appmetrica.analytics.impl.DistributionSource
import io.appmetrica.analytics.impl.SatelliteDataProvider
import io.appmetrica.analytics.impl.SdkUtils
import io.appmetrica.analytics.impl.Utils
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger
import org.json.JSONObject

class PreloadInfoFromSatelliteProvider(private val context: Context) : SatelliteDataProvider<PreloadInfoState?> {
    private val tag = "[PreloadInfoFromSatelliteProvider]"

    private val uri = "content://$authority/preload_info"
    private val columnNameTrackingId = "tracking_id"
    private val columnNameAdditionalParameters = "additional_parameters"

    override fun invoke(): PreloadInfoState? {
        if (!PackageManagerUtils.hasContentProvider(context, authority)) {
            SdkUtils.logAttribution("Satellite content provider with preload info was not found.")
            return null
        }
        var cursor: Cursor? = null
        try {
            val contentResolver = context.contentResolver
            cursor = contentResolver.query(Uri.parse(uri), null, null, null, null)
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    val trackingId = cursor.getString(cursor.getColumnIndexOrThrow(columnNameTrackingId))
                    val additionalParamsColumn = cursor.getColumnIndexOrThrow(columnNameAdditionalParameters)
                    val additionalParams = parseAdditionalParams(cursor.getString(additionalParamsColumn))

                    DebugLogger.info(
                        tag,
                        "Parsed tracking id: $trackingId, additional parameters: $additionalParams"
                    )

                    if (TextUtils.isEmpty(trackingId) || ParseUtils.parseLong(trackingId) != null) {
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
                        SdkUtils.logAttributionW("Tracking id from Satellite is not a number.")
                    }
                } else {
                    SdkUtils.logAttribution("No Preload Info data in Satellite content provider")
                }
            } else {
                SdkUtils.logAttribution("No Satellite content provider found")
            }
        } catch (ex: Throwable) {
            DebugLogger.error(tag, ex)
        } finally {
            Utils.closeCursor(cursor)
        }
        return null
    }

    private fun parseAdditionalParams(additionalParamsString: String?): JSONObject = try {
        if (additionalParamsString?.isEmpty() != false) JSONObject() else JSONObject(additionalParamsString)
    } catch (ex: Throwable) {
        DebugLogger.error(tag, ex, "Could not parse additional parameters")
        JSONObject()
    }
}
