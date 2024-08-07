package io.appmetrica.analytics.impl.clids

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.text.TextUtils
import io.appmetrica.analytics.coreutils.internal.services.PackageManagerUtils
import io.appmetrica.analytics.impl.DistributionSource
import io.appmetrica.analytics.impl.SatelliteDataProvider
import io.appmetrica.analytics.impl.SdkUtils
import io.appmetrica.analytics.impl.Utils
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger

internal class SatelliteClidsInfoProvider(
    private val context: Context
) : SatelliteDataProvider<ClidsInfo.Candidate?> {
    private val tag = "[SatelliteClidsInfoProvider]"

    private val uri = "content://$authority/clids"
    private val columnClidKey = "clid_key"
    private val columnClidValue = "clid_value"

    override fun invoke(): ClidsInfo.Candidate? {
        if (!PackageManagerUtils.hasContentProvider(context, authority)) {
            SdkUtils.logAttribution("Satellite content provider with clids was not found.")
            return null
        }
        var cursor: Cursor? = null
        try {
            val contentResolver = context.contentResolver
            cursor = contentResolver.query(Uri.parse(uri), null, null, null, null)
            if (cursor != null) {
                val clidsFromSatellite = mutableMapOf<String, String>()
                while (cursor.moveToNext()) {
                    try {
                        val clidKey = cursor.getString(cursor.getColumnIndexOrThrow(columnClidKey))
                        val clidValue = cursor.getString(cursor.getColumnIndexOrThrow(columnClidValue))
                        if (!TextUtils.isEmpty(clidKey) && !TextUtils.isEmpty(clidValue)) {
                            clidsFromSatellite[clidKey] = clidValue
                        } else {
                            SdkUtils.logAttribution("Invalid clid {%s : %s}", clidKey, clidValue)
                        }
                    } catch (ex: Throwable) {
                        DebugLogger.error(tag, ex)
                    }
                }
                SdkUtils.logAttribution("Clids from satellite: %s", clidsFromSatellite)
                return ClidsInfo.Candidate(clidsFromSatellite, DistributionSource.SATELLITE)
            } else {
                SdkUtils.logAttribution("No Satellite content provider found")
            }
        } catch (ex: Throwable) {
            DebugLogger.error(tag, ex)
            SdkUtils.logAttributionE(ex, "Error while getting satellite clids")
        } finally {
            Utils.closeCursor(cursor)
        }
        return null
    }
}
